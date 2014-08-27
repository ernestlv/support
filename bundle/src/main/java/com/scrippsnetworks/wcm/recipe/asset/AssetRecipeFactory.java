package com.scrippsnetworks.wcm.recipe.asset;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.asset.impl.AssetRecipeImpl;

/**
 * @author Jason Clark
 *         Date: 5/12/13
 */
public class AssetRecipeFactory {

    /** SniPage */
    private SniPage sniPage;

    /**
     * Construct an SniPage with the components passed into the Factory.
     * @return AssetRecipe
     */
    public AssetRecipe build() {
        return new AssetRecipeImpl(sniPage);
    }

    /**
     * Add an SniPage to your factory
     * @param sniPage SniPage you want to add
     * @return this
     */
    public AssetRecipeFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }
}
