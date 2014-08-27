package com.scrippsnetworks.wcm.page.impl;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.page.impl.MockPage.MockPageFactory;
import com.scrippsnetworks.wcm.snipackage.SniPackage;

/** Tests SniPackage accessor, and old deprecated getPackageAnchor. */
public class TestSniPagePackageAccess {

    public static final String DIRECT_PACKAGE_PATH = "/content/food/shows/a/a-show";

    @Mock Page page;
    @Mock Resource pageResource;
    @Mock ValueMap pageProperties;
    @Mock PageManager pageManager;

    @Mock Page directPackageAnchor;
    @Mock Resource directPackageAnchorResource;
    @Mock ValueMap directPackageAnchorProperties;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(page.getProperties()).thenReturn(pageProperties);
        when(page.getContentResource()).thenReturn(pageResource);
        when(pageResource.adaptTo(ValueMap.class)).thenReturn(pageProperties);
        when(pageProperties.get(SniPackage.PACKAGE_PROPERTY, String.class)).thenReturn(DIRECT_PACKAGE_PATH);
        when(page.getPageManager()).thenReturn(pageManager);
        when(pageManager.getPage(DIRECT_PACKAGE_PATH)).thenReturn(directPackageAnchor);
        when(directPackageAnchor.isValid()).thenReturn(true);
        when(directPackageAnchor.getContentResource()).thenReturn(directPackageAnchorResource);
        when(directPackageAnchor.getPageManager()).thenReturn(pageManager);
        when(directPackageAnchor.getProperties()).thenReturn(directPackageAnchorProperties);
        when(directPackageAnchor.getPath()).thenReturn(DIRECT_PACKAGE_PATH);
    }

    @Test
    public void testSniPackageAccess() {
        SniPage sniPage = PageFactory.getSniPage(page);
        SniPackage pkg = sniPage.getSniPackage();
        assertNotNull("package not null", pkg);
        SniPackage pkg2 = sniPage.getSniPackage();
        assertTrue("rerequest returns same object", pkg == pkg2);
    }

    @Test
    public void testOldPackageAnchorAccessor() {
        SniPage sniPage = PageFactory.getSniPage(page);
        SniPage anchor = sniPage.getPackageAnchor();
        assertNotNull("direct package anchor returned by old accessor", anchor);
        assertTrue("package anchor is correct", DIRECT_PACKAGE_PATH.equals(anchor.getPath()));
        SniPage anchor2 = sniPage.getPackageAnchor();
        assertTrue("rerequest returns same object", anchor == anchor2);
    }
}
