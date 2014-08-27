package com.scrippsnetworks.wcm.replication.impl;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.StringUtils;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyOption;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.jcrclustersupport.ClusterAware;
import com.day.cq.replication.Replicator;
import com.day.cq.replication.ReplicationException;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageEvent;
import com.day.cq.wcm.api.PageModification;
import com.day.cq.wcm.api.PageModification.ModificationType;

import com.scrippsnetworks.wcm.fnr.util.AssetRootPaths;
import com.scrippsnetworks.wcm.replication.ReplicationManager;
import com.scrippsnetworks.wcm.impl.NamedThreadFactory;

/**
 * <code>SNIAssetReplicationHandler</code> implements a service that listens for
 * page modification events from /etc/sni-assets and calls on the SNI Replication Manager to perform the appropriate replication action.
 *
 */
@Component(enabled = true, immediate = true, metatype = true,
    configurationFactory = true, policy = ConfigurationPolicy.REQUIRE,
    label = "SNI WCM Asset Replication Event Handler",
    description = "Handler that listens for ani-asset changes and activate pages.")
@Service(value = EventHandler.class)
@Property(name = EventConstants.EVENT_TOPIC, value = PageEvent.EVENT_TOPIC)
public class SNIAssetReplicationHandler implements EventHandler {

    private static final long serialVersionUID = 1L;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private boolean active;
    private AtomicLong counter;
    private ScheduledExecutorService queue;
    protected ResourceResolver resourceResolver;
    private Session session;

    private static final String CONTENT_SPONSORSHIPS_PATH = "/content/sponsorships";

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private Replicator replicator = null;    
    
    @Reference
    private ReplicationManager replicationManager;    

    private static final int DEFAULT_RETRY_LIMIT = 10;
    private static final long DEFAULT_RETRY_DELAY = 300;
    private static final String DEFAULT_RETRY_TIMEUNIT = "SECONDS";

    @Property(label = "Retry Limit", description = "Maximum retry attempts after a failed activation", intValue = DEFAULT_RETRY_LIMIT)
    private static final String RETRY_LIMIT = "retryLimit";

    @Property(label = "Retry Delay", description = "Interval between activation attempts", longValue = DEFAULT_RETRY_DELAY)
    private static final String RETRY_DELAY = "retryDelay";

    @Property(label = "Retry Time Unit", description = "Interval time unit", value = DEFAULT_RETRY_TIMEUNIT,
        options = {
            // The "name" parameter is derived from TimeUnit enum, but property attribute must be a constant
            @PropertyOption(name = "SECONDS", value = "Seconds"),
            @PropertyOption(name = "MINUTES", value = "Minutes"),
            @PropertyOption(name = "HOURS", value = "Hours"),
            @PropertyOption(name = "DAYS", value = "Days")
        }
    )
    private static final String RETRY_TIMEUNIT_STRING = "retryTimeUnitString";

    private int retryLimit;
    private long retryDelay;
    private String retryTimeUnitString;
    private TimeUnit retryTimeUnit;

    public void handleEvent(Event event) {
        logger.debug("handleEvent called");
        final PageEvent pageEvent = PageEvent.fromEvent(event);
        final Iterator<PageModification> modifications = pageEvent.getModifications();
        while (modifications.hasNext()) {
            final PageModification mod = modifications.next();
            logger.trace("Inside modifications while");
            if (shouldActivate(mod)) {
                if (mod.getPath().startsWith(AssetRootPaths.ASSET_ROOT.path()) || mod.getPath().startsWith(CONTENT_SPONSORSHIPS_PATH)){
                    logger.debug("Modification path passes all conditions");
                    switch (mod.getType()) {
                        case CREATED:
                            enqueueUpdate(mod.getPath(),Type.CREATED);
                            break;                        
                        case MODIFIED:
                            enqueueUpdate(mod.getPath(),Type.MODIFIED);
                            break;
                        default:
                            // ignore all others
                    }                    
                }
            }
        }
    }

    private void enqueueUpdate(String path, Type type) {
        counter.incrementAndGet();
        logger.info("Queuing executor for : " + path);
        queue.submit(new ActivatePage(path, type));
    }

    private void enqueueRetry(String path, Type type, int attempts) {
        counter.incrementAndGet();
        logger.warn("Queuing scheduled executor to retry {}/{} for : " + path, attempts+1, retryLimit);
        queue.schedule(new ActivatePage(path, type, attempts+1), retryDelay, retryTimeUnit);
    }

    @Activate
    protected void activate(final ComponentContext ctx) throws LoginException {
        Dictionary<?, ?> props = ctx.getProperties();

        retryLimit = OsgiUtil.toInteger(props.get(RETRY_LIMIT), DEFAULT_RETRY_LIMIT);
        retryDelay = OsgiUtil.toLong(props.get(RETRY_DELAY), DEFAULT_RETRY_DELAY);
        retryTimeUnit = TimeUnit.valueOf(OsgiUtil.toString(props.get(RETRY_TIMEUNIT_STRING), DEFAULT_RETRY_TIMEUNIT));

        logger.info("Retry Limit {}", retryLimit);
        logger.info("Retry delay {}", retryDelay);
        logger.info("Retry timeunit {}", retryTimeUnit.name());

        queue = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(getThreadPrefix()));
        resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
        session = resourceResolver.adaptTo(Session.class);
        counter = new AtomicLong();
    }

    @Deactivate
    protected void deactivate(final ComponentContext ctx) throws InterruptedException {
        active = false;
        queue.shutdown();
        try {
            queue.awaitTermination(500, TimeUnit.MILLISECONDS);
        } finally {
            resourceResolver.close();
            resourceResolver = null;
            session = null;
            counter = null;
        }
        logger.info("deactivated component {}", getClass().getName());
    }

    protected String getThreadPrefix() {
        return StringUtils.substringAfterLast(this.getClass().getName(), ".");
    }

    /**
     * Determine if the provided PageModification should trigger a property
     * update. Defaults to handling either created or modified events.
     */
    protected boolean shouldActivate(PageModification mod) {
        return mod.getType() == ModificationType.CREATED || mod.getType() == ModificationType.MODIFIED;
    }


    private static enum Type {
        MODIFIED, MOVED, CREATED, DELETE, ON, OFF, MODIFIED_DISTRIBUTE
    }    
    
    private class ActivatePage implements Runnable {
        private final String path;
        private final Type type;
        private final int attempts;

        public ActivatePage(String path, Type type) {
            this.path = path;
            this.type = type;
            this.attempts = 0;
        }

        public ActivatePage(String path, Type type, int attempts) {
            this.path = path;
            this.type = type;
            this.attempts = attempts;
        }

        public void run() {
            try {
                switch (type) {
                    case CREATED:
                        //notify replication manager sni-asset created
                        logger.debug("ReplicationManager notified as a new sni-asset created: " + path);
                        replicationManager.sniAssetCreated(resourceResolver, replicator, path);
                        break;                        
                    case MODIFIED:
                        //notify replication manager of change to sni-asset
                        logger.debug("ReplicationManager notified of change to sni-asset or content page: " + path);
                        replicationManager.sniAssetModified(resourceResolver, replicator, path);
                        break;
                    default:
                        //do nothing
                }                    
            } catch (ReplicationException e) {
                logger.warn("An error occurred during replication of {}: {}", path, e.toString());
                if (attempts < retryLimit) {
                    enqueueRetry(path, type, attempts);
                } else {
                    logger.error("Exceeded retry limit attempting to activate {}: {}", path, e.toString());
                }
            }
            long currentCounter = counter.decrementAndGet();

            // only log the count every 100
            if (currentCounter > 0 && (currentCounter % 100 == 0)) {
                logger.debug("remaining pages to activate: {}", currentCounter);
            }
        }
    }

}
