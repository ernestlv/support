package com.scrippsnetworks.wcm.config;

/**
 * Service interface which surfaces a set of template-related paths for aid in page construction
 * 
 * The interface is a simple set of accessor/mutators for properties
 * 
 * Current documentation for each of these fields supported can be found in the implementation class
 * 
 * @author Ken Shih
 * @since May 2012
 */
public interface TemplateConfigService {

    /* path to resource supporting the Article Simple template */
    String getArticleSimpleRegionsPath();
    void setArticleSimpleRegionsPath(String articleSimpleRegionsPath);

    /* path to resource supporting the Article Simple template */
    String getArticleStepByStepRegionsPath();
    void setArticleStepByStepRegionsPath(String articleStepByStepRegionsPath);

    /* path to Bio Page */
    String getBioPageRegionsPath();
    void setBioPageRegionsPath(String bioPageRegionsPath);

    /* URL for Contact Us page. */
    String getContactUsURL();
    void setContactUsURL(String contactUsURL);

    /* URL for weekly program guide page. */
    String getDailyProgramGuideURL();
    void setDailyProgramGuideURL(String dailyProgramGuideURL);

    /* URL for weekly program guide page. */
    String getMobileDailyProgramGuideURL();
    void setMobileDailyProgramGuideURL(String mobileDailyProgramGuideURL);

    /* path to Episode Search Results Region */
    String getEpisodeArchiveRegionsPath();
    void setEpisodeArchiveRegionsPath(String episodeArchiveRegionsPath);

    /* path to resource supporting the Episode template */
    String getEpisodeRegionsPath();
    void setEpisodeRegionsPath(String episodeRegionsPath);

    /* path to Episode Search Results Region */
    String getEpisodeSearchResultsRegionsPath();
    void setEpisodeSearchResultsRegionsPath(String episodeSearchResultsRegionsPath);

    /* URL for episode search results page. */
    String getEpisodeSearchResultsURL();
    void setEpisodeSearchResultsURL(String episodeSearchResultsURL);

    /* URL for 500 Internal Server error. */
    String getFiveHundredPath();
    void setFiveHundredPath(String fiveHundredPath);

    /* path to footer resources */
    String getFooterPath();
    void setFooterPath(String footerPath);

    /* URL for 404 Not Found page. */
    String getFourOhFourPath();
    void setFourOhFourPath(String fourOhFourPath);

    /* path to Free form regions path */
    String getFreeFormPageRegionPath();
    void setFreeFormPageRegionPath(String freeFormPageRegionPath);

    /* path to header resources */
    String getHeaderPath();
    void setHeaderPath(String headerPath);

    /* path to resource supporting the Index Page template */
    String getIndexPageRegionsPath();
    void setIndexPageRegionsPath(String indexPageRegionsPath);

    /* path to nutrition Page */
    String getNutritionPageRegionsPath();
    void setNutritionPageRegionsPath(String nutritionPageRegionsPath);

    /* path to resources supporting the Package Landing template */
    String getPackageLandingPageRegionsPath();
    void setPackageLandingPageRegionsPath(String videoPageRegionsPath);

    /* path to resource supporting the Photo Gallery template */
    String getPhotoGalleryRegionsPath();
    void setPhotoGalleryRegionsPath(String photoGalleryRegionsPath);

    /* path to resources supporting the Recipe Detail template */
    String getRecipeDetailPageRegionsPath();
    void setRecipeDetailPageRegionsPath(String recipeDetailPageRegionsPath);

    /* path to Search Results Page */
    String getSearchResultsRegionsPath();
    void setSearchResultsRegionsPath(String searchResultsRegionsPath);

    /* URL for search results page. */
    String getSearchResultsURL();
    void setSearchResultsURL(String searchResultsURL);

    /* path to show Page */
    String getShowPageRegionsPath();
    void setShowPageRegionsPath(String showPageRegionsPath);

    /* path to show section Page */
    String getShowSectionRegionsPath();
    void setShowSectionRegionsPath(String showSectionRegionsPath);

    /* path to show top recipes Page */
    String getShowTopRecipiesPageRegionsPath();
    void setShowTopRecipiesPageRegionsPath(String nutritionPageRegionsPath);

    /* URL for Topics A to Z page. */
    String getShowsAtoZURL();
    void setShowsAtoZURL(String showsAtoZURL);

    /* SiteName code */
    String getSiteName();
    void setSiteName(String siteName);

    /* path to SNI Activate Asset workflow model */
    String getSNIActivateAssetWorkflowPath();
    void setSNIActivateAssetWorkflowPath(String sniActivateAssetPath);

    /* path to the Teaser Component Used to deliver the Sponsored Pad in the "first" position */
    String getSponsoredPodTeaserPosition1Path();
    void setSponsoredPodTeaserPosition1Path(String sponsoredPodTeaserPosition1Path);

   /* path to the Teaser Component Used to deliver the Sponsored Pad in the "second" position */
    String getSponsoredPodTeaserPosition2Path();
    void setSponsoredPodTeaserPosition2Path(String sponsoredPodTeaserPosition2Path);

   /* path to the Teaser Component Used to deliver the Sponsored Pad in the "third" position */
    String getSponsoredPodTeaserPosition3Path();
    void setSponsoredPodTeaserPosition3Path(String sponsoredPodTeaserPosition3Path);

    /* path to resource Talent Page */
    String getTalentPageRegionsPath();
    void setTalentPageRegionsPath(String talentPageRegionsPath);

    /* path to resource supporting the Topic template */
    String getTopicRegionsPath();
    void setTopicRegionsPath(String topicRegionsPath);

    /* URL for Topics A to Z page. */
    String getTopicsAtoZURL();
    void setTopicsAtoZURL(String topicAtoZURL);

    /* path to video channel Page */
    String getVideoChannelRegionsPath();
    void setVideoChannelRegionsPath(String videoChannelRegionsPath);

    /* path to resources supporting the Video Page template */
    String getVideoPageRegionsPath();
    void setVideoPageRegionsPath(String videoPageRegionsPath);

    /* path to resource Video Player Page */
    String getVideoPlayerPageRegionsPath();
    void setVideoPlayerPageRegionsPath(String videoPlayerPageRegionsPath);

    /* path to video player Page */
    String getVideoPlayerRegionsPath();
    void setVideoPlayerRegionsPath(String videoPlayerRegionsPath);

    /* path to Web Series regions path */
    String getWebSeriesPageRegionsPath();
    void setWebSeriesPageRegionsPath(String webSeriesPageRegionsPath);

    /* URL for weekly program guide page. */
    String getWeeklyProgramGuideURL();
    void setWeeklyProgramGuideURL(String weeklyProgramGuideURL);
    
    /* URL for OTR search results page. */
    String getOtrSearchResultsURL();
    void setOtrSearchResultsURL(String otrSearchResultsURL);

    /* URL for Modal Window page. */
    String getModalWindowPath();
    void setModalWindowPath(String modalWindowPath);

    /* URL for video search results page. */
    String getVideoSearchResultsURL();
    void setVideoSearchResultsURL(String videoSearchResultsURL);

    /* URL for recipe search results page. */
    String getRecipeSearchResultsURL();
    void setRecipeSearchResultsURL(String recipeSearchResultsURL);
    
    /* List of modules that belong on pages 2+ of Topic pages. */
    String[] getTopicsPagesTwoPlusModules();
    void setTopicsPagesTwoPlusModules(String[] topicsPagesTwoPlusModules);
}
