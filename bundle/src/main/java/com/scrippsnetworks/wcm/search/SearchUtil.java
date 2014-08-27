package com.scrippsnetworks.wcm.search;

import java.util.Collections;
import java.util.Map;
import java.lang.String;

import org.apache.sling.api.scripting.SlingScriptHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchUtil {

    private static Logger logger = LoggerFactory.getLogger(SearchUtil.class);
    
    @Deprecated
    public static Map<String, Object> getSearchResponseMap(SlingScriptHelper sling, String serviceName, Map<String, String> params) {
        SearchService searchService = sling.getService(SearchService.class);
        return getSearchResponseMap(searchService, serviceName, params);
    }

    public static Map<String, Object> getSearchResponseMap(SearchService searchService, String serviceName, Map<String, String> params) {
        Map<String, Object> map = null;

        SearchResponse sr = getSearchResponse(searchService, serviceName, params);

        if (sr != null) {
            if (sr.isValid()) {
                map = SearchObjectMapper.getAsMap(sr);
            } else {
                logger.warn("getSearchResponse: invalid response from {}", sr.getServiceRequestURL());
            }
        }

        if (map != null) {
            return map;
        } else {
            return Collections.EMPTY_MAP;
        }
    }

    @Deprecated
    public static SearchResponse getSearchResponse(SlingScriptHelper sling, String searchService, Map<String, String> params) {
        SearchService ss = sling.getService(SearchService.class);
        return getSearchResponse(ss, searchService, params);
    }

    public static SearchResponse getSearchResponse(SearchService searchService, String serviceName, Map<String, String> params) {
        Map<String,Object> map = null;

        SearchResponse sr = null;

        if (searchService != null) {
            SearchRequestHandler srh;
            try {
                srh = searchService.getSearchRequestHandler();
            } catch (IllegalStateException ise) {
                srh = null;
                logger.warn("getSearchResponse: illegal state exception from search service", ise);
            }

            if (srh != null) {
                sr = srh.getResponse(serviceName, params != null ? params : Collections.EMPTY_MAP);
                // Should never be null, the SearchResponse catches and wraps exceptions 
                logger.debug("getSearchResponse: request URL {}", sr.getServiceRequestURL());
                logger.debug("getSearchResponse: code {} in {} ms", sr.getHttpCode(), sr.getRequestTime());
            }
        } else {
            logger.warn("search service not available");
        }

        return sr;
    }
}
