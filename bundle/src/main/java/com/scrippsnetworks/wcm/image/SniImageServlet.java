package com.scrippsnetworks.wcm.image;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Component;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import javax.jcr.Node;
import java.io.IOException;
import org.apache.commons.lang3.exception.ExceptionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.sling.commons.mime.MimeTypeService;

@Component(name = "com.scrippsnetworks.wcm.image.SniImageServlet", immediate = true, metatype = false, enabled = true )
@Service( value = javax.servlet.Servlet.class )
@Properties({
//define the extensions that are valid for this selector
@Property(name="sling.servlet.extensions",value={"jpg","gif","png","jpeg"}),
@Property(name="sling.servlet.resourceTypes",value="sling/servlet/default"),
@Property(name="sling.servlet.methods",value="GET"),
// in order for this to fire, the first selector passed in must be "rend"
@Property(name="sling.servlet.selectors",value="rend")
})
public class SniImageServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = -5641893704226158128L;
    public static final String NO_IMAGE_SPECIFIED = "No image size specified";
    public static final String NO_RESOURCE_SPECIFIED = "No resource specified";
    public static final String IMAGE_NOT_FOUND = "Image not found";
    public static final String BASE_IMAGE_NOT_DEFINED = "base image not defined";
    public static final String RESOURCE_IS_NOT_ASSET = "could not adapt resource to Asset";
    public static final String RENDITION_NOT_SUPPORTED = "rendition not supported";
    public static final String ASPECT_NOT_SUPPORTED = "aspect not supported";
    public static final String ASPECT_NOT_SUPPORTED_AT_REQUESTED_SIZE = "aspect not supported at size";
    public static final String EXTENSION_MUST_MATCH = "extension must must match type of original";
    public static final String TOO_MANY_SELECTORS = "too many selectors";
    
    @Reference
    MimeTypeService mimeTypeService;

    @Reference
    ResourceResolverFactory resourceResolverFactory;

    private final Logger log = LoggerFactory.getLogger(SniImageServlet.class);

    //entry point for servlet, for now all the logic is in here so you can see  the stream of thought, we'd refactor this into some classes/services and enums
    protected void doGet(SlingHttpServletRequest req, SlingHttpServletResponse res)
            throws IOException {
        try {

            // get the path of the request, we'll use this to figure out what we need to do
            final RequestPathInfo pathInfo = req.getRequestPathInfo();

            // get the selectors that we'll use
            final String[] selectors = pathInfo.getSelectors();

            // ensure the url has the required selectors
            if (selectors.length < 2) {
                //not enough selectors to generate image
                res.sendError(500,NO_IMAGE_SPECIFIED);
                return;
            }

            if (selectors.length > 3) {
                res.sendError(404,TOO_MANY_SELECTORS);
                return;
            }

            // ensure that their is a path to operate against
            if(pathInfo.getResourcePath() == null || pathInfo.getResourcePath().equals("")) {
                res.sendError(500,NO_RESOURCE_SPECIFIED);
                return;
            }

            //pull out the aspect ratio and image size requested in the selectors
            String imageSize = selectors[1];

            String imageAspectRatio = null;
            if (selectors.length == 3) {
                imageAspectRatio = selectors[2];
            }

            // get the resource we need to get the rendition for
            Resource resource = req.getResource();

            // make sure the resource is valid, if not send back 404
            if(resource == null) {
                res.sendError(404,IMAGE_NOT_FOUND);
                return;
            }

            // Make sure the resource is an asset.
            Node node = resource.adaptTo((Node.class));
            if(!node.getProperty("jcr:primaryType").getString().equals("dam:Asset")) {
                res.sendError(404,BASE_IMAGE_NOT_DEFINED);
                return;
            }

            // adapt the base asset resource into an asset object
            final Asset imageAsset = resource.adaptTo(Asset.class);

            if (imageAsset == null) {
                res.sendError(404,RESOURCE_IS_NOT_ASSET);
                return;
            }

            RenditionInfo renditionInfo = safeValueOf(RenditionInfo.class, imageSize);
            if (renditionInfo == null) {
                res.sendError(404,RENDITION_NOT_SUPPORTED);
                return;
            }

            ImageAspect imageAspect = safeValueOf(ImageAspect.class, imageAspectRatio);
            if (imageAspectRatio != null && imageAspect == null) {
                res.sendError(404,ASPECT_NOT_SUPPORTED);
                return;
            }

            if (imageAspect != null && !renditionInfo.hasAspect(imageAspect)) {
                res.sendError(404,ASPECT_NOT_SUPPORTED_AT_REQUESTED_SIZE);
                return;
            }

            // This requires the extension used be the one the mime type service returns for the mime type.
            // Which means we don't accept multiple extensions for JPEG.
            String imageMimeType = imageAsset.getMimeType();
            String imageMimeExtension = mimeTypeService.getExtension(imageMimeType);
            String requestExtension = pathInfo.getExtension();
            if (!imageMimeExtension.equals(requestExtension)) {
                res.sendError(404,EXTENSION_MUST_MATCH);
                return;
            }

            // Beyond this point, let everything be an error. Don't even check if rendition is nonnull, since we're supposed to be generating it.
            GeneratingRenditionPicker renditionPicker = new GeneratingRenditionPicker(renditionInfo, imageAspect,
                    ImageQuality.web, mimeTypeService, resourceResolverFactory);
            Rendition rendition = imageAsset.getRendition(renditionPicker);
            res.getOutputStream().write(IOUtils.toByteArray(rendition.getStream()));

        } catch (Exception ex) {
            //something unexpected happened, log it, and return a 500 for the request
            log.error("Error occurred generating image", ExceptionUtils.getStackTrace(ex));
            res.sendError(500,"Error occurred generating image");
        }

    }

    /**
     * This feels like it should be in a util type class, but placing here for
     * now until discussion can be had about where it truly belongs.
     * 
     * @param enumType
     *            - The enum we would like to safely retrieve a value of
     * @param name
     *            - The string value we are attempting to turn into a enum
     * @return - null if the valueOf would throw an exception, else, value it
     *         would return
     */
    private static <T extends Enum<T>> T safeValueOf(Class<T> enumType, String name) {
        T value = null;
        try {
            value = Enum.valueOf(enumType, name);
        } catch (NullPointerException npe) {
            // Thrown when null is passed as the name
        } catch (IllegalArgumentException iae) {
            // Thrown when the name is not a equivalent value to a enum
            // representation
        }
        return value;
    }
    
}
