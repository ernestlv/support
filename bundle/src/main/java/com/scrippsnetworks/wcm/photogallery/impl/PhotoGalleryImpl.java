package com.scrippsnetworks.wcm.photogallery.impl;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.photogallery.PhotoGallery;
import com.scrippsnetworks.wcm.photogallery.PhotoGallerySlide;
import com.scrippsnetworks.wcm.photogallery.PhotoGallerySlideFactory;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.sling.api.resource.Resource;

import java.util.List;

/**
 * @author Jason Clark
 *         Date: 4/28/13
 * @author Patrick Armstrong (parmstrong@siteworx.com)
 *         Date: 6/5/13
 * @author Jonathan Bell
 *         Date: 6/10/13
 */
public class PhotoGalleryImpl implements PhotoGallery {

    private static final String SLIDES_HOME = "content-well/carousel-slider/slides";
    
    private Resource resource;
    private SniPage page;
    private List<PhotoGallerySlide> slides;
    private List<PhotoGallerySlide> recipeSlides;
    private List<SniPage> recipePages;

    /** default */
    public PhotoGalleryImpl() {}

    /**
     *
     * @param resource
     */
    public PhotoGalleryImpl(Resource resource) {
        this.resource = resource;
        this.page = PageFactory.getSniPage(resource.adaptTo(Page.class));
        initializeSlides();
    }

    /**
     *
     * @param page
     */
    public PhotoGalleryImpl(SniPage page) {
        this.page = page;
        this.resource = page.adaptTo(Resource.class);
        initializeSlides();
    }

    
    private void initializeSlides() {
        Iterator<Resource> slidesIt = null;
        Resource slidesParent = page.getContentResource(SLIDES_HOME);
        slides = new ArrayList<PhotoGallerySlide>();
        PhotoGallerySlideFactory pgsf = new PhotoGallerySlideFactory();
        
        if (slidesParent != null) {
            slidesIt = slidesParent.listChildren();
            while (slidesIt.hasNext()) {
                Resource slide = slidesIt.next();
                if (!slide.getName().startsWith("crxdao:")) {
                    slides.add(pgsf.withResource(slide).build());
                }
            }
        }
    }

    /**
     *
     * @return
     */
    @Override
    public List<PhotoGallerySlide> getAllSlides() {
        return slides;
    }

    /**
     * can return null
     * @param index
     * @return
     */
    public PhotoGallerySlide getSlide(int index) {
        if (slides == null) {
            return null;
        }
        PhotoGallerySlide slide = null;
        try {
            slide = slides.get(index);
        } catch (Exception e) {
            //log msg here
        }
        return slide;
    }

    public List<SniPage> getRecipePages() {
        if (recipePages == null) {
            recipePages = new ArrayList<SniPage>();
            for (PhotoGallerySlide currentSlide : getRecipeSlides()) {
                recipePages.add(currentSlide.getAssetPage());                
            }
        }

        return recipePages;
    }

    /**
     * @return
     *
     */
    public List<PhotoGallerySlide> getRecipeSlides() {
        if (recipeSlides == null) {
            recipeSlides = new ArrayList<PhotoGallerySlide>();
            for (PhotoGallerySlide currentSlide : slides) {
                if (currentSlide.getAssetType().equals("recipe")) {
                    recipeSlides.add(currentSlide);
                }    
            }
        }

        return recipeSlides;
    }

    public int getSlideCount() {
        return slides.size();
    }

    @Override
    public SniPage getPage() {
        return page;
    }
}
