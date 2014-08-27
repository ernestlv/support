package com.scrippsnetworks.wcm.fnr.util;

/**
 * @author Jason Clark
 *         Date: 11/16/12
 */
public enum AssetRootPaths {

    ASSET_ROOT ("/etc/sni-asset"),
    SHOWS ("/etc/sni-asset/food/shows"),
    RECIPES ("/etc/sni-asset/recipes"),
    PEOPLE ("/etc/sni-asset/people"),
    SCHEDULES ("/etc/sni-asset/schedules/food"),
    CHEFS ("/etc/sni-asset/people/chef"),
    VIDEOS ("/etc/sni-asset/food/videos"),
    RESTAURANTS("/content/food/restaurants");

    private String path;

    private AssetRootPaths(final String path) {this.path = path;}

    public String path() {return this.path;}

}
