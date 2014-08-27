package com.scrippsnetworks.wcm.image;

import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Set;
import com.scrippsnetworks.wcm.image.ImageDimensions;

/** Encapsulates renditions as a collection of image dimensions of various aspects.
 *
 * A rendition is a virtual layer allowing for actual rendition dimensions to vary depending on the aspect of the original. If
 * your requirement is that a rendition be 616 pixels wide, but that the height can vary depending on whether the image is
 * 4x3, 3x4, 16x9, etc., this enum can accomodate that by providing several possible dimensions which can be selected from
 * at runtime based on the original image's dimensions.
 *
 * @author Scott Everett Johnson
 */
public enum RenditionInfo {
    // old image sizes are lowercase
    // sm(new ImageDimensions[] { ImageDimensions.w92h69 }),
    // tz(new ImageDimensions[] { ImageDimensions.w120h90, ImageDimensions.w90h120 }),
    // med(new ImageDimensions[] { ImageDimensions.w160h120 }),
    // al(new ImageDimensions[] { ImageDimensions.w266h200, ImageDimensions.w266h354 }),
    // lead(new ImageDimensions[] { ImageDimensions.w400h300 }),
    // lg(new ImageDimensions[] { ImageDimensions.w616h462, ImageDimensions.w616h821 }),
    sni2col(new ImageDimensions[] { ImageDimensions.w56h56 }),
    sni3col(new ImageDimensions[] { ImageDimensions.w91h68, ImageDimensions.w91h91, ImageDimensions.w91h51 }),
    sni4col(new ImageDimensions[] { ImageDimensions.w126h95, ImageDimensions.w126h126, ImageDimensions.w126h71 }),
    sni5col(new ImageDimensions[] { ImageDimensions.w161h121, ImageDimensions.w161h161, ImageDimensions.w161h91 }),
    sni6col(new ImageDimensions[] { ImageDimensions.w196h147, ImageDimensions.w196h262, ImageDimensions.w196h196, ImageDimensions.w196h110 }),
    sni8col(new ImageDimensions[] { ImageDimensions.w266h200, ImageDimensions.w266h355, ImageDimensions.w266h266, ImageDimensions.w266h150 }),
    sni9col(new ImageDimensions[] { ImageDimensions.w301h226, ImageDimensions.w301h401, ImageDimensions.w301h301, ImageDimensions.w301h169 }),
    sni10col(new ImageDimensions[] { ImageDimensions.w336h252, ImageDimensions.w336h448, ImageDimensions.w336h336, ImageDimensions.w336h189 }),
    sni12col(new ImageDimensions[] { ImageDimensions.w406h305, ImageDimensions.w406h541, ImageDimensions.w406h406, ImageDimensions.w406h228 }),
    sni18col(new ImageDimensions[] { ImageDimensions.w616h462, ImageDimensions.w616h821, ImageDimensions.w616h616, ImageDimensions.w616h347 }),
    sniipad(new ImageDimensions[] { ImageDimensions.w1024h768, ImageDimensions.w768h1024 }),
    sniipadlarge(new ImageDimensions[] { ImageDimensions.w2048h1536, ImageDimensions.w1536h2048 }),
    snicarousel(new ImageDimensions[] { ImageDimensions.w322h242 }),
    snigalleryslide(new ImageDimensions[] { ImageDimensions.w616h462, ImageDimensions.w616h347, ImageDimensions.w347h462 }),
    snigallerythumb(new ImageDimensions[] { ImageDimensions.w126h95 }),
    w141h106(new ImageDimensions[] { ImageDimensions.w141h106 }),
    snibioportrait(new ImageDimensions[] { ImageDimensions.w347h462 }),
    superlead2x(new ImageDimensions[]{ImageDimensions.w532h400, ImageDimensions.w532h710, ImageDimensions.w532h532, ImageDimensions.w532h300 }),
    circlecarousel(new ImageDimensions[]{ImageDimensions.w116h116}),
    circlecarousel2x(new ImageDimensions[]{ImageDimensions.w232h232}),
    imageacross2x(new ImageDimensions[]{ImageDimensions.w252h190}),
    searchimage(new ImageDimensions[]{ImageDimensions.w126h96}),
    searchimage2x(new ImageDimensions[]{ImageDimensions.w252h192}),
    searchvideoimage(new ImageDimensions[]{ImageDimensions.w126h71}),
    searchvideoimage2x(new ImageDimensions[]{ImageDimensions.w252h142}),
    sni3col2x(new ImageDimensions[] { ImageDimensions.w182h136, ImageDimensions.w182h182, ImageDimensions.w182h102 }),
    sni2col2x(new ImageDimensions[] { ImageDimensions.w102h102 }),
    sni10col2x(new ImageDimensions[] { ImageDimensions.w672h504, ImageDimensions.w672h896, ImageDimensions.w672h672, ImageDimensions.w672h378 }),
    sni6col2x(new ImageDimensions[] { ImageDimensions.w392h294, ImageDimensions.w392h524, ImageDimensions.w392h392, ImageDimensions.w392h220 });

    HashMap<ImageAspect, ImageDimensions> dimensions = new HashMap<ImageAspect, ImageDimensions>();

    // First in array passed to constructor.
    ImageAspect defaultAspect;

    RenditionInfo(ImageDimensions[] dims) {

        if (dims.length < 1) {
            throw new RuntimeException("must initialize rendition with at least one dimension");
        }

        defaultAspect = dims[0].getAspect();
        for (ImageDimensions d : dims) {
            if (dimensions.get(d.getAspect()) != null) {
                throw new RuntimeException("only one dimension per aspect allowed");
            }
            dimensions.put(d.getAspect(), d);
        }
    }

    /** True if there is a rendition defined of the requested aspect. */
    public boolean hasAspect(String aspectStr) {
        return dimensions.containsKey(ImageAspect.valueOf(aspectStr));
    }

    /** True if there is a rendition defined of the requested aspect. */
    public boolean hasAspect(ImageAspect aspect) {
        return aspect == null ? false : dimensions.containsKey(aspect);
    }

    /** Returns the dimensions of the rendition of the requested aspect. */
    public ImageDimensions getImageDimensions(ImageAspect aspect) {
        if (aspect == null) {
            aspect = defaultAspect;
        }
        return dimensions.get(aspect);
    }

    /** Returns the dimensions of the rendition's default aspect. */
    public ImageDimensions getImageDimensions() {
        return dimensions.get(defaultAspect);
    }

    /** Returns the aspect of the rendition dimensions most closely matching the provided dimensions. */
    public ImageAspect getNearestAspect(int width, int height) {
        Double ratio = (double) width / height;
        TreeMap<Double, ImageAspect> distances = new TreeMap<Double, ImageAspect>();
        for (Map.Entry<ImageAspect, ImageDimensions> entry : dimensions.entrySet()) {
            Double aspectRatio = entry.getKey().ratio();
            Double distance = Math.abs(aspectRatio - ratio);
            distances.put(distance, entry.getKey());
        }
        return distances.firstEntry().getValue();
    }

    /** Returns the name of the rendition of the requested aspect and extension. */
    public String getRenditionName(ImageAspect imageAspect, ImageQuality imageQuality, String extension) {
        return dimensions.get(imageAspect).getRenditionName(imageQuality, extension);
    }

    /** Returns the default aspect for this rendition. */
    public ImageAspect getDefaultAspect() {
        return defaultAspect;
    }

    /** Returns all defined aspects for this rendition. */
    public Set<ImageAspect> getAspects() {
        return dimensions.keySet();
    }
}
