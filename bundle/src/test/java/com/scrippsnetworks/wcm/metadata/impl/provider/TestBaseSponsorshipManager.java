package com.scrippsnetworks.wcm.metadata.impl.provider;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.metadata.SponsorshipManager;
import com.scrippsnetworks.wcm.metadata.SponsorshipProvider;
import com.scrippsnetworks.wcm.metadata.SponsorshipSource;
import com.day.cq.wcm.api.Page; 
import com.day.cq.wcm.api.PageManager; 
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import static com.scrippsnetworks.wcm.page.PagePropertyConstants.*;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TestBaseSponsorshipManager {

    private Map<String, String> contentProps;
    private final String TEST_RESOURCE_TYPE = "sni-wcm/test/resource";
    private final String TEST_CONTENT_PATH = "/content/food/recipes/no-chef/t/te/tes/test/test-recipe/reviews";
    private final String TEST_NAME = "test-recipe";
    private final int TEST_CONTENT_DEPTH = 8;
    private final String TEST_PAGE_NUMBER = "1";
    private final String TEST_ASSET_UID = "b9dbee4b-78b0-4bb0-b391-046b8436d4cc";
    private final String TEST_TITLE = "A Title";

    private final String TEST_HUB_MASTER_PATH = "/content/food/recipes/no-chef/t/te/tes/test/test-recipe";
    private final String TEST_PACKAGE_ANCHOR_PATH = "/content/food/shows/a/a-show";
    private final String TEST_HUB_PACKAGE_ANCHOR_PATH = "/content/food/shows/articles/an-article";

    private final String TEST_PAGE_SPONSOR_PATH = "/content/food/sponsorships/page-sponsor";
    private final String TEST_HUB_MASTER_SPONSOR_PATH = "/content/food/sponsorships/hub-sponsor";
    private final String TEST_PACKAGE_SPONSOR_PATH = "/content/food/sponsorships/package-sponsor";
    private final String TEST_HUB_PACKAGE_SPONSOR_PATH = "/content/food/sponsorships/hubpackage-sponsor";

    @Mock PageManager pageManager; 
    @Mock ResourceResolver resourceResolver;
    @Mock Page contentPage;
    @Mock Resource contentPageResource;
    @Mock Resource hubResource;
    @Mock ValueMap hubResources;
    @Mock Resource contentCR;
    @Mock ValueMap contentProperties;

    @Mock Hub hub;
    @Mock ValueMap hubProperties;
    @Mock Resource hubChildResource1;
    @Mock ValueMap hubChildResource1Properties;
    @Mock Resource hubChildResource2;
    @Mock ValueMap hubChildResource2Properties;

    @Mock Page hubMasterPage;
    @Mock Resource hubMasterCR;
    @Mock ValueMap hubMasterProperties;
    @Mock Page packageAnchorPage;
    @Mock Resource packageAnchorCR;
    @Mock ValueMap packageAnchorProperties;
    @Mock Page hubPackageAnchorPage;
    @Mock Resource hubPackageAnchorCR;
    @Mock ValueMap hubPackageAnchorProperties;

    @Mock Page pageSponsorPage;
    @Mock Page hubSponsorPage;
    @Mock Page packageSponsorPage;
    @Mock Page hubPackageSponsorPage;

    SniPage testPage;
    SponsorshipManager sponsorshipManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        contentProps = new HashMap<String, String>();

        contentProps.put(PROP_SNI_ASSETUID, TEST_ASSET_UID);
        contentProps.put(PROP_SNI_SPONSORSHIP, TEST_PAGE_SPONSOR_PATH);
        contentProps.put(PROP_SNI_PACKAGE, TEST_PACKAGE_ANCHOR_PATH);
        contentProps.put(PROP_JCR_TITLE, TEST_TITLE);

        // PageManager
        when(contentPage.getPageManager()).thenReturn(pageManager);
        when(hubMasterPage.getPageManager()).thenReturn(pageManager);
        when(packageAnchorPage.getPageManager()).thenReturn(pageManager);
        when(hubPackageAnchorPage.getPageManager()).thenReturn(pageManager);
        when(pageManager.getPage(TEST_HUB_MASTER_PATH)).thenReturn(hubMasterPage);
        when(pageManager.getPage(TEST_PACKAGE_ANCHOR_PATH)).thenReturn(packageAnchorPage);
        when(pageManager.getPage(TEST_HUB_PACKAGE_ANCHOR_PATH)).thenReturn(hubPackageAnchorPage);
        when(pageManager.getPage(TEST_PAGE_SPONSOR_PATH)).thenReturn(pageSponsorPage);
        when(pageManager.getPage(TEST_HUB_MASTER_SPONSOR_PATH)).thenReturn(hubSponsorPage);
        when(pageManager.getPage(TEST_PACKAGE_SPONSOR_PATH)).thenReturn(packageSponsorPage);
        when(pageManager.getPage(TEST_HUB_PACKAGE_SPONSOR_PATH)).thenReturn(hubPackageSponsorPage);

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

        when(hubMasterPage.getPath()).thenReturn(TEST_HUB_MASTER_PATH);
        when(hubMasterPage.getProperties()).thenReturn(hubMasterProperties);
        when(hubMasterPage.getContentResource()).thenReturn(hubMasterCR);
        when(hubMasterCR.adaptTo(ValueMap.class)).thenReturn(hubMasterProperties);
        when(hubMasterProperties.get(PagePropertyConstants.PROP_SNI_SPONSORSHIP, String.class)).thenReturn(TEST_HUB_MASTER_SPONSOR_PATH);
        when(hubMasterProperties.get(PagePropertyConstants.PROP_SNI_PACKAGE, String.class)).thenReturn(TEST_HUB_PACKAGE_ANCHOR_PATH);

        when(packageAnchorPage.isValid()).thenReturn(true);
        when(packageAnchorPage.getPath()).thenReturn(TEST_PACKAGE_ANCHOR_PATH);
        when(packageAnchorPage.getProperties()).thenReturn(packageAnchorProperties);
        when(packageAnchorPage.getContentResource()).thenReturn(packageAnchorCR);
        when(packageAnchorCR.adaptTo(ValueMap.class)).thenReturn(packageAnchorProperties);
        when(packageAnchorProperties.get(PagePropertyConstants.PROP_SNI_SPONSORSHIP, String.class)).thenReturn(TEST_PACKAGE_SPONSOR_PATH);

        when(hubPackageAnchorPage.isValid()).thenReturn(true);
        when(hubPackageAnchorPage.getPath()).thenReturn(TEST_HUB_PACKAGE_ANCHOR_PATH);
        when(hubPackageAnchorPage.getProperties()).thenReturn(hubPackageAnchorProperties);
        when(hubPackageAnchorPage.getContentResource()).thenReturn(hubPackageAnchorCR);
        when(hubPackageAnchorCR.adaptTo(ValueMap.class)).thenReturn(hubPackageAnchorProperties);
        when(hubPackageAnchorProperties.get(PagePropertyConstants.PROP_SNI_SPONSORSHIP, String.class)).thenReturn(TEST_HUB_PACKAGE_SPONSOR_PATH);

        when(pageSponsorPage.isValid()).thenReturn(true);
        when(pageSponsorPage.getTitle()).thenReturn(SponsorshipSource.PAGE.name());
        when(pageSponsorPage.getPath()).thenReturn(TEST_PAGE_SPONSOR_PATH);
        when(pageSponsorPage.getPageManager()).thenReturn(pageManager);
        when(hubSponsorPage.isValid()).thenReturn(true);
        when(hubSponsorPage.getTitle()).thenReturn(SponsorshipSource.HUB.name());
        when(hubSponsorPage.getPath()).thenReturn(TEST_HUB_MASTER_SPONSOR_PATH);
        when(hubSponsorPage.getPageManager()).thenReturn(pageManager);
        when(packageSponsorPage.isValid()).thenReturn(true);
        when(packageSponsorPage.getTitle()).thenReturn(SponsorshipSource.PACKAGE.name());
        when(packageSponsorPage.getPath()).thenReturn(TEST_PACKAGE_SPONSOR_PATH);
        when(packageSponsorPage.getPageManager()).thenReturn(pageManager);
        when(hubPackageSponsorPage.isValid()).thenReturn(true);
        when(hubPackageSponsorPage.getTitle()).thenReturn(SponsorshipSource.HUBPACKAGE.name());
        when(hubPackageSponsorPage.getPath()).thenReturn(TEST_HUB_PACKAGE_SPONSOR_PATH);
        when(hubPackageSponsorPage.getPageManager()).thenReturn(pageManager);

        when(contentPage.adaptTo(Resource.class)).thenReturn(contentCR);
        when(contentCR.getResourceResolver()).thenReturn(resourceResolver);
        when(resourceResolver.getResource(TEST_CONTENT_PATH)).thenReturn(contentPageResource);
        when(contentPageResource.adaptTo(Page.class)).thenReturn(contentPage);
        when(contentPage.getParent()).thenReturn(hubMasterPage);
        when(contentPage.getPath()).thenReturn(TEST_CONTENT_PATH);
        when(hubMasterPage.getContentResource()).thenReturn(hubMasterCR);
        when(hubMasterCR.getChild(Hub.HUB_NODE_NAME)).thenReturn(hubResource);
        when(hubMasterPage.getContentResource(Hub.HUB_NODE_NAME)).thenReturn(hubResource);
        when(hubMasterPage.adaptTo(Resource.class)).thenReturn(hubResource);
        when(hubResource.adaptTo(ValueMap.class)).thenReturn(hubProperties);
        when(hubProperties.containsKey(Hub.HUB_PAGES_PROPERTY)).thenReturn(true);
        when(hubProperties.get(Hub.HUB_PAGES_PROPERTY, String[].class)).thenReturn(new String[] { TEST_CONTENT_PATH });
        when(hubResource.getResourceResolver()).thenReturn(resourceResolver);
    }

    @Test
    public void testHappyPath() {
        testPage = PageFactory.getSniPage(contentPage);
        Hub theHub = testPage.getHub();
        assertNotNull("hub is nonnull", theHub);
        assertNotNull("hub master is nonnull", theHub.getHubMaster());
        sponsorshipManager = testPage.getSponsorshipManager();
        assertNotNull("sponsorship manager not null", sponsorshipManager);
        assertEquals("hubpackage is effective sponsorship", SponsorshipSource.HUBPACKAGE.name(), sponsorshipManager.getEffectiveSponsorshipValue());
        List<SponsorshipProvider> sponsorships = sponsorshipManager.getAllSponsorshipProviders();
        for (SponsorshipSource s : sponsorshipManager.getOrderedSources()) {
            SponsorshipProvider p = sponsorshipManager.getSponsorshipProvider(s);
            assertNotNull("source " + s.name() + " has provider", p);
        }
    }

}
