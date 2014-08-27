package com.scrippsnetworks.wcm.taglib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import com.scrippsnetworks.wcm.metadata.impl.MetadataUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.scripting.jsp.util.TagUtil;

public class AnchorTag extends TagSupport {

    private static final long serialVersionUID = 1L;
    
    private String href;
    private String cssClass;
    private String title;
    private String rel;
    private boolean recordEvent;
    private String linkPosition;
    private String target;
    private String fragmentId;
    private String dataToggle;
    private String dataTarget;
    private String itemProp;
    
    public int doStartTag() throws JspException {
        final SlingHttpServletRequest slingRequest = TagUtil.getRequest(pageContext);
        final Resource resource = slingRequest.getResource();
        final String resourceType = resource.getResourceType();
        final String componentName = StringUtils.substringAfterLast(resourceType, "/");
        final String path = resource.getPath();
        
        if (href != null && href.length() > 0) {
            try {
                JspWriter out = pageContext.getOut();
                StringBuilder sb = new StringBuilder();

                sb.append("<a href=\"");
                sb.append(TagUtils.completeHREF(MetadataUtil.getSourcePagePath(href)));
                if (fragmentId != null && fragmentId.length() > 0) {
                    sb.append("#");
                    sb.append(StringEscapeUtils.escapeHtml(fragmentId));
                }
                sb.append("\"");
                if (cssClass != null && cssClass.length() > 0) {
                    sb.append(" class=\"");
                    sb.append(StringEscapeUtils.escapeHtml(cssClass));
                    sb.append("\" ");
                }
                if (title != null && title.length() > 0) {
                    sb.append(" title=\"");
                    sb.append(StringEscapeUtils.escapeHtml(title));
                    sb.append("\" ");
                }
                if (rel != null && rel.length() > 0) {
                    sb.append(" rel=\"");
                    sb.append(StringEscapeUtils.escapeHtml(rel));
                    sb.append("\" ");
                }
                if (target != null && target.length() > 0) {
                    sb.append(" target=\"");
                    sb.append(StringEscapeUtils.escapeHtml(target));
                    sb.append("\"");
                }
                if (dataToggle != null && dataToggle.length() > 0) {
                    sb.append(" data-toggle=\"");
                    sb.append(StringEscapeUtils.escapeHtml(dataToggle));
                    sb.append("\" ");
    			}
                if (dataTarget != null && dataTarget.length() > 0) {
                    sb.append(" data-target=\"");
                    sb.append(StringEscapeUtils.escapeHtml(dataTarget));
                    sb.append("\" ");
                }
                if (itemProp != null && itemProp.length() > 0) {
                    sb.append(" itemprop=\"");
                    sb.append(StringEscapeUtils.escapeHtml(itemProp));
                    sb.append("\" ");
                }
                if (recordEvent) {
                    sb.append(" onclick=\"CQ_Analytics.record({");
                    sb.append("event: 'linkclicked',");
                    sb.append("values: {");
                    sb.append("componentPath: '").append(resourceType).append("',");
                    sb.append("componentName: '").append(componentName).append("',");
                    sb.append("resourcePath: '").append(path).append("'");
                    if (linkPosition != null && linkPosition.length() > 0) {
                        sb.append(",linkPosition: '").append(linkPosition).append("'");
                    }
                    sb.append("}, options: { obj: this },");
                    sb.append("componentPath: '").append(resourceType).append("'");
                    sb.append("});\"");
                }
                sb.append(">");
                out.print(sb.toString());

            } catch (IOException ioe) {
                throw new JspException(ioe.getMessage());
            }
        }
        return EVAL_BODY_INCLUDE;
    }
    
    public int doEndTag() throws JspException {
        if (href != null && href.length() > 0) {
            try {
                JspWriter out = pageContext.getOut();
                out.print("</a>");
            } catch (IOException ioe) {
                throw new JspException(ioe.getMessage());
            }
        }    
        return SKIP_BODY;
    }
    
    public void setHref (String pHref) {
        href = pHref;
    }
    
    public void setCssClass (String pCssClass) {
        cssClass = pCssClass;
    }
    
    public void setTitle (String pTitle) {
        title = pTitle;
    }

    public void setRel (String pRel) {
        rel = pRel;
    }
    
    public void setRecordEvent (boolean pRecordEvent) {
        recordEvent = pRecordEvent;
    }
    
    public void setLinkPosition (String pLinkPosition) {
        linkPosition = pLinkPosition;
    }

    public void setTarget(String pTarget) {
        target = pTarget;
    }
    
    public void setFragmentId(String pFragmentId) {
        fragmentId = pFragmentId;
    } 

    public void setDataTarget(String dataTarget) {
        this.dataTarget = dataTarget;
    }

    public void setDataToggle(String dataToggle) {
        this.dataToggle = dataToggle;
    }

    public void setItemProp(String pItemProp) {
        this.itemProp = pItemProp;
    }

}
