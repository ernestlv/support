package com.scrippsnetworks.wcm.page.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.page.impl.MockPage.MockPageFactory;
import com.scrippsnetworks.wcm.resource.MockResource.MockResourceFactory;

public class TestSniPageImpl {

    private final String testAssetPath = "/path/to/test/asset";
    private final String sharedProp = "sni:testProp1";
    private final String assetOnlyProp = "sni:testProp2";
    private final String contentOnlyProp = "sni:testProp3";
    private final String childRelPath = "path/to/child";
    private final String CONTENT = "content";
    private final String ASSET = "asset";

    private final MockPageFactory pageFactory = new MockPageFactory();
    private final MockResourceFactory resourceFactory = new MockResourceFactory();
    
    Page contentPage;
    Page assetPage;
    Page noAssetContentPage;
    Resource contentChild;
    Resource assetChild;
    
    SniPage testPage;
    SniPage testNoAssetPage;

    @Before
    public void setUp() {
        Map<String, Object> contentProperties = new HashMap<String, Object>();
        contentProperties.put(PagePropertyConstants.PROP_SNI_ASSETLINK, testAssetPath);
        contentProperties.put(sharedProp, CONTENT);
        contentProperties.put(contentOnlyProp, CONTENT);
        
        Map<String, Object> assetProperties = new HashMap<String, Object>();
        assetProperties.put(sharedProp, ASSET);
        assetProperties.put(assetOnlyProp, ASSET);
        
        assetChild = resourceFactory.withPath(childRelPath).withProperties(assetProperties).build();
        contentChild = resourceFactory.withPath(childRelPath).withProperties(contentProperties).build();
        
        assetPage = pageFactory.withPath(testAssetPath).withProperties(assetProperties).reliesOnChildResource(assetChild).build();
        testPage = pageFactory.withPath(testAssetPath).withProperties(contentProperties).reliesOnPage(assetPage).reliesOnChildResource(contentChild).build();

        Map<String, Object> noAssetContentPageProperties = new HashMap<String, Object>();
        noAssetContentPageProperties.put(PagePropertyConstants.PROP_SNI_ASSETLINK, null);
        noAssetContentPageProperties.put(sharedProp, CONTENT);
        noAssetContentPageProperties.put(contentOnlyProp, CONTENT);
        testNoAssetPage = pageFactory.withPath("/").withProperties(noAssetContentPageProperties).build();
        
    }

    @Test
    public void testTest() {
        String assetPath = testPage.getProperties().get(PagePropertyConstants.PROP_SNI_ASSETLINK, String.class);
        String assetPagePath = testPage.getPageManager().getPage(testAssetPath).getPath();
        assertEquals("asset page is set up correctly", assetPath, assetPagePath);
    }

    @Test
    public void testMerge() {
        assertEquals("prop on content and asset comes from content", CONTENT, testPage.getProperties().get(sharedProp, String.class));
        assertEquals("prop only on asset comes from asset", ASSET, testPage.getProperties().get(assetOnlyProp, String.class));
        assertEquals("prop only on content comes from content", CONTENT, testPage.getProperties().get(contentOnlyProp, String.class));
    }

    @Test
    public void testResourceMerge() {
        Resource cr = testPage.getContentResource();
        ValueMap props = cr.adaptTo(ValueMap.class);
        assertEquals("prop on content and asset comes from content", CONTENT, props.get(sharedProp, String.class));
        assertEquals("prop only on asset comes from asset", ASSET, props.get(assetOnlyProp, String.class));
        assertEquals("prop only on content comes from content", CONTENT, props.get(contentOnlyProp, String.class));
    }

    @Test
    public void testRelPathMerge() {
        assertEquals("prop on content and asset comes from content", CONTENT, testPage.getProperties(childRelPath).get(sharedProp, String.class));
        assertEquals("prop only on asset comes from asset", ASSET, testPage.getProperties(childRelPath).get(assetOnlyProp, String.class));
        assertEquals("prop only on content comes from content", CONTENT, testPage.getProperties(childRelPath).get(contentOnlyProp, String.class));
    }

    @Test
    public void testRelPathResourceMerge() {
        Resource cr = testPage.getContentResource(childRelPath);
        ValueMap props = cr.adaptTo(ValueMap.class);
        assertEquals("prop on content and asset comes from content", CONTENT, props.get(sharedProp, String.class));
        assertEquals("prop only on asset comes from asset", ASSET, props.get(assetOnlyProp, String.class));
        assertEquals("prop only on content comes from content", CONTENT, props.get(contentOnlyProp, String.class));
    }

    @Test
    public void testNoAssetPage() {
        assertNull("prop not on content is null", testNoAssetPage.getProperties().get(assetOnlyProp, String.class));
        assertEquals("prop only on content comes from content", CONTENT, testNoAssetPage.getProperties().get(contentOnlyProp, String.class));
       // assertEquals("content resource is underlying content page's", noAssetContentCR, testNoAssetPage.getContentResource());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullConstructor() {
        Page nullPage = null;
        SniPage boom = new SniPageImpl(nullPage);
    }
}
