package com.scrippsnetworks.wcm.cache;

import org.apache.sling.api.resource.Resource;

public interface TopicPageCacheService {

    public String getTopicPagePath(Resource resource, String rawTag, String brand);

    public void invalidate(String rawTag, String brand);

    public void invalidateAll();

}
