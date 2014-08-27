package com.scrippsnetworks.wcm.snipackage.impl;

import java.util.Arrays;
import java.util.Map;
import java.util.List;
import org.apache.jackrabbit.JcrConstants; 
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.snipackage.SniPackage;
import com.scrippsnetworks.wcm.snipackage.impl.BaseSniPackage;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class BaseSniPackageTest {

    public static final String REGION_RESOURCE_NAME = "right-rail";
    public static final String CURRENT_PAGE_TYPE = "recipe";
    public static final String PACKAGE_NAME = "A Package";
    public static final String PACKAGE_THEME = "Blueberry";
    public static final String REGION_PATH_ANY = "/content/food/regions/right-rail/any/my-right-rail";
    public static final String REGION_PATH_RECIPE = "/content/food/regions/recipe/right-rail/my-right-rail";
    public static final String REGION_PATH_NONEXISTING = "/content/food/regions/right-rail/does-not-exist";
    public static final String ANY_REGIONS_VALUE = SniPackage.WILDCARD_PAGE_TYPE + "|" + REGION_PATH_ANY;
    public static final String CURRENT_REGIONS_VALUE = CURRENT_PAGE_TYPE + "|" + REGION_PATH_RECIPE;
    public static final String ANOTHER_REGIONS_VALUE = "foobar|/content/food/regions/right-rail/my-right-rail";
    public static final String NONEXISTING_REGIONS_VALUE = CURRENT_PAGE_TYPE + "|" + REGION_PATH_NONEXISTING;
    public static final String ANCHOR_PATH = "/content/food/shows/a/a-show";
    public static final String MODULE_PATH = ANCHOR_PATH + "/" + JcrConstants.JCR_CONTENT + "/superlead/a-superlead-module";
    public static final String MODULE_NONEXISTING_PATH = ANCHOR_PATH + "/" + JcrConstants.JCR_CONTENT + "/noreagion/doesntexist";
    public static final String[] MODULES_VALUE = { MODULE_PATH, MODULE_NONEXISTING_PATH };

    @Mock SniPage sniPage;
    @Mock Resource sniPageCR;
    @Mock ValueMap sniPageProperties;
    @Mock SniPage packageAnchor;
    @Mock Resource packageAnchorCR;
    @Mock ValueMap packageAnchorProperties;
    @Mock PageManager pageManager;
    @Mock ResourceResolver resourceResolver;

    @Mock SniPage hubPackageAnchor;
    @Mock Resource hubPackageAnchorCR;
    @Mock ValueMap hubPackageAnchorProperties;

    @Mock Page anyRegionEditor;
    @Mock Resource anyRegionEditorCR;
    @Mock Resource anyRegionEditorParsys;
    @Mock Page recipeRegionEditor;
    @Mock Resource recipeRegionEditorCR;
    @Mock Resource recipeRegionEditorParsys;

    @Mock Resource moduleResource;
    @Mock Resource moduleResourceNonexisting;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        when(sniPage.getPageManager()).thenReturn(pageManager);
        when(sniPage.getProperties()).thenReturn(sniPageProperties);
        when(sniPage.getPageType()).thenReturn(CURRENT_PAGE_TYPE);
        when(sniPage.getContentResource()).thenReturn(sniPageCR);
        when(packageAnchor.getContentResource()).thenReturn(packageAnchorCR);
        when(packageAnchorCR.getResourceResolver()).thenReturn(resourceResolver);
        when(sniPageCR.getResourceResolver()).thenReturn(resourceResolver);
        when(packageAnchorCR.getResourceResolver()).thenReturn(resourceResolver);
        when(resourceResolver.getResource(MODULE_PATH)).thenReturn(moduleResource);
        when(pageManager.getPage(REGION_PATH_ANY)).thenReturn(anyRegionEditor);
        when(pageManager.getPage(REGION_PATH_RECIPE)).thenReturn(recipeRegionEditor);
        when(anyRegionEditor.getPath()).thenReturn(REGION_PATH_ANY);
        when(recipeRegionEditor.getPath()).thenReturn(REGION_PATH_RECIPE);
        when(anyRegionEditor.getContentResource()).thenReturn(anyRegionEditorCR);
        when(recipeRegionEditor.getContentResource()).thenReturn(recipeRegionEditorCR);
        List<Resource> anyChildren = Arrays.<Resource>asList(anyRegionEditorParsys);
        List<Resource> recipeChildren = Arrays.<Resource>asList(recipeRegionEditorParsys);
        when(anyRegionEditorCR.getPath()).thenReturn(REGION_PATH_ANY + JcrConstants.JCR_CONTENT);
        when(recipeRegionEditorCR.getPath()).thenReturn(REGION_PATH_RECIPE + JcrConstants.JCR_CONTENT);
        when(anyRegionEditorCR.listChildren()).thenReturn(anyChildren.iterator());
        when(recipeRegionEditorCR.listChildren()).thenReturn(recipeChildren.iterator());
        when(anyRegionEditorParsys.getResourceType()).thenReturn(SniPackage.REGION_RESOURCE_TYPE);
        when(recipeRegionEditorParsys.getResourceType()).thenReturn(SniPackage.REGION_RESOURCE_TYPE);
        when(anyRegionEditorParsys.getName()).thenReturn(REGION_RESOURCE_NAME);
        when(recipeRegionEditorParsys.getName()).thenReturn(REGION_RESOURCE_NAME);
        when(packageAnchor.getPageManager()).thenReturn(pageManager);
        when(packageAnchor.getTitle()).thenReturn(PACKAGE_NAME);
        when(packageAnchor.getProperties()).thenReturn(packageAnchorProperties);
        String[] regionsValue = { ANY_REGIONS_VALUE, CURRENT_REGIONS_VALUE, NONEXISTING_REGIONS_VALUE };
        when(packageAnchorProperties.get(SniPackage.REGIONS_PROPERTY, String[].class)).thenReturn(regionsValue);
        when(packageAnchorProperties.get(SniPackage.MODULES_PROPERTY, String[].class)).thenReturn(MODULES_VALUE);

        when(resourceResolver.getResource(MODULE_PATH)).thenReturn(moduleResource);
    }

    @Test
    public void testDirectHappyPath() {
        // use direct package anchor
        BaseSniPackage pkg = new BaseSniPackage(sniPage, packageAnchor, null);

        assertNotNull("package is not null", pkg);
        assertEquals("anchor is returned", packageAnchor, pkg.getPackageAnchor());
        assertEquals("relation is correct", SniPackage.PackageRelation.DIRECT, pkg.getPackageRelation());
        Map<String, Resource> regions = pkg.getRegions();
        assertNotNull("regions not null", regions);
        assertTrue("region available", regions.size() > 0);
        Resource regionResource = regions.get(REGION_RESOURCE_NAME);
        assertTrue("recipe region resource returned", regionResource == recipeRegionEditorParsys);

        List<Resource> modules = pkg.getModules();
        // only existing module (one the resource resolver returns) is returned.
        assertTrue("modules size > 0", modules.size() == 1);
        // the returned module is the existing one
        assertTrue("correct module returned", modules.get(0) == moduleResource);
    }

    @Test
    public void testHubHappyPath() {
        // use direct package anchor
        BaseSniPackage pkg = new BaseSniPackage(sniPage, null, packageAnchor);

        assertNotNull("package is not null", pkg);
        assertEquals("anchor is returned", packageAnchor, pkg.getPackageAnchor());
        assertEquals("relation is correct", SniPackage.PackageRelation.HUB, pkg.getPackageRelation());
        Map<String, Resource> regions = pkg.getRegions();
        assertNotNull("regions not null", regions);
        assertTrue("region available", regions.size() > 0);
        Resource regionResource = regions.get(REGION_RESOURCE_NAME);
        assertTrue("recipe region resource returned", regionResource == recipeRegionEditorParsys);

        List<Resource> modules = pkg.getModules();
        // only existing module (one the resource resolver returns) is returned.
        assertTrue("modules size > 0", modules.size() == 1);
        // the returned module is the existing one
        assertTrue("correct module returned", modules.get(0) == moduleResource);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNoAnchorException() {
        BaseSniPackage pkg = new BaseSniPackage(sniPage, null, null);
    }

    @Test
    public void testHubPrecedence() {
        when(hubPackageAnchor.getContentResource()).thenReturn(hubPackageAnchorCR);
        when(hubPackageAnchorCR.getResourceResolver()).thenReturn(resourceResolver);
        when(hubPackageAnchor.getTitle()).thenReturn("HUB " + PACKAGE_NAME);
        when(hubPackageAnchor.getProperties()).thenReturn(hubPackageAnchorProperties);
        BaseSniPackage pkg = new BaseSniPackage(sniPage, hubPackageAnchor, hubPackageAnchor);
        assertNotNull("package is not null", pkg);
        assertEquals("package anchor is hub anchor", hubPackageAnchor, pkg.getPackageAnchor());
    }

}
