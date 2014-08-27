package com.scrippsnetworks.wcm.taglib.photogallery;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import com.scrippsnetworks.wcm.asset.photogallery.PhotoGallery;
import java.io.IOException;
import java.util.*;

/**
 * Writes out markup for thumbnail gallery, for use within photo gallery pages
 * Uses 92x69 rendition of the image to create thumbnail gallery
 * @author Jason Clark
 * Date: 7/5/12
 */
public class ThumbnailGalleryTag extends TagSupport {

    private List<PhotoGallery> galleryList;
    private Integer currentPageNum;

    public int doStartTag() throws JspException {

        if (galleryList == null || currentPageNum == null) {
            return SKIP_BODY;
        }

        JspWriter writer = pageContext.getOut();
        StringBuilder output = new StringBuilder();

        output.append("<ul class=\"pg-thumbnails clrfix\">");

        Iterator<PhotoGallery> galleryIterator = galleryList.iterator();

        //this is to match up the current gallery page with the correct "page" from the Paginator
        //to wrap that photo gallery entry with a "pg-selected-thumbnail" css class
        int currentPageIndex = currentPageNum;
        int i = 0;

        while (galleryIterator.hasNext()) {
            PhotoGallery gallery = galleryIterator.next();

            //set the "selected" css value, if on the "current" page
            String liClass = currentPageIndex == ++i ? " class=\"pg-selected-thumbnail\"" : "";

            output.append("<li");
            output.append(liClass);
            output.append("><a class=\"pg-thumbnail\" alt=\"");
            output.append(gallery.getSubhead());
            output.append("\"><img data-src=\"");
            output.append(gallery.getThumbPath());
            output.append("\" alt=\"");
            output.append(gallery.getSubhead());
            output.append("\" title=\"");
            output.append(gallery.getSubhead());
            output.append("\"></a></li>");
        }
        /* Adding sharethis gallery thumbnail */
        if(i>0){
        output.append("<li>");
        output.append("<a class=\"pg-thumbnail\">");
        output.append("<img src=\"http://web.hgtv.com/webhgtv/hg20/imgs/email-share_sm.jpg\">");
        output.append("</a></li>");
        }
        output.append("</ul>");

        try {
            writer.print(output);
        } catch (IOException ioe) {
            throw new JspException(ioe.getMessage());
        }

        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        return SKIP_BODY;
    }

    public void setGalleryList(List<PhotoGallery> galleryList) {
        this.galleryList = galleryList;
    }

    public void setCurrentPageNum(Integer currentPageNum) {
        this.currentPageNum = currentPageNum;
    }
}
