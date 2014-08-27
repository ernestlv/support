package com.scrippsnetworks.wcm.image;

import com.day.cq.commons.ImageHelper;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.api.RenditionPicker;
import com.day.image.Layer;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.mime.MimeTypeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/** Implementation of RenditionPicker which will generate a rendition before returning it if necessary.
 *
 * This class uses the RenditionInfo and ImageAspect enums to identify the rendition desired.
 *
 * @author Scott Everett Johnson
 */
public class GeneratingRenditionPicker implements RenditionPicker {

    private final Logger log = LoggerFactory.getLogger(GeneratingRenditionPicker.class);

    Asset imageAsset = null;
    RenditionInfo renditionInfo;
    ImageAspect imageAspect;
    ImageQuality imageQuality;
    MimeTypeService mimeTypeService;
    ResourceResolverFactory resolverFactory; // If nonnull, use admin resolver to generate.

    /** Constructs a new rendition picker using the RenditionInfo and ImageAspect enums to identify which rendition to return.
     */
    public GeneratingRenditionPicker(RenditionInfo renditionInfo, ImageAspect imageAspect, ImageQuality imageQuality,
                                     MimeTypeService mimeTypeService, ResourceResolverFactory resolverFactory) {
        this.renditionInfo = renditionInfo;
        this.imageAspect = imageAspect;
        this.mimeTypeService = mimeTypeService;
        this.imageQuality = imageQuality;
        this.resolverFactory = resolverFactory;
    }

    /** @see RenditionPicker#getRendition */
    public Rendition getRendition(Asset imageAsset) {
        if (imageAsset == null) {
            return null;
        }

        Rendition retVal;

        if (imageAspect == null) {
            imageAspect = getBestAspect(imageAsset);
        }

        log.debug("using rendition {} aspect {}", renditionInfo, imageAspect);

        final String mimeType = imageAsset.getMimeType();
        final String extension = mimeTypeService.getExtension(mimeType);
        retVal = imageAsset.getRendition(renditionInfo.getRenditionName(imageAspect, imageQuality, extension));

        if (retVal == null) {
            Asset assetToGenerate = imageAsset;
            ResourceResolver adminResolver = null;
            try {
                if (resolverFactory != null) {
                    adminResolver = resolverFactory.getAdministrativeResourceResolver(null);
                } else {
                    log.warn("resolver factory null, won't be able to generate rendition");
                }
                if (adminResolver != null) {
                    Resource resourceToAdapt = adminResolver.getResource(imageAsset.getPath());
                    if (resourceToAdapt != null) {
                        assetToGenerate = resourceToAdapt.adaptTo(Asset.class);
                        if (assetToGenerate == null) {
                            log.warn("admin-acquired resource did not adapt to asset");
                            assetToGenerate = imageAsset;
                        }
                    } else {
                        log.warn("couuld not reacquire {} with admin resolver", imageAsset.getPath());
                    }
                } else {
                    log.warn("administrative resolver null, cannot generate rendition");
                }
                RenditionGenerator generator = new RenditionGenerator(assetToGenerate, mimeTypeService);
                generator.generate(renditionInfo.getImageDimensions(imageAspect), imageQuality, mimeType);
            } catch (LoginException e) {
                throw new RenditionGenerationException(e);
            } catch (IOException e) {
                // Catching system exception, rethrowing as runtime.
                log.error("failed to generate rendition", e);
                throw new RenditionGenerationException(e);
            } finally {
                if (adminResolver != null) {
                    adminResolver.close();
                }
            }
            retVal = imageAsset.getRendition(renditionInfo.getRenditionName(imageAspect, imageQuality, extension));
        } else {
            log.debug("using existing rendition for {}", imageAsset.getPath());
        }
        return retVal;
    }

    /** Determines which of the rendition's supported aspects is closest to the actual dimensions of the image. 
     *
     * The tiff:imageLength and tiff:imageWidth properties are used to determine the image's dimensions. The actual dimensions
     * could be used, but a layer would have to be opened just for that.
     */
    private ImageAspect getBestAspect(Asset imageAsset) {
        Integer height = null;
        Integer width = null;

        if (renditionInfo.getAspects().size() == 1) {
            return renditionInfo.getDefaultAspect();
        }

        String heightStr = imageAsset.getMetadataValue("tiff:ImageLength");
        String widthStr = imageAsset.getMetadataValue("tiff:ImageWidth");

        if (!heightStr.isEmpty() && !widthStr.isEmpty()) {
            try {
                height = Integer.valueOf(heightStr);
                width = Integer.valueOf(widthStr);
            } catch (NumberFormatException e) {
                // do nothing, we'll use the default aspect
            }
        }

        if (height == null || width == null) {
            return renditionInfo.getDefaultAspect();
        }

        return renditionInfo.getNearestAspect(width.intValue(), height.intValue());
    }
}
