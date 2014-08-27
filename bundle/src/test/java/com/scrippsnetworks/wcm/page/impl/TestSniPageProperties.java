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
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.util.PagePropertyNames;
import com.scrippsnetworks.wcm.page.impl.MockPage.MockPageFactory;
import com.scrippsnetworks.wcm.resource.MockResource.MockResourceFactory;

/** Tests non-merging behavior of SniPage */
public class TestSniPageProperties {

    private final String TEST_UID = "8658d12e-214d-4a60-904d-5527d74c585f";
    private final String TEST_RESOURCE_TYPE = "sni-food/components/pagetypes/recipe";
    private final String TEST_ASSET_PATH = "/etc/sni-asset/foo/dee/bar";
    private final String TEST_CONTENT_PATH = "/content/food/foo/dee/bar";
    private final String TEST_SITE_PATH = "/content/food";
    private final String EXPECTED_PAGE_TYPE = "recipe";
    private final String EXPECTED_BRAND = "food";
    private final MockPageFactory pageFactory = new MockPageFactory();
    private final MockResourceFactory resourceFactory = new MockResourceFactory();
    
    Page contentPage;
    Page assetPage;
    Page siteLevelPage;
    
    SniPage testPage;
    SniPage testNoAssetPage;

    @Before
    public void setUp() {
        Map<String, Object> contentProperties = new HashMap<String, Object>();
        contentProperties.put(PagePropertyNames.SNI_ASSET_LINK.propertyName(), TEST_ASSET_PATH);
        contentProperties.put(PagePropertyNames.SNI_ASSET_UID.propertyName(), TEST_UID);
        contentProperties.put(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName(), TEST_RESOURCE_TYPE);

        Map<String, Object> assetProperties = new HashMap<String, Object>();
        
        assetPage = pageFactory.withPath(TEST_ASSET_PATH).withProperties(assetProperties).build();
        contentPage = pageFactory.withPath(TEST_CONTENT_PATH).withProperties(contentProperties).reliesOnPage(assetPage).build();

        try {
            testPage = PageFactory.getSniPage(contentPage);
        } catch (Exception e) {
            System.out.println("exception creating page " + e.getClass().getName() + " " + e.getMessage());
        }
        
    }

    @Test
    public void testProperties() {
        // Since current implementation uses DataUtil, we're only testing plumbing works.
        assertEquals("page type has expected value", EXPECTED_PAGE_TYPE, testPage.getPageType());
        assertEquals("page uid has expected value", TEST_UID, testPage.getUid());
        assertEquals("page brand has expected value", EXPECTED_BRAND, testPage.getBrand());
    }
}
