package com.scrippsnetworks.wcm.search.impl;

import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;

import com.scrippsnetworks.wcm.search.SearchRequestException;
import com.scrippsnetworks.wcm.search.SearchRequestHandler;
import com.scrippsnetworks.wcm.search.SearchResponse;

public class StaticSearchRequestHandlerImpl implements SearchRequestHandler {

    private final Map<String, String> responses;
    
    public StaticSearchRequestHandlerImpl(final Map<String, String> responses) {
        this.responses = responses;
    }

    @Override
    public SearchResponse getResponse(String serviceName, Map<String, String> params) {
        if (params != null) {
            String offset = params.get("offset");
            String numOfResults = params.get("numOfResults");
            String altKey = null;
            if (offset != null && numOfResults != null) {
                altKey = serviceName + "-" + offset + "-" + numOfResults;
            }
            if (altKey != null && responses.containsKey(altKey)) {
                return new SearchResponseImpl(serviceName, "static/" + altKey, HttpStatus.SC_OK, "OK", 1, null, responses.get(altKey));
            }
        }

        if (responses.containsKey(serviceName)) {
            return new SearchResponseImpl(serviceName, "static/" + serviceName, HttpStatus.SC_OK, "OK", 1, null, responses.get(serviceName));
        }

        return new SearchResponseImpl(serviceName, "static/" + serviceName, HttpStatus.SC_NOT_FOUND, "The static request value has not been configured.", -1, new SearchRequestException("The static request values has not been configured for serviceName: " + serviceName), null);
    }
    
    
    
}
