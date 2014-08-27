package com.scrippsnetworks.wcm.fnr.util;

import java.util.EnumSet;

/**
 * Enum for sling:resourceType properties of all pagetypes
 * These are based on the specification defined here:
 *
 * https://wiki.scrippsnetworks.com/display/wcm/ir-fni-food-page-types
 *
 * @author Jason Clark
 *         Date: 11/16/12
 */
public enum PageSlingResourceTypes {

    BASE_UTIL_PAGE ("sni-food/components/util/page"),
    PAGE_TYPE_ROOT("sni-food/components/pagetypes"),
    PAGE_TYPE_MOBILE_ROOT ("sni-food/components/pagetypes/mobile"),
    RECIPE ("sni-food/components/pagetypes/recipe"),
    RECIPE_MOBILE ("sni-food/components/pagetypes/mobile/recipe"),
    SHOW ("sni-food/components/pagetypes/show"),
    SHOW_MOBILE("sni-food/components/pagetypes/mobile/show"),
    EPISODE ("sni-food/components/pagetypes/episode"),
    TALENT ("sni-food/components/pagetypes/talent"),
    PHOTO_GALLERY ("sni-food/components/pagetypes/photo-gallery"),
    PHOTO_GALLERY_MOBILE ("sni-food/components/pagetypes/mobile/photo-gallery"),
    PHOTO_GALLERY_LISTING ("sni-food/components/pagetypes/photo-gallery-listing"),
    BIO ("sni-food/components/pagetypes/bio"),
    VIDEO ("sni-food/components/pagetypes/video"),
    VIDEO_CHANNEL ("sni-food/components/pagetypes/video-channel"),
    VIDEO_CHANNEL_MOBILE ("sni-food/components/pagetypes/mobile/video-channel"),
    VIDEO_PLAYER ("sni-food/components/pagetypes/video-player"),
    VIDEO_PLAYER_MOBILE ("sni-food/components/pagetypes/mobile/video-player"),
    TOPIC ("sni-food/components/pagetypes/topic"),
    ARTICLE_SIMPLE ("sni-food/components/pagetypes/article-simple"),
    ARTICLE_STEP_BY_STEP ("sni-food/components/pagetypes/article-step-by-step"),
    CHEFS_AND_HOSTS_SECTION ("sni-food/components/pagetypes/chefs-and-hosts-section"),
    EPISODE_ARCHIVE ("sni-food/components/pagetypes/episode-archive"),
    EPISODE_LISTING ("sni-food/components/pagetypes/episode-listing"),
    EPISODE_SEARCH_RESULTS ("sni-food/components/pagetypes/episode-search-results"),
    INDEX ("sni-food/components/pagetypes/index"),
    PACKAGE_LANDING ("sni-food/components/pagetypes/package-landing"),
    PROGRAM_GUIDE_DAILY ("sni-food/components/pagetypes/program-guide-daily"),
    PROGRAM_GUIDE_WEEKLY ("sni-food/components/pagetypes/program-guide-weekly"),
    RECIPE_SECTION ("sni-food/components/pagetypes/recipe-section"),
    RECIPE_LISTING ("sni-food/components/pagetypes/recipe-listing"),
    RECIPE_NUTRITION ("sni-food/components/pagetypes/recipe-nutrition"),
    RECIPE_REVIEWS ("sni-food/components/pagetypes/recipe-reviews"),
    SEARCH_RESULTS ("sni-food/components/pagetypes/search-results"),
    SHOW_SECTION ("sni-food/components/pagetypes/show-section"),
    SHOW_TOP_RECIPES ("sni-food/components/pagetypes/show-recipes"),
    TALENT_TOP_RECIPES ("sni-food/components/pagetypes/talent-recipes"),
    SPONSORSHIP ("sni-food/components/pagetypes/sponsorship"),
    FREE_FORM_TEXT ("sni-food/components/pagetypes/free-form-text"),
    FREE_FORM_TEXT_WITH_RRAIL ("sni-food/components/pagetypes/free-form-text-w-rail"),
    COMPANY ("sni-food/components/pagetypes/company"),
    SECTION ("sni-food/components/pagetypes/section"),
    MENU ("sni-food/components/pagetypes/menu"),
    MENU_LISTING("sni-food/components/pagetypes/menu-listing"),
    ASSET_RECIPES ("sni-food/components/pagetypes/asset-recipes"),
    HOMEPAGE ("sni-food/components/pagetypes/homepage"),
    UNIVERSAL_LANDING ("sni-food/components/pagetypes/universal-landing"),
    WEB_SERIES ("sni-food/components/pagetypes/web-series");

    private String resourceType;

    private PageSlingResourceTypes(final String type) {
        this.resourceType = type;
    }

    public String resourceType() {return this.resourceType;}

    /* GENERATED PAGE TYPES */
    public static final EnumSet<PageSlingResourceTypes> GENERATED_TYPES =
            EnumSet.of(TALENT_TOP_RECIPES,
                    SHOW_TOP_RECIPES,
                    RECIPE_NUTRITION,
                    RECIPE_REVIEWS,
                    EPISODE_LISTING);
}
