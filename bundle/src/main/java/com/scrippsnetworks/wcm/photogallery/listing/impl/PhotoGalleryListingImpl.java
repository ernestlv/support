package com.scrippsnetworks.wcm.photogallery.listing.impl;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.photogallery.PhotoGallery;
import com.scrippsnetworks.wcm.photogallery.PhotoGalleryFactory;
import com.scrippsnetworks.wcm.photogallery.listing.PhotoGalleryListing;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Jason Clark
 *         Date: 5/11/13
 */
public class PhotoGalleryListingImpl implements PhotoGalleryListing {

    private static final String GALLERIES_HOME = "galleries";

    private SniPage page;
    private Resource resource;
    private ValueMap vm;
    private int total;
    private List<PhotoGallery> photoGalleries;

    /**
     *
     * @param sniPage
     */
    public PhotoGalleryListingImpl(SniPage page) {
        this.page = page;
        this.resource = page.adaptTo(Resource.class);
        this.vm = page.getContentResource().adaptTo(ValueMap.class);
        this.photoGalleries = new ArrayList<PhotoGallery>();
        initializeGalleries();
    }

    private void initializeGalleries() {
        String[] galleryPaths = vm.get(GALLERIES_HOME, String[].class);
        if (galleryPaths != null) {
            PhotoGalleryFactory pgf = new PhotoGalleryFactory();

            for (String galleryPath : galleryPaths) {
                if (!StringUtils.isEmpty(galleryPath)) {
                    ResourceResolver rr = resource.getResourceResolver();
                    Resource res = rr.getResource(galleryPath);
                    PhotoGallery gallery = pgf.withParsysResource(res).build();
                    if (gallery != null && gallery.getSlideCount() != 0) {
                        photoGalleries.add(gallery);
                    }
                }
            }

            total = photoGalleries.size();
        }
    }

    @Override
    public List<PhotoGallery> getPhotoGalleries() {
        return photoGalleries;
    }

    @Override
    public int getTotalSize() {
        return total;
    }
}
