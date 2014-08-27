package com.scrippsnetworks.wcm.cache.expiration;

import com.day.cq.wcm.api.PageEvent;
import com.day.cq.wcm.api.PageModification;
import com.day.cq.wcm.api.PageModification.ModificationType;
import com.scrippsnetworks.wcm.cache.RegionCacheService;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.EventUtil;
import org.apache.sling.event.jobs.JobUtil;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * @author Jason Clark
 *         Date: 11/12/13
 */
@Component( name = "com.scrippsnetworks.wcm.cache.expiration.RegionCacheExpirationEventListener", immediate = true, metatype = false, enabled = true)
@Service( value = org.osgi.service.event.EventHandler.class )
@Properties({
        @Property( name="description", value="Flushes region cache when region component is activated/modified/deleted"),
        @Property( name="event.topics", value= PageEvent.EVENT_TOPIC)
})
public class RegionCacheExpirationEventListener implements EventHandler {

    private static final Logger log = LoggerFactory.getLogger(RegionCacheExpirationEventListener.class);

    @Reference
    private RegionCacheService regionCacheService;

    @Reference
    private ResourceResolverFactory resolverFactory;

    /** If a region is created, modified, deleted we flush it from the cache. */
    public void handleEvent(final Event event) {
        log.debug("RegionCacheExpirationEventListener handleEvent");
        if (EventUtil.isLocal(event)) {
            final Iterator<PageModification> modifications = PageEvent.fromEvent(event).getModifications();
            while (modifications.hasNext()) {
                final PageModification modification = modifications.next();
                ModificationType type = modification.getType();
                if (type.equals(ModificationType.CREATED)
                        || type.equals(ModificationType.MODIFIED)
                        || type.equals(ModificationType.DELETED)) {
                    JobUtil.processJob(event,
                            new RegionCacheExpirationJob(modification.getPath(), resolverFactory, regionCacheService));
                }
            }
        }
    }

}
