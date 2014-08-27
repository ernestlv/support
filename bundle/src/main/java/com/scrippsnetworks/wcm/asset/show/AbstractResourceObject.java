package com.scrippsnetworks.wcm.asset.show;

import javax.jcr.Node;
import javax.jcr.Value;
import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;

import com.scrippsnetworks.wcm.util.AssetPropertyNames;
import com.scrippsnetworks.wcm.taglib.Functions;
import com.scrippsnetworks.wcm.fnr.util.AssetRootPaths;
import com.scrippsnetworks.wcm.page.PagePropertyConstants;

/**
 * Abstract Object that is the base of all asset resource object of shows
 * 
 * @author mei-yichang
 * 
 */

@SuppressWarnings("rawtypes")
public class AbstractResourceObject implements Comparable {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	/* property names of content node constants */
	public static final String PROPERTY_ASSET_TYPE = "sni:assetType";
	public static final String PROPERTY_INDEX_LETTER = "sni:firstLetter";
	public static final String PROPERTY_SORT_TITLE = "sni:sortTitle";
	public static final String PROPERTY_TITLE = "sni:title";
	public static final String PROPERTY_DESCRIPTION = "sni:description";
	public static final String PROPERTY_FASTFWDID = "sni:fastfwdId";

	/* root path of content node constants */
	public static final String CONTENT_ROOT = "/content/food";
	public static final String SNI_ASSET_PREFIX = "/etc/sni-asset";

	protected ResourceResolver resolver;

	/* basic properties */
	String assetPath;
	String contentPath;
	String title;

	String id;

	Resource resource;

    protected ValueMap assetProperties;
	ValueMap contentProperties;

	/**
	 * Base Constructor
	 * 
	 * @param resource
	 */
	public AbstractResourceObject(Resource resource) {
		this.resource = resource;
		try{
			if (resource != null) {
				resolver = resource.getResourceResolver();
				assetPath = resource.getPath();
				Resource assetContent = resolver.getResource(assetPath + "/" + JcrConstants.JCR_CONTENT);
				
				/*assetPath = assetPath.substring(SNI_ASSET_PREFIX.length());
				// TODO: hack for now to fix issue with shows/videos living under /etc/sni-asset/food, we need to remove the "/food"
				if(assetPath.startsWith("/food")) {
					assetPath = assetPath.substring("/food".length());
				}
				// the content node will have the same structure as the asset node
				contentPath = CONTENT_ROOT + assetPath;*/
	
				//Resource assetContent = resource.getChild(JcrConstants.JCR_CONTENT);
				
				if(assetContent != null){
					assetProperties = ResourceUtil.getValueMap(assetContent);
					Node assetNode = assetContent.adaptTo(Node.class);
					
					if(assetNode != null){
						if(assetNode.hasProperty(AssetPropertyNames.SNI_PAGE_LINKS.propertyName())){
							Value[] values = assetNode.getProperty(AssetPropertyNames.SNI_PAGE_LINKS.propertyName()).getValues();
							if(values != null){
								contentPath = values[0].getString();
							}
						}else {
							contentPath = Functions.getBasePath(assetPath.replace(AssetRootPaths.ASSET_ROOT.path(), CONTENT_ROOT));
						}
						if(assetNode.hasProperty(PagePropertyConstants.PROP_SNI_FASTFWDID)){
							id = assetNode.getProperty(PagePropertyConstants.PROP_SNI_FASTFWDID).getString();
						}
					}
				}
				
				
				Resource content = resolver.getResource(contentPath + "/"
						+ JcrConstants.JCR_CONTENT);
				if (content != null) {
					contentProperties = ResourceUtil.getValueMap(content);	
		            String jcrTitle = contentProperties.get(PagePropertyConstants.PROP_JCR_TITLE, String.class);
		            String sniTitle = contentProperties.get(PagePropertyConstants.PROP_SNI_TITLE, String.class);
		            title = (StringUtils.isNotBlank(sniTitle)) ? sniTitle : jcrTitle;
				}
			}
		}catch(Exception e){
			log.error("Exception in AbstractResourceObject" + e.getMessage());
		}
	}

	public String getTitle() {
		return title;
	}

	/**
	 * Utility function to check whether the resource is an instance of the
	 * specified asset type
	 * 
	 * @param resource
	 * @param type
	 * @return
	 */
	protected boolean checkResourceAssetType(Resource resource, String type) {
		try {
			if (resource != null) {
				Node parentNode = resource.adaptTo(Node.class);
				String propPath = JcrConstants.JCR_CONTENT + "/"
						+ PROPERTY_ASSET_TYPE;
				if (parentNode != null && parentNode.hasProperty(propPath)) {
					String assetType = parentNode.getProperty(propPath)
							.getString();
					return assetType.equals(type);
				}
			}
		} catch (RepositoryException e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

    public ValueMap getAssetProperties() {
        return assetProperties;
    }

	public String getAssetPath() {
		return assetPath;
	}

	public String getContentPath() {
		return contentPath;
	}

	/**
	 * Object is sorted by title by default
	 */
	public int compareTo(Object o) throws ClassCastException {
		return this.title.compareToIgnoreCase(
                ((AbstractResourceObject) o)
				    .getTitle());
	}

	public String getId() {
		return id;
	}
}
