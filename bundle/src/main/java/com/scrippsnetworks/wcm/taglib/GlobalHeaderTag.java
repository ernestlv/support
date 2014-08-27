package com.scrippsnetworks.wcm.taglib;

import com.scrippsnetworks.wcm.page.SniPage;
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
 *
 * @author Patrick Armstrong
 *         Date: 8/12/2013
 */
public class GlobalHeaderTag extends SafeSlingIncludeTag {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(GlobalHeaderTag.class);

    /** currentSniPage */
    private SniPage sniPage;
    
    /** The validity of the global header, used to determine whether or not to render. */
    private boolean valid;
    
    /** The variable name to write back into the page context telling the validity of the global header. In the case it's invalid, the static placeholder will be used. */
    private String validVarName;

    private Resource globalHeaderResource;

    private boolean isMobile = false;

     /** Inject a global header resource into the request. */
    @Override
    public int doStartTag() throws JspException {
        valid = false;
        if (sniPage != null) {
            GlobalHeader globalHeader = new GlobalHeaderFactory()
                    .withSniPage(sniPage)
                    .withIsMobile(isMobile)
                    .build();
            globalHeaderResource = globalHeader.getGlobalHeaderResource();
            valid = globalHeader.isValid();
            if (valid) {
                super.setResource(globalHeaderResource);
                super.setResourceType(globalHeaderResource.getResourceType());
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
