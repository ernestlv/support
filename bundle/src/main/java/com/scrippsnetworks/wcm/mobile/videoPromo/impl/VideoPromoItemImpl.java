package com.scrippsnetworks.wcm.mobile.videoPromo.impl;

import com.scrippsnetworks.wcm.mobile.videoPromo.VideoPromoItem;

/**
 * Created by Dzmitry_Drepin on 2/17/14.
 */
public class VideoPromoItemImpl implements VideoPromoItem{
    private String title;
    private String time;
    private String url;
    private String imageDamPath;

    public String getTitle() {
        return title;
    }

    public VideoPromoItemImpl setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getTime() {
        return time;
    }

    public VideoPromoItemImpl setTime(String time) {
        this.time = time;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public VideoPromoItemImpl setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getImageDamPath() {
        return imageDamPath;
    }

    public VideoPromoItemImpl setImageDamPath(String imageDamPath) {
        this.imageDamPath = imageDamPath;
        return this;
    }
}
