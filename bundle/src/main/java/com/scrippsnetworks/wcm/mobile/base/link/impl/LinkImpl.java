package com.scrippsnetworks.wcm.mobile.base.link.impl;


import com.scrippsnetworks.wcm.mobile.base.link.Link;

public class LinkImpl implements Link {
    private String linksTitle;
    private String clazz;
    private String href;
    private String title;
    private String target;

    public String getLinksTitle() {
        return linksTitle;
    }

    public LinkImpl setLinksTitle(String linksTitle) {
        this.linksTitle = linksTitle;
        return this;
    }

    public String getClazz() {
        return clazz;
    }

    public LinkImpl setClazz(String clazz) {
        this.clazz = clazz;
        return this;
    }

    public String getHref() {
        return href;
    }

    public LinkImpl setHref(String href) {
        this.href = href;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public LinkImpl setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getTarget() {
        return target;
    }

    public LinkImpl setTarget(String target) {
        this.target = target;
        return this;
    }

}
