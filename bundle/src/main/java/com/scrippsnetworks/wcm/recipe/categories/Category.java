package com.scrippsnetworks.wcm.recipe.categories;

import com.scrippsnetworks.wcm.snitag.SniTag;

import java.util.List;

/**
 * @author Jason Clark
 *         Date: 6/29/13
 */
public interface Category {

    /** The name (facet) of this Category. */
    public String getDisplayName();

    /** The Tags related to this Category. */
    public List<SniTag> getSniTags();

    /** The Enum for the type of this Category (used for sorting). */
    public CategoryTypes getCategoryType();

}
