package com.scrippsnetworks.wcm.image.impl;

import java.lang.String;
import java.lang.StringBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Reference;
import com.day.cq.commons.Externalizer;
import com.scrippsnetworks.wcm.image.RenditionInfo;
import com.scrippsnetworks.wcm.image.ImageAspect;
import com.scrippsnetworks.wcm.image.ImageUrlService;
import org.apache.sling.commons.mime.MimeTypeService;

/**
 * @{inheritDoc}
 */
@Component
@Service
public class ImageUrlServiceImpl implements ImageUrlService {

    public static final String IMAGE_EXTERNALIZER_DOMAIN = "sndimg";
    public static final String IMAGE_SERVLET_SELECTOR = "rend";
    public static final String IMAGE_DEFAULT_EXTENSION = "jpeg";

    @Reference
    protected Externalizer externalizer;
    
    @Reference
    protected MimeTypeService mimeTypeService;
    
    /**
     * {@inheritDoc}
     */
    public String getImageUrl(String path) {
        return getImageUrl(path, null, null);
    }

    /**
     * {@inheritDoc}
     */
    public String getImageUrl(String path, RenditionInfo rendition) {
        return getImageUrl(path, rendition, null);
    }

    /**
     * {@inheritDoc}
     */
    public String getImageUrl(String path, RenditionInfo rendition, ImageAspect aspect) {
        return getImageUrl(path, rendition, aspect, IMAGE_EXTERNALIZER_DOMAIN);
    }

    /**
     * {@inheritDoc}
     */
    public String getImageUrl(String path, RenditionInfo rendition, ImageAspect aspect, String domain) {
        if (path == null || externalizer == null) {
            return null;
        }

        StringBuilder srcBuilder = new StringBuilder(path);

        if (rendition != null) {
            srcBuilder.append(".").append(IMAGE_SERVLET_SELECTOR).append(".").append(rendition.name());

            if (aspect != null) {
                srcBuilder.append(".").append(aspect.name());
            }

            String imageExtension = mimeTypeService.getExtension(mimeTypeService.getMimeType(FilenameUtils.getExtension(path)));
            srcBuilder.append(".").append(imageExtension == null ? IMAGE_DEFAULT_EXTENSION : imageExtension);
        }

        return externalizer.externalLink(null, domain == null ? IMAGE_EXTERNALIZER_DOMAIN : domain, srcBuilder.toString());
    }
}
