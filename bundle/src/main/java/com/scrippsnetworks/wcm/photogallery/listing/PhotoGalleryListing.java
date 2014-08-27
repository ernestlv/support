package com.scrippsnetworks.wcm.photogallery.listing;

import com.scrippsnetworks.wcm.photogallery.PhotoGallery;

import java.util.List;

/**
 * @author Jason Clark
 *         Date: 5/11/13
 */
public interface PhotoGalleryListing {
    public static final int ITEMS_PER_PAGE = 12;

    public List<PhotoGallery> getPhotoGalleries();
    public int getTotalSize();
}
