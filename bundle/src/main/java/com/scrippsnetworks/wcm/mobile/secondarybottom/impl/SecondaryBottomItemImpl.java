package com.scrippsnetworks.wcm.mobile.secondarybottom.impl;

import com.scrippsnetworks.wcm.mobile.base.link.Link;
import com.scrippsnetworks.wcm.mobile.secondarybottom.SecondaryBottomItem;

import java.util.List;


public class SecondaryBottomItemImpl implements SecondaryBottomItem {
    private String title;
    private String imageDamPath;
    private String url;
    private String cssClassName;
    private String description;
    private List<Link> links;


    public String getTitle() {
        return title;
    }

    public SecondaryBottomItemImpl setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getImageDamPath() {
        return imageDamPath;
    }

    public SecondaryBottomItemImpl setImageDamPath(String imageDamPath) {
        this.imageDamPath = imageDamPath;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public SecondaryBottomItemImpl setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getCssClassName() {
        return cssClassName;
    }

    public SecondaryBottomItemImpl setCssClassName(String cssClassName) {
        this.cssClassName = cssClassName;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public SecondaryBottomItemImpl setDescription(String description) {
        this.description = description;
        return this;
    }

    public List<Link> getLinks() {
        return links;
    }

    public SecondaryBottomItemImpl setLinks(List<Link> links) {
        this.links = links;
        return this;
    }
}
