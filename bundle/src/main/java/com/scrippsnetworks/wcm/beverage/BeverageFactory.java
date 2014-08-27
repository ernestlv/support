package com.scrippsnetworks.wcm.beverage;

import com.scrippsnetworks.wcm.beverage.impl.BeverageImpl;

import com.scrippsnetworks.wcm.page.SniPage;

/**
 * @author Patrick Armstrong
 *         Date: 7/23/13
 */
public class BeverageFactory {
    private SniPage sniPage;

    public Beverage build() {
        return new BeverageImpl(sniPage);
    }

    public BeverageFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }
}
