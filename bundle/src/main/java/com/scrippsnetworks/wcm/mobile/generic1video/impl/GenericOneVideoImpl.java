package com.scrippsnetworks.wcm.mobile.generic1video.impl;

import com.scrippsnetworks.wcm.mobile.generic1video.GenericOneVideo;

/**
  * Charles E. Lewis Jr, 2014-04-29,
  *   adapted from GnericOneImageImpl.java by Dzmitry_Drepin on 1/30/14;
 */
 
public class GenericOneVideoImpl implements GenericOneVideo {

    private String channelUrl;
    private String imageDamPath;
    private String headline;
    private String caption;
    private String endSlideLink;

    public String getChannelUrl() {
        return channelUrl;
    }

    public GenericOneVideo setChannelUrl(String channelUrl) {
        this.channelUrl = channelUrl;
        return this;
    }
    
    public String getImageDamPath() {
        return imageDamPath;
    }

    public GenericOneVideo setImageDamPath(String imageDamPath) {
        this.imageDamPath = imageDamPath;
        return this;
    }

    public String getHeadline() {
        return headline;
    }

    public GenericOneVideo setHeadline(String headline) {
        this.headline = headline;
        return this;
    }
    
    public String getCaption() {
        return caption;
    }

    public GenericOneVideo setCaption(String caption) {
        this.caption = caption;
        return this;
    }
    
    public String getEndSlideLink() {
        return endSlideLink;
    }

    public GenericOneVideo setEndSlideLink(String endSlideLink) {
        this.endSlideLink = endSlideLink;
        return this;
    }
    
}
