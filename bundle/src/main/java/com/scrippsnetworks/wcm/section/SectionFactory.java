package com.scrippsnetworks.wcm.section;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.section.impl.FoodSectionImpl;

/**
 *
 * @author Patrick Armstrong
 */
public class SectionFactory {
    
    private SniPage sniPage;
    
    public SectionFactory() {
        sniPage = null;
    }
    
    public SectionFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }
    
    public Section build() {
        if (sniPage == null) {
            return null;
        }
        return new FoodSectionImpl(sniPage);
    }
    
}
