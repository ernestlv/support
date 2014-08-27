package com.scrippsnetworks.wcm.cache;

import org.apache.sling.api.resource.Resource;
import java.util.List;

public interface RelationshipModelCacheService {

    public List<String> searchForAssets(Resource resource,
                                         String pathToAssetRoot,
                                         String resourceType,
                                         String searchTerm,
                                         String sortKey,
                                         String sortOrder);

}
