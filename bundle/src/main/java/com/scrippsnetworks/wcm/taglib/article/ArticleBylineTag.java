package com.scrippsnetworks.wcm.taglib.article;

import com.scrippsnetworks.wcm.taglib.TagUtils;
import com.scrippsnetworks.wcm.asset.DataUtil;
import com.scrippsnetworks.wcm.taglib.Functions;
import com.scrippsnetworks.wcm.util.NodeNames;
import com.scrippsnetworks.wcm.fnr.util.PageSlingResourceTypes;
import com.scrippsnetworks.wcm.util.PagePropertyNames;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * Tag for writing out a link to a Show page if an article is the child of that page
 * Expects a Sling Resource for the page in hand, which should be an article.
 * Checks the parent of that resource to see if it is a show page, if so writes out a link
 * to that page.
 *
 * @author Jason Clark
 * Date: 8/1/12
 */
public class ArticleBylineTag extends TagSupport {

    private Resource resource;
	private String openTags;
	private String closeTags;
	
    public int doStartTag() throws JspException {
        if (resource == null) {
            return SKIP_BODY;
        }
        JspWriter writer = pageContext.getOut();
        StringBuilder output = new StringBuilder();

        Resource parentResource = resource.getResourceResolver()
                .getResource(Functions.getBasePath(resource.getPath())).getParent();
        if (parentResource == null) {
            return SKIP_BODY;
        }

        String parentPagePath = Functions.getBasePath(parentResource.getPath());
        if (parentResource.getName().equals(NodeNames.JCR_CONTENT.nodeName())) {
            output.append(doWriteParentPageLink(parentResource, parentPagePath));
        } else {
            Resource parentContentResource = parentResource.getChild(NodeNames.JCR_CONTENT.nodeName());
            if (parentContentResource == null) {
                return SKIP_BODY;
            }
            output.append(doWriteParentPageLink(parentContentResource, parentPagePath));
        }

        try {
            writer.print(output);
        } catch (IOException ioe) {
            throw new JspException(ioe);
        }

        return SKIP_BODY;
    }

    /**
     * util method to format link for output, currently will link to show page if that is the parent
     * otherwise will return the value for the byline property
     * @param testResource resource for jcr:content node of parent page
     * @param pagePath String path to page, in case
     * @return
     */
    private String doWriteParentPageLink(final Resource testResource, final String pagePath) {
        if (DataUtil.valueMapIsType(ResourceUtil.getValueMap(testResource),
                PageSlingResourceTypes.SHOW.resourceType())  || 
                DataUtil.valueMapIsType(ResourceUtil.getValueMap(testResource),
                        PageSlingResourceTypes.SHOW_MOBILE.resourceType())) {
            StringBuilder output = new StringBuilder();
            output.append("<strong>Show: <a href=\"");
            output.append(TagUtils.completeHREF(pagePath));
            output.append("\">");
            output.append(ResourceUtil.getValueMap(testResource)
                    .get(PagePropertyNames.JCR_TITLE.propertyName(), String.class));
            output.append("</a></strong>");
            return output.toString();
        } else {
            try {
                String byline = ResourceUtil.getValueMap(resource).get("byline").toString();
                if (byline.trim().length() > 0 && !byline.contains("<br")) {
					byline = "By " + byline;
					//If we were passed the optional parameters, out put them as well.
					if(openTags != null)
					{
						byline = openTags + byline;
					}
					if(closeTags != null)
					{
						byline = byline + closeTags;
					}	
                    return byline;
                } else {
                    return byline;
                }
				
            } catch (NullPointerException npe) {
                return "";
            }
        }
    }

    public int doEndTag() {
        return SKIP_BODY;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }
	
	public void setOpenTags(String openTags) {
        this.openTags = openTags;
    }
	
	public void setCloseTags(String closeTags) {
        this.closeTags = closeTags;
    }
}
