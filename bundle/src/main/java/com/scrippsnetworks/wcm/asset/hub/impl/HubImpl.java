package com.scrippsnetworks.wcm.asset.hub.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import com.day.cq.wcm.api.Page;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import com.scrippsnetworks.wcm.asset.hub.Hub;
import com.scrippsnetworks.wcm.hub.button.HubButton;

/**
 * Builds the hub using the <code>sni:hub</code> content resource from the master page. Each child resource node's
 * name becomes a key which refers to the Page/Resource referred to by the child node's <code>path</code> property.
 *
 * A notion of default pages is also supported. If a list of String keys is provided via constructor, the keys are
 * concatenated with the master's path to form a new path to the page's node. If the node exists, it is added to the
 * hub.
 *
 * @author Scott Everett Johnson
 */
public class HubImpl implements Hub {

    private static final String MASTER = "master";

    private Resource hubResource;
    private Page masterPage;
    private String masterPageType;
    private Map<String, Page> hubPages;
    private ResourceResolver resourceResolver;
    private List<HubButton> hubButtons;
  
    /**
     * Construct a new hub using the provided master page and hub resource.
     *
     * @param masterPage the Page which is the master for the hub
     * @param hubResource the <code>sni:hub</code> resource whose child nodes define hub membership
     */
    public HubImpl(Page masterPage, Resource hubResource) {
        this(masterPage, hubResource, null);
    }

    /**
     * Construct a new hub using the provided master page, hub resource, and list of child pages added by default if they
     * exist.
     *
     * @param masterPage the Page which is the master for the hub
     * @param hubResource the <code>sni:hub</code> resource whose child nodes define hub membership
     * @param defaults a List of Strings whose names are child nodes automatically added to the hub if they exist
     */
    public HubImpl(Page masterPage, Resource hubResource, List<String> defaults) {
        this.hubResource = hubResource;
        this.masterPage = masterPage;
        resourceResolver = masterPage.adaptTo(Resource.class).getResourceResolver();

        hubPages = new HashMap<String, Page>();
        hubPages.put(MASTER, masterPage);

        // Considering the possibility we'll try to construct a hub for a master-eligible parent page.
        // In that case hubResource would be null.
        if (hubResource != null) {
            Iterator<Resource> it = hubResource.listChildren();
            while (it.hasNext()) {
                Resource child = it.next();
                ValueMap cProps = child.adaptTo(ValueMap.class);
                String name = child.getName();
                String childPath = cProps.get("path", String.class);
                if (childPath != null && childPath.length() > 0) {
                    Resource childPageResource = resourceResolver.getResource(childPath);
                    if (childPageResource != null) {
                        Page childPage = childPageResource.adaptTo(Page.class);
                        // If the resource is not a page, childPage is null.
                        if (childPage != null) {
                            hubPages.put(name, childPage);
                        }
                    }
                }
            }
        }

        this.masterPageType = getResourceType(masterPage);

        if (defaults != null && !defaults.isEmpty()) {
            ResourceResolver resourceResolver = masterPage.adaptTo(Resource.class).getResourceResolver();
            for (String path : defaults) {
                Resource child = resourceResolver.getResource(masterPage.getPath() + "/" + path);
                if (child != null) {
                    Page childPage = child.adaptTo(Page.class);
                    if (childPage != null) {
                        hubPages.put(path,childPage);
                    }
                }
            }
        }
    }

	/**
     * {@inheritDoc}
	 */
    public String getMasterPageType() {
        return masterPageType;
    }

	/**
     * {@inheritDoc}
	 */
    public Map<String, Page> getAllPages() {
        return hubPages;
    }

	/**
     * {@inheritDoc}
	 */
    public Page getMasterPage() {
        return hubPages.get(MASTER);
    }

    /**
     * When you want all of the pages in a hub, except the master page...
     * @return List of pages excluding "master" page
     */
    public List<Page> getChildPages() {
        List<Page> pages = new ArrayList<Page>();
        for (Map.Entry<String, Page> e : hubPages.entrySet()) {
            if (e.getKey().equals(MASTER)) {
                continue;
            }
            pages.add(e.getValue());
        }
        return pages;
    }

	/**
     * {@inheritDoc}
	 */
	public Map<String, Resource> getAllResources() {
        HashMap<String, Resource> retVal = new HashMap<String, Resource>();
        for (Map.Entry<String, Page> e : hubPages.entrySet()) {
            retVal.put(e.getKey(), e.getValue().getContentResource());
        }
        return retVal;
	}

	/**
     * {@inheritDoc}
	 */
	private String getResourceType(Page page) {
        Resource res = page.getContentResource();
		if (null != res) {
            String assetType = res.getResourceType();
			if (StringUtils.isNotBlank(assetType) && assetType.indexOf('/') >= 0) {
				assetType = StringUtils.substringAfterLast(assetType, "/");
				return assetType;
			}
		}
		return null;
	}
}
