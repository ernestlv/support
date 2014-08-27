package com.scrippsnetworks.wcm.export.snipage.content.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.text.Text;
import com.scrippsnetworks.wcm.config.SiteConfigService;
import com.scrippsnetworks.wcm.export.snipage.ExportWriter;
import com.scrippsnetworks.wcm.export.snipage.PageExportException;
import com.scrippsnetworks.wcm.export.snipage.content.PageExport;
import com.scrippsnetworks.wcm.export.snipage.content.PageExportFactory;
import com.scrippsnetworks.wcm.fnr.util.PageSlingResourceTypes;
import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.hub.HubPageTypeKeys;
import com.scrippsnetworks.wcm.hub.button.HubButton;
import com.scrippsnetworks.wcm.hub.button.HubButtonContainer;
import com.scrippsnetworks.wcm.metadata.SponsorshipManager;
import com.scrippsnetworks.wcm.metadata.SponsorshipProvider;
import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.snipackage.SniPackage;
import com.scrippsnetworks.wcm.snitag.SniTag;
import com.scrippsnetworks.wcm.util.StringUtil;

/** Tests the SniPageExport class, whose job it is to extract export values from an SniPage.
 *
 */
public class SniPageExportTest {

    private static final String PAGE_PATH = "/content/food/shows/a/a-show"; // there is a specific test below relying on this being a show
    private static final String PAGE_ASSET_PATH = "/etc/sni-asset/shows/a/a-show";
    private static final String PAGE_URL = PAGE_PATH + ".html";
    private static final String PAGE_FRIENDLY_URL = "/shows/a-show.html";
    private static final String PAGE_BRAND = "food";
    private static final String PAGE_RESOURCE_TYPE = PageSlingResourceTypes.SHOW.resourceType();
    private static final String PAGE_TYPE = Text.getName(PAGE_RESOURCE_TYPE);
    private static final String PAGE_SECTION_NAME = "shows";
    private static final String PAGE_LAST_REPLICATION_ACTION = "Publish";
    private static final String PAGE_TITLE = "A ‒ Title";
    private static final String PAGE_SHORT_TITLE = "A short title";
    private static final String PAGE_LONG_DESCRIPTION = "A Description";
    private static final String PAGE_SEO_TITLE = "An SEO Title";
    private static final String PAGE_SEO_DESCRIPTION = "An SEO Description";
    private static final String[] PAGE_CQ_TAGS = { "food-tags:foo/bar", "food-tags:baz/boo" };
    private static final String[] PAGE_PEOPLE = { "/etc/sni-asset/people/hosts/a/a-host", "/etc/sni-asset/people/hosts/a/a-host" };
    private static final String PAGE_PRIMARY_TAG = "food-tags:primary";
    private static final String PAGE_SECONDARY_TAG = "food-tags:secondary";
    private static final String SPONSORSHIP_SNI_ADKEY = "provider-sponsorship";
    private static final String PAGE_ASSET_UID = "dead-beef-dead-beef";
    private static final String HUB_MASTER_PAGE_TYPE = Text.getName(PageSlingResourceTypes.SHOW.name());
    private static final String HUB_MASTER_UID = "mast-eraa-aaaa-beef";
    private static final String HUB_CHILD_PATH = PAGE_PATH + "/photos";
    private static final String HUB_CHILD_UID = "aaaa-dead-beef-aaaa";
    private static final String HUB_CHILD_RESOURCE_TYPE = PageSlingResourceTypes.PHOTO_GALLERY.name();
    private static final String HUB_CHILD_PAGE_TYPE = Text.getName(HUB_CHILD_RESOURCE_TYPE);
    private static final String HUB_CHILD2_PATH = PAGE_PATH + "/videos";
    private static final String HUB_CHILD2_UID = "dead-beef-dead-beef";
    private static final String HUB_CHILD2_RESOURCE_TYPE = PageSlingResourceTypes.VIDEO_CHANNEL.resourceType();
    private static final String HUB_CHILD2_PAGE_TYPE = Text.getName(HUB_CHILD2_RESOURCE_TYPE);
    private static final String PAGE_PRIMARY_TALENT_ASSET_PATH = "/etc/sni-asset/people/hosts/a/a-host";
    private static final String PAGE_PRIMARY_TALENT_CONTENT_PATH = "/content/food/chefs/a/a-host";
    private static final String PAGE_PRIMARY_TALENT_OTHERSITE_PATH = "/content/othersite/chefs/a/a-host";
    private static final String PAGE_PRIMARY_TALENT_UID = "bbbb-dead-beef-bbbb";
    private static final String PAGE_PRIMARY_TALENT_URL = PAGE_PRIMARY_TALENT_CONTENT_PATH + ".html";
    private static final String PAGE_PRIMARY_TALENT_PAGE_TYPE = "talent";
    private static final String PAGE_PRIMARY_TALENT_NAME = "A Talent";
    private static final String EFFECTIVE_SPONSORSHIP = "FOODEBAR";
    private static final String PAGE_IMAGE_PATH = "/content/dam/images/food/2013/1/1/0/canonical-image-for-this-page.jpg";
    private static final String PAGE_PACKAGE_PATH = "/content/food/shows/a-package-landing-page";
    private static final Integer PAGE_SNI_FASTFWD_ID = 123456;
    private static final String PAGE_SHOW_ABBREVIATION = "FDBR";
    private static final String PAGE_SOURCE = "food-sources:esearch";
    private static final String PAGE_TUNE_IN_TIME = "Saturdays at 11:30am/10:30c ";
    private static final Boolean PAGE_DO_NOT_FEATURE = true;
    private static final Boolean PAGE_DO_NOT_SEARCH = true;
    private static final String PAGE_ABSTRACT = "Abstraction information";
    private static final String PAGE_SOURCE_IMAGEPATH = "/content/dam/images/food/unsized/2013/8/23/0/logo-entwine-135x40.jpg";
    private static final String PAGE_SOURCE_URL = "http://www.entwine-wines.com/";
    private static final String ALTERNATE_URL = "http://www.foodnetwork.com/content/food/show-name";

    @Mock SniPage sniPage;
    @Mock Resource sniPageCR;
    @Mock ValueMap sniPageProperties;
    @Mock Hub hub;
    @Mock HubButtonContainer hubButtonContainer;
    @Mock HubButton hubButton1;
    @Mock HubButton hubButton2;
    @Mock HubButton hubButton3;
    @Mock SniPackage sniPackage;
    @Mock SniPage packageAnchor;
    @Mock SniPage hubMaster;
    @Mock SniPage hubChild;
    @Mock Resource hubChildResource;
    @Mock SniPage hubChild2;
    @Mock Resource hubChild2Resource;
    @Mock SniPage sectionPage;
    @Mock Page talentAssetPage;
    @Mock ValueMap talentAssetPageProperties;
    @Mock SniPage talentContentPage;
    @Mock ValueMap talentContentPageProperties;
    @Mock SniPage sponsorshipProviderPage;
    @Mock ValueMap sponsorshipProviderPageProperties;
    @Mock SponsorshipManager sponsorshipManager;
    @Mock SponsorshipProvider sponsorshipProvider;
    @Mock PageManager pageManager;
    @Mock SniTag sniTag;
    @Mock SniPage tagPage;
    @Mock ValueMap tagPageProperties;
    @Mock SiteConfigService siteConfigService;

    private final Calendar created = Calendar.getInstance();
    private final Calendar lastModified = Calendar.getInstance();
    private final Calendar lastReplicated = Calendar.getInstance();
    
    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        when(sniPage.getPath()).thenReturn(PAGE_PATH);
        when(sniPage.getDepth()).thenReturn((PAGE_PATH.split("/")).length - 1);
        when(sniPage.hasContent()).thenReturn(true);
        when(sniPage.getProperties()).thenReturn(sniPageProperties);
        when(sniPage.getPageManager()).thenReturn(pageManager);
    }

    /** Sets up id and path, and path-related properties. */
    private void setupBasicPage() {
        // properties taken from SniPage accessor methods
        when(sniPage.getPath()).thenReturn(PAGE_PATH);
        when(sniPage.getBrand()).thenReturn(PAGE_BRAND);
        when(sniPage.getPageType()).thenReturn(PAGE_TYPE);
        when(sectionPage.getName()).thenReturn(PAGE_SECTION_NAME);
        when(sniPage.getAbsoluteParent(2)).thenReturn(sectionPage);
        when(sniPage.getUid()).thenReturn(PAGE_ASSET_UID);
        when(sniPage.getFriendlyUrl()).thenReturn(PAGE_FRIENDLY_URL);
        when(sniPage.getContentResource()).thenReturn(sniPageCR);
        when(sniPageCR.getResourceType()).thenReturn(PAGE_RESOURCE_TYPE);
        
        when(sniPage.getCanonicalUrl()).thenReturn(ALTERNATE_URL);

        when(sniPageProperties.get(SniPageExport.PAGE_PROP_SNI_ASSET_LINK, String.class)).thenReturn(PAGE_ASSET_PATH);
        when(sniPageProperties.get(SniPageExport.PAGE_PROP_SNI_FASTFWD_ID, Integer.class)).thenReturn(PAGE_SNI_FASTFWD_ID);
    }
    
    /** Sets up source Sponsorship. */
    private void setupSourceSponsorship() {
        when(sniPage.getSourcePage()).thenReturn(tagPage);
        when(tagPage.getProperties()).thenReturn(tagPageProperties);
        when(tagPageProperties.get(PagePropertyConstants.PROP_SNI_TAG_IMAGE, String.class)).thenReturn(PAGE_SOURCE_IMAGEPATH);
        when(tagPageProperties.get(PagePropertyConstants.PROP_SNI_TAG_LINKURL, String.class)).thenReturn(PAGE_SOURCE_URL);
    }

    /** Sets up audit dates. */
    private void setupAuditProperties() {
        when(sniPage.getLastModified()).thenReturn(lastModified);
        when(sniPageProperties.get(SniPageExport.PAGE_PROP_CQ_LAST_REPLICATION_ACTION, String.class)).thenReturn(PAGE_LAST_REPLICATION_ACTION);
        when(sniPageProperties.get(SniPageExport.PAGE_PROP_CQ_LAST_REPLICATED, Calendar.class)).thenReturn(lastReplicated);
        when(sniPageProperties.get(SniPageExport.PAGE_PROP_JCR_CREATED, Calendar.class)).thenReturn(created);
    }

    /** Sets up the basic Title/Image/Description tuple. */
    private void setupTitleImageDescriptionProperties() {
        when(sniPage.getTitle()).thenReturn(PAGE_TITLE);
        when(sniPage.getDescription()).thenReturn(PAGE_LONG_DESCRIPTION);
        when(sniPage.getCanonicalImagePath()).thenReturn(PAGE_IMAGE_PATH);
    }
    
    /** Sets up short title. */
    private void setupShortTitle() {
        when(sniPage.getShortTitle()).thenReturn(PAGE_SHORT_TITLE);
    }
    
    /** Sets up SEO properties. */
    private void setupSeoProperties() {
        when(sniPage.getSeoTitle()).thenReturn(PAGE_SEO_TITLE);
        when(sniPage.getSeoDescription()).thenReturn(PAGE_SEO_DESCRIPTION);
    }

    /** Sets up tags. */
    private void setupTags() {
        when(sniPageProperties.get(SniPageExport.PAGE_PROP_CQ_TAGS, String[].class)).thenReturn(PAGE_CQ_TAGS);
        when(sniPageProperties.get(SniPageExport.PAGE_PROP_SNI_PRIMARY_TAG, String.class)).thenReturn(PAGE_PRIMARY_TAG);
        when(sniPageProperties.get(SniPageExport.PAGE_PROP_SNI_SECONDARY_TAG, String.class)).thenReturn(PAGE_SECONDARY_TAG);
        when(sniPageProperties.get(SniPageExport.PAGE_PROP_SNI_SOURCE, String.class)).thenReturn(PAGE_SOURCE);
    }
    
    /** Sets up people. */
    private void setupPeople() {
        when(sniPageProperties.get(SniPageExport.PAGE_PROP_SNI_PEOPLE, String[].class)).thenReturn(PAGE_PEOPLE);
    }
    
    /** Set up Abstraction. */
    private void setupAbstract() {
        when(sniPageProperties.get(SniPageExport.PAGE_PROP_SNI_ABSTRACT, String.class)).thenReturn(PAGE_ABSTRACT);
    }

    /** Sets up hub relationship. */
    private void setupHub() {
        setupBasicPage();
        List<SniPage> hubChildren = Arrays.asList(hubChild, hubChild2);
        when(sniPage.getHub()).thenReturn(hub);
        when(hub.getHubMaster()).thenReturn(sniPage);
        when(hub.getHubChildren()).thenReturn(hubChildren);
        when(hubChild.getPath()).thenReturn(HUB_CHILD_PATH);
        when(hubChild.getUid()).thenReturn(HUB_CHILD_UID);
        when(hubChild.getPageType()).thenReturn(HUB_CHILD_PAGE_TYPE);
        when(hubChild.getContentResource()).thenReturn(hubChildResource);
        when(hubChildResource.getResourceType()).thenReturn(HUB_CHILD_RESOURCE_TYPE);
        when(hubChild2.getPath()).thenReturn(HUB_CHILD2_PATH);
        when(hubChild2.getUid()).thenReturn(HUB_CHILD2_UID);
        when(hubChild2.getPageType()).thenReturn(HUB_CHILD2_PAGE_TYPE);
        when(hubChild2.getContentResource()).thenReturn(hubChild2Resource);
        when(hubChild2Resource.getResourceType()).thenReturn(HUB_CHILD_RESOURCE_TYPE);

        // hub button setup
        when(hub.getHubButtonContainer()).thenReturn(hubButtonContainer);
        when(hubButton1.getButtonLabel()).thenReturn(HubPageTypeKeys.MAIN.name());
        when(hubButton2.getButtonLabel()).thenReturn(HubPageTypeKeys.PHOTO.name());
        when(hubButton3.getButtonLabel()).thenReturn(HubPageTypeKeys.VIDEO.name());
        List<HubButton> hubButtonList = Arrays.asList(hubButton1, hubButton2, hubButton3);
        when(hubButtonContainer.getHubButtons()).thenReturn(hubButtonList);

        // setup hub master as a show
        when(sniPage.getPageType()).thenReturn(HUB_MASTER_PAGE_TYPE);
        when(sniPage.getUid()).thenReturn(HUB_MASTER_UID);
    }

    /** Sets up the package relationship. */
    private void setupPackage() {
        when(sniPage.getSniPackage()).thenReturn(sniPackage);
        when(sniPackage.getPackageAnchor()).thenReturn(packageAnchor);
        when(sniPage.getPackageAnchor()).thenReturn(packageAnchor);
        when(packageAnchor.getPath()).thenReturn(PAGE_PACKAGE_PATH);
    }

    /** Sets up sponsorship and adkey, which both have the same inheritance behavior. */
    private void setupSponsorProperties() {
        when(sniPage.getSponsorshipManager()).thenReturn(sponsorshipManager);
        when(sponsorshipManager.getEffectiveSponsorshipProvider()).thenReturn(sponsorshipProvider);
        when(sponsorshipProvider.getProvider()).thenReturn(sponsorshipProviderPage);
        when(sponsorshipProviderPage.getProperties()).thenReturn(sponsorshipProviderPageProperties);
        when(sponsorshipProviderPageProperties.get(SniPageExport.PAGE_PROP_SNI_ADKEY, String.class)).thenReturn(SPONSORSHIP_SNI_ADKEY);
        when(sponsorshipProvider.getSponsorshipValue()).thenReturn(EFFECTIVE_SPONSORSHIP);
    }

    /** Sets up the talent relationship. */
    private void setupPrimaryTalent() {
        when(pageManager.getPage(PAGE_PRIMARY_TALENT_ASSET_PATH)).thenReturn(talentAssetPage);
        when(pageManager.getPage(PAGE_PRIMARY_TALENT_CONTENT_PATH)).thenReturn(talentContentPage);
        when(sniPageProperties.get(SniPageExport.PAGE_PROP_SNI_PRIMARY_TALENT, String.class)).thenReturn(PAGE_PRIMARY_TALENT_ASSET_PATH);
        when(talentAssetPage.getProperties()).thenReturn(talentAssetPageProperties);
        when(talentAssetPageProperties.get(SniPageExport.PAGE_PROP_SNI_PAGE_LINKS, String[].class))
                .thenReturn(new String[] { PAGE_PRIMARY_TALENT_OTHERSITE_PATH, PAGE_PRIMARY_TALENT_CONTENT_PATH });
        when(talentContentPage.hasContent()).thenReturn(true);
        when(talentContentPage.getUid()).thenReturn(PAGE_PRIMARY_TALENT_UID);
        when(talentContentPage.getFriendlyUrl()).thenReturn(PAGE_PRIMARY_TALENT_URL);
        when(talentContentPage.getPageType()).thenReturn(PAGE_PRIMARY_TALENT_PAGE_TYPE);
        when(talentContentPage.getTitle()).thenReturn(PAGE_PRIMARY_TALENT_NAME);
        when(talentContentPage.getPath()).thenReturn(PAGE_PRIMARY_TALENT_CONTENT_PATH);
    }

    private void setupSearchFlags() {
        when(sniPage.isDoNotSearch()).thenReturn(PAGE_DO_NOT_SEARCH);
        when(sniPageProperties.get(SniPageExport.PAGE_PROP_DO_NOT_FEATURE, false)).thenReturn(PAGE_DO_NOT_FEATURE);
       }
    
    /** Sets up various properties pages might or might not have */
    private void setupOtherProperties() {
        when(sniPageProperties.get(SniPageExport.PAGE_PROP_SNI_SHOW_ABBREVIATION, String.class)).thenReturn(PAGE_SHOW_ABBREVIATION);
        when(sniPageProperties.get(SniPageExport.PAGE_PROP_SNI_TUNE_IN_TIME, String.class)).thenReturn(PAGE_TUNE_IN_TIME);
    }

    void fullSetup() {
        setupBasicPage();
        setupAuditProperties();
        setupTitleImageDescriptionProperties();
        setupShortTitle();
        setupSeoProperties();
        setupTags();
        setupPeople();
        setupSponsorProperties();
        setupPrimaryTalent();
        setupPackage();
        setupHub();
        setupSearchFlags();
        setupOtherProperties();
        setupAbstract();
        setupSourceSponsorship();
    }

    @Test
    public void testAllPropertiesSetup() {
        fullSetup();
        PageExport pageExport = new SniPageExport(sniPage);
        ValueMap exportProps = pageExport.getValueMap();

        for (SniPageExport.ExportProperty prop : SniPageExport.ExportProperty.values()) {
        	if(prop.name() != null && prop.name().equals(SniPageExport.ExportProperty.CORE_IMAGE_URL.name())) {
        		
        		assertNull(SniPageExport.ExportProperty.CORE_IMAGE_URL.name(),
        		exportProps.get(SniPageExport.ExportProperty.CORE_IMAGE_URL.name(),
                SniPageExport.ExportProperty.CORE_IMAGE_URL.valueClass()));
        		
        	} else {
        		assertNotNull(prop.name(), exportProps.get(prop.name(), prop.valueClass()));
        	}
        }
    }
    
    @Test
    public void testNullPage() {
        assertNull(PageExportFactory.createPageExport(null));
    }

    @Test
    public void testNoContentPage() {
        when(sniPage.hasContent()).thenReturn(false);
        when(sniPage.getContentResource()).thenReturn(null);
        assertNull(PageExportFactory.createPageExport(sniPage));
    }

    @Test
    public void testConstructorNullPage() {
        assertNotNull(new SniPageExport(sniPage));
    }

    @Test
    public void testConstructorNoContentPage() {
        when(sniPage.hasContent()).thenReturn(false);
        when(sniPage.getContentResource()).thenReturn(null);
        assertNotNull(new SniPageExport(sniPage));
    }

    @Test
    public void testNoProperties() {
        SniPageExport pageExport = new SniPageExport(sniPage);
        assertNotNull("nonnull value map", pageExport.getValueMap());
    }

    @Test
    public void testBasicPage() {
        setupBasicPage();
        PageExport pageExport = new SniPageExport(sniPage);
        ValueMap exportProps = pageExport.getValueMap();

        assertEquals(SniPageExport.ExportProperty.CORE_PAGE_TYPE.name(), PAGE_TYPE,
                exportProps.get(SniPageExport.ExportProperty.CORE_PAGE_TYPE.name(),
                        SniPageExport.ExportProperty.CORE_PAGE_TYPE.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_ASSETUID.name(), PAGE_ASSET_UID,
                exportProps.get(SniPageExport.ExportProperty.CORE_ASSETUID.name(),
                        SniPageExport.ExportProperty.CORE_ASSETUID.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_BRAND.name(), PAGE_BRAND,
                exportProps.get(SniPageExport.ExportProperty.CORE_BRAND.name(),
                        SniPageExport.ExportProperty.CORE_BRAND.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_SECTION_NAME.name(), PAGE_SECTION_NAME,
                exportProps.get(SniPageExport.ExportProperty.CORE_SECTION_NAME.name(),
                        SniPageExport.ExportProperty.CORE_SECTION_NAME.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_URL.name(), PAGE_URL,
                exportProps.get(SniPageExport.ExportProperty.CORE_URL.name(),
                        SniPageExport.ExportProperty.CORE_URL.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_FRIENDLY_URL.name(), PAGE_FRIENDLY_URL,
                exportProps.get(SniPageExport.ExportProperty.CORE_FRIENDLY_URL.name(),
                        SniPageExport.ExportProperty.CORE_FRIENDLY_URL.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_CRX_PATH.name(), PAGE_ASSET_PATH,
                exportProps.get(SniPageExport.ExportProperty.CORE_CRX_PATH.name(),
                        SniPageExport.ExportProperty.CORE_CRX_PATH.valueClass()));
        
        assertEquals(SniPageExport.ExportProperty.CORE_ALTERNATE_URL.name(), ALTERNATE_URL,
                exportProps.get(SniPageExport.ExportProperty.CORE_ALTERNATE_URL.name(),
                        SniPageExport.ExportProperty.CORE_ALTERNATE_URL.valueClass()));
        
    }

    @Test
    public void testTagProperties() {
        setupTags();
        PageExport pageExport = new SniPageExport(sniPage);
        ValueMap exportProps = pageExport.getValueMap();

        assertEquals(SniPageExport.ExportProperty.CORE_TAG_CRX_PATH.name(), PAGE_CQ_TAGS,
                exportProps.get(SniPageExport.ExportProperty.CORE_TAG_CRX_PATH.name(),
                        SniPageExport.ExportProperty.CORE_TAG_CRX_PATH.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_PRIMARY_TAG.name(), PAGE_PRIMARY_TAG,
                exportProps.get(SniPageExport.ExportProperty.CORE_PRIMARY_TAG.name(),
                        SniPageExport.ExportProperty.CORE_PRIMARY_TAG.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_SECONDARY_TAG.name(), PAGE_SECONDARY_TAG,
                exportProps.get(SniPageExport.ExportProperty.CORE_SECONDARY_TAG.name(),
                        SniPageExport.ExportProperty.CORE_SECONDARY_TAG.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_SOURCE.name(), PAGE_SOURCE,
                exportProps.get(SniPageExport.ExportProperty.CORE_SOURCE.name(),
                        SniPageExport.ExportProperty.CORE_SOURCE.valueClass()));
    }

    @Test
    public void testTitleImageDescriptionProperties() {
        setupTitleImageDescriptionProperties();
        PageExport pageExport = new SniPageExport(sniPage);
        ValueMap exportProps = pageExport.getValueMap();

        assertEquals(SniPageExport.ExportProperty.CORE_TITLE.name(), PAGE_TITLE,
                exportProps.get(SniPageExport.ExportProperty.CORE_TITLE.name(),
                        SniPageExport.ExportProperty.CORE_TITLE.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_LONG_DESCRIPTION.name(), PAGE_LONG_DESCRIPTION,
                exportProps.get(SniPageExport.ExportProperty.CORE_LONG_DESCRIPTION.name(),
                        SniPageExport.ExportProperty.CORE_LONG_DESCRIPTION.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_IMAGE_PATH.name(), PAGE_IMAGE_PATH,
                exportProps.get(SniPageExport.ExportProperty.CORE_IMAGE_PATH.name(),
                        SniPageExport.ExportProperty.CORE_IMAGE_PATH.valueClass()));

    }

    @Test
    public void testShortTitle() {
        setupShortTitle();
        PageExport pageExport = new SniPageExport(sniPage);
        ValueMap exportProps = pageExport.getValueMap();

        assertEquals(SniPageExport.ExportProperty.CORE_SHORT_TITLE.name(), PAGE_SHORT_TITLE,
                exportProps.get(SniPageExport.ExportProperty.CORE_SHORT_TITLE.name(),
                        SniPageExport.ExportProperty.CORE_SHORT_TITLE.valueClass()));
        
    }
    
    @Test
    public void testSourceSponsorship() {
        setupSourceSponsorship();
        PageExport pageExport = new SniPageExport(sniPage);
        ValueMap exportProps = pageExport.getValueMap();

        assertEquals(SniPageExport.ExportProperty.CORE_SOURCE_IMAGEPATH.name(), PAGE_SOURCE_IMAGEPATH,
                exportProps.get(SniPageExport.ExportProperty.CORE_SOURCE_IMAGEPATH.name(),
                        SniPageExport.ExportProperty.CORE_SOURCE_IMAGEPATH.valueClass()));
        
        assertEquals(SniPageExport.ExportProperty.CORE_SOURCE_URL.name(), PAGE_SOURCE_URL,
                exportProps.get(SniPageExport.ExportProperty.CORE_SOURCE_URL.name(),
                        SniPageExport.ExportProperty.CORE_SOURCE_URL.valueClass()));
    }
    
    @Test
    public void testAuditProperties() {
        setupAuditProperties();
        PageExport pageExport = new SniPageExport(sniPage);
        ValueMap exportProps = pageExport.getValueMap();

        assertEquals(SniPageExport.ExportProperty.CQ_LAST_MODIFIED.name(), SniPageExport.getDateInString(lastModified),
                exportProps.get(SniPageExport.ExportProperty.CQ_LAST_MODIFIED.name(),
                        SniPageExport.ExportProperty.CQ_LAST_MODIFIED.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CQ_LAST_REPLICATION_ACTION.name(), PAGE_LAST_REPLICATION_ACTION,
                exportProps.get(SniPageExport.ExportProperty.CQ_LAST_REPLICATION_ACTION.name(),
                        SniPageExport.ExportProperty.CQ_LAST_REPLICATION_ACTION.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_CREATION_DATE.name(), SniPageExport.getDateInString(created),
                exportProps.get(SniPageExport.ExportProperty.CORE_CREATION_DATE.name(),
                        SniPageExport.ExportProperty.CORE_CREATION_DATE.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_RELEASE_DATE.name(), SniPageExport.getDateInString(lastReplicated),
                exportProps.get(SniPageExport.ExportProperty.CORE_RELEASE_DATE.name(),
                        SniPageExport.ExportProperty.CORE_RELEASE_DATE.valueClass()));

    }

    @Test
    public void testSeoProperties() {
        setupSeoProperties();
        PageExport pageExport = new SniPageExport(sniPage);
        ValueMap exportProps = pageExport.getValueMap();

        assertEquals(SniPageExport.ExportProperty.CORE_SEO_TITLE.name(), PAGE_SEO_TITLE,
                exportProps.get(SniPageExport.ExportProperty.CORE_SEO_TITLE.name(),
                        SniPageExport.ExportProperty.CORE_SEO_TITLE.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_SEO_DESCRIPTION.name(), PAGE_SEO_DESCRIPTION,
                exportProps.get(SniPageExport.ExportProperty.CORE_SEO_DESCRIPTION.name(),
                        SniPageExport.ExportProperty.CORE_SEO_DESCRIPTION.valueClass()));

    }

    @Test
    public void testSponsorProperties() {
        setupSponsorProperties();
        PageExport pageExport = new SniPageExport(sniPage);
        ValueMap exportProps = pageExport.getValueMap();

        assertEquals(SniPageExport.ExportProperty.CORE_SPONSOR_CODE.name(), EFFECTIVE_SPONSORSHIP,
                exportProps.get(SniPageExport.ExportProperty.CORE_SPONSOR_CODE.name(),
                        SniPageExport.ExportProperty.CORE_SPONSOR_CODE.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_ADKEY.name(), SPONSORSHIP_SNI_ADKEY,
                exportProps.get(SniPageExport.ExportProperty.CORE_ADKEY.name(),
                        SniPageExport.ExportProperty.CORE_ADKEY.valueClass()));
    }

    @Test
    public void testNoSponsorAdkeyProperty() {
        setupSponsorProperties();
        when(sponsorshipManager.getEffectiveSponsorshipProvider()).thenReturn(null);
        when(sniPageProperties.get(SniPageExport.PAGE_PROP_SNI_ADKEY, String.class)).thenReturn(SPONSORSHIP_SNI_ADKEY);
        PageExport pageExport = new SniPageExport(sniPage);
        ValueMap exportProps = pageExport.getValueMap();

        // With no sponsorship, the adkey should come from the page

        assertNull(SniPageExport.ExportProperty.CORE_SPONSOR_CODE.name(),
                exportProps.get(SniPageExport.ExportProperty.CORE_SPONSOR_CODE.name(),
                        SniPageExport.ExportProperty.CORE_SPONSOR_CODE.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_ADKEY.name(), SPONSORSHIP_SNI_ADKEY,
                exportProps.get(SniPageExport.ExportProperty.CORE_ADKEY.name(),
                        SniPageExport.ExportProperty.CORE_ADKEY.valueClass()));
    }

    @Test
    public void testHub() {
        setupHub();
        PageExport pageExport = new SniPageExport(sniPage);
        ValueMap exportProps = pageExport.getValueMap();

        assertEquals(SniPageExport.ExportProperty.CORE_HUBMASTER.name(), new SniPageExport.HubMember(sniPage).getValue(),
                exportProps.get(SniPageExport.ExportProperty.CORE_HUBMASTER.name(),
                        SniPageExport.ExportProperty.CORE_HUBMASTER.valueClass()));

        String[] hubChildren = exportProps.get(SniPageExport.ExportProperty.CORE_HUBCHILD.name(), String[].class);
        assertEquals("count of hub children", hub.getHubChildren().size(), hubChildren.length);

        String hubChildValue = hubChildren[0];

        assertEquals(SniPageExport.ExportProperty.CORE_HUBCHILD.name(), new SniPageExport.HubMember(hubChild).getValue(),
                hubChildValue);

        String hubChild2Value = hubChildren[1];
        assertEquals(SniPageExport.ExportProperty.CORE_HUBCHILD.name(), new SniPageExport.HubMember(hubChild2).getValue(),
                hubChild2Value);

        assertEquals(SniPageExport.ExportProperty.CORE_HUB_DO_NOT_DISPLAY.name(), false,
                exportProps.get(SniPageExport.ExportProperty.CORE_HUB_DO_NOT_DISPLAY.name(),
                        true));
    }

    @Test
    public void testHubNoDisplay() {
        setupHub();
        setupPackage();
        PageExport pageExport = new SniPageExport(sniPage);
        ValueMap exportProps = pageExport.getValueMap();

        // when the page has a package, do not display flag should be true
        assertEquals(SniPageExport.ExportProperty.CORE_HUB_DO_NOT_DISPLAY.name(), true,
                exportProps.get(SniPageExport.ExportProperty.CORE_HUB_DO_NOT_DISPLAY.name(),
                        false));
    }

    @Test
    public void testHubDoNotDisplayFlagEmptyButtonList() {
        setupHub();
        when(hub.getHubButtonContainer()).thenReturn(hubButtonContainer);
        when(hubButtonContainer.getHubButtons()).thenReturn(Collections.EMPTY_LIST);
        PageExport pageExport = new SniPageExport(sniPage);
        ValueMap exportProps = pageExport.getValueMap();

        assertEquals(SniPageExport.ExportProperty.CORE_HUB_DO_NOT_DISPLAY.name(), true,
                exportProps.get(SniPageExport.ExportProperty.CORE_HUB_DO_NOT_DISPLAY.name(),
                        false));
    }

    @Test
    public void testHubDoNotDisplayFlagNullButtonList() {
        setupHub();
        when(hub.getHubButtonContainer()).thenReturn(hubButtonContainer);
        when(hubButtonContainer.getHubButtons()).thenReturn(null);
        PageExport pageExport = new SniPageExport(sniPage);
        ValueMap exportProps = pageExport.getValueMap();

        assertEquals(SniPageExport.ExportProperty.CORE_HUB_DO_NOT_DISPLAY.name(), true,
                exportProps.get(SniPageExport.ExportProperty.CORE_HUB_DO_NOT_DISPLAY.name(),
                        false));
    }

    @Test
    public void testPrimaryTalent() {
        setupPrimaryTalent();
        PageExport pageExport = new SniPageExport(sniPage);
        ValueMap exportProps = pageExport.getValueMap();

        assertEquals(SniPageExport.ExportProperty.CORE_TALENT_ID.name(), PAGE_PRIMARY_TALENT_UID,
                exportProps.get(SniPageExport.ExportProperty.CORE_TALENT_ID.name(),
                        SniPageExport.ExportProperty.CORE_TALENT_ID.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_TALENT_NAME.name(), PAGE_PRIMARY_TALENT_NAME,
                exportProps.get(SniPageExport.ExportProperty.CORE_TALENT_NAME.name(),
                        SniPageExport.ExportProperty.CORE_TALENT_NAME.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_TALENT_URL.name(), PAGE_PRIMARY_TALENT_URL,
                exportProps.get(SniPageExport.ExportProperty.CORE_TALENT_URL.name(),
                        SniPageExport.ExportProperty.CORE_TALENT_URL.valueClass()));


    }

    @Test
    public void testSearchFlags() {
        setupSearchFlags();
        PageExport pageExport = new SniPageExport(sniPage);
        ValueMap exportProps = pageExport.getValueMap();

        assertEquals(SniPageExport.ExportProperty.CORE_DO_NOT_FEATURE.name(), PAGE_DO_NOT_FEATURE,
                exportProps.get(SniPageExport.ExportProperty.CORE_DO_NOT_FEATURE.name(),
                        false));

        assertEquals(SniPageExport.ExportProperty.CORE_DO_NOT_SEARCH.name(), PAGE_DO_NOT_SEARCH,
                exportProps.get(SniPageExport.ExportProperty.CORE_DO_NOT_SEARCH.name(),
                        false));

    }

    @Test
    public void testOtherProperties() {
        setupOtherProperties();
        PageExport pageExport = new SniPageExport(sniPage);
        ValueMap exportProps = pageExport.getValueMap();

        assertEquals(SniPageExport.ExportProperty.CORE_SHOW_ABBREVIATION.name(), PAGE_SHOW_ABBREVIATION,
                exportProps.get(SniPageExport.ExportProperty.CORE_SHOW_ABBREVIATION.name(),
                        SniPageExport.ExportProperty.CORE_SHOW_ABBREVIATION.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_TUNE_IN_TIME.name(), PAGE_TUNE_IN_TIME,
                exportProps.get(SniPageExport.ExportProperty.CORE_TUNE_IN_TIME.name(),
                        SniPageExport.ExportProperty.CORE_TUNE_IN_TIME.valueClass()));

    }

    @Test
    public void testPeople() {
        setupPeople();
        PageExport pageExport = new SniPageExport(sniPage);
        ValueMap exportProps = pageExport.getValueMap();
        
        assertEquals(SniPageExport.ExportProperty.CORE_PEOPLE.name(), PAGE_PEOPLE,
                exportProps.get(SniPageExport.ExportProperty.CORE_PEOPLE.name(),
                        SniPageExport.ExportProperty.CORE_PEOPLE.valueClass()));
        
        String[] people = exportProps.get(SniPageExport.ExportProperty.CORE_PEOPLE.name(), String[].class);

        assertEquals("count of people", PAGE_PEOPLE.length, people.length);
        
        assertEquals("1st talent asset", PAGE_PEOPLE[0], people[0]);
        
        assertEquals("2nd talent asset", PAGE_PEOPLE[1], people[1]);
    }
    
    @Test
    public void testEscaping() {
        String replaceables =  "‒ ><&'\" &#151; &reg; <i> <b attribute=\"foo\">";
        String replaced = StringUtil.cleanToPlainText(replaceables);

        when(sniPage.getTitle()).thenReturn(replaceables);
        when(sniPage.getDescription()).thenReturn(replaceables);
        when(sniPage.getSeoDescription()).thenReturn(replaceables);
        when(sniPage.getSeoTitle()).thenReturn(replaceables);

        PageExport pageExport = new SniPageExport(sniPage);
        ValueMap exportProps = pageExport.getValueMap();

        assertEquals(SniPageExport.ExportProperty.CORE_TITLE.name(), replaced,
                exportProps.get(SniPageExport.ExportProperty.CORE_TITLE.name(),
                        SniPageExport.ExportProperty.CORE_TITLE.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_LONG_DESCRIPTION.name(), replaced,
                exportProps.get(SniPageExport.ExportProperty.CORE_LONG_DESCRIPTION.name(),
                        SniPageExport.ExportProperty.CORE_LONG_DESCRIPTION.valueClass()));


        assertEquals(SniPageExport.ExportProperty.CORE_SEO_TITLE.name(), replaced,
                exportProps.get(SniPageExport.ExportProperty.CORE_SEO_TITLE.name(),
                        SniPageExport.ExportProperty.CORE_SEO_TITLE.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_SEO_DESCRIPTION.name(), replaced,
                exportProps.get(SniPageExport.ExportProperty.CORE_SEO_DESCRIPTION.name(),
                        SniPageExport.ExportProperty.CORE_SEO_DESCRIPTION.valueClass()));
    }
    
    @Test
    public void testAbstract() {
        setupAbstract();
        PageExport pageExport = new SniPageExport(sniPage);
        ValueMap exportProps = pageExport.getValueMap();
        assertEquals(SniPageExport.ExportProperty.CORE_ABSTRACT.name(), PAGE_ABSTRACT,
                exportProps.get(SniPageExport.ExportProperty.CORE_ABSTRACT.name(),
                        SniPageExport.ExportProperty.CORE_ABSTRACT.valueClass()));
    }


    /* The following is not a unit test. However, there's a lot of setup in this class that I want to reuse for checking the resulting xml. */
    // @Test
    public void testWithXmlWriter() throws XPathExpressionException {
        fullSetup();
        StringWriter writer = new StringWriter();
        writeXml(sniPage, new PrintWriter(writer));
        String exportXml = writer.toString();
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (SniPageExport.ExportProperty prop : SniPageExport.ExportProperty.values()) {
            StringBuilder path = new StringBuilder();
            path.append("/RECORDS/RECORD/PROP[@NAME=\"")
                    .append(prop.name())
                    .append("\"]/PVAL");
            NodeList nodeList = (NodeList) xpath.evaluate(path.toString(), new InputSource(new StringReader(exportXml)), XPathConstants.NODESET);
            List<String> resultList = new ArrayList<String>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String result = node.getTextContent();
                    resultList.add(result);
                }
            }
            System.out.println(path + " = " + (resultList.size() == 1 ? resultList.get(0) : resultList.toString()));
        }
    }

    private void writeXml(SniPage sniPage, PrintWriter writer) {
        try {
            ExportWriter.writeExportXml(sniPage, writer);
        } catch (PageExportException e) {
            e.printStackTrace();
            throw new RuntimeException("error writing export xml " + e.getMessage(), e);
        }
    }

}
