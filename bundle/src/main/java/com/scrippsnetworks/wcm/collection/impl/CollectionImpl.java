package com.scrippsnetworks.wcm.collection.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.collection.Collection;
import com.scrippsnetworks.wcm.page.ExportConstants;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * @author Mallik Vamaraju Date: 9/23/13
 * @updated Venkata Naga Sudheer Donaboina: 11/21/2013
 * 
 * New or higher resource on asset Types needs an update. Currently the class handles only page or image asset types.
 * 
 */

public class CollectionImpl implements Collection {
	
	private static final Logger LOG = LoggerFactory.getLogger(CollectionImpl.class);
	
	/** ValueMap of properties merged from collection page and asset */
	private ValueMap collectionProperties;
	
	/** ResourceResolver for convenience. */
	private ResourceResolver resourceResolver;
	
	/** Property containing assets paths in collection. */
	private static final String SNI_COLLECTED_ASSETS = "sni:collectedAssets";
	
	/** Property containing collected text in collection. */
	private static final String SNI_COLLECTED_TEXT = "sni:collectedText";
	
	/** SniPage of collection used to create this object. */
	private SniPage sniPage;
	
	/** Member for List of collected assets to this collection. */
	private List<Map<String, String>> collectionAssetMapList;
	
	/** Member for list of collected assets paths to this collection. */
	private String[] relatedAssetsPaths;
	
	/** Member for list of collected assets to this collection. */
	private List<String> collectedText;
	
	/** Mobile-App Tag. */
	private static final String MOBILE_APP_TAG ="food-mobile-app:use";
	
	/** Construct a new CollectionImpl given an SniPage. */
	public CollectionImpl(SniPage sniPage) {
		this.sniPage = sniPage;
		this.collectionProperties = sniPage.getProperties();
		Resource resource = sniPage.getContentResource();
		if (resource != null) {
			resourceResolver = resource.getResourceResolver();
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public List<String> getCollectedText() {
		if (collectedText == null && sniPage != null) {
			collectedText = new ArrayList<String>();
			collectionProperties = sniPage.getProperties();
			if (collectionProperties != null
					&& collectionProperties.containsKey(SNI_COLLECTED_TEXT)) {
				String[] sniCollectedText = collectionProperties.get(
						SNI_COLLECTED_TEXT, String[].class);
				
				for (String item : sniCollectedText) {
					if (item != null) {
						collectedText.add(item);
					}
				}
			}
			
		}
		return collectedText;
	}
	
	/** This method is used to retrieve the collection asset paths. */
	private String[] getCollectedAssetsPaths() {
		if (relatedAssetsPaths == null && sniPage != null) {
			collectionProperties = sniPage.getProperties();
			if (collectionProperties != null
					&& collectionProperties.containsKey(SNI_COLLECTED_ASSETS)) {
				relatedAssetsPaths = collectionProperties.get(SNI_COLLECTED_ASSETS, String[].class);
			}
		}
		return relatedAssetsPaths;
	}
	
	/** {@inheritDoc} */
	@Override
	public List<Map<String, String>> getCollectionAssetMapList() {
		if(collectionAssetMapList == null) {
			String[] assetPaths = getCollectedAssetsPaths();
			if(assetPaths != null) {
				Resource assetPathResource = null;
				SniPage page = null;
				collectionAssetMapList = new ArrayList<Map<String, String>>();
				Map<String, String> collectionAssetMap = null;
				for (String path : assetPaths) {
					assetPathResource = resourceResolver.getResource(path);
					if (assetPathResource != null) {
						/**
						 * Used characters in switch statement as Strings are
						 * not supported in jdk1.6 and making default assetType
						 * to p 
						 * p - page
						 * i - image
						 */
						char assetType = 'p';
						collectionAssetMap = new HashMap<String, String>();
						page = PageFactory.getSniPage(assetPathResource.adaptTo(Page.class));
						if(page != null) {
							assetType = 'p';
						} else if(path.contains("/content/dam")) {
							assetType = 'i';
						}
						switch (assetType) {
						case 'p':
							collectionAssetMap.put(ExportConstants.PAGETYPE, page.getPageType());
							collectionAssetMap.put(ExportConstants.PATH, path);
							collectionAssetMap.put(ExportConstants.CONTENT, page.getUid());
							break;
						case 'i':
							collectionAssetMap.put(ExportConstants.PAGETYPE, "image");
							collectionAssetMap.put(ExportConstants.PATH, path);

							Asset asset = assetPathResource
									.adaptTo(Asset.class);
							Map<String, Object> assetMap = asset.getMetadata();
							if (assetMap != null) {
								if (assetMap.containsKey(ExportConstants.PROP_IMAGE_WIDTH)) {
									Object width =  assetMap.get(ExportConstants.PROP_IMAGE_WIDTH);
									if(width != null) {
										if(width instanceof String) {
											collectionAssetMap.put(ExportConstants.WIDTH, (String) width);
										} else if(width instanceof Long) {
											collectionAssetMap.put(ExportConstants.WIDTH, Long.toString((Long)width));
										}
									}
								}
								if (assetMap.containsKey(ExportConstants.PROP_IMAGE_LENGTH)) {
									Object height = assetMap.get(ExportConstants.PROP_IMAGE_LENGTH);
									if(height != null) {
										if(height instanceof String) {
											collectionAssetMap.put(ExportConstants.HEIGHT, (String) height);
										} else if(height instanceof Long) {
											collectionAssetMap.put(ExportConstants.HEIGHT, Long.toString((Long)height));
										}
									}
								}
								if (assetMap.containsKey(ExportConstants.PROP_IMAGE_TAGS)
										&& assetMap.get(ExportConstants.PROP_IMAGE_TAGS) != null) {
									Object[] objectArray = (Object[]) assetMap.get(ExportConstants.PROP_IMAGE_TAGS);
									
									String tag = null;
									List<String> tagList = new ArrayList<String>();
									for(Object object : objectArray) {
										if (object != null) {
											tag = (String) object;
											if (tag.contains(MOBILE_APP_TAG)) {
												tagList.add(tag);
											}
										}
									}
									if (tagList.size() > 0) {
										collectionAssetMap.put(ExportConstants.USE, StringUtils.join(tagList, ','));
									}
									
								}
							}
							break;
						}
						collectionAssetMapList.add(collectionAssetMap);
					}
				}
			}
		}
		return collectionAssetMapList;
	}
	
	
	public SniPage getSniPage() {
		return sniPage;
	}
	
}
