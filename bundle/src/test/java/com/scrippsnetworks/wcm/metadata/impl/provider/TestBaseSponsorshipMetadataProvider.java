package com.scrippsnetworks.wcm.metadata.impl.provider;

import java.util.Map;
import java.util.HashMap;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.metadata.MetadataProperty;
import com.scrippsnetworks.wcm.metadata.MetadataProvider;
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

public class TestBaseSponsorshipMetadataProvider {

    private final String TEST_TITLE = "A Title";
    private final String TEST_NAME = "test-recipe";
    private final String TEST_RESOURCE_TYPE = "sni-wcm/test/resource";
    private final String TEST_CONTENT_PATH = "/content/food/recipes/no-chef/t/te/tes/test/test-recipe";
    private final int TEST_CONTENT_DEPTH = 8;
    private final String TEST_PAGE_NUMBER = "1";
    private final String TEST_ASSET_UID = "b9dbee4b-78b0-4bb0-b391-046b8436d4cc";

    private final String TEST_SPONSORSHIP_TITLE = "CONTENT-SPONSOR";
    private final String TEST_SPONSORSHIP_PATH = "/content/food/sponsorships/c/content-sponsor";
    private final String TEST_SPONSORSHIP_RESOURCE_TYPE = "sni-wcm/test/sponsorship";
    private final int TEST_SPONSORSHIP_DEPTH = 4;

    private Map<String, String> contentProps;
    private Map<String, String> sponsorshipProps;

    @Mock PageManager pageManager; 
    @Mock Page contentPage;
    @Mock Resource contentCR;
    @Mock ValueMap contentProperties;
    @Mock Page sponsorshipPage;
    @Mock Resource sponsorshipCR;
    @Mock ValueMap sponsorshipProperties;

    SniPage testPage;
    MetadataProvider metadataProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        contentProps = new HashMap<String, String>();

        contentProps.put(PROP_SNI_ASSETUID, TEST_ASSET_UID);
        contentProps.put(PROP_SNI_SPONSORSHIP, TEST_SPONSORSHIP_PATH);
        contentProps.put(PROP_JCR_TITLE, TEST_TITLE);

        // PageManager
        when(contentPage.getPageManager()).thenReturn(pageManager);
        when(pageManager.getPage(TEST_SPONSORSHIP_PATH)).thenReturn(sponsorshipPage);

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

        when(sponsorshipPage.getTitle()).thenReturn(TEST_SPONSORSHIP_TITLE);
        when(sponsorshipPage.getName()).thenReturn(TEST_SPONSORSHIP_PATH.substring(TEST_SPONSORSHIP_PATH.lastIndexOf('/')+1));
        when(sponsorshipPage.hasContent()).thenReturn(true);
        when(sponsorshipPage.getDepth()).thenReturn(TEST_SPONSORSHIP_DEPTH);
        when(sponsorshipPage.getPath()).thenReturn(TEST_SPONSORSHIP_PATH);
        when(sponsorshipPage.getProperties()).thenReturn(ValueMap.EMPTY);
        when(sponsorshipPage.getContentResource()).thenReturn(sponsorshipCR);
        when(sponsorshipPage.isValid()).thenReturn(true); // important!
        when(sponsorshipCR.getResourceType()).thenReturn(TEST_SPONSORSHIP_RESOURCE_TYPE);
        when(sponsorshipCR.adaptTo(ValueMap.class)).thenReturn(ValueMap.EMPTY);

    }

    @Test
    public void testHappyPath() {
        // Simple case with sponsorship set directly on the page.
        // Since sponsorship behavior is encapsulated in SponsorshipManager, more complicated cases
        // should be tested there I'd think.
        testPage = PageFactory.getSniPage(contentPage);
        metadataProvider = new BaseSponsorshipMetadataProvider(testPage);
        String sponsorship = metadataProvider.getProperty(MetadataProperty.SPONSORSHIP);
        assertNotNull("sponsorship is nonnull", sponsorship);
        String hubSponsor = metadataProvider.getProperty(MetadataProperty.HUBSPONSOR);
        assertTrue("hub sponsor is null or empty", hubSponsor == null || hubSponsor.equals(""));
    }

    @Test
    public void testNoSponsorshipProperty() {
        when(contentProperties.get(PagePropertyConstants.PROP_SNI_SPONSORSHIP, String.class)).thenReturn(null); // 
        testPage = PageFactory.getSniPage(contentPage);
        metadataProvider = new BaseSponsorshipMetadataProvider(testPage);
        String sponsorship = metadataProvider.getProperty(MetadataProperty.SPONSORSHIP);
        assertTrue("sponsorship is null or empty", sponsorship == null || sponsorship.equals(""));
        String hubSponsor = metadataProvider.getProperty(MetadataProperty.HUBSPONSOR);
        assertTrue("hub sponsor is null or empty", hubSponsor == null || hubSponsor.equals(""));
    }

}
