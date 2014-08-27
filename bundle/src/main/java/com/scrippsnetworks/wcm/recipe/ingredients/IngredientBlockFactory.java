package com.scrippsnetworks.wcm.recipe.ingredients;

import com.scrippsnetworks.wcm.recipe.ingredients.impl.IngredientBlockImpl;
import org.apache.sling.api.resource.Resource;

/**
 * @author Jason Clark
 *         Date: 6/17/13
 */
public class IngredientBlockFactory {

    private Resource resource;

    /** Construct a new IngredientBlock. */
    public IngredientBlock build() {
        if (resource != null) {
            return new IngredientBlockImpl(resource);
        }
        return null;
    }

    /**
     * Add a Resource to this builder.
     * Expected to be the resource of a recipe data block.
     */
    public IngredientBlockFactory withResource(Resource resource) {
        this.resource = resource;
        return this;
    }

}
