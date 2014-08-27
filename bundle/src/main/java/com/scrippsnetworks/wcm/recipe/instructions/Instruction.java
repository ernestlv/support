package com.scrippsnetworks.wcm.recipe.instructions;

/**
 * Recipe instructions.
 * @author Jason Clark
 *         Date: 6/14/13
 */
public interface Instruction {

    /** Title above the instruction text, if any. */
    public String getTitle();

    /** Text from the instruction. */
    public String getText();

    /** sni:rankOrder from the data block. */
    public int getRankOrder();

}
