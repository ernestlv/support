package com.scrippsnetworks.wcm.recipe.listing;

import com.scrippsnetworks.wcm.recipe.Recipe;

import java.util.List;

/**
 * @author Jason Clark
 *         Date: 5/11/13
 */
public interface RecipeListing {
    public static final int ITEMS_PER_PAGE = 15;

    public List<Recipe> getRecipes();
    public int getTotalSize();
}
