package com.scrippsnetworks.wcm.recipe.categories.impl;

import com.scrippsnetworks.wcm.recipe.categories.Category;
import com.scrippsnetworks.wcm.recipe.categories.CategoryTypes;
import com.scrippsnetworks.wcm.snitag.SniTag;

import java.util.List;

/**
 * @author Jason Clark
 *         Date: 6/29/13
 */
public class CategoryImpl implements Category {

    /** Tag passed into constructor. */
    private List<SniTag> sniTags;

    /** Display name of this category. */
    private String displayName;

    /** The CategoryType Enum associated with this Category. */
    private CategoryTypes categoryType;

    /** Construct a new Category given a Tag. */
    public CategoryImpl(final CategoryTypes categoryType, final List<SniTag> sniTags) {
        if (sniTags != null && sniTags.size() > 0 && categoryType != null) {
            this.sniTags = sniTags;
            this.categoryType = categoryType;
            this.displayName = categoryType.displayName();
        }
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return displayName;
    }

    /** {@inheritDoc} */
    public List<SniTag> getSniTags() {
        return sniTags;
    }

    /** {@inheritDoc} */
    public CategoryTypes getCategoryType() {
        return categoryType;
    }

}
