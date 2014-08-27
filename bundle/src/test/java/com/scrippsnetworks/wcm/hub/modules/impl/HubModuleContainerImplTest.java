package com.scrippsnetworks.wcm.hub.modules.impl;

import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.hub.modules.HubModuleContainer;
import com.scrippsnetworks.wcm.page.SniPage;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

public class HubModuleContainerImplTest {

    @Mock SniPage sniPage;
    @Mock Hub hub;

    @Mock ValueMap hubProps;
    @Mock Resource pageContent;
    @Mock ResourceResolver resourceResolver;

    @Mock Resource hubResource1;
    @Mock Resource hubResource2;

    private static final String MODULE_PATH1 = "/content/food/modules/module1";
    private static final String MODULE_PATH2 = "/content/food/modules/module2";

    private String[] modulePaths = new String[] {MODULE_PATH1, MODULE_PATH2};

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testNullSniPage() {
        // Null object (not null value) when initialized with null page.
        // Container's modules() returns an empty list.
        HubModuleContainerImpl hubModuleContainer = new HubModuleContainerImpl(null);
        assertNotNull(hubModuleContainer);
        List<Resource> hubModules = hubModuleContainer.getModules();
        assertNotNull(hubModules);
        assertEquals(0,hubModules.size());
    }

    @Test
    public void testNullHub() {
        // Null object (not null value) when initialized with page with null hub.
        // Container's modules() returns an empty list.
        HubModuleContainerImpl hubModuleContainer = new HubModuleContainerImpl(sniPage);
        assertNotNull(hubModuleContainer);
        List<Resource> hubModules = hubModuleContainer.getModules();
        assertNotNull(hubModules);
        assertEquals(0,hubModules.size());
    }

    @Test
    public void testPageNoContentResource() {
        // Null object (not null value) when initialized with page with no content resource.
        // Container's modules() returns an empty list.
        when(sniPage.hasContent()).thenReturn(false);
        HubModuleContainerImpl hubModuleContainer = new HubModuleContainerImpl(sniPage);
        assertNotNull(hubModuleContainer);
        List<Resource> hubModules = hubModuleContainer.getModules();
        assertNotNull(hubModules);
        assertEquals(0,hubModules.size());
    }

    @Test
    public void testHappyPath() {
        when(sniPage.getHub()).thenReturn(hub);
        when(sniPage.getContentResource()).thenReturn(pageContent);
        when(sniPage.hasContent()).thenReturn(true);
        when(pageContent.getResourceResolver()).thenReturn(resourceResolver);
        when(hub.getHubProperties()).thenReturn(hubProps);
        when(hubProps.containsKey(HubModuleContainer.HUB_MODULES_PROPERTY)).thenReturn(true);
        when(hubProps.get(HubModuleContainer.HUB_MODULES_PROPERTY, String[].class)).thenReturn(modulePaths);
        when(resourceResolver.getResource(MODULE_PATH1)).thenReturn(hubResource1);
        when(resourceResolver.getResource(MODULE_PATH2)).thenReturn(hubResource2);

        HubModuleContainerImpl hubModuleContainer = new HubModuleContainerImpl(sniPage);
        assertNotNull(hubModuleContainer);
        List<Resource> hubModules = hubModuleContainer.getModules();
        assertNotNull(hubModules);
        assertEquals(2,hubModules.size());
    }
}