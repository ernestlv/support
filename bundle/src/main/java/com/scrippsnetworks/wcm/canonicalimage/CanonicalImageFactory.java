package com.scrippsnetworks.wcm.canonicalimage;

import org.apache.sling.api.resource.Resource;

import com.scrippsnetworks.wcm.canonicalimage.impl.CanonicalImageImpl;
import com.scrippsnetworks.wcm.page.SniPage;

public class CanonicalImageFactory {

    private SniPage page;
    private Resource resource;
    private boolean defaultImageFlag;
    
    public CanonicalImage build() {
        if (resource != null) {
            return new CanonicalImageImpl(resource, defaultImageFlag);
        }
        if (page != null) {
            return new CanonicalImageImpl(page, defaultImageFlag);
        }
        return null;
    }

    public CanonicalImageFactory withSniPage(SniPage page) {
        this.page = page;
        return this;
    }

    public CanonicalImageFactory withParsysResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public CanonicalImageFactory withDefaultImage(boolean defaultImageFlag) {
        this.defaultImageFlag = defaultImageFlag;
        return this;
    } 
}
