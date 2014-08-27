package com.scrippsnetworks.wcm.recipe.listing.impl;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.Recipe;
import com.scrippsnetworks.wcm.recipe.RecipeFactory;
import com.scrippsnetworks.wcm.recipe.listing.RecipeListing;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Jonathan Bell
 *         Date: 8/30/2013
 */
public class RecipeListingImpl implements RecipeListing {

    private static final String ITEMS_HOME = "recipes";

    private SniPage sniPage;
    private Resource resource;
    private ValueMap vm;
    private int total;
    private List<Recipe> recipes;

    public RecipeListingImpl(SniPage sniPage) {
        this.sniPage = sniPage;
        this.resource = sniPage.adaptTo(Resource.class);
        this.vm = sniPage.getContentResource().adaptTo(ValueMap.class);
        this.recipes = new ArrayList<Recipe>();
        initializeRecipes();
    }

    private void initializeRecipes() {
        String[] recipePaths = vm.get(ITEMS_HOME, String[].class);
        if (recipePaths != null) {
            RecipeFactory rf = new RecipeFactory();

            for (String recipePath : recipePaths) {
                if (!StringUtils.isEmpty(recipePath)) {
                    ResourceResolver rr = resource.getResourceResolver();
                    Resource res = rr.getResource(recipePath);
                    Recipe recipe = rf.withResource(res).build();
                    if (recipe != null) {
                        recipes.add(recipe);
                    }
                }
            }

            total = recipes.size();
        }
    }

    /** {@inheritDoc} */
    public List<Recipe> getRecipes() {
        return recipes;
    }

    /** {@inheritDoc} */
    public int getTotalSize() {
        return total;
    }
}

