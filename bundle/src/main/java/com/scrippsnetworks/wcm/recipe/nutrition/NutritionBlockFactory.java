package com.scrippsnetworks.wcm.recipe.nutrition;

import com.scrippsnetworks.wcm.recipe.nutrition.impl.NutritionBlockImpl;
import org.apache.sling.api.resource.Resource;

/**
 * @author Jason Clark
 *         Date: 7/15/13
 */
public class NutritionBlockFactory {

    /** Resource of nutrition block. */
    private Resource resource;

    /** Build a new NutritionBlock from the given Resource. */
    public NutritionBlock build() {
        if (resource != null) {
            return new NutritionBlockImpl(resource);
        }
        return null;
    }

    /** Add a Resource to this builder. */
    public NutritionBlockFactory withResource(Resource resource) {
        this.resource = resource;
        return this;
    }
}
