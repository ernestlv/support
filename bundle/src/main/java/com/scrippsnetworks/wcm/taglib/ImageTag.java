package com.scrippsnetworks.wcm.taglib;

import java.io.IOException;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.ServletRequest;

import javax.jcr.Node;

import com.day.cq.commons.Externalizer;
import com.day.cq.commons.ImageResource;
import org.apache.sling.api.resource.Resource;
import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.foundation.Image;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.commons.util.PrefixRenditionPicker;
import com.day.cq.wcm.api.WCMMode;
import org.apache.commons.lang.StringEscapeUtils;
import com.day.cq.commons.Doctype;
import com.day.cq.wcm.api.designer.Style;
import com.day.cq.wcm.api.components.DropTarget;

import com.scrippsnetworks.wcm.config.impl.SiteConfigServiceImpl;
import com.scrippsnetworks.wcm.util.ImageSizes;

/*
 * Provides the sni:image tag that encapsulates image logic so that a single custom tag can be used, while
 * reusing as much of the existing cq image logic as possible.
 * @author Scott Johnson
 */
public class ImageTag extends TagSupport {
	private static final long serialVersionUID = 1L;

    private static final String WIDTH_METADATA_PROPERTY = "exif:PixelYDimension";
    private static final String HEIGHT_METADATA_PROPERTY = "exif:PixelYDimension";
    private static final String WEB_PREFIX = "cq5dam.web";
    private static final String WCM_PREFIX = "sni.wcm";

	private Resource resource;
	private String renditionPrefix;
	private String selector;
	private Boolean drawAnchor = true;
	private String linkClass;
	private String linkTitle;
	private String imgAlt;
	private String imgClass;
	private String relativeResourceName;
	private String htmlHeight;
	private String htmlWidth;
	private String imagTitle;
    private String imgDomain;

    private String requestedRenditionHeight = null;
    private String requestedRenditionWidth = null;
    private String metadataHeight = null;
    private String metadataWidthh = null;

	/*
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {
		ServletRequest request = pageContext.getRequest();
		Rendition rendition = null;

        boolean requireRendition = (renditionPrefix != null);

		if (resource == null) {
			return SKIP_BODY;
		}

        if (imgDomain == null) {
            SiteConfigServiceImpl scService = new SiteConfigServiceImpl();
            if (scService != null) {
                this.imgDomain = scService.getImgDomain();
            }
        }

        Image image;
		if (relativeResourceName != null && relativeResourceName.length() > 0) {
			image = new Image(resource, relativeResourceName);
		} else {
			image = new Image(resource);
		}

		try {
			if (renditionPrefix != null) {
                rendition = getRendition(image, renditionPrefix);
			}
		} catch (Exception e) {
			throw new JspException("Error: " + e.getMessage());
		}

		// rather than nesting, just bail if there's nothing to render
		if (!image.hasContent()
				&& !(WCMMode.fromRequest(request) == WCMMode.EDIT)) {
			return SKIP_BODY;
		}

		if (rendition == null && requireRendition) {
            // Whether the image is sized or unsized, if it's size matches use it.
            requireRendition = !originalIsRequestedSize();
        }

		if (rendition == null && requireRendition) {
            //attempt to find vertically-oriented rendition
            if (requestedRenditionHeight == null
                    || requestedRenditionWidth == null) {
                return SKIP_BODY;
            }
            try {
                for (ImageSizes size : ImageSizes.values()) {
                    if (size.width() == Integer.parseInt(requestedRenditionWidth)
                            && size.height() == Integer.parseInt(requestedRenditionHeight)) {
                        String verticalSizeName = size.name().replace("HORIZONTAL", "VERTICAL");
                        ImageSizes verticalSize = ImageSizes.valueOf(verticalSizeName);
                        String verticalRenditionPrefix = WEB_PREFIX + "." + verticalSize.width() + "." + verticalSize.height();
                        rendition = getRendition(image, verticalRenditionPrefix);
                        break;
                    }
                }
            } catch (NullPointerException npe) {
                return SKIP_BODY;
            } catch (NumberFormatException nfe) {
                return SKIP_BODY;
            }
            if (rendition == null) {
                if (WCMMode.fromRequest(request) == WCMMode.EDIT) {
                    writeEmptyImageTag();
                }
                return SKIP_BODY;
            }
        }

		image.setDoctype(Doctype.fromRequest(request));
		// I'm not 100% sure about this drop target class, but I believe it's
		// going to be right in all cases.
		image.addCssClass(DropTarget.CSS_CLASS_PREFIX + resource.getName());
		Style currentStyle = (Style) pageContext.getAttribute("currentStyle");
		if (currentStyle != null) {
			image.loadStyleData(currentStyle);
		}

		if (imgAlt != null && imgAlt.length() > 0) {
			image.setAlt(imgAlt);
		}
		if (imagTitle != null && imagTitle.length() > 0) {
			image.setTitle(imagTitle);
		}
		if (imgClass != null && imgClass.length() > 0) {
			image.addCssClass(imgClass);
		}

		if (rendition != null) {
			// use dam rendition
			image.setSrc(rendition.getPath());
		} else {
			if (selector != null) {
				image.setSelector(selector);
			}
		}


		// If linkURL is set at all draw() will use an anchor tag. We'll unset
		// here if
		// the link isn't desired (perhaps because the jsp wants to draw it.
		if (!drawAnchor) {
			image.set(Image.PN_LINK_URL, "");
		}

        try {
			JspWriter out = pageContext.getOut();
			String linkURL = image.get(Image.PN_LINK_URL);
			boolean hasLinkURL = linkURL.length() > 0;
			boolean hasLinkClass = (linkClass != null && linkClass.length() > 0);
			boolean hasLinkTitle = (linkTitle != null && linkTitle.length() > 0);
			boolean hasHtmlHeight = (htmlHeight != null && htmlHeight.length() > 0);
			boolean hasHtmlWidth = (htmlWidth != null && htmlWidth.length() > 0);


			if (hasHtmlHeight) {
				image.set(Image.PN_HTML_HEIGHT, htmlHeight);
			}

			if (hasHtmlWidth) {
				image.set(Image.PN_HTML_WIDTH, htmlWidth);
			}

			// CQ image code won't
			if (drawAnchor && hasLinkURL && !hasLinkClass && !hasLinkTitle) {
                out.print(getImageTag(resource, image));
			} else {
				// unset linkURL so we can use draw() for the image itself
				image.set(Image.PN_LINK_URL, "");
				StringBuilder sb = new StringBuilder();
				if (drawAnchor && hasLinkURL) {
                    sb.append("<a href=\"");
                    sb.append(TagUtils.completeHREF(linkURL));
					sb.append("\"");
					if (hasLinkClass) {
						sb.append(" class=\"");
						sb.append(StringEscapeUtils.escapeHtml(linkClass));
						sb.append("\" ");
					}
					if (hasLinkTitle) {
						sb.append(" title=\"");
						sb.append(StringEscapeUtils.escapeHtml(linkTitle));
						sb.append("\" ");
					}
					sb.append(">");
				}
				out.print(sb.toString());
                out.print(getImageTag(resource,image));

				if (drawAnchor & hasLinkURL) {
					out.print("</a>");
				}
			}
		} catch (IOException ioe) {
			throw new JspException("Error: " + ioe.getMessage());
		}
		return SKIP_BODY;
	}

    private String getImageTag(Resource resource, Image img ) {
        StringBuilder imageTag = new StringBuilder();

        Externalizer externalizer = resource.getResourceResolver().adaptTo(Externalizer.class);
       

        if (img.getSrc() != null && img.getSrc().length() > 0) {
        	
        	String externalizedURL = externalizer.externalLink(null, "sndimg", img.getSrc());
        	 
            if (externalizedURL != null && externalizedURL.length() > 0) {
                imageTag.append("<img src=\""+ externalizedURL +"\" ");
            } else {
                imageTag.append("<img src=\"");
                imageTag.append(img.getSrc() +"\" ");
            }

            if (img.getAlt() != null && img.getAlt().length() > 0) {
                imageTag.append("alt=\""+ img.getAlt() +"\" ");
            }

            if (img.get(ImageResource.PN_HTML_HEIGHT) != null && img.get(ImageResource.PN_HTML_HEIGHT).length() > 0) {
                imageTag.append("height=\""+ img.get(ImageResource.PN_HTML_HEIGHT) +"\" ");
            }

            if (img.get(ImageResource.PN_HTML_WIDTH) != null && img.get(ImageResource.PN_HTML_WIDTH).length() > 0) {
                imageTag.append("width=\""+ img.get(ImageResource.PN_HTML_WIDTH) +"\" ");
            }

            if (img.getTitle() != null && img.getTitle().length() > 0) {
                imageTag.append("title=\""+ img.getTitle().trim() +"\" ");
            }

            if (imgClass != null && imgClass.length() > 0) {
                imageTag.append("class=\""+ imgClass +"\" ");
            }

            imageTag.append("/>");
        } 

        return imageTag.toString();
    }

    public void writeEmptyImageTag() throws JspException {
        StringBuilder imageTag = new StringBuilder();
        StringBuilder sizeCss = new StringBuilder();

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
            imageTag.append("<img style=\"display: block; background-color: #c0c0c0; color: #b10000; text-align: center;");
            if (sizeCss.length() > 0) {
                imageTag.append(sizeCss);
            }
            imageTag.append("\"");

            if (imgClass != null && imgClass.length() > 0) {
                imageTag.append(" class=\"")
                    .append(imgClass)
                    .append("\"");
            }

            imageTag.append("src=\"\" alt=\"rendition ")
                .append(renditionPrefix.replace("cq5dam.web.",""))
                .append(" not available\"");
            imageTag.append("/>");
            out.print(imageTag);
        } catch (IOException e) {
			throw new JspException("Error: " + e.getMessage(), e);
		}
    }

	public int doEndTag() throws JspException {
		return EVAL_PAGE;
	}

    /**
     * Retrieve an image rendition from an Image given a rendition prefix
     * @param image Image from which to retrieve a rendition
     * @param prefix String prefix used to retrieve rendition
     * @return Rendition
     */
    private Rendition getRendition(final Image image, final String prefix) {
        if (image == null || prefix == null) {
            return null;
        }
        Rendition rendition = null;
        try {
            Node thisNode = image.getResource().adaptTo(Node.class);
            if (thisNode != null && thisNode.hasProperty("fileReference")) {
                String fileRef = thisNode.getProperty("fileReference")
                        .getString();
                Resource currentResource = resource.getResourceResolver()
                        .getResource(fileRef);

                if (currentResource != null) {
                    Asset currentAsset = currentResource
                            .adaptTo(Asset.class);
                    if (currentAsset != null) {
                        if (metadataHeight == null || metadataWidthh == null) {
                            metadataHeight = currentAsset.getMetadataValue(HEIGHT_METADATA_PROPERTY);
                            metadataWidthh = currentAsset.getMetadataValue(WIDTH_METADATA_PROPERTY);
                        }
                        rendition = currentAsset
                                .getRendition(new PrefixRenditionPicker(
                                        prefix));
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return rendition;
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

	public void setResource(Resource pResource) {
		resource = pResource;
	}

	public void setRelativeResourceName(String pRelativeResourceName) {
		relativeResourceName = pRelativeResourceName;
	}

	public void setSelector(String pSelector) {
		selector = null;
	}

	public void setDrawAnchor(Boolean pDrawAnchor) {
		drawAnchor = pDrawAnchor;
	}

	public void setLinkClass(String pLinkClass) {
		linkClass = pLinkClass;
	}

	public void setLinkTitle(String pLinkTitle) {
		linkTitle = pLinkTitle;
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

	public String getImgTitle() {
		return imagTitle;
	}

	public void setImgTitle(String title) {
		this.imagTitle = title;
	}

    public void setImgDomain(String pDomain) {
        this.imgDomain = pDomain;
    }

    public String qetImgDomain() {
        return this.imgDomain;
    }

    public boolean originalIsRequestedSize() {
        return requestedRenditionHeight != null
            && requestedRenditionWidth != null
            && metadataHeight != null
            && metadataWidthh != null
            && metadataHeight.equals(requestedRenditionHeight)
            && metadataWidthh.equals(requestedRenditionWidth);
    }

}
