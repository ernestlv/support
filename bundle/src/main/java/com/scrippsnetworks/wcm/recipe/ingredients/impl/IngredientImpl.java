package com.scrippsnetworks.wcm.recipe.ingredients.impl;

import com.scrippsnetworks.wcm.recipe.ingredients.Ingredient;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Logical representation of an Ingredient.
 * Parsed from recipe data.
 * @author Jason Clark
 *         Date: 6/14/13
 */
public class IngredientImpl implements Ingredient {

    /** Member for ingredient amount. */
    private String amount;

    /** Member for ingredient name. */
    private String name;

    /** Title from ingredients block. */
    private String title;

    /**
     * Parse ingredient from raw ingredient data.
     * @param rawText Raw data to parse.
     */
    public IngredientImpl(final String rawText) {
        if (StringUtils.isNotBlank(rawText)) {
            Pattern ingredientPattern = Pattern.compile("^([0-9/ ]+)?(.+)?$");
            Matcher ingredientMatcher = ingredientPattern.matcher(rawText);
            if (ingredientMatcher.matches()) {
                String amount = ingredientMatcher.group(1);
                String name = ingredientMatcher.group(2);
                if (StringUtils.isNotBlank(amount)) {
                    this.amount = StringUtils.strip(amount);
                }
                if (StringUtils.isNotBlank(name)) {
                    this.name = StringUtils.strip(name);
                }
            }
            if (rawText.equals(rawText.toUpperCase()) || rawText.matches(".*:$")) {
                this.title = rawText;
            }
        }
    }

    /**
     * Construct an Ingredient deterministically.
     * @param amount String amount of ingredient.
     * @param name String name of ingredient.
     */
    public IngredientImpl(final String amount, final String name) {
        this.amount = amount;
        this.name = name;
    }

    /** {@inheritDoc} */
    public String getAmount() {
        return amount;
    }

    /** {@inheritDoc} */
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    public String getTitle() {
        return title;
    }

    /** {@inheritDoc} */
    public String toString() {
        if (StringUtils.isNotBlank(title)) {
            return title;
        }
        return (amount != null ? amount + " " : "") + (name != null ? name : "");
    }
}
