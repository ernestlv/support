package com.scrippsnetworks.wcm.search.impl;

import java.util.Dictionary;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.client.HttpClient;

import org.apache.sling.commons.osgi.OsgiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.search.SearchRequestHandler;

public class LiveSearchServiceDelegate implements SearchServiceDelegate {

    private static Logger log = LoggerFactory.getLogger(LiveSearchServiceDelegate.class);

    private static class ConfigState {

        private String serviceContext;
        private HttpClient httpClient;
        private HttpHost httpHost;
        private String siteName;

        private PoolingClientConnectionManager connMgr;
        boolean active;

        public ConfigState() {
            this.active = false;
        }

        public ConfigState(Dictionary props) throws ConfigStateCreationException {

            try {
                String serviceHostname = OsgiUtil.toString(props.get(SERVICE_HOSTNAME), null);
                int servicePort = OsgiUtil.toInteger(props.get(SERVICE_PORT), SearchServiceImpl.DEFAULT_SERVICE_PORT);
                serviceContext = OsgiUtil.toString(props.get(SERVICE_CONTEXT), null);
                int poolSize = OsgiUtil.toInteger(props.get(POOL_SIZE), SearchServiceImpl.DEFAULT_POOL_SIZE);
                int connectionTimeout = OsgiUtil.toInteger(props.get(CONNECTION_TIMEOUT), SearchServiceImpl.DEFAULT_CONNECTION_TIMEOUT);
                int socketTimeout = OsgiUtil.toInteger(props.get(SOCKET_TIMEOUT), SearchServiceImpl.DEFAULT_SOCKET_TIMEOUT);

                siteName = OsgiUtil.toString(props.get(SITE_NAME), null);

                httpHost = new HttpHost(serviceHostname, servicePort);
                HttpRoute httpRoute = new HttpRoute(httpHost);

                connMgr = new PoolingClientConnectionManager();
                connMgr.setMaxTotal(poolSize);
                connMgr.setMaxPerRoute(httpRoute, poolSize);

                httpClient = new DefaultHttpClient(connMgr);
                HttpParams httpParams = httpClient.getParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeout);
                HttpConnectionParams.setSoTimeout(httpParams, socketTimeout);

                active = true;
            } catch (Exception e) {
                if (connMgr != null) {
                    connMgr.shutdown();
                }
                connMgr = null;
                httpClient = null;
                httpHost = null;
                serviceContext = null;
                active = false;
                throw new ConfigStateCreationException("failed to configure Search Service", e);
            }
        }

        public void destroy() {
            if (connMgr != null) {
                connMgr.shutdown();
            }
            connMgr = null;
            httpClient = null;
            httpHost = null;
            serviceContext = null;
        }

        public boolean isActive() {
            return active;
        }

        public HttpClient getHttpClient() {
            return httpClient;
        }

        public HttpHost getHttpHost() {
            return httpHost;
        }

        public String getServiceContext() {
            return serviceContext;
        }

        public String getSiteName() {
            return siteName;
        }

    }

    private AtomicReference<ConfigState> configRef = new AtomicReference<ConfigState>();
    
    public LiveSearchServiceDelegate() {
    }
    
    @Override
    public boolean isActive() {
        return configRef.get() != null && configRef.get().isActive();
    }

    @Override
    public void deactivate() {
        log.info("deactivating Live Search Service Delegate");
        ConfigState newConfig = new ConfigState();
        ConfigState oldConfig = configRef.get();
        configRef.set(newConfig); // inactive configuration
        oldConfig.destroy();
    }


    @Override
    public void activate(Dictionary props) throws ConfigStateCreationException {
        ConfigState newConfig = null;
        try {
            newConfig = new ConfigState(props);
        } catch (ConfigStateCreationException se) {
            // catch here to create empty nonfunctional config, then rethrow
            newConfig = new ConfigState();
            throw new ConfigStateCreationException("creating nonfunctional config due to configuration error", se);
        } finally {
            configRef.set(newConfig);
        }
    }

    @Override
    public void modified(Dictionary props) throws ConfigStateCreationException {
        deactivate();
        activate(props);
    }

    /** {@inheritDoc} */
    @Override
    public SearchRequestHandler getSearchRequestHandler() {
        ConfigState configState = configRef.get();
        if (configState.isActive()) {
            // This is just a bean-type constructor, so there won't be an exception.
            return new SearchRequestHandlerImpl(configState.getSiteName(), configState.getHttpClient(), configState.getHttpHost(), configState.getServiceContext());
        } else {
            throw new IllegalStateException();
        }
    }

}
