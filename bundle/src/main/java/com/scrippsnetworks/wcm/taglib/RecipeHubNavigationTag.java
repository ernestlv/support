package com.scrippsnetworks.wcm.taglib;

import com.scrippsnetworks.wcm.page.SniPage;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/** Hub navigation tag just for Recipe pages.
 * Because Recipes are very special.
 * @author Jason Clark
 *         Date: 7/8/13
 */
public class RecipeHubNavigationTag extends TagSupport {

    /** Should be the SniPage for the Recipe you're currently on. */
    private SniPage sniPage;

    @Override
    public int doStartTag() throws JspException {
        return super.doStartTag();
    }

    @Override
    public int doEndTag() throws JspException {
        return super.doEndTag();
    }

    /** Public setter for sniPage. */
    public void setSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
    }
}
