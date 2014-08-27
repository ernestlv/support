package com.scrippsnetworks.wcm.recipe.categories;

import com.scrippsnetworks.wcm.snitag.SniTag;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/** Recipe CategoryTypes.
 * This Enum is in order of precedence that these categories should render on
 * Recipe pages. The facetNames are possible tag facets that align with this
 * category. The display name is the friendly name of the category.
 * @author Jason Clark
 *         Date: 6/29/13
 */
public enum CategoryTypes {

    MAIN_INGREDIENT(new ArrayList<String>(){{add("main-ingredient");}}, "Main Ingredient", 3),
    COURSE(new ArrayList<String>(){{add("meal-part"); add("meal-type");}}, "Course", 2),
    DISH(new ArrayList<String>(){{add("dish");}}, "Dish", 1),
    DRINK(new ArrayList<String>(){{add("drinks");}}, "Drink", 1),
    TECHNIQUE(new ArrayList<String>(){{add("technique");}}, "Technique", 1),
    CUISINE(new ArrayList<String>(){{add("cuisine");}}, "Cuisine", 1),
    OCCASION(new ArrayList<String>(){{add("occasions");}}, "Occasion", 2),
    WHOS_DINING(new ArrayList<String>(){{add("whos-dining");}}, "Who's Dining", 1),
    COOKING_STYLE(new ArrayList<String>(){{add("cooking-styles");}}, "Cooking Style", 2),
    NUTRITION(new ArrayList<String>(){{add("nutrition");}}, "Nutrition", 7),
    SEASON(new ArrayList<String>(){{add("season");}}, "Season", 1),
    HERBS_SPICES(new ArrayList<String>(){{add("herbs-spices"); add("herbs-and-spices");}}, "Herbs & Spices", 2);

    /** Names of possible tag facets that align with the category. */
    private List<String> facetNames;

    /** Render-friendly name of the category. */
    private String displayName;

    /** Limit on the number of items of this kind that can be on the page at once. */
    private int limit;

    private CategoryTypes(List<String> facetNames, String displayName, int limit) {
        this.facetNames = facetNames;
        this.displayName = displayName;
        this.limit = limit;
    }

    /** String[] of facet names that align with this category. */
    public List<String> facetNames() {
        return this.facetNames;
    }

    /** Display-friendly String for this category. */
    public String displayName() {
        return this.displayName;
    }

    /** Get the allowed number of this type of tag for the recipe page. */
    public int limit() {
        return this.limit;
    }

    /** Identify the Category for a given Tag. Can return null. */
    public static CategoryTypes getTagCategory(final SniTag sniTag) {
        if (sniTag != null) {
            String facet = sniTag.getFacet();
            if (StringUtils.isNotBlank(facet)) {
                for (CategoryTypes category : CategoryTypes.values()) {
                    for (String categoryFacet : category.facetNames()) {
                        if (facet.equals(categoryFacet)) {
                            return category;
                        }
                    }
                }
            }
        }
        return null;
    }
}
