package com.scrippsnetworks.wcm.export.snipage.content.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.Recipe;
import com.scrippsnetworks.wcm.recipe.listing.RecipeListing;

public class RecipeListingExportTest {
	
	public static final String PAGE_PATH = "/content/food/shows/recipes/a-recipelisting";
	public static final String PAGE_TYPE = "recipe-listing";
	
	public static final String RECIPE_PAGE1_UID = "aaaa-bbbb-cccc-dddd";
	public static final String RECIPE_PAGE2_UID = "aaaa-cccc-bbbb-dddd";
	public static final String RECIPE_PAGE3_UID = "aaaa-bbbb-dddd-cccc";
	
	@Mock RecipeListing recipeListing;
	
	@Mock Resource recipeListingPageCR;
	@Mock ValueMap recipeListingPageProperties;

    @Mock PageManager pageManager;
    @Mock ResourceResolver resourceResolver;
    
    @Mock SniPage recipeListingPage;
    
    @Mock Recipe recipe1;
    @Mock Recipe recipe2;
    @Mock Recipe recipe3;
    
    @Mock SniPage recipePage1;
    @Mock SniPage recipePage2;
    @Mock SniPage recipePage3;

    @Mock List<Recipe> recipes;
    
    @Before
    public void before() {
    	MockitoAnnotations.initMocks(this);
    	
    	when(recipeListingPage.hasContent()).thenReturn(true);
    	when(recipeListingPage.getProperties()).thenReturn(recipeListingPageProperties);
    	when(recipeListingPage.getContentResource()).thenReturn(recipeListingPageCR);
    	when(recipeListingPage.getPath()).thenReturn(PAGE_PATH);
    	when(recipeListingPage.getPageType()).thenReturn(PAGE_TYPE);
    	
    	when(recipeListingPage.getPageManager()).thenReturn(pageManager);
    	
    }
    
    /** set up recipes, recipe pages and recipePage Uid. */
    private void setupRecipes() {
    	recipes = Arrays.asList(recipe1, recipe2, recipe3);
    	when(recipeListing.getRecipes()).thenReturn(recipes);
    	
    	when(recipe1.getRecipePage()).thenReturn(recipePage1);
    	when(recipe2.getRecipePage()).thenReturn(recipePage2);
    	when(recipe3.getRecipePage()).thenReturn(recipePage3);
    	
    	when(recipePage1.getUid()).thenReturn(RECIPE_PAGE1_UID);
    	when(recipePage2.getUid()).thenReturn(RECIPE_PAGE2_UID);
    	when(recipePage3.getUid()).thenReturn(RECIPE_PAGE3_UID);
    	
    }
    
    @Test
    public void testRecipes() {
    	setupRecipes();
    	RecipeListingExport recipeListingExport = new RecipeListingExport(recipeListingPage, recipeListing);
    	ValueMap exportProps = recipeListingExport.getValueMap();
    	
    	String[] recipes = exportProps.get(RecipeListingExport.ExportProperty.RECIPELISTING_RECIPES.name(), String[].class);
    	
    	int i = 0;
    	for(Recipe recipe : recipeListing.getRecipes()) {
    		assertEquals("Recipe Page", recipe.getRecipePage().getUid(), recipes[i++]);
    	}
    }
}
