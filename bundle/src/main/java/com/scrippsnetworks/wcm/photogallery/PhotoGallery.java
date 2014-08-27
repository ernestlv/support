package com.scrippsnetworks.wcm.photogallery;

import com.scrippsnetworks.wcm.page.SniPage;

import java.util.List;

/**
 * @author Jason Clark
 *         Date: 4/28/13
 */
public interface PhotoGallery {
    public List<PhotoGallerySlide> getAllSlides();
    public PhotoGallerySlide getSlide(int index);
    public List<SniPage> getRecipePages();
    public List<PhotoGallerySlide> getRecipeSlides();
    public int getSlideCount();
    public SniPage getPage();
}
