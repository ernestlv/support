package com.scrippsnetworks.wcm.fnr.sitesearch.mobile;

import java.lang.String;

import java.util.*;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.AbstractSearchComponent;
import com.scrippsnetworks.wcm.asset.SearchTermMetadata;
import com.scrippsnetworks.wcm.fnr.sitesearch.*;
import com.scrippsnetworks.wcm.metadata.MetadataProperty;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.search.SearchResponse;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Encapsulates Food Network Site Search functionality.
 *
 * The object is intended to be constructed as a bean via taglib. On construction the request is inspected,
 * parameters extracted from it, and a service request is prepared and executed.
 *
 * Support for presentation elements such as tabs, filters, and pagination are provided by this class and
 * its inner supporting classes.
 */
public class SiteSearchComponent extends com.scrippsnetworks.wcm.fnr.sitesearch.SiteSearchComponent {
    /** Returns a list of tabs for presenting the tab interface. */
    public List<SearchTab> getSearchTabs() {
        if (tabs == null) {
            tabs = new ArrayList<SearchTab>();
            List<SearchType> order = Arrays.asList(SearchType.all, SearchType.mobile_recipes, SearchType.mobile_videos, SearchType.mobile_episodes);

            for (SearchType tab : order) {
                tabs.add(new SearchTab(tab, null,
                		currentSearchType != tab
                                ? urlHelper.getSearchUrlForSearchType(tab)
                                : null));
            }
        }
        return tabs;
    }
    
    public List<SortOption> getSortOptions() {
        if (sortOptions == null) {
            sortOptions = new ArrayList<SortOption>();
            if (currentSearchType == SearchType.mobile_recipes) {
                sortOptions.add(new SortOption(SortKey.relevancy.getLabel(),
                        currentSortKey != SortKey.relevancy
                                ? urlHelper.getSearchUrlForSortKey(SortKey.relevancy)
                                : null));
                sortOptions.add(new SortOption(SortKey.rating.getLabel(),
                        currentSortKey != SortKey.rating
                            ? urlHelper.getSearchUrlForSortKey(SortKey.rating)
                            : null));
                sortOptions.add(new SortOption(SortKey.popular.getLabel(),
                        currentSortKey != SortKey.popular
                            ? urlHelper.getSearchUrlForSortKey(SortKey.popular)
                            : null));
            }
        }
        return sortOptions;
    }
}
