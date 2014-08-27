package com.scrippsnetworks.wcm.taglib.photogallery;

import com.scrippsnetworks.wcm.asset.photogallery.PhotoGallery;
import com.scrippsnetworks.wcm.asset.photogallery.PhotoGalleryUtil;
import com.scrippsnetworks.wcm.url.UrlMapper;
import com.scrippsnetworks.wcm.taglib.TagUtils;
import org.apache.commons.lang.StringEscapeUtils;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.*;
import org.apache.sling.api.scripting.SlingScriptHelper;

/**
 * Tag to encapsulate logic that generates JSON block used in Photo Galleries.
 * Uses Paginator object from the current Photo Gallery page to iterate over the photo gallery contents.
 * @author Jason Clark
 * Date: 7/7/12
 */
public class PhotoGalleryJsonTag extends TagSupport {

    private List<PhotoGallery> galleryList;
    private Integer currentIndex;

    public int doStartTag() throws JspException {
        if (galleryList == null || currentIndex == null) {
            return SKIP_BODY;
        }
        JspWriter writer = pageContext.getOut();
        StringBuilder output = new StringBuilder();

        SlingScriptHelper sling = (SlingScriptHelper)pageContext.getAttribute("sling");
        UrlMapper urlMapper = null;
        if (sling != null) {
            urlMapper = sling.getService(UrlMapper.class);
        }

        output.append("<script type=\"text/javascript\">\n");

        output.append("var dhpg = new SNI.CC.PhotoGallery.init(jQuery(\"#6039018-pg\"), {");
        output.append("current_index: ");
        output.append(currentIndex);
        output.append(",\n");
        output.append("hotspotURL: \"6039018/6039019\",");
        output.append("images: [");

        Iterator<PhotoGallery> galleryIterator = galleryList.iterator();

        while (galleryIterator.hasNext()) {
            PhotoGallery gallery = galleryIterator.next();

            // COOKING-4578 attached asset URL needs to be mapped. Since the output is written as JSON,
            // the path will have to be mapped here;
            String rurl = TagUtils.completeHREF(gallery.getAttachedAssetPath());
            if (urlMapper != null && rurl != null && rurl.length() > 0) {
                rurl = urlMapper.map(rurl);
            }

            //orientation of image for lightbox thingy
            Integer bvertValue;
            if (gallery.getOrientation() != null
                    && gallery.getOrientation().equals(PhotoGalleryUtil.IMAGE_ORIENTATION_VERTICAL)) {
                bvertValue = 1;
            } else {
                bvertValue = 0;
            }

            output.append("{ iid: \"");
            output.append(gallery.getPageNum());
            output.append("\", ");

            output.append("iurl: \"");
            output.append(gallery.getGalleryRenditionPath());
            output.append("\", ");

            output.append("itnurl: \"");
            output.append(gallery.getThumbPath());
            output.append("\", ");

            output.append("ialt: \"");
            output.append(StringEscapeUtils.escapeJavaScript(gallery.getSubhead()));
            output.append("\", ");

            output.append("rtitle: \"");
            output.append(StringEscapeUtils.escapeJavaScript(gallery.getAttachedAssetTitle()));
            output.append("\", ");

            output.append("rtxt: \"");
            output.append(StringEscapeUtils.escapeJavaScript(gallery.getAttachedAssetSlug()));
			output.append("...");  // Appended "..." for the issue COOKING-3421
            output.append("\", ");

            output.append("rurl: \"");
            output.append(rurl);
            output.append("\", ");

            output.append("ititle: \"");
            output.append(StringEscapeUtils.escapeJavaScript(gallery.getSubhead()));
            output.append("\", ");

            output.append("icap: \"");
            output.append(StringEscapeUtils.escapeJavaScript(gallery.getCaption()));
            output.append("\", ");

            output.append("creator: \"\", ");

            output.append("pgUrl: \"");
            output.append(gallery.getPagePath());
            output.append(".page-");
            output.append(gallery.getPageNum());
            output.append(".html");
            output.append("\", ");

            output.append("bvert: ");
            output.append(bvertValue);
            output.append(", ");
            output.append("bhs: 0 }");

            if (galleryIterator.hasNext()) {
                output.append(",\n");
            }
        }

        output.append("]});");

        output.append("</script>");

        try {
            writer.print(output);
        } catch (IOException ioe) {
            throw new JspException(ioe);
        }

        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        return SKIP_BODY;
    }

    public void setGalleryList(List<PhotoGallery> galleryList) {
        this.galleryList = galleryList;
    }

    public void setCurrentIndex(Integer currentIndex) {
        this.currentIndex = currentIndex;
    }

}
