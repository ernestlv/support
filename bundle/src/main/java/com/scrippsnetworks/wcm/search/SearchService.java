package com.scrippsnetworks.wcm.search;

import java.lang.String;

/**
 * Provides search request handler objects for making search requests.
 *
 * @author Scott Everett Johnson
 */
public interface SearchService {

    /**
     * @return A SearchRequestHandler object which can be used to make a search request.
     */
    public SearchRequestHandler getSearchRequestHandler();
}
