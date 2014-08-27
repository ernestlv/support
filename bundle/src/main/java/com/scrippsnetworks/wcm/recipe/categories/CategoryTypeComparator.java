package com.scrippsnetworks.wcm.recipe.categories;

import java.util.Comparator;

/** For sorting Categories by their type.
 * @author Jason Clark
 *         Date: 6/29/13
 */
public class CategoryTypeComparator implements Comparator<Category> {
    public int compare(Category category, Category category2) {
        CategoryTypes type1 = category.getCategoryType();
        CategoryTypes type2 = category2.getCategoryType();
        if (type1 == null && type2 == null) {
            return 0;
        } else if (type1 != null && type2 == null) {
            return Integer.MIN_VALUE;
        } else if (type1 == null) {
            return Integer.MAX_VALUE;
        } else {
            return type1.compareTo(type2);
        }
    }
}
