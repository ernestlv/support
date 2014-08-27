package com.scrippsnetworks.wcm.snipackage;

import org.apache.sling.api.resource.ValueMap;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.snipackage.SniPackage;
import com.scrippsnetworks.wcm.snipackage.SniPackageFactory;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SniPackageFactoryTest {

    public static final String DIRECT_PACKAGE_PATH = "/content/food/shows/a/a-show";
    public static final String HUB_PACKAGE_PATH = "/content/food/shows/b/b-show";

    @Mock SniPage sniPage;
    @Mock SniPage directPackageAnchor;
    @Mock ValueMap sniPageProperties;
    @Mock PageManager pageManager;

    @Mock Hub hub;
    @Mock SniPage hubPackageAnchor;
    @Mock SniPage hubMaster;
    @Mock ValueMap hubMasterProperties;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        when(sniPage.getProperties()).thenReturn(sniPageProperties);
        when(sniPage.getPageManager()).thenReturn(pageManager);
        when(pageManager.getPage(DIRECT_PACKAGE_PATH)).thenReturn(directPackageAnchor);
        when(directPackageAnchor.isValid()).thenReturn(true);

        when(hubMaster.getProperties()).thenReturn(hubMasterProperties);
        when(hubMaster.getPageManager()).thenReturn(pageManager);
        when(pageManager.getPage(HUB_PACKAGE_PATH)).thenReturn(hubPackageAnchor);
        when(hubPackageAnchor.isValid()).thenReturn(true);

        when(hub.getHubMaster()).thenReturn(hubMaster);
    }

    @Test
    public void testDirectPackage() {
        // Has a direct package and is not in a hub at all.

        // Setup
        when(sniPageProperties.get(SniPackage.PACKAGE_PROPERTY, String.class)).thenReturn(DIRECT_PACKAGE_PATH);

        // Test
        SniPackage pkg = SniPackageFactory.getSniPackage(sniPage);
        assertNotNull("direct package is not null", pkg);
        assertEquals("package anchor is direct anchor", directPackageAnchor, pkg.getPackageAnchor());
    }

    @Test
    public void testHubPackage() {
        // Has no direct package, but is in a hub whose master has a package.

        // Setup
        when(hubMasterProperties.get(SniPackage.PACKAGE_PROPERTY, String.class)).thenReturn(HUB_PACKAGE_PATH);
        when(sniPage.getHub()).thenReturn(hub);

        // Test
        SniPackage pkg = SniPackageFactory.getSniPackage(sniPage);
        assertNotNull("hub package is not null", pkg);
        assertEquals("package anchor is hub anchor", hubPackageAnchor, pkg.getPackageAnchor());
    }

    @Test
    public void testHubPrecedence() {
        // Has direct package, but also in a hub whose master has a package.

        // Setup
        when(sniPageProperties.get(SniPackage.PACKAGE_PROPERTY, String.class)).thenReturn(DIRECT_PACKAGE_PATH);
        when(hubMasterProperties.get(SniPackage.PACKAGE_PROPERTY, String.class)).thenReturn(HUB_PACKAGE_PATH);
        when(sniPage.getHub()).thenReturn(hub);

        // Test
        SniPackage pkg = SniPackageFactory.getSniPackage(sniPage);
        assertNotNull("hub package is not null", pkg);
        assertEquals("package anchor is hub anchor", hubPackageAnchor, pkg.getPackageAnchor());
    }

    @Test
    public void testHasHubButNoHubPackage() {
        // Has no direct package, and is in a hub with no package.
        
        // Setup
        when(sniPage.getHub()).thenReturn(hub);

        SniPackage pkg = SniPackageFactory.getSniPackage(sniPage);
        assertNull("no package when prop not set and hub master has no prop either", pkg);
    }

    @Test
    public void testInvalidDirect() {
        // Tests whether package respects the onTime/offTime behavior reflected in Page#isValid.

        // Setup
        when(sniPageProperties.get(SniPackage.PACKAGE_PROPERTY, String.class)).thenReturn(DIRECT_PACKAGE_PATH);
        when(directPackageAnchor.isValid()).thenReturn(false);

        // Test
        SniPackage pkg = SniPackageFactory.getSniPackage(sniPage);
        assertNull("no package when direct anchor page not valid", pkg);
    }

    @Test
    public void testInvalidHub() {
        // Tests whether package respects the onTime/offTime behavior reflected in Page#isValid.

        // Setup
        when(hubMasterProperties.get(SniPackage.PACKAGE_PROPERTY, String.class)).thenReturn(HUB_PACKAGE_PATH);
        when(sniPage.getHub()).thenReturn(hub);
        when(hubPackageAnchor.isValid()).thenReturn(false);

        // Test
        SniPackage pkg = SniPackageFactory.getSniPackage(sniPage);
        assertNull("no package when hub's anchor page not valid", pkg);
    }
}
