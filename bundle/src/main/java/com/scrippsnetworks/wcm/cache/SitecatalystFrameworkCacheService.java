package com.scrippsnetworks.wcm.cache;

import com.day.cq.analytics.sitecatalyst.Framework;

public interface SitecatalystFrameworkCacheService {

    public Framework getFramework(String configPath);

    public void invalidate(String configPath);

    public void invalidateAll();

}
