package com.scrippsnetworks.wcm.image.impl;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.commons.util.PrefixRenditionPicker;
import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.page.SniPage;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.image.ImageAspect;
import com.scrippsnetworks.wcm.image.ImageUrlService;
import com.scrippsnetworks.wcm.image.RenditionInfo;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * @author Jason Clark
 *         Date: 4/28/13
 */
public class SniImageImpl implements SniImage {

    private static final Logger log = LoggerFactory.getLogger(SniImageImpl.class);

    private static final String METADATA_PROPERTY_CAPTION = "dc:title";
    private static final String METADATA_PROPERTY_TITLE = "dc:title";
    private static final String EMPTY_STRING = "";

    protected int width;
    protected int height;
    protected String path;
    private Resource resource;

    private static ImageUrlService imageUrlService;

    private String aspect;
    private String canonicalUrl;
    private String caption;
    private String rendition;
    private String title;
    private Resource utilResource; //this is for working with other Resources

    /** Default */
    public SniImageImpl() {}

    /** Unified constructor **/
    public SniImageImpl(final String path, final Resource resource, final SniPage page,
        final String rendition, final String aspect) {

        if (path != null) {
            this.path = path;
        }

        if (resource == null && page != null) {
            try {
                this.resource = page.getContentResource();
            } catch (Exception e) {
                log.error(e.getMessage());
                this.resource = null;
            }
        // What's the use case for this?
        } else if (resource != null && path != null) {
            this.utilResource = resource.getResourceResolver().getResource(resource, path);
        } else if (resource != null) {
            this.resource = resource;
        }

        if (resource == null && path != null && path.startsWith("/")) {
            this.resource = null;// TODO: Try logging into repository
        }

        if (rendition != null) {
            this.rendition = rendition;
        }

        if (aspect != null) {
            this.aspect = aspect;
        }
    }

    /**
     *
     * @return
     */
    public Resource getResource() {
        return this.resource;
    }

    /**
     *
     * @return
     */
    @Override
    public String getPath() {
        return path;
    }

    private ImageUrlService getImageUrlService() {
        if (imageUrlService == null) {
            BundleContext bundle = FrameworkUtil.getBundle(ImageUrlService.class).getBundleContext();
            ServiceReference serviceReference = bundle.getServiceReference(ImageUrlService.class.getName());
            if (serviceReference != null) {
                imageUrlService = (ImageUrlService)bundle.getService(serviceReference);
            }
        }

        return imageUrlService;
    }

    public String getUrl() {
        return getCanonicalUrl();
    }

    /**
     *
     * @return
     */
    @Override
    public String getCanonicalUrl() {
        if (canonicalUrl == null) {
            canonicalUrl = EMPTY_STRING;
            if (path != null) {
                if (path.startsWith("/")) {
                    ImageUrlService urlService = getImageUrlService();
                    if (urlService != null) {
                        if (rendition != null && aspect != null) {
                            canonicalUrl = urlService.getImageUrl(path, RenditionInfo.valueOf(rendition), ImageAspect.valueOf(aspect));
                        } else if (rendition != null) {
                            canonicalUrl = urlService.getImageUrl(path, RenditionInfo.valueOf(rendition));
                        } else {
                            canonicalUrl = urlService.getImageUrl(path);
                        }
                    }
                } else {
                    canonicalUrl = path;
                }
            }
        }

        return canonicalUrl;
    }

    /**
     *
     * @return
     */
    @Override
    public int height() {
        return 0;
    }

    /**
     *
     * @return
     */
    @Override
    public int width() {
        return 0;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isRendition() {
        return StringUtils.isNotBlank(rendition);
    }

    public String getCaption() {
        if (StringUtils.isEmpty(caption) && resource != null) {
            Asset asset = resource.adaptTo(Asset.class);
            if (asset != null) {
                caption = asset.getMetadataValue(METADATA_PROPERTY_CAPTION);
            }
        }

        return caption;
    }

    public String getTitle() {
        if (StringUtils.isEmpty(title) && resource != null) {
            Asset asset = resource.adaptTo(Asset.class);
            if (asset != null) {
                title = asset.getMetadataValue(METADATA_PROPERTY_TITLE);
            }
        }

        return title;
    }

    public String getAspect() {
        return aspect;
    }

    public String getRendition() {
        return rendition;
    }
}
