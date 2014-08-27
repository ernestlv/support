package com.scrippsnetworks.wcm.fnr.sitesearch;

public enum SortKey {
    relevancy("Best Match", null),
    rating("Rating", "rating"),
    popular("Most Popular", "mostPopular");

    final String label;
    final String key;

    SortKey(String label, String key) {
        this.label = label;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }
}
