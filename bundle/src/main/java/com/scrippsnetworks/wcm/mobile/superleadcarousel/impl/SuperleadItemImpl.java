package com.scrippsnetworks.wcm.mobile.superleadcarousel.impl;

import com.scrippsnetworks.wcm.mobile.superleadcarousel.SuperleadItem;

public class SuperleadItemImpl implements SuperleadItem{
    private String title;
    private String descr;
    private String imageDamPath;
    private String url;
    private String cssClassName;

    public SuperleadItemImpl(String title, String descr, String imageDamPath, String url, String cssClassName) {
        this.title = title;
        this.descr = descr;
        this.imageDamPath = imageDamPath;
        this.url = url;
        this.cssClassName = cssClassName;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescr() {
        return descr;
    }

    @Override
    public String getImageDamPath() {
        return imageDamPath;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getCssClassName() {
        return cssClassName;
    }
}
