package com.scrippsnetworks.wcm.mobile.videoPromo.impl;

import com.scrippsnetworks.wcm.mobile.base.link.Link;
import com.scrippsnetworks.wcm.mobile.videoPromo.VideoPromo;
import com.scrippsnetworks.wcm.mobile.videoPromo.VideoPromoItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dzmitry_Drepin on 2/17/14.
 */
public class VideoPromoImpl implements VideoPromo {
    private String header;
    private Link moreLink;
    private List<VideoPromoItem> items= new ArrayList<VideoPromoItem>(16);

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public Link getMoreLink() {
        return moreLink;
    }

    public void setMoreLink(Link moreLink) {
        this.moreLink = moreLink;
    }

    public List<VideoPromoItem> getItems() {
        return items;
    }

    public void setItems(List<VideoPromoItem> items) {
        this.items = items;
    }
}
