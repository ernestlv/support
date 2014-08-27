package com.scrippsnetworks.wcm.cache;

/**
 * @author Jason Clark
 *         Date: 11/12/13
 */
public interface RegionCacheService {

    /** Get the cached HTML output for the given path. */
    public String get(String path);

    /** Load the cache with HTML given the path to the desired page. */
    public void put(String path, String html);

    /** Expire cache for given path. */
    public void expire(String path);

}
