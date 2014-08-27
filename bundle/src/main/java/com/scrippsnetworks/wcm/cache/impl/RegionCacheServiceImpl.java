package com.scrippsnetworks.wcm.cache.impl;

import com.scrippsnetworks.wcm.cache.RegionCacheService;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


/**
 * @author Jason Clark
 *         Date: 11/12/13
 */
@Component(immediate = true, metatype = true, label = "Region Cache Service", description = "Cache the HTML output for a given page path.")
@Service(value = RegionCacheService.class)
public class RegionCacheServiceImpl implements RegionCacheService {

    private final static Logger log = LoggerFactory.getLogger(RegionCacheServiceImpl.class);

    /* Cache for region HTML, keyed by page path */
    private static final Cache<String, String> cache = CacheBuilder
            .newBuilder()
            .maximumSize(100)
            .expireAfterAccess(60, TimeUnit.MINUTES)
            .recordStats()
            .build();

    /** {@inheritDoc} */
    public String get(final String path) {
        log.debug("Checking for cached markup for path: {}", path);
        return cache.getIfPresent(path);
    }

    /** {@inheritDoc} */
    public void put(final String path, final String html) {
        if (StringUtils.isNotBlank(path) && StringUtils.isNotBlank(html)) {
            log.debug("loading cache with KEY={} VALUE={}", path, html);
            cache.put(path, html);
        }
    }

    /** {@inheritDoc} */
    public void expire(final String path) {
        log.debug("Invalidating cache for: {}", path);
        cache.invalidate(path);
    }

}
