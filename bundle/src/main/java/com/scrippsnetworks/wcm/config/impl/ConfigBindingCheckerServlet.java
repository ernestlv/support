package com.scrippsnetworks.wcm.config.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;

/**
 * Servlet which verifies that all OSGi Configuration objects
 * are bound to bundle at the configured location.
 */
@SuppressWarnings("serial")
@SlingServlet(paths = "/bin/config/check")
public class ConfigBindingCheckerServlet extends SlingSafeMethodsServlet {

    @Reference
    private ConfigurationAdmin configAdmin;

    private BundleContext bundleContext;

    @Activate
    protected void activate(ComponentContext ctx) {
        this.bundleContext = ctx.getBundleContext();
    }

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException,
            IOException {
        Map<String, Bundle> bundles = new HashMap<String, Bundle>();
        for (Bundle bundle : bundleContext.getBundles()) {
            bundles.put(bundle.getLocation(), bundle);
        }

        response.setContentType("text/plain");
        List<Configuration> unboundConfigurations = new ArrayList<Configuration>();
        try {
            Configuration[] configs = configAdmin.listConfigurations(null);
            for (Configuration config : configs) {
                String location = config.getBundleLocation();
                if (!bundles.containsKey(location)) {
                    unboundConfigurations.add(config);
                }
            }
            PrintWriter writer = response.getWriter();
            if (unboundConfigurations.isEmpty()) {
                writer.println("OK");
            } else {
                writer.println("ERROR");
                for (Configuration config : unboundConfigurations) {
                    writer.printf("%s, bound to %s\n", config.getPid(), config.getBundleLocation());
                }
            }
            
        } catch (InvalidSyntaxException e) {
            // should not happen because we're passing 'null' as the filter
            throw new ServletException(e);
        }
    }

}
