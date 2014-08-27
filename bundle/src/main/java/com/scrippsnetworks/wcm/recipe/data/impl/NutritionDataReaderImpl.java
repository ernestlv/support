package com.scrippsnetworks.wcm.recipe.data.impl;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.nutrition.NutritionBlock;
import com.scrippsnetworks.wcm.recipe.nutrition.NutritionBlockFactory;
import com.scrippsnetworks.wcm.recipe.data.NutritionDataReader;
import com.scrippsnetworks.wcm.util.ResourceRankOrderComparator;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Jason Clark
 *         Date: 7/15/13
 */
public class NutritionDataReaderImpl implements NutritionDataReader {

    private static final String NUTRITION_NODE = "nutrition";
    private static final String SNI_RANKORDER = "sni:rankOrder";
    private static final String JCR_TITLE = "jcr:title";
    private static final String NUTRITION_ANALYSIS = "Nutritional Analysis";

    /** Member for NutritionBlocks. */
    private List<NutritionBlock> nutritionBlocks;

    /** Construct a new Nutrition Data Reader. */
    public NutritionDataReaderImpl(final SniPage sniPage) {
        Resource nutritionResource = sniPage.getContentResource().getChild(NUTRITION_NODE);
        if (nutritionResource != null) {
            List<Resource> foundBlocks = new ArrayList<Resource>();
            Iterator<Resource> nodeIterator = nutritionResource.listChildren();
            while (nodeIterator.hasNext()) {
                Resource resource = nodeIterator.next();
                if (resource != null) {
                    ValueMap properties = resource.adaptTo(ValueMap.class);
                    if (properties.containsKey(SNI_RANKORDER)
                            && properties.containsKey(JCR_TITLE)
                            && !properties
                                .get(JCR_TITLE, String.class)
                                .equalsIgnoreCase(NUTRITION_ANALYSIS)) {
                        foundBlocks.add(resource);
                    }
                }
            }
            if (foundBlocks.size() > 1) {
                Collections.sort(foundBlocks, new ResourceRankOrderComparator());
            }
            nutritionBlocks = new ArrayList<NutritionBlock>();
            for (Resource resource : foundBlocks) {
                NutritionBlock block = new NutritionBlockFactory()
                        .withResource(resource)
                        .build();
                if (block != null) {
                    nutritionBlocks.add(block);
                }
            }
        }
    }

    /** {@inheritDoc} */
    public List<NutritionBlock> getNutritionBlocks() {
        return nutritionBlocks;
    }

}
