package com.scrippsnetworks.wcm.recipe.ingredients.impl

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Test IngredientImpl.
 * @author Jason Clark
 * Date: 6/17/13
 */
class IngredientImplTest extends Specification {
    @Unroll
    def "Deterministically built Ingredient should be properly formatted."() {
        when:
        String ingredient = new IngredientImpl(amount, name).toString()

        then:
        ingredient == result

        where:
        amount << ["1 1/2", "1"]
        name << ["teaspoons of foo", "yucca, chopped"]
        result << ["1 1/2 teaspoons of foo", "1 yucca, chopped"]
    }

    @Unroll
    def "Ingredient built from crazy rma string should have an amount and a name."() {

        when:
        IngredientImpl ingredient = new IngredientImpl(rmaText)

        then:
        ingredient.getAmount() == amount
        ingredient.getName() == name

        where:
        rmaText << ["1 1/2 onions", "5 1/2 cups of blah", "a bag of beans", "1 2/3"]
        amount << ["1 1/2", "5 1/2", null, "1 2/3"]
        name << ["onions", "cups of blah", "a bag of beans", null]

    }

    @Unroll
    def "Ingredient built from crazy rma string should look OK when toString is called"() {

        when:
        String result = new IngredientImpl(rmaText).toString()

        then:
        result == expectedResult

        where:
        rmaText << ["foo bar baz", "1 1/2   tons of fun     "]
        expectedResult << ["foo bar baz", "1 1/2 tons of fun"]

    }

    @Unroll
    def "Ingredient built from title-like data should have a title."() {

        when:
        IngredientImpl ingredient = new IngredientImpl(titleData)

        then:
        ingredient.getTitle() == titleData

        where:
        titleData << [ "EXCITABLE TITLE", "foobarbaz:", "THIS ACTUALLY TRIPS THE COLON PATTERN:", "THIS-SHOULD-NOT-FAIL"]

    }
}
