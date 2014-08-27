package com.scrippsnetworks.wcm.hub;

import com.scrippsnetworks.wcm.hub.button.HubButtonContainer;
import com.scrippsnetworks.wcm.page.SniPage;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.List;

/**
 * @author Jason Clark
 *         Date: 5/16/13
 */
public interface Hub {

    /** The name of the JCR node which stores the hub data structure. */
    public static final String HUB_NODE_NAME = "sni:hub";

    /** The name of the JCR Property on the hub node which contains the hub data. */
    public static final String HUB_PAGES_PROPERTY = "hubPages";

    /** Retrieve the SniPage parent in this hub structure. */
    public SniPage getHubMaster();

    /** Retrieve a List of the SniPage children in this hub structure. */
    public List<SniPage> getHubChildren();

    /** Retrieve a ValueMap of properties from the sni:hub node */
    public ValueMap getHubProperties();

    /** Retrieve a List of Resources for shared modules. */
    public List<Resource> getSharedModules();

    /** Retrieve the HubButtonContainer for this Hub. */
    public HubButtonContainer getHubButtonContainer();

    /** Checks if a given SniPage is a child in the hub. */
    public boolean isPageHubChild(SniPage sniPage);

    /** Checks if a given SniPage is the master of the hub. */
    public boolean isPageHubMaster(SniPage sniPage);

    /** Checks if a given SniPage is in the hub at all. */
    public boolean isPageInHub(SniPage sniPage);
}
