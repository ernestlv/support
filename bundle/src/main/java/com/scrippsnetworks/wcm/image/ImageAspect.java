package com.scrippsnetworks.wcm.image;

/** Defines the allowable set of aspect names for image renditions.
 *
 * @author Scott Everett Johnson
 */
public enum ImageAspect {

    landscape(4,3),
    portrait(3,4),
    square(1,1),
    wide(16,9);

    private final int x;
    private final int y;

    ImageAspect(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** Returns the ratio of this aspect. */
    public double ratio() {
        return (double) x / y;
    }

}
