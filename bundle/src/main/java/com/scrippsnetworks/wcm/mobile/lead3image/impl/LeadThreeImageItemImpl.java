package com.scrippsnetworks.wcm.mobile.lead3image.impl;

import com.scrippsnetworks.wcm.mobile.lead3image.LeadThreeImageItem;

/**
 * Created by Dzmitry_Drepin on 1/29/14.
 */
public class LeadThreeImageItemImpl implements LeadThreeImageItem {
    private String title; //lead3image.item.title
    private String imageDamPath;   //lead3image.item.imageDamPath
    private String url;//lead3image.item.url
    private String cssClassName; //lead3image.item.cssClassName
    private String description;

    public String getTitle() {
        return title;
    }

    public LeadThreeImageItemImpl setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getImageDamPath() {
        return imageDamPath;
    }

    public LeadThreeImageItemImpl setImageDamPath(String imageDamPath) {
        this.imageDamPath = imageDamPath;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public LeadThreeImageItemImpl setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getCssClassName() {
        return cssClassName;
    }

    public LeadThreeImageItemImpl setCssClassName(String cssClassName) {
        this.cssClassName = cssClassName;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public LeadThreeImageItemImpl setDescription(String description) {
        this.description = description;
        return this;
    }
}
