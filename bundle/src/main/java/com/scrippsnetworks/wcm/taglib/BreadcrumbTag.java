package com.scrippsnetworks.wcm.taglib;

import org.apache.sling.scripting.jsp.util.TagUtil;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspWriter;

/**
 * This tag accepts an Breadcrumb object and writes out the breadcrumb for the page
 * <sni:breadcrumb breadcrumb="${currentSniPage.breadcrumb}" />
 * @author Rahul Anand
 *         Date: 5/06/13
 * @deprecated this is deprecated in favor of breadcrumb.jsp (Ken Shih 7/18/2013)
 */
@Deprecated
public class BreadcrumbTag extends TagSupport {

    @Override
    public int doStartTag() {
        return SKIP_BODY;
    }

    @Override
    public int doEndTag() {
        return SKIP_BODY;
    }
    
}
