package com.scrippsnetworks.wcm.regions.impl;

import com.scrippsnetworks.wcm.episode.Episode;
import com.scrippsnetworks.wcm.episode.EpisodeFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.regions.RightRail;
import com.scrippsnetworks.wcm.regions.RightRailFactory;
import com.scrippsnetworks.wcm.snipackage.SniPackage;
import com.scrippsnetworks.wcm.util.PageTypes;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jason Clark
 *         Date: 5/23/13
 */
public class RightRailImpl implements RightRail {

    Logger log = LoggerFactory.getLogger(RightRailImpl.class);

    private static final String PAR_RESOURCE_TYPE = "foundation/components/parsys";
    private static final String RAIL_REGION_PATH = "/jcr:content/right-rail";
    private static final String DEFAULT_RAIL_RESOURCE_PATH = "default" + RAIL_REGION_PATH;
    private static final String CONTENT_BASE = "/content";
    private static final String REGIONS_NODE = "regions";
    private static final String REGION_TYPE = "right-rail";

    /** Path to active right rail. */
    private String rightRailPath;

    /** Resource for active right rail. */
    private Resource rightRailResource;

    /** Needed to make it all work */
    private SniPage sniPage;

    /**
     * Core Right Rail implementation.
     * @param sniPage Current SniPage
     */
    public RightRailImpl(final SniPage sniPage) {
        if (sniPage != null) {
            this.sniPage = sniPage;
            //try the package rail first
            if (!usePackageRail()) {
                //ok, try the page override next
                if (!useOverrideRail()) {
                    //check if the page inherits a rail
                    if (!useInheritedRail()) {
                        //when all else fails
                        useDefaultRail();
                    }
                }
            }
        }
    }

    /** Util class to check for null & resource type */
    private boolean isGoodRailResource(Resource resource) {
        return (resource != null && resource.isResourceType(PAR_RESOURCE_TYPE));
    }

    /**
     * Try to construct the right rail data based on shared package right rail.
     * @return boolean based on successful creation of right rail from that property.
     */
    protected boolean usePackageRail() {
        SniPackage sniPackage = sniPage.getSniPackage();
        //String pageType = sniPage.getPageType();
        if (sniPackage != null) {
            Resource res = sniPackage.getRegions().get(REGION_TYPE);
            if (isGoodRailResource(res)) {
                rightRailResource = res;
                rightRailPath = res.getPath();
                return true;
            }
        }
        return false;
    }

    /**
     * Try to construct the right rail data from a page's inheritance rule.
     * @return boolean based on successful creation of the right rail.
     */
    protected boolean useInheritedRail() {
        SniPage parentPage = null;
        PageTypes type = PageTypes.findPageType(sniPage.getPageType());
        if (type != null) {
            switch (type) {
                case EPISODE:
                    Episode episode = new EpisodeFactory()
                        .withSniPage(sniPage)
                        .build();
                    if (episode != null) {
                        parentPage = episode.getRelatedShowPage();
                    }
                    break;
                default:
                    break;
            }
        }
        if (parentPage != null) {
            RightRail inheritedRail = new RightRailFactory()
                .withSniPage(parentPage)
                .build();
            if (inheritedRail != null) {
                rightRailResource = inheritedRail.getRightRailResource();
                rightRailPath = inheritedRail.getRightRailPath();
                return true;
            }
        }
        return false;
    }

    /**
     * Try to construct the right rail data based on the path contained in sni:rightRail property.
     * @return boolean based on successful creation of right rail.
     */
    protected boolean useOverrideRail() {
        String pageRailOverride;
        ValueMap pageProperties = sniPage.getProperties();
        if (pageProperties.containsKey(RIGHT_RAIL_PROPERTY_NAME)) {
            pageRailOverride = pageProperties.get(RIGHT_RAIL_PROPERTY_NAME, String.class) + RAIL_REGION_PATH;
        } else {
            return false;
        }
        if (StringUtils.isNotBlank(pageRailOverride)) {
            Resource railResource = sniPage
                   .getContentResource()
                   .getResourceResolver()
                   .getResource(pageRailOverride);
            if (isGoodRailResource(railResource)) {
               rightRailResource = railResource;
               rightRailPath = pageRailOverride;
               return true;
            }
        }
        return false;
    }

    /**
     * Try to construct the right rail data based on shared package right rail.
     * @return boolean based on successful creation of right rail from that property.
     */
    protected boolean useDefaultRail() {
        String pageType = sniPage.getPageType();
        String brand = sniPage.getBrand();
        if (StringUtils.isNotBlank(pageType)
                && StringUtils.isNotBlank(brand)) {
            String railPath = CONTENT_BASE
                    + "/" + brand
                    + "/" + REGIONS_NODE
                    + "/" + pageType
                    + "/" + REGION_TYPE
                    + "/" + DEFAULT_RAIL_RESOURCE_PATH;
            Resource railResource = sniPage
                    .getContentResource()
                    .getResourceResolver()
                    .getResource(railPath);
            if (isGoodRailResource(railResource)) {
                rightRailResource = railResource;
                rightRailPath = railPath;
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the path of the active right rail for the current page.
     * @return String current right rail path.
     */
    @Override
    public String getRightRailPath() {
        return rightRailPath;
    }

    /**
     * Returns Resource of right rail for current page.
     * @return Resource
     */
    @Override
    public Resource getRightRailResource() {
        return rightRailResource;
    }
}
