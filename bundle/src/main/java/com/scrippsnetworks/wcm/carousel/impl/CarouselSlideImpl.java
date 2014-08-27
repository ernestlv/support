package com.scrippsnetworks.wcm.carousel.impl;

import com.scrippsnetworks.wcm.carousel.CarouselSlide;

/**
 *
 * @author Patrick Armstrong (parmstrong@siteworx.com)
 */
public class CarouselSlideImpl implements CarouselSlide {

    private String link, imgPath, desc;
    private boolean showIcon, endFrame;
    
    public CarouselSlideImpl(String link, String imgPath, String desc, boolean showIcon, boolean endFrame) {
        this.link = link;
        this.imgPath = imgPath;
        this.desc = desc;
        this.showIcon = showIcon;
        this.endFrame = endFrame;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String getImgPath() {
        return imgPath;
    }

    @Override
    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    @Override
    public String getLink() {
        return link;
    }

    @Override
    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public boolean isShowIcon() {
        return showIcon;
    }

    @Override
    public void setShowIcon(boolean showIcon) {
        this.showIcon = showIcon;
    }
    
    @Override
    public boolean isEndFrame() {
        return endFrame;
    }

    @Override
    public void setEndFrame(boolean endFrame) {
        this.endFrame = endFrame;
    }
    
}
