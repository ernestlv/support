package com.scrippsnetworks.wcm.recipe.titlecombiner;

import com.scrippsnetworks.wcm.recipe.ingredients.IngredientBlock;
import com.scrippsnetworks.wcm.recipe.instructions.Instruction;
import com.scrippsnetworks.wcm.recipe.titlecombiner.impl.TitleCombinerImpl;

import java.util.List;

/**
 * @author Jason Clark
 *         Date: 11/4/13
 */
public class TitleCombinerFactory {

    private List<Instruction> instructions;
    private List<IngredientBlock> ingredientBlocks;

    /** Build a new TitleCombiner using the Instructions and IngredientBlocks given. */
    public TitleCombiner build() {
        if (instructions != null && ingredientBlocks != null) {
            return new TitleCombinerImpl(ingredientBlocks, instructions);
        }
        return null;
    }

    /** Add a list of instructions to this builder. */
    public TitleCombinerFactory withInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
        return this;
    }

    public TitleCombinerFactory withIngredientBlocks(List<IngredientBlock> ingredientBlocks) {
        this.ingredientBlocks = ingredientBlocks;
        return this;
    }

}
