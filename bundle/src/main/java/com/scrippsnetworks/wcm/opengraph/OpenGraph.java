package com.scrippsnetworks.wcm.opengraph;

import com.scrippsnetworks.wcm.image.ImageDimensions;

import java.util.Map;

/**
 * Interface for OpenGraph data management object. This is just a representation
 * of the properties and their values; the usage of these values should be
 * handled in a renderer.
 * 
 * @see com.scrippsnetworks.wcm.taglib.OpenGraphTag
 * @author Jason Clark Date: 4/18/13
 */
public interface OpenGraph {
	public static final String OG_PROPERTY_TITLE = "og:title";
	public static final String OG_PROPERTY_URL = "og:url";
	public static final String OG_PROPERTY_DESCRIPTION = "og:description";
	public static final String OG_PROPERTY_TYPE = "og:type";
	public static final String OG_PROPERTY_IMAGE = "og:image";
	public static final String OG_VALUE_TYPE_EPISODE = "video.episode";
	public static final String OG_VALUE_TYPE_SHOW = "video.tv_show";
	public static final String OG_VALUE_TYPE_ARTICLE = "article";
	public static final String DEFAULT_OG_TYPE = OG_VALUE_TYPE_ARTICLE;
	public static final String JCR_PROPERTY_OG_TITLE = "sni:ogTitle";
	public static final String JCR_PROPERTY_OG_DESCRIPTION = "sni:ogDescription";
	public static final String JCR_PROPERTY_OG_IMAGE = "sni:ogImage";
	public static final ImageDimensions DEFAULT_IMG_DIMENSIONS = ImageDimensions.w616h616;

	/** Returns the OpenGraph Title for the current page. */
	public String getOGTitle();

	/** Returns the OpenGraph Image for the current page. */
	public String getOGImg();

	/** Returns the OpenGraph Description for the current page. */
	public String getOGDescription();

	/** Returns the OpenGraph Type for the current page. */
	public String getOGType();

	public Map<String, String> getData();
}
