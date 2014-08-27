package com.scrippsnetworks.wcm.recipe.related.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;

import com.scrippsnetworks.wcm.fnr.util.PageSlingResourceTypes;
import com.scrippsnetworks.wcm.article.Article;
import com.scrippsnetworks.wcm.article.ArticleFactory;
import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.mobile.imageicons.TransformIconClassToMobile;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.photogallery.PhotoGallery;
import com.scrippsnetworks.wcm.photogallery.PhotoGalleryFactory;
import com.scrippsnetworks.wcm.recipe.related.Related;
import com.scrippsnetworks.wcm.util.PagePropertyNames;
import com.scrippsnetworks.wcm.video.Video;
import com.scrippsnetworks.wcm.video.VideoFactory;

/**
 * @author Jonathan Bell
 *         Date: 7/19/13
 */
public class RelatedImpl implements Related {
    private static final String EMPTY_STRING = "";
    private static final String IMAGE_DAM_PREFIX = "/content/dam";
    private SniPage sniPage;
    private String promoImage;
    private String videoRunTime;
    

    public RelatedImpl(final SniPage sniPage) {
        this.sniPage = sniPage;
    }

    /** {@inheritDoc} */
    public String getPromoImage() {
        SniImage pageImage = null;
        String resourceType = sniPage.getContentResource().getResourceType();

        for (PageSlingResourceTypes type : PageSlingResourceTypes.values()) {
            if (type.resourceType().equals(resourceType)) {
                switch (type) {
                    case ARTICLE_SIMPLE:
                    case PHOTO_GALLERY:
                    case VIDEO:
                        promoImage = sniPage.getCanonicalImagePath();
                        break;
                    default:
                        break;
                }
            }
        }

        return promoImage == null ? EMPTY_STRING : promoImage;
    }

    /** {@inheritDoc} */
    public String getOverlayType() {
        String overlayType = null;
        String resourceType = sniPage.getContentResource().getResourceType();
        for (PageSlingResourceTypes type : PageSlingResourceTypes.values()) {
            if (type.resourceType().equals(resourceType)) {
                switch (type) {
                    case PHOTO_GALLERY:
                        overlayType = "ss-layers";
                        break;
                    case VIDEO:
                        overlayType = "ss-play";
                        break;
                    default:
                        break;
                }
            }
        }

        return overlayType == null ? EMPTY_STRING : overlayType;
    }
    
    @Override
    public String getMobileOverlayType() {
    	return TransformIconClassToMobile.modify(getOverlayType());
    }

    /** {@inheritDoc} */
    public SniPage getSniPage() {
        return sniPage;
    }

    /** {@inheritDoc} */
    public Boolean getIsDamImage() {
        return promoImage != null && promoImage.startsWith(IMAGE_DAM_PREFIX);
    }
    
    @Override
    public String getVideoRunTime() {
    	String resourceType = sniPage.getContentResource().getResourceType();
    	ValueMap pageProperties = sniPage.getProperties();
    	if (videoRunTime == null && pageProperties.containsKey(Video.RUNTIME) && PageSlingResourceTypes.VIDEO.resourceType().equals(resourceType)) {
            String rawRunTime = pageProperties.get(Video.RUNTIME, String.class);
            if (StringUtils.isNotBlank(rawRunTime)) {
                String[] runTimeParts = rawRunTime.split(":");
                if (runTimeParts.length >= 3 && runTimeParts[0].equals("00")) {
                	videoRunTime = StringUtils.join(runTimeParts, ":", 1, runTimeParts.length);
                } else {
                	videoRunTime = rawRunTime;
                }
            }
        }
        return videoRunTime;
    }
}
