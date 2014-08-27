package com.scrippsnetworks.wcm.article.impl;

import java.util.Iterator;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.article.Article;
import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.image.SniImageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.util.ContentRegionNames;
import com.scrippsnetworks.wcm.util.DialogPropertyNames;
import com.scrippsnetworks.wcm.util.PagePropertyNames;

/**
 * @author Jason Clark Date: 4/29/13
 * @updated Ken Shih 7/5/13
 * @updated Venkata Naga Sudheer Donaboina 9/12/13
 */
public class ArticleImpl implements Article {

    private static final Logger LOG = LoggerFactory.getLogger(ArticleImpl.class);

    private SniImage firstImage;
    private SniPage page;

    private static final String IMAGE_RESOURCE_TYPE = "sni-food/components/modules/article-image";
    private static final String RESOURCE_TYPE_TITLE_BLOCK =
            "sni-food/components/pagetypes/article-simple/components/title-block";
    private static final String RESOURCE_TYPE_RICH_TEXT_EDITOR = "sni-food/components/pagetypes/article-simple/components/rich-text-editor";
    private static final String IMAGE_PATH = "image/fileReference";

    private String byline;

    private String articleBody;
    
    /** Resource for convenience, because you need a Resource from time to time. */
    private Resource resource;
    
    private Resource contentWellResource;
    private Resource contentWellPaginatedResource;

    public ArticleImpl() {
    }

    public ArticleImpl(final SniPage page) {
        this.page = page;
        resource = page.getContentResource();
    }

    @Override
    public SniImage getFirstImage() {
        if (firstImage == null) {
            Iterator < Resource > childItr = page.getContentResource().listChildren();
            findFirstImage: while (childItr.hasNext()) {
                Resource childRes = childItr.next();
                ValueMap childVm = ResourceUtil.getValueMap(childRes);
                Iterator < Resource > grandItr = childRes.listChildren();
                while (grandItr.hasNext()) {
                    Resource grandRes = grandItr.next();
                    ValueMap grandVm = ResourceUtil.getValueMap(grandRes);
                    if (grandVm.containsKey(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName())) {
                        String rt = grandVm.get(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName(), String.class);
                        if (rt.equals(IMAGE_RESOURCE_TYPE)) {
                            firstImage = new SniImageFactory().withPath(grandVm.get(IMAGE_PATH, String.class)).build();
                            break findFirstImage;
                        }
                    }
                }
            }
        }
        return this.firstImage;
    }
    
    /** This method is used to get the content-well region/resource. */ 
    private Resource getContentWellResource() {
    	if(contentWellResource == null) {
	    	if(resource != null) {
	    		contentWellResource = resource.getChild(ContentRegionNames.CONTENT_WELL.regionName());
	    	}
    	}
    	return contentWellResource;
    }
    
    /** This method is used to get the content-well-paginated region/resource. */ 
    private Resource getContentWellPaginatedResource() {
    	if(contentWellPaginatedResource == null) {
	    	if(resource != null) {
	    		contentWellPaginatedResource = resource.getChild(ContentRegionNames.CONTENT_WELL_PAGINATED.regionName());
	    	}
    	}
    	return contentWellPaginatedResource;
    }

    /** This method is specifically used for Article Export. */
    @Override
    public String getByLine() {
        if (byline == null) {
            Resource contentWellResource = getContentWellResource();
            if (contentWellResource != null) {
                Iterator < Resource > childNodes = contentWellResource.listChildren();
                if (childNodes != null) {
                    Resource titleBlock = getChildComponent(childNodes, RESOURCE_TYPE_TITLE_BLOCK);
                    if (titleBlock != null) {
                        byline = getFieldValueFromComponent(titleBlock, DialogPropertyNames.ARTICLE_BYLINE_PREFACE.dialogPropertyName());
                    }
                }
            }
        }
        return byline;
    }

    /**
     * Iterates through the child nodes and list out the required component.
     * 
     * @param childNodes
     * @param componentName
     * @return
     */
    private Resource getChildComponent(Iterator < Resource > childNodes, String componentName) {
        LOG.debug("In getChildComponent");
        Resource childResource = null;
        while (childNodes.hasNext()) {
            childResource = childNodes.next();
            LOG.debug("getBylineFromComponent - childResource resourceType is:: " + childResource.getResourceType());
            if (childResource.getResourceType().equals(componentName)) {
            	break;
            }
        }
        return childResource;
    }

    /**
     * Retrieve the byline from the dialog/component properties.
     * @param childResource
     * @return
     */
    private String getFieldValueFromComponent(Resource childResource, String fieldName) {
        LOG.debug("In getFieldValueFromComponent");
        ValueMap componentProperties = ResourceUtil.getValueMap(childResource);
        if (componentProperties != null) {
            if (componentProperties.containsKey(fieldName)) {
                return componentProperties.get(fieldName, String.class);
            }
        }
        return null;
    }

    /** {@inheritDoc} .*/
	@Override
	public String getBody() {
		if (articleBody == null) {
            Resource contentWellResource = getContentWellResource();
            String cwArticleBody = getArticleBodyFromRegion(contentWellResource);
            cwArticleBody = cwArticleBody != null ? cwArticleBody.trim() : "";
            
            Resource contentWellPaginatedResource = getContentWellPaginatedResource();
            String cwpArticleBody = getArticleBodyFromRegion(contentWellPaginatedResource);
            cwpArticleBody = cwpArticleBody != null ? cwpArticleBody.trim() : "";
            
            articleBody = cwArticleBody + " " + cwpArticleBody;
            articleBody = articleBody.trim();
        }
		return articleBody;
	}

	/**
	 * The Method is used to retrieve the rich text editor components from the region
	 * @param resource
	 * @return
	 */
	public String getArticleBodyFromRegion(Resource resource) {
		if (resource != null) {
            Iterator < Resource > childNodes = resource.listChildren();
            if (childNodes != null) {
            	Resource childResource = null;
            	StringBuffer strBuffer = new StringBuffer("");
            	String text = null;
            	 while (childNodes.hasNext()) {
                     childResource = childNodes.next();
                     LOG.debug("getArticleBodyFromRegion - childResource resourceType is:: " + childResource.getResourceType());
                     if (childResource.getResourceType().equals(RESOURCE_TYPE_RICH_TEXT_EDITOR)) {
                    	 text = getFieldValueFromComponent(childResource, DialogPropertyNames.ARTICLE_TEXT.dialogPropertyName());
                    	 if(text != null) {
                    		 strBuffer.append(text);
                    		 strBuffer.append(" ");
                    	 }
                     }
                 }
            	 if(strBuffer.length() > 0) {
            		 return strBuffer.toString();
            	 }
            }
        }
		return null;
	}
}
