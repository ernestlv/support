package com.scrippsnetworks.wcm.util;

/**
 * @author Jason Clark
 *         Date: 4/28/13
 */
public enum ContentRegionNames {
    CONTENT_WELL ("content-well"),
    SUPERLEAD ("superlead"),
    RIGHT_RAIL ("right-rail"),
    CONTENT_WELL_PAGINATED ("content-well-paginated");

    private String regionName;

    private ContentRegionNames(final String name) {
        this.regionName = name;
    }

    public String regionName() {return this.regionName;}
}
