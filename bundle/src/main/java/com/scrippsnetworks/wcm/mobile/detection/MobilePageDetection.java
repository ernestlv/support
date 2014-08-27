package com.scrippsnetworks.wcm.mobile.detection;

import org.apache.sling.api.resource.ResourceResolver;

import com.scrippsnetworks.wcm.page.SniPage;


/**
 *  Interface for detection mobile pages
 */
public interface MobilePageDetection {
    
    public boolean isSupportMobileVersion(SniPage sniPage);
}
