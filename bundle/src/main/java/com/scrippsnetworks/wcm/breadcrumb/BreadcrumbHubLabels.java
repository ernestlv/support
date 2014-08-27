package com.scrippsnetworks.wcm.breadcrumb;

/**
 * @author Jonathan Bell
 *         Date: 10/2/2013
 */
public enum BreadcrumbHubLabels {
    ARTICLE_SIMPLE("Article"),
    ASSET_RECIPES("Recipes"),
    BIO("Bio"),
    EPISODE_LISTING("Episodes"),
    MENU("Menu"),
    MENU_LISTING("Menus"),
    PHOTOGALLERY("Photos"),
    PHOTOGALLERY_LISTING("Photos"),
    RECIPE_LISTING("Recipes"),
    VIDEO("Video"),
    VIDEO_CHANNEL("Videos"),
    VIDEO_PLAYER("Videos");

    private String title;

    private BreadcrumbHubLabels(final String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }

}
