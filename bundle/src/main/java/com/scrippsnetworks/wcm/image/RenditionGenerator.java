package com.scrippsnetworks.wcm.image;

import com.day.cq.dam.commons.util.MemoryUtil;
import com.day.cq.commons.ImageHelper;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.api.Asset;
import com.day.image.Layer;
import org.apache.sling.commons.mime.MimeTypeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.IllegalArgumentException;

/** Generates and saves renditions for Assets.
 *
 * This class uses the ImageDimensions enum to determine target rendition dimensions, and the name of the saved rendition.
 * When the image's aspect does not match the ratio of the requested dimensions, a crop is applied which is centered on the
 * too-large dimension. That is, if the original is taller than the requested rendition, a crop is made which is as wide as
 * the original and cut equally on top and bottom to match the aspect of the requested dimensions.
 *
 * @author Scott Everett Johnson
 */
public class RenditionGenerator {

    private final Logger log = LoggerFactory.getLogger(RenditionGenerator.class);
    
    private static final String IMAGE_WIDTH_PROPERTY = "tiff:ImageWidth";
    private static final String IMAGE_HEIGHT_PROPERTY = "tiff:ImageLength";
    private static final String HORIZONTAL_RENDITION_NAME = "horizontal-base";

    Asset imageAsset;
    MimeTypeService mimeTypeService;

    /** Create a new RenditionGenerator for the given asset, providing the MimeTypeService as well.
     *
     * An IllegalArgumentException exception will be thrown if either argument is null.
     * */
    public RenditionGenerator(Asset imageAsset, MimeTypeService mimeTypeService) {
        if (imageAsset == null || mimeTypeService == null) {
            throw new IllegalArgumentException("all arguments to rendition generator constructor must be nonnull");
        }
        this.imageAsset = imageAsset;
        this.mimeTypeService = mimeTypeService;
    }

    /** Generates a rendition for the provided ImageDimensions and mime type.
     *
     * @param dimensions ImageDimensions enum value from which to acquire rendition information.
     * @param mimeType The mime type of the rendition, also determines rendition name extension.
     * @throws IOException when an IO exception is encountered
     * @throws IllegalArgumentException if arguments are  null, empty, or invalid
     * @throws RenditionGenerationException if MemoryUtil#hasEnoughMemory returns false
     */
    public void generate(final ImageDimensions dimensions, ImageQuality imageQuality, final String mimeType) throws IOException {

        if (dimensions == null) {
            throw new IllegalArgumentException("dimensions must be nonnull");
        }

        if (mimeType == null || mimeType.isEmpty()) {
            throw new IllegalArgumentException("mime type must be provided");
        }
        
        String extension = mimeTypeService.getExtension(mimeType);
        if (extension == null) {
            throw new IllegalArgumentException("valid mime type must be provided");
        }

        if (!MemoryUtil.hasEnoughMemory(imageAsset)) {
            throw new RenditionGenerationException("not enough memory to proceed");
        }

        Layer layer = resize(dimensions);

        // byte array will be used to save image data to jcr
        ByteArrayOutputStream imgOut = new ByteArrayOutputStream();
        
        //gifs do compression numbers differently than other images, set 100% quality at first using whatever number is appropriate
        double quality = ("image/gif".equals(mimeType)) ? 255.0D : 1D;
        quality = quality * imageQuality.getQualityFactor() * dimensions.getQualityFactor();
        
        // apply compression to image
        layer.write(mimeType, quality, imgOut);
        
        // add the new rendition we've created to the original image in DAM
        imageAsset.addRendition(dimensions.getRenditionName(imageQuality, extension),
                    new ByteArrayInputStream(imgOut.toByteArray()), mimeType);
    }

    /** Returns a layer resized to the requested dimensions. */
    public Layer resize(final ImageDimensions dimensions) {

        int renditionWidth = dimensions.getWidth();
        int renditionHeight = dimensions.getHeight();
        int originalWidth = 0;
        int originalHeight = 0;

        if (! imageAsset.getMetadataValue(IMAGE_WIDTH_PROPERTY).isEmpty() &&
            ! imageAsset.getMetadataValue(IMAGE_HEIGHT_PROPERTY).isEmpty()) {
            originalWidth = Integer.parseInt(imageAsset.getMetadataValue(IMAGE_WIDTH_PROPERTY));
            originalHeight = Integer.parseInt(imageAsset.getMetadataValue(IMAGE_HEIGHT_PROPERTY));
        }
        
        Layer layer = null;
        //Use the rendition horizontal-base instead of the original if this is a request for a horizontal rendition of a vertical image
        if (renditionWidth > renditionHeight && originalWidth < originalHeight) {
            Rendition horizontalBase = imageAsset.getRendition(HORIZONTAL_RENDITION_NAME);
            if (horizontalBase != null) {
                layer = ImageHelper.createLayer(horizontalBase);
            }
        }
        if (layer == null) {
            layer = ImageHelper.createLayer(imageAsset.getOriginal());
        }
        Layer returnLayer = null;

        Rectangle cropRect = dimensions.getCropRect(layer.getWidth(), layer.getHeight());

        // crop the image
        layer.crop(cropRect);
        
        //resize layer
        layer.resize(renditionWidth, renditionHeight);
        
        returnLayer = layer;
        
        return returnLayer;
    }

}
