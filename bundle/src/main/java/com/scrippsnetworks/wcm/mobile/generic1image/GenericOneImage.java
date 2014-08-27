package com.scrippsnetworks.wcm.mobile.generic1image;

import com.scrippsnetworks.wcm.mobile.base.link.Link;

import java.util.List;

public interface GenericOneImage {

    GenericOneImageItem getItem();

    void setItem(GenericOneImageItem item);

    String getLinksTitle();

    void setLinksTitle(String linksTitle);

    List<Link> getLinks();

    void setLinks(List<Link> links);
}
