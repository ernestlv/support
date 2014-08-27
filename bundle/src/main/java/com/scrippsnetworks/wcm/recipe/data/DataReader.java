package com.scrippsnetworks.wcm.recipe.data;

import org.apache.sling.api.resource.Resource;

import java.util.List;

/**
 * @author Jason Clark
 *         Date: 6/11/13
 */
public interface DataReader {
    String NODE_NAME_BLOCKS = "blocks";
    String JCR_TITLE = "jcr:title";
    String RECIPE_BODY = "sni:recipeBody";
    String RANK_ORDER = "sni:rankOrder";
    String XLINKED = "crosslinked";

    /** Returns a list of the block resources sorted by rank order. */
    public List<Resource> getSortedBlocks();
}
