package com.scrippsnetworks.wcm.config.impl;
import java.util.Dictionary;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.component.ComponentContext;

/**
 * Implementation of {@link com.scrippsnetworks.wcm.config.TemplateConfigService}
 */
import com.scrippsnetworks.wcm.config.TemplateConfigService;

/**
 * Implementation of {@link TemplateConfigService}
 * to add another property
 * 
 * Synchronization/implementation notes:
 * objects of this class have members declared using the volatile keyword because we assume:
 * 1. the replacement of value doesn't depend on current value
 * 2. one value does not depend on another value
 * 
 * if these assumptions change, we could either 
 * a) pack up state into a config object and use an AtomicReference, like SearchServiceImpl
 * b) consider different synchronization policy
 * 
 * @inheritDoc
 * @author Ken Shih
 * @since May 2012
 */
/*
@Component(label = "SNI WCM Template Config Service",
    configurationFactory = true,
    description = "Provides access to configuration information for Templates",
    immediate = true,
    metatype = true)
@Service(value=TemplateConfigService.class)
*/
public class TemplateConfigServiceFood implements TemplateConfigService {
    
    /** Apache commons log. */
    // Can throw LogConfigurationException. 
    // following precedent set by SearchServiceImpl
    // if we want to make logging optional, this will need to be updated
    private static Log log = LogFactory.getLog(TemplateConfigServiceFood.class);

//-------------------- Property Definitions
    @Property(label = "Article Simple Regions Path",
        description = "Path to the content node containing default regions and content for Article Simple",
        value = "/content/food/regions/article-simple-regions")
    private static final String ARTICLE_SIMPLE_REGIONS_PATH = "articleSimpleRegionsPath";
   
    @Property(label = "Article Step by Step Regions Path",
        description = "Path to the content node containing default regions and content for Article Step by Step",
        value = "/content/food/regions/article-step-by-step-regions")
    private static final String ARTICLE_STEP_BY_STEP_REGIONS_PATH = "articleStepByStepRegionsPath";

    @Property(label = "Bio Page Regions Path",
        description = "Path to the content node containing default region paths for Bio pages",
        value = "/content/food/regions/bio-page-regions")
    private static final String BIO_PAGE_REGIONS_PATH = "bioPageRegionsPath";
    
    @Property(label = "Contact Us URL",
        description = "URL for Contact Us page",
        value = "/content/food/contact-us.html")
    private static final String CONTACT_US_URL = "contactUsURL";
    
    @Property(label = "Daily Program Guide URL",
        description = "URL for Daily Program Guide page",
        value = "/content/food/shows/tv-schedule")
    private static final String DAILY_PROGRAM_GUIDE_URL = "dailyProgramGuideURL";

    @Property(label = "Mobile Daily Program Guide URL",
            description = "Mobile URL for Daily Program Guide page",
            value = "/content/food/shows/tv-schedule")
    private static final String MOBILE_DAILY_PROGRAM_GUIDE_URL = "mobileDailyProgramGuideURL";
    
    @Property(label = "Episode Archive Regions Path",
        description = "Path to the content node containing default region paths for all episode archive pages",
        value = "/content/food/regions/episode-archive-regions")
    private static final String EPISODE_ARCHIVE_REGIONS_PATH = "episodeArchiveRegionsPath";
    
    @Property(label = "Episode Page Regions Path",
        description = "Path to the content node containing default regions and content for Episode page",
        value = "/content/food/regions/episode-page-regions")
    private static final String EPISODE_REGIONS_PATH = "episodeRegionsPath";

    @Property(label = "Episode Search Results Regions Path",
        description = "Path to the content node containing default region paths for all episode search results pages",
        value = "/content/food/regions/episode-search-results-regions")
    private static final String EPISODE_SEARCH_RESULTS_REGIONS_PATH = "episodeSearchResultsRegionsPath";

    @Property(label = "Episode Search Results URL",
        description = "URL for Episode Search Results page",
        value = "/content/food/episode-search-results.html")
    private static final String EPISODE_SEARCH_RESULTS_URL = "episodeSearchResultsURL";

    @Property(label = "500 Error Page Path",
        description = "Path to 500 Error page",
        value = "/content/food/site/500-internal-error")
    private static final String FIVE_HUNDRED_PATH = "fiveHundredPath";
    
    @Property(label = "Footer Path",
        description = "Path to site Footer content node",
        value = "/content/food/regions/main-footer")
    private static final String FOOTER_PATH = "footerPath";
    
    @Property(label = "404 Error Page Path",
        description = "Path to Pretty 404 Error page",
        value = "/content/food/site/404-not-found")
    private static final String FOUR_OH_FOUR_PATH = "fourOhFourPath";
    
    @Property(label = "Free Form Page Regions Path",
        description = "Path to the content node containing default regions and content for Free Form page",
        value = "/content/food/regions/free-form-regions")
    private static final String FREE_FORM_REGIONS_PATH = "freeFormPageRegionPath";
    
    @Property(label = "Header Path",
        description = "Path to site Header content node",
        value = "/content/food/regions/main-header")
    private static final String HEADER_PATH = "headerPath";

    @Property(label = "Index Page Regions Path",
        description = "Path to the content node containing default region paths for Index Pages",
        value = "/content/food/regions/index-page-regions")
    private static final String INDEX_PAGE_REGIONS_PATH = "indexPageRegionsPath";

    @Property(label = "Nutrition Page Regions Path",
        description = "Path to the content node containing default region paths for Nutrition pages",
        value = "/content/food/regions/nutrition-page-regions")
    private static final String NUTRITION_PAGE_REGIONS_PATH = "nutritionPageRegionsPath";

    @Property(label = "Package Landing Page Regions Path",
        description = "Path to content node containing default regions and contents for the Package Landing Page template",
        value = "/content/food/regions/package-landing-page-regions")
    private static final String PACKAGE_LANDING_PAGE_REGIONS_PATH = "packageLandingPageRegionsPath";

    @Property(label = "Photo Gallery Regions Path",
        description = "Path to content node containing default regions for Photo Gallery",
        value = "/content/food/regions/photo-gallery-page-regions")
    private static final String PHOTO_GALLERY_REGIONS_PATH = "photoGalleryRegionsPath";

    @Property(label = "Recipe Detail Page Regions Path",
        description = "Path to the content node containing default regions and contents for Recipe Detail Page",
        value = "/content/food/regions/recipe-detail-page-regions")
    private static final String RECIPE_DETAIL_PAGE_REGIONS_PATH = "recipeDetailPageRegionsPath";
    
    @Property(label = "Search Results Regions Path",
        description = "Path to the content node containing default region paths for all search results pages",
        value = "/content/food/regions/search-results-regions")
    private static final String SEARCH_RESULTS_REGIONS_PATH = "searchResultsRegionsPath";

    @Property(label = "Search Results URL",
        description = "URL for Search Results page",
        value = "/content/food/search-results.html")
    private static final String SEARCH_RESULTS_URL = "searchResultsURL";

    @Property(label = "Show Page Regions Path",
        description = "Path to the content node containing default region paths for Show pages",
        value = "/content/food/regions/show-page-regions")
    private static final String SHOW_PAGE_REGIONS_PATH = "showPageRegionsPath";
    
    @Property(label = "Show Section Page Regions Path",
        description = "Path to the content node containing default regions and content for Show Section page",
        value = "/content/food/regions/show-section-regions")
    private static final String SHOW_SECTION_REGIONS_PATH = "showSectionRegionsPath";

    @Property(label = "Show Top Recipes Page Regions Path",
        description = "Path to the content node containing default region paths for Show Top Recipes pages",
        value = "/content/food/regions/show-top-recipes-regions")
    private static final String SHOW_TOP_RECIPES_PAGE_REGIONS_PATH = "showTopRecipiesPageRegionsPath";
    
    @Property(label = "Shows A-Z URL",
        description = "URL for Shows A-Z page",
        value = "/content/food/shows/shows-a-z.html")
    private static final String SHOWS_A_Z_URL = "showsAtoZURL";

    @Property(label = "Site Name",
        description = "Site name code",
        value = "cook")
    private static final String SITE_NAME = "siteName";
    
    @Property(label = "SNI Activate Asset Workflow Path",
        description = "Path to the content node containing default region paths for Index Pages",
        value = "/etc/workflow/models/sni-activate-asset")
    private static final String SNI_ACTIVATE_ASSET_WORKFLOW_PATH = "sniActivateAssetWorkflowPath";

    @Property(label = "Sponsored Pod Teaser Position 1 Path",
        description = "Path to the Teaser Component used to deliver Sponsored Pods for a Cook Campaign (Position 1)",
        value = "/content/modules/cook/sponsored-pods/teaser-pos-1")
    private static final String SPONSORED_POD_TEASER_POSITION_1_PATH = "sponsoredPodTeaserPosition1Path";

    @Property(label = "Sponsored Pod Teaser Position 2 Path",
        description = "Path to the Teaser Component used to deliver Sponsored Pods for a Cook Campaign (Position 2)",
        value = "/content/modules/cook/sponsored-pods/teaser-pos-2")
    private static final String SPONSORED_POD_TEASER_POSITION_2_PATH = "sponsoredPodTeaserPosition2Path";

    @Property(label = "Sponsored Pod Teaser Position 3 Path",
        description = "Path to the Teaser Component used to deliver Sponsored Pods for a Cook Campaign (Position 3)",
        value = "/content/modules/cook/sponsored-pods/teaser-pos-3")
    private static final String SPONSORED_POD_TEASER_POSITION_3_PATH = "sponsoredPodTeaserPosition3Path";
    
    @Property(label = "Talent Page Regions Path",
        description = "Path to the content node containing default regions and content for Talent page",
        value = "/content/food/regions/talent-page-regions")
    private static final String TALENT_PAGE_REGIONS_PATH = "talentPageRegionsPath";

    @Property(label = "Talent Top Recipes Path",
        description = "Path to the content node containing default regions and content for Talent Top Recipes",
        value = "/content/food/regions/talent-top-recipes-regions")
    private static final String TALENT_TOP_RECIPES_PATH = "talentTopRecipesPath";
    
    @Property(label = "Topics A-Z URL",
        description = "URL for Topics A-Z page",
        value = "/content/food/topics/a-z.html")
    private static final String TOPICS_A_Z_URL = "topicsAtoZURL";

    @Property(label = "Recipes A-Z URL",
        description = "URL for Recipes A-Z page",
        value = "/content/food/recipes/a-z.html")
    private static final String RECIPES_A_Z_URL = "recipesAtoZURL";    
    
    @Property(label = "Topic Page Regions Path",
        description = "Path to the content node containing default regions and content for Topic page",
        value = "/content/food/regions/topic-page-regions")
    private static final String TOPIC_REGIONS_PATH = "topicRegionsPath";

    @Property(label = "Video Channel Page Regions Path",
        description = "Path to the content node containing default regions and content for Video Channel page",
        value = "/content/food/regions/video-channel-regions")
    private static final String VIDEO_CHANNEL_REGIONS_PATH = "videoChannelRegionsPath";
    
    @Property(label = "Video Page Regions Path",
        description = "Path to content node containing default regions and contents for the Video Page template",
        value = "/content/food/regions/video-page-regions")
    private static final String VIDEO_PAGE_REGIONS_PATH = "videoPageRegionsPath";
    
    @Property(label = "Video Player Page Regions Path",
        description = "Path to content node containing default regions and contents for the Video Player Page template",
        value = "/content/food/regions/video-player-page-regions")
    private static final String VIDEO_PLAYER_PAGE_REGIONS_PATH = "videoPlayerPageRegionsPath";
    
    @Property(label = "Video Player Page Regions Path",
        description = "Path to the content node containing default regions and content for Video Player page",
        value = "/content/food/regions/video-player-regions")
    private static final String VIDEO_PLAYER_REGIONS_PATH = "videoPlayerRegionsPath";

    @Property(label = "Web Series Page Regions Path",
        description = "Path to the content node containing default region paths for Web Series pages",
        value = "/content/food/regions/web-series-page-regions")
    private static final String WEB_SERIES_PAGE_REGIONS_PATH = "webSeriesPageRegionsPath";
    
    @Property(label = "Weekly Program Guide URL",
        description = "URL for Weekly Program Guide page",
        value = "/content/food/shows/tv-schedule-weekly")
    private static final String WEEKLY_PROGRAM_GUIDE_URL = "weeklyProgramGuideURL";
   
    @Property(label = "OTR Search Results URL",
        description = "URL for OTR Search Results page",
        value = "/content/food/otr-search-results.html")
    private static final String OTR_SEARCH_RESULTS_URL = "otrSearchResultsURL";

    @Property(label = "Recipe Search Results URL",
        description = "URL for Recipe Search Results page",
        value = "/content/food/search-results.recipes.html")
    private static final String RECIPE_SEARCH_RESULTS_URL = "recipeSearchResultsURL";
    
    @Property(label = "Video Search Results URL",
        description = "URL for Video Search Results page",
        value = "/content/food/search-results.videos.html")
    private static final String VIDEO_SEARCH_RESULTS_URL = "videoSearchResultsURL";
    
    @Property(label = "Topics Pages 2+ Modules",
        description = "List of modules that belong on pages 2+ of Topic pages.",
        value = {"/components/modules/breadcrumb", "/components/pagetypes/topic/components/title-description", "/components/modules/global-toolbar", 
            "/components/modules/topic-results", "/components/modules/pagination-simple", "/components/pagetypes/topic/components/more-topics"})
    private static final String TOPICS_PAGES_TWO_PLUS_MODULES = "topicsPagesTwoPlusModules";

    @Property(label = "Modal Window Path",
            description = "URL for Modal Window",
            value = "/content/food/modal-mobile/media.html")
    private static final String MODAL_WINDOW_PAGE_PATH = "modal.page.path";

//-------------------- Members
    private volatile String articleSimpleRegionsPath;
    private volatile String articleStepByStepRegionsPath;    
    private volatile String bioPageRegionsPath;
    private volatile String contactUsURL;
    private volatile String dailyProgramGuideURL;
    private volatile String mobileDailyProgramGuideURL;
    private volatile String episodeArchiveRegionsPath;
    private volatile String episodeRegionsPath;
    private volatile String episodeSearchResultsRegionsPath;
    private volatile String episodeSearchResultsURL;
    private volatile String fiveHundredPath;
    private volatile String footerPath;
    private volatile String fourOhFourPath;
    private volatile String freeFormPageRegionPath;
    private volatile String headerPath;
    private volatile String indexPageRegionsPath;
    private volatile String nutritionPageRegionsPath;
    private volatile String packageLandingPageRegionsPath;
    private volatile String photoGalleryRegionsPath;
    private volatile String recipeDetailPageRegionsPath;
    private volatile String searchResultsRegionsPath;
    private volatile String searchResultsURL;
    private volatile String showPageRegionsPath; 
    private volatile String showSectionRegionsPath;
    private volatile String showTopRecipiesPageRegionsPath; 
    private volatile String showsAtoZURL;
    private volatile String siteName;
    private volatile String sniActivateAssetWorkflowPath;
    private volatile String sponsoredPodTeaserPosition1Path;
    private volatile String sponsoredPodTeaserPosition2Path;
    private volatile String sponsoredPodTeaserPosition3Path;
    private volatile String talentPageRegionsPath;
    private volatile String talentTopRecipesPath;    
    private volatile String topicRegionsPath;
    private volatile String topicsAtoZURL;
    private volatile String recipesAtoZURL;
    private volatile String universalLandingPageRegionsPath;
    private volatile String videoChannelRegionsPath;
    private volatile String videoPageRegionsPath;
    private volatile String videoPlayerPageRegionsPath;
    private volatile String videoPlayerRegionsPath;
    private volatile String webSeriesPageRegionsPath;  
    private volatile String weeklyProgramGuideURL;
    private volatile String otrSearchResultsURL;
    private volatile String modalWindowPath;
    private volatile String recipeSearchResultsURL;
    private volatile String videoSearchResultsURL;
    private volatile String[] topicsPagesTwoPlusModules;

//-------------------- Lifecycle behaviors
    
    /**
     * called by {@link #activate} and {@link #modified} to update Property values of this bean
     * @param ctx componentContext passed in by framework
     */
    private void mutatePropertiesFromContext(ComponentContext ctx){
        Dictionary<?, ?> props = ctx.getProperties();
        this.articleSimpleRegionsPath = OsgiUtil.toString(props.get(ARTICLE_SIMPLE_REGIONS_PATH), null);
        this.articleStepByStepRegionsPath = OsgiUtil.toString(props.get(ARTICLE_STEP_BY_STEP_REGIONS_PATH), null);        
        this.bioPageRegionsPath = OsgiUtil.toString(props.get(BIO_PAGE_REGIONS_PATH), null);
        this.contactUsURL = OsgiUtil.toString(props.get(CONTACT_US_URL), null);
        this.dailyProgramGuideURL = OsgiUtil.toString(props.get(DAILY_PROGRAM_GUIDE_URL), null);
        this.mobileDailyProgramGuideURL = OsgiUtil.toString(props.get(MOBILE_DAILY_PROGRAM_GUIDE_URL),null);
        this.episodeArchiveRegionsPath = OsgiUtil.toString(props.get(EPISODE_ARCHIVE_REGIONS_PATH), null);
        this.episodeRegionsPath = OsgiUtil.toString(props.get(EPISODE_REGIONS_PATH), null);
        this.episodeSearchResultsRegionsPath = OsgiUtil.toString(props.get(EPISODE_SEARCH_RESULTS_REGIONS_PATH), null);
        this.episodeSearchResultsURL = OsgiUtil.toString(props.get(EPISODE_SEARCH_RESULTS_URL), null);
        this.fiveHundredPath = OsgiUtil.toString(props.get(FIVE_HUNDRED_PATH), null);
        this.footerPath = OsgiUtil.toString(props.get(FOOTER_PATH), null);
        this.fourOhFourPath = OsgiUtil.toString(props.get(FOUR_OH_FOUR_PATH), null);
        this.freeFormPageRegionPath = OsgiUtil.toString(props.get(FREE_FORM_REGIONS_PATH), null);
        this.headerPath = OsgiUtil.toString(props.get(HEADER_PATH), null);
        this.indexPageRegionsPath = OsgiUtil.toString(props.get(INDEX_PAGE_REGIONS_PATH), null);
        this.nutritionPageRegionsPath = OsgiUtil.toString(props.get(NUTRITION_PAGE_REGIONS_PATH), null);
        this.packageLandingPageRegionsPath = OsgiUtil.toString(props.get(PACKAGE_LANDING_PAGE_REGIONS_PATH), null);
        this.photoGalleryRegionsPath = OsgiUtil.toString(props.get(PHOTO_GALLERY_REGIONS_PATH), null);
        this.recipeDetailPageRegionsPath = OsgiUtil.toString(props.get(RECIPE_DETAIL_PAGE_REGIONS_PATH), null);
        this.searchResultsRegionsPath = OsgiUtil.toString(props.get(SEARCH_RESULTS_REGIONS_PATH), null);
        this.searchResultsURL = OsgiUtil.toString(props.get(SEARCH_RESULTS_URL), null);
        this.showPageRegionsPath = OsgiUtil.toString(props.get(SHOW_PAGE_REGIONS_PATH), null);
        this.showSectionRegionsPath = OsgiUtil.toString(props.get(SHOW_SECTION_REGIONS_PATH), null);
        this.showTopRecipiesPageRegionsPath = OsgiUtil.toString(props.get(SHOW_TOP_RECIPES_PAGE_REGIONS_PATH), null);
        this.showsAtoZURL = OsgiUtil.toString(props.get(SHOWS_A_Z_URL), null);
        this.siteName = OsgiUtil.toString(props.get(SITE_NAME), null);
        this.sniActivateAssetWorkflowPath = OsgiUtil.toString(props.get(SNI_ACTIVATE_ASSET_WORKFLOW_PATH), null);
        this.sponsoredPodTeaserPosition1Path = OsgiUtil.toString(props.get(SPONSORED_POD_TEASER_POSITION_1_PATH), null);
        this.sponsoredPodTeaserPosition2Path = OsgiUtil.toString(props.get(SPONSORED_POD_TEASER_POSITION_2_PATH), null);
        this.sponsoredPodTeaserPosition3Path = OsgiUtil.toString(props.get(SPONSORED_POD_TEASER_POSITION_3_PATH), null);
        this.talentPageRegionsPath = OsgiUtil.toString(props.get(TALENT_PAGE_REGIONS_PATH), null);
        this.talentTopRecipesPath = OsgiUtil.toString(props.get(TALENT_TOP_RECIPES_PATH), null);        
        this.topicRegionsPath = OsgiUtil.toString(props.get(TOPIC_REGIONS_PATH), null);
        this.topicsAtoZURL = OsgiUtil.toString(props.get(TOPICS_A_Z_URL), null);
        this.recipesAtoZURL = OsgiUtil.toString(props.get(RECIPES_A_Z_URL), null);
        this.modalWindowPath = OsgiUtil.toString(props.get(MODAL_WINDOW_PAGE_PATH),null);
        this.videoChannelRegionsPath = OsgiUtil.toString(props.get(VIDEO_CHANNEL_REGIONS_PATH), null);
        this.videoPageRegionsPath = OsgiUtil.toString(props.get(VIDEO_PAGE_REGIONS_PATH), null);
        this.videoPlayerPageRegionsPath = OsgiUtil.toString(props.get(VIDEO_PLAYER_PAGE_REGIONS_PATH), null);
        this.videoPlayerRegionsPath = OsgiUtil.toString(props.get(VIDEO_PLAYER_REGIONS_PATH), null);
        this.webSeriesPageRegionsPath = OsgiUtil.toString(props.get(WEB_SERIES_PAGE_REGIONS_PATH), null);
        this.weeklyProgramGuideURL = OsgiUtil.toString(props.get(WEEKLY_PROGRAM_GUIDE_URL), null);
        this.otrSearchResultsURL = OsgiUtil.toString(props.get(OTR_SEARCH_RESULTS_URL), null);
        this.recipeSearchResultsURL = OsgiUtil.toString(props.get(RECIPE_SEARCH_RESULTS_URL), null);
        this.videoSearchResultsURL = OsgiUtil.toString(props.get(VIDEO_SEARCH_RESULTS_URL), null);
        this.topicsPagesTwoPlusModules = OsgiUtil.toStringArray(props.get(TOPICS_PAGES_TWO_PLUS_MODULES));
        if (this.topicsPagesTwoPlusModules == null) {
            this.topicsPagesTwoPlusModules = new String[0];
        }
    }
    
    /**
     * Activates this OSGi component, setting its properties from the ComponentContext and
     * initializing its state.
     */
    @Activate
    protected void activate(ComponentContext ctx) {
        log.info("Activating service properties on "+this.getClass());
        mutatePropertiesFromContext(ctx);
    }
    
    /**
     * Updates the state of this OSGi component when the ComponentContext has changed.
     */
    @Modified
    protected void modified(ComponentContext ctx) {
        log.info("Modifying service properties on "+this.getClass());
        mutatePropertiesFromContext(ctx);
    }

//-------------------- Getter/Setters
    public String getArticleSimpleRegionsPath() {
        return articleSimpleRegionsPath;
    }

    public void setArticleSimpleRegionsPath(String articleSimpleRegionsPath) {
        this.articleSimpleRegionsPath = articleSimpleRegionsPath;
    }

    public String getArticleStepByStepRegionsPath() {
        return articleStepByStepRegionsPath;
    }

    public void setArticleStepByStepRegionsPath(String articleStepByStepRegionsPath) {
        this.articleStepByStepRegionsPath = articleStepByStepRegionsPath;
    }

    public String getBioPageRegionsPath() {
        return bioPageRegionsPath;
    }

    public void setBioPageRegionsPath(String bioPageRegionsPath) {
        this.bioPageRegionsPath = bioPageRegionsPath;
    }
    
    public String getContactUsURL() {
        return contactUsURL;
    }

    public void setContactUsURL(String contactUsURL) {
        this.contactUsURL = contactUsURL;
    }
    
    public String getDailyProgramGuideURL() {
        return dailyProgramGuideURL;
    }

    public void setDailyProgramGuideURL(String dailyProgramGuideURL) {
        this.dailyProgramGuideURL = dailyProgramGuideURL;        
    }
    
    public String getEpisodeArchiveRegionsPath() {
        return episodeArchiveRegionsPath;
    }

    public void setEpisodeArchiveRegionsPath(String episodeArchiveRegionsPath) {
        this.episodeArchiveRegionsPath = episodeArchiveRegionsPath;
    }

    public String getEpisodeRegionsPath() {
        return episodeRegionsPath;
    }

    public void setEpisodeRegionsPath(String episodeRegionsPath) {
        this.episodeRegionsPath = episodeRegionsPath;
    }
    
    public String getEpisodeSearchResultsRegionsPath() {
        return episodeSearchResultsRegionsPath;
    }

    public void setEpisodeSearchResultsRegionsPath(String episodeSearchResultsRegionsPath) {
        this.episodeSearchResultsRegionsPath = episodeSearchResultsRegionsPath;
    }
    
    public String getEpisodeSearchResultsURL() {
        return episodeSearchResultsURL;
    }

    public void setEpisodeSearchResultsURL(String episodeSearchResultsURL) {
        this.episodeSearchResultsURL = episodeSearchResultsURL;
    }

    public String getFiveHundredPath() {
        return fiveHundredPath;
    }

    public void setFiveHundredPath(String fiveHundredPath) {
        this.fiveHundredPath = fiveHundredPath;
    }

    public String getFooterPath() {
        return footerPath;
    }

    public void setFooterPath(String footerPath) {
        this.footerPath = footerPath;
    }

    public String getFourOhFourPath() {
        return fourOhFourPath;
    }

    public void setFourOhFourPath(String fourOhFourPath) {
        this.fourOhFourPath = fourOhFourPath;
    }
    
    public String getFreeFormPageRegionPath() {
        return freeFormPageRegionPath;
    }

    public void setFreeFormPageRegionPath(String freeFormPageRegionPath) {
        this.freeFormPageRegionPath = freeFormPageRegionPath;
    }

    public String getHeaderPath() {
        return headerPath;
    }

    public void setHeaderPath(String headerPath) {
        this.headerPath = headerPath;
    }

    public String getIndexPageRegionsPath() {
        return indexPageRegionsPath;
    }

    public void setIndexPageRegionsPath(String indexPageRegionsPath) {
        this.indexPageRegionsPath = indexPageRegionsPath;
    }
    
    public String getNutritionPageRegionsPath() {
        return nutritionPageRegionsPath;
    }

    public void setNutritionPageRegionsPath(String nutritionPageRegionsPath) {
        this.nutritionPageRegionsPath = nutritionPageRegionsPath;
    }

    public String getPackageLandingPageRegionsPath() {
        return packageLandingPageRegionsPath;
    }

    public void setPackageLandingPageRegionsPath(String packageLandingPageRegionsPath) {
        this.packageLandingPageRegionsPath = packageLandingPageRegionsPath;
    }

    public String getPhotoGalleryRegionsPath() {
        return photoGalleryRegionsPath;
    }

    public void setPhotoGalleryRegionsPath(String photoGalleryRegionsPath) {
        this.photoGalleryRegionsPath = photoGalleryRegionsPath;
    }

    public String getRecipeDetailPageRegionsPath() {
        return recipeDetailPageRegionsPath;
    }

    public void setRecipeDetailPageRegionsPath(String recipeDetailPageRegionsPath) {
        this.recipeDetailPageRegionsPath = recipeDetailPageRegionsPath;
    }

    public String getSearchResultsRegionsPath() {
        return searchResultsRegionsPath;
    }

    public void setSearchResultsRegionsPath(String searchResultsRegionsPath) {
        this.searchResultsRegionsPath = searchResultsRegionsPath;
    }
    
    public String getSearchResultsURL() {
        return searchResultsURL;
    }

    public void setSearchResultsURL(String searchResultsURL) {
        this.searchResultsURL = searchResultsURL;
    }
    
    public String getShowPageRegionsPath() {
        return showPageRegionsPath;
    }

    public void setShowPageRegionsPath(
            String showPageRegionsPath) {
        this.showPageRegionsPath = showPageRegionsPath;
    }
    
    public String getShowSectionRegionsPath() {
        return showSectionRegionsPath;
    }

    public void setShowSectionRegionsPath(String showSectionRegionsPath) {
        this.showSectionRegionsPath = showSectionRegionsPath;
    }
    
    public String getShowTopRecipiesPageRegionsPath() {
        return showTopRecipiesPageRegionsPath;
    }

    public void setShowTopRecipiesPageRegionsPath(
            String showTopRecipiesPageRegionsPath) {
        this.showTopRecipiesPageRegionsPath = showTopRecipiesPageRegionsPath;
    }
    
    public String getShowsAtoZURL() {
        return showsAtoZURL;
    }

    public void setShowsAtoZURL(String showsAtoZURL) {
        this.showsAtoZURL = showsAtoZURL;
    }
    
    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSNIActivateAssetWorkflowPath() {
        return sniActivateAssetWorkflowPath;
    }

    public void setSNIActivateAssetWorkflowPath(String sniActivateAssetWorkflowPath) {
        this.sniActivateAssetWorkflowPath = sniActivateAssetWorkflowPath;
    }

    public String getSponsoredPodTeaserPosition1Path() {
        return sponsoredPodTeaserPosition1Path;
    }

    public void setSponsoredPodTeaserPosition1Path(String sponsoredPodTeaserPosition1Path) {
        this.sponsoredPodTeaserPosition1Path = sponsoredPodTeaserPosition1Path;
    }

    public String getSponsoredPodTeaserPosition2Path() {
        return sponsoredPodTeaserPosition2Path;
    }

    public void setSponsoredPodTeaserPosition2Path(String sponsoredPodTeaserPosition2Path) {
        this.sponsoredPodTeaserPosition2Path = sponsoredPodTeaserPosition2Path;
    }

    public String getSponsoredPodTeaserPosition3Path() {
        return sponsoredPodTeaserPosition3Path;
    }

    public void setSponsoredPodTeaserPosition3Path(String sponsoredPodTeaserPosition3Path) {
        this.sponsoredPodTeaserPosition3Path = sponsoredPodTeaserPosition3Path;
    }
    
    public String getTalentPageRegionsPath() {
        return talentPageRegionsPath;
    }

    public void setTalentPageRegionsPath(String talentRegionsPath) {
        this.talentPageRegionsPath = talentRegionsPath;
    }
    
    public String getTalentTopRecipesPath() {
        return talentTopRecipesPath;
    }

    public void setTalentTopRecipesPath(String talentTopRecipesPath) {
        this.talentTopRecipesPath = talentTopRecipesPath;
    }

    public String getTopicRegionsPath() {
        return topicRegionsPath;
    }

    public void setTopicRegionsPath(String topicRegionsPath) {
        this.topicRegionsPath = topicRegionsPath;
    }
    
    public String getTopicsAtoZURL() {
        return topicsAtoZURL;
    }

    public void setTopicsAtoZURL(String topicsAtoZURL) {
        this.topicsAtoZURL = topicsAtoZURL;
    }

    public String getRecipesAtoZURL() {
        return recipesAtoZURL;
    }

    public void setRecipesAtoZURL(String recipesAtoZURL) {
        this.recipesAtoZURL = recipesAtoZURL;
    }    
    
    public String getVideoChannelRegionsPath(){
        return videoChannelRegionsPath;
    }
    
    public void setVideoChannelRegionsPath(String videoChannelRegionsPath) {
        this.videoChannelRegionsPath = videoChannelRegionsPath;
    }

    public String getVideoPageRegionsPath() {
        return videoPageRegionsPath;
    }

    public void setVideoPageRegionsPath(String videoPageRegionsPath) {
        this.videoPageRegionsPath = videoPageRegionsPath;
    }

    public String getVideoPlayerPageRegionsPath() {
        return videoPlayerPageRegionsPath;
    }

    public void setVideoPlayerPageRegionsPath(String videoPlayerPageRegionsPath) {
        this.videoPlayerPageRegionsPath = videoPlayerPageRegionsPath;
    }
    
    public String getVideoPlayerRegionsPath(){
        return videoPlayerRegionsPath;
    }
    
    public void setVideoPlayerRegionsPath(String videoPlayerRegionsPath) {
        this.videoPlayerRegionsPath = videoPlayerRegionsPath;
    }
    
    public String getWebSeriesPageRegionsPath() {
        return webSeriesPageRegionsPath;
    }

    public void setWebSeriesPageRegionsPath(String webSeriesPageRegionsPath) {
        this.webSeriesPageRegionsPath = webSeriesPageRegionsPath;
    }
    
    public String getWeeklyProgramGuideURL() {        
        return weeklyProgramGuideURL;
    }

    public void setWeeklyProgramGuideURL(String weeklyProgramGuideURL) {
        this.weeklyProgramGuideURL = weeklyProgramGuideURL;
    }

    public String getOtrSearchResultsURL() {
        return otrSearchResultsURL;
    }

    public void setOtrSearchResultsURL(String otrSearchResultsURL) {
        this.otrSearchResultsURL = otrSearchResultsURL;
    }

    public String getRecipeSearchResultsURL() {
        return recipeSearchResultsURL;
    }

    public void setRecipeSearchResultsURL(String recipeSearchResultsURL) {
        this.recipeSearchResultsURL = recipeSearchResultsURL;
    }
    
    public String getVideoSearchResultsURL() {
        return videoSearchResultsURL;
    }

    public void setVideoSearchResultsURL(String videoSearchResultsURL) {
        this.videoSearchResultsURL = videoSearchResultsURL;
    }
    
    public String[] getTopicsPagesTwoPlusModules() {
        return topicsPagesTwoPlusModules;
    }

    public void setTopicsPagesTwoPlusModules(String[] topicsPagesTwoPlusModules) {
        this.topicsPagesTwoPlusModules = topicsPagesTwoPlusModules;
    }

    public String getMobileDailyProgramGuideURL() {
        return mobileDailyProgramGuideURL;
    }

    public void setMobileDailyProgramGuideURL(String mobileDailyProgramGuideURL) {
        this.mobileDailyProgramGuideURL = mobileDailyProgramGuideURL;
    }

    public String getModalWindowPath() {
        return modalWindowPath;
    }

    public void setModalWindowPath(String modalWindowPath) {
        this.modalWindowPath = modalWindowPath;
    }
}
