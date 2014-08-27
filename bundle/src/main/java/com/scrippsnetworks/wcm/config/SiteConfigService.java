package com.scrippsnetworks.wcm.config;

/**
 * Service interface which surfaces a set of site-based metadata.
 */
public interface SiteConfigService {

    /**
     * Get the name of the Brand to be Displayed
     *
     * @return String representation of the brand name.
     */
    String getSiteName();
    
    String getEnvironment();

    boolean getConmioRedirectHomepage();

    boolean getConmioRedirectRecipes();

    String getDomain();

    String getAdServerUrl();

    String getSnapPlayListUrl();

    String getSnapBinary();

    String getSnapConfigs();
    
    boolean isSnapEnableHTML5();

    String getAutoSuggestContainer();

    String getAutoSuggestService();

    String[] getOmnitureMultiVariables();

    String getOmnitureSingleVariable();

    String getRsiKeyWord();

    boolean isUR3();

    boolean isAIM();

    long getAnimationSpeed();

    String getCommunityUR3Domain();

    String getCommunityAIMDomain();

    String getCommunitySsoControllerPath();

    String getNielsenHitCountHtmlUrl();

    boolean isNielsenUseIframeTracking();

    boolean isNielsenAutoTrackPages();

    long getPhotoGalleryThumbnailPageSize();

    String getPhotoGalleryThumbnailLoader();

    String getPhotoGalleryDefaultProductImageSmall();

    String getPhotoGalleryDefaultProductImageLarge();

    long getPhotoGalleryAnimationSpeed();

    long getPhotoGalleryLongTitleLength();

    long getPhotoGalleryLongCaptionLength();

    long getPhotoGalleryShortCaptionLength();

    String getPhotoGalleryImageWidthNarrow();

    String getPhotoGalleryImageWidthWide();

    String getFacebookChannelUrl();
    
    public String getTwitterSearchUrl();

    String getPinterestDefaultImageUrl();

    String getPinterestDefaultFromMessage();
    
    String getCommunityMinReviewsWidgetId() ;
    
    String getCommunityFavWidgetId() ;
    
    String getCommunityFullReviewsWidgetId();
    
    String getCommunityMobileReviewsWidgetId();
    
    String getCommunityRecipeDomain();
    
    public String getCommunitySanitization();
    
    public String getCommunityEditorMaxLimit();
    
    public String getCommunitySortOrder();
    
    public String getCommunityItemsPerPage();
    
    public String getCommunityChildItemsPerPage();
    
    public String getCommunityCssDefault();
    
    public String getCommunityShowLike();
    
    public String getCommunityShowShare();
    
    public String getCommunityShowReply();
    
    public String getCommunityShowFlag();
    
    public String getCommunityShowDelete();
    
    public String getCommunityRequireApproval();
    
    public String getCommunityColorcodeRolesMatch();
    
    public String getCommunityIconRolesMatch();

    String getCommunityUrl();

    String getRecipeOriginRootUrl();

    String getMobileDomain();

    String getBrandLogoDamPath();

    String getBrandLogoDesignsPath();

    String getBrandSiteTitle();

    String getFnMagazineTagPath();
    
    String getScheduleRootContentPath();

    String getScheduleRootAssetPath();

    String getScheduleStartTime();

    String getFeaturedSchedulePath();

    String getEmailFriendTemplatePath();

    String getEmailFriendFromEmail();

    String getEmailFriendSubject();

    boolean useEmailFriendFromNameInFromAddress();

    String getRecaptchaPublicKey();

    String getSiteHomepagePath();
    
    String getSiteLocale();

    public String getDefaultImage();

    String[] getBreadcrumbHiddenSections();

    String[] getBreadcrumbIndexSections();

    String[] getBreadcrumbAToZSections();
}
