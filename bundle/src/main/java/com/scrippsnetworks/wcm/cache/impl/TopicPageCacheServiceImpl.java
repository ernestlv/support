package com.scrippsnetworks.wcm.cache.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.scrippsnetworks.wcm.cache.TopicPageCacheService;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Component(immediate = true, metatype = true, label = "Topic Page Cache Service", description = "Cache for topic pages for a given topic tag")
@Service(value = TopicPageCacheService.class)
public class TopicPageCacheServiceImpl implements TopicPageCacheService {

    static final Cache<String, String> cache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(60l, TimeUnit.MINUTES)
            .recordStats()
            .build();

    private final static Logger log = LoggerFactory.getLogger(TopicPageCacheServiceImpl.class);

    private static String getCacheKey(String rawTag, String brand) {
        return String.format("%s_%s", brand, rawTag);
    }

    @Override
    public String getTopicPagePath(final Resource resource, final String rawTag, final String brand) {
        try {
            log.debug(String.format("Checking cache for key: %s",getCacheKey(brand,rawTag)));
            return cache.get(getCacheKey(brand,rawTag),
                    new Callable<String>() {
                        public String call() throws Exception {
                            try {
                                log.debug(String.format("Not found in cache, querying for topic cache key: %s",getCacheKey(brand,rawTag)));
                                QueryManager queryManager;
                                queryManager = resource
                                        .adaptTo(Node.class)
                                        .getSession()
                                        .getWorkspace()
                                        .getQueryManager();
                                StringBuilder queryBuilder = new StringBuilder();
                                queryBuilder
                                        .append("/jcr:root/content/")
                                        .append(brand)
                                        .append("/topics//element(*,cq:PageContent)[@sni:assetType='TOPIC' and @sni:primaryTopicTag='")
                                        .append(rawTag)
                                        .append("']");
                                Query compiledQuery = queryManager
                                        .createQuery(queryBuilder.toString(), Query.XPATH);
                                compiledQuery.setLimit(1);
                                NodeIterator nodes = compiledQuery.execute().getNodes();
                                if (nodes.hasNext()) {
                                    Node node = nodes.nextNode();
                                    String path = node.getParent().getPath();
                                    if (StringUtils.isNotBlank(path)) {
                                        return path;
                                    }
                                }
                            } catch (RepositoryException re) {
                                log.error("RepositoryException caught: ", re);
                            } catch (Exception e) {
                                log.error("Exception caught: ", e);
                            }
                            return "";
                        }

                    });
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return "";
    }

    @Override
    public void invalidate(String rawTag, String brand) {
        cache.invalidate(getCacheKey(brand,rawTag));
    }

    @Override
    public void invalidateAll() {
        cache.invalidateAll();
    }

}


