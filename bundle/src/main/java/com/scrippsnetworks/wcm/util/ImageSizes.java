package com.scrippsnetworks.wcm.util;

/**
 * @author Jason Clark
 * Date: 10/18/12
 */
public enum ImageSizes {
    LG_HORIZONTAL (616, 462),
    LG_VERTICAL (616, 821),
    LEAD (400, 300),
    AL_HORIZONTAL (266, 200),
    AL_VERTICAL (266, 354),
    MED (160, 120),
    TZ_HORIZONTAL (120, 90),
    TZ_VERTICAL (90, 120),
    SM (92, 69);

    private final int width;
    private final int height;

    private ImageSizes(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int width() {return this.width;}
    public int height() {return this.height;}
}
