package com.scrippsnetworks.wcm.recipe.categories.impl;

import com.scrippsnetworks.wcm.recipe.categories.*;
import com.scrippsnetworks.wcm.snitag.SniTag;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author Jason Clark
 *         Date: 7/9/13
 */
public class CategoryBlockImpl implements CategoryBlock {

    /** Categories in this block. */
    private List<Category> categories;

    /** Construct a CategoryBlock with a List of SniTags. */
    public CategoryBlockImpl(List<SniTag> tags) {
        if (tags != null && tags.size() > 0) {

            Map<CategoryTypes, List<SniTag>> categoryMap =
                    new EnumMap<CategoryTypes, List<SniTag>>(CategoryTypes.class);

            boolean sortMainIngredients = true; //if main ingredient is primary/secondary tag, no sorting

            for (int i = 0; i < tags.size(); i++) {
                SniTag tag = tags.get(i);
                if (tag != null && StringUtils.isNotBlank(tag.getValue()) && tag.getTopicPage() != null) {
                    CategoryTypes tagType = CategoryTypes.getTagCategory(tag);
                    if (tagType != null) {
                        //check the first couple of tags; if they are main ingredients, don't sort main ingredients
                        if ((i == 0 || i == 1) && tagType.equals(CategoryTypes.MAIN_INGREDIENT)) {
                            sortMainIngredients = false;
                        }
                        if (categoryMap.containsKey(tagType)) {
                            List<SniTag> categoryTags = categoryMap.get(tagType);
                            if (categoryTags == null) {
                                categoryTags = new ArrayList<SniTag>();
                            }
                            //only add tag to categories if limit for this type of tag isn't met
                            if (categoryTags.size() < tagType.limit()) {
                                categoryTags.add(tag);
                            }
                            categoryMap.put(tagType, categoryTags);
                        } else {
                            List<SniTag> categoryTags = new ArrayList<SniTag>();
                            categoryTags.add(tag);
                            categoryMap.put(tagType, categoryTags);
                        }
                    }
                }
            }

            if (categoryMap.size() > 0) {
                categories = new ArrayList<Category>();
                for (Map.Entry<CategoryTypes, List<SniTag>> entry : categoryMap.entrySet()) {
                    List<SniTag> sniTags = entry.getValue();
                    //check if we're on main ingredients category, respect primary/secondary tags if so
                    boolean isMainIngredient = entry.getKey().equals(CategoryTypes.MAIN_INGREDIENT);
                    if (!isMainIngredient || (isMainIngredient && sortMainIngredients)) {
                        //alphabetize the tags before stuffing them into a category
                        Collections.sort(sniTags, new TagNameAlphabeticalComparator());
                    }
                    Category category = new CategoryFactory()
                            .withCategoryType(entry.getKey())
                            .withSniTags(sniTags)
                            .build();
                    if (category != null) {
                        categories.add(category);
                    }
                }
            }

            if (categories != null) {
                Collections.sort(categories, new CategoryTypeComparator());
            }
        }
    }

    /** {@inheritDoc} */
    public List<Category> getCategories() {
        return categories;
    }

}
