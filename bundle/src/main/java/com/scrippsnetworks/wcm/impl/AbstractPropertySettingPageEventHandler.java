package com.scrippsnetworks.wcm.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
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
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.jcrclustersupport.ClusterAware;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageEvent;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.PageModification;
import com.day.cq.wcm.api.PageModification.ModificationType;

/**
 * Abstract base class for components which set a property (or properties) when
 * a page changes. Also has support for performing a batch update when the
 * component is activated.
 */
@Component(componentAbstract = true)
@org.apache.felix.scr.annotations.Property(name = EventConstants.EVENT_TOPIC, value = PageEvent.EVENT_TOPIC)
public abstract class AbstractPropertySettingPageEventHandler implements EventHandler, ClusterAware {

    private static final int SAVE_INTERVAL = 1000;

    private boolean active;

    private AtomicLong counter;

    private PageManager pageManager;

    private ExecutorService queue;

    protected ResourceResolver resourceResolver;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private Session session;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public void bindRepository(String repositoryId, String clusterId, boolean isMaster) {
        active = isMaster;
        if (active) {
            queue.submit(new InitialTask());
        }
    }

    public void handleEvent(Event event) {
        final PageEvent pageEvent = PageEvent.fromEvent(event);
        final Iterator<PageModification> modifications = pageEvent.getModifications();
        while (modifications.hasNext()) {
            final PageModification mod = modifications.next();
            if (shouldUpdate(mod)) {
                enqueueUpdate(mod.getPath());
            }
        }
    }

    public void unbindRepository() {
        active = false;
    }

    private void enqueueUpdate(String path) {
        counter.incrementAndGet();
        queue.submit(new UpdateTask(path));
    }

    @Activate
    protected void activate(final ComponentContext ctx) throws LoginException {
        queue = Executors.newSingleThreadExecutor(new NamedThreadFactory(getThreadPrefix()));
        resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
        pageManager = resourceResolver.adaptTo(PageManager.class);
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

    /**
     * Returns the query used for the initial batch update. If this method
     * returns null, no query is done.
     */
    protected abstract String getInitialQuery();

    protected String getThreadPrefix() {
        return StringUtils.substringAfterLast(this.getClass().getName(), ".");
    }

    /**
     * Determine if the provided PageModification should trigger a property
     * update. Defaults to handling either created or modified events.
     */
    protected boolean shouldUpdate(PageModification mod) {
        return mod.getType() == ModificationType.CREATED || mod.getType() == ModificationType.MODIFIED;
    }

    /**
     * Update the page properties. Return true if a change was made.
     */
    protected abstract boolean updateProperties(Page page) throws RepositoryException;

    private class InitialTask implements Runnable {

        @SuppressWarnings("deprecation")
        public void run() {
            final long timestamp = System.currentTimeMillis();
            logger.info("starting to run initial update");
            long totalCounter = 0;
            long iterationCounter = 0;
            int nonPageCounter = 0;
            int skipCounter = 0;
            try {
                final QueryManager qm = session.getWorkspace().getQueryManager();
                final String queryString = getInitialQuery();
                if (queryString != null) {
                    final Query query = qm.createQuery(queryString, Query.XPATH);
                    final QueryResult results = query.execute();
                    final RowIterator rows = results.getRows();
                    final int queryResultSize = (int) rows.getSize();
                    logger.info("finished with initial query. Took {} ms and returned {} results.",
                            (System.currentTimeMillis() - timestamp), queryResultSize);
                    int counter = 0;
                    final List<String> paths = new ArrayList<String>(queryResultSize);
                    while (rows.hasNext()) {
                        paths.add(rows.nextRow().getPath());
                    }

                    for (final String path : paths) {
                        iterationCounter++;
                        if (!active) {
                            logger.info("while inside initial query results, deactivated, so breaking out of the loop");
                            break;
                        }
                        final Page page = pageManager.getPage(path);
                        if (page != null) {
                            if (updateProperties(page)) {
                                totalCounter++;
                                counter++;
                                if (counter == SAVE_INTERVAL) {
                                    logger.info("Reached save interval. Saving {} nodes... Total count is {} of {}.",
                                            new Object[] { counter, totalCounter, queryResultSize });
                                    session.save();
                                    counter = 0;
                                }
                            } else {
                                skipCounter++;
                            }
                        } else {
                            nonPageCounter++;
                        }
                    }
                    if (active && counter > 0) {
                        logger.info("Last save interval. Saving {} nodes ...", counter);
                        session.save();
                    }
                    logger.info(
                            "finished with initial update set. Took {} ms. Iterated over {} nodes. Saved a total of {} nodes. Skipped {} nodes. Encountered {} non-page nodes.",
                            new Object[] { (System.currentTimeMillis() - timestamp), iterationCounter, totalCounter,
                                    skipCounter, nonPageCounter });
                }
            } catch (RepositoryException e) {
                logger.error("Unable to run initial update", e);
            }
        }

    }

    private class UpdateTask implements Runnable {
        private final String path;

        public UpdateTask(String path) {
            this.path = path;
        }

        public void run() {
            if (active) {
                try {
                    session.refresh(false);
                    final Page page = pageManager.getPage(path);
                    if (page != null) {
                        if (updateProperties(page)) {
                            session.save();
                        }
                    }
                } catch (RepositoryException e) {
                    logger.error("Unable to update properties for " + path, e);
                }
            }
            long currentCounter = counter.decrementAndGet();

            // only log the count every 100
            if (currentCounter > 0 && (currentCounter % 100 == 0)) {
                logger.info("remaining pages to update: {}", currentCounter);
            }
        }
    }

}
