package com.scrippsnetworks.wcm.taglib;

import com.adobe.granite.xss.XSSAPI;
import com.scrippsnetworks.wcm.config.SiteConfigService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

public class EncodeXssForHtml extends TagSupport {
    private String text;
    public int doStartTag() throws JspException {
        JspWriter out = pageContext.getOut();
        BundleContext bundleContext = FrameworkUtil.getBundle(SiteConfigService.class).getBundleContext();
        ServiceReference serviceReference = bundleContext.getServiceReference(XSSAPI.class.getName());
        XSSAPI xssApi = null;
        if (serviceReference != null) {
            xssApi = (XSSAPI)bundleContext.getService(serviceReference);
        } else{
            return SKIP_BODY;
        }
        try{
            out.print(xssApi.encodeForHTML(text));
        } catch (IOException ioe) {
            throw new JspException(ioe.getMessage());
        }
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        return SKIP_BODY;
    }

    public void setText(String text) {
        this.text = text;
    }
}
