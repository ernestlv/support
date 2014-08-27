package com.scrippsnetworks.wcm.util.modalwindow;

import org.apache.sling.api.resource.ResourceResolver;


/**
 *  Interface for implementing modal windows urls
 *  on mobile site
 */
public interface MobileModalPath {
    /**
     * <p>GetModalWindowPath</p> return path for implementing mobile modal window
     */
    public String getModalWindowPath(ResourceResolver resourceResolver, String currentPath, int pageNumber);
    public String getModalWindowPath(ResourceResolver resourceResolver, String currentPath);
}
