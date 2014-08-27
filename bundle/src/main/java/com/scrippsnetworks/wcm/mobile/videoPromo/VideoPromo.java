package com.scrippsnetworks.wcm.mobile.videoPromo;

import com.scrippsnetworks.wcm.mobile.base.link.Link;

import java.util.List;

public interface VideoPromo {

    String getHeader();

    void setHeader(String header);

    Link getMoreLink();

    void setMoreLink(Link moreLink);

    List<VideoPromoItem> getItems();

    void setItems(List<VideoPromoItem> items);

}
