/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scrippsnetworks.wcm.carousel.impl;

import com.day.cq.wcm.api.Page;
import com.day.cq.widget.HtmlLibraryManager;
import com.scrippsnetworks.wcm.carousel.SuperleadCarouselSlide;
import com.scrippsnetworks.wcm.mobile.imageicons.TransformIconClassToMobile;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.photogallery.PhotoGallery;
import com.scrippsnetworks.wcm.photogallery.PhotoGalleryFactory;
import com.scrippsnetworks.wcm.photogallery.PhotoGallerySlide;
import com.scrippsnetworks.wcm.util.modalwindow.MobileModalPath;
import com.scrippsnetworks.wcm.video.Video;
import com.scrippsnetworks.wcm.video.VideoFactory;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Patrick
 */
public class SuperleadCarouselSlideImpl implements SuperleadCarouselSlide {
    
    private String SLUG, description, imagePath, link, type;
    private boolean videoThumbnail;

    private static final String PG_TYPE = "photogallery";
    private static final String VIDEO_TYPE = "video";
    private static final String OTHER_TYPE = "other";
    
    private static final String PROP_TYPE = "type";
    private static final String PROP_SLUG = "SLUG";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_LINK = "link";
    private static final String PROP_IMAGE = "image/fileReference";
    private final Logger log = LoggerFactory.getLogger(SuperleadCarouselSlideImpl.class);
    public SuperleadCarouselSlideImpl(Resource slideRes) {
        Resource linkedRes = null;
        ValueMap props = slideRes.adaptTo(ValueMap.class);
        
        link = props.get(PROP_LINK, "");
        if (link.startsWith("/")) {
            linkedRes = slideRes.getResourceResolver().getResource(link);
        }
        
        videoThumbnail = false;
        type = props.get(PROP_TYPE, "");
        imagePath = props.get(PROP_IMAGE, "");
        if (imagePath.isEmpty() && (type.equals(PG_TYPE) || type.equals(VIDEO_TYPE)) && linkedRes != null && linkedRes.isResourceType("cq:Page")) {
            if (type.equals(PG_TYPE)) {
                SniPage sniPage = PageFactory.getSniPage(linkedRes.adaptTo(Page.class));
                PhotoGallery photoGallery = new PhotoGalleryFactory().withSniPage(sniPage).build();
                List<PhotoGallerySlide> slides = photoGallery.getAllSlides();
                if (slides.size() > 0) {
                    imagePath = slides.get(0).getSniImage().getPath();
                } else {
                    imagePath = "";
                }
            } else if (type.equals(VIDEO_TYPE)) {
                SniPage sniPage = PageFactory.getSniPage(linkedRes.adaptTo(Page.class));
                Video video = new VideoFactory().withSniPage(sniPage).build();
                imagePath = video.getThumbnailUrl();
                videoThumbnail = true;
            }
        }
        if(StringUtils.isNotBlank(link) && type.equals(VIDEO_TYPE)){
        	if(!link.contains(".html")){
        		link +=".html";
        	}
        }

        SLUG = props.get(PROP_SLUG, "");
        description = props.get(PROP_DESCRIPTION, "");
    }

    @Override
    public String getSLUG() {
        return SLUG;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public String getImagePath() {
        return imagePath;
    }

    @Override
    public String getLink() {
        return link;
    }
    
    @Override
    public boolean isVideoThumbnail() {
        return videoThumbnail;
    }
    
    @Override
    public String getIconCls() {
        if (type.equals(PG_TYPE)) {
            return "ss-layers";
        } else if (type.equals(VIDEO_TYPE)) {
            return "ss-play";
        } else {
            return "";
        }
    }
    
    @Override
    public String getMobileIconCls() {
    	return TransformIconClassToMobile.modify(getIconCls());
    }
    
}
