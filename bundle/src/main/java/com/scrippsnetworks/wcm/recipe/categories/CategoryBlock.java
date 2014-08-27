package com.scrippsnetworks.wcm.recipe.categories;

import java.util.List;

/** Takes SniTags and turns them into groups of Categories.
 * @author Jason Clark
 *         Date: 7/9/13
 */
public interface CategoryBlock {

    /** Get the Categories from this container. */
    public List<Category> getCategories();

}
