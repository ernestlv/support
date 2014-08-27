package com.scrippsnetworks.wcm.recipe.titlecombiner;

import com.scrippsnetworks.wcm.recipe.ingredients.IngredientBlock;
import com.scrippsnetworks.wcm.recipe.instructions.Instruction;

import java.util.List;

/**
 * @author Jason Clark
 *         Date: 11/4/13
 */
public interface TitleCombiner {

    /** Get a List of Instructions from the TitleCombiner. */
    public List<Instruction> getInstructions();

    /** Get a List of IngredientBlocks from the TitleCombiner. */
    public List<IngredientBlock> getIngredientBlocks();

}
