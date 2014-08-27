package com.scrippsnetworks.wcm.metadata.impl.provider;

import java.util.Map;
import java.util.HashMap;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.metadata.MetadataProperty;
import com.scrippsnetworks.wcm.metadata.MetadataProvider;
import com.day.cq.wcm.api.Page; 
import com.day.cq.wcm.api.PageManager; 
import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.resource.Resource;


import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;



public class TestBaseLocationMetadataProvider {

    private final String TEST_CONTENT_PAGE_TITLE = "A Title";
    private final String TEST_RESOURCE_TYPE = "sni-wcm/test/resource";
    private final String TEST_CONTENT_PATH = "/content/food/recipes/no-chef/t/te/tes/test/test-recipe";
    private final String TEST_NAME = "test-recipe";
    private final int TEST_CONTENT_DEPTH = 8;

    private final String TEST_PARENT_PATH = "/content/food/recipes/no-chef";
    private final String TEST_PARENT_NAME = "no-chef";
    private final int TEST_PARENT_DEPTH = 3;
    private final String TEST_PARENT_PAGE_TYPE = "sni-food/components/pagetypes/parent-type";

    private final String TEST_SECTION_PATH = "/content/food/recipes";
    private final String TEST_SECTION_NAME = "recipes";
    private final int TEST_SECTION_DEPTH = 2;
    private final String TEST_SECTION_PAGE_TYPE = "sni-food/components/pagetypes/section";

    private final String TEST_EXPECTED_SECTION = "recipes";
    private final String TEST_EXPECTED_SITE = "food";
    private final String TEST_EXPECTED_WEB_DELIVERY_CHANNEL = "Web";
    private final String TEST_EXPECTED_MOBILE_DELIVERY_CHANNEL = "Mobile";

    private Map<String, String> contentProps;

    @Mock PageManager pageManager; 
    @Mock Page contentPage;
    @Mock Resource contentCR;
    @Mock ValueMap contentProperties;

    @Mock Page parentPage;
    @Mock Resource parentCR;

    @Mock Page sectionPage;
    @Mock Resource sectionCR;
    @Mock ValueMap sectionVM;

    SniPage testPage;
    SniPage testNoAssetPage;
    MetadataProvider metadataProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        contentProps = new HashMap<String, String>();

        // PageManager
        when(contentPage.getPageManager()).thenReturn(pageManager);
        
        // content page properties
        when(contentPage.getTitle()).thenReturn(TEST_CONTENT_PAGE_TITLE);
        when(contentPage.getName()).thenReturn(TEST_NAME);
        when(contentPage.hasContent()).thenReturn(true);
        when(contentPage.getDepth()).thenReturn(TEST_CONTENT_DEPTH);
        when(contentPage.getPath()).thenReturn(TEST_CONTENT_PATH);
        when(contentPage.getProperties()).thenReturn(contentProperties);
        when(contentPage.getContentResource()).thenReturn(contentCR);
        when(contentPage.getPageManager()).thenReturn(pageManager);
        when(contentCR.getResourceType()).thenReturn(TEST_RESOURCE_TYPE);
        when(contentCR.adaptTo(ValueMap.class)).thenReturn(contentProperties);
        
        // parent page properties
        when(parentPage.getTitle()).thenReturn(TEST_PARENT_NAME);
        when(parentPage.getName()).thenReturn(TEST_PARENT_NAME);
        when(parentPage.hasContent()).thenReturn(true);
        when(parentPage.getDepth()).thenReturn(TEST_PARENT_DEPTH);
        when(parentPage.getPath()).thenReturn(TEST_PARENT_PATH);
        when(parentPage.getProperties()).thenReturn(ValueMap.EMPTY);
        when(parentPage.getContentResource()).thenReturn(parentCR);
        when(parentPage.getPageManager()).thenReturn(pageManager);
        when(parentCR.getResourceType()).thenReturn(TEST_PARENT_PAGE_TYPE);
        when(parentCR.adaptTo(ValueMap.class)).thenReturn(ValueMap.EMPTY);
        
        //section VM
        when(sectionVM.get(PagePropertyConstants.PROP_SNI_TITLE, String.class)).thenReturn(TEST_SECTION_NAME);
        
        // section page properties
        when(sectionPage.getTitle()).thenReturn(TEST_SECTION_NAME);
        when(sectionPage.getName()).thenReturn(TEST_SECTION_NAME);
        when(sectionPage.hasContent()).thenReturn(true);
        when(sectionPage.getDepth()).thenReturn(TEST_SECTION_DEPTH);
        when(sectionPage.getPath()).thenReturn(TEST_SECTION_PATH);
        when(sectionPage.getProperties()).thenReturn(sectionVM);
        when(sectionPage.getContentResource()).thenReturn(sectionCR);
        when(sectionPage.getPageManager()).thenReturn(pageManager);
        when(sectionCR.getResourceType()).thenReturn(TEST_SECTION_PAGE_TYPE);
        when(sectionCR.adaptTo(ValueMap.class)).thenReturn(sectionVM);

        when(contentPage.getParent()).thenReturn(parentPage);
        when(parentPage.getParent()).thenReturn(sectionPage);
        
        when(contentPage.getAbsoluteParent(eq(TEST_SECTION_DEPTH))).thenReturn(sectionPage);
    }

    @Test
    public void testHappyPath() {
        // all values set, returning correctly
        testPage = PageFactory.getSniPage(contentPage);
        metadataProvider = new BaseLocationMetadataProvider(testPage);
        String site = metadataProvider.getProperty(MetadataProperty.SITE);
        assertNotNull("site is nonnull", site);
        assertTrue("site has correct value", site.equals(TEST_EXPECTED_SITE));
        String section = metadataProvider.getProperty(MetadataProperty.SECTION);
        assertNotNull("section is nonnull", section);
        assertTrue("section has correct value", section.equals(TEST_EXPECTED_SECTION));
        section = metadataProvider.getProperty(MetadataProperty.CATEGORYDSPNAME);
        assertNotNull("section is nonnull", section);
        assertTrue("categorydspname has correct value", section.equals(TEST_EXPECTED_SECTION));
        section = metadataProvider.getProperty(MetadataProperty.SCTNDSPNAME);
        assertNotNull("section is nonnull", section);
        assertTrue("sectiondspname has correct value", section.equals(TEST_EXPECTED_SECTION));
        String classification = metadataProvider.getProperty(MetadataProperty.CLASSIFICATION);
        assertNotNull("classification is nonnull", classification);
        assertTrue("classification has correct value", classification.equals(section + ", " + site));
        String deliveryChannel = metadataProvider.getProperty(MetadataProperty.DELIVERYCHANNEL);
        assertNotNull("deliveryChannel is not null", deliveryChannel);
        assertTrue("deliveryChannel has correct value", deliveryChannel.equals(TEST_EXPECTED_WEB_DELIVERY_CHANNEL));
    }

    @Test
    public void testNoParent() {
        // page has no parent
        when(contentPage.getParent()).thenReturn(null);
        when(contentPage.getAbsoluteParent(eq(TEST_SECTION_DEPTH))).thenReturn(null);
        testPage = PageFactory.getSniPage(contentPage);
        metadataProvider = new BaseLocationMetadataProvider(testPage);
        String site = metadataProvider.getProperty(MetadataProperty.SITE);
        assertNotNull("site is nonnull", site);
        assertTrue("site has correct value", site.equals(TEST_EXPECTED_SITE));
        String section = metadataProvider.getProperty(MetadataProperty.SECTION);
        assertNotNull("section is nonnull", section);
        assertTrue("section has correct value", section.equals(site)); // if no section determined, use site
        section = metadataProvider.getProperty(MetadataProperty.CATEGORYDSPNAME);
        assertNotNull("section is nonnull", section); 
        assertTrue("categorydspname has correct value", section.equals(site)); // if no section, use site
        section = metadataProvider.getProperty(MetadataProperty.SCTNDSPNAME);
        assertNotNull("section is nonnull", section);
        assertTrue("sectiondspname has correct value", section.equals(site)); // if no section, use site
        String classification = metadataProvider.getProperty(MetadataProperty.CLASSIFICATION);
        assertNotNull("classification is nonnull", classification);
        assertTrue("classification has correct value", classification.equals(section + ", " + site));
        String deliveryChannel = metadataProvider.getProperty(MetadataProperty.DELIVERYCHANNEL);
        assertNotNull("deliveryChannel is not null", deliveryChannel);
        assertTrue("deliveryChannel has correct value", deliveryChannel.equals(TEST_EXPECTED_WEB_DELIVERY_CHANNEL));
    }

    @Test
    public void testNoSite() {
        // page has no path usable for site
        when(contentPage.getParent()).thenReturn(null);
        when(contentPage.getAbsoluteParent(eq(TEST_SECTION_DEPTH))).thenReturn(null);
        when(contentPage.getPath()).thenReturn("/content");
        when(contentPage.getDepth()).thenReturn(0);
        testPage = PageFactory.getSniPage(contentPage);
        metadataProvider = new BaseLocationMetadataProvider(testPage);
        String site = metadataProvider.getProperty(MetadataProperty.SITE);
        assertNotNull("site is nonnull", site);
        assertTrue("site has correct value", site.equals(BaseLocationMetadataProvider.DEFAULT_SITE)); // default value
        String section = metadataProvider.getProperty(MetadataProperty.SECTION);
        assertNotNull("section is nonnull", section);
        assertTrue("section has correct value", section.equals(site)); // if no section determined, use site
        section = metadataProvider.getProperty(MetadataProperty.CATEGORYDSPNAME);
        assertNotNull("categorydspname is nonnull", section); 
        assertTrue("categorydspname has correct value", section.equals(site)); // if no section, use site
        section = metadataProvider.getProperty(MetadataProperty.SCTNDSPNAME);
        assertNotNull("sectiondspname is nonnull", section);
        assertTrue("sectiondspname has correct value", section.equals(site)); // if no section, use site
        String classification = metadataProvider.getProperty(MetadataProperty.CLASSIFICATION);
        assertNotNull("classification is nonnull", classification);
        assertTrue("classification has correct value", classification.equals(section + ", " + site));
        String deliveryChannel = metadataProvider.getProperty(MetadataProperty.DELIVERYCHANNEL);
        assertNotNull("deliveryChannel is not null", deliveryChannel);
        assertTrue("deliveryChannel has correct value", deliveryChannel.equals(TEST_EXPECTED_WEB_DELIVERY_CHANNEL)); // will default to web
    }
 
}
