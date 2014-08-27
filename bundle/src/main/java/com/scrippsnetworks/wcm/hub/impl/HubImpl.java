package com.scrippsnetworks.wcm.hub.impl;

import com.day.cq.wcm.api.Page;

import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.hub.HubPageKeyComparator;
import com.scrippsnetworks.wcm.hub.button.HubButtonContainer;
import com.scrippsnetworks.wcm.hub.button.HubButtonContainerFactory;
import com.scrippsnetworks.wcm.hub.modules.HubModuleContainer;
import com.scrippsnetworks.wcm.hub.modules.HubModuleContainerFactory;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Hub Implementation
 * @author Jason Clark
 */
public class HubImpl implements Hub {

    private static final Logger log = LoggerFactory.getLogger(HubImpl.class);

    /** Resource for the sni:hub node itself */
    private Resource hubResource;

    /** SniPage object used to construct this Hub */
    private SniPage currentSniPage;

    /** Map of hub children keyed on path */
    private Map<String, SniPage> hubChildPathMap;

    /** Hub Module Container for internal use. */
    private HubModuleContainer hubModuleContainer;

    /** These are exposed through the interface */
    private ValueMap hubProperties;
    private HubButtonContainer hubButtonContainer;
    private SniPage hubMaster;
    private List<SniPage> hubChildren;
    private List<Resource> sharedModules;


    /**
     * Hub Implementation.
     * @param sniPage SniPage
     */
    public HubImpl(final SniPage sniPage) {
        if (sniPage != null) {
            currentSniPage = sniPage;
            Resource hubResource = getHubNodeResource(sniPage);
            if (hubResource == null || !hubContainsPages(hubResource)) {
                Page parentPage = sniPage.getParent();
                if (parentPage != null) {
                    hubResource = getHubNodeResource(parentPage);
                    if (hubResource != null
                            && hubContainsPages(hubResource)
                            && hubContainsSniPage(hubResource, sniPage)) {
                        this.hubResource = hubResource;
                        hubMaster = PageFactory.getSniPage(parentPage);
                    }
                }
            } else {
                this.hubResource = hubResource;
                hubMaster = sniPage;
            }
        }
    }

    /** Private util method to encapsulate retrieving the Hub node from a page */
    private static Resource getHubNodeResource(Page page) {
        Resource hubResource = null;
        if (page != null) {
            Resource pageContent = page.getContentResource();
            if (pageContent != null) {
                hubResource = pageContent.getChild(HUB_NODE_NAME);
            }
        }
        return hubResource;
    }

    /** Check if the given hub resource contains the given SniPage. */
    private static boolean hubContainsSniPage(final Resource hubResource, final SniPage sniPage) {
        if (hubResource != null && sniPage != null) {
            ValueMap hubProps = hubResource.adaptTo(ValueMap.class);
            if (hubProps != null && hubContainsPages(hubResource)) {
                String[] hubPages = hubProps.get(HUB_PAGES_PROPERTY, String[].class);
                if (hubPages != null) {
                    for (String path : hubPages) {
                        if (sniPage.getPath().equals(path)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /** check if the hub resource actually contains pages. */
    private static boolean hubContainsPages(final Resource resource) {
        if (resource != null) {
            ValueMap props = resource.adaptTo(ValueMap.class);
            return props.containsKey(HUB_PAGES_PROPERTY);
        }
        return false;
    }

    /** Return a HubModuleContainer to manage the sharing of modules in this hub. */
    private HubModuleContainer getHubModuleContainer() {
        if (hubModuleContainer == null) {
            hubModuleContainer = new HubModuleContainerFactory()
                    .withSniPage(hubMaster)
                    .build();
        }
        return hubModuleContainer;
    }

    /**
     * Util method to get properties from hub resource, can return null.
     * @return ValueMap properties from sni:hub node
     */
    public ValueMap getHubProperties() {
        if (hubProperties == null && hubResource != null) {
            hubProperties = hubResource.adaptTo(ValueMap.class);
        }
        return hubProperties;
    }

    /**
     * Returns HubButtonContainer for this hub.
     * @return HubButtonContainer
     */
    public HubButtonContainer getHubButtonContainer() {
        if (hubButtonContainer == null) {
            hubButtonContainer = new HubButtonContainerFactory()
                    .withSniPage(currentSniPage)
                    .build();
        }
        return hubButtonContainer;
    }

    /**
     * Get the HubParent from the Hub.
     * @return SniPage
     */
    public SniPage getHubMaster() {
        return hubMaster;
    }

    /**
     * Get a List of all HubChildren for this Hub.
     * @return List of SniPages
     */
    public List<SniPage> getHubChildren() {
        if (hubChildren == null && hubResource != null) {
            hubChildren = new ArrayList<SniPage>();
            hubChildPathMap = new HashMap<String, SniPage>();
            ValueMap hubProps = hubResource.adaptTo(ValueMap.class);
            if (hubProps.containsKey(HUB_PAGES_PROPERTY)) {
                String[] hubPages = hubProps.get(HUB_PAGES_PROPERTY, String[].class);
                if (hubPages != null && hubPages.length > 0) {
                    for (String path : hubPages) {
                        Resource childResource = hubResource
                                .getResourceResolver()
                                .getResource(path);
                        if (childResource != null) {
                            SniPage childPage = PageFactory
                                    .getSniPage(childResource.adaptTo(Page.class));
                            if (childPage != null) {
                                hubChildren.add(childPage);
                                hubChildPathMap.put(childPage.getPath(), childPage);
                            }
                        }
                    }
                }
            }
            Collections.sort(hubChildren, new HubPageKeyComparator());
        }
        return hubChildren;
    }

    /** Retrieve List of Resources for shared modules. */
    public List<Resource> getSharedModules() {
        if (sharedModules == null) {
            HubModuleContainer container = getHubModuleContainer();
            sharedModules = container.getModules();
        }
        return sharedModules;
    }

    /**
     * Checks if a given SniPage is in the list of hub children for this hub.
     * @param sniPage SniPage you want to check
     * @return boolean
     */
    public boolean isPageHubChild(SniPage sniPage) {
        return (
                sniPage != null
                && getHubChildren() != null
                && hubChildPathMap.containsKey(sniPage.getPath())
        );
    }

    /**
     * Checks if a given SniPage is the hub master.
     * @param sniPage SniPage you wish to check.
     * @return boolean
     */
    public boolean isPageHubMaster(SniPage sniPage) {
        return sniPage != null
                && hubMaster != null
                && sniPage.getPath().equals(hubMaster.getPath())
                && getHubChildren().size() > 0;
    }

    /**
     * Checks if a given SniPage is in the hub at all.
     * @param sniPage SniPage you wish to check.
     * @return boolean got hub?
     */
    public boolean isPageInHub(SniPage sniPage) {
        return (isPageEpisode(sniPage) || isPageHubMaster(sniPage) || isPageHubChild(sniPage));
    }

    /* shim to make episodes work until we create episode-specific navigation component. */
    private boolean isPageEpisode(SniPage sniPage) {
        return sniPage.getPageType().equals("episode");
    }
}
