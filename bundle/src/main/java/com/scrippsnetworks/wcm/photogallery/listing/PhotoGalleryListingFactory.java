package com.scrippsnetworks.wcm.photogallery.listing;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.photogallery.listing.impl.PhotoGalleryListingImpl;

/**
 * @author Jason Clark
 *         Date: 5/11/13
 */
public class PhotoGalleryListingFactory {

    /** SniPage */
    private SniPage sniPage;

    /**
     *
     * @return
     */
    public PhotoGalleryListing build() {
        return new PhotoGalleryListingImpl(sniPage);
    }

    /**
     *
     * @param sniPage
     * @return
     */
    public PhotoGalleryListingFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }
}
