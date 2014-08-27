/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scrippsnetworks.wcm.beverage.impl;

import com.scrippsnetworks.wcm.beverage.Beverage;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 *
 * @author Patrick
 */
public class BeverageImpl implements Beverage {
    
    private static final String IMAGE_PROP = "sni:image/fileReference";
    private static final String TERM_LINK_PROP = "sni:termLink";
    private static final String DRINK_PROMOTION_PROP = "drinkPromotion";
    private static final String CTA_LINK_PROP = "ctaLink";
    
    private SniPage sniPage;
    
    public BeverageImpl(SniPage sniPage) {
        this.sniPage = sniPage;
    }

    @Override
    public SniPage getBeveragePage() {
        return sniPage;
    }

    @Override
    public String getTitle() {
        if (sniPage != null) {
            return sniPage.getTitle();
        }
        return "";
    }

    @Override
    public String getDescription() {
        if (sniPage != null) {
            return sniPage.getDescription();
        }
        return "";
    }

    @Override
    public String getImagePath() {
        if (sniPage != null) {
            //TODO: refactor to SniPage?
            return sniPage.getProperties().get(IMAGE_PROP, "");
        }
        return "";
    }

    @Override
    public String getTermLink() {
        if (sniPage != null) {
            return sniPage.getProperties().get(TERM_LINK_PROP, "");
        }
        return "";
    }

    @Override
    public String getDrinkPromotion() {
        if (sniPage != null) {
            return sniPage.getProperties().get(DRINK_PROMOTION_PROP, "");
        }
        return "";
    }

    @Override
    public String getCallToAction() {
        if (sniPage != null) {
            return sniPage.getProperties().get(CTA_LINK_PROP, "");
        }
        return "";
    }
    
}
