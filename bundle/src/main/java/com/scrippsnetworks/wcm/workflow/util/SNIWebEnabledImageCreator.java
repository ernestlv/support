package com.scrippsnetworks.wcm.workflow.util;

import com.day.cq.dam.api.Asset;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.image.Layer;
import com.day.cq.dam.commons.util.OrientationUtil;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.commons.mime.MimeTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* The <code>SNIWebEnabledImageCreator</code> is a custom SNI class provides all functionality in
* order to create a web enabled image. This class is being modified to restrict the aspect ratio
* processing while resizing the renditions.
*/
public class SNIWebEnabledImageCreator {
    /**
* Logger instance for this class.
*/
    private static final Logger log = LoggerFactory.getLogger(SNIWebEnabledImageCreator.class);

    private static final String WEB_SPECIFIER = "web";

    /** the asset */
    private Asset asset;

    /** the mimetype service used for lookup */
    private MimeTypeService mimeTypeService;

    public SNIWebEnabledImageCreator(Asset asset, MimeTypeService mimeTypeService) {
        this.asset = asset;
        this.mimeTypeService = mimeTypeService;
    }

    /**
* This method creates the web enabled rendition. if the <code>force</code>
* param is set to <code>false</code> than following check is executed:
* check if layer has to persisted. in case the layer is still equal
* than the ori file can be taken (save space).
*
* @param image buffered image used to create web enabled image
* @param defaultMimetype default output mimetype
* @param dimensions comma separated string containing max. with, max. height
* @param keepFormat contains all mimetypes that should be kept (instead of using the default mimetype)
* @param qualityStr image quality in percent
* @param force if <code>true</code> than the webenabled image is always created
* nevertheless the ori image is smaller than the requested image dimensions
*
* @throws RepositoryException in case the web rendition could not be persisted
* @throws IOException while processing the image
*/
    public void create(BufferedImage image, String defaultMimetype, String dimensions,
                       String keepFormat, String qualityStr, boolean force)
            throws RepositoryException, IOException {
        int maxWidth = getDimension(dimensions)[0];
        int maxHeight = getDimension(dimensions)[1];
        String oriMimeType = getMimeType(asset);

        // test if ori mimetype/format has to be kept
        String mimetype = (StringUtils.isNotBlank(oriMimeType) && keepFormat.contains(oriMimeType))
                ? oriMimeType : defaultMimetype;

        double quality = (mimetype.equals("image/gif")) ? getQuality(255, qualityStr) : getQuality(1.0, qualityStr);

        // create image
        Layer layer = createImage(image, maxWidth, maxHeight);
        
        // rotate image according to orientation metadata
        if(OrientationUtil.hasOrientationMetadata(asset)) {
            OrientationUtil.adjustOrientation(asset, layer);
        }

        String renditionName = "cq5dam.web." + maxWidth + "." + maxHeight + "." + getExtension(mimetype);

        // persist
        if (StringUtils.contains(keepFormat, oriMimeType)) {
            // check if layer has to persisted. in case the layer is still equal
            // than the ori file can be taken (save space)
            if (image.getHeight() == layer.getHeight() && image.getWidth() == layer.getWidth() && !force) {
                InputStream oriIs = null;
                try {
                    oriIs = asset.getOriginal().adaptTo(Node.class)
                            .getProperty(JcrConstants.JCR_CONTENT + "/" + JcrConstants.JCR_DATA).getBinary().getStream();
                    asset.addRendition(renditionName, oriIs, mimetype);
                } finally {
                    IOUtils.closeQuietly(oriIs);
                }
            } else {
                // save layer as new file
                saveImage(asset, layer, mimetype, quality, renditionName);
            }
        } else {
            // save layer as new file
            saveImage(asset, layer, mimetype, quality, renditionName);
        }
    }

    //-------------< helper >---------------------------------------------------
    protected void saveImage(Asset asset, Layer layer, String mimetype,
                           double quality, String renditionName) throws IOException {
        File tmpFile = File.createTempFile(WEB_SPECIFIER, "." + getExtension(mimetype));
        OutputStream out = FileUtils.openOutputStream(tmpFile);
        InputStream is = null;
        try {
            layer.write(mimetype, quality, out);
            is = FileUtils.openInputStream(tmpFile);
            asset.addRendition(renditionName, is, mimetype);
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(is);
            FileUtils.deleteQuietly(tmpFile);
        }
    }

    /**
* A image with the given <code>maxWith</code> and <code>maxHeight</code>
* is generated out if the <code>image</code> source.
*
* @param image image source
* @param maxWidth max. thumbnail width
* @param maxHeight max. thumbnail height
* @return the resized image layer
*/
    protected Layer createImage(BufferedImage image, int maxWidth, int maxHeight) {
        long startTime = System.currentTimeMillis();
        Layer layer = new Layer(image);

        //The below out of the box code takes care of the aspect ratio processing which we are disabling in this custom class
        /*int height = layer.getHeight();
        int width = layer.getWidth();

        if (height > maxHeight || width > maxWidth) {
            // resize image
            int newWidth, newHeight;
            if (height > width) {
                newHeight = maxHeight;
                newWidth = (width * maxHeight / height);
                if (newWidth > maxWidth) {
                    newWidth = maxWidth;
                    newHeight = (height * maxWidth / width);
                }
            } else {
                newWidth = maxWidth;
                newHeight = (height * maxWidth / width);
                if (newHeight > maxHeight) {
                    newHeight = maxHeight;
                    newWidth = (width * maxHeight / height);
                }
            }
            layer.resize(newWidth, newHeight);
        }*/
		        
        layer.resize(maxWidth, maxHeight);
        
        //Sometimes it does not resize correctly (1 Pixel Off API BUG?) just adding the below check to resize correctly
        if((layer.getWidth() != maxWidth) && (layer.getHeight() != maxHeight)){
        	layer.resize(maxWidth, maxHeight);
        }
                
        // ensure "transparency" (for gif images)
        if (asset.getName().endsWith(".gif")) {
            layer.setTransparency(new Color(0xFFF0E0D0));
        }
        log.debug("createImage took " + (System.currentTimeMillis() - startTime) + "ms");
        return layer;
    }

    protected String getExtension(String mimetype) {
        return mimeTypeService.getExtension(mimetype);
    }

    protected String getMimeType(Asset asset) {
        String name = asset.getName().toLowerCase();
        return mimeTypeService.getMimeType(name);
    }

    protected Integer[] getDimension(String dimensions) {
        if (dimensions != null) {
            String splits[] = dimensions.split(":");
            Integer d[] = new Integer[2];
            d[0] = Integer.valueOf(splits[0]);
            d[1] = Integer.valueOf(splits[1]);
            return d;
        }
        // default value(s)
        return new Integer[]{1000, 1000};
    }

    protected double getQuality(double base, String qualityStr) {
        int q = Integer.valueOf(qualityStr);
        double res = base * q / 100;
        return res;
    }
}