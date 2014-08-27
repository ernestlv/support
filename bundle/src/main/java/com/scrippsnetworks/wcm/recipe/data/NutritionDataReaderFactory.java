package com.scrippsnetworks.wcm.recipe.data;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.data.impl.NutritionDataReaderImpl;

/**
 * @author Jason Clark
 *         Date: 7/15/13
 */
public class NutritionDataReaderFactory {

    /** Member for SniPage used to construction NutritionDataReader. */
    private SniPage sniPage;

    /** Build a new NutritionDataReader. */
    public NutritionDataReader build() {
        if (sniPage != null) {
            return new NutritionDataReaderImpl(sniPage);
        }
        return null;
    }

    /** Add an SniPage to this builder. */
    public NutritionDataReaderFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }
}
