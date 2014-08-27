package com.scrippsnetworks.wcm.export.snipage.content.impl;

import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.photogallery.PhotoGallery;
import com.scrippsnetworks.wcm.photogallery.PhotoGalleryFactory;
import com.scrippsnetworks.wcm.photogallery.PhotoGallerySlide;
import com.scrippsnetworks.wcm.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryExport extends SniPageExport {

    public enum ExportProperty {
        PHOTOGALLERY_PHOTO(String.class);

        final Class clazz;

        ExportProperty(Class clazz) {
            this.clazz = clazz;
        }

        public Class valueClass() {
            return clazz;
        }
    }

    private final PhotoGallery photoGallery;

    public PhotoGalleryExport(SniPage sniPage) {
        super(sniPage);
        this.photoGallery = new PhotoGalleryFactory().withSniPage(sniPage).build();
        initialize();
    }

    protected PhotoGalleryExport(SniPage sniPage, PhotoGallery photoGallery) {
        super(sniPage);
        this.photoGallery = photoGallery;
        initialize();
    }

    private void initialize() {
        if (sniPage == null || !sniPage.hasContent() || photoGallery == null) {
            return;
        }

        List<String> photos = new ArrayList<String>();

        int i = 1;
        for (PhotoGallerySlide slide : photoGallery.getAllSlides()) {
            SniImage image = slide.getSniImage();
            if (image != null && image.getPath() != null) {
                String title = slide.getTitle();
                if (title != null) {
                    title = escapeCompositeValue(StringUtil.cleanToPlainText(title)); // Pipe is value delimiter, need to convert.
                } else {
                    title = "";
                }
                String imagePath = image.getPath();
                StringBuilder sb = new StringBuilder();
                sb.append(title)
                    .append(COMPOSITE_VALUE_DELIMITER)
                    .append(imagePath)
                    .append(COMPOSITE_VALUE_DELIMITER)
                    .append(sniPage.getPath());
                if (i > 1) {
                    sb.append(".page-").append(String.valueOf(i));
                }
                sb.append(".html")
                    .append(COMPOSITE_VALUE_DELIMITER)
                    .append(String.valueOf(i));
                photos.add(sb.toString());
            }
            i++;
        }

        setProperty(ExportProperty.PHOTOGALLERY_PHOTO.name(), photos.toArray(new String[photos.size()]));
    }
}
