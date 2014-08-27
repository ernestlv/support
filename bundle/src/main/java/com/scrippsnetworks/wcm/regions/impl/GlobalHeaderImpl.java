package com.scrippsnetworks.wcm.regions.impl;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.regions.GlobalHeader;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Patrick Armstrong
 *         Date: 8/12/2013
 */
public class GlobalHeaderImpl implements GlobalHeader {

    Logger log = LoggerFactory.getLogger(GlobalHeaderImpl.class);

    private static final String GLOBAL_HEADER_PROPERTY_NAME = "sni:globalHeader";
    private static final String HEADER_REGION_PATH = "/jcr:content/global-header";
    private static final String DEFAULT_HEADER_RESOURCE_PATH = "default" + HEADER_REGION_PATH;
    private static final String DEFAULT_MOBILE_HEADER_RESOURCE_PATH = "mobile" + HEADER_REGION_PATH;
    private static final String CONTENT_BASE = "/content";
    private static final String REGIONS_NODE = "regions";
    private static final String REGION_TYPE = "header";

    /** Path to active global header. */
    private String globalHeaderPath;

    /** Resource for active global header. */
    private Resource globalHeaderResource;
    
    /** Keeps track of if a valid header resource was found */
    private boolean valid;

    /** Needed to make it all work */
    private SniPage sniPage;

    /**
     * Core Global Header implementation.
     * @param pSniPage Current SniPage
     */
    public GlobalHeaderImpl(final SniPage pSniPage, boolean isMobile) {
        sniPage = pSniPage;
        globalHeaderResource = null;
        if (sniPage != null) {
            ResourceResolver resourceResolver = sniPage.getContentResource().getResourceResolver();
            String overridePath = sniPage.getProperties().get(GLOBAL_HEADER_PROPERTY_NAME, "");
            //First, try to get the resource at the overriden path
            if (!StringUtils.isEmpty(overridePath)) {
                globalHeaderResource = resourceResolver.getResource(overridePath + HEADER_REGION_PATH);
            } 
            //Then, fall back to the default
            if (globalHeaderResource == null) {
                if (isMobile){
                    globalHeaderResource = resourceResolver.getResource(String.format("%s/%s/%s/%s/%s",
                            CONTENT_BASE,
                            sniPage.getBrand(),
                            REGIONS_NODE,
                            REGION_TYPE,
                            DEFAULT_MOBILE_HEADER_RESOURCE_PATH));
                } else{
                    globalHeaderResource = resourceResolver.getResource(String.format("%s/%s/%s/%s/%s",
                            CONTENT_BASE,
                            sniPage.getBrand(),
                            REGIONS_NODE,
                            REGION_TYPE,
                            DEFAULT_HEADER_RESOURCE_PATH));
                }
            }
        }
        if (globalHeaderResource == null) {
            globalHeaderPath = null;
            valid = false;
        } else {
            globalHeaderPath = globalHeaderResource.getPath();
            //TODO: check resource type?
            valid = true;
        }
    }

    /**
     * Returns the path of the active right rail for the current page.
     * @return String current right rail path.
     */
    @Override
    public String getGlobalHeaderPath() {
        return globalHeaderPath;
    }

    /**
     * Returns Resource of right rail for current page.
     * @return Resource
     */
    @Override
    public Resource getGlobalHeaderResource() {
        return globalHeaderResource;
    }
    
    /**
     * Checks the validity of the global header object.
     * @return If the current global header contains a valid, non-null resource.
     */
    @Override
    public boolean isValid() {
        return valid;
    }
}
