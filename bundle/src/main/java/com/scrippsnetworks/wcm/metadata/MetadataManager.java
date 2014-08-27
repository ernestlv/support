package com.scrippsnetworks.wcm.metadata;

import java.util.Map;

public interface MetadataManager extends Map<MetadataProperty, String> {
    public String getMetadataManagerJson();
}
