package com.scrippsnetworks.wcm.cache.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.day.cq.analytics.sitecatalyst.Framework;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.scrippsnetworks.wcm.cache.SitecatalystFrameworkCacheService;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;

@Component(immediate = true, metatype = true, label = "Sitecatalyst Framework Cache Service", description = "Cache for CQ Sitecatalyst Framework objects")
@Service(value = SitecatalystFrameworkCacheService.class)
public class SitecatalystFrameworkCacheServiceImpl implements SitecatalystFrameworkCacheService, RemovalListener<String, Framework> {

    @Reference
    ResourceResolverFactory resolverFactory;

    static final Cache<String, Framework> cache = CacheBuilder.newBuilder()
            .maximumSize(16)
            .expireAfterAccess(60l, TimeUnit.MINUTES)
            .recordStats()
            .build();

    private final static ConcurrentMap<String, ResourceResolver> resolverCache = new ConcurrentHashMap<String, ResourceResolver>();

    private final static Logger log = LoggerFactory.getLogger(SitecatalystFrameworkCacheServiceImpl.class);

    @Override
    public Framework getFramework(final String configPath) {
        try {
            return cache.get(configPath,
                new Callable<Framework>() {
                    public Framework call() throws Exception {
                        log.debug("loading Framework for {}", configPath);
                        ResourceResolver resolver = null;
                        try {
                            resolver = resolverFactory.getAdministrativeResourceResolver(null);
                            resolverCache.put(configPath, resolver);
                            return resolver.getResource(configPath).adaptTo(Framework.class);
                        } catch (Exception e) {
                            if (resolver != null && resolverCache.containsKey(configPath)) {
                                resolver.close();
                                resolverCache.remove(configPath);
                            }
                            throw e;
                        }
                    }
                });
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return null;
    }

    @Override
    public void invalidate(String configPath) {
        cache.invalidate(configPath);
    }

    @Override
    public void invalidateAll() {
        cache.invalidateAll();
    }

    /**
     * @inheritDoc
     *
     * This is implemented here so we can explicitly close the resource resolver associated with the expired framework.
     */
    public void onRemoval(RemovalNotification<String, Framework> notification) {
        String key = notification.getKey();
        try {
            ResourceResolver toClose = resolverCache.get(key);
            if (toClose != null) {
                resolverCache.remove(key);
                toClose.close();
            }
        } catch (Exception e) {
            log.warn("caught exception closing resource resolver", e);
        }
    }

}


