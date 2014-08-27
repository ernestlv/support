package com.scrippsnetworks.wcm.taglib;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Generic tag that accepts a tag name and writes a tag with that name.
 * @author Jason Clark
 *         Date: 5/6/13
 */
public class SniTag extends TagSupport {

    private Logger log = LoggerFactory.getLogger(SniTag.class);

    /** name of tag to be rendered */
    private String tagName;
    /** css class */
    private String cssClass;
    /** id */
    private String id;

    private JspWriter out;

    /**
     *
     * @return
     * @throws JspException
     */
    @Override
    public int doStartTag() throws JspException {
        try {
            out = pageContext.getOut();
            StringBuilder builder = new StringBuilder();
            if (StringUtils.isNotBlank(tagName)) {
                builder
                        .append("<")
                        .append(tagName);
                if (StringUtils.isNotBlank(cssClass)) {
                    builder
                            .append(" class=\"")
                            .append(cssClass)
                            .append("\"");
                }
                if (StringUtils.isNotBlank(id)) {
                    builder
                            .append(" id=\"")
                            .append(id)
                            .append("\"");
                }
                builder.append(">");
                out.write(builder.toString());
            }
        } catch (Exception e) {
            log.error("Exception in SniTag" + e.getMessage());
            throw new JspException(e);
        }
        return EVAL_BODY_INCLUDE;
    }

    /**
     *
     * @return
     * @throws JspException
     */
    @Override
    public int doEndTag() throws JspException {
        try {
            if (out != null && StringUtils.isNotBlank(tagName)) {
                StringBuilder builder = new StringBuilder();
                builder
                        .append("</")
                        .append(tagName)
                        .append(">");
                out.write(builder.toString());
            }
        } catch (Exception e) {
            throw new JspException(e);
        }
        return SKIP_BODY;
    }

    /**
     * Setter for tagName
     * @param tagName String tag to be rendered
     */
    public void setTagName(final String tagName) {
        this.tagName = tagName;
    }

    /**
     * Setter for cssClass
     * @param cssClass String optional CSS class(es)
     */
    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    /**
     * Setter for id
     * @param id String id
     */
    public void setId(String id) {
        this.id = id;
    }
}
