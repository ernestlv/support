package com.scrippsnetworks.wcm.metadata.impl.provider;

import static com.scrippsnetworks.wcm.metadata.MetadataProperty.DETAILID;
import static com.scrippsnetworks.wcm.metadata.MetadataProperty.FASTFWDID;
import static com.scrippsnetworks.wcm.metadata.MetadataProperty.HUBID;
import static com.scrippsnetworks.wcm.metadata.MetadataProperty.HUBPATH;
import static com.scrippsnetworks.wcm.metadata.MetadataProperty.HUBTYPE;
import static com.scrippsnetworks.wcm.metadata.MetadataProperty.IMGURL;
import static com.scrippsnetworks.wcm.metadata.MetadataProperty.PACKAGENAME;
import static com.scrippsnetworks.wcm.metadata.MetadataProperty.PAGENUMBER;
import static com.scrippsnetworks.wcm.metadata.MetadataProperty.PUBLISHTIME;
import static com.scrippsnetworks.wcm.metadata.MetadataProperty.SHOWABBR;
import static com.scrippsnetworks.wcm.metadata.MetadataProperty.TALENTNAME;
import static com.scrippsnetworks.wcm.metadata.MetadataProperty.TITLE;
import static com.scrippsnetworks.wcm.metadata.MetadataProperty.TYPE;
import static com.scrippsnetworks.wcm.metadata.MetadataProperty.UNIQUEID;
import static com.scrippsnetworks.wcm.metadata.MetadataProperty.URL;
import static com.scrippsnetworks.wcm.metadata.MetadataProperty.CANONICALURL;

import java.util.Arrays;
import java.util.List;

import com.scrippsnetworks.wcm.episode.Episode;
import com.scrippsnetworks.wcm.episode.EpisodeFactory;
import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.metadata.MetadataProperty;
import com.scrippsnetworks.wcm.metadata.MetadataProvider;
import com.scrippsnetworks.wcm.metadata.impl.MetadataUtil;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.Recipe;
import com.scrippsnetworks.wcm.recipe.RecipeFactory;
import com.scrippsnetworks.wcm.show.Show;
import com.scrippsnetworks.wcm.show.ShowFactory;
import com.scrippsnetworks.wcm.talent.Talent;
import com.scrippsnetworks.wcm.talent.TalentFactory;
import com.scrippsnetworks.wcm.util.PageTypes;

public class BaseContentMetadataProvider implements MetadataProvider {

    public static final String DEFAULT_SITE_NAME = "unknown";
    public static final String DEFAULT_PAGE_TYPE = "page";
    public static final String DEFAULT_DETAIL_ID = "unknown";
    public static final String DEFAULT_HUB_PATH = "unknown";
 
    private SniPage page;
    private String siteName = null;
    private String title = null;
    private String pageNumber = "1";
    private String pageType = null;
    private String detailId = null;
    private String uniqueId = null;
    private String fastFwdId = null;
    private String publishTime = null;
    private String url = null;
    private String hubPkgId = null;
    private String hubType = null;
    private String hubPath = null;
    private String pkgName = null;
    private String talentName = null;
    private String showAbbr = null;
    private String imgUrl = null;
    private String canonicalUrl=null;

    public BaseContentMetadataProvider(SniPage page) {
        if (page == null) {
            throw new IllegalArgumentException("page must not be null");
        }

        if (page.getDepth() < 1) {
            throw new IllegalArgumentException("page must be at least at site level");
        } 
        this.page = page;
        this.siteName = MetadataUtil.getSiteName(page);
        this.pageType = MetadataUtil.getResourceType(page);
        if (this.pageType == null || this.pageType.isEmpty()) {
            this.pageType = DEFAULT_PAGE_TYPE;
        }
        this.url = page.getFriendlyUrl();
        this.canonicalUrl = page.getCanonicalUrl();
        this.title = MetadataUtil.getTitle(page);
        this.publishTime = MetadataUtil.getPublishTime(page);

        if (page.getDeepLinkPageNumber() != null) {
            this.pageNumber = page.getDeepLinkPageNumber().toString();
        }

        this.hubPath = MetadataUtil.getHubPath(page);
        if (this.hubPath.isEmpty()) {
            this.hubPath = DEFAULT_HUB_PATH;
        }

        this.detailId = page.getProperties().get(PagePropertyConstants.PROP_SNI_ASSETUID, String.class);
        this.uniqueId = String.format("%s|%s|%s|%s",
                this.siteName != null ? this.siteName : DEFAULT_SITE_NAME,
                this.pageType,
                this.detailId != null ? this.detailId : DEFAULT_DETAIL_ID,
                this.pageNumber);
        this.fastFwdId = page.getProperties().get(PagePropertyConstants.PROP_SNI_FASTFWDID, String.class);
        
        Hub hub = page.getHub();
        SniPage hubPackage = null;
        
        if (hub != null) {
        	hubPackage = hub.getHubMaster().getPackageAnchor();
            this.hubPkgId = hub.getHubMaster().getProperties().get(PagePropertyConstants.PROP_SNI_ASSETUID, String.class);
            this.hubType = MetadataUtil.getResourceType(page);
        } else if (page.getPackageAnchor() != null) {
            this.hubPkgId = page.getPackageAnchor().getProperties().get(PagePropertyConstants.PROP_SNI_ASSETUID, String.class);
        }
        
        if (hub != null && hubPackage != null) {
        	this.pkgName = hubPackage.getTitle();
        } else if (this.pkgName == null && page.getPackageAnchor() != null) {
        	this.pkgName = page.getPackageAnchor().getTitle(); 
        }
        
        this.talentName = getTalent();
        this.showAbbr = page.getProperties().get(PagePropertyConstants.PROP_SNI_SHOWABBR, String.class); 
        this.imgUrl = page.getCanonicalImageUrl();
    }

    public List<MetadataProperty> provides() {
        return Arrays.asList(DETAILID, FASTFWDID, HUBID, HUBTYPE, HUBPATH, IMGURL, PACKAGENAME, TITLE, TYPE,
            UNIQUEID, URL,CANONICALURL, PAGENUMBER, PUBLISHTIME, SHOWABBR, TALENTNAME);
    }

    private String getTalent() {
        String retVal = "";
        SniPage parentPage = null;
        SniPage talentPage = null;

        PageTypes type = PageTypes.findPageType(pageType);
        if (type != null) {
            switch (type) {
                case ASSET_RECIPES:
                case BIO:
                    parentPage = PageFactory.getSniPage(page.getParent());
                    if (parentPage == null) {
                        talentPage = null;
                    } else if (parentPage.getPageType().equals(PageTypes.SHOW.pageType())) {
                        Show show = new ShowFactory()
                            .withSniPage(parentPage)
                            .build();
                        talentPage = show.getTalentPage();
                    } else if (parentPage.getPageType().equals(PageTypes.TALENT.pageType())) {
                        talentPage = parentPage;
                    }
                    break;
                case EPISODE:
                    Episode episode = new EpisodeFactory()
                        .withSniPage(page)
                        .build();
                    talentPage = episode.getPrimaryTalentPage();
                    break;
                case SHOW:
                    Show show = new ShowFactory()
                        .withSniPage(page)
                        .build();
                    talentPage = show.getTalentPage();
                    break;
                case RECIPE:
                    Recipe recipe = new RecipeFactory()
                        .withSniPage(page)
                        .build();
                    talentPage = recipe.getRelatedTalentPage();
                    break;
                case TALENT:
                    talentPage = page;
                    break;
                default:
                    break;
            }
        }

        if (talentPage != null) {
            Talent talent = new TalentFactory()
                .withSniPage(talentPage)
                .build();
            if (talent != null) {
                retVal = talent.getTitle();
            }
        }

        return retVal;
    }


    public String getProperty(MetadataProperty prop) {
        if (prop == null) {
            return null;
        }
        String retVal = null;

        switch (prop) {
            case DETAILID:
                retVal = detailId != null ? detailId : DEFAULT_DETAIL_ID;
                break;
            case FASTFWDID:
                retVal = fastFwdId;
                break;
            case HUBID:
                retVal = hubPkgId;
                break;
            case HUBPATH:
                retVal = hubPath;
                break;
            case HUBTYPE:
                retVal = hubType;
                break;
            case IMGURL:
                retVal = imgUrl;
                break;
            case PACKAGENAME:
                retVal = pkgName;
                break;
            case PAGENUMBER:
                retVal = pageNumber;
                break;
            case PUBLISHTIME:
                retVal = publishTime;
                break;
            case SHOWABBR:
                retVal = showAbbr;
                break;
            case TALENTNAME:
                retVal = talentName;
                break;
            case TITLE:
                retVal = title;
                break;
            case TYPE:
                retVal = pageType;
                break;
            case UNIQUEID:
                retVal = uniqueId;
                break;
            case URL:
                retVal = url;
                break;
            case CANONICALURL:
                retVal = canonicalUrl;
                break;
            default:
                throw new IllegalArgumentException("invalid property");
        }

        return retVal;

    }
}
