package com.scrippsnetworks.wcm.regions;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.regions.impl.GlobalHeaderImpl;

/**
 * @author Patrick Armstrong
 *         Date: 8/12/2013
 */
public class GlobalHeaderFactory {

    /** Current SniPage. */
    private SniPage sniPage;

    private boolean isMobile = false;

    /**
     * Build a new GlobalHeader object.
     * @return GlobalHeader
     */
    public GlobalHeader build() {
        if (sniPage != null) {
            return new GlobalHeaderImpl(sniPage, isMobile);
        }
        return null;
    }

    /** Add an SniPage to this builder. */
    public GlobalHeaderFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }

    public GlobalHeaderFactory withIsMobile(boolean isMobile) {
        this.isMobile = isMobile;
        return this;
    }
}
