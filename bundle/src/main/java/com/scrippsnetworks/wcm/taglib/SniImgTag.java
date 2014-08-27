package com.scrippsnetworks.wcm.taglib;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.scripting.jsp.util.TagUtil;
import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.WCMMode;

import com.scrippsnetworks.wcm.util.StringUtil;
import com.scrippsnetworks.wcm.image.ImageDimensions;
import com.scrippsnetworks.wcm.image.ImageAspect;
import com.scrippsnetworks.wcm.image.RenditionInfo;
import com.scrippsnetworks.wcm.image.ImageUrlService;
import com.scrippsnetworks.wcm.image.LazyLoadInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Provides the sni:img tag.
 *
 * @author Scott Everett Johnson
 */
public class SniImgTag extends TagSupport {
    private static final long serialVersionUID = 1L;

    private String resourcePath = null;
    private String damPath = null;
    private String rendition = null;
    private String aspect = null;
    private boolean lazy = false;
    private String itemProp = null;
    private String lazyMode = null;
    private String imgAlt = null;
    private String imgTitle = null;
    private String imgClass = null;
    private String htmlHeight = null;
    private String htmlWidth = null;
    private String defaultImage = null;

    private SlingScriptHelper sling;
    private SlingHttpServletRequest request;
    private WCMMode wcmMode;
    private RenditionInfo renditionInfoEnum;
    private ImageAspect imageAspectEnum;
    private ResourceResolver resourceResolver;
    private LazyLoadInfo lazyLoadInfo;

    public static String IMAGE_RESOURCE_FILE_PROPERTY = "fileReference";
    public static String DEFAULT_ALT_METADATA_PROPERTY = "dc:title";

    protected static Logger logger = LoggerFactory.getLogger(SniImgTag.class);

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    public int doEndTag() throws JspException {
        SlingBindings bindings = (SlingBindings)this.pageContext.getRequest().getAttribute(SlingBindings.class.getName());
        sling = bindings.getSling();
        request = TagUtil.getRequest(this.pageContext);
        wcmMode = WCMMode.fromRequest(request);
        resourceResolver = (ResourceResolver) pageContext.getAttribute("resourceResolver");
        renditionInfoEnum = null;
        imageAspectEnum = null;

        if (rendition != null) {
            renditionInfoEnum = RenditionInfo.valueOf(rendition);
        }

        if (aspect != null) {
            imageAspectEnum = ImageAspect.valueOf(aspect);
        }

        if (imageAspectEnum != null && renditionInfoEnum == null) {
            throw new JspException("must provide a rendition when providing an aspect");
        }

        // If given an image component resource, extract the file reference from the image resource, collapsing
        // the damPath and resourcePath cases.
        if (resourcePath != null && !resourcePath.isEmpty()) {
            try {
                Resource resource = request.getResource();
                ResourceResolver rr = resource.getResourceResolver();
                Resource imageCmpnRes = rr.getResource(resource, resourcePath);
                if (imageCmpnRes != null) {
                    ValueMap imageCmpnProps = imageCmpnRes.adaptTo(ValueMap.class);
                    if (imageCmpnProps != null) {
                        damPath = imageCmpnProps.get(IMAGE_RESOURCE_FILE_PROPERTY, String.class);
                    }
                }
            } catch (Exception e) {
                return EVAL_PAGE;
            }
        }

        if (wcmMode == WCMMode.EDIT) {
            if (StringUtils.isEmpty(damPath) && StringUtils.isEmpty(defaultImage)) {
                return writeEmptyImageTag();
            }
        }

        return writeImageTagForDamPath();
    }

    /** Writes the image tag to the output.
     *
     * @throws JspException if the damPath member is null or empty.
     */
    protected int writeImageTagForDamPath() throws JspException {

        if (StringUtils.isEmpty(damPath) && StringUtils.isEmpty(defaultImage)) {
            return EVAL_PAGE;
        }

        if (sling == null) {
            return EVAL_PAGE;
        }

        ImageUrlService imageUrlService = sling.getService(ImageUrlService.class);
        if (imageUrlService == null) {
            logger.warn("image url service not available, cannot externalize image urls");
            return EVAL_PAGE;
        }

        String externalizedURL = null;
        if (StringUtils.isEmpty(damPath) && StringUtils.isNotEmpty(defaultImage)) {
            externalizedURL = imageUrlService.getImageUrl(defaultImage, renditionInfoEnum, imageAspectEnum);
        } else {
            externalizedURL = imageUrlService.getImageUrl(damPath, renditionInfoEnum, imageAspectEnum);
        }
        
        Map<String, String> tagAttributes = getTagAttributes();
        try {
            JspWriter out = pageContext.getOut();
            StringBuilder sb = new StringBuilder();

            if (lazy && lazyMode != null) {
                lazyLoadInfo = LazyLoadInfo.valueOf(lazyMode.toLowerCase());

                if (lazyLoadInfo.getSrcAttribute() != null) {
                    tagAttributes.put(lazyLoadInfo.getSrcAttribute(), externalizedURL);
                }

                if (lazyLoadInfo.getClassName() != null) {
                    tagAttributes.put("class", lazyLoadInfo.getClassName() + 
                        (!tagAttributes.containsKey("class") ? "" : " " + tagAttributes.get("class")));
                }

                sb.append("<" + lazyLoadInfo.getTag());
                for (Map.Entry<String, String> entry : tagAttributes.entrySet()) {
                    sb.append(" ").append(entry.getKey()).append("=\"");
                    sb.append(StringEscapeUtils.escapeHtml(entry.getValue())).append("\"");
                }
                if (lazyLoadInfo.isEmptyElement()) {
                    sb.append(" />");
                } else {
                    sb.append(">");
                    if (lazyLoadInfo.getSrcAttribute() == null) {
                        sb.append(externalizedURL);
                    }
                    sb.append("</" + lazyLoadInfo.getTag() + ">");
                }
            } else {
                String srcAttribute = (lazy == true) ? "data-src" : "src";
                tagAttributes.put(srcAttribute, externalizedURL);
                sb.append("<img");
                for (Map.Entry<String, String> entry : tagAttributes.entrySet()) {
                    sb.append(" ").append(entry.getKey()).append("=\"");
                    sb.append(StringEscapeUtils.escapeHtml(entry.getValue())).append("\"");
                }
                sb.append(" />");
            }
            logger.debug("printed image tag {}", sb.toString());

            out.print(sb.toString());

        } catch (IllegalArgumentException iae) {
            throw new JspException("Caught IllegalArgumentException writing image tag. " +
                "Allowed lazyMode options: " + LazyLoadInfo.getModes(), iae);
        } catch (IOException ioe) {
            throw new JspException("Caught IOException writing image tag", ioe);
        }
        return EVAL_PAGE;
    }

    /** Writes an empty image tag.
     *
     * The image tag has height and width attributes set using values
     * from default aspect of the requested rendition.
     */
    protected int writeEmptyImageTag() throws JspException {
        StringBuilder imageTag = new StringBuilder();
        StringBuilder sizeCss = new StringBuilder();

        if (renditionInfoEnum == null) {
            return EVAL_PAGE;
        }

        if (imageAspectEnum == null) {
            imageAspectEnum = renditionInfoEnum.getDefaultAspect();
            if (imageAspectEnum == null) {
                throw new JspException("got null default image aspect from rendition");
            }
        }

        ImageDimensions imageDimensionsEnum = renditionInfoEnum.getImageDimensions(imageAspectEnum);
        if (imageDimensionsEnum == null) {
            throw new JspException("got null image dimensions from rendition");
        }

        String requestedRenditionHeight = String.valueOf(imageDimensionsEnum.getHeight());
        String requestedRenditionWidth = String.valueOf(imageDimensionsEnum.getWidth());

        if (requestedRenditionHeight != null && requestedRenditionHeight.length() > 0) {
            sizeCss.append(" height: ")
                .append(requestedRenditionHeight)
                .append("px;");
        }

        if (requestedRenditionWidth != null && requestedRenditionWidth.length() > 0) {
            sizeCss.append(" width: ")
                .append(requestedRenditionWidth)
                .append("px;");
        }
        
        try {
            JspWriter out = pageContext.getOut();
            imageTag.append("<img style=\"display:block; background-color:#c0c0c0; color:#b10000; text-align:center;");
            if (sizeCss.length() > 0) {
                imageTag.append(sizeCss);
            }
            imageTag.append("\"");

            if (imgClass != null && imgClass.length() > 0) {
                imageTag.append(" class=\"")
                    .append(imgClass)
                    .append("\"");
            }

            imageTag.append("src=\"\" alt=\"no image selected\"");
            imageTag.append("/>");
            out.print(imageTag);
        } catch (IOException e) {
            throw new JspException("Error: " + e.getMessage(), e);
        }

        return EVAL_PAGE;
    }

    Map<String, String> getTagAttributes() {
        Map<String, String> retVal = new HashMap<String, String>();

        ImageAspect nearestAspect = imageAspectEnum;

        // If you supply either of these, you own both.
        boolean useRenditionDimensions = (htmlHeight == null || htmlHeight.isEmpty()) ||
            (htmlWidth == null || htmlWidth.isEmpty());

        if (damPath != null) {
            if (resourceResolver != null) {
                Resource assetResource = resourceResolver.getResource(damPath);
                if (assetResource != null) {
                    Asset asset = assetResource.adaptTo(Asset.class);
                    if (asset != null) {
                        String caption = asset.getMetadataValue(DEFAULT_ALT_METADATA_PROPERTY);
                        if (caption != null && !caption.isEmpty()) {
                            retVal.put("alt", caption);
                            retVal.put("title", caption);
                        }
                        
                        if (nearestAspect == null && useRenditionDimensions && renditionInfoEnum != null) {
                            String mdWidth = asset.getMetadataValue("tiff:ImageWidth");
                            String mdHeight = asset.getMetadataValue("tiff:ImageLength");
                            if (mdWidth != null && mdHeight != null && !mdWidth.isEmpty() && !mdHeight.isEmpty()) {
                                try {
                                    int width = Integer.parseInt(mdWidth);
                                    int height = Integer.parseInt(mdHeight);
                                    nearestAspect = renditionInfoEnum.getNearestAspect(width, height);
                                } catch (NumberFormatException e) {
                                    logger.warn("error converting metadata height and width to integer");
                                }
                            }
                        }
                    } else {
                        logger.debug("could not adapt Resource {} to Asset", damPath);
                    }
                } else {
                    logger.debug("could not acquire image resource {}", damPath);
                }
            } else {
                logger.warn("could not acquire resource resolver");
            }
        }

        ImageDimensions dimensions = null;
        if (useRenditionDimensions && renditionInfoEnum != null && nearestAspect != null) {
            dimensions = renditionInfoEnum.getImageDimensions(nearestAspect);
            if (dimensions != null) {
                int renditionWidth = dimensions.getWidth();
                int renditionHeight = dimensions.getHeight();
                retVal.put("width", String.valueOf(renditionWidth));
                retVal.put("height", String.valueOf(renditionHeight));
            }
        }

        if (imgAlt != null && imgAlt.length() > 0) {
            retVal.put("alt", imgAlt);
            retVal.put("title", imgAlt);
        }

        if (imgTitle != null && imgTitle.length() > 0) {
            retVal.put("title", imgTitle);
        }

        if (imgClass != null && imgClass.length() > 0) {
            retVal.put("class", imgClass);
        }

        if (htmlHeight != null && htmlHeight.length() > 0) {
            retVal.put("height", htmlHeight);
        }

        if (htmlWidth != null && htmlWidth.length() > 0) {
            retVal.put("width", htmlWidth);
        }

        if (StringUtils.isNotBlank(itemProp)) {
            retVal.put("itemprop", itemProp);
        }

        return retVal;
    }

    public void setRendition(String pRendition) {
        rendition = pRendition;
    }

    public void setAspect(String pAspect) {
        aspect = pAspect;
    }

    public void setLazy(boolean pLazy) {
        lazy = pLazy;
    }

    public void setResourcePath(String pResourcePath) {
        resourcePath = pResourcePath;
        damPath = null;
    }

    public void setDamPath(String pDamPath) {
        damPath = pDamPath;
        resourcePath = null;
    }

    public void setImgAlt(String pImgAlt) {
        imgAlt = StringUtil.cleanToPlainText(pImgAlt);
    }

    public void setImgClass(String pImgClass) {
        imgClass = pImgClass;
    }

    public void setImgTitle(String pImgTitle) {
        imgTitle = StringUtil.cleanToPlainText(pImgTitle);
    }

    public void setHtmlHeight(String pHtmlHeight) {
        htmlHeight = pHtmlHeight;
    }

    public void setItemProp(String itemProp) {
        this.itemProp = itemProp;
    }

    public void setHtmlWidth(String pHtmlWidth) {
        htmlWidth = pHtmlWidth;
    }

    public void setLazyMode(String pLazyMode) {
        lazyMode = pLazyMode;
    }
    
    public void setDefaultImage(String pDefaultImage) {
        defaultImage = pDefaultImage;
    }
}
