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
import com.scrippsnetworks.wcm.menu.Menu;
import com.scrippsnetworks.wcm.menu.listing.MenuListing;
import com.scrippsnetworks.wcm.page.SniPage;

public class MenuListingExportTest {
	
	public static final String PAGE_PATH = "/content/food/shows/menus/a-menulisting";
	public static final String PAGE_TYPE = "menu-listing";
	
	public static final String MENU_PAGE1_UID = "aaaa-bbbb-cccc-dddd";
	public static final String MENU_PAGE2_UID = "aaaa-cccc-bbbb-dddd";
	public static final String MENU_PAGE3_UID = "aaaa-bbbb-dddd-cccc";
	
	@Mock MenuListing menuListing;
	
	@Mock Resource menuListingPageCR;
	@Mock ValueMap menuListingPageProperties;

    @Mock PageManager pageManager;
    @Mock ResourceResolver resourceResolver;
    
    @Mock SniPage menuListingPage;
    
    @Mock Menu menu1;
    @Mock Menu menu2;
    @Mock Menu menu3;
    
    @Mock SniPage menuPage1;
    @Mock SniPage menuPage2;
    @Mock SniPage menuPage3;

    @Mock List<Menu> menus;
    
    @Before
    public void before() {
    	MockitoAnnotations.initMocks(this);
    	
    	when(menuListingPage.hasContent()).thenReturn(true);
    	when(menuListingPage.getProperties()).thenReturn(menuListingPageProperties);
    	when(menuListingPage.getContentResource()).thenReturn(menuListingPageCR);
    	when(menuListingPage.getPath()).thenReturn(PAGE_PATH);
    	when(menuListingPage.getPageType()).thenReturn(PAGE_TYPE);
    	
    	when(menuListingPage.getPageManager()).thenReturn(pageManager);
    	
    }
    
    /** set up menus, menu pages and menuPage Uid. */
    private void setupMenus() {
    	menus = Arrays.asList(menu1, menu2, menu3);
    	when(menuListing.getMenus()).thenReturn(menus);
    	
    	when(menu1.getSniPage()).thenReturn(menuPage1);
    	when(menu2.getSniPage()).thenReturn(menuPage2);
    	when(menu3.getSniPage()).thenReturn(menuPage3);
    	
    	when(menuPage1.getUid()).thenReturn(MENU_PAGE1_UID);
    	when(menuPage2.getUid()).thenReturn(MENU_PAGE2_UID);
    	when(menuPage3.getUid()).thenReturn(MENU_PAGE3_UID);
    	
    }
    
    @Test
    public void testMenus() {
    	setupMenus();
    	MenuListingExport menuListingExport = new MenuListingExport(menuListingPage, menuListing);
    	ValueMap exportProps = menuListingExport.getValueMap();
    	
    	String[] menus = exportProps.get(MenuListingExport.ExportProperty.MENULISTING_MENUS.name(), String[].class);
    	
    	int i = 0;
    	for(Menu menu : menuListing.getMenus()) {
    		assertEquals("Menu Page", menu.getSniPage().getUid(), menus[i++]);
    	}
    }
    
}
