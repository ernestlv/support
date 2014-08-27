package com.scrippsnetworks.wcm;

import java.lang.String;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import com.day.cq.wcm.api.Page;
import org.apache.sling.api.scripting.SlingScriptHelper;
import com.scrippsnetworks.wcm.search.SearchService;
import com.scrippsnetworks.wcm.search.SearchUtil;
import com.scrippsnetworks.wcm.search.SearchResponse;
import com.scrippsnetworks.wcm.search.SearchObjectMapper;
import com.scrippsnetworks.wcm.page.SniPage;
import javax.servlet.http.HttpServletRequest;

/**
 * Base Class for the Search Components
 * 
 * @author Scott Johnson
 * 
 */
public abstract class AbstractSearchComponent extends AbstractComponent {

    private SniPage currentSniPage;
    private SearchService searchService;
    private Map<String, Object> dynamicAttributes = new HashMap<String, Object>();
    private String serviceName;

    // These get set lazily by accessors.
    private Map<String, Object> searchResponseMap = null;
    private SearchResponse searchResponse = null;

    public abstract Map<String, String> getSearchParameters();

    public void setCurrentSniPage(SniPage currentSniPage) {
        this.currentSniPage = currentSniPage;
    }

    public void setDynamicAttributes(Map<String, Object> dynamicAttributes) {
        this.dynamicAttributes = dynamicAttributes;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public SniPage getCurrentSniPage() {
        return currentSniPage;
    }

    public Map<String, Object> getDynamicAttributes() {
        return dynamicAttributes;
    }

    public String getServiceName() {
        return serviceName;
    }


    public SearchService getSearchService() {
        return searchService;
    }

    public SearchResponse getSearchResponse() {
        if (searchResponse == null) {
            if (serviceName == null || serviceName.isEmpty()) {
                throw new RuntimeException("must provide serviceName");
            }

            SearchService searchService = getSearchService();
            if (searchService != null) {
                searchResponse = SearchUtil.getSearchResponse(searchService, serviceName, getSearchParameters());
            } else {
                log.error("cannot retrieve search service");
            }
        }
        return searchResponse;
    }

    public Map<String, Object> getSearchResponseMap() {
        if (searchResponseMap == null) {
            SearchResponse sr = getSearchResponse();
            if (sr != null) {
                if (sr.isValid()) {
                    // an invalid response is not an error condition, might just be no results
                    searchResponseMap = SearchObjectMapper.getAsMap(sr);
                }
            } else {
                // Should never happen as a searchRequestHandler always returns a SearchResponse object.
                log.error("search response is null");
            }
        }
        return searchResponseMap;
    }
}
