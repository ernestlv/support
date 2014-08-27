package com.scrippsnetworks.wcm.seo;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.seo.impl.SeoTitleImpl;

/**
 * @author Jonatahn Bell
 *         Date: 8/14/2013
 */
public class SeoTitleFactory {
    private SniPage sniPage;

    public SeoTitle build() {
        return new SeoTitleImpl(sniPage);
    }

    public SeoTitleFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }
}
