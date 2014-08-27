package com.scrippsnetworks.wcm.export.snipage.content.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.scrippsnetworks.wcm.util.PageTypes;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.text.Text;
import com.scrippsnetworks.wcm.config.SiteConfigService;
import com.scrippsnetworks.wcm.export.snipage.content.AbstractPageExport;
import com.scrippsnetworks.wcm.export.snipage.content.ExportPropertyName;
import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.hub.HubPageTypeKeys;
import com.scrippsnetworks.wcm.hub.button.HubButton;
import com.scrippsnetworks.wcm.hub.button.HubButtonContainer;
import com.scrippsnetworks.wcm.metadata.SponsorshipManager;
import com.scrippsnetworks.wcm.metadata.SponsorshipProvider;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.snipackage.SniPackage;
import com.scrippsnetworks.wcm.snitag.SniTag;
import com.scrippsnetworks.wcm.snitag.SniTagFactory;
import com.scrippsnetworks.wcm.url.impl.PathHelper;
import com.scrippsnetworks.wcm.util.StringUtil;

public class SniPageExport extends AbstractPageExport {

    private static final Logger LOG = LoggerFactory.getLogger(SniPageExport.class);
            
    public static final String COMPOSITE_VALUE_DELIMITER = "|";
    public static final String COMPOSITE_VALUE_REPLACEMENT = "&#x7c;";
    public static final String HTTP_PROTOCOL = "http://";
    public static final String CONTENT_ROOT = "/content";
    public static final String SLASH = "/";
    
    public enum ExportProperty implements ExportPropertyName {
        CORE_ADKEY(String.class),
        CORE_ASSETUID(String.class),
        CORE_BRAND(String.class),
        CORE_CREATION_DATE(String.class),
        CORE_CRX_PATH(String.class),
        CORE_FFID(Integer.class),
        CORE_FRIENDLY_URL(String.class),
        CORE_HUBCHILD(String[].class),
        CORE_HUBMASTER(String.class),
        CORE_HUB_DO_NOT_DISPLAY(Boolean.class),
        CORE_IMAGE_PATH(String.class),
        CORE_IMAGE_URL(String.class),
        CORE_LONG_DESCRIPTION(String.class),
        CORE_PACKAGE_ID(String.class),
        CORE_PAGE_TYPE(String.class),
        CORE_PRIMARY_TAG(String.class),
        CORE_RELEASE_DATE(String.class),
        CORE_SECONDARY_TAG(String.class),
        CORE_SECTION_ID(String.class),
        CORE_SECTION_NAME(String.class),
        CORE_SEO_DESCRIPTION(String.class),
        CORE_SEO_TITLE(String.class),
        CORE_SHOW_ABBREVIATION(String.class),
        CORE_SHOW_ID(String.class),
        CORE_SOURCE(String.class),
        CORE_SPONSOR_CODE(String.class),
        CORE_TAG_CRX_PATH(String[].class),
        CORE_TALENT_ID(String.class),
        CORE_TALENT_NAME(String.class),
        CORE_TALENT_URL(String.class),
        CORE_TALENT_FRIENDLY_URL(String.class),
        CORE_TITLE(String.class),
        CORE_TUNE_IN_TIME(String.class),
        CORE_URL(String.class),
        CQ_LAST_MODIFIED(String.class),
        CQ_LAST_REPLICATION_ACTION(String.class),
        CORE_DO_NOT_FEATURE(Boolean.class),
        CORE_DO_NOT_SEARCH(Boolean.class),
        CORE_PEOPLE(String[].class),
        CORE_ABSTRACT(String.class),
        CORE_SHORT_TITLE(String.class),
        CORE_SOURCE_IMAGEPATH(String.class),
        CORE_SOURCE_URL(String.class),
        CORE_ALTERNATE_URL(String.class);

        final Class clazz;

        ExportProperty(Class clazz) {
            this.clazz = clazz;
        }

        public Class valueClass() {
            return clazz;
        }
    }

    public static final String PAGE_PROP_CQ_LAST_REPLICATION_ACTION = NameConstants.PN_PAGE_LAST_REPLICATION_ACTION;
    public static final String PAGE_PROP_CQ_LAST_REPLICATED = NameConstants.PN_PAGE_LAST_REPLICATED;
    public static final String PAGE_PROP_JCR_CREATED = JcrConstants.JCR_CREATED;
    public static final String PAGE_PROP_CQ_TAGS = NameConstants.PN_TAGS;
    public static final String PAGE_PROP_SNI_PRIMARY_TAG = "sni:primaryTag";
    public static final String PAGE_PROP_SNI_SECONDARY_TAG = "sni:secondaryTag";
    public static final String PAGE_PROP_SNI_ASSET_LINK = "sni:assetLink";
    public static final String PAGE_PROP_SNI_ADKEY = "sni:adkey";
    public static final String PAGE_PROP_SNI_PRIMARY_TALENT = "sni:primaryTalent";
    public static final String PAGE_PROP_SNI_PAGE_LINKS = "sni:pageLinks";
    public static final String PAGE_PROP_SNI_SHOW_ABBREVIATION = "sni:showAbbreviation";
    public static final String PAGE_PROP_SNI_FASTFWD_ID = "sni:fastfwdId";
    public static final String PAGE_PROP_SNI_IMAGE = "sni:image";
    public static final String PAGE_PROP_SNI_SOURCE = "sni:source";
    public static final String PAGE_PROP_SNI_TUNE_IN_TIME = "sni:tuneInTime";
    public static final String PAGE_PROP_DO_NOT_FEATURE = "sni:doNotFeature";
    public static final String PAGE_PROP_DO_NOT_SEARCH = "sni:doNotSearch";
    public static final String PAGE_PROP_SNI_PEOPLE = "sni:people";
    public static final String PAGE_PROP_SNI_ASSETUID = "sni:assetUId";
    public static final String PAGE_PROP_SNI_ABSTRACT = "sni:abstract";

    /** Used to serialize hub member pages into a String property value. YUK. */
    public static class HubMember {
        final SniPage sniPage;
        public HubMember(SniPage sniPage) {
            this.sniPage = sniPage;
        }
        
        public String getAssetUid() {
        	if(sniPage == null) {
        		return null;
        	}
        	return sniPage.getUid();
        }

        public String getValue() {
            if (sniPage == null) {
                return null;
            }

            HubPageTypeKeys key = HubPageTypeKeys.getKeyForSniPage(sniPage);
            String buttonLabel = null;
            if (key != null) {
                buttonLabel = key.keyName();
            }

            Hub hub = sniPage.getHub();
            if (hub != null) {
                HubButtonContainer hubButtonContainer = hub.getHubButtonContainer();
                if (hubButtonContainer != null) {
                    List<HubButton> buttons = hubButtonContainer.getHubButtons();
                    if (buttons != null && buttons.size() > 0) {
                        HubButton button = getHubButton(buttons, sniPage);
                        if (button != null) {
                            buttonLabel = StringUtils.capitalize(button.getButtonLabel());
                        }
                    }
                }
            }

            return sniPage.getPageType()
                + COMPOSITE_VALUE_DELIMITER + ((buttonLabel != null) ? buttonLabel : "")
                + COMPOSITE_VALUE_DELIMITER + sniPage.getUid()
                + COMPOSITE_VALUE_DELIMITER + sniPage.getUrl()
                + COMPOSITE_VALUE_DELIMITER + sniPage.getFriendlyUrl()
                + COMPOSITE_VALUE_DELIMITER + "TRUE"; // This was hardcoded for CCTV, presumably it is the pub status of the child which would be published on a pub node
        }
    }

    public SniPageExport(SniPage sniPage) {
        super(sniPage);
        initialize();
    }

    private void initialize() {
        
        LOG.info("Started Core Exports");
        if (sniPage == null || !sniPage.hasContent()) {
            return;
        }

        // Basic page properties.
        setProperty(ExportProperty.CORE_PAGE_TYPE.name(), sniPage.getPageType());
        setProperty(ExportProperty.CORE_BRAND.name(), sniPage.getBrand());
        Page sectionPage = sniPage.getAbsoluteParent(sniPage.getDepth() > 1 ? 2 : 1); // Consider putting this section nonsense in SniPage
        if (sectionPage != null) {
            setProperty(ExportProperty.CORE_SECTION_NAME.name(), sectionPage.getName()); // name, not title?
            if (sniPage.getBrand() != null) {
                setProperty(ExportProperty.CORE_SECTION_ID.name(), sniPage.getBrand() + "/" + sectionPage.getName()); // why is this brand/section?
            }
        }
        String pageUid = sniPage.getUid();
        setProperty(ExportProperty.CORE_ASSETUID.name(), pageUid);
        if (sniPage.getPath() != null) {
            // Path should only ever be nonnull in a testing situation.
            setProperty(ExportProperty.CORE_URL.name(), sniPage.getPath() + ".html");
        }
        setProperty(ExportProperty.CORE_FRIENDLY_URL.name(), sniPage.getFriendlyUrl());
        setProperty(ExportProperty.CORE_CRX_PATH.name(), sniPage.getProperties().get(PAGE_PROP_SNI_ASSET_LINK, String.class));
        setProperty(ExportProperty.CORE_FFID.name(), sniPage.getProperties().get(PAGE_PROP_SNI_FASTFWD_ID, Integer.class));

        // Tag properties.
        setProperty(ExportProperty.CORE_TAG_CRX_PATH.name(), sniPage.getProperties().get(PAGE_PROP_CQ_TAGS, String[].class));
        setProperty(ExportProperty.CORE_PRIMARY_TAG.name(), sniPage.getProperties().get(PAGE_PROP_SNI_PRIMARY_TAG, String.class));
        setProperty(ExportProperty.CORE_SECONDARY_TAG.name(), sniPage.getProperties().get(PAGE_PROP_SNI_SECONDARY_TAG, String.class));
        setProperty(ExportProperty.CORE_SOURCE.name(), sniPage.getProperties().get(PAGE_PROP_SNI_SOURCE, String.class));

        // Audit properties.
        setProperty(ExportProperty.CQ_LAST_MODIFIED.name(), getDateInString(sniPage.getLastModified()));
        setProperty(ExportProperty.CQ_LAST_REPLICATION_ACTION.name(), sniPage.getProperties().get(PAGE_PROP_CQ_LAST_REPLICATION_ACTION, String.class));
        setProperty(ExportProperty.CORE_RELEASE_DATE.name(), getDateInString(sniPage.getProperties().get(PAGE_PROP_CQ_LAST_REPLICATED, Calendar.class)));
        setProperty(ExportProperty.CORE_CREATION_DATE.name(), getDateInString(sniPage.getProperties().get(PAGE_PROP_JCR_CREATED, Calendar.class)));

        // Title/Image/Description properties.
        setProperty(ExportProperty.CORE_TITLE.name(), StringUtil.cleanToPlainText(sniPage.getTitle()));
        setProperty(ExportProperty.CORE_LONG_DESCRIPTION.name(), StringUtil.cleanToPlainText(sniPage.getDescription()));
        
        if(getCanonicalImagePath() != null && !getCanonicalImagePath().startsWith(HTTP_PROTOCOL))
        {	
        	setProperty(ExportProperty.CORE_IMAGE_PATH.name(), sniPage.getCanonicalImagePath());
        } else {
        	setProperty(ExportProperty.CORE_IMAGE_URL.name(), sniPage.getCanonicalImageUrl());
        }
        
        setProperty(ExportProperty.CORE_SHORT_TITLE.name(), sniPage.getShortTitle());
        
        // SEO properties.
        setProperty(ExportProperty.CORE_SEO_TITLE.name(), StringUtil.cleanToPlainText(sniPage.getSeoTitle()));
        setProperty(ExportProperty.CORE_SEO_DESCRIPTION.name(), StringUtil.cleanToPlainText(sniPage.getSeoDescription()));

        // Search flags
        setProperty(ExportProperty.CORE_DO_NOT_FEATURE.name(), sniPage.getProperties().get(PAGE_PROP_DO_NOT_FEATURE, false));
        setProperty(ExportProperty.CORE_DO_NOT_SEARCH.name(), sniPage.isDoNotSearch());
        
        // Abstraction Property
        setProperty(ExportProperty.CORE_ABSTRACT.name(), sniPage.getProperties().get(PAGE_PROP_SNI_ABSTRACT, String.class));

        // Package
        SniPackage sniPackage = sniPage.getSniPackage();
        boolean hasPackage = false;
        if (sniPackage != null) {
            SniPage packageAnchor = sniPackage.getPackageAnchor();
            setProperty(ExportProperty.CORE_PACKAGE_ID.name(), packageAnchor.getPath()); // Path, really? The name says ID!
            hasPackage = true;
        }

        // Hub
        Hub hub = sniPage.getHub();
        boolean isHubChild = false;
        if (hub != null) {
            SniPage master = hub.getHubMaster();
            HubMember masterHubMember = new HubMember(master);
            String hubMasterValue = masterHubMember.getValue();
            List<String> childrenValue = new ArrayList<String>();
            List<String> childrenPageIds = new ArrayList<String>();
            HubMember childHubMember;
            for (SniPage child : hub.getHubChildren()) {
            	childHubMember = new HubMember(child);
                childrenValue.add(childHubMember.getValue());
                childrenPageIds.add(childHubMember.getAssetUid());
            }
            if(childrenPageIds.contains(pageUid) || (masterHubMember.getAssetUid().equals(pageUid) && childrenPageIds.size() > 0)) {
            	isHubChild = true; 
                setProperty(ExportProperty.CORE_HUBMASTER.name(), hubMasterValue);
                setProperty(ExportProperty.CORE_HUBCHILD.name(), childrenValue.toArray(new String[childrenValue.size()]));
            }
            //set show uid when hub master is a show
            if(PageTypes.SHOW.name().equalsIgnoreCase(master.getPageType())) {
                setProperty(ExportProperty.CORE_SHOW_ID.name(), master.getUid());
            }
        }

        if (hub != null && isHubChild) {
            Boolean hubDoNotDisplay = false;
            HubButtonContainer hubButtonContainer = hub.getHubButtonContainer();
            if (hubButtonContainer != null) {
                List<HubButton> buttons = hubButtonContainer.getHubButtons();
                hubDoNotDisplay = buttons == null || buttons.size() < 1 || hasPackage;
            }
            setProperty(ExportProperty.CORE_HUB_DO_NOT_DISPLAY.name(), hubDoNotDisplay);
        }



        // Sponsorship/AdKey
        setProperty(ExportProperty.CORE_ADKEY.name(), sniPage.getProperties().get(PAGE_PROP_SNI_ADKEY, String.class));
        SponsorshipManager sponsorshipManager = sniPage.getSponsorshipManager();
        if (sponsorshipManager != null) {
            SponsorshipProvider sponsorshipProvider = sponsorshipManager.getEffectiveSponsorshipProvider();

            if (sponsorshipProvider != null) {
                setProperty(ExportProperty.CORE_SPONSOR_CODE.name(), sponsorshipProvider.getSponsorshipValue());
                setProperty(ExportProperty.CORE_ADKEY.name(), sponsorshipProvider.getProvider().getProperties().get(PAGE_PROP_SNI_ADKEY, String.class));
            }
        }

        /** These might be show-specific, but they're driven by direct page properties. */
        setProperty(ExportProperty.CORE_SHOW_ABBREVIATION.name(), sniPage.getProperties().get(PAGE_PROP_SNI_SHOW_ABBREVIATION, String.class));
        setProperty(ExportProperty.CORE_TUNE_IN_TIME.name(), sniPage.getProperties().get(PAGE_PROP_SNI_TUNE_IN_TIME, String.class));
        // Eeew, this should be encapsulated somewhere.
        String talentPath = sniPage.getProperties().get(PAGE_PROP_SNI_PRIMARY_TALENT, String.class);
        if (talentPath != null) {
            // Theoretically this is always an asset path, but let's be sure.
            if (talentPath.startsWith("/etc")) {
                SniPage talentPage = getContentPageFromAssetPath(talentPath, sniPage.getPageManager(), sniPage.getPath());
                if (talentPage != null) {
                   setProperty(ExportProperty.CORE_TALENT_ID.name(), talentPage.getUid());
                   setProperty(ExportProperty.CORE_TALENT_URL.name(), talentPage.getPath() + ".html");
                   setProperty(ExportProperty.CORE_TALENT_FRIENDLY_URL.name(), talentPage.getFriendlyUrl());
                   setProperty(ExportProperty.CORE_TALENT_NAME.name(), talentPage.getTitle());
               }
            } // else it's a content path, currently unhandled
        }
        
        /**
         * Iterating through the paths to retrieve the sni:assetUid value and set the property
         */
        setProperty(ExportProperty.CORE_PEOPLE.name(), sniPage.getProperties().get(PAGE_PROP_SNI_PEOPLE, String[].class));
        String[] sniPeople = sniPage.getProperties().get(PAGE_PROP_SNI_PEOPLE, String[].class);
        if(sniPeople != null) {
            String talentPageAssetUId;
            List<String> talentPageIds = new ArrayList<String>();
            for(String talentAssetPath : sniPeople) {
                talentPageAssetUId = getSniPageAssetUId(talentAssetPath);
                if(talentPageAssetUId != null) {
                    talentPageIds.add(talentPageAssetUId);    
                }
            }
            if(talentPageIds.size() > 0) {
                setProperty(ExportProperty.CORE_PEOPLE.name(), talentPageIds.toArray(new String[talentPageIds.size()]));
            }
        }
        
        /* Set the property for Sponsorship Image and Sponsorship Image Url. */
        SniPage tagPage = sniPage.getSourcePage();
        if(tagPage != null) {
            ValueMap tagPageProperties = tagPage.getProperties();
            if(tagPageProperties != null) {
                setProperty(ExportProperty.CORE_SOURCE_IMAGEPATH.name(), tagPageProperties.get(PagePropertyConstants.PROP_SNI_TAG_IMAGE, String.class));
                
                String sourceLinkUrl = tagPageProperties.get(PagePropertyConstants.PROP_SNI_TAG_LINKURL, String.class);
				if (sourceLinkUrl != null && !sourceLinkUrl.startsWith(HTTP_PROTOCOL)) {
					SiteConfigService siteConfig = sniPage.getSiteConfigService();
					if (siteConfig != null) {
						sourceLinkUrl = HTTP_PROTOCOL + siteConfig.getDomain() + getFriendlyUrl(sourceLinkUrl);
					}
				}
				setProperty(ExportProperty.CORE_SOURCE_URL.name(), sourceLinkUrl);
            }
        }
        setProperty(ExportProperty.CORE_ALTERNATE_URL.name(), sniPage.getCanonicalUrl());
        LOG.info("Completed Core Exports");
        
    }
    
    String getFriendlyUrl(String url) {
    	if(url != null) {
    	    return PathHelper.unBucketPath(url).replaceFirst(CONTENT_ROOT + SLASH + sniPage.getBrand(), "") + ".html";
    	}
        return null;
    }

    /** Returns the "canonical" image path. */
    String getCanonicalImagePath() {
        if (sniPage == null) {
            return null;
        }
        return sniPage.getCanonicalImagePath();
    }
    
    /**
     * This method is used to retrieve the assetUid for the corresponding page
     * @param assetPath
     * @return
     */
    String getSniPageAssetUId(String assetPath) {
        if (assetPath == null || assetPath.isEmpty()) {
            return null;
        }

        PageManager pageManager = sniPage.getPageManager();
        if (pageManager == null) {
            return null;
        }
        Page page = pageManager.getPage(assetPath);
        
        if (page != null && page.hasContent()) {
            SniPage assetSniPage = PageFactory.getSniPage(page);
            return assetSniPage.getUid();
        } else {
            return null;
        }
    }

    public static SniPage getContentPageFromAssetPath(String assetPath, PageManager pageManager, String thisPagePath) {
        if (assetPath == null || assetPath.isEmpty() || pageManager == null || thisPagePath == null || thisPagePath.isEmpty()) {
            return null;
        }

        Page assetPage = pageManager.getPage(assetPath);
        Page contentPage = null;

        if (assetPage != null) {
            String[] pages = assetPage.getProperties().get(PAGE_PROP_SNI_PAGE_LINKS, String[].class);
            if (pages != null && pages.length > 0) {
                String pagePath = null;
                String pathPrefix = Text.getAbsoluteParent(thisPagePath, 1);
                for (String p : pages) {
                    if (p.startsWith(pathPrefix)) {
                        pagePath = p;
                        break;
                    }
                }
                if (pagePath != null && !pagePath.isEmpty()) {
                    contentPage = pageManager.getPage(pagePath);
                }
            }
        }

        if (contentPage != null && contentPage.hasContent()) {
            return PageFactory.getSniPage(contentPage);
        } else {
            return null;
        }
    }

    protected static String escapeCompositeValue(String str) {
        if (str == null) {
            return null;
        }
        return str.replace(COMPOSITE_VALUE_DELIMITER, COMPOSITE_VALUE_REPLACEMENT);
    }

    private static HubButton getHubButton(List<HubButton> hubButtons, SniPage page) {
        if (hubButtons == null || page == null) {
            return null;
        }
        for (HubButton button : hubButtons) {
            String path = button.getPagePath();
            if (path != null && path.equals(page.getPath())) {
                return button;
            }
        }
        return null;
    }
    
    /**
     * The Method loops through the page list and returns the assetUid List.
     * 
     * @param pageList
     * @return
     */
    public List<String> getSniPageIds(List<SniPage> pageList) {
        List<String> pageIds = null;
        if (pageList != null && pageList.size() > 0) {
            pageIds = new ArrayList<String>();
            for (SniPage sniPage : pageList) {
                if (sniPage.getUid() != null) {
                    pageIds.add(sniPage.getUid());
                }
            }
        }
        if (pageIds != null && pageIds.size() > 0) {
            return pageIds;
        }
        return null;
    }
    
    /**
     * This methods returns date in the format yyyymmdd.
     * @param calendar
     * @return
     */
    public static String getDateInString(Calendar calendar) {
        if (calendar != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            return sdf.format(calendar.getTime());
        }
        return null;
    }
    
   
}



