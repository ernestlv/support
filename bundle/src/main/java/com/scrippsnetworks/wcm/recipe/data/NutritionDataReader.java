package com.scrippsnetworks.wcm.recipe.data;

import com.scrippsnetworks.wcm.recipe.nutrition.NutritionBlock;

import java.util.List;

/**
 * @author Jason Clark
 *         Date: 7/15/13
 */
public interface NutritionDataReader {

    /** Retrieve the list of NutritionBlocks from this reader. */
    public List<NutritionBlock> getNutritionBlocks();

}
