package com.scrippsnetworks.wcm.regions;

import org.apache.sling.api.resource.Resource;

/**
 * User: kenshih
 * Date: 8/23/13
 */
public interface Footer {
    public static final String OVERRIDE_PROPERTY_NAME = "sni:footer";

    /** Returns path to the active footer */
    public String getPath();

    /** Returns Resource of active footer */
    public Resource getResource();

    /** Returns if the footer has a valid resource. */
    public boolean isValid();
}
