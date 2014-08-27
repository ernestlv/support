package com.scrippsnetworks.wcm.asset.photogallery;

import com.scrippsnetworks.wcm.parsys.Paginator;
import com.scrippsnetworks.wcm.taglib.Functions;
import com.scrippsnetworks.wcm.util.CharsetConversion;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility stuff for photo gallery rendering
 * Also contains any static strings that could be abstracted and used for other purposes
 * @author Jason Clark
 * Date: 7/8/12
 */
@Deprecated
public class PhotoGalleryUtil {

    /* STATIC STRINGS */
    public final static String PHOTO_GALLERY_RESOURCE_TYPE = "sni-core/components/pagetypes/photo-gallery";
    public final static String IMAGE_RESOURCE_NAME = "frame/image";
    public final static String SUBHEADING_PROPERTY_NAME = "title";
    public final static String CAPTION_PROPERTY_NAME = "caption";
    public final static String ATTACHED_CONTENT_PATH_PROPERTY_NAME = "attachedContent";
    public final static String ATTACHED_CONTENT_TYPE_PROPERTY_NAME = "attachedContentType";
    public final static String FILE_REFERENCE_PARAMETER_NAME = "fileReference";
    public final static String ASSET_TITLE_PROPERTY_NAME = "jcr:title";
    public final static String IMAGE_ORIENTATION_PROPERTY_NAME = "sni:orientation";
    public final static String RESOURCE_TYPE_PROPERTY_NAME = "sling:resourceType";
    public final static String IMAGE_ORIENTATION_VERTICAL = "vertical";

    /* PATHS RELATIVE TO BASE IMAGE NODE */
    public final static String IMAGE_METADATA_NODE_PATH = "/jcr:content/metadata";
    public final static String RENDITIONS_PATH = "/jcr:content/renditions/";
    public final static String WEB_RENDITION_PREFIX = "cq5dam.web";
    public final static String THUMBNAIL_RENDITION_PATH = RENDITIONS_PATH + WEB_RENDITION_PREFIX + ".92.69.jpeg";
    public final static String GALLERY_HORIZONTAL_RENDITION_PATH = RENDITIONS_PATH + WEB_RENDITION_PREFIX + ".616.462.jpeg";
    public final static String GALLERY_VERTICAL_RENDITION_PATH = RENDITIONS_PATH + WEB_RENDITION_PREFIX + ".616.821.jpeg";

	private final static Logger log = LoggerFactory.getLogger(PhotoGalleryUtil.class);

    /**
     * don't instantiate me!
     */
    private PhotoGalleryUtil() {}

    /**
     * Checks if a given Resource is a photo gallery component
     * @param resource Sling Resource that you wish to check
     * @return boolean true if resource is a photo gallery component, otherwise false
     */
    public static boolean isPhotoGalleryComponent(final Resource resource) {
        if (resource == null) {
            return false;
        }
        ValueMap props = ResourceUtil.getValueMap(resource);
        if (props.containsKey(RESOURCE_TYPE_PROPERTY_NAME)) {
            String type = props.get(RESOURCE_TYPE_PROPERTY_NAME, String.class);
            return type.equals(PHOTO_GALLERY_RESOURCE_TYPE);
        } else {
            return false;
        }
    }

    /**
     * Retrieve the Resource for an image contained in a Photo Gallery component
     * @param resource Sling Resource of the photo gallery component you wish to retrieve an image from
     * @return Resource for the image that you are retrieving
     */
    public static Resource imageFromPhotoGallery(final Resource resource) {
        if (resource == null) {
            return null;
        }
        return Functions.getResourceChild(resource, IMAGE_RESOURCE_NAME);
    }

    /**
     * Retrieve the subhead property from a photo gallery component resource
     * @param resource Sling Resource of the photo gallery you are getting the subhead from
     * @return String of the value of subhead, without markup
     */
    public static String subheadingFromPhotoGallery(final Resource resource) {
        if (resource == null) {
            return null;
        }
        return Functions.removeMarkup(Functions.getResourceProperty(resource, SUBHEADING_PROPERTY_NAME));
    }

    /**
     * Retrieve the caption property from a photo gallery component resource
     * @param resource Sling Resource of the photo gallery you a retrieving the caption from
     * @return String value of the caption, without markup
     */
    public static String captionFromPhotoGallery(final Resource resource) {
        if (resource == null) {
            return null;
        }
        return CharsetConversion.convertToUniCode(Functions.getResourceProperty(resource, CAPTION_PROPERTY_NAME));
    }

    /**
     * Retrieve the attached content path property from a photo gallery component
     * @param resource Sling Resource of the photo gallery you wish to retrieve the attached content path from
     * @return String value of the attached content path
     */
    public static String attachedContentPathFromPhotoGallery(final Resource resource) {
        if (resource == null) {
            return null;
        }
        return Functions.getResourceProperty(resource, ATTACHED_CONTENT_PATH_PROPERTY_NAME);
    }

    /**
     * Retrieves the value from the attachedContentType property on the given photo gallery component Resource
     * This value appears as the "slug" above the attached content link on the photo gallery
     * @param resource Sling Resource of the photo gallery component
     * @return String value of the slug for the attached content type
     */
    public static String attachedContentTypeSlugFromPhotoGallery(final Resource resource) {
        if (resource == null) {
            return null;
        }
        return Functions.getResourceProperty(resource, ATTACHED_CONTENT_TYPE_PROPERTY_NAME);
    }

    /**
     * Retrieve the value of the title property from the asset attached to the photo gallery
     * @param resource Sling Resource of the photo gallery
     * @return String title of the asset attached to the photo gallery
     */
    public static String attachedContentTitleFromPhotoGallery(final Resource resource) {
        if (resource == null) {
            return null;
        }
        String jcrContentPath = attachedContentPathFromPhotoGallery(resource) + "/jcr:content";
        Resource contentResource = Functions.getResource(resource.getResourceResolver(), jcrContentPath);
        if (contentResource == null) {
            return null;
        }
        Node contentNode = contentResource.adaptTo(Node.class);
        try {
            if (contentNode.hasProperty(ASSET_TITLE_PROPERTY_NAME)) {
                return contentNode.getProperty(ASSET_TITLE_PROPERTY_NAME).getString();
            } else {
                return null;
            }
        } catch (RepositoryException e) {
            return null;
        }
    }

    /**
     * Retrieve the fileReference value (path to image) from an image Resource
     * @param resource Sling Resource for the image you wish to retrieve the path from
     * @return String of the image's resource path
     */
    public static String pathFromImage(final Resource resource) {
        if (resource == null) {
            return null;
        }
        Node imageNode = resource.adaptTo(Node.class);
        try {
            if (imageNode != null && imageNode.hasProperty(FILE_REFERENCE_PARAMETER_NAME)) {
                return imageNode.getProperty(FILE_REFERENCE_PARAMETER_NAME).getString();
            } else {
                return null;
            }
        } catch (RepositoryException e) {
            return null;
        } catch(NullPointerException ex){
        	return null;
        }
    }

    /**
     * Return a path to an image's photo gallery-sized rendition.
     * @param resource Resource of an image in DAM
     * @return String path to the gallery-sized rendition of the image
     */
    public static String galleryRenditionPathFromImage(final Resource resource, final String orientation) {
        if (resource == null) {
            return null;
        }
        if (orientation == null) {
            return pathFromImage(resource);
        } else if (orientation.equals(IMAGE_ORIENTATION_VERTICAL)) {
            return pathFromImage(resource) + GALLERY_VERTICAL_RENDITION_PATH;
        } else {
            return pathFromImage(resource) + GALLERY_HORIZONTAL_RENDITION_PATH;
        }
    }
    
    /**
     * Return just the preferred rendition based on orientation
     * @return String path to the gallery-sized rendition of the image
     */
    public static String galleryRenditionPrefix(final String orientation) {
        if (orientation == null) {
            return null;
        } else if (orientation.equals(IMAGE_ORIENTATION_VERTICAL)) {
            return "sni.wcm.616.821";
        } else {
            return "sni.wcm.616.462";
        }
    }

    /**
     * Return the path to the image's thumbnail rendition, given the Resource of an image
     * @param resource Sling Resource of the image whose thumbnail path you want
     * @return String path to the thumbnail rendition
     */
    public static String thumbPathFromImage(final Resource resource) {
        if (resource == null) {
            return null;
        }
        return pathFromImage(resource) + THUMBNAIL_RENDITION_PATH;
    }

    /**
     * Retrieve the description of the image orientation from an image node
     * @param resource Sling Resource of the image whose orientation you want
     * @return String value of the image orientation
     */
    public static String imageOrientation(final Resource resource) {
        if (resource == null) {
            return null;
        }
        Resource metadataResource = Functions.getResource(resource.getResourceResolver(),
                pathFromImage(resource) + IMAGE_METADATA_NODE_PATH);

        return Functions.getResourceProperty(metadataResource, IMAGE_ORIENTATION_PROPERTY_NAME);
    }

    /**
     * Generate a List of PhotoGallery objects from a Paginator. This retrieves all of the photo gallery
     * components without page-break components.  This method also sets the pageNum and pagePath fields
     * on each photo gallery object as it is retrieved.
     * @param paginator Paginator object from which to retrieve PhotoGallery objects
     * @return List of PhotoGallery objects
     */
    public static List<PhotoGallery> photoGalleriesFromPaginator(final Paginator paginator) {
        if (paginator == null) {
            return null;
        }

        List<PhotoGallery> galleries = new ArrayList<PhotoGallery>();
        Iterator<Resource> pageIterator = paginator.getParagraphs().iterator();

        //this is the path to the page resource, before selectors, extension & suffix
        String pagePath = paginator.getSlingRequest().getRequestPathInfo().getResourcePath()
                .replaceFirst("/jcr:content.*", "");

        //will be used to increment "page number". assumption is one gallery per "page"
        int pageNum = 0;

        while (pageIterator.hasNext()) {
            Resource resource = pageIterator.next();
            //skip if we're on a page break component
            if (!PhotoGalleryUtil.isPhotoGalleryComponent(resource)) {
                continue;
            }
            //only increment page count if on a photo gallery component
            pageNum++;
            PhotoGallery gallery = new PhotoGallery(resource);
            gallery.setPageNum(pageNum);
            gallery.setPagePath(pagePath);
            galleries.add(gallery);
        }
        return galleries;
    }

    /**
     * ugggh
     * Identify and return a PhotoGallery from a List using that PhotoGallery's imgPath as a key
     * @param galleries List of PhotoGallery objects
     * @param path String path of image
     * @return PhotoGallery object that correlates to the image path given
     * todo: refactor all of this so it is not necessary to iterate over lists multiple times to render photo gallery
     */
    public static PhotoGallery findGalleryByImgPath(List<PhotoGallery> galleries, String path) {
        if (galleries == null || path == null) {
            return null;
        }
        Iterator<PhotoGallery> galleryItr = galleries.iterator();
        while (galleryItr.hasNext()) {
            PhotoGallery gallery = galleryItr.next();
            if (gallery.getImgPath() != null && gallery.getImgPath().equals(path)) {
                return gallery;
            }
        }
        return null;
    }
}
