package com.scrippsnetworks.wcm.menu.listing;

import com.scrippsnetworks.wcm.menu.listing.impl.MenuListingImpl;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * @author Jason Clark
 *         Date: 5/11/13
 */
public class MenuListingFactory {
    /** SniPage */
    private SniPage sniPage;

    /**
     *
     * @return
     */
    public MenuListing build() {
        return new MenuListingImpl(sniPage);
    }

    /**
     *
     * @param sniPage
     * @return
     */
    public MenuListingFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }
}
