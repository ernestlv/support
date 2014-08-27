package com.scrippsnetworks.wcm.fnr.sitesearch;

public class SortOption {
    final String url;
    final String label;

    public SortOption(String label, String url) {
        this.label = label;
        this.url = url;
    }

    public String getLabel() {
        return label;
    }

    public String getUrl() {
        return url;
    }
}
