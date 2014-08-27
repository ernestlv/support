package com.scrippsnetworks.wcm;

import org.apache.sling.api.adapter.Adaptable;

/**
 * Factory which will build out an Adaptable object. 
 */
public interface SniMockFactory {
    Adaptable build();
}
