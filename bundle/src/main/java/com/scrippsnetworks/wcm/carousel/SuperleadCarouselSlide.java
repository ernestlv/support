/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scrippsnetworks.wcm.carousel;

/**
 *
 * @author Patrick
 */
public interface SuperleadCarouselSlide {
    
    public String getSLUG();
    public String getDescription();
    public String getImagePath();
    public String getLink();
    public boolean isVideoThumbnail();
    public String getIconCls();
    String getMobileIconCls();
    
}
