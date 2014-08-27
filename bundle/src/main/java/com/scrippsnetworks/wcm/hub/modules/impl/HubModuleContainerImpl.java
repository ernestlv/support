package com.scrippsnetworks.wcm.hub.modules.impl;

import com.scrippsnetworks.wcm.hub.modules.HubModuleContainer;
import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.page.SniPage;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jason Clark
 *         Date: 5/29/13
 */
public class HubModuleContainerImpl implements HubModuleContainer {

    /** loggit */
    private static final Logger log = LoggerFactory.getLogger(HubModuleContainerImpl.class);

    /** List of module Resources. */
    private List<Resource> modules;

    /**
     * Core implementation of a hub module container. This is an object to handle
     * the business logic of building a collection of modules that are shared in a hub.
     * @param sniPage SniPage for hub master
     */
    public HubModuleContainerImpl(SniPage sniPage) {
        modules = new ArrayList<Resource>();
        if (sniPage != null) {
            Hub hub = sniPage.getHub();
            if (hub != null) {
                ValueMap props = hub.getHubProperties();
                if (sniPage.hasContent()) {
                    ResourceResolver resolver = sniPage.getContentResource().getResourceResolver();
                    if (props != null && props.containsKey(HUB_MODULES_PROPERTY)) {
                        String[] moduleProps = props.get(HUB_MODULES_PROPERTY, String[].class);
                        for (String prop : moduleProps) {
                            try {
                                Resource moduleResource = resolver.getResource(prop);
                                if (moduleResource != null) {
                                    modules.add(moduleResource);
                                }
                            } catch (Exception e) {
                                log.error("Exception when converting hub module path into a resource: "
                                        + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns a List of Resources modules that have been shared within a Hub.
     * @return List of Resources
     */
    @Override
    public List<Resource> getModules() {
        return modules;
    }
}
