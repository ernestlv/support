package com.scrippsnetworks.wcm.image;

/**
 * @author Jason Clark
 *         Date: 4/28/13
 */
public interface SniImage {
    public String getAspect();
    public String getCanonicalUrl();
    public String getCaption();
    public String getPath();
    public String getRendition();
    public String getTitle();
    public String getUrl();
    public int height();
    public int width();
    public boolean isRendition();
}
