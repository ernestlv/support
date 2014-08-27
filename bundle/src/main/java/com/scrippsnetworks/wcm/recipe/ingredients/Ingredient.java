package com.scrippsnetworks.wcm.recipe.ingredients;

/**
 * Recipe ingredient.
 * @author Jason Clark
 *         Date: 6/14/13
 */
public interface Ingredient {

    /** If the ingredient had a title. */
    public String getTitle();

    /** How much ingredient. */
    public String getAmount();

    /** Name of ingredient. */
    public String getName();
}
