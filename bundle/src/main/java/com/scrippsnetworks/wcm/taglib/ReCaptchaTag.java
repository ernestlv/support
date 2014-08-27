package com.scrippsnetworks.wcm.taglib;

import com.scrippsnetworks.wcm.credentialstore.CredentialStore;
import com.scrippsnetworks.wcm.credentialstore.CredentialStoreService;
import com.scrippsnetworks.wcm.page.SniPage;

import java.io.IOException;
import java.util.Properties;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.scripting.jsp.util.TagUtil;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;

public class ReCaptchaTag extends TagSupport {

    private static final long serialVersionUID = 1L;
    private static final String ATTRIBUTE_PAGE = "currentSniPage";
    private static final String EMPTY_STRING = "";
    private static final String KEY_PUBLIC_KEY = "recaptcha.publicKey";
    private static final String KEY_PRIVATE_KEY = "recaptcha.privateKey";

    private boolean noscript = false;
    private CredentialStoreService credentialStoreService;
    private String theme;
    private String lang;
    private String widgetId;
    private Integer tabIndex = 0;

    public int doStartTag() throws JspException {
        try {
            JspWriter out = pageContext.getOut();
            String challengeHtml = getChallengeHtml();
            out.write(challengeHtml);
        } catch (IOException ioe) {
            throw new JspException("Error: " + ioe.getMessage());
        }
        return SKIP_BODY;
    }
    
    public int doEndTag() throws JspException {
        return SKIP_BODY;
    }

    public void setNoscript(boolean pNoscript) {
        noscript = pNoscript;
    }

    public void setTheme(String pTheme) {
        theme = pTheme;
    }

    public void setLang(String pLang) {
        lang = pLang;
    }

    public void setWidgetId(String pWidgetId) {
        widgetId = pWidgetId;
    }

    public void setTabIndex(int pTabIndex) {
        tabIndex = pTabIndex;
    }

    private String getBrand() {
        SlingHttpServletRequest request = TagUtil.getRequest(this.pageContext);
        SniPage page = (SniPage)request.getAttribute(ATTRIBUTE_PAGE);
        return page.getBrand();
    }

    private String getChallengeHtml() {
        String challengeHtml = EMPTY_STRING;
        SlingHttpServletRequest request = TagUtil.getRequest(this.pageContext);
        SniPage page = (SniPage)request.getAttribute(ATTRIBUTE_PAGE);

        CredentialStoreService cs = getCredentialStoreService();

        if (cs != null) {
            ReCaptcha rc = ReCaptchaFactory.newReCaptcha(
                cs.getCredential(getBrand(), KEY_PUBLIC_KEY),
                cs.getCredential(getBrand(), KEY_PRIVATE_KEY),
                noscript);
            Properties options = getProperties();
            
            challengeHtml = rc.createRecaptchaHtml(null, options);
        }

        return challengeHtml;
    }

    private Properties getProperties() {
        Properties props = new Properties();

        if (StringUtils.isNotBlank(theme)) {
            props.setProperty("theme", theme);
        }
        if (StringUtils.isNotBlank(lang)) {
            props.setProperty("lang", lang);
        }
        if (StringUtils.isNotBlank(widgetId)) {
            props.setProperty("custom_theme_widget", widgetId);
        }
        if (tabIndex != 0) {
            props.setProperty("tabindex", tabIndex.toString());
        }

        return props;
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
