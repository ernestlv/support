package com.scrippsnetworks.wcm.image;

public enum LazyLoadInfo {
    none("img", "src", null),
    datasrc("img", "data-src", null),
    rsanchor("a", "href", "rsImg"),
    rsdiv("div", null, "rsImg"),
    snirsdiv("div", null, "lazy-img");

    private final String tag;
    private final String srcAttribute;
    private final String className;

    LazyLoadInfo(String tag, String srcAttribute, String className) {
        this.tag = tag;
        this.srcAttribute = srcAttribute;
        this.className = className;
    }

    public String getTag() {
        return tag;
    }

    public String getSrcAttribute() {
        return srcAttribute;
    }

    public String getClassName() {
        return className;
    }

    public boolean isEmptyElement() {
        return tag.equals("img") ? true : false;
    }

    public static String getModes() {
        StringBuilder modes = new StringBuilder();

        for (LazyLoadInfo i : LazyLoadInfo.values()) {
            modes.append(i + " ");
        }

        return modes.toString().trim();
    }
}

