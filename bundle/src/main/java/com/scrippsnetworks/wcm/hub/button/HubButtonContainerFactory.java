package com.scrippsnetworks.wcm.hub.button;

import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.hub.button.impl.HubButtonContainerImpl;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * @author Jason Clark
 *         Date: 5/13/13
 */
public class HubButtonContainerFactory {
    private Hub hub;

    private SniPage sniPage;

    /**
     * Construct a new HubContainer for a given SniPage.
     * Can return null.
     * @return HubButtonContainer
     */
    public HubButtonContainer build() {
        if (sniPage != null) {
            return new HubButtonContainerImpl(sniPage);
        }
        return null;
    }

    /** Construct a HubButtonContainer given an SniPage */
    public HubButtonContainerFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }
}
