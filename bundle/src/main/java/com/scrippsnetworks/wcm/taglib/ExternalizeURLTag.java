package com.scrippsnetworks.wcm.taglib;


import com.day.cq.commons.Externalizer;
import java.io.IOException;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;

import javax.servlet.jsp.JspWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.api.scripting.SlingBindings;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Patrick Armstrong
 */
public class ExternalizeURLTag extends TagSupport {

    private SlingScriptHelper sling;
    private Externalizer externalizer;
    private ResourceResolver resourceResolver;
    
    private String value;

    protected static Logger logger = LoggerFactory.getLogger(ExternalizeURLTag.class);

    @Override
    public int doEndTag() throws JspException {
        SlingBindings bindings = (SlingBindings)this.pageContext.getRequest().getAttribute(SlingBindings.class.getName());
        sling = bindings.getSling();
        externalizer = sling.getService(Externalizer.class);
        
        resourceResolver = (ResourceResolver)this.pageContext.getAttribute("resourceResolver");
        
        JspWriter out = pageContext.getOut();
        StringBuilder sb = new StringBuilder();
        
        if (externalizer == null || StringUtils.isEmpty(value) || !value.startsWith("/")) {
            sb.append(value);
        } else {
            sb.append(externalizer.publishLink(resourceResolver, value));
        }
        
        try {
            out.write(sb.toString());
        } catch (IOException e) {
            throw new JspException(e);
        }
        
        return EVAL_PAGE;
    }
    
    public void setValue(String pValue) {
        this.value = pValue;
    }

}
