package com.scrippsnetworks.wcm.seo;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.seo.impl.SeoDescriptionImpl;

/**
 * @author Jonatahn Bell
 *         Date: 8/14/2013
 */
public class SeoDescriptionFactory {
    private SniPage sniPage;

    public SeoDescription build() {
        return new SeoDescriptionImpl(sniPage);
    }

    public SeoDescriptionFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }
}
