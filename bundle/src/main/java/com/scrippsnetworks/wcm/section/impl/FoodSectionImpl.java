package com.scrippsnetworks.wcm.section.impl;

import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.section.Section;

/**
 * Implements Section. Used to abstract complex section logic with the intention of easily scaling into future sites.
 * Package private.
 * @author Patrick Armstrong
 */
public class FoodSectionImpl implements Section {
    
    String sectionName = null;
    String sectionDisplayName = null;
    
    private SniPage sniPage, sectionPage;
    
    public FoodSectionImpl(SniPage sniPage) {
        this.sniPage = sniPage;
        this.sectionPage = null;
    }
    
    private void lazyLoad() {
        if (sniPage != null && sectionPage == null) {
            sectionPage = PageFactory.getSniPage(sniPage.getAbsoluteParent(2));
        }
    }
    
    @Override
    public String getSectionName() {
        lazyLoad();
        if (sniPage == null || sectionPage == null) {
            return null;
        }
        if (sectionName == null) {
            sectionName = sectionPage.getName();
        }
        return sectionName;
    }
    
    @Override
    public String getSectionDisplayName() {
        lazyLoad();
        if (sectionDisplayName == null) {
            if (sectionPage == null) {
                //If no section, use site
                sectionDisplayName = sniPage.getBrand();
            } else {
                sectionDisplayName = sectionPage.getTitle();
            }
        }
        return sectionDisplayName;
    }
    
}
