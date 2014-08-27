package com.scrippsnetworks.wcm.regions;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.regions.impl.RightRailImpl;

/**
 * @author Jason Clark
 *         Date: 5/23/13
 */
public class RightRailFactory {

    /** Current SniPage. */
    private SniPage sniPage;

    /**
     * Build a new RightRail object.
     * @return RightRail
     */
    public RightRail build() {
        if (sniPage != null) {
            return new RightRailImpl(sniPage);
        }
        return null;
    }

    /** Add an SniPage to this builder. */
    public RightRailFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }
}
