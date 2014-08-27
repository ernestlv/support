package com.scrippsnetworks.wcm.mobile.generic1image.impl;


import com.scrippsnetworks.wcm.mobile.base.link.Link;
import com.scrippsnetworks.wcm.mobile.generic1image.GenericOneImage;
import com.scrippsnetworks.wcm.mobile.generic1image.GenericOneImageItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dzmitry_Drepin on 1/30/14.
 */
public class GenericOneImageImpl implements GenericOneImage {
    private GenericOneImageItem item = new GenericOneImageItemImpl();
    private String linksTitle;
    private List<Link> links=new ArrayList<Link>(4);

    public GenericOneImageItem getItem() {
        return item;
    }

    public void setItem(GenericOneImageItem item) {
        this.item = item;
    }

    public String getLinksTitle() {
        return linksTitle;
    }

    public void setLinksTitle(String linksTitle) {
        this.linksTitle = linksTitle;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }
}
