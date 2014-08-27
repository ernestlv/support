package com.scrippsnetworks.wcm.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.felix.scr.Component;
import org.apache.felix.scr.ScrService;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The CQ JDBC Pool bundle does not automatically start any pools which failed to
 * start due to a missing dependency. This listener handles that case.
 */
@org.apache.felix.scr.annotations.Component
public class DataSourceStartingBundleListener implements BundleListener, Runnable {

    private static final String COMPONENT_NAME = "com.day.commons.datasource.jdbcpool.JdbcPoolService";

    private final Logger logger = LoggerFactory.getLogger(DataSourceStartingBundleListener.class);

    @Reference
    private ScrService scrService;

    private ExecutorService queue;

    @SuppressWarnings("unused")
    @Activate
    private void activate(ComponentContext componentContext) {
        queue = Executors.newSingleThreadExecutor();
        componentContext.getBundleContext().addBundleListener(this);
    }

    @SuppressWarnings("unused")
    @Deactivate
    private void deactivate(ComponentContext componentContext) {
        componentContext.getBundleContext().removeBundleListener(this);
        queue.shutdown();
    }

    public void bundleChanged(BundleEvent event) {
        if (event.getType() == BundleEvent.RESOLVED) {
            queue.submit(this);
        }
    }

    public void run() {
        final Component[] components = scrService.getComponents(COMPONENT_NAME);
        if (components != null) {
            for (final Component component : components) {
                final Object componentId = component.getProperties().get(Constants.SERVICE_ID);
                if (component.getState() == Component.STATE_REGISTERED) {
                    logger.info("need to start {}", componentId);
                    try {
                        component.disable();
                    } catch (Exception e) {
                        // no-op; still try to enable
                    }
                    try {
                        component.enable();
                    } catch (Exception e) {
                        logger.warn("Unable to start data source {}", componentId);
                    }
                }
            }
        }
    }

}
