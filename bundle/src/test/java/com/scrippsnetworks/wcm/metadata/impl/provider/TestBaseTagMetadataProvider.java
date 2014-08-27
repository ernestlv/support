package com.scrippsnetworks.wcm.metadata.impl.provider;

import java.util.Map;
import java.util.HashMap;

import com.scrippsnetworks.wcm.metadata.SponsorshipManager;
import com.scrippsnetworks.wcm.metadata.SponsorshipProvider;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.metadata.MetadataProperty;
import com.scrippsnetworks.wcm.metadata.MetadataProvider;
import com.scrippsnetworks.wcm.metadata.impl.MetadataUtil;
import com.day.cq.wcm.api.Page; 
import com.day.cq.wcm.api.PageManager;
import com.day.cq.tagging.TagConstants;
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

public class TestBaseTagMetadataProvider {

    private final String TEST_PROP_ADKEY =  "food-adkeys:tv/show";
    private final String TEST_PROP_PRIMARYTAG = "food-tags:main-ingredient/shellfish/lobster";
    private final String TEST_PROP_SECONDARYTAG = "food-tags:dish/roll";
    private final String TEST_PROP_SOURCE = "cook-sources:food";
    private final String TEST_EXPECTED_ADKEY1 = "tv";
    private final String TEST_EXPECTED_ADKEY2 = "show";
    private final String TEST_EXPECTED_CONTENTTAG1 = "lobster";
    private final String TEST_EXPECTED_CONTENTTAG2 = "roll";
    private final String TEST_EXPECTED_SOURCE = "food";


    @Mock PageManager pageManager; 
    @Mock Page contentPage;
    @Mock Resource contentCR;
    @Mock ValueMap contentProperties;
    @Mock Page assetPage;
    @Mock Resource assetCR;
    @Mock ValueMap assetProperties;

    @Mock SniPage sniPage;
    @Mock ValueMap sniPageProperties;
    @Mock SponsorshipManager sponsorshipManager;
    @Mock SponsorshipProvider sponsorshipProvider;
    @Mock SniPage providerPage;
    @Mock ValueMap providerPageProperties;

    SniPage testPage;
    MetadataProvider metadataProvider;

    private Map<String, String> contentProps;
    private Map<String, String> assetProps;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        // PageManager
        // when(sniPage.getPageManager()).thenReturn(pageManager);

        when(sniPage.getProperties()).thenReturn(sniPageProperties);
        when(sniPageProperties.get(PROP_SNI_PRIMARYTAG, String.class)).thenReturn(TEST_PROP_PRIMARYTAG);
        when(sniPageProperties.get(PROP_SNI_SECONDARYTAG, String.class)).thenReturn(TEST_PROP_SECONDARYTAG);
        when(sniPageProperties.get(PROP_SNI_SOURCE, String.class)).thenReturn(TEST_PROP_SOURCE);
        when(sniPageProperties.get(PROP_SNI_ADKEY, String.class)).thenReturn(TEST_PROP_ADKEY);
        when(sniPageProperties.get(eq(TagConstants.PN_TAGS), any(String[].class))).thenReturn(new String[0]);
    }

    @Test
    public void testHappyPath() {
        // all values set, returning correctly
        metadataProvider = new BaseTagMetadataProvider(sniPage);

        String adkey1 = metadataProvider.getProperty(MetadataProperty.ADKEY1);
        assertNotNull("adkey1 is nonnull", adkey1);
        assertEquals("adkey1 has correct value", TEST_EXPECTED_ADKEY1, adkey1);
        String adkey2 = metadataProvider.getProperty(MetadataProperty.ADKEY2);
        assertNotNull("adkey2 is nonnull", adkey2);
        assertEquals("adkey2 has correct value", TEST_EXPECTED_ADKEY2, adkey2);
        String primaryTag = metadataProvider.getProperty(MetadataProperty.CONTENTTAG1);
        assertNotNull("primary tag is nonnull", primaryTag);
        assertEquals("primary tag has correct value", TEST_EXPECTED_CONTENTTAG1, primaryTag);
        String secondaryTag = metadataProvider.getProperty(MetadataProperty.CONTENTTAG2);
        assertNotNull("secondary tag is nonnull", secondaryTag);
        assertEquals("secondary tag has correct value", TEST_EXPECTED_CONTENTTAG2, secondaryTag);
        String source = metadataProvider.getProperty(MetadataProperty.SOURCE);
        assertNotNull("source nonnull", source);
        assertEquals("source has correct value", TEST_EXPECTED_SOURCE, source);
    }

    @Test
    public void testInheritedAdkey() {
        when(sniPage.getSponsorshipManager()).thenReturn(sponsorshipManager);
        when(sniPage.getProperties()).thenReturn(ValueMap.EMPTY);
        when(sponsorshipManager.getEffectiveSponsorshipProvider()).thenReturn(sponsorshipProvider);
        when(sponsorshipProvider.getProvider()).thenReturn(providerPage);
        when(providerPage.getProperties()).thenReturn(providerPageProperties);
        when(providerPageProperties.get(PROP_SNI_ADKEY, String.class)).thenReturn(TEST_PROP_ADKEY);

        metadataProvider = new BaseTagMetadataProvider(sniPage);

        String adkey1 = metadataProvider.getProperty(MetadataProperty.ADKEY1);
        assertNotNull("adkey1 is nonnull", adkey1);
        assertEquals("adkey1 has correct value", TEST_EXPECTED_ADKEY1, adkey1);
        String adkey2 = metadataProvider.getProperty(MetadataProperty.ADKEY2);
        assertNotNull("adkey2 is nonnull", adkey2);
        assertEquals("adkey2 has correct value", TEST_EXPECTED_ADKEY2, adkey2);
    }

    @Test
    public void testNullProperties() {
        // no values set, returning correctly
        when(sniPageProperties.get(PROP_SNI_PRIMARYTAG, String.class)).thenReturn(null);
        when(sniPageProperties.get(PROP_SNI_SECONDARYTAG, String.class)).thenReturn(null);
        when(sniPageProperties.get(PROP_SNI_ADKEY, String.class)).thenReturn(null);
        when(sniPageProperties.get(PROP_SNI_SOURCE, String.class)).thenReturn(null);

        metadataProvider = new BaseTagMetadataProvider(sniPage);

        String adkey1 = metadataProvider.getProperty(MetadataProperty.ADKEY1);
        assertNull("adkey1 is null", adkey1);
        String adkey2 = metadataProvider.getProperty(MetadataProperty.ADKEY2);
        assertNull("adkey2 is null", adkey2);
        String primaryTag = metadataProvider.getProperty(MetadataProperty.CONTENTTAG1);
        assertNull("primary tag is null", primaryTag);
        String secondaryTag = metadataProvider.getProperty(MetadataProperty.CONTENTTAG2);
        assertNull("secondary tag is null", secondaryTag);
        String source = metadataProvider.getProperty(MetadataProperty.SOURCE);
        assertNull("source  is null", source);
    }

    @Test
    public void testEmptyProperties() {
        // no values set, returning correctly
        when(sniPageProperties.get(PROP_SNI_PRIMARYTAG, String.class)).thenReturn("");
        when(sniPageProperties.get(PROP_SNI_SECONDARYTAG, String.class)).thenReturn("");
        when(sniPageProperties.get(PROP_SNI_ADKEY, String.class)).thenReturn("");
        when(sniPageProperties.get(PROP_SNI_SOURCE, String.class)).thenReturn("");

        metadataProvider = new BaseTagMetadataProvider(sniPage);

        String adkey1 = metadataProvider.getProperty(MetadataProperty.ADKEY1);
        assertNull("adkey1 is null", adkey1);
        String adkey2 = metadataProvider.getProperty(MetadataProperty.ADKEY2);
        assertNull("adkey2 is null", adkey2);
        String primaryTag = metadataProvider.getProperty(MetadataProperty.CONTENTTAG1);
        assertNull("primary tag is null", primaryTag);
        String secondaryTag = metadataProvider.getProperty(MetadataProperty.CONTENTTAG2);
        assertNull("secondary tag is null", secondaryTag);
        String source = metadataProvider.getProperty(MetadataProperty.SOURCE);
        assertNull("source  is null", source);
    }
}
