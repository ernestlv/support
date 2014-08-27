package com.scrippsnetworks.wcm.search.impl;
 
import java.util.Dictionary;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.component.ComponentContext;

import com.scrippsnetworks.wcm.search.SearchRequestHandler;
import com.scrippsnetworks.wcm.search.SearchService;
import com.scrippsnetworks.wcm.search.impl.SearchServiceDelegate.ConfigStateCreationException;

/**
 * Implements the Search Service, which provides search request handler objects using pooled persistent HTTP connections.
 * @author Scott Everett Johnson
 */
@Component(label="SNI WCM Search Service",description="Provides access to search requests using pooled persistent HTTP connections or static resources",immediate=true,metatype=true,enabled=true,configurationFactory = true, policy = ConfigurationPolicy.REQUIRE)
@Service(value=SearchService.class)
public class SearchServiceImpl implements SearchService {
    
    public static final int DEFAULT_SERVICE_PORT = 80;
    public static final int DEFAULT_POOL_SIZE = 128;
    public static final int DEFAULT_CONNECTION_TIMEOUT = 2000;
    public static final int DEFAULT_SOCKET_TIMEOUT = 2000;

    /** Name of OSGi property setting the hostname of the search service endpoint. */
    @Property(label="Hostname",description="Hostname of service endpoint",value="search.stage-dev1.aws.foodnetwork.com")
    public static final String SERVICE_HOSTNAME = SearchServiceDelegate.SERVICE_HOSTNAME; 

    /** Name of OSGi property setting the TCP port of the search service endpoint. */
    @Property(label="Port",description="Port of service endpoint",intValue=80)
    public static final String SERVICE_PORT = SearchServiceDelegate.SERVICE_PORT;

    /** Name of OSGi property setting the context path of the search service endpoint. */
    @Property(label="Service Context",description="Path context of service endpoint",value="/services/food")
    public static final String SERVICE_CONTEXT = SearchServiceDelegate.SERVICE_CONTEXT;

    /** Name of OSGi property setting the pool size . */
    @Property(label="Pool Size",description="Size of the pool of persistent connections",intValue=128)
    public static final String POOL_SIZE = SearchServiceDelegate.POOL_SIZE;

    /** Name of OSGi property setting the connection timeout for search requests. */
    @Property(label="Connection Timeout (ms)",description="Socket-level connection timeout in milliseconds",intValue=1000)
    public static final String CONNECTION_TIMEOUT = SearchServiceDelegate.CONNECTION_TIMEOUT;

    /** Name of OSGi property setting the socket (inactivity) timeout for search requests. */
    @Property(label="Socket Timeout (ms)",description="Socket-level inactivity timeout in milliseconds",intValue=1000)
    public static final String SOCKET_TIMEOUT = SearchServiceDelegate.SOCKET_TIMEOUT;
    
    @Property(label="Resource Locations",description="Comma separated list of resources which hold static responses for search queries.",value="")
    public static final String RESOURCE_LOCATIONS = SearchServiceDelegate.RESOURCE_LOCATIONS;
    
    @Property(label="Site Name",description="Site name to be used for filtering.",value="food")
    public static final String SITE_NAME = SearchServiceDelegate.SITE_NAME;

    @Reference private ResourceResolverFactory resolverFactory;
    
    /** Apache commons log. */
    // Can throw LogConfigurationException. Presumably we don't want to go on without logging.
    private static Log log = LogFactory.getLog(SearchServiceImpl.class);

    private AtomicReference<SearchServiceDelegate> configRef;

    public SearchServiceImpl() {
        configRef = new AtomicReference<SearchServiceDelegate>(new StaticSearchServiceDelegate(resolverFactory));
    }

    /**
     * Activates this OSGi component, setting its properties from the ComponentContext and
     * initializing its state.
     * @throws ConfigStateCreationException 
     */
    @Activate
    protected void activate(ComponentContext ctx) throws ConfigStateCreationException {
        activate(ctx.getProperties());
    }
    
    protected void activate(Dictionary props) throws ConfigStateCreationException {
        String resourceLocations = OsgiUtil.toString(props.get(RESOURCE_LOCATIONS), null);
        if(resourceLocations != null && !resourceLocations.trim().isEmpty()) {
            log.debug(String.format("Setting up the static search service, using locations {%s}", props.get(RESOURCE_LOCATIONS)));
            configRef.getAndSet(new StaticSearchServiceDelegate(resolverFactory));
        } else {
            log.debug("Setting up the live search service");    
            configRef.getAndSet(new LiveSearchServiceDelegate());
        }
        configRef.get().activate(props);
    }

    /**
     * Deactivates this OSGi component, cleaning up any state.
     */
    @Deactivate
    protected void deactivate(ComponentContext ctx) {
        configRef.get().deactivate();
    }

    /**
     * Updates the state of this OSGi component when the ComponentContext has changed.
     */
    @Modified
    protected void modified(ComponentContext ctx) throws ConfigStateCreationException {
        deactivate(ctx);
        activate(ctx);
    }
 
    /** {@inheritDoc} */
    public SearchRequestHandler getSearchRequestHandler() {
        SearchServiceDelegate delegate = configRef.get();
        if (delegate.isActive()) {
            return delegate.getSearchRequestHandler();
        } else {
            throw new IllegalStateException("The configured SearchRequestDelegate is inavtive.");
        }
    }

}
