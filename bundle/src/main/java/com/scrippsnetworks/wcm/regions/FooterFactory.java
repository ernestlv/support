package com.scrippsnetworks.wcm.regions;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.regions.impl.FooterImpl;

/**
 * User: kenshih
 * Date: 8/23/13
 */
public class FooterFactory {
    /** Current SniPage. */
    private SniPage sniPage;

    private boolean isMobile = false;

    /**
     * Build a new GlobalHeader object.
     * @return GlobalHeader
     */
    public Footer build() {
        if (sniPage != null) {
            return new FooterImpl(sniPage, isMobile);
        }
        return null;
    }

    /** Add an SniPage to this builder. */
    public FooterFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }

    public FooterFactory withIsMobile(boolean isMobile) {
        this.isMobile = isMobile;
        return this;
    }
}
