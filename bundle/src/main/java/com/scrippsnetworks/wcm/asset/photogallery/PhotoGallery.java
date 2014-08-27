package com.scrippsnetworks.wcm.asset.photogallery;

import org.apache.sling.api.resource.Resource;
import org.jsoup.helper.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.Externalizer;

import java.util.*;

/**
 * Data structure that represents the interesting info from a photo gallery.
 * @author Jason Clark
 * Date: 7/9/12
 */
@Deprecated
public class PhotoGallery {

    private String imgPath;
    private String galleryRenditionPath;
    private String thumbPath;
    private String subhead;
    private String caption;
    private String attachedAssetTitle;
    private String attachedAssetSlug;
    private String attachedAssetPath;
    private String orientation;
    private Integer pageNum;
    private String pagePath;
    private String renditionPrefix;
    private Externalizer externalizer;
  
    /**
     * In case, for some reason, you want to populate these values manually...
     */
    public PhotoGallery() {}

    /**
     * Constructor for PhotoGallery object, takes a Sling Resource for a photo-gallery component
     * and populates fields based on that Resource.
     * @param resource Sling Resource, must resolve to a photo-gallery component or will throw an exception.
     */
    public PhotoGallery(Resource resource) {
        Validate.notNull(resource);

        if (!PhotoGalleryUtil.isPhotoGalleryComponent(resource)) {
            throw new InputMismatchException("Resource must be a photo-gallery component.");
        }

        externalizer = resource.getResourceResolver().adaptTo(Externalizer.class);
        Resource imageResource = PhotoGalleryUtil.imageFromPhotoGallery(resource);
        orientation = PhotoGalleryUtil.imageOrientation(imageResource);
        imgPath = PhotoGalleryUtil.pathFromImage(imageResource);
        galleryRenditionPath = getAbsoluteRendtionPathToImage(imageResource);
        renditionPrefix = PhotoGalleryUtil.galleryRenditionPrefix(orientation);
        thumbPath = getAbsolutePathToThumbnail(imageResource);
        subhead = PhotoGalleryUtil.subheadingFromPhotoGallery(resource);
        caption = PhotoGalleryUtil.captionFromPhotoGallery(resource);
        attachedAssetTitle = PhotoGalleryUtil.attachedContentTitleFromPhotoGallery(resource);
        attachedAssetSlug = PhotoGalleryUtil.attachedContentTypeSlugFromPhotoGallery(resource);
        attachedAssetPath = PhotoGalleryUtil.attachedContentPathFromPhotoGallery(resource);
    }
    
    private String getAbsoluteRendtionPathToImage(Resource imageResource) {
    	
    	if(imageResource != null) {
    		
    		String renditionPath = PhotoGalleryUtil.galleryRenditionPathFromImage(imageResource, orientation);
    		
    		if(renditionPath != null && externalizer != null) {
    			return externalizer.externalLink(null, "sndimg", renditionPath);
    		}
    	}
    	
    	return null;
    }
    
    private String getAbsolutePathToThumbnail(Resource imageResource) {
    	if(imageResource != null) {
    		String renditionPath =  PhotoGalleryUtil.thumbPathFromImage(imageResource);
    		if(renditionPath != null && externalizer != null) {
    			return externalizer.externalLink(null, "sndimg", renditionPath);
    		}
    	}
    	
    	return null;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }
    public String getImgPath() {
        return imgPath;
    }
    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }
    public String getThumbPath() {
        return thumbPath;
    }
    public void setSubhead(String subhead) {
        this.subhead = subhead;
    }
    public String getSubhead() {
        return subhead;
    }
    public void setCaption(String caption) {
        this.caption = caption;
    }
    public String getCaption() {
        return caption;
    }
    public void setAttachedAssetTitle(String attachedAssetTitle) {
        this.attachedAssetTitle = attachedAssetTitle;
    }
    public String getAttachedAssetTitle() {
        return attachedAssetTitle;
    }
    public void setAttachedAssetSlug(String attachedAssetSlug) {
        this.attachedAssetSlug = attachedAssetSlug;
    }
    public String getAttachedAssetSlug() {
        return attachedAssetSlug;
    }
    public void setAttachedAssetPath(String attachedAssetPath) {
        this.attachedAssetPath = attachedAssetPath;
    }
    public String getAttachedAssetPath() {
        return attachedAssetPath;
    }
    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }
    public String getOrientation() {
        return orientation;
    }
    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }
    public Integer getPageNum() {
        return pageNum;
    }
    public void setPagePath(String pagePath) {
        this.pagePath = pagePath;
    }
    public String getPagePath() {
        return pagePath;
    }
    public String getGalleryRenditionPath() {
        return galleryRenditionPath;
    }
    
    public String getRenditionPrefix() {
    	return renditionPrefix;
    }
}
