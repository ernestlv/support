package com.scrippsnetworks.wcm.taglib.photogallery;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.carousel.CarouselSlide;
import com.scrippsnetworks.wcm.carousel.CarouselSlideFactory;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.photogallery.PhotoGallery;
import com.scrippsnetworks.wcm.photogallery.PhotoGalleryFactory;
import com.scrippsnetworks.wcm.photogallery.PhotoGallerySlide;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Patrick Armstrong (parmstrong@siteworx.com)
 */
public class PhotoGalleryCarouselItemsTag extends TagSupport {
    
    private Logger log = LoggerFactory.getLogger(PhotoGalleryCarouselItemsTag.class);
    
    private static final int MAX_SLIDES = 16;
    
    private String tabItems = null;
    private String slidesVar = null;
    
    @Override
    public int doStartTag() throws JspException {
        
        ArrayList<CarouselSlide> carouselSlides = new ArrayList<CarouselSlide>();

        SlingScriptHelper sling = (SlingScriptHelper)pageContext.getAttribute("sling");
        ResourceResolver resourceResolver = sling.getRequest().getResourceResolver();
        
        String [] tabItemsArr = tabItems.split(",");
        
        if (tabItemsArr.length == 1) {
            addSinglePhotoGallery(resourceResolver.getResource(tabItemsArr[0]), carouselSlides);
        } else {
            for (int i = 0; i < Math.min(tabItemsArr.length, MAX_SLIDES); i++) {
                addMultiplePhotoGallery(resourceResolver.getResource(tabItemsArr[i]), carouselSlides);
            }
        }
        
        pageContext.setAttribute(slidesVar, carouselSlides);

        return SKIP_BODY;
    }
    
    @Override
    public int doEndTag() throws JspException {
        return SKIP_BODY;
    }
    
    //Helper Methods
    private void addSinglePhotoGallery(Resource pgRes, ArrayList<CarouselSlide> carouselSlides) {
        if (pgRes != null && pgRes.isResourceType("cq:Page")) { 
            SniPage pgPage = PageFactory.getSniPage(pgRes.adaptTo(Page.class));
            PhotoGallery pg = new PhotoGalleryFactory().withSniPage(pgPage).build();
            List<PhotoGallerySlide> photoGallerySlides = pg.getAllSlides();

            //Go to MAX_SLIDES - 1, because we'll use an end slide.
            for (int i = 0; i < Math.min(photoGallerySlides.size(), MAX_SLIDES - 1) ; i++) {
                PhotoGallerySlide photoGallerySlide = photoGallerySlides.get(i);
                String imgPath = photoGallerySlide.getSniImage().getPath();
                String link = pgPage.getPath() + ".page-" + (i+1) + ".html"; 
                carouselSlides.add(CarouselSlideFactory.getSlide(link, imgPath, null, false, false));
            }
            
            //Add the end frame
            carouselSlides.add(CarouselSlideFactory.getSlide(pgPage.getPath() + ".html", "", "", false, true));
        }
    }
    
    private void addMultiplePhotoGallery(Resource pgRes, ArrayList<CarouselSlide> carouselSlides) {
        if (pgRes != null && pgRes.isResourceType("cq:Page")) {
            SniPage pgPage = PageFactory.getSniPage(pgRes.adaptTo(Page.class));
            PhotoGallery pg = new PhotoGalleryFactory().withSniPage(pgPage).build();
            List<PhotoGallerySlide> photoGallerySlides = pg.getAllSlides();
            
            if (photoGallerySlides.size() > 0) {
                PhotoGallerySlide photoGallerySlide = photoGallerySlides.get(0);
                String imgPath = photoGallerySlide.getSniImage().getPath();
                String link = pgPage.getPath() + ".html";
                String desc = pgPage.getTitle();
                carouselSlides.add(CarouselSlideFactory.getSlide(link, imgPath, desc, true, false));
            }
        }
    }
    
    //Setters    
    public void setSlidesVar(String slidesVar) {
        this.slidesVar = slidesVar;
    }
    
    public void setTabItems(String tabItems) {
        this.tabItems = tabItems;
    }
}
