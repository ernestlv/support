package com.scrippsnetworks.wcm.recipe.categories;

import com.scrippsnetworks.wcm.recipe.categories.impl.CategoryImpl;
import com.scrippsnetworks.wcm.snitag.SniTag;
import com.scrippsnetworks.wcm.page.SniPage;

import java.util.List;

/**
 * @author Jason Clark
 *         Date: 6/29/13
 */
public class CategoryFactory {

    /** SniTags used to construct the category. */
    private List<SniTag> sniTags;

    /** CategoryTypes Enum used to identify this type. */
    private CategoryTypes categoryType;

    /** Construct a new Category from the input given. */
    public Category build() {
        if (sniTags != null && categoryType != null) {
            return new CategoryImpl(categoryType, sniTags);
        }
        return null;
    }

    /** Add SniTags to this Category. */
    public CategoryFactory withSniTags(List<SniTag> sniTags) {
        this.sniTags = sniTags;
        return this;
    }

    /** Add a CategoryTypes Enum to this Category. */
    public CategoryFactory withCategoryType(CategoryTypes categoryType) {
        this.categoryType = categoryType;
        return this;
    }

}
