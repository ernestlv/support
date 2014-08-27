package com.scrippsnetworks.wcm.hub.modules;

import com.scrippsnetworks.wcm.hub.modules.impl.HubModuleContainerImpl;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * @author Jason Clark
 *         Date: 5/29/13
 */
public class HubModuleContainerFactory {

    /** SniPage for the hub master */
    private SniPage sniPage;

    /**
     * Construct a HubModuleContainer with the given objects.
     * @return HubModuleContainer
     */
    public HubModuleContainer build() {
        if (sniPage == null) {
            return null;
        }
        return new HubModuleContainerImpl(sniPage);
    }

    /** Add an SniPage to your builder */
    public HubModuleContainerFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }

}
