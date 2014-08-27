package com.scrippsnetworks.wcm.export.snipage.content.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.video.Video;
import com.scrippsnetworks.wcm.video.VideoFactory;

/**
 * This class generates the Video page specific properties.
 * @author Venkata Naga Sudheer Donaboina
 */
public class VideoExport extends SniPageExport {
    private static final Logger LOG = LoggerFactory.getLogger(VideoExport.class);

    private Video video;

    public VideoExport(SniPage sniPage) {
        super(sniPage);
        this.video = new VideoFactory().withSniPage(sniPage).build();
        initialize();
    }

    protected VideoExport(SniPage sniPage, Video video) {
        super(sniPage);
        this.video = video;
        initialize();
    }

    public enum ExportProperty {
        VIDEO_LENGTH(String.class),
        VIDEO_SHOW(String.class),
        VIDEO_HOME(String.class),
        VIDEO_THUMBNAIL_IMAGE_16x9(String.class),
        VIDEO_THUMBNAIL_IMAGE_4x3(String.class);

        final Class clazz;

        ExportProperty(Class clazz) {
            this.clazz = clazz;
        }

        public Class valueClass() {
            return clazz;
        }
    }

    public void initialize() {
        LOG.debug("Started Video Export overrides");
        if (sniPage == null || !sniPage.hasContent() || video == null) {
            return;
        }
        setProperty(ExportProperty.VIDEO_LENGTH.name(), video.getVideoRunTime());
        
      if (sniPage != null) {
        	setProperty(SniPageExport.ExportProperty.CORE_IMAGE_PATH.name(), sniPage.getCanonicalImageUrl());
        	setProperty(SniPageExport.ExportProperty.CORE_IMAGE_URL.name(), sniPage.getCanonicalImageUrl());
        }
      
        SniPage videoShowPage = getContentPageFromAssetPath(video.getShow(), sniPage.getPageManager(), sniPage.getPath());
        if(videoShowPage != null) {
        	setProperty(ExportProperty.VIDEO_SHOW.name(), videoShowPage.getUid());
        }
        
		/*
		 * Promo Url is set as Video Home, if it is not present, video path is
		 * added as video home.
		 */
        String promoUrl = video.getPromoUrl();
        if(promoUrl == null) {
        	promoUrl = video.getSniPage().getFriendlyUrl();
        }
        setProperty(ExportProperty.VIDEO_HOME.name(), promoUrl);
        
        setProperty(ExportProperty.VIDEO_THUMBNAIL_IMAGE_4x3.name(), video.getThumbnailUrl());
        
        setProperty(ExportProperty.VIDEO_THUMBNAIL_IMAGE_16x9.name(), video.getThumbnailImage16X9());
        
        LOG.debug("Completed Video Export overrides");
    }

}
