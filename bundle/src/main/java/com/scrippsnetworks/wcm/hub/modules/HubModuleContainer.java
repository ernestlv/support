package com.scrippsnetworks.wcm.hub.modules;

import org.apache.sling.api.resource.Resource;

import java.util.List;

/**
 * @author Jason Clark
 *         Date: 5/29/13
 */
public interface HubModuleContainer {

    /** Name of the property which stores a path to an instantiated module to share in the hub. */
    public static final String HUB_MODULES_PROPERTY = "hubModules";

    /** Returns a List of Resources for non-lead modules shared across a hub */
    public List<Resource> getModules();

}
