package com.scrippsnetworks.wcm.taboola;

public enum TaboolaProperties {
    videosYouWillLike("organic-thumbs-1r", "taboola-below-article-thumbs-mix", "below content single video"),
    fromAroundTheWeb("thumbs-1r", "taboola-below-article-thumbs-2nd", "below content single video 2nd"),
    moreVideos("organic-thumbs-1r-search", "taboola-below-article-thumbs-mix", "below content");
    private String mode;
    private String container;
    private String placement;

    private TaboolaProperties(String mode, String container, String placement) {
        this.mode = mode;
        this.container = container;
        this.placement = placement;
    }

    public String getMode() {
        return mode;
    }

    public String getContainer() {
        return container;
    }

    public String getPlacement() {
        return placement;
    }
}
