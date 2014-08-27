package com.scrippsnetworks.wcm.recipe.nutrition.impl;

import com.scrippsnetworks.wcm.recipe.nutrition.NutritionBlock;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

/**
 * @author Jason Clark
 *         Date: 7/15/13
 */
public class NutritionBlockImpl implements NutritionBlock {

    private static final String TITLE = "jcr:title";
    private static final String VALUE = "sni:value";

    /** Resource used to construct this block. */
    private Resource resource;

    /** Member for nutrition block title. */
    private String title;

    /** Member for nutrition block value. */
    private String value;

    /** Create a new NutritionBlock given a Resource. */
    public NutritionBlockImpl(final Resource resource) {
        this.resource = resource;
        ValueMap properties = resource.adaptTo(ValueMap.class);
        if (properties != null) {
            if (properties.containsKey(TITLE)) {
                title = properties.get(TITLE, String.class);
            }
            if (properties.containsKey(VALUE)) {
                value = properties.get(VALUE, String.class);
            }
        }
    }

    /** {@inheritDoc} */
    public String getTitle() {
        return title;
    }

    /** {@inheritDoc} */
    public String getValue() {
        return value;
    }

}
