package com.scrippsnetworks.wcm.fnr.sitesearch;

public enum SearchForm {
    global("global header search"),
    inpage("in-page search"),
    module("module search");

    String metadataValue;

    SearchForm(String metadataValue) {
        this.metadataValue = metadataValue;
    }

    public String getMetadataValue() {
        return metadataValue;
    }
}
