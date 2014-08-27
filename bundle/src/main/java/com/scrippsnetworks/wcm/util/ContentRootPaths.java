package com.scrippsnetworks.wcm.util;

/**
 * Enum for content root paths in CRX
 * @author Jason Clark
 *         Date: 11/16/12
 */
public enum ContentRootPaths {

    CONTENT_COOK ("/content/cook"),
    CONTENT_COOK_MOBILE ("/content/cook-mobile"),
    CONTENT_FOOD ("/content/food"),
    CONTENT_FOOD_TEST ("/content/food/test"),
    CHEFS ("/content/food/chefs"),
    SHOWS ("/content/food/shows"),
    RECIPES ("/content/food/recipes"),
    VIDEOS ("/content/food/videos"),
    TOPICS ("/content/food/topics"),
    TOPICS_A_Z ("/content/food/topics/a-z"),
    HOSTS ("/content/food/hosts"),
    GUEST_CHEFS ("/content/food/chefs/gc");

    private String path;

    private ContentRootPaths(final String path) {
        this.path = path;
    }

    public String path() {return this.path;}

}
