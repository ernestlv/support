package com.scrippsnetworks.wcm.cache.expiration;

import com.scrippsnetworks.wcm.cache.RegionCacheService;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.event.jobs.JobProcessor;

import org.osgi.service.event.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * @author Jason Clark
 *         Date: 11/12/13
 */
public class RegionCacheExpirationJob implements JobProcessor {

    private static final Logger log = LoggerFactory.getLogger(RegionCacheExpirationJob.class);

    private static final String SNI_CACHEABLE = "cacheable";
    private static final String JCR_CONTENT_PATH = "/jcr:content";

    /** Path of item in cache to invalidate. */
    private String path;

    /** RegionCacheService with the cached data to flush. */
    private RegionCacheService regionCacheService;

    /** ResourceResolver for loading and checking contents of page nodes. */
    private ResourceResolverFactory resolverFactory;

    /** Create job to expire region cache with given path as a key. */
    public RegionCacheExpirationJob(final String path,
                                    final ResourceResolverFactory resolverFactory,
                                    final RegionCacheService regionCacheService) {
        super();
        this.path = path;
        this.regionCacheService = regionCacheService;
        this.resolverFactory = resolverFactory;
    }

    /** Check the path and see if it's cacheable */
    public boolean process(final Event event) {
        ResourceResolver resolver = null;
        try {
            if (isPathCachable(path)) {
                String contentPath = path + JCR_CONTENT_PATH;
                resolver = resolverFactory.getAdministrativeResourceResolver(null);
                Resource contentResource = resolver.getResource(contentPath);
                if (contentResource != null) {
                    recurseNodes(contentResource);
                }
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (resolver != null) {
                resolver.close();
            }
        }
        return false;
    }

    /* Does the page path meet base requirement for being a region? */
    private boolean isPathCachable(String path) {
        return StringUtils.contains(path, "/regions/");
    }

    /* Check the props of the Resource for our magic cachable flag. */
    private boolean isResourceCachable(Resource resource) {
        log.debug("checking if resourceType of component is cachable: {}", resource.getPath());
        if (StringUtils.isNotBlank(resource.getResourceType())) {
            ResourceResolver resolver = resource.getResourceResolver();
            Resource resourceType = resolver.getResource(resource.getResourceType());
            if (resourceType != null) {
                ValueMap properties = resourceType.adaptTo(ValueMap.class);
                return properties.containsKey(SNI_CACHEABLE);
            }
        }
        return false;
    }

    /* Iterate over nodes checking if all the children in this tree are cachable and flush. */
    private void recurseNodes(final Resource resource) {
        if (isResourceCachable(resource)) {
            log.debug("flushing component from cache: {}", resource.getPath());
            regionCacheService.expire(resource.getPath());
        }
        Iterator<Resource> childNodes = resource.listChildren();
        if (childNodes != null) {
            log.debug("recursing children of node: {}", resource.getPath());
            while (childNodes.hasNext()) {
                Resource childNode = childNodes.next();
                if (childNode != null) {
                    recurseNodes(childNode);
                }
            }
        }
    }

}
