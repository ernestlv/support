package com.scrippsnetworks.wcm.recipe;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.impl.RecipeImpl;
import org.apache.sling.api.resource.Resource;

/**
 * @author Jason Clark
 *         Date: 5/11/13
 */
public class RecipeFactory {
    private SniPage sniPage;
    private Resource resource;
    private Recipe recipe;

    public Recipe build() {
        recipe = null;

        if (sniPage != null) {
            recipe = new RecipeImpl(sniPage);
        } else if (resource != null) {
            recipe = new RecipeImpl(resource);
        }

        return recipe;
    }

    public RecipeFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }

    public RecipeFactory withResource(Resource resource) {
        this.resource = resource;
        return this;        
    }
}
