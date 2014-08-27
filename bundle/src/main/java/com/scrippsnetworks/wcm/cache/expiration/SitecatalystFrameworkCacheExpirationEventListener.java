package com.scrippsnetworks.wcm.cache.expiration;

import com.day.cq.wcm.api.PageEvent;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.PageModification;
import com.scrippsnetworks.wcm.cache.SitecatalystFrameworkCacheService;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.event.EventUtil;
import org.apache.sling.api.resource.LoginException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component( name = "com.scrippsnetworks.wcm.cache.expiration.SitecatalystFrameworkCacheExpirationEventListener", immediate = true, metatype = false, enabled = true)
@Service( value = org.osgi.service.event.EventHandler.class )
@Properties({
        @Property( name="description", value="Flushes sitecatalyst framwork cache items when configuration changes are made"),
        @Property( name="event.topics", value= PageEvent.EVENT_TOPIC)
})
public class SitecatalystFrameworkCacheExpirationEventListener implements EventHandler {

    private static final Logger log = LoggerFactory
            .getLogger(SitecatalystFrameworkCacheExpirationEventListener.class);

    private static final String sitecatalystPath = "/etc/cloudservices/sitecatalyst";

    @Reference
    private SitecatalystFrameworkCacheService sitecatalystFrameworkCacheService;

    @Override
    public void handleEvent(Event event) {
        try {
            log.debug("SitecatalystFrameworkCacheExpirationEventListener handleEvent");
            if(EventUtil.isLocal(event)) {
                final Iterator<PageModification> mods = PageEvent.fromEvent(event)
                        .getModifications();
                boolean doFlush = false;
                while (mods.hasNext()) {
                    final PageModification pm = mods.next();
                    if (pm.getPath().startsWith(sitecatalystPath)) {
                        doFlush = true;
                        log.debug("will invalidate cache for change to {}", pm.getPath());
                        break;
                    }
                }

                if (sitecatalystFrameworkCacheService != null) {
                    // racing, but that's why we have a try/catch
                    sitecatalystFrameworkCacheService.invalidateAll();
                } else {
                    throw new ServiceMissingException("sitecatalystFrameworkCacheService not available, could not invalidate cache");
                }
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private class ServiceMissingException extends Exception {
        public ServiceMissingException() {
            super();
        }

        public ServiceMissingException(String message) {
            super(message);
        }
    }
}
