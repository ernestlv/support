package com.scrippsnetworks.wcm.regions;

import org.apache.sling.api.resource.Resource;

/**
 * @author Jason Clark
 *         Date: 5/23/13
 */
public interface RightRail {

    public static final String RIGHT_RAIL_PROPERTY_NAME = "sni:rightRail";

    /** Returns path to the active right rail */
    public String getRightRailPath();

    /** Returns Resource of active right rail */
    public Resource getRightRailResource();

}
