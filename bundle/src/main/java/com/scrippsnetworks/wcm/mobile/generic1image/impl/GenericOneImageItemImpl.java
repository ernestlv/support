package com.scrippsnetworks.wcm.mobile.generic1image.impl;

import com.scrippsnetworks.wcm.mobile.generic1image.GenericOneImageItem;

/**
 * Created by Dzmitry_Drepin on 1/29/14.
 */
public class GenericOneImageItemImpl implements GenericOneImageItem {
    private String title;
    private String imageDamPath;
    private String url;
    private String cssClassName;
    private String eyebrow;
    private String caption;

    public String getTitle() {
        return title;
    }

    public GenericOneImageItemImpl setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getImageDamPath() {
        return imageDamPath;
    }

    public GenericOneImageItemImpl setImageDamPath(String imageDamPath) {
        this.imageDamPath = imageDamPath;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public GenericOneImageItemImpl setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getCssClassName() {
        return cssClassName;
    }

    public GenericOneImageItemImpl setCssClassName(String cssClassName) {
        this.cssClassName = cssClassName;
        return this;
    }

    public String getEyebrow() {
        return eyebrow;
    }

    public GenericOneImageItemImpl setEyebrow(String eyebrow) {
        this.eyebrow = eyebrow;
        return this;
    }

    public String getCaption() {
        return caption;
    }

    public GenericOneImageItemImpl setCaption(String caption) {
        this.caption = caption;
        return this;
    }
}
