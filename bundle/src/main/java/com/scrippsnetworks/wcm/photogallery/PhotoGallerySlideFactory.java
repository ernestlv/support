package com.scrippsnetworks.wcm.photogallery;

import com.scrippsnetworks.wcm.photogallery.impl.PhotoGallerySlideImpl;
import org.apache.sling.api.resource.Resource;

/**
 * @author Jason Clark
 *         Date: 4/28/13
 */
public class PhotoGallerySlideFactory {

    private Resource resource;

    /**
     * Build a PhotoGallerySlide Object
     * @return PhotoGallerySlide
     */
    public PhotoGallerySlide build() {
        if (resource != null) {
            return new PhotoGallerySlideImpl(resource);
        }
        return new PhotoGallerySlideImpl();
    }

    /**
     *
     * @param path
     * @return
     */
    public PhotoGallerySlideFactory withResource(Resource resource) {
        this.resource = resource;
        return this;
    }

}
