package com.scrippsnetworks.wcm.taglib;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.regions.Footer;
import com.scrippsnetworks.wcm.regions.FooterFactory;
import com.scrippsnetworks.wcm.regions.GlobalHeader;
import com.scrippsnetworks.wcm.regions.GlobalHeaderFactory;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspException;
import java.io.IOException;

/**
 * TODO make generic sni:region tag
 *
 * @author Ken Shih
 *         Date: 8/23/2013
 */
public class FooterTag extends SafeSlingIncludeTag {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(FooterTag.class);

    /** currentSniPage */
    private SniPage sniPage;
    
    /** The validity of the footer, used to determine whether or not to render. */
    private boolean valid;
    
    /** The variable name to write back into the page context telling the validity of the footer. In the case it's invalid, the static placeholder will be used. */
    private String validVarName;

    private Resource footerResource;

    private boolean isMobile = false;

     /** Inject a global header resource into the request. */
    @Override
    public int doStartTag() throws JspException {
        valid = false;
        if (sniPage != null) {
            Footer footer = new FooterFactory()
                    .withSniPage(sniPage)
                    .withIsMobile(isMobile)
                    .build();
            footerResource = footer.getResource();
            valid = footer.isValid();
            if (valid) {
                super.setResource(footerResource);
                super.setResourceType(footerResource.getResourceType());
            }
        }
        pageContext.setAttribute(validVarName, valid);
        return super.doStartTag();
    }
    
    /** Overridden to allow decision about whether or not to render. */
    @Override
    protected void dispatch(RequestDispatcher dispatcher,
                            ServletRequest request,
                            ServletResponse response)
            throws IOException, ServletException {
        if (valid) {
            super.dispatch(dispatcher, request, response);
        }
    }

    /** Set an SniPage to generate a global header. */
    public void setSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
    }
    
    /** The variable name to store the validity of the global header into. */
    public void setValidVarName(String pValidVarName) {
        this.validVarName = pValidVarName;
    }

    public void setIsMobile(String pIsMobile) {
        this.isMobile = Boolean.parseBoolean(pIsMobile);
    }
}
