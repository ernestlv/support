package com.scrippsnetworks.wcm.config.impl;


import com.scrippsnetworks.wcm.config.SiteConfigService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiteConfigUtil {

    private static final Logger log = LoggerFactory.getLogger(SiteConfigUtil.class);

    public static SiteConfigService getSiteConfigService(SlingHttpServletRequest request) {
	    String path = request.getRequestPathInfo().getResourcePath();
        path = path.substring(path.lastIndexOf("/content/") + "/content/".length());
        path = path.substring(0, path.indexOf("/"));

        try {
            BundleContext bundleContext = FrameworkUtil.getBundle(SiteConfigService.class).getBundleContext();
            String filter = "(siteName=" + path + ")";
            ServiceReference[] serviceReference = bundleContext.getServiceReferences(SiteConfigService.class.getName(),filter);
            return (SiteConfigService)bundleContext.getService(serviceReference[0]);

        } catch (Exception ex) {
            log.error(ex.getMessage(),ex);
        }
        return null;
    }
}

