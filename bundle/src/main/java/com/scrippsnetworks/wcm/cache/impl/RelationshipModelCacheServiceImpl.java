package com.scrippsnetworks.wcm.cache.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.scrippsnetworks.wcm.cache.RelationshipModelCacheService;
import com.scrippsnetworks.wcm.cache.TopicPageCacheService;
import com.scrippsnetworks.wcm.relationship.impl.RelationshipModelImpl;
import com.scrippsnetworks.wcm.fnr.util.AssetRootPaths;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Component(immediate = true, metatype = true, label = "Topic Page Cache Service", description = "Cache for topic pages for a given topic tag")
@Service(value = RelationshipModelCacheService.class)
public class RelationshipModelCacheServiceImpl implements RelationshipModelCacheService {

    private static final String SLING_RESOURCETYPE = "sling:resourceType";
    private static final String JCR_CONTENT = "jcr:content";
    private static final String SORT_ORDER_ASC = "ASC";
    private static final String SORT_ORDER_DESC = "DESC";

    static final Cache<String, List<String>> cache = CacheBuilder.newBuilder()
            .maximumSize(100000)
            .expireAfterAccess(60l, TimeUnit.MINUTES)
            .recordStats()
            .build();

    private final static Logger log = LoggerFactory.getLogger(RelationshipModelCacheServiceImpl.class);

    private static String getCacheKey(String pathToAssetRoot,
                                      String resourceType,
                                      String searchTerm,
                                      String sortKey,
                                      String sortOrder) {

        return String.format("%s_%s_%s_%s_%s", pathToAssetRoot,resourceType,searchTerm,sortKey,sortOrder);
    }

    @Override
    public List<String> searchForAssets(final Resource resource,
                                        final String pathToAssetRoot,
                                        final String resourceType,
                                        final String searchTerm,
                                        final String sortKey,
                                        final String sortOrder) {
        try {
            log.debug(String.format("Checking cache for key: %s",getCacheKey(pathToAssetRoot,resourceType,searchTerm,sortKey,sortOrder)));
            return cache.get(getCacheKey(pathToAssetRoot,resourceType,searchTerm,sortKey,sortOrder),
                    new Callable<List<String>>() {
                        public List<String> call() throws Exception {
                            List<String> assets = new ArrayList<String>();
                            try {
                                log.debug(String.format("Not found in cache, querying for relationshipmodel cache key: %s",getCacheKey(pathToAssetRoot,resourceType,searchTerm,sortKey,sortOrder)));
                                boolean isSorted = StringUtils.isNotBlank(sortKey);
                                boolean hasSortOrder = StringUtils.isNotBlank(sortOrder);
                                String querySearchTerm;
                                if (searchTerm == null) {
                                    querySearchTerm = "";
                                } else if (pathToAssetRoot.startsWith(AssetRootPaths.SHOWS.path()) &&
                                    searchTerm.startsWith(AssetRootPaths.ASSET_ROOT.path())) {
                                    //If we are looking for episodes with a particular pagetype, use sni:[pagetype] of the episode
                                    //This is because some [pagetype] paths only differ by a number or "-[pagetype]" at the end, so we need to make an exact match.
                                    // this was the case for recipes --> FNRHL-764

                                    String type = "";

                                    Node assetNode = resource.getResourceResolver().getResource(searchTerm).adaptTo(Node.class);
                                    if (assetNode.hasNode("jcr:content")) {
                                        Node jcrContent = assetNode.getNode("jcr:content");
                                        if (jcrContent.hasProperty("sling:resourceType")) {
                                            String[] paths = jcrContent.getProperty("sling:resourceType").getString().split("/");
                                            type = paths[paths.length-1];
                                            if (!type.endsWith("s")) type += "s";
                                        }
                                    }

                                    querySearchTerm = "and s.'sni:" + type + "' LIKE '" + searchTerm + "'";
                                } else {
                                    querySearchTerm = "and CONTAINS(s.*,'" + searchTerm + "')";
                                }
                                QueryManager queryManager;
                                queryManager = resource
                                        .adaptTo(Node.class)
                                        .getSession()
                                        .getWorkspace()
                                        .getQueryManager();
                                StringBuilder queryBuilder = new StringBuilder();

                                String queryString = "SELECT * FROM [nt:base] AS s WHERE ISDESCENDANTNODE([" +
                                        pathToAssetRoot + "]) AND NAME(s) = '" + JCR_CONTENT + "' " +
                                        querySearchTerm + " and CONTAINS(s.'" + SLING_RESOURCETYPE +
                                        "', '" + resourceType + "')";

                                if (isSorted) {
                                    queryString += " order by [" + sortKey + "] ";
                                    if (hasSortOrder && (sortOrder.equalsIgnoreCase(SORT_ORDER_ASC)
                                            || sortOrder.equalsIgnoreCase(SORT_ORDER_DESC))) {
                                        queryString += sortOrder;
                                    } else {
                                        queryString += SORT_ORDER_ASC;
                                    }
                                }

                                log.debug("QUERY STRING:");
                                log.debug(queryString);
                                queryBuilder.append(queryString);

                                Query compiledQuery = queryManager.createQuery(queryBuilder.toString(), Query.JCR_SQL2);
                                NodeIterator nodeItr = compiledQuery.execute().getNodes();

                                while (nodeItr.hasNext()) {
                                    Node node = nodeItr.nextNode();
                                    Resource nodeResource = resource.getResourceResolver().getResource(node.getPath());
                                    if (nodeResource != null) {
                                        assets.add(node.getPath());
                                    }
                                }
                            } catch (RepositoryException re) {
                                log.error("RepositoryException caught: ", re);
                            } catch (Exception e) {
                                log.error("Exception caught: ", e);
                            }

                            return assets;
                        }

                    });
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return new ArrayList<String>();
    }


}


