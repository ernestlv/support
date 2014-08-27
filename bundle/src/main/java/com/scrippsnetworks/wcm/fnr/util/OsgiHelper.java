package com.scrippsnetworks.wcm.fnr.util;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This util was created to access OSGi services from the context of jsp
 * It is created as an instantiable Helper instead of static Utility, in order for it to be more testable/mockable
 *
 * TODO core and arch may decide to refactor this into Core package
 *
 * @author Ken Shih (156223)
 * @created 4/17/13 11:38 AM
 */
public class OsgiHelper {

    private static OsgiHelper singleton = new OsgiHelper();
    private static final Logger LOG = LoggerFactory.getLogger(OsgiHelper.class);

            /**
             * This utility method assumes it is being run in the context of an OSGi container
             * as such it will look for a service reference for a class sought by consumer
             * and return it.
             * @param className class interface (can be implementation) of service sought
             * @param <T> type of class interface (can be implementation)
             * @return service instance object
             */

    public <T> T getOsgiService(String className){
        BundleContext bundleContext = FrameworkUtil.getBundle(OsgiHelper.class).getBundleContext();
        ServiceReference serviceReference=bundleContext.getServiceReference(className);
        return (T) bundleContext.getService(serviceReference);
    }

    public <T> T getOsgiServiceBySite(String className,String site){
        BundleContext bundleContext;
        ServiceReference serviceReference;
        try {
            bundleContext = FrameworkUtil.getBundle(OsgiHelper.class).getBundleContext();
            ServiceReference[] serviceReferences=bundleContext.getServiceReferences(className,"(siteName="+site+")");
            serviceReference = serviceReferences[0];
        } catch (InvalidSyntaxException e) {
            LOG.error("error in sytax when trying to get className ("+className+")"+" from site ("+site+")",e);
            return null;
        } catch (NullPointerException e) {
            LOG.error("error getting bundle/service", e);
            return null;
        }
        return (T) bundleContext.getService(serviceReference);
    }

    /**
     * same as {@link #getOsgiService(String)} but meant to be exposed in taglib
     */
    public static <T> T taglibGetOsgiService(String className){
        return (T)singleton.getOsgiService(className);
    }
}
