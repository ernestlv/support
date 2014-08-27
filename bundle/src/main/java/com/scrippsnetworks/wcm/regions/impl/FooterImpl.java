package com.scrippsnetworks.wcm.regions.impl;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.regions.Footer;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

/**
 * This should probably be an Adapter class or more generic region-maker
 * User: kenshih
 * Date: 8/23/13
 */
public class FooterImpl implements Footer {
    private static final String FOOT_REGION_PATH = "/jcr:content/footer";
    private static final String DEFAULT_FOOT_RESOURCE_PATH = "default" + FOOT_REGION_PATH;
    private static final String DEFAULT_MOBILE_FOOT_RESOURCE_PATH = "mobile" + FOOT_REGION_PATH;
    private static final String CONTENT_BASE = "/content";
    private static final String REGIONS_NODE = "regions";
    private static final String REGION_TYPE = "footer";

    /** Path to active global header. */
    private String path;

    /** Resource for active global header. */
    private Resource footerResource;

    /** Keeps track of if a valid header resource was found */
    private boolean valid;

    /** Needed to make it all work */
    private SniPage sniPage;

    public FooterImpl(final SniPage sniPage) {
        this(sniPage, false);
    }

    public FooterImpl(final SniPage sniPage, boolean isMobile) {
        this.sniPage = sniPage;
        footerResource = null;
        if (sniPage != null) {
            ResourceResolver resourceResolver = sniPage.getContentResource().getResourceResolver();
            String overridePath = sniPage.getProperties().get(OVERRIDE_PROPERTY_NAME, "");
            //First, try to get the resource at the overriden path
            if (!StringUtils.isEmpty(overridePath)) {
                footerResource = resourceResolver.getResource(overridePath + FOOT_REGION_PATH);
            }
            //Then, fall back to the default
            if (footerResource == null) {
                if (isMobile){
                    footerResource = resourceResolver.getResource(String.format("%s/%s/%s/%s/%s",
                            CONTENT_BASE,
                            sniPage.getBrand(),
                            REGIONS_NODE,
                            REGION_TYPE,
                            DEFAULT_MOBILE_FOOT_RESOURCE_PATH));
                } else{
                    footerResource = resourceResolver.getResource(String.format("%s/%s/%s/%s/%s",
                            CONTENT_BASE,
                            sniPage.getBrand(),
                            REGIONS_NODE,
                            REGION_TYPE,
                            DEFAULT_FOOT_RESOURCE_PATH));
                }
            }
        }
        if (footerResource == null) {
            path = null;
            valid = false;
        } else {
            path = footerResource.getPath();
            valid = true;
        }
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Resource getResource() {
        return footerResource;
    }

    @Override
    public boolean isValid() {
        return valid;
    }
}
