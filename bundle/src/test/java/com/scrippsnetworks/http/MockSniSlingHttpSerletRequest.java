package com.scrippsnetworks.http;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.commons.testing.sling.MockSlingHttpServletRequest;

public class MockSniSlingHttpSerletRequest extends MockSlingHttpServletRequest {

    private Map<String, Object> attributes = new HashMap<String, Object>();
    private Map<String, String> parameters = new HashMap<String, String>();
    
    public MockSniSlingHttpSerletRequest(String resourcePath, String selectors,
            String extension, String suffix, String queryString,
            String requestPath, String scheme, String server, int port,
            String contextPath) {
        super(resourcePath, selectors, extension, suffix, queryString,
                requestPath, scheme, server, port, contextPath);
        setParameters(queryString);
    }

    public MockSniSlingHttpSerletRequest(String resourcePath, String selectors,
            String extension, String suffix, String queryString) {
        super(resourcePath, selectors, extension, suffix, queryString);
        setParameters(queryString);
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }
    
    @Override
    public void setAttribute(String name, Object o) {
        attributes.put(name, o);
    }
    
    @Override
    public String getParameter(String name) {
        return parameters.get(name);
    } 
    
    @Override
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        return null;
    }
    
    protected void setParameters(String queryString) {
        String[] params = queryString.split("&");
        for (String param : params) {
            String[] values = param.split("=");
            String name = values[0];
            String value = "";
            if (values.length == 2) {
                value = values[1];
            }
            parameters.put(name, value);
        }
    }

}
