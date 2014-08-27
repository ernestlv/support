package com.scrippsnetworks.wcm.sitemap;

public enum SitemapPageType {
    EPISODE("monthly", "0.5"),
    PHOTOGALLERY("weekly", "0.5"),
    RECIPE("daily", "0.5"),
    SECTION("daily", "0.5"),
    SHOW("monthly", "0.5"),
    RESTAURANT("monthly", "0.5"),
    TALENT("monthly", "0.5"),
    TOPIC("daily", "0.5"),
    UNIVERSAL_LANDING("weekly", "0.5"),
    VIDEO("weekly", "0.5"),
    VIDEO_CHANNEL("monthly", "0.5"),
    VIDEO_PLAYER("monthly", "0.5");

    private String changeFreq;
    private String priority;

    SitemapPageType(String changeFreq, String priority) {
        this.changeFreq = changeFreq;
        this.priority = priority;
    }

    public String getChangeFreq() {
        return changeFreq;
    }

    public String getPriority() {
        return priority;
    }

}

