package com.scrippsnetworks.wcm.taglib;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.regions.RightRailFactory;
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
 * @author Jason Clark
 *         Date: 5/22/13
 */
public class RightRailTag extends SafeSlingIncludeTag {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(RightRailTag.class);

    /** currentSniPage */
    private SniPage sniPage;

    private Resource rightRailResource;

    /** Inject a right rail resource into the request. */
    @Override
    public int doStartTag() throws JspException {
        if (sniPage != null) {
            rightRailResource = new RightRailFactory()
                    .withSniPage(sniPage)
                    .build()
                    .getRightRailResource();
            if (rightRailResource != null) {
                super.setResource(rightRailResource);
                super.setResourceType(rightRailResource.getResourceType());
            }
        }
        return super.doStartTag();
    }

    /** Overridden to allow decision about whether or not to render. */
    @Override
    protected void dispatch(RequestDispatcher dispatcher,
                            ServletRequest request,
                            ServletResponse response)
            throws IOException, ServletException {
        if (rightRailResource != null) {
            super.dispatch(dispatcher, request, response);
        }
    }

    /** Set an SniPage to generate a right rail. */
    public void setSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
    }
}
