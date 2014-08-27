package com.scrippsnetworks.wcm.recipe.ingredients;

import com.scrippsnetworks.wcm.recipe.ingredients.impl.IngredientImpl;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Jason Clark
 *         Date: 6/17/13
 */
public class IngredientFactory {

    private String amount;
    private String name;
    private String rmaText;

    /** Construct a new Ingredient.
     * At present, arguments that make sense are amount + name, or rma text.
     * Can return null.
     * @return Ingredient
     */
    public Ingredient build() {
        if (StringUtils.isNotBlank(amount) || StringUtils.isNotBlank(name)) {
            return new IngredientImpl(amount, name);
        } else if (StringUtils.isNotBlank(rmaText)) {
            return new IngredientImpl(rmaText);
        }
        return null;
    }

    /** Add a an amount to the ingredient.  You should add a name, too. */
    public IngredientFactory withAmount(String amount) {
        this.amount = amount;
        return this;
    }

    /** Add a name to the ingredient.  You should add an amount, too. */
    public IngredientFactory withName(String name) {
        this.name = name;
        return this;
    }

    /** Add an ingredient line from the rma and we'll parse it as best as we can. */
    public IngredientFactory withRmaText(String rmaText) {
        this.rmaText = rmaText;
        return this;
    }
}
