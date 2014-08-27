package com.scrippsnetworks.wcm.asset.hub;

import java.lang.RuntimeException;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.Resource;
import com.scrippsnetworks.wcm.asset.hub.impl.HubImpl;
import org.apache.commons.lang.StringUtils;

/** Provides access to the relevant hub for a page.
 *
 * The hub structure is defined by the <code>sni:hub</code> resource under the master page's content resource. A child node
 * exists for each possible member page. If the <code>path</code> property is set on the child node and the page exists as a
 * child of the master page, the page is added to the hub using the <code>sni:hub</code> child resource's node name as a key
 * for retrieval.
 *
 * The HubManager also supports a notion of default pages. A list of child page node names is provided for all hub types (the
 * hub type being determined by the type of the master page) which should automatically include those nodes in the hub if they
 * exist. The child page's node name (e.g., reviews) is used as they key for retrieval.
 *
 * Thus there are two ways to use the HubManager. You can either construct the hub using the defaults so that a
 * <code>sni:hub</code> is not necessarily required for hubs to appear when required child pages are present, or you can
 * require that all hub membership be explicitly indicated in the <code>sni:hub</code> node.
 *
 */
@Deprecated
public class HubManager {


    /** A map defining hub defaults.
     *
     * The map is keyed by brand. The values are Maps keyed by page type having List values defining the node names of child
     * pages added by default to hubs whose master is of the key page type.
     *
     * By virtue of a page type existing in the brand's default map, the page is considered a master page even without the
     * existence of a <code>sni:hub</code> node. These maps are only used when calling {@link #findHubWithDefaults}.
     */
    public static final Map<String, Map<String, List<String>>> autoMasters = new HashMap<String, Map<String, List<String>>>();
    static { autoMasters.put("cook", new HashMap<String, List<String>>());
        autoMasters.get("cook").put("recipe",Arrays.asList("reviews", "nutrition"));
        autoMasters.get("cook").put("talent",Arrays.asList("recipes"));
        autoMasters.get("cook").put("show",Arrays.asList("recipes")); }

    /**
     * Returns the page's Hub.
     */
    public static Hub findHub(Page page) {

        Page masterPage = null;
        Hub hub = null;
        Hub retVal = null;

        Resource hubResource = page.getContentResource("sni:hub");
        if (hubResource == null) {
            // Hookay, maybe I'm a child?
            Page parentPage = page.getParent();
            if (parentPage != null) {
                hubResource = parentPage.getContentResource("sni:hub");
                if (hubResource != null) {
                    masterPage = parentPage;
                }
            }
        } else {
            masterPage = page;
        }

        if (masterPage != null) {
            hub = new HubImpl(masterPage, hubResource);
        }

        if (hub != null) {
            // OK, we have a hub, but is the current page in it?
            // If the master is our parent, we may not be.
            Map<String, Page> hubPages = hub.getAllPages();
            String myPath = page.getPath();
            boolean inHub = false;
            for (Map.Entry<String, Page> e : hubPages.entrySet()) {
                if (myPath.equals(e.getValue().getPath())) {
                    inHub = true;
                }
            }

            if (inHub && hubPages.size() > 1) {
                retVal = hub;
            }
        }

        return retVal;
    }
	
	/**
     * Returns whether the page is hubbed (in a Hub or a Hub master).
     */
    public static Boolean isHubbed(Page page) {
 
        Page masterPage = null;
        Hub hub = null;
		
		//check for existance of an sni:hub node as a child of this page
        Resource hubResource = page.getContentResource("sni:hub");
        if (hubResource == null) {
            // sni:hub was not found, so maybe this is a child and the parent has it
            Page parentPage = page.getParent();
            if (parentPage != null) {
                hubResource = parentPage.getContentResource("sni:hub");
				//the parent does in fact have an sni:hub node
                if (hubResource != null) {
                    masterPage = parentPage;
                }
            }
        } else {
			//this page is a hub master/parent as it has an sni:hub node
            masterPage = page;
        }
		
		return masterPage != null; //because masterPage only gets a value if the page is a hub parent or child		
     }

    /**
     * Returns the page's Hub using defaults defined in this class.
     */
    public static Hub findHubWithDefaults(Page page) {
        Page masterPage = null;
        Hub hub = null;
        Hub retVal = null;
        boolean autoMaster = false;

        String brand = page.getPath().split("/")[2];
        String resType = getResourceType(page);
        Map<String, List<String>> brandDefaults = autoMasters.get(brand);
        if (brandDefaults == null) {
            brandDefaults = new HashMap<String, List<String>>();
        }
        List<String> typeDefaults = null;

        Resource hubResource = page.getContentResource("sni:hub");
        if (hubResource == null) {
            resType = getResourceType(page);
            if (brandDefaults.containsKey(resType)) {
                masterPage = page;
            } else {
                // Hookay, maybe I'm a child?
                Page parentPage = page.getParent();
                if (parentPage != null) {
                    hubResource = parentPage.getContentResource("sni:hub");
                    resType = getResourceType(parentPage);
                    if (hubResource != null || brandDefaults.containsKey(resType)) {
                        masterPage = parentPage;
                    }
                }
            }
        } else {
            masterPage = page;
        }

        if (masterPage != null) {
            String masterType = getResourceType(masterPage);
            if (brandDefaults.containsKey(masterType)) {
                typeDefaults = brandDefaults.get(masterType);
            }
            hub = new HubImpl(masterPage, hubResource, typeDefaults);
        } else {
            throw new RuntimeException("masterPage is null brand=" + brand + " resType=" + resType);
        }

        if (hub != null) {
            // OK, we have a hub, but is the current page in it?
            // If the master is our parent, we may not be.
            Map<String, Page> hubPages = hub.getAllPages();
            String myPath = page.getPath();
            boolean inHub = false;
            for (Map.Entry<String, Page> e : hubPages.entrySet()) {
                if (myPath.equals(e.getValue().getPath())) {
                    inHub = true;
                }
            }
            
            if (inHub && hubPages.size() > 1) {
                retVal = hub;
            }
        }

        return retVal;
    }

    /**
     * Returns the last path component of a Page's <code>sling:resourceType</code>.
     */
	private static String getResourceType(Page page) {
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
