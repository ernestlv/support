package com.scrippsnetworks.wcm.recipe.titlecombiner.impl;

import com.scrippsnetworks.wcm.recipe.ingredients.IngredientBlock;
import com.scrippsnetworks.wcm.recipe.instructions.Instruction;
import com.scrippsnetworks.wcm.recipe.instructions.InstructionFactory;
import com.scrippsnetworks.wcm.recipe.titlecombiner.TitleCombiner;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jason Clark
 *         Date: 11/4/13
 */
public class TitleCombinerImpl implements TitleCombiner {

    /* The original Lists. */
    private List<IngredientBlock> ingredientBlocks;
    private List<Instruction> instructions;

    /* The modified Lists. */
    private List<Instruction> combinedInstructions;

    /** Build a new combiner using the ingredients and instructions that we have. */
    public TitleCombinerImpl(final List<IngredientBlock> ingredientBlocks,
                             final List<Instruction> instructions) {
        this.ingredientBlocks = ingredientBlocks;
        this.instructions = instructions;
    }

    /** {@inheritDoc} */
    public List<Instruction> getInstructions() {
        if (combinedInstructions == null) {
            combinedInstructions = new ArrayList<Instruction>();
            if (ingredientBlocks != null && instructions != null
                    && instructions.size() > 1
                    && ingredientBlocks.size() == instructions.size()) {
                for (int i = 0; i < ingredientBlocks.size(); i++) {
                    IngredientBlock block = ingredientBlocks.get(i);
                    Instruction instruction = instructions.get(i);
                    if (!(instruction.getRankOrder() - block.getRankOrder() > 10)
                            && StringUtils.isBlank(instruction.getTitle())) {
                        Instruction combinedInstruction = new InstructionFactory()
                                .withText(instruction.getText())
                                .withTitle(block.getTitle())
                                .withRankOrder(instruction.getRankOrder())
                                .build();
                        if (combinedInstruction != null) {
                            combinedInstructions.add(combinedInstruction);
                        }
                    } else {
                        combinedInstructions.add(instruction);
                    }
                }
            } else {
                combinedInstructions = instructions;
            }
        }
        return combinedInstructions;
    }

    /** {@inheritDoc} */
    public List<IngredientBlock> getIngredientBlocks() {
        return ingredientBlocks;
    }
}
