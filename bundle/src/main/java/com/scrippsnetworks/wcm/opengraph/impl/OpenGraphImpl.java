package com.scrippsnetworks.wcm.opengraph.impl;

import com.scrippsnetworks.wcm.canonicalimage.CanonicalImage;
import com.scrippsnetworks.wcm.canonicalimage.CanonicalImageFactory;
import com.scrippsnetworks.wcm.fnr.util.PageSlingResourceTypes;
import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.opengraph.OpenGraph;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.util.PagePropertyNames;
import com.scrippsnetworks.wcm.util.StringUtil;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.ValueMap;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Jason Clark
 *         Date: 4/19/13
 */
public class OpenGraphImpl implements OpenGraph {

    private static final Logger log = LoggerFactory.getLogger(OpenGraphImpl.class);

    private Map<String, String> data;

    private SniPage page;
    private ValueMap properties;

    /**
     * Implementation of SNI OpenGraph protocol
     * @param page SniPage
     */
    public OpenGraphImpl(final SniPage page) {
        if (page == null) {
            log.error("Page object was null");
            this.data = null;
        } else {
            this.data = new HashMap<String, String>();
            this.page = page;
            this.properties = page.getProperties();
            try {
                data.put(OG_PROPERTY_TITLE, this.getOGTitle());
                data.put(OG_PROPERTY_DESCRIPTION, this.getOGDescription());
                data.put(OG_PROPERTY_URL, page.getCanonicalUrl());
                data.put(OG_PROPERTY_TYPE, this.getOGType());
                data.put(OG_PROPERTY_IMAGE, this.getOGImg());
            } catch (Exception e) {
                log.error("Exception in OpenGraphImpl constructor: " + ArrayUtils.toString(e.getStackTrace()));
            }
        }
    }

    /**
     * Returns the OpenGraph Title. First checks page properties for user override
     * otherwise uses SEO Title from SniPage object.
     * @return String OpenGraph Title
     */
    @Override
    public String getOGTitle() {
        String userTitle = getProperty(JCR_PROPERTY_OG_TITLE);
        if (userTitle == null) {
            userTitle = page.getSeoTitle();
        }
        return StringUtil.cleanText(userTitle, false, true);
    }

    /**
     * Returns the OpenGraph description.  Checks page properties for user override
     * otherwise uses SEO Description from the SniPage object.
     * @return String OpenGraph Description
     */
    @Override
    public String getOGDescription() {
        String userDescription = getProperty(JCR_PROPERTY_OG_DESCRIPTION);
        if (userDescription == null) {
            userDescription = page.getSeoDescription();
        }
        return StringUtil.cleanText(userDescription, false, true);
    }

    /**
     * Get the OpenGraph image url for this page.
     * @return String URL to image for this page's OpenGraph
     */
    @Override
    public String getOGImg() {
        String ogImgUrl = "";
        CanonicalImage canImg = new CanonicalImageFactory().withSniPage(page).build();
        if (canImg != null) {
            SniImage ogImg = canImg.getImage();
            if (ogImg != null) {
                ogImgUrl = ogImg.getUrl();
            }
        }
        return ogImgUrl;
    }

    /**
     * OpenGraph type property
     * @return String OpenGraph type value
     */
    @Override
    public String getOGType() {
        try {
            String slingResourceType = properties
                    .get(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName(), String.class);
            if (StringUtils.isNotBlank(slingResourceType)) {
                if (slingResourceType.equals(PageSlingResourceTypes.SHOW.resourceType())) {
                    return OG_VALUE_TYPE_SHOW;
                } else if (slingResourceType.equals(PageSlingResourceTypes.EPISODE.resourceType())) {
                    return OG_VALUE_TYPE_EPISODE;
                }
            }
        } catch (Exception e) {
            log.error("Caught Exception in getOGType " + e.getMessage());
        }
        return DEFAULT_OG_TYPE;
    }

    /**
     * Encapsulate the retrieval of props for my purposes, can return null
     * @param property String property name to retrieve
     * @return String property value
     */
    protected String getProperty(String property) {
        try {
            if (properties.containsKey(property)) {
                String value = properties.get(property).toString();
                if (StringUtils.isNotBlank(value)) {
                    return value;
                }
            }
        } catch (Exception e) {
            log.error("Exception caught in getProperty " + e.getMessage());
        }
        return null;
    }

    /**
     * Get the OpenGraph data Map from this object
     * @return Map of OpenGraph properties and values as strings
     */
    @Override
    public Map<String, String> getData() {
        return this.data;
    }
}
