package com.scrippsnetworks.wcm.taglib;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import com.day.cq.commons.Externalizer;
import com.scrippsnetworks.wcm.util.ImageSizes;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.commons.util.PrefixRenditionPicker;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.settings.SlingSettingsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Provides the sni:damImage tag.
 * @author Scott Johnson
 */
public class DamImageTag extends TagSupport {
	private static final long serialVersionUID = 1L;

    private static final String WEB_PREFIX = "cq5dam.web";
    private static final String WCM_PREFIX = "sni.wcm";

	private Resource resource;
    private String path;
	private String renditionPrefix;
	private String imgAlt;
	private String imgClass;
	private String htmlHeight;
	private String htmlWidth;
    private String requestedRenditionWidth;
    private String requestedRenditionHeight;

    protected static Logger logger = LoggerFactory.getLogger(DamImageTag.class);

	/*
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doEndTag() throws JspException {

        logger.debug("doEnd()");
		Rendition rendition = null;
        String imgSrc = null;

        if (resource == null && path != null) {
            ResourceResolver resourceResolver = (ResourceResolver)pageContext.getAttribute("resourceResolver");
           
            
            if (resourceResolver != null) {
                resource = resourceResolver.getResource(path);
            } else {
                return EVAL_PAGE;
            }
        }

        if (resource == null) {
            return EVAL_PAGE;
        }

		try {
			if (renditionPrefix != null) {
                rendition = getRendition(renditionPrefix);
			}
		} catch (Exception e) {
			throw new JspException("Exception retrieving rendition: " + e.getMessage(), e);
		}

		if (rendition == null) {
            //try to get a vertical rendition of the image
            try {
                for (ImageSizes size : ImageSizes.values()) {
                    if (size.width() == Integer.parseInt(requestedRenditionWidth)
                            && size.height() == Integer.parseInt(requestedRenditionHeight)) {
                        String verticalSizeName = size.name().replace("HORIZONTAL", "VERTICAL");
                        ImageSizes verticalSize = ImageSizes.valueOf(verticalSizeName);
                        String verticalRenditionPrefix = WEB_PREFIX + "." + verticalSize.width() + "." + verticalSize.height();
                        rendition = getRendition(verticalRenditionPrefix);
                        break;
                    }
                }
            } catch (NullPointerException npe) {
                return SKIP_BODY;
            } catch (NumberFormatException nfe) {
                //return SKIP_BODY;
            }
		}

        if (rendition != null) {
            imgSrc = rendition.getPath();
        } else {
            imgSrc = resource.getPath();
        }
  
        Externalizer externalizer = resource.getResourceResolver().adaptTo(Externalizer.class);
     
        String externalizedURL = externalizer.externalLink(null, "sndimg", imgSrc);
        try{
        logger.error(" Received extenrlizer link "+externalizedURL);
        
        /*Below code snippet added to make sure images should work irrespective of squad environment.  --START */
        if(externalizedURL.contains("localhost")){  // this means no specific runmode is pickedup . 
    	String squadEnvironmentUrl=pageContext.getRequest().getServerName()+":"+pageContext.getRequest().getServerPort();
       // String relativeUrl=externalizer.relativeLink((SlingHttpServletRequest)pageContext.getRequest(), imgSrc);
      	URL url = new URL(externalizedURL);
      	String domain = url.getHost()+":"+url.getPort();
       externalizedURL=externalizedURL.replaceAll(domain,squadEnvironmentUrl);
      //  externalizedURL=relativeUrl;
       logger.error("  extenrlizer link changed to  "+externalizedURL);
        }
      
        			  
    
         /*Below code snippet added to make sure images should work irrespective of squad environment.  --END */
   
			JspWriter out = pageContext.getOut();

            StringBuilder sb = new StringBuilder();
            sb.append("<img src=\"").append(externalizedURL).append("\"");

            if (imgAlt != null && imgAlt.length() > 0) {
                sb.append(" alt=\"").append(StringEscapeUtils.escapeHtml(imgAlt)).append("\"");
            }

            if (imgClass != null && imgClass.length() > 0) {
                sb.append(" class=\"").append(StringEscapeUtils.escapeHtml(imgClass)).append("\"");
            }

            if (htmlHeight != null && htmlHeight.length() > 0) {
                sb.append(" height=\"").append(StringEscapeUtils.escapeHtml(htmlHeight)).append("\"");
            }

            if (htmlWidth != null && htmlWidth.length() > 0) {
                sb.append(" width=\"").append(StringEscapeUtils.escapeHtml(htmlWidth)).append("\"");
            }

            sb.append(" />");
            logger.debug("printed image tag {}", sb.toString());

            out.print(sb.toString());

		} catch (IOException ioe) {
			throw new JspException("Caught IOException writing image tag", ioe);
		}
		return EVAL_PAGE;
	}

    /**
     * routine to retrieve a rendition given a rendition prefix
     * @param renditionPrefix String rendition prefix
     * @return Rendition or null if something went wrong
     */
    private Rendition getRendition(final String renditionPrefix) {
        try {
            Asset currentAsset = resource.adaptTo(Asset.class);
            if (currentAsset != null) {
                return currentAsset.getRendition(new PrefixRenditionPicker(renditionPrefix));
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public void setRenditionPrefix(String pRenditionPrefix) {
   		if (pRenditionPrefix != null) {
   			renditionPrefix = pRenditionPrefix.replace(WCM_PREFIX, WEB_PREFIX);
               String[] renditionElements = renditionPrefix.split("[.]");
               if (renditionElements.length == 4) {
                   requestedRenditionWidth = renditionElements[2];
                   requestedRenditionHeight = renditionElements[3];
               }
   		} else {
   			renditionPrefix = null;
   		}
   	}

    public void setPath(String pPath) {
        path = pPath;
    }

	public void setResource(Resource pResource) {
		resource = pResource;
	}

	public void setImgAlt(String pImgAlt) {
		imgAlt = pImgAlt;
	}

	public void setImgClass(String pImgClass) {
		imgClass = pImgClass;
	}

	public void setHtmlHeight(String pHtmlHeight) {
		htmlHeight = pHtmlHeight;
	}

	public void setHtmlWidth(String pHtmlWidth) {
		htmlWidth = pHtmlWidth;
	}

}
