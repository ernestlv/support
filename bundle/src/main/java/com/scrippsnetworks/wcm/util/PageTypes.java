package com.scrippsnetworks.wcm.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Jason Clark
 *         Date: 6/30/13
 */
public enum PageTypes {
    ARTICLE_SIMPLE("article-simple"),
    ASSET_RECIPES("asset-recipes"),
    BEVERAGE("beverage"),
    BIO("bio"),
    EPISODE("episode"),
    COMPANY("company"),
    HOMEPAGE("homepage"),
    INDEX("index"),
    MENU("menu"),
    MENU_LISTING("menu-listing"),
    PHOTOGALLERY("photo-gallery"),
    PHOTOGALLERY_LISTING("photo-gallery-listing"),
    RECIPE("recipe"),
    RECIPE_LISTING("recipe-listing"),
    SEARCH_RESULTS("search-results"),
    SECTION("section"),
    SHOW("show"),
    STRUCTURAL("structural"),
    TALENT("talent"),
    TOPIC("topic"),
    UNIVERSAL_LANDING("universal-landing"),
    VIDEO("video"),
    VIDEO_CHANNEL("video-channel"),
    VIDEO_PLAYER("video-player"),
    FREE_FORM_TEXT("free-form-text"),
    COLLECTION("wcm-collection"),
    WCM_FREEFORM("wcm-freeform"),
    SOURCE_RECIPES("source-recipes"),
    CALENDAR("calendar"),
    EPISODE_LISTING("episode-listing");

    private String pageType;

    private PageTypes(final String pageType) {
        this.pageType = pageType;
    }

    public String pageType() {
        return pageType;
    }

    /** For identifying your PageType by the pageType value that comes from SniPage. */
    public static PageTypes findPageType(final String typeText) {
        if (StringUtils.isNotBlank(typeText)) {
            for (PageTypes type : PageTypes.values()) {
                if (typeText.equals(type.pageType())) {
                    return type;
                }
            }
        }
        return null;
    }
}
