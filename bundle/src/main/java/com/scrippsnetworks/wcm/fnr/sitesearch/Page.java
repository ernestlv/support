package com.scrippsnetworks.wcm.fnr.sitesearch;

public class Page {
    private final Integer pageNum;
    private final String url;

    public Page(Integer pageNum, String url) {
        this.pageNum = pageNum;
        this.url = url;
    }

    public Integer getPageNumber() {
        return pageNum;
    }

    public String getUrl() {
        return url;
    }

    public boolean isSpacer() {
        return pageNum == null;
    }
}
