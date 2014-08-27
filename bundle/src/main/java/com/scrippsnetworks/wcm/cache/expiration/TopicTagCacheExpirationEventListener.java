package com.scrippsnetworks.wcm.cache.expiration;

import com.day.cq.wcm.api.PageEvent;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.PageModification;
import com.scrippsnetworks.wcm.cache.TopicPageCacheService;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.*;
import org.apache.sling.event.EventUtil;
import org.apache.sling.event.jobs.JobProcessor;
import org.apache.sling.event.jobs.JobUtil;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Component( name = "com.scrippsnetworks.wcm.cache.expiration.TopicTagCacheExpirationEventListener", immediate = true, metatype = false, enabled = true)
@Service( value = org.osgi.service.event.EventHandler.class )
@Properties({
        @Property( name="description", value="Flushes topic tag to topic page cache items when topic page is activated/modified/deleted"),
        @Property( name="event.topics", value= PageEvent.EVENT_TOPIC)
})
public class TopicTagCacheExpirationEventListener implements EventHandler {

    private static final Logger log = LoggerFactory
            .getLogger(TopicTagCacheExpirationEventListener.class);

    private static final Pattern topicPathPattern = Pattern.compile("/content/[^/]+/topics/[^/]/.*");

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private TopicPageCacheService topicPageCacheService;

    @Override
    public void handleEvent(Event event) {
        try {
            log.debug("TopicTagCacheExpirationEventListener handleEvent");
            if(EventUtil.isLocal(event)) {
                final Iterator<PageModification> mods = PageEvent.fromEvent(event)
                        .getModifications();
                while (mods.hasNext()) {
                    final PageModification pm = mods.next();
                    if (pm.getType().equals(PageModification.ModificationType.CREATED) ||
                            pm.getType().equals(PageModification.ModificationType.DELETED) ||
                            pm.getType().equals(PageModification.ModificationType.MODIFIED))
                        log.debug("create job for {} on {}", pm.getType().name(), pm.getPath());
                        JobUtil.processJob(event, new TopicPageCacheExpirationJob(
                            resourceResolverFactory,pm.getType(), pm.getPath(),topicPageCacheService));
                }
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    @Activate
    protected void activate(final ComponentContext ctx) throws LoginException {
        log.info("activated component");
    }

    @Deactivate
    protected void deactivate(final ComponentContext ctx) throws InterruptedException {
        log.info("deactivated component");
    }

    private class ServiceMissingException extends Exception {
        public ServiceMissingException() {
            super();
        }

        public ServiceMissingException(String message) {
            super(message);
        }
    }

    protected class TopicPageCacheExpirationJob implements JobProcessor {

        private ResourceResolverFactory resolverFactory;

        private PageModification.ModificationType eventType;

        private String path;

        private TopicPageCacheService topicPageCacheService;

        public TopicPageCacheExpirationJob(ResourceResolverFactory resolverFactory, PageModification.ModificationType eventType, String path, TopicPageCacheService topicPageCacheService) {
            super();
            this.resolverFactory = resolverFactory;
            this.eventType = eventType;
            this.path = path;
            this.topicPageCacheService = topicPageCacheService;
        }

        @Override
        public boolean process(Event event) {
            ResourceResolver resolver = null;

            try {
                log.debug("processing job for {}", path);
                if (eventType.equals(PageModification.ModificationType.CREATED)
                        || eventType.equals(PageModification.ModificationType.MODIFIED)) {

                    if (resourceResolverFactory == null) {
                        throw new ServiceMissingException("ResourceResolverFactory not available");
                    }

                    resolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
                    PageManager pageManager = resolver.adaptTo(PageManager.class);
                    SniPage sniPage = PageFactory.getSniPage(pageManager, path);
                    if (sniPage!=null) {
                        ValueMap properties = sniPage.getProperties();
                        if (properties.containsKey("sni:primaryTopicTag")) {
                            if (topicPageCacheService == null) {
                                throw new ServiceMissingException("TopicPageCacheService not available");
                            }

                            log.debug("topic page changed, flushing relationship of primary tag tied to topic page for tag: {}, brand: {}",properties.get("sni:primaryTopicTag").toString(),sniPage.getBrand());
                            topicPageCacheService.invalidate(properties.get("sni:primaryTopicTag").toString(),sniPage.getBrand());
                        } else {
                            log.debug("ignoring event for path {}", path);
                        }
                    } else {
                        log.debug("could not retrieve page for {}", path);
                    }
                } else if (eventType.equals(PageModification.ModificationType.DELETED)) {
                    Matcher m = topicPathPattern.matcher(path);
                    if (m.matches()) {
                        if (topicPageCacheService == null) {
                            throw new ServiceMissingException("TopicPageCacheService not available");
                        }

                        log.debug("topic page deleted, flushing all cached topic tag to page relationships");
                        topicPageCacheService.invalidateAll();
                    } else {
                        log.debug("ignoring created event for {}", path);
                    }
                }
                return true;
            } catch (ServiceMissingException ex) {
                log.warn(ex.getMessage() + " returning false");
            } catch (Exception ex) {
                String msg = ex.getMessage() != null ? ex.getMessage() : "";
                log.error(msg + " handling " + path, ex);
            } finally {
                if (resolver != null) {
                    resolver.close();
                }
            }
            return false;
        }
    }
}
