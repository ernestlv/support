package com.scrippsnetworks.wcm.image;

import java.lang.Math;
import java.awt.Rectangle;

import com.scrippsnetworks.wcm.image.ImageAspect;

/** Dimensions for use in specifying image renditions.
 *
 * Each set of dimensions also has an ImageAspect provided which needn't be exactly the ratio of the dimensions themselves.
 *
 * @author Scott Everett Johnson
 */
public enum ImageDimensions {
	w182h136(182,136,ImageAspect.landscape),
    w91h68(91,68,ImageAspect.landscape),
    w126h95(126,95,ImageAspect.landscape),
    w126h96(126,96,ImageAspect.landscape),
    w141h106(141,106,ImageAspect.landscape),
    w161h121(161,121,ImageAspect.landscape),
    w196h147(196,147,ImageAspect.landscape),
    w252h190(252,190,ImageAspect.landscape),
    w252h192(252,192,ImageAspect.landscape),
    w266h200(266,200,ImageAspect.landscape),
    w532h400(532,400,ImageAspect.landscape),
    w301h226(301,226,ImageAspect.landscape),
    w322h242(322,242,ImageAspect.landscape),
    w336h252(336,252,ImageAspect.landscape),
    w347h462(347,462,ImageAspect.portrait),
    w406h305(406,305,ImageAspect.landscape),
    w616h462(616,462,ImageAspect.landscape),
    w1024h768(1024,768,ImageAspect.landscape),
    w2048h1536(2048,1536,ImageAspect.landscape),
    w672h504(672,504,ImageAspect.landscape),
    w392h294(392,294,ImageAspect.landscape),
    w266h355(266,355,ImageAspect.portrait),
    w532h710(532,710,ImageAspect.portrait),
    w301h401(301,401,ImageAspect.portrait),
    w336h448(336,448,ImageAspect.portrait),
    w406h541(406,541,ImageAspect.portrait),
    w616h821(616,821,ImageAspect.portrait),
    w768h1024(768,1024,ImageAspect.portrait),
    w1536h2048(1536,2048,ImageAspect.portrait),
    w672h896(672,896,ImageAspect.portrait),
    w196h262(196,262,ImageAspect.portrait),
    w392h524(392,524,ImageAspect.portrait),
    w56h56(56,56,ImageAspect.square),
    w102h102(102,102,ImageAspect.square),
    w91h91(91,91,ImageAspect.square),
    w182h182(182,182,ImageAspect.square),
    w126h126(126,126,ImageAspect.square),
    w161h161(161,161,ImageAspect.square),
    w196h196(196,196,ImageAspect.square),
    w266h266(266,266,ImageAspect.square),
    w532h532(532,532,ImageAspect.square),
    w301h301(301,301,ImageAspect.square),
    w336h336(336,336,ImageAspect.square),
    w406h406(406,406,ImageAspect.square),
    w116h116(116,116,ImageAspect.square),
    w232h232(232,232,ImageAspect.square),
    w616h616(616,616,ImageAspect.square),
    w672h672(672,672,ImageAspect.square),
    w392h392(392,392,ImageAspect.square),
    w91h51(91,51,ImageAspect.wide),
    w182h102(182,102,ImageAspect.wide),
    w126h71(126,71,ImageAspect.wide),
    w161h91(161,91,ImageAspect.wide),
    w196h110(196,110,ImageAspect.wide),
    w252h142(252,142,ImageAspect.wide),
    w266h150(266,150,ImageAspect.wide),
    w532h300(532,300,ImageAspect.wide),
    w301h169(301,169,ImageAspect.wide),
    w336h189(336,189,ImageAspect.wide),
    w406h228(406,228,ImageAspect.wide),
    w616h347(616,347,ImageAspect.wide),
    w672h378(672,378,ImageAspect.wide),
    w392h220(392,220,ImageAspect.wide),

    // old standard sizes
    w92h69(92,69,ImageAspect.landscape),
    w120h90(120,90,ImageAspect.landscape),
    w90h120(90,120,ImageAspect.portrait),
    w160h120(160,120,ImageAspect.landscape),
    w266h220(266,120,ImageAspect.landscape),
    w266h354(266,354,ImageAspect.portrait),
    w400h300(400,300,ImageAspect.landscape);

	public static final String RENDITION_PREFIX = "sni";
    public static final String DEFAULT_EXTENSION = "jpeg";
    public static final double DEFAULT_IMAGE_QUALITY_FACTOR = 1D;
    private final int height;
    private final int width;
    private final int ratioHeight;
    private final int ratioWidth;
    private final long gcm;
    private final ImageAspect aspect;
    private double qualityFactor;

    /** Creates an enum value with the given width, height, and aspect key. */
    ImageDimensions(int width, int height, ImageAspect aspect) {
        this.width = width;
        this.height = height;
        this.aspect = aspect;
        this.gcm = gcm(width, height);
        this.ratioHeight = Math.round(height / (1.0f * gcm));
        this.ratioWidth = Math.round(width / (1.0f * gcm));
        this.qualityFactor = DEFAULT_IMAGE_QUALITY_FACTOR;
    }

    /** Returns the actual aspect ratio for these dimensions. */
    public float aspectRatio() {
        return (float) width / height;
    }

    /** Calculates and returns the greatest common multiple of these dimensions. */
    private static long gcm(long a, long b) {
        return b == 0 ? a : gcm(b, a % b);
    }

    /* Returns the greatest common multiple of these dimensions. 
    public long getGcm() { return gcm; } */

    /** Returns the height. */
    public int getHeight() { return height; }

    /** Returns the width. */
    public int getWidth() { return width; }

    /** Returns the denominator of the ratio of these dimensions. */
    public int getRatioHeight() { return ratioHeight; }

    /** Returns the numerator of the ratio of these dimensions. */
    public int getRatioWidth() { return ratioWidth; }

    /** Returns the ImageAspect of this rendition. */
    public ImageAspect getAspect() { return aspect; }

    /** Returns the rendition name for renditions with these dimensions. */
    public String getRenditionName(ImageQuality imageQuality, String extension) {
        if (extension == null) {
            extension = DEFAULT_EXTENSION;
        };
        return RENDITION_PREFIX + "." + imageQuality.name() + "." + String.valueOf(width) + "." + String.valueOf(height) + "." + extension;
    }

    /** Returns a Rectange for use in cropping an image of the given height and width to fit in the aspect of these dimensions. */
    public Rectangle getCropRect(int widthParam, int heightParam) {
        int finalWidth = 0;
        int finalHeight = 0;
        int cropX = 0;
        int cropY = 0;
        float ratioSize = aspectRatio();
        
        if (widthParam * ratioSize <= heightParam) {
            //widthParam is greater than what is needed, crop x, keep y
            finalWidth = Math.round(heightParam * ratioSize);
            finalHeight = heightParam;
            if (finalWidth <= widthParam) {
                cropX = Math.round((widthParam - finalWidth) / 2.0f);
                cropY = 0;
            } else {
                //crop is wider than original image, take full image widthParam and cropx instead
                finalWidth = widthParam;
                finalHeight = Math.round(widthParam / ratioSize);
                cropX = 0;
                cropY = Math.round((heightParam - finalHeight) / 2.0f);
            }
        } else {
            //widthParam is smaller than what we need, crop on y keeping x
            finalWidth = widthParam;
            finalHeight = Math.round(widthParam / ratioSize);
            if (finalHeight <= heightParam) {
                cropX = 0;
                cropY = Math.round((heightParam - finalHeight) / 2.0f);
            } else {
                // the heightParam we needed was greater than the
                // heightParam of the image, take the full image
                // heightParam and crop x instead
                finalWidth = Math.round(heightParam * ratioSize);
                finalHeight = heightParam;
                cropX = Math.round((widthParam - finalWidth) / 2.0f);
                cropY = 0;
            }
        }
        return new Rectangle(cropX, cropY, finalWidth, finalHeight);
    }

    public double getQualityFactor() {
        return qualityFactor;
    }
}
