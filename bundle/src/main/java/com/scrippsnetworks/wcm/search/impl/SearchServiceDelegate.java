package com.scrippsnetworks.wcm.search.impl;

import java.util.Dictionary;

import com.scrippsnetworks.wcm.search.SearchService;

public interface SearchServiceDelegate extends SearchService {
    boolean isActive();
    void deactivate();
    void activate(Dictionary props) throws ConfigStateCreationException;
    void modified(Dictionary props) throws ConfigStateCreationException;
    
    public static final String RESOURCE_LOCATIONS = "sni.wcm.search.resourceLocations";
    public static final String SERVICE_HOSTNAME = "sni.wcm.search.servicehostname"; 
    public static final String SERVICE_PORT = "sni.wcm.search.serviceport";
    public static final String SERVICE_CONTEXT = "sni.wcm.search.servicecontext";
    public static final String POOL_SIZE = "sni.wcm.search.poolsize";
    public static final String CONNECTION_TIMEOUT = "sni.wcm.search.connectiontimeout";
    public static final String SOCKET_TIMEOUT = "sni.wcm.search.sockettimeout";
    public static final String SITE_NAME = "siteName";
    
    
    public static final class ConfigStateCreationException extends Exception {
        public ConfigStateCreationException(String msg) {
            super(msg);
        }
        public ConfigStateCreationException(String msg, Throwable t) {
            super(msg, t);
        }
    }
}
