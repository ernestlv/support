package com.scrippsnetworks.wcm.recipe.ingredients.impl;

import static com.scrippsnetworks.wcm.recipe.data.DataReader.RECIPE_BODY;
import static com.scrippsnetworks.wcm.recipe.data.DataReader.RANK_ORDER;
import com.scrippsnetworks.wcm.recipe.ingredients.Ingredient;
import com.scrippsnetworks.wcm.recipe.ingredients.IngredientBlock;
import com.scrippsnetworks.wcm.recipe.ingredients.IngredientFactory;
import com.scrippsnetworks.wcm.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jason Clark
 *         Date: 6/17/13
 */
public class IngredientBlockImpl implements IngredientBlock {

    private static final Logger log = LoggerFactory.getLogger(IngredientBlockImpl.class);

    /** The list of Ingredients parsed from an ingredients data resource. */
    private List<Ingredient> ingredients;

    /** Rank Order of this block of ingredients. */
    private int rankOrder;

    /** Title from the recipe block. */
    private String title;

    /** A new IngredientsBlock from a Resource. */
    public IngredientBlockImpl(final Resource resource) {
        ValueMap properties = resource.adaptTo(ValueMap.class);
        if (properties.containsKey(RANK_ORDER)) {
            String rawRankOrder = properties.get(RANK_ORDER, String.class);
            if (StringUtils.isNotBlank(rawRankOrder)) {
                try {
                    rankOrder = Integer.valueOf(rawRankOrder);
                } catch (NumberFormatException e) {
                    log.error("NumberFormatException in IngredientBlock: {}", e);
                    rankOrder = 0;
                }
            }
        }
        if (properties.containsKey(RECIPE_BODY)) {
            String rawText = properties.get(RECIPE_BODY, String.class);
            if (StringUtils.isNotBlank(rawText)) {
                String[] rawTextLines = rawText.split("\n");
                List<String> rawIngredients = new ArrayList<String>();
                for (String line : rawTextLines) {
                    if (line.equals("")) {
                        continue;
                    }
                    String cleanedIngredient = StringUtil
                            .removeMarkupExceptAnchors(line)
                            .replaceAll("\\r|\\n", " ");
                    rawIngredients.add(cleanedIngredient);
                }
                if (rawIngredients.size() > 0) {
                    ingredients = new ArrayList<Ingredient>();
                    for (String rawIngredient : rawIngredients) {
                        Ingredient preppedIngredient = new IngredientFactory()
                                .withRmaText(rawIngredient)
                                .build();
                        if (preppedIngredient != null) {
//the obvious assumption here is that each block has only one title.  without that assumption, this title logic does not work
                            if (StringUtils.isNotBlank(preppedIngredient.getTitle())) {
                                title = preppedIngredient.getTitle();
                            }
                            ingredients.add(preppedIngredient);
                        }
                    }
                }
            }
        }

        /* This empties ingredients if it contains only a title with no other ingredients. */
        if (ingredients != null
                && ingredients.size() == 1
                && StringUtils.isNotBlank(ingredients.get(0).getTitle())) {
            ingredients = null;
        }
    }

    /** {@inheritDoc} */
    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    /** {@inheritDoc} */
    public int getRankOrder() {
        return rankOrder;
    }

    /** {@inheritDoc} */
    public String getTitle() {
        return title;
    }
}
