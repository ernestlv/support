package com.scrippsnetworks.wcm.recipe.data;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.data.impl.DataReaderImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Get recipe data from the blocks under the recipe asset. The data
 * that is returned takes into account crosslinking and sorting by
 * rank order.
 * @author Jason Clark
 *         Date: 6/12/13
 */
public class DataReaderFactory {

    /** Should be SniPage for the recipe content page. */
    private SniPage sniPage;

    /** This will identify the type of block to retrieve. */
    private List<DataBlockTypes> blockTypes = new ArrayList<DataBlockTypes>();

    /** Try to share titles between ingredients and Instructions? */
    private boolean shareTitles = false;

    /**
     * Construct a new RecipeBlock with the parameters given.
     * @return RecipeBlock
     */
    public DataReader build() {
        if (sniPage != null && blockTypes.size() > 0) {
            return new DataReaderImpl(sniPage, blockTypes);
        }
        return null;
    }

    /** Add an SniPage to this builder. */
    public DataReaderFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }

    /** Get blocks that are named "ingredients" or some variation. */
    public DataReaderFactory readIngredients() {
        blockTypes.add(DataBlockTypes.INGREDIENTS);
        return this;
    }

    /** Get blocks that are named "instructions" or some variation. */
    public DataReaderFactory readInstructions() {
        blockTypes.add(DataBlockTypes.INSTRUCTIONS);
        return this;
    }

    /** Get blocks that are named "directions" or some variation. */
    public DataReaderFactory readDirections() {
        blockTypes.add(DataBlockTypes.DIRECTIONS);
        return this;
    }

    /** Get blocks that are named "notes" or some variation. */
    public DataReaderFactory readNotes() {
        blockTypes.add(DataBlockTypes.NOTES);
        return this;
    }

    /** We've got recipe blocks that can be named anything, so get those. */
    public DataReaderFactory readOthers() {
        blockTypes.add(DataBlockTypes.OTHER);
        return this;
    }

    /** Try to share titles between ingredients and instructions. */
    public DataReaderFactory shareTitles() {
        shareTitles = true;
        return this;
    }

}
