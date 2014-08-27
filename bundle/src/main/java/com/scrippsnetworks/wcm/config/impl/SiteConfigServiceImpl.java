package com.scrippsnetworks.wcm.config.impl;

import java.util.Dictionary;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyUnbounded;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.component.ComponentContext;

import com.scrippsnetworks.wcm.config.SiteConfigService;

@Component(metatype = true, configurationFactory = true, policy = ConfigurationPolicy.REQUIRE, label = "SNI WCM Site Configuration Service")
@Service
public class SiteConfigServiceImpl implements SiteConfigService {

    @Property(label = "Ad Server URL")
    private static final String PROP_AD_SERVER_URL = "adServerUrl";

    @Property(label = "Animation Speed")
    private static final String PROP_ANIMATION_SPEED = "animationSpeed";

    @Property(label = "Auto Suggest Container")
    private static final String PROP_AUTO_SUGGEST_CONTAINER = "autoSuggestContainer";

    @Property(label = "Auto Suggest Service")
    private static final String PROP_AUTO_SUGGEST_SERVICE = "autoSuggestService";

    @Property(label = "Brand Logo DAM Path")
    private static final String PROP_BRAND_LOGO_DAM_PATH = "brand.logoDamPath";

    @Property(label = "Brand Logo Designs Path")
    private static final String PROP_BRAND_LOGO_DESIGNS_PATH = "brand.logoDesignsPath";

    @Property(label = "Brand Site Title")
    private static final String PROP_BRAND_SITE_TITLE = "brand.siteTitle";

    @Property(label = "Breadcrumb A-Z Sections", unbounded=PropertyUnbounded.ARRAY)
    private static final String PROP_BREADCRUMB_ATOZ_SECTIONS = "breadcrumb.aToZSections";

    @Property(label = "Breadcrumb Hidden Sections", unbounded=PropertyUnbounded.ARRAY)
    private static final String PROP_BREADCRUMB_HIDDEN_SECTIONS = "breadcrumb.hiddenSections";

    @Property(label = "Breadcrumb Index Sections", unbounded=PropertyUnbounded.ARRAY)
    private static final String PROP_BREADCRUMB_INDEX_SECTIONS = "breadcrumb.indexSections";

    @Property(label = "Conmio Redirect Recipes", boolValue = false)
    private static final String PROP_CONMIO_REDIRECT_RECIPES = "conmio.redirectRecipes";

    @Property(label = "Conmio Redirect Homepage", boolValue = false)
    private static final String PROP_CONMIO_REDIRECT_HOMEPAGE = "conmio.redirectHomepage";

    @Property(label = "Domain")
    private static final String PROP_DOMAIN = "domain";
    
    @Property(label = "Domain for review and rating api")
    private static final String PROP_DOMAIN_REVIEW_RATING = "domain.review";

    @Property(label = "Image Domain")
    private static final String PROP_IMG_DOMAIN = "img.domain";

    @Property(label = "Environment")
    private static final String PROP_ENVIRONMENT = "environment";

    @Property(label = "Facebook Channel URL")
    private static final String PROP_FACEBOOK_CHANNEL_URL = "facebook.channelUrl";

    @Property(label = "Twitter Search URL")
    private static final String PROP_TWITTER_SEARCH_URL = "twitter.searchUrl";

    @Property(label = "Nielsen Auto Track Pages", boolValue = false)
    private static final String PROP_NIELSEN_AUTO_TRACK_PAGES = "nielsen.autoTrackPages";

    @Property(label = "Neilsen Hit Count HTML URL")
    private static final String PROP_NIELSEN_HIT_COUNT_HTML_URL = "nielsen.hitCountHtmlUrl";

    @Property(label = "Nielsen Use IFrame Tracking", boolValue = false)
    private static final String PROP_NIELSEN_USE_IFRAME_TRACKING = "nielsen.useIframeTracking";
    
    @Property(label="Omniture Multi Variables", unbounded=PropertyUnbounded.ARRAY)
    private static final String PROP_OMNITURE_MULTI_VARIABLES = "omniture.multiVariables";
    
    @Property(label="Omniture Single Variable")
    private static final String PROP_OMNITURE_SINGLE_VARIABLES = "omniture.singleVariable";
    
    @Property(label="Photo Gallery Animation Speed", longValue=-1)
    private static final String PROP_PHOTO_GALLERY_ANIMATION_SPEED = "photogallery.animationSpeed";
    
    @Property(label="Photo Gallery Default Product Image Large", longValue=-1)
    private static final String PROP_PHOTO_GALLERY_DEFAULT_PRODUCT_IMAGE_LARGE = "photogallery.defaultProductImageLarge";
    
    @Property(label="Photo Gallery Default Product Image Small", longValue=-1)
    private static final String PROP_PHOTO_GALLERY_DEFAULT_PRODUCT_IMAGE_SMALL = "photogallery.defaultProductImageSmall";
    
    @Property(label="Photo Gallery Image Width Narrow", longValue=-1)
    private static final String PROP_PHOTO_GALLERY_IMAGE_WIDTH_NARROW = "photoGallery.imageWidthNarrow";
    
    @Property(label="Photo Gallery Image Width Wide")
    private static final String PROP_PHOTO_GALLERY_IMAGE_WIDTH_WIDE = "photoGallery.imageWidthWide";
    
    @Property(label="Photo Gallery Long Caption Length")
    private static final String PROP_PHOTO_GALLERY_LONG_CAPTION_LENGTH = "photoGallery.longCaptionLength";
    
    @Property(label="Photo Gallery Long Title Length")
    private static final String PROP_PHOTO_GALLERY_LONG_TITLE_LENGTH = "photoGallery.longTitleLength";
    
    @Property(label="Photo Gallery Short Caption Length")
    private static final String PROP_PHOTO_GALLERY_SHORT_CAPTION_LENGTH = "photoGallery.shortCaptionLength";
    
    @Property(label="Photo Gallery Thumbanil Loader")
    private static final String PROP_PHOTO_GALLERY_THUMBNAIL_LOADER = "photoGallery.thumbnailLoader";
   
    @Property(label="PHoto Gallery Thumbnail Page Size")
    private static final String PROP_PHOTO_GALLERY_THUMBNAIL_PAGE_SIZE = "photoGallery.thumbnailPageSize";

    @Property(label="Pinterest Default From Message")
    private static final String PROP_PINTEREST_DEFAULT_FROM_MESSAGE = "pinterest.defaultFromMessage";
    
    @Property(label="Pinterest Default Image URL")
    private static final String PROP_PINTEREST_DEFAULT_IMAGE_URL = "pinterest.defaultImageUrl";

    @Property(label="reCAPTCHA Public Key")
    private static final String PROP_RECAPTCHA_PUBLIC_KEY = "recaptcha.publicKey";
    
    @Property(label="RSI Keyword")
    private static final String PROP_RSI_KEY_WORD = "rsiKeyWord";
    
    @Property(label = "Site Name")
    private static final String PROP_SITE_NAME = "siteName";

    @Property(label = "Site Homepage Path")
    private static final String PROP_SITE_HOMEPAGE_PATH = "site.homepagePath";
    
    @Property(label="Snap Binary")
    private static final String PROP_SNAP_BINARY = "snapBinary";
    
    @Property(label="Snap Configs")
    private static final String PROP_SNAP_CONFIGS = "snapConfigs";
    
    @Property(label="Snap Playlist URL")
    private static final String PROP_SNAP_PLAYLIST_URL = "snapPlayListUrl";
    
    @Property(label="Snap Enable HTML5 Video", boolValue = false)
    private static final String PROP_SNAP_ENABLE_HTML5 = "snapEnableHTML5";
    
    @Property(label="UR3")
    private static final String PROP_UR3 = "ur3";

    @Property(label="AIM")
    private static final String PROP_AIM = "AIM";
    
    @Property(label="UR3 Domain")
    private static final String PROP_UR3_DOMAIN = "Community.ur3Domain";
    
    @Property(label="AIM Domain")
    private static final String PROP_AIM_DOMAIN = "Community.aimDomain";

    @Property(label="SSO Controller URL")
    private static final String PROP_SSOCONTROLLERPATH = "Community.ssoControllerPath";
    
    @Property(label="Community Mini Reviews Widget Id")
    private static final String PROP_MINREVIEWS_WIDGET = "Community.minReviewsWidgetId";

    @Property(label="Community Favorite Widget Id")
    private static final String PROP_FAVORITE_WIDGET = "Community.favWidgetId";
    
    @Property(label="Community Full Reviews Widget Id")
    private static final String PROP_FULLREVIEWS_WIDGET = "Community.fullReviewsWidgetId";
    
    @Property(label="Community Mobile Reviews Widget Id")
    private static final String PROP_MOBILE_REVIEWS_WIDGET = "Community.mobileReviewsWidgetId";
    
    @Property(label="Community Recipe Domain")
    private static final String PROP_RECIPE_DOMAIN = "Community.recipeDomain";
    
    @Property(label="Community Sanitization")
    private static final String PROP_COMMUNITY_SANITIZATION = "Community.Sanitization";
    
    @Property(label="Community Editor Max Limit")
    private static final String PROP_COMMUNITY_EDITOR_MAX_LIMIT = "Community.EditorMaxLimit";
    
    @Property(label="Community Sort Order")
    private static final String PROP_COMMUNITY_SORT_ORDER = "Community.SortOrder";
    
    @Property(label="Community Items Per Page")
    private static final String PROP_COMMUNITY_ITEMS_PER_PAGE = "Community.ItemsPerPage";
    
    @Property(label="Community Child Items Per Page")
    private static final String PROP_COMMUNITY_CHILD_ITEMS_PER_PAGE = "Community.ChildItemsPerPage";
    
    @Property(label="Community CSS Default")
    private static final String PROP_COMMUNITY_CSS_DEFAULT = "Community.CssDefault";
    
    @Property(label="Community Show Like")
    private static final String PROP_COMMUNITY_SHOW_LIKE = "Community.ShowLike";
    
    @Property(label="Community Show Share")
    private static final String PROP_COMMUNITY_SHOW_SHARE = "Community.ShowShare";
    
    @Property(label="Community Show Reply")
    private static final String PROP_COMMUNITY_SHOW_REPLY = "Community.ShowReply";
    
    @Property(label="Community Show Flag")
    private static final String PROP_COMMUNITY_SHOW_FLAG = "Community.ShowFlag";
    
    @Property(label="Community Show Delete")
    private static final String PROP_COMMUNITY_SHOW_DELETE = "Community.ShowDelete";
    
    @Property(label="Community Require Approval")
    private static final String PROP_COMMUNITY_REQUIRE_APPROVAL = "Community.RequireApproval";
    
    @Property(label="Community Colorcode Roles Match")
    private static final String PROP_COMMUNITY_COLORCODE_ROLES_MATCH = "Community.ColorcodeRolesMatch";
    
    @Property(label="Community Icon Roles Match")
    private static final String PROP_COMMUNITY_ICON_ROLES_MATCH = "Community.IconRolesMatch";
   
    @Property(label="Community URL")
    private static final String PROP_COMMUNITY_URL = "community.url";
 
    @Property(label="Recipe Origin Root Url")
    private static final String PROP_RECIPE_ORIGIN_ROOT_URL = "recipeOriginRootUrl";

    @Property(label="Mobile Domain")
    private static final String PROP_MOBILE_DOMAIN = "mobile.domain";
    
    @Property(label="Blog Feed Host")
    private static final String PROP_BLOG_FEED_HOST = "blogFeedHost";

    @Property(label="Fn Magazine Tag Path")
    private static final String PROP_FN_MAGAZINE_TAG_PATH = "fnMagazineTagPath";

    @Property(label="Root Content Path")
    private static final String PROP_SCHEDULE_ROOT_CONTENT_PATH = "schedule.rootContentPath";

    @Property(label="Root Asset Path")
    private static final String PROP_SCHEDULE_ROOT_ASSET_PATH = "schedule.rootAssetPath";

    @Property(label="Schedule Daily Start Time HH:mm")
    private static final String PROP_SCHEDULE_START_TIME = "schedule.startTime";

    @Property(label="Featured Schedule Component Path")
    private static final String PROP_SCHEDULE_FEATURED_PATH = "schedule.featuredPath";

    @Property(label="Email a friend from address")
    private static final String PROP_EMAIL_FRIEND_FROM_ADDRESS = "emailFriend.fromAddress";

    @Property(label="Email a friend template path")
    private static final String PROP_EMAIL_FRIEND_TEMPLATE_PATH = "emailFriend.templatePath";

    @Property(label="Email a friend subject")
    private static final String PROP_EMAIL_FRIEND_SUBJECT = "emailFriend.subject";

    @Property(label="Email a friend should use from name in from address in email")
    private static final String PROP_EMAIL_FRIEND_FROM_NAME_IN_FROM_ADDRESS = "emailFriend.fromNameInFromAddress";
    
    @Property(label = "Site Locale")
    private static final String PROP_SITE_LOCALE = "siteLocale";

    @Property(label="Default image")
    private static final String PROP_DEFAULT_IMAGE = "defaultImage";

    private String adServerUrl;

    private long animationSpeed;

    private String autoSuggestContainer;

    private String autoSuggestService;

    private String brandLogoDamPath;

    private String brandLogoDesignsPath;

    private String brandSiteTitle;

    private String[] breadcrumbHiddenSections;

    private String[] breadcrumbIndexSections;

    private String[] breadcrumbAToZSections;

    private boolean conmioRedirectRecipes;

    private boolean conmioRedirectHomepage;

    private String domain;
    
    private String domainRatingReview;

    private String imgDomain;

    private String environment;

    private String facebookChannelUrl;
    
    private String twitterSearchUrl;

    private boolean nielsenAutoTrackPages;

    private String nielsenHitCountHtmlUrl;

    private boolean nielsenUseIframeTracking;

    private String[] omnitureMultiVariables;

    private String omnitureSingleVariable;

    private long photoGalleryAnimationSpeed;

    private String photoGalleryDefaultProductImageLarge;

    private String photoGalleryDefaultProductImageSmall;

    private String photoGalleryImageWidthNarrow;

    private String photoGalleryImageWidthWide;

    private long photoGalleryLongCaptionLength;

    private long photoGalleryLongTitleLength;

    private long photoGalleryShortCaptionLength;

    private String photoGalleryThumbnailLoader;

    private long photoGalleryThumbnailPageSize;

    private String pinterestDefaultFromMessage;

    private String pinterestDefaultImageUrl;

    private String recaptchaPublicKey;

    private String rsiKeyWord;

    private String siteName;

    private String siteHomepagePath;

    private String snapBinary;

    private String snapConfigs;

    private String snapPlayListUrl;

    private boolean snapEnableHTML5;

    private boolean ur3;

    private String ur3Domain;

    private boolean aim;

    private String aimDomain;

    private String ssoControllerPath;
    
    private String communityMinReviewsWidgetId;

    private String communityFavWidgetId;
    
    private String communityFullReviewsWidgetId;
    
    private String communityMobileReviewsWidgetId;
    
    private String communitySanitization;
    
    private String communityEditorMaxLimit;
    
    private String communitySortOrder;
    
    private String communityItemsPerPage;
    
    private String communityChildItemsPerPage;
    
    private String communityCssDefault;
    
    private String communityShowLike;
    
    private String communityShowShare;
    
    private String communityShowReply;
    
    private String communityShowFlag;
    
    private String communityShowDelete;
    
    private String communityRequireApproval;
    
    private String communityColorcodeRolesMatch;
    
    private String communityIconRolesMatch;
    
    private String communityRecipeDomain;

    private String communityUrl;

    private String recipeOriginRootUrl;

    private String mobileDomain;

    private String blogFeedHost;

    private String fnMagazineTagPath;

    private String scheduleRootContentPath;

    private String scheduleRootAssetPath;

    private String scheduleStartTime;

    private String featuredSchedulePath;

    private String emailFriendTemplatePath;

    private String emailFriendFromEmail;

    private String emailFriendSubject;

    private boolean emailFriendFromNameInFromAddress;
    
    private String siteLocale;

    public String getSiteLocale() {
        return siteLocale;
    }

    private String defaultImage;

    public String getAdServerUrl() {
        return adServerUrl;
    }

    public long getAnimationSpeed() {
        return animationSpeed;
    }

    public String getAutoSuggestContainer() {
        return autoSuggestContainer;
    }

    public String getAutoSuggestService() {
        return autoSuggestService;
    }

    public String getBrandLogoDamPath() {
        return brandLogoDamPath;
    }

    public String getBrandLogoDesignsPath() {
        return brandLogoDesignsPath;
    }

    public String getBrandSiteTitle() {
        return brandSiteTitle;
    }

    public String[] getBreadcrumbHiddenSections() {
        return breadcrumbHiddenSections;
    }

    public String[] getBreadcrumbIndexSections() {
        return breadcrumbIndexSections;
    }

    public String[] getBreadcrumbAToZSections() {
        return breadcrumbAToZSections;
    }

    public boolean getConmioRedirectRecipes() {
        return conmioRedirectRecipes;
    }

    public boolean getConmioRedirectHomepage() {
        return conmioRedirectHomepage;
    }

    public String getDomain() {
        return domain;
    }

    public String getImgDomain() {
        return imgDomain;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getFacebookChannelUrl() {
        return facebookChannelUrl;
    }
    
    public String getTwitterSearchUrl() {
        return twitterSearchUrl;
    }

    public boolean isNielsenAutoTrackPages() {
        return nielsenAutoTrackPages;
    }

    public String getNielsenHitCountHtmlUrl() {
        return nielsenHitCountHtmlUrl;
    }

    public boolean isNielsenUseIframeTracking() {
        return nielsenUseIframeTracking;
    }

    public String[] getOmnitureMultiVariables() {
        return omnitureMultiVariables;
    }

    public String getOmnitureSingleVariable() {
        return omnitureSingleVariable;
    }

    public long getPhotoGalleryAnimationSpeed() {
        return photoGalleryAnimationSpeed;
    }

    public String getPhotoGalleryDefaultProductImageLarge() {
        return photoGalleryDefaultProductImageLarge;
    }

    public String getPhotoGalleryDefaultProductImageSmall() {
        return photoGalleryDefaultProductImageSmall;
    }

    public String getPhotoGalleryImageWidthNarrow() {
        return photoGalleryImageWidthNarrow;
    }

    public String getPhotoGalleryImageWidthWide() {
        return photoGalleryImageWidthWide;
    }

    public long getPhotoGalleryLongCaptionLength() {
        return photoGalleryLongCaptionLength;
    }

    public long getPhotoGalleryLongTitleLength() {
        return photoGalleryLongTitleLength;
    }

    public long getPhotoGalleryShortCaptionLength() {
        return photoGalleryShortCaptionLength;
    }

    public String getPhotoGalleryThumbnailLoader() {
        return photoGalleryThumbnailLoader;
    }

    public long getPhotoGalleryThumbnailPageSize() {
        return photoGalleryThumbnailPageSize;
    }

    public String getPinterestDefaultFromMessage() {
        return pinterestDefaultFromMessage;
    }

    public String getPinterestDefaultImageUrl() {
        return pinterestDefaultImageUrl;
    }

    public String getRecaptchaPublicKey() {
        return recaptchaPublicKey;
    }

    public String getRsiKeyWord() {
        return rsiKeyWord;
    }

    public String getSiteName() {
        return siteName;
    }

    public String getSiteHomepagePath() {
        return siteHomepagePath;
    }

    public String getSnapBinary() {
        return snapBinary;
    }

    public String getSnapConfigs() {
        return snapConfigs;
    }

    public String getSnapPlayListUrl() {
        return snapPlayListUrl;
    }

    public boolean isSnapEnableHTML5() {
        return snapEnableHTML5;
    }

    public String getCommunityUR3Domain() {
        return ur3Domain;
    }

    public String getCommunityAIMDomain() {
        return aimDomain;
    }

    public String getCommunitySsoControllerPath() {
        return ssoControllerPath;
    }

    public boolean isUR3() {
        return ur3;
    }

    public boolean isAIM() {
        return aim;
    }

    public String getCommunityMinReviewsWidgetId() {
        return communityMinReviewsWidgetId;
    }
    
    public String getCommunityFavWidgetId() {
        return communityFavWidgetId;
    }
    
    public String getCommunityFullReviewsWidgetId() {
        return communityFullReviewsWidgetId;
    }
    
    public String getCommunityMobileReviewsWidgetId() {
        return communityMobileReviewsWidgetId;
    }
    
    public String getCommunityRecipeDomain() {
        return communityRecipeDomain;
    }
    
    public String getCommunitySanitization() {
        return communitySanitization;
    }
    
    public String getCommunityEditorMaxLimit() {
        return communityEditorMaxLimit;
    }
    
    public String getCommunitySortOrder() {
        return communitySortOrder;
    }
    
    public String getCommunityItemsPerPage() {
        return communityItemsPerPage;
    }
    
    public String getCommunityChildItemsPerPage() {
        return communityChildItemsPerPage;
    }
    
    public String getCommunityCssDefault() {
        return communityCssDefault;
    }
    
    public String getCommunityShowLike() {
        return communityShowLike;
    }
    
    public String getCommunityShowShare() {
        return communityShowShare;
    }
    
    public String getCommunityShowReply() {
        return communityShowReply;
    }
    
    public String getCommunityShowFlag() {
        return communityShowFlag;
    }
    
    public String getCommunityShowDelete() {
        return communityShowDelete;
    }
    
    public String getCommunityRequireApproval() {
        return communityRequireApproval;
    }
    
    public String getCommunityColorcodeRolesMatch() {
        return communityColorcodeRolesMatch;
    }
    
    public String getCommunityIconRolesMatch() {
        return communityIconRolesMatch;
    }

    public String getCommunityUrl() {
        return communityUrl;
    }

    public String getRecipeOriginRootUrl() {
        return recipeOriginRootUrl;
    }

    public String getMobileDomain() {
        return mobileDomain;
    }

    public String getBlogFeedHost(){
        return blogFeedHost;
    }

    public String getFnMagazineTagPath(){
        return fnMagazineTagPath;
    }

    public String getScheduleRootContentPath() {
        return scheduleRootContentPath;
    }

    public String getScheduleRootAssetPath() {
        return scheduleRootAssetPath;
    }

    public String getScheduleStartTime() {
        return scheduleStartTime;
    }

    public String getFeaturedSchedulePath() {
        return featuredSchedulePath;
    }

    public String getEmailFriendTemplatePath() {
        return emailFriendTemplatePath;
    }

    public String getEmailFriendFromEmail() {
        return emailFriendFromEmail;
    }

    public String getEmailFriendSubject() {
        return emailFriendSubject;
    }

    public boolean useEmailFriendFromNameInFromAddress() {
        return emailFriendFromNameInFromAddress;
    }

    @Override
    public String getDefaultImage() {
        return defaultImage;
    }

    public String getDomainRatingReview() {
		return domainRatingReview;
	}

	public void setDomainRatingReview(String domainRatingReview) {
		this.domainRatingReview = domainRatingReview;
	}

	protected void activate(ComponentContext ctx) {
        Dictionary<?, ?> props = ctx.getProperties();
        this.siteName = OsgiUtil.toString(props.get(PROP_SITE_NAME), null);
        this.environment = OsgiUtil.toString(props.get(PROP_ENVIRONMENT), null);
        this.adServerUrl = OsgiUtil.toString(props.get(PROP_AD_SERVER_URL), null);
        this.animationSpeed = OsgiUtil.toLong(props.get(PROP_ANIMATION_SPEED), -1);
        this.autoSuggestContainer = OsgiUtil.toString(props.get(PROP_AUTO_SUGGEST_CONTAINER), null);
        this.autoSuggestService = OsgiUtil.toString(props.get(PROP_AUTO_SUGGEST_SERVICE), null);
        this.brandLogoDamPath = OsgiUtil.toString(props.get(PROP_BRAND_LOGO_DAM_PATH), null);
        this.brandLogoDesignsPath = OsgiUtil.toString(props.get(PROP_BRAND_LOGO_DESIGNS_PATH), null);
        this.brandSiteTitle = OsgiUtil.toString(props.get(PROP_BRAND_SITE_TITLE), null);
        this.breadcrumbHiddenSections = OsgiUtil.toStringArray(props.get(PROP_BREADCRUMB_HIDDEN_SECTIONS));
        if (this.breadcrumbHiddenSections == null) {
            this.breadcrumbHiddenSections = new String[0];
        }
        this.breadcrumbIndexSections = OsgiUtil.toStringArray(props.get(PROP_BREADCRUMB_INDEX_SECTIONS));
        if (this.breadcrumbIndexSections == null) {
            this.breadcrumbIndexSections = new String[0];
        }
        this.breadcrumbAToZSections = OsgiUtil.toStringArray(props.get(PROP_BREADCRUMB_ATOZ_SECTIONS));
        if (this.breadcrumbAToZSections == null) {
            this.breadcrumbAToZSections = new String[0];
        }
        this.conmioRedirectHomepage = OsgiUtil.toBoolean(props.get(PROP_CONMIO_REDIRECT_HOMEPAGE), false);
        this.conmioRedirectRecipes = OsgiUtil.toBoolean(props.get(PROP_CONMIO_REDIRECT_RECIPES), false);
        this.domain = OsgiUtil.toString(props.get(PROP_DOMAIN), null);
        this.domainRatingReview = OsgiUtil.toString(props.get(PROP_DOMAIN_REVIEW_RATING), null);
        this.imgDomain = OsgiUtil.toString(props.get(PROP_IMG_DOMAIN), null);
        this.facebookChannelUrl = OsgiUtil.toString(props.get(PROP_FACEBOOK_CHANNEL_URL), null);
        this.twitterSearchUrl = OsgiUtil.toString(props.get(PROP_TWITTER_SEARCH_URL), null);
        this.nielsenAutoTrackPages = OsgiUtil.toBoolean(props.get(PROP_NIELSEN_AUTO_TRACK_PAGES), false);
        this.nielsenHitCountHtmlUrl = OsgiUtil.toString(props.get(PROP_NIELSEN_HIT_COUNT_HTML_URL), null);
        this.nielsenUseIframeTracking = OsgiUtil.toBoolean(props.get(PROP_NIELSEN_USE_IFRAME_TRACKING), false);
        this.omnitureMultiVariables = OsgiUtil.toStringArray(props.get(PROP_OMNITURE_MULTI_VARIABLES));
        if (this.omnitureMultiVariables == null) {
            this.omnitureMultiVariables = new String[0];
        }
        this.omnitureSingleVariable = OsgiUtil.toString(props.get(PROP_OMNITURE_SINGLE_VARIABLES), null);
        this.photoGalleryAnimationSpeed = OsgiUtil.toLong(props.get(PROP_PHOTO_GALLERY_ANIMATION_SPEED), -1);
        this.photoGalleryDefaultProductImageLarge = OsgiUtil.toString(
                props.get(PROP_PHOTO_GALLERY_DEFAULT_PRODUCT_IMAGE_LARGE), null);
        this.photoGalleryDefaultProductImageSmall = OsgiUtil.toString(
                props.get(PROP_PHOTO_GALLERY_DEFAULT_PRODUCT_IMAGE_SMALL), null);
        this.photoGalleryImageWidthNarrow = OsgiUtil.toString(props.get(PROP_PHOTO_GALLERY_IMAGE_WIDTH_NARROW), null);
        this.photoGalleryImageWidthWide = OsgiUtil.toString(props.get(PROP_PHOTO_GALLERY_IMAGE_WIDTH_WIDE), null);
        this.photoGalleryLongCaptionLength = OsgiUtil.toLong(props.get(PROP_PHOTO_GALLERY_LONG_CAPTION_LENGTH), -1);
        this.photoGalleryLongTitleLength = OsgiUtil.toLong(props.get(PROP_PHOTO_GALLERY_LONG_TITLE_LENGTH), -1);
        this.photoGalleryShortCaptionLength = OsgiUtil.toLong(props.get(PROP_PHOTO_GALLERY_SHORT_CAPTION_LENGTH), -1);
        this.photoGalleryThumbnailPageSize = OsgiUtil.toLong(props.get(PROP_PHOTO_GALLERY_THUMBNAIL_PAGE_SIZE), -1);
        this.pinterestDefaultFromMessage = OsgiUtil.toString(props.get(PROP_PINTEREST_DEFAULT_FROM_MESSAGE), null);
        this.pinterestDefaultImageUrl = OsgiUtil.toString(props.get(PROP_PINTEREST_DEFAULT_IMAGE_URL), null);
        this.recaptchaPublicKey = OsgiUtil.toString(props.get(PROP_RECAPTCHA_PUBLIC_KEY), null);
        this.rsiKeyWord = OsgiUtil.toString(props.get(PROP_RSI_KEY_WORD), null);
        this.siteHomepagePath = OsgiUtil.toString(props.get(PROP_SITE_HOMEPAGE_PATH), null);
        this.snapBinary = OsgiUtil.toString(props.get(PROP_SNAP_BINARY), null);
        this.snapConfigs = OsgiUtil.toString(props.get(PROP_SNAP_CONFIGS), null);
        this.snapPlayListUrl = OsgiUtil.toString(props.get(PROP_SNAP_PLAYLIST_URL), null);
        this.snapEnableHTML5 = OsgiUtil.toBoolean(props.get(PROP_SNAP_ENABLE_HTML5), false);
        this.photoGalleryThumbnailLoader = OsgiUtil.toString(props.get(PROP_PHOTO_GALLERY_THUMBNAIL_LOADER), null);
        this.ur3Domain = OsgiUtil.toString(props.get(PROP_UR3_DOMAIN), null);
        this.aimDomain = OsgiUtil.toString(props.get(PROP_AIM_DOMAIN), null);
        this.ssoControllerPath = OsgiUtil.toString(props.get(PROP_SSOCONTROLLERPATH), null);
        this.ur3 = OsgiUtil.toBoolean(props.get(PROP_UR3), false);
        this.aim = OsgiUtil.toBoolean(props.get(PROP_AIM), false);
        this.communityFavWidgetId=OsgiUtil.toString(props.get(PROP_FAVORITE_WIDGET), null);
        this.communityMobileReviewsWidgetId=OsgiUtil.toString(props.get(PROP_MOBILE_REVIEWS_WIDGET), null);
        this.communityMinReviewsWidgetId=OsgiUtil.toString(props.get(PROP_MINREVIEWS_WIDGET), null);
        this.communityFullReviewsWidgetId=OsgiUtil.toString(props.get(PROP_FULLREVIEWS_WIDGET), null);
        this.communityRecipeDomain=OsgiUtil.toString(props.get(PROP_RECIPE_DOMAIN),null);
        this.communitySanitization=OsgiUtil.toString(props.get(PROP_COMMUNITY_SANITIZATION),null);
        this.communityEditorMaxLimit=OsgiUtil.toString(props.get(PROP_COMMUNITY_EDITOR_MAX_LIMIT),null);
        this.communitySortOrder=OsgiUtil.toString(props.get(PROP_COMMUNITY_SORT_ORDER),null);
        this.communityItemsPerPage=OsgiUtil.toString(props.get(PROP_COMMUNITY_ITEMS_PER_PAGE),null);
        this.communityChildItemsPerPage=OsgiUtil.toString(props.get(PROP_COMMUNITY_CHILD_ITEMS_PER_PAGE),null);
        this.communityCssDefault=OsgiUtil.toString(props.get(PROP_COMMUNITY_CSS_DEFAULT),null);
        this.communityShowLike=OsgiUtil.toString(props.get(PROP_COMMUNITY_SHOW_LIKE),null);
        this.communityShowShare=OsgiUtil.toString(props.get(PROP_COMMUNITY_SHOW_SHARE),null);
        this.communityShowReply=OsgiUtil.toString(props.get(PROP_COMMUNITY_SHOW_REPLY),null);
        this.communityShowFlag=OsgiUtil.toString(props.get(PROP_COMMUNITY_SHOW_FLAG),null);
        this.communityShowDelete=OsgiUtil.toString(props.get(PROP_COMMUNITY_SHOW_DELETE),null);
        this.communityRequireApproval=OsgiUtil.toString(props.get(PROP_COMMUNITY_REQUIRE_APPROVAL),null);
        this.communityColorcodeRolesMatch=OsgiUtil.toString(props.get(PROP_COMMUNITY_COLORCODE_ROLES_MATCH),null);
        this.communityIconRolesMatch=OsgiUtil.toString(props.get(PROP_COMMUNITY_ICON_ROLES_MATCH),null);
        this.communityUrl=OsgiUtil.toString(props.get(PROP_COMMUNITY_URL),null);
        this.recipeOriginRootUrl = OsgiUtil.toString(props.get(PROP_RECIPE_ORIGIN_ROOT_URL), null);
        this.mobileDomain = OsgiUtil.toString(props.get(PROP_MOBILE_DOMAIN), null);
        this.blogFeedHost=OsgiUtil.toString(props.get(PROP_BLOG_FEED_HOST),null);
        this.fnMagazineTagPath=OsgiUtil.toString(props.get(PROP_FN_MAGAZINE_TAG_PATH),null);
        this.scheduleRootContentPath = OsgiUtil.toString(props.get(PROP_SCHEDULE_ROOT_CONTENT_PATH), null);
        this.scheduleRootAssetPath = OsgiUtil.toString(props.get(PROP_SCHEDULE_ROOT_ASSET_PATH), null);
        this.scheduleStartTime = OsgiUtil.toString(props.get(PROP_SCHEDULE_START_TIME), null);        
        this.featuredSchedulePath = OsgiUtil.toString(props.get(PROP_SCHEDULE_FEATURED_PATH), null);
        this.emailFriendFromEmail = OsgiUtil.toString(props.get(PROP_EMAIL_FRIEND_FROM_ADDRESS), null);
        this.emailFriendFromNameInFromAddress = OsgiUtil.toBoolean(props.get(PROP_EMAIL_FRIEND_FROM_NAME_IN_FROM_ADDRESS), false);
        this.emailFriendSubject = OsgiUtil.toString(props.get(PROP_EMAIL_FRIEND_SUBJECT), null);
        this.emailFriendTemplatePath = OsgiUtil.toString(props.get(PROP_EMAIL_FRIEND_TEMPLATE_PATH), null);
        this.defaultImage = OsgiUtil.toString(props.get(PROP_DEFAULT_IMAGE), null);
        this.siteLocale = OsgiUtil.toString(props.get(PROP_SITE_LOCALE), null);
    }
}
