package com.scrippsnetworks.wcm.recipe.asset.impl;

import java.util.Collections;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.Recipe;
import com.scrippsnetworks.wcm.recipe.asset.AssetRecipe;

import java.util.List;

/**
 * @author Jason Clark
 *         Date: 5/12/13
 */
public class AssetRecipeImpl implements AssetRecipe {

    private SniPage sniPage;
    private List<Recipe> recipes;

    public AssetRecipeImpl(final SniPage sniPage) {
        this.sniPage = sniPage;
    }

    /**
     *
     * @return
     */
    public List<Recipe> getRecipes() {
        return Collections.<Recipe>emptyList();
    }
}
