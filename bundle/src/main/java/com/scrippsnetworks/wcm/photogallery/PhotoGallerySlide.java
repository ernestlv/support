package com.scrippsnetworks.wcm.photogallery;

import org.apache.sling.api.resource.Resource;
import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * This represents the Photo Gallery Slide component in CQ.
 * @author Jason Clark
 *         Date: 4/28/13
 */
public interface PhotoGallerySlide {
    public SniImage getSniImage();
    public String getCaption();
    public String getTitle();
    public SniPage getAssetPage();
    public String getAssetType();
    public String getJcrPath();
    public String getCredit();
    public String getLinkLabel();
    public String getLinkUrl();
    public String getLinkText();
    public boolean getFreeEnabled();
    public String getFreeLinkLabel();
    public String getFreeLinkText();
    public String getIconType();
}
