package com.scrippsnetworks.wcm.export.snipage.content.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.menu.Menu;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * This class is used for testing Menu Exports.
 * @author Venkata Naga Sudheer Donaboina
 */
public class MenuExportTest {
	public static final String PAGE_PATH = "/content/food/shows/menus/a-menu";
    public static final String PAGE_TYPE = "menu";

	private static final String[] MENU_MEAL_RECIPES = {
			"Appetizer|7c1f17b8-3b28-45b6-8045-34bd0cb6a985,Pancetta Wrapped Pork Roast",
			"Side Dish|e9fa481c-b2b4-477d-bc76-34719bef8f86,Prosciutto Wrapped Scallops" };
    
    @Mock
    SniPage menuPage;
    @Mock
    Menu menu;

    @Mock
    Resource menuPageCR;
    @Mock
    ValueMap menuPageProperties;

    @Mock
    PageManager pageManager;
    @Mock
    ResourceResolver resourceResolver;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        when(menuPage.hasContent()).thenReturn(true);
        when(menuPage.getProperties()).thenReturn(menuPageProperties);
        when(menuPage.getContentResource()).thenReturn(menuPageCR);
        when(menuPage.getPath()).thenReturn(PAGE_PATH);
        when(menuPage.getPageType()).thenReturn(PAGE_TYPE);
        when(menuPage.getPageManager()).thenReturn(pageManager);
        when(menu.getMealTypeRecipes()).thenReturn(MENU_MEAL_RECIPES);
    }

    @Test
    public void testMenuPropertyValues() {
    	MenuExport menuExport = new MenuExport(menuPage, menu);
        ValueMap exportProps = menuExport.getValueMap();

        String[] mealTypeRecipes = exportProps.get(MenuExport.ExportProperty.MENU_MEALTYPE_RECIPES.name(), String[].class);

        assertEquals("count of mealTypeRecipes", MENU_MEAL_RECIPES.length, mealTypeRecipes.length);
        
        assertEquals("1st mealType Recipe", MENU_MEAL_RECIPES[0], mealTypeRecipes[0]);
        
        assertEquals("2nd mealType Recipe", MENU_MEAL_RECIPES[1], mealTypeRecipes[1]);

    }
    
}
