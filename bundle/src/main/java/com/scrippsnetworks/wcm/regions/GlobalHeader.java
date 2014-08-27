package com.scrippsnetworks.wcm.regions;

import org.apache.sling.api.resource.Resource;

/**
 * @author Patrick Armstrong
 *         Date: 8/12/2013
 */
public interface GlobalHeader {

    /** Returns path to the active global header */
    public String getGlobalHeaderPath();

    /** Returns the Resource of the active global header */
    public Resource getGlobalHeaderResource();
    
    /** Returns if the GlobalHeader has a valid resource. */
    public boolean isValid();

}
