package com.scrippsnetworks.wcm.universallanding;

/**
 * Describe where a particular component's lead image should be found.
 */
public enum LeadImageInfo {
    lead3img("lead-3-image", "image1/fileReference"),
    leadimgss("lead-1-image-scroll-with-stack", "slide1/image/fileReference"),
    genericimg("generic-1-image", "leftImage/fileReference"),
    genericimgp("generic-1-image-plus-link-list", "leftImage/fileReference");

    private final String resourceType;
    private final String imageReference;

    LeadImageInfo(String resourceType, String imageReference) {
        this.resourceType = resourceType;
        this.imageReference = imageReference;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getImageReference() {
        return imageReference;
    }
}

