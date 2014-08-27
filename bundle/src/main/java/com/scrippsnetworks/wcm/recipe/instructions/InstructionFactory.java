package com.scrippsnetworks.wcm.recipe.instructions;

import com.scrippsnetworks.wcm.recipe.instructions.impl.InstructionImpl;
import org.apache.sling.api.resource.Resource;

/**
 * Build an Instruction object out of a Resource for recipe data block.
 * @author Jason Clark
 *         Date: 6/18/13
 */
public class InstructionFactory {

    private Resource resource;
    private String rmaText;
    private int rankOrder = 0;
    private String title;
    private String text;

    /** Construct an Instruction out of the pieces we have. */
    public Instruction build() {
        if (resource != null) {
            return new InstructionImpl(resource);
        } else if (rmaText != null) {
            return new InstructionImpl(rmaText, rankOrder);
        } else if (text != null) {
            return new InstructionImpl(title, text, rankOrder);
        }
        return null;
    }

    /** Add an instruction block Resource to this builder. */
    public InstructionFactory withResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    /** Add raw RMA text to your instruction. */
    public InstructionFactory withRmaText(String rmaText) {
        this.rmaText = rmaText;
        return this;
    }

    /** Add a title to your instruction. */
    public InstructionFactory withTitle(String title) {
        this.title = title;
        return this;
    }

    /** Add text to your instruction. */
    public InstructionFactory withText(String text) {
        this.text = text;
        return this;
    }

    /** Add a rank order to this Instruction. */
    public InstructionFactory withRankOrder(int rankOrder) {
        this.rankOrder = rankOrder;
        return this;
    }

}
