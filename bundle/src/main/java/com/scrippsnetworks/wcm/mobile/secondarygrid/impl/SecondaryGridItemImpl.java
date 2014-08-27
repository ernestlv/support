package com.scrippsnetworks.wcm.mobile.secondarygrid.impl;

import com.scrippsnetworks.wcm.mobile.secondarygrid.SecondaryGridItem;

/**
 * Created by Dzmitry_Drepin on 1/29/14.
 */
public class SecondaryGridItemImpl implements SecondaryGridItem {
    private String title; //subheader
    private String altText; //imgalttext
    private String imageDamPath;
    private String url;//imageLink
    private String cssClassName; //style
    private String description;


    public String getCssClassName() {
        return cssClassName;
    }

    public SecondaryGridItemImpl setCssClassName(String cssClassName) {
        this.cssClassName = cssClassName;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public SecondaryGridItemImpl setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getImageDamPath() {
        return imageDamPath;
    }

    public SecondaryGridItemImpl setImageDamPath(String imageDamPath) {
        this.imageDamPath = imageDamPath;
        return this;
    }

    public String getAltText() {
        return altText;
    }

    public SecondaryGridItemImpl setAltText(String altText) {
        this.altText = altText;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public SecondaryGridItemImpl setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public SecondaryGridItemImpl setDescription(String description) {
        this.description = description;
        return this;
    }
}
