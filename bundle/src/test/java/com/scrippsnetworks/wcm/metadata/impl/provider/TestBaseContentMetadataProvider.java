package com.scrippsnetworks.wcm.metadata.impl.provider;

import java.util.Map;
import java.util.HashMap;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.metadata.MetadataProperty;
import com.scrippsnetworks.wcm.metadata.MetadataProvider;
import com.scrippsnetworks.wcm.metadata.impl.MetadataUtil;
import com.day.cq.wcm.api.Page; 
import com.day.cq.wcm.api.PageManager; 
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.resource.Resource;

import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import static com.scrippsnetworks.wcm.page.PagePropertyConstants.*;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.apache.sling.commons.testing.sling.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestBaseContentMetadataProvider {

    private Map<String, String> contentProps;
    private final String TEST_RESOURCE_TYPE = "sni-wcm/test/resource";
    private final String TEST_CONTENT_PATH = "/content/food/recipes/no-chef/t/te/tes/test/test-recipe";
    private final String TEST_NAME = "test-recipe";
    private final int TEST_CONTENT_DEPTH = 8;
    private final String TEST_PAGE_NUMBER = "1";
    private final String TEST_ASSET_UID = "b9dbee4b-78b0-4bb0-b391-046b8436d4cc";
    private final String TEST_TITLE = "A Title";

    @Mock PageManager pageManager; 
    @Mock Page contentPage;
    @Mock Resource contentCR;
    @Mock ValueMap contentProperties;

    SniPage testPage;
    MetadataProvider metadataProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        contentProps = new HashMap<String, String>();

        contentProps.put(PROP_SNI_ASSETUID, TEST_ASSET_UID);
        contentProps.put(PROP_JCR_TITLE, TEST_TITLE);

        // PageManager
        when(contentPage.getPageManager()).thenReturn(pageManager);

        // content page properties
        when(contentPage.getTitle()).thenReturn(TEST_TITLE);
        when(contentPage.getName()).thenReturn(TEST_NAME);
        when(contentPage.hasContent()).thenReturn(true);
        when(contentPage.getDepth()).thenReturn(TEST_CONTENT_DEPTH);
        when(contentPage.getPath()).thenReturn(TEST_CONTENT_PATH);
        for (Map.Entry<String, String> entry : contentProps.entrySet()) {
            if (entry.getValue().contains(",")) {
                String[] values = entry.getValue().split(", ");
                when(contentProperties.get(entry.getKey(), String[].class)).thenReturn(values);
            } else {
                when(contentProperties.get(entry.getKey(), String.class)).thenReturn(entry.getValue());
            }
        }
        when(contentPage.getProperties()).thenReturn(contentProperties);
        when(contentPage.getContentResource()).thenReturn(contentCR);
        when(contentCR.getResourceType()).thenReturn(TEST_RESOURCE_TYPE);
        when(contentCR.adaptTo(ValueMap.class)).thenReturn(contentProperties);
    }

    @Test
    public void testHappyPath() {
        // all values set, returning correctly
        testPage = PageFactory.getSniPage(contentPage);
        metadataProvider = new BaseContentMetadataProvider(testPage);
        String title = metadataProvider.getProperty(MetadataProperty.TITLE);
        assertNotNull("title is nonnull", title);
        assertTrue("title has correct value", title.equals(TEST_TITLE));
        String detailId = metadataProvider.getProperty(MetadataProperty.DETAILID);
        assertNotNull("detailId is nonnull", detailId);
        assertTrue("detailId has correct value", detailId.equals(TEST_ASSET_UID));
        String type = metadataProvider.getProperty(MetadataProperty.TYPE);
        assertNotNull("type is nonnull", type);
        String[] typeArr = TEST_RESOURCE_TYPE.split("/");
        assertTrue("type has correct value", type.equals(typeArr[typeArr.length - 1]));
        String uniqueId = metadataProvider.getProperty(MetadataProperty.UNIQUEID);
        assertNotNull("uniqueId is not null", uniqueId);
        assertTrue("uniqueId has correct value", uniqueId.equals(MetadataUtil.getSiteName(TEST_CONTENT_PATH) +"|"+ type +"|"+ detailId +"|"+ TEST_PAGE_NUMBER));
    }

    @Test
    public void testNoDetailId() {
        // With no asset id, detail id should return the default defined in the provider
        when(contentProperties.get(PagePropertyConstants.PROP_SNI_ASSETUID, String.class)).thenReturn(null);
        testPage = PageFactory.getSniPage(contentPage);
        metadataProvider = new BaseContentMetadataProvider(testPage);
        String title = metadataProvider.getProperty(MetadataProperty.TITLE);
        assertNotNull("title is nonnull", title);
        assertTrue("title has correct value", title.equals(TEST_TITLE));
        String detailId = metadataProvider.getProperty(MetadataProperty.DETAILID);
        assertNotNull("detailId is nonnull", detailId);
        assertTrue("detailId has correct value", detailId.equals(BaseContentMetadataProvider.DEFAULT_DETAIL_ID)); // this is the case
        String type = metadataProvider.getProperty(MetadataProperty.TYPE);
        assertNotNull("type is nonnull", type);
        String[] typeArr = TEST_RESOURCE_TYPE.split("/");
        assertTrue("type has correct value", type.equals(typeArr[typeArr.length - 1]));
        String uniqueId = metadataProvider.getProperty(MetadataProperty.UNIQUEID);
        assertNotNull("uniqueId is not null", uniqueId);
        assertTrue("uniqueId has correct value", uniqueId.equals(MetadataUtil.getSiteName(TEST_CONTENT_PATH) +"|"+ type +"|"+ detailId +"|"+ TEST_PAGE_NUMBER));
    }

    /*@Test
    public void testNoTitle() {
        // With a null title, the title should fall back to the name of the page node
        when(contentPage.getTitle()).thenReturn(null);
        testPage = PageFactory.getSniPage(contentPage);
        metadataProvider = new BaseContentMetadataProvider(testPage);
        String title = metadataProvider.getProperty(MetadataProperty.TITLE);
        assertNotNull("title is nonnull", title);
        assertTrue("title has correct value", title.equals(TEST_NAME)); // this is the case
        String detailId = metadataProvider.getProperty(MetadataProperty.DETAILID);
        assertNotNull("detailId is nonnull", detailId);
        assertTrue("detailId has correct value", detailId.equals(TEST_ASSET_UID));
        String type = metadataProvider.getProperty(MetadataProperty.TYPE);
        assertNotNull("type is nonnull", type);
        String[] typeArr = TEST_RESOURCE_TYPE.split("/");
        assertTrue("type has correct value", type.equals(typeArr[typeArr.length - 1]));
        String uniqueId = metadataProvider.getProperty(MetadataProperty.UNIQUEID);
        assertNotNull("uniqueId is not null", uniqueId);
        assertTrue("uniqueId has correct value", uniqueId.equals(MetadataUtil.getSiteName(TEST_CONTENT_PATH) +"|"+ type +"|"+ detailId +"|"+ TEST_PAGE_NUMBER));
    }

    @Test
    public void testEmptyTitle() {
        // With an empty title, the title should fall back to the name of the page node
        when(contentPage.getTitle()).thenReturn("");
        testPage = PageFactory.getSniPage(contentPage);
        metadataProvider = new BaseContentMetadataProvider(testPage);
        String title = metadataProvider.getProperty(MetadataProperty.TITLE);
        assertNotNull("title is nonnull", title);
        assertTrue("title has correct value", title.equals(TEST_NAME)); // this is the case
        String detailId = metadataProvider.getProperty(MetadataProperty.DETAILID);
        assertNotNull("detailId is nonnull", detailId);
        assertTrue("detailId has correct value", detailId.equals(TEST_ASSET_UID));
        String type = metadataProvider.getProperty(MetadataProperty.TYPE);
        assertNotNull("type is nonnull", type);
        String[] typeArr = TEST_RESOURCE_TYPE.split("/");
        assertTrue("type has correct value", type.equals(typeArr[typeArr.length - 1]));
        String uniqueId = metadataProvider.getProperty(MetadataProperty.UNIQUEID);
        assertNotNull("uniqueId is not null", uniqueId);
        assertTrue("uniqueId has correct value", uniqueId.equals(MetadataUtil.getSiteName(TEST_CONTENT_PATH) +"|"+ type +"|"+ detailId +"|"+ TEST_PAGE_NUMBER));
    }*/

    @Test
    public void testNoResourceType() {
        // with a null resource type, the type should fall back to the default defined in the provider (should be "page")
        when(contentCR.getResourceType()).thenReturn(null);
        testPage = PageFactory.getSniPage(contentPage);
        metadataProvider = new BaseContentMetadataProvider(testPage);
        String title = metadataProvider.getProperty(MetadataProperty.TITLE);
        assertNotNull("title is nonnull", title);
        assertTrue("title has correct value", title.equals(TEST_TITLE));
        String detailId = metadataProvider.getProperty(MetadataProperty.DETAILID);
        assertNotNull("detailId is nonnull", detailId);
        assertTrue("detailId has correct value", detailId.equals(TEST_ASSET_UID));
        String type = metadataProvider.getProperty(MetadataProperty.TYPE);
        assertNotNull("type is nonnull", type);
        String[] typeArr = TEST_RESOURCE_TYPE.split("/");
        assertTrue("type has correct value", type.equals(BaseContentMetadataProvider.DEFAULT_PAGE_TYPE)); // this is the case
        String uniqueId = metadataProvider.getProperty(MetadataProperty.UNIQUEID);
        assertNotNull("uniqueId is not null", uniqueId);
        assertTrue("uniqueId has correct value", uniqueId.equals(MetadataUtil.getSiteName(TEST_CONTENT_PATH) +"|"+ type +"|"+ detailId +"|"+ TEST_PAGE_NUMBER));
    }

    @Test
    public void testEmptyResourceType() {
        // with an empty resource type, the type should fall back to the default defined in the provider (should be "page")
        when(contentCR.getResourceType()).thenReturn(""); // if this really happened, the page wouldn't render anyway
        testPage = PageFactory.getSniPage(contentPage);
        metadataProvider = new BaseContentMetadataProvider(testPage);
        String title = metadataProvider.getProperty(MetadataProperty.TITLE);
        assertNotNull("title is nonnull", title);
        assertTrue("title has correct value", title.equals(TEST_TITLE));
        String detailId = metadataProvider.getProperty(MetadataProperty.DETAILID);
        assertNotNull("detailId is nonnull", detailId);
        assertTrue("detailId has correct value", detailId.equals(TEST_ASSET_UID));
        String type = metadataProvider.getProperty(MetadataProperty.TYPE);
        assertNotNull("type is nonnull", type);
        String[] typeArr = TEST_RESOURCE_TYPE.split("/");
        assertTrue("type has correct value", type.equals(BaseContentMetadataProvider.DEFAULT_PAGE_TYPE)); // this is the case
        String uniqueId = metadataProvider.getProperty(MetadataProperty.UNIQUEID);
        assertNotNull("uniqueId is not null", uniqueId);
        assertTrue("uniqueId has correct value", uniqueId.equals(MetadataUtil.getSiteName(TEST_CONTENT_PATH) +"|"+ type +"|"+ detailId +"|"+ TEST_PAGE_NUMBER));
    }

}
