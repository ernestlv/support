package com.scrippsnetworks.wcm.carousel;

/**
 *
 * @author Patrick Armstrong (parmstrong@siteworx.com)
 */
public interface CarouselSlide {
    
    public String getDesc();
    public void setDesc(String desc);

    public String getImgPath();
    public void setImgPath(String imgPath);

    public String getLink();
    public void setLink(String link);
    
    public boolean isShowIcon();
    public void setShowIcon(boolean showIcon);
    
    public boolean isEndFrame();
    public void setEndFrame(boolean endFrame);
    
}
