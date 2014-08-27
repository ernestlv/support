package com.scrippsnetworks.wcm.search;

import java.lang.String;
import java.util.Map;

/**
 * Command object for making search requests. The handler does not keep any request-specific state, so it can be reused for
 * multiple requests.
 *
 * @author Scott Everett Johnson
 */
public interface SearchRequestHandler {

    /**
     * Gets a SearchResponse object encapsulating the response of a request and metadata about the request.
     * Getting the response implies making the request.
     *
     * @param serviceName A string value representing the name of the service to make a request of.
     * @param params A map of name,value pairs representing parameters for the search request.
     * @return SearchResponse containing a response body if the request succeeded, and metadata about the request.
     * The handler always returns a SearchResponse object, which is expected to indicate any failures itself.
     */
    public SearchResponse getResponse(String serviceName, Map<String,String> params);
}
