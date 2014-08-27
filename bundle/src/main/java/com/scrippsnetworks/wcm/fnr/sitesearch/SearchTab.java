package com.scrippsnetworks.wcm.fnr.sitesearch;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a tab in the tab bar.
 */
public class SearchTab {
    private final SearchType searchType;
    private final Integer count;
    private final String url;

    public SearchTab(SearchType searchType, Integer count, String url) {
        this.searchType = searchType;
        this.count = count;
        this.url = url;
    }

    public String getLabel() {
        return searchType != null ? searchType.getLabel() : SearchType.all.getLabel();
    }

    public String getUrl() {
        return url;
    }

    public Integer getCount() {
        return count;
    }

    public List<FacetKey> getFacetList() {
        if (SearchType.recipes.equals(searchType)) {
            return Arrays.asList(FacetKey.TALENT, FacetKey.SHOW, FacetKey.COURSE, FacetKey.MAIN_INGREDIENT, FacetKey.SOURCE);
        } else if (SearchType.episodes.equals(searchType)) {
            return Arrays.asList(FacetKey.SHOW, FacetKey.TALENT, FacetKey.AIRDATE);
        } else {
            return Collections.EMPTY_LIST;
        }
    }
}
