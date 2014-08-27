package com.scrippsnetworks.wcm.hub.count;

import com.scrippsnetworks.wcm.hub.count.impl.HubCountImpl;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * Construct a HubCount object. Requires an SniPage to do this.
 * @author Jason Clark
 *         Date: 5/10/13
 */
public class HubCountFactory {

    private SniPage sniPage;

    /**
     * Build a new HubCount object, must have an SniPage handy to do so.
     * @return HubCount
     */
    public HubCount build() {
        if (sniPage != null) {
            return new HubCountImpl(sniPage);
        }
        return null;
    }

    /**
     * Add an SniPage to your HubCount object
     * @param page SniPage
     * @return this
     */
    public HubCountFactory withSniPage(SniPage page) {
        this.sniPage = page;
        return this;
    }
}
