package com.scrippsnetworks.wcm.asset.hub;

import java.util.List;
import java.util.Map;

import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.Resource;

/** Methods for the hub object.
 *
 * Provides access to the hub object.
 *
 * @author Scott Everett Johnson
 */
@Deprecated
public interface Hub {

    /**
     * The master Page's type.
     *
     * The master page type is the last component of the sling:resourceType from the Page's
     * content resource.
     *
     */
	public String getMasterPageType();

    
    /**
     * The hub's master Page.
     *
     * @see Page
     */
    public Page getMasterPage();

    /**
     * Map relating string keys to Resources.
     *
     * @see Resource
     */
	public Map<String,Resource> getAllResources();

    /**
     * Map relating string keys to Pages.
     *
     * @see Page
     */
	public Map<String,Page> getAllPages();

    /**
     * Hub's child pages
     * @return
     */
    public List<Page> getChildPages();
}
