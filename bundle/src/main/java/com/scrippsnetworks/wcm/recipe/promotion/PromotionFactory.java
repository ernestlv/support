package com.scrippsnetworks.wcm.recipe.promotion;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.Recipe;
import com.scrippsnetworks.wcm.recipe.promotion.impl.PromotionImpl;

/**
 * @author Jonathan Bell
 *         Date: 7/23/13
 */

public class PromotionFactory {
    private SniPage sniPage;
    private Recipe recipe;

    /** Construct a new Promotion from a Recipe Snipage. */
    public Promotion build() {
        if (sniPage != null) {
            return new PromotionImpl(sniPage);
        }
        return null;
    }

    public PromotionFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }
}
