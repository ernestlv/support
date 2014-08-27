package com.scrippsnetworks.wcm.recipe.listing;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.listing.impl.RecipeListingImpl;
import org.apache.sling.api.resource.Resource;

/**
 * @author Jason Clark
 *         Date: 5/11/13
 */
public class RecipeListingFactory {

    /** SniPage */
    private SniPage sniPage;

    /**
     *
     * @return
     */
    public RecipeListing build() {
        return new RecipeListingImpl(sniPage);
    }

    /**
     *
     * @param sniPage
     * @return
     */
    public RecipeListingFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }
}
