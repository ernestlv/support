package com.scrippsnetworks.wcm.recipe.categories;

import com.scrippsnetworks.wcm.recipe.categories.impl.CategoryBlockImpl;
import com.scrippsnetworks.wcm.snitag.SniTag;

import java.util.List;

/**
 * @author Jason Clark
 *         Date: 7/9/13
 */
public class CategoryBlockFactory {

    /** List of SniTags from SniPage. */
    private List<SniTag> tags;

    /** Return a new CategoryBlock using the inputs given. */
    public CategoryBlock build() {
        if (tags != null) {
            return new CategoryBlockImpl(tags);
        }
        return null;
    }

    /** Add Tags to this builder. */
    public CategoryBlockFactory withTags(List<SniTag> tags) {
        this.tags = tags;
        return this;
    }

}
