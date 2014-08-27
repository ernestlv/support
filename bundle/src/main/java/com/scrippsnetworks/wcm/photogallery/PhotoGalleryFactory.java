package com.scrippsnetworks.wcm.photogallery;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.photogallery.impl.PhotoGalleryImpl;
import org.apache.sling.api.resource.Resource;

/**
 * @author Jason Clark
 *         Date: 4/28/13
 */
public class PhotoGalleryFactory {

    private SniPage page;
    private Resource resource;

    public PhotoGallery build() {
        if (resource != null) {
            return new PhotoGalleryImpl(resource);
        }
        if (page != null) {
            return new PhotoGalleryImpl(page);
        }
        return null;
    }

    public PhotoGalleryFactory withSniPage(SniPage page) {
        this.page = page;
        return this;
    }

    public PhotoGalleryFactory withParsysResource(Resource resource) {
        this.resource = resource;
        return this;
    }

}
