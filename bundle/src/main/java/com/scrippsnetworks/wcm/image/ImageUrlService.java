package com.scrippsnetworks.wcm.image;

import java.lang.String;
import com.scrippsnetworks.wcm.image.RenditionInfo;
import com.scrippsnetworks.wcm.image.ImageAspect;

/** Service to provide urls for the dynamic image resizing servlet.
 *
 * @author Scott Everett Johnson
 */
public interface ImageUrlService {

    /** Returns the url for the image original. */
    public String getImageUrl(String path);

    /** Returns the url for the requested rendition of the image. */
    public String getImageUrl(String path, RenditionInfo rendition);

    /** Returns the url for the requested aspect ratio of the requested rendition of the image. */
    public String getImageUrl(String path, RenditionInfo rendition, ImageAspect aspect);

    /** Returns the url for the requested aspect ratio of the requested rendition for the requested domain of the image. */
    public String getImageUrl(String path, RenditionInfo rendition, ImageAspect aspect, String domain);
}
