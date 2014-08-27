package com.scrippsnetworks.wcm.breadcrumb.crumb;

/**
 * @author Jonathan Bell
 *         Date: 10/2/2013
 */
public enum CrumbTypes {
    HOME(true),
    HUB(true),
    PAGE(false),
    PARENT(true),
    SECTION(true),
    SELECTOR(false),
    SHOW(true);

    private boolean urlRequired;

    private CrumbTypes(final boolean urlRequired) {
        this.urlRequired = urlRequired;
    }

    public boolean urlRequired() {
        return urlRequired;
    }
}
