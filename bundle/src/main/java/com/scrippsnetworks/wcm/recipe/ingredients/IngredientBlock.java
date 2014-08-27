package com.scrippsnetworks.wcm.recipe.ingredients;

import java.util.List;

/**
 * This contains ingredients and ingredient titles.
 * @author Jason Clark
 *         Date: 6/17/13
 */
public interface IngredientBlock {

    /** The title from the Ingredient Block. */
    public String getTitle();

    /** Get a List of Ingredient objects from the same grouping. */
    public List<Ingredient> getIngredients();

    /** sni:rankOrder property from data block. */
    public int getRankOrder();

}
