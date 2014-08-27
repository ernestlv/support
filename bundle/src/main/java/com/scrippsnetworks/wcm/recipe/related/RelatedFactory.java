package com.scrippsnetworks.wcm.recipe.related;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.related.impl.RelatedImpl;

/**
 * @author Jonathan Bell
 *         Date: 7/19/13
 */
public class RelatedFactory {

    /** SniPage for Recipe used to build related content. */
    private SniPage sniPage;

    /** Construct a new Related from a Recipe Snipage. */
    public Related build() {
        if (sniPage != null) {
            return new RelatedImpl(sniPage);
        }
        return null;
    }

    /** Add an SniPage to this builder. */
    public RelatedFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }

}
