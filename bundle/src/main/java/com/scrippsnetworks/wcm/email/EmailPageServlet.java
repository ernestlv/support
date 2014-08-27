package com.scrippsnetworks.wcm.email;

import com.day.cq.commons.Externalizer;
import com.day.cq.commons.TidyJSONWriter;
import com.day.cq.commons.mail.MailTemplate;
import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.config.SiteConfigService;
import com.scrippsnetworks.wcm.config.impl.SiteConfigUtil;
import com.scrippsnetworks.wcm.credentialstore.CredentialStore;
import com.scrippsnetworks.wcm.credentialstore.CredentialStoreService;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;
import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.mail.HtmlEmail;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import com.day.cq.mailer.MessageGatewayService;
import javax.jcr.Session;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

@Component(name = "com.scrippsnetworks.wcm.email.EmailPageServlet", immediate = true, metatype = false, enabled = true )
@Service( value = javax.servlet.Servlet.class )
@Properties({
        @Property(name="sling.servlet.extensions",value={"html"}),
        @Property(name="sling.servlet.resourceTypes",value="sling/servlet/default"),
        @Property(name="sling.servlet.methods",value="GET"),
        @Property(name="sling.servlet.selectors",value="emailsend")
})
public class EmailPageServlet extends SlingAllMethodsServlet {

    private static final String KEY_PRIVATE_KEY = "recaptcha.privateKey";

    private static final Logger log = LoggerFactory.getLogger(EmailPageServlet.class);
    private static final long serialVersionUID = 1L;

    @Reference
    protected MessageGatewayService mailService;

    @Reference
    protected Externalizer externalizer;

    private CredentialStoreService credentialStoreService;

    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws IOException {

        boolean success = true;
        String message = "";

        try {

            //first validate the fields

            // from email
            String fromEmail = "";
            List<InternetAddress> fromEmails = new ArrayList<InternetAddress>();
            if(request.getParameter("fromemail")!=null) {
                fromEmail = request.getParameter("fromemail");
            }
            try{
                InternetAddress email =  new InternetAddress(fromEmail);
                email.validate();
                fromEmails.add(email);
            } catch (Exception ex){
                message+="From email is not valid<br/>";
                success=false;
            }

            // from name
            String fromName = request.getParameter("fromname");
            if(fromName==null || fromName.trim().isEmpty()) {
                //if we have a valid from email, use that as the from name
                if(success) {
                    fromName = fromEmail.substring(0,fromEmail.indexOf("@"));
                }
            }

            //to emails
            String recipientEmail = "";
            List<InternetAddress> recipientEmails = new ArrayList<InternetAddress>();;
            if(request.getParameter("recipientemail")!=null) {
                recipientEmail = request.getParameter("recipientemail");
            }
            if(recipientEmail=="") {
                message+="Recipient email is not valid<br/>";
                success=false;
            } else {
                String[] recipientEmailSplit = recipientEmail.split(",");
                for(String item : recipientEmailSplit) {
                    try{
                        InternetAddress email =  new InternetAddress(item);
                        email.validate();
                        InternetAddress [] emailToList = new InternetAddress [] {email};
                        recipientEmails.add(email);
                    } catch (Exception ex){
                        message+="Recipient email " + item + " is not valid<br/>";
                        success=false;
                    }
                }
            }

            // get SniPage from resource
            SniPage currentSniPage = PageFactory.getSniPage(request.getResource().adaptTo(Page.class));
            if(currentSniPage==null) {
                throw new Exception("SniPage is null");
            }
            // validate captcha
            CredentialStoreService cs = getCredentialStoreService();

            String remoteAddr = request.getRemoteAddr();
            log.error(remoteAddr);

            // so you can develop from hotel wifi :-(
            if(remoteAddr.equals("0:0:0:0:0:0:0:1")) {
                remoteAddr="127.0.0.1";
            }
            ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
            reCaptcha.setPrivateKey(cs.getCredential(currentSniPage.getBrand(), KEY_PRIVATE_KEY));
            String challenge = request.getParameter("recaptcha_challenge_field");
            String uresponse = request.getParameter("recaptcha_response_field");
            ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, uresponse);
            if (!reCaptchaResponse.isValid()) {
                success=false;
                message+="Captcha is incorrect<br/>";
            }

            //send email if there were not validation errors
            if(success) {
                //create mail template
                SiteConfigService siteConfig = SiteConfigUtil.getSiteConfigService(request);
                ResourceResolver resourceResolver = request.getResourceResolver();
                final MailTemplate mailTemplate = MailTemplate.create(siteConfig.getEmailFriendTemplatePath(), resourceResolver.adaptTo(Session.class));

                String userMessage = "";
                if(request.getParameter("message")!=null) {
                    userMessage = request.getParameter("message").trim();
                }
                currentSniPage = PageFactory.getSniPage(request.getResource().adaptTo(Page.class));

                // add params for email to map so we can inject into mail template
                Map<String, String> mailTokens = new HashMap<String, String>();
                mailTokens.put("fromName",fromName);
                mailTokens.put("fromEmail",fromEmail);
                mailTokens.put("message",userMessage);
                mailTokens.put("pageHeadline",currentSniPage.getTitle()==null ? "" : currentSniPage.getTitle());
                mailTokens.put("pageUrl",externalizer.publishLink(resourceResolver, currentSniPage.getUrl()));
                if (currentSniPage.getCanonicalImageUrl() != null) {
                    mailTokens.put("imagePath",externalizer.publishLink(resourceResolver, currentSniPage.getCanonicalImageUrl()));
                }
                mailTokens.put("pageDescription",currentSniPage.getDescription()==null ? "" : currentSniPage.getDescription());

                //create email body based on template
                HtmlEmail email = mailTemplate.getEmail(StrLookup.mapLookup(mailTokens),HtmlEmail.class);

                // set properties on email
                email.setCharset("utf-8");
                email.setSubject(String.format(siteConfig.getEmailFriendSubject(),fromName, currentSniPage.getTitle()));
                email.setTo(recipientEmails);
                // does this site want to use the from friendly name in the from address?  Different email clients may bring up this email address for that name going forward
                if(siteConfig.useEmailFriendFromNameInFromAddress()) {
                    email.setFrom(siteConfig.getEmailFriendFromEmail(), fromName);
                } else {
                    email.setFrom(siteConfig.getEmailFriendFromEmail());
                }
                email.setReplyTo(fromEmails);

                // send email using cq mail gateway
                if(mailService != null && mailService.getGateway(HtmlEmail.class) != null) {
                    mailService.getGateway(HtmlEmail.class).send(email);
                    success=true;
                } else {
                    throw new Exception("mail service was null in email article to a friend");
                }
            }
        } catch (Exception ex) {
            success=false;
            message = "An error occurred sending your email, please try again later<br/>";
            log.error("Error emailing article to a friend",ex);
        }

        // create json response
        try {
            TidyJSONWriter writer = new TidyJSONWriter(response.getWriter());
            writer.object();
            writer.key("success").value(success);
            writer.key("message").value(message);
            writer.endObject();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
        } catch (Exception ex) {
            log.error("Error generating json response for email article to friend",ex);
        }
    }

    private CredentialStoreService getCredentialStoreService() {
        if (credentialStoreService == null) {
            BundleContext bundle = FrameworkUtil.getBundle(CredentialStoreService.class).getBundleContext();
            ServiceReference serviceReference = bundle.getServiceReference(CredentialStoreService.class.getName());
            if (serviceReference != null) {
                credentialStoreService = (CredentialStoreService)bundle.getService(serviceReference);
            }
        }

        return credentialStoreService;
    }

}
