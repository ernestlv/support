package com.scrippsnetworks.wcm.page.impl;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.asset.DataUtil;
import com.scrippsnetworks.wcm.breadcrumb.Breadcrumb;
import com.scrippsnetworks.wcm.breadcrumb.BreadcrumbFactory;
import com.scrippsnetworks.wcm.canonicalimage.CanonicalImage;
import com.scrippsnetworks.wcm.canonicalimage.CanonicalImageFactory;
import com.scrippsnetworks.wcm.config.SiteConfigService;
import com.scrippsnetworks.wcm.fnr.util.OsgiHelper;
import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.hub.HubFactory;
import com.scrippsnetworks.wcm.hub.HubUtil;
import com.scrippsnetworks.wcm.hub.button.HubButtonContainer;
import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.metadata.MetadataManager;
import com.scrippsnetworks.wcm.metadata.MetadataManagerFactory;
import com.scrippsnetworks.wcm.metadata.SponsorshipManager;
import com.scrippsnetworks.wcm.metadata.SponsorshipManagerFactory;
import com.scrippsnetworks.wcm.metadata.impl.MetadataUtil;
import com.scrippsnetworks.wcm.opengraph.OpenGraph;
import com.scrippsnetworks.wcm.opengraph.OpenGraphFactory;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.section.Section;
import com.scrippsnetworks.wcm.section.SectionFactory;
import com.scrippsnetworks.wcm.seo.SeoDescription;
import com.scrippsnetworks.wcm.seo.SeoDescriptionFactory;
import com.scrippsnetworks.wcm.seo.SeoTitle;
import com.scrippsnetworks.wcm.seo.SeoTitleFactory;
import com.scrippsnetworks.wcm.snipackage.SniPackage;
import com.scrippsnetworks.wcm.snipackage.SniPackageFactory;
import com.scrippsnetworks.wcm.snitag.SniTag;
import com.scrippsnetworks.wcm.snitag.SniTagFactory;
import com.scrippsnetworks.wcm.url.impl.PathHelper;
import com.scrippsnetworks.wcm.util.MergingResourceWrapper;
import com.scrippsnetworks.wcm.util.MergingValueMap;
import com.scrippsnetworks.wcm.util.PageWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.scrippsnetworks.wcm.page.PagePropertyConstants.*;

/** SNI page implementation.
 *
 * By wrapping the CQ page, this class can provide transparent access to asset properties if the page is backed with an asset
 * page, with priority given to the same-name properties on the content page. Methods are overridden in this class so access
 * to page properties or resources will automatically provide the merging behavior. If the page is not an asset page, the
 * class functions as a pass-through to the CQ Page.
 *
 * @author Scott Everett Johnson et al
 * @see Page
 */
public class SniPageImpl extends PageWrapper implements SniPage {

    private static final Logger log = LoggerFactory.getLogger(SniPageImpl.class);
    /** The source page for a launch if this SniPage is a launch page.  current SniPage  otherwise. */
    private SniPage launchSourceSniPage = null;
    /** The backing CQ Page. */
    private Page contentPage = null;
    /** The backing asset page. */
    private Page assetPage = null;
    /** Cache of retrieved child resources. */
    private HashMap<String, Resource> cachedResources = new HashMap<String, Resource>();
    /** The merged content resource for the page. */
    private Resource contentResource = null;
    /** The merged properties for this page */
    private ValueMap mergedProperties = null;
    /** Tag Page Properties. */
    private ValueMap tagPageProperties = null;

    /** SlingHttpServletRequest for this SniPage. */
    private SlingHttpServletRequest slingRequest;

    /** The ResourceResolver for the SniPage */
    private ResourceResolver resourceResolver;

    /** The brand extracted from the path */
    private String brand;

    /** The page's hub (if any) */
    private Hub hub;

    /** The page's HubButtonContainer */
    private HubButtonContainer hubButtonContainer;

    /** The page's own package anchor (if any) */
    private SniPage packageAnchor;

    private String bannerLink;

    /** The page's canonical image (if any) */
    private String canonicalImagePath;
    private String canonicalImageUrl;

    /** The pages's package object (if the relationship is defined.) */
    private SniPackage sniPackage;

    /** The page's metadata manager */
    private MetadataManager metadataManager = null;

    /** The page's metadata manager */
    private SponsorshipManager sponsorshipManager = null;

    /** The page's OpenGraph data */
    private OpenGraph openGraph;

    /** An OsgiHelper instance */
    private OsgiHelper osgiHelper;
    
    /** A section object, used to get sectionName and sectionDisplayName. */
    private Section section;
    
    /** The page's Breadcrumb data */
    private Breadcrumb breadcrumb;    

    /** SEO Title and Description */
    private String seoTitle;
    private String seoDescription;

    /** Short Title. */
    private String shortTitle;

    /** Hub Count */
    private Integer hubCount;

    /** Deep Linking Page Number */
    private Integer deepLinkPageNumber;

    /** SniTag objects built from the cq:tags on the page. */
    private List<SniTag> sniTags;
    private List<SniTag> categoryTags;

    /** from sni:primaryTag */
    private SniTag primarySniTag;

    /** from sni:secondaryTag */
    private SniTag secondarySniTag;

    boolean hubInitialized = false;
    boolean packageAnchorInitialized = false;
    boolean packageInitialized = false;
    
    /** The sourcePage from SponsorShip. */
    private SniPage sourcePage;

    private String url;
    private String friendlyUrl;

    private String title;
    private String description;

    private List<String> pageSelectors;

    /** Used for identifying /content/brand part of URL. */
    private String urlPrefix;
    
    private SiteConfigService siteConfig = null;

    private SniImage canonicalImage;
    private boolean canonicalImageSet = false;

    private static final String HTTP_PROTOCOL = "http://";

    /** Construct a new SniPage given a Page object.
     *
     * @param contentPage the page to wrap with SNI page functionality
     * @throws IllegalArgumentException if the contentPage parameter is null
     */
    public SniPageImpl(Page contentPage) {
        this(contentPage, null);
    }

    /** Construct a new SniPage given a Page object.
     *
     * @param contentPage the page to wrap with SNI page functionality
     * @param ssh For getting the SlingRequest object, externalizer, and any other OSGi services needed at runtime.
     * @throws IllegalArgumentException if the contentPage parameter is null
     */
    public SniPageImpl(Page contentPage, SlingScriptHelper ssh) {
        super(contentPage);
        this.contentPage = contentPage;
        //there is not always a contentResource
        // For example, if this is on a publish node where the requested
        // resource is an ancestor of the page you're on
        // and if the anscestor is not published, youll get null for getContentResource
        if(contentPage.getContentResource()!=null){
            this.resourceResolver = contentPage.getContentResource().getResourceResolver();
        }

        if (ssh != null) {
            this.slingRequest = ssh.getRequest();
        } else {
            this.slingRequest = null;
        }

        PageManager pm = contentPage.getPageManager();
        if (pm == null) {
            // This is not supposed to happen, if it does it's truly exceptional.
            throw new RuntimeException("got null PageManager from page at " + contentPage.getPath());
        }

        String assetPath = contentPage.getProperties().get(PROP_SNI_ASSETLINK, String.class);
        if (assetPath != null && !assetPath.isEmpty()) {
            assetPage = pm.getPage(assetPath);
        }

        this.brand = extractBrand();

        this.urlPrefix = "/content/" + brand;

        this.launchSourceSniPage = getLaunchSourceSniPage();
    }

    /** Returns a page content resource whose properties are transparently merged with those of any backing asset page.
     *
     * @see MergingResourceWrapper
     */
    @Override
    public Resource getContentResource() {

        if (assetPage != null && assetPage.hasContent()) {

            if (contentResource != null) {
                return contentResource;
            }

            Resource contentRes = contentPage.getContentResource();
            Resource assetRes = assetPage.getContentResource();

            // contentPage is assumed to have a content resource because we read assetPath from it
            // assetPage's hasContent was checked in the conditional above
            // So we assume both are nonnull here
            contentResource = new MergingResourceWrapper(contentRes, assetRes);
            return contentResource;
        }

        return super.getContentResource();
    }

    /** Returns a child content resource whose properties are transparently merged with those of the same child resource of a
     * backing asset page.
     *
     * @see MergingResourceWrapper
     */
    @Override public Resource getContentResource(String relPath) {
        if (relPath == null || relPath.isEmpty()) {
            return super.getContentResource(relPath);
        }

        if (assetPage != null && assetPage.hasContent()) {

            if (cachedResources.containsKey(relPath)) {
                return cachedResources.get(relPath);
            }

            Resource contentResource = contentPage.getContentResource(relPath);
            Resource assetResource = assetPage.getContentResource(relPath);
            Resource retVal = null;
            if (contentResource != null && assetResource != null) {
                retVal = new MergingResourceWrapper(contentResource, assetResource);
            } else if (contentResource != null && assetResource == null) {
                retVal = contentResource;
            } else if (contentResource == null && assetResource != null) {
                retVal = assetResource;
            }

            cachedResources.put(relPath, retVal);

            return retVal;
        }
        return super.getContentResource(relPath);
    }

    /** Returns properties transparently merged with those of a backing asset page.
     *
     * @see MergingValueMap
     */
    @Override
    public ValueMap getProperties() {
        if (mergedProperties == null) {
            Resource contentResource = getContentResource();
            if (contentResource != null) {
                mergedProperties = contentResource.adaptTo(ValueMap.class);
                if (mergedProperties == null) {
                    mergedProperties = ValueMap.EMPTY;
                }
            } else {
                mergedProperties = ValueMap.EMPTY;
            }
        }
        return mergedProperties;
    }

    /** Returns child resource properties transparently merged with those of a backing asset page's child resource.
     *
     * @see MergingValueMap
     */
    public ValueMap getProperties(String relPath) {
        Resource res = getContentResource(relPath);
        if (res != null) {
            return res.adaptTo(ValueMap.class);
        } else {
            return ValueMap.EMPTY;
        }
    }

    /** Returns the page's asset page, or null. */
    public Page getAssetPage() {
        return assetPage;
    }

    /** Returns the page's Hub, or null. */
    public Hub getHub() {
        if (!hubInitialized) {
            hub = new HubFactory()
                    .withSniPage(this)
                    .build();
            hubInitialized = true;
        }
        return hub;
    }

    /** Returns the page's package anchor.
     */
    public SniPage getPackageAnchor() {
        SniPackage thePkg = getSniPackage();
        if (thePkg != null) {
            return thePkg.getPackageAnchor();
        } else {
            return null;
        }
    }

    /** Retrieve the page's canonical image URL.
     */
    public String getCanonicalImagePath() {
        if (canonicalImagePath == null) {
            SniImage can = getCanonicalImage();
            if (can != null) {
                canonicalImagePath = can.getPath();
            }
        }
        return canonicalImagePath;
    }

    /** Retrieve the page's canonical image URL.
     */
    public String getCanonicalImageUrl() {
        if (canonicalImageUrl == null) {
            SniImage can = getCanonicalImage();
            if (can != null) {
                canonicalImageUrl = can.getUrl();
            }
        }
        return canonicalImageUrl;
    }

    /** Retrieve the canonical SniImage for the page.
     */
    public SniImage getCanonicalImage() {
        if (!canonicalImageSet) {
            CanonicalImage canon = new CanonicalImageFactory()
                .withSniPage(this)
                .withDefaultImage(false)
                .build();
            if (canon != null) {
                canonicalImage = canon.getImage();
                canonicalImageSet = true;
            }
        }
        return canonicalImage;
    }

    /** Returns an SniPackage object for the current page.
     *
     */
    public SniPackage getSniPackage() {
        if (!packageInitialized) {
            sniPackage = SniPackageFactory.getSniPackage(this);
            packageInitialized = true;
        }
        return sniPackage;
    }

    /** Returns the page's MetadataManager */
    public MetadataManager getMetadataManager() {
        if (metadataManager == null) {
            metadataManager = MetadataManagerFactory.getMetadataManager(this);
        }
        return metadataManager;
    }

    /** Returns the page's SponsorshipManager */
    public SponsorshipManager getSponsorshipManager() {
        if (sponsorshipManager == null) {
            sponsorshipManager = SponsorshipManagerFactory.getSponsorshipManager(this);
        }
        return sponsorshipManager;
    }

    /** Returns the page's OpenGraph object */
    public OpenGraph getOpenGraph() {
        if (openGraph == null) {
            openGraph = OpenGraphFactory.createOpenGraph(this);
        }
        return openGraph;
    }

    /** Returns the page's Breadcrumb object */
    public Breadcrumb getBreadcrumb() {
        if (breadcrumb == null) {
            breadcrumb = new BreadcrumbFactory()
                .withSniPage(this)
                .build();
        }
        return breadcrumb;
    }

    /** Returns the SEO Page Title */
    public String getSeoTitle() {
        if (seoTitle == null) {
            SeoTitle st = new SeoTitleFactory()
                .withSniPage(this)
                .build();
            seoTitle = st.getTitle();
        }

        return seoTitle;
    }

    /** Returns the SEO Page Description */
    public String getSeoDescription() {
        if (seoDescription == null) {
            SeoDescription sd = new SeoDescriptionFactory()
                .withSniPage(this)
                .build();
            seoDescription = sd.getDescription();
        }

        return seoDescription;
    }

    /** Returns the page's friendly URL. */
    @Override
    public String getFriendlyUrl() {
        if (friendlyUrl == null) { 	
        	if (getPath().equals("/content/"+brand+"/home")){
        		friendlyUrl="";
        	}
        	else
        	{	
        		friendlyUrl = PathHelper.unBucketPath(getPath()).replaceFirst(urlPrefix, "") + ".html";
        	}
        }
        return friendlyUrl;
    }

    /** Returns the page's URL. */
    @Override
    public String getUrl() {
        if (url == null) {
            url = getPath() + ".html";
        }
        return url;
    }    

    /** Returns the page type, derived from the resource type, or null if not set. */
    public String getPageType() {
        String retVal = DataUtil.getPageType(contentPage); // explicitly use content page
        // DataUtil can return an empty string, we'll prefer null
        if (retVal == null || retVal.isEmpty()) {
            retVal = null;
        }
        return retVal;
    }

    /** Returns the page's asset id, or null if not set. */
    public String getUid() {
        // SniPageImpl construction fails on null contentPage, and getProperties always returns map by contract
        return contentPage.getProperties().get(PROP_SNI_ASSETUID, String.class);
    }

    /** Returns the page's brand, or null if the brand could not be determined from the path. */
    public String getBrand() {
        return brand;
    }

    /** Used to calculate the page's brand. */
    private String extractBrand() {
        String path = contentPage.getPath();
        return MetadataUtil.getSiteName(path);
    }

    /**
     * This is the count that appears on Hub Buttons and other places. This getter
     * is in the SniPage because the counts are very page-specific, also used in
     * places other than hubs.
     * @return Integer hub count
     */
    @Override
    public Integer getHubCount() {
        return HubUtil.getHubCount(this);
    }

    /**
     * Retrieve the SlingHttpServletRequest object if there is one (can return null).
     * @return SlingHttpSerlvetRequest or null.
     */
    @Override
    public SlingHttpServletRequest getSlingRequest() {
        return slingRequest;
    }


    /**
     * Return the page number from the sling request, if any. Can return null.
     * @return Integer page number, or null.
     */
    @Override
    public Integer getDeepLinkPageNumber() {
        if (deepLinkPageNumber == null) {
            if (slingRequest == null) {
                return null;
            }
            int output = 1;
            RequestPathInfo pathInfo = slingRequest.getRequestPathInfo();
            Pattern pattern = Pattern.compile("^page-([0-9]+)$");
            for (String selector : pathInfo.getSelectors()) {
                Matcher matcher = pattern.matcher(selector);
                if (matcher.matches()) {
                    output = Integer.valueOf(matcher.group(1));
                    break;
                }
            }
            //checking for zero, because I know a QA person is going to do that on purpose
            deepLinkPageNumber = output > 0 ? output : 1;
        }
        return deepLinkPageNumber;
    }

    @Override
    public Section getSection() {
        if (section == null) {
            section = new SectionFactory().withSniPage(this).build();
        }
        return section;
    }
    
    /**
     * Return the selectors from the sling request, if any; can return null.
     * @return List<String>, or null
     */
    @Override
    public List<String> getSelectors() {
        if (pageSelectors == null) {
            if (slingRequest == null) {
                return null;
            }
            RequestPathInfo pathInfo = slingRequest.getRequestPathInfo();
            pageSelectors = Arrays.asList(pathInfo.getSelectors());
        }
        return pageSelectors;
    }

    /** Returns the Page Title */
    @Override
    public String getTitle() {
        if (title == null) {
            String jcrTitle = contentPage.getProperties().get(PROP_JCR_TITLE, String.class);
            String sniTitle = contentPage.getProperties().get(PROP_SNI_TITLE, String.class);
            title = (StringUtils.isNotBlank(sniTitle)) ? sniTitle : jcrTitle;
        }
        return title;
    }    

    /** Returns the Page Description */
    @Override
    public String getDescription() {
        if (description == null) {
            String jcrDescription = contentPage.getProperties().get(PROP_JCR_DESCRIPTION, String.class);
            String sniDescription = contentPage.getProperties().get(PROP_SNI_DESCRIPTION, String.class);
            description = (StringUtils.isNotBlank(sniDescription)) ? sniDescription : jcrDescription;
        }
        return description;
    }

    /** {@inheritDoc} */
    @Override
    public String getShortTitle() {
        if(shortTitle == null) {
            shortTitle = contentPage.getProperties().get(PAGE_SNI_SHORT_TITLE, String.class);
        }
        return shortTitle;
    }

    /** {@inheritDoc} */
    @Override
    public List<SniTag> getSniTags() {
        if (sniTags == null) {
            sniTags = new ArrayList<SniTag>();
            SniTag primary = getPrimarySniTag();
            if (primary != null) {
                sniTags.add(primary);
            }
            SniTag secondary = getSecondarySniTag();
            if (secondary != null) {
                sniTags.add(secondary);
            }
            String[] cqTags = getProperties().get(PROP_CQ_TAGS, String[].class);
            if (cqTags != null && cqTags.length > 0) {
                for (String tag : cqTags) {
                    SniTag processedSniTag = new SniTagFactory()
                            .withTagText(tag)
                            .withSniPage(this)
                            .build();
                    if (processedSniTag != null) {
                        sniTags.add(processedSniTag);
                    }
                }
            }
        }
        return sniTags;
    }

    /** {@inheritDoc} */
    public List<SniTag> getCategoryTags() {
        if (categoryTags == null) {
            categoryTags = new ArrayList<SniTag>();
            List<String> rawTags = new ArrayList<String>();
            for (SniTag tag : getSniTags()) {
                if ((!rawTags.contains(tag.getRawTag()) &&
                    tag.getTopicPage() != null &&
                    tag.getTopicPage().isValid())) {
                    categoryTags.add(tag);
                    rawTags.add(tag.getRawTag());
                }
            }
        }
        return categoryTags;
    }

    /** {@inheritDoc} */
    @Override
    public SniTag getPrimarySniTag() {
        if (primarySniTag == null) {
            String primaryTag = getProperties().get(PROP_SNI_PRIMARY_TAG, String.class);
            if (StringUtils.isNotBlank(primaryTag)) {
                primarySniTag = new SniTagFactory()
                        .withTagText(primaryTag)
                        .withSniPage(this)
                        .build();
            }
        }
        return primarySniTag;
    }

    /** {@inheritDoc} */
    @Override
    public SniTag getSecondarySniTag() {
        if (secondarySniTag == null) {
            String secondaryTag = getProperties().get(PROP_SNI_SECONDARY_TAG, String.class);
            if (StringUtils.isNotBlank(secondaryTag)) {
                secondarySniTag = new SniTagFactory()
                        .withTagText(secondaryTag)
                        .withSniPage(this)
                        .build();
            }
        }
        return secondarySniTag;
    }

    /** {@inheritDoc} */
    @Override
    public Boolean isPackageAnchor() {
        SniPage pkgPage = getPackageAnchor();
        if (pkgPage != null) {
            return pkgPage.getPath().equals(this.getPath()); 
        } else {
            return false;
        }
    }

    @Override
    public String getLargeBannerImage() {
        return getProperties().get(PROP_SNI_LARGE_BANNER_IMAGE, String.class);
    }

    @Override
    public String getSocialTag() {
        return getProperties().get(PROP_SNI_SOCIAL_TAG, String.class);
    }

    @Override
    public String getTuneInTime() {
        return getProperties().get(PROP_SNI_TUNE_IN_TIME, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getSource() {
        return getProperties().get(PROP_SNI_SOURCE, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public SniPage getSourcePage() {
        if(sourcePage == null) {
            String source = getProperties().get(PROP_SNI_SOURCE, String.class);
            if(source != null) {
                SniTag tag = new SniTagFactory()
                        .withTagText(source)
                        .withSniPage(this)
                        .build();
                if (tag != null) {
                    sourcePage = tag.getTagMetadataPage();
                }
            }
        }
        return sourcePage;
    }

    /** {@inheritDoc} */
    @Override
    public String getCanonicalUrl() {
        SiteConfigService siteConfig = getSiteConfigService();
        if (siteConfig != null) {
            return HTTP_PROTOCOL + siteConfig.getDomain() + getFriendlyUrl();
        }
        return null;
    }

    /**
     * This method is used to get the SiteConfigService.
     * As sling request is not available in the exports.
     * @return
     */
    public SiteConfigService getSiteConfigService() {
        if (siteConfig == null) {
            siteConfig = getService(SiteConfigService.class);
        }
        return siteConfig;
    }
    
    private <T> T getService(Class<T> clazz) {
        T retVal = null;
        if (osgiHelper == null) {
            osgiHelper = new OsgiHelper();
        }
        if (osgiHelper != null) {
            log.info("************************** SniPageImpl getService brand is " + brand + " ******************************");
            retVal = osgiHelper.getOsgiServiceBySite(clazz.getName(), brand);
        }
        return retVal;
    }
    
    /**
     * This method is used to determine the value of search property based on
     *  doNotSearch and searchable attribute values.
     * @return
     */
    public boolean isDoNotSearch() {
        Boolean doNotSearch = getProperties().get(PROP_SNI_DO_NOT_SEARCH, Boolean.class);
        if (doNotSearch != null) return doNotSearch;
        String searchable = getProperties().get(PROP_SNI_SEARCHABLE, String.class);
        if ("undefined".equals(searchable)){
            return false;
        }
        doNotSearch = getProperties().get(PROP_SNI_SEARCHABLE, Boolean.class);
        if (doNotSearch != null) return !doNotSearch;
        return false; // return the default value for sni:doNotSearch
    }

    @Override
    public String getBannerLink(){
        if (bannerLink != null){
            return bannerLink;
        }

        SniPage thePkgAnch = getPackageAnchor();
        bannerLink = "";

        if (StringUtils.isNotEmpty(getProperties().get("bannerLink", ""))){
            bannerLink = getProperties().get("bannerLink", "");
        } else if (thePkgAnch != null) {
            ValueMap packageVm = thePkgAnch.getProperties();
            if (packageVm.containsKey("bannerLink")){
                bannerLink = packageVm.get("bannerLink", "");
            } else {
                bannerLink = thePkgAnch.getPath();
            }
        }

        if (StringUtils.isEmpty(bannerLink)){
            Hub hub = getHub();
            if (hub != null){
                SniPage hubMaster = hub.getHubMaster();
                if (hubMaster != null){
                    bannerLink = hubMaster.getPath();
                }
            }
        }

        if (getPath() != null && getPath().equals(bannerLink)){
            bannerLink = "";
        }

        return bannerLink;

    }
    public boolean isLaunchPage() {
        String path = this.contentPage.getPath();
        if (path != null && !path.isEmpty())
            return path.startsWith(MetadataUtil.LAUNCHES_PATH);
        return false;
    }

    /**
     * This method determines the source path of the source page of launch pages
     * @return
     */
    public SniPage getLaunchSourceSniPage() {
        if (!isLaunchPage()) return this;

        String originalPath = this.contentPage.getPath();
        originalPath = MetadataUtil.getSourcePagePath(originalPath);

        return PageFactory.getSniPage(this.getPageManager(), originalPath);
    }
}
