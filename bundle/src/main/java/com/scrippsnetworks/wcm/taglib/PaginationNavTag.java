package com.scrippsnetworks.wcm.taglib;

import com.scrippsnetworks.wcm.parsys.Paginator;
import org.apache.sling.api.request.RequestPathInfo;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * This tag draws the navigation controls for paginated articles and photo galleries.
 * @author Jason Clark
 * Date: 6/19/12
 * modified: 7/3/12 added formatting for photo gallery, refactored a few things
 *
 * todo if we update language level to java 7+, change ugly if/else block in doStartTag() to a switch stmt
 */
public class PaginationNavTag extends TagSupport {

    public static final String WEB_ARTICLE = "web";
    public static final String MOBILE_ARTICLE = "mobile";
    public static final String WEB_PHOTO_GALLERY = "web_photo_gallery";
    public static final String MOBILE_PHOTO_GALLERY = "mobile_photo_gallery";

    private Paginator paginator;
    private String appType;

    public int doStartTag() throws JspException {
        if (paginator == null) {
            return SKIP_BODY;
        }

        JspWriter writer = pageContext.getOut();
        String output;

        Integer totalPages = paginator.getPageCount();
        Integer currentPage = paginator.getPageNum();

        RequestPathInfo pathInfo = paginator.getSlingRequest().getRequestPathInfo();
        String path = pathInfo.getResourcePath().replaceFirst("/jcr:content$", "");
        String suffix = pathInfo.getSuffix() != null ? pathInfo.getSuffix() : "";

        if (appType == null || appType.equalsIgnoreCase(WEB_ARTICLE)) {
            output = formatWebArticleNav(currentPage, totalPages, path, suffix);
        } else if (appType.equalsIgnoreCase(MOBILE_ARTICLE)) {
            output = formatMobileArticleNav(currentPage, totalPages, path, suffix);
        } else if (appType.equalsIgnoreCase(WEB_PHOTO_GALLERY)) {
            output = formatWebPhotoGalleryNav(currentPage, totalPages, path);
        } else {
            output = "";
        }

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

    /**
     * Format the output for Web article page navigation
     * @param currentPage Integer for current page number
     * @param totalPages Integer of total pages in parsys
     * @param path String of path up to the resource name, before selectors, extension & suffix
     * @param suffix String of path suffix, if any
     * @return String formatted output
     */
    private String formatWebArticleNav(final Integer currentPage, final Integer totalPages,
                                       final String path, final String suffix) {
        StringBuilder output = new StringBuilder();
        output.append("<div class=\"pagi clrfix\">");

        //format the "previous" button
        if (currentPage == 1) {
            output.append("<span class=\"nextprev prev\">");
        } else {
            Integer prevNum = currentPage - 1;
            output.append("<a href=\"");
            output.append(path + ".page-" + prevNum + ".html" + suffix);
            output.append("\">");
        }
        output.append("&laquo; Previous");
        if (currentPage == 1) {
            output.append("</span>");
        } else {
            output.append("</a>");
        }

        int previousPage = 1;
        for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
            if (pageNum <= 2 ||
                 pageNum == currentPage - 2 ||
                 pageNum == currentPage - 1 ||
                 pageNum == currentPage ||
                 pageNum == currentPage + 1 ||
                 pageNum == currentPage + 2 ||
                 pageNum >= totalPages - 1) {

                //ellipsis for gaps
                if (pageNum > previousPage + 1) {
                    output.append("<span>&hellip;</span>");
                }

                previousPage = pageNum;

                if (pageNum == currentPage) {
                    output.append("<span class=\"current\">");
                } else {
                    output.append("<a href=\"");
                    output.append(path + ".page-" + pageNum + ".html" + suffix);
                    output.append("\">");
                }
                output.append(pageNum);
                if (pageNum == currentPage) {
                    output.append("</span>");
                } else {
                    output.append("</a>");
                }
            }
        }

        if (currentPage.equals(totalPages)) {
            output.append("<span class=\"nextprev prev\">");
        } else {
            Integer nextNum = currentPage + 1;
            output.append("<a href=\"");
            output.append(path + ".page-" + nextNum + ".html" + suffix);
            output.append("\">");
        }
        output.append("Next &raquo;");
        if (currentPage.equals(totalPages)) {
            output.append("</span>");
        } else {
            output.append("</a>");
        }
        output.append("</div>");

        return output.toString();
    }

    /**
     * Format the output for Mobile article page navigation controls
     * @param currentPage Integer of current page number
     * @param totalPages Integer of total number of pages in parsys
     * @param path String of path up to resource name, before selectors, extension & suffix
     * @param suffix String of path suffix, if any
     * @return String formatted output
     */
    private String formatMobileArticleNav(final Integer currentPage, final Integer totalPages,
                                          final String path, final String suffix) {
        StringBuilder output = new StringBuilder();
        Integer nextNum, prevNum;

        output.append("<div class=\"pagi clrfix\">");
       	if (currentPage == 1) {
            output.append("<button class=\"horz prev dis\"> &laquo; Previous </button>");
            nextNum = currentPage + 1;
            if(currentPage.equals(totalPages)){
            	output.append("<button class=\"horz next dis\"> Next Page &raquo; </button>");
            } else {
            	output.append("<a href=\"");
            	output.append(path + ".page-" + nextNum + ".html" + suffix);
            	output.append("\"><button class=\"horz next\"> Next Page &raquo; </button></a>");
            }
        } else if (currentPage.equals(totalPages)) {
        	prevNum = currentPage - 1;
        	output.append("<a href=\"");
        	output.append(path + ".page-" + prevNum + ".html" + suffix);
        	output.append("\"><button class=\"horz prev \"> &laquo; Previous  </button> </a>");
           	output.append("<button class=\"horz next dis\"> Next Page &raquo; </button>");
        } else {
        	prevNum = currentPage - 1;
        	output.append("<a href=\"");
        	output.append(path + ".page-" + prevNum + ".html" + suffix);
        	output.append("\"><button class=\"horz prev\"> &laquo; Previous  </button> </a>");
        	nextNum = currentPage + 1;
	        output.append("<a href=\"");
	        output.append(path + ".page-" + nextNum + ".html" + suffix);
	        output.append("\"><button class=\"horz next\"> Next Page &raquo; </button></a>");
        }
        output.append("</div>");
        return output.toString();
    }

    /**
     * Format the output for Web photo gallery page navigation controls
     * Also contains logic to update mdManager page number as page loads and as user clicks through slides
     * @param currentPage Integer of current page in pageset
     * @param totalPages Integer of total pages in pageset
     * @param path String path to resource, before selectors, extension & suffix
     * @return String formatted output
     */
    private String formatWebPhotoGalleryNav(final Integer currentPage, final Integer totalPages,
                                            final String path) {

        //to strip off any part of the path beyond the containing page
        String galleryPath = path.replaceFirst("/jcr:content/.*", "");

        StringBuilder output = new StringBuilder();

        output
            .append("<script>mdManager.setParameter(\"PageNumber\", ")
            .append(currentPage)
            .append(");</script>")
            .append("<div class=\"pg-photo-count\">")
            .append("<p>PHOTO <span class=\"count\">")
            .append(currentPage)
            .append("</span> of <span class=\"total\">")
            .append(totalPages + 1)
            .append("</span></p></div><a class=\"pg-thumbnails-button\"><span>VIEW THUMBNAILS</span></a>");

        Integer prevNum = currentPage - 1;
        Integer nextNum = currentPage + 1;

        if (currentPage == 1) {
            //on the first page
            output
                .append("<a onclick='updateMetaDataPageNumber(-1, ")
                .append(totalPages)
                .append(")' ")
                .append("href=\"#\" class=\"pg-previous pg-disabled\"><span>PREVIOUS</span></a>")
                .append("<a onclick='s_objectID=\"")
                .append(galleryPath)
                .append(".page-2.html\"; updateMetaDataPageNumber(1, ")
                .append(totalPages)
                .append(");return this.s_oc?this.s_oc(e):true' href=\"")
                .append(galleryPath)
                .append(".page-2.html\" class=\"pg-next\"><span>NEXT PHOTO</span></a>");
        } else if (currentPage.equals(totalPages)) {
            //on the last page
            output
                .append("<a onclick='s_objectID=\"")
                .append(galleryPath)
                .append(".html\"; updateMetaDataPageNumber(-1, ")
                .append(totalPages)
                .append(");return this.s_oc?this.s_oc(e):true' href=\"")
                .append(galleryPath)
                .append(".page-")
                .append(prevNum)
                .append(".html\" class=\"pg-previous\"><span>PREVIOUS</span></a>")
                .append("<a onclick='s_objectID=\"")
                .append(galleryPath)
                .append(".html\"; updateMetaDataPageNumber(1, ")
                .append(totalPages)
                .append(");return this.s_oc?this.s_oc(e):true' href=\"")
                .append(galleryPath)
                .append(".html\" class=\"pg-next\"><span>NEXT PHOTO</span></a>");
        } else {
            //somewhere in the middle
            output
                .append("<a onclick='s_objectID=\"")
                .append(galleryPath)
                .append(".html\"; updateMetaDataPageNumber(-1, ")
                .append(totalPages)
                .append("); return this.s_oc?this.s_oc(e):true' href=\"")
                .append(galleryPath)
                .append(".page-")
                .append(prevNum)
                .append(".html\" class=\"pg-previous\"><span>PREVIOUS</span></a>")
                .append("<a onclick='s_objectID=\"")
                .append(galleryPath)
                .append(".html\"; updateMetaDataPageNumber(1, ")
                .append(totalPages)
                .append(");return this.s_oc?this.s_oc(e):true' href=\"")
                .append(galleryPath)
                .append(".page-")
                .append(nextNum)
                .append(".html\" class=\"pg-next\"><span>NEXT PHOTO</span></a>");
        }

        return output.toString();
    }

    public void setPaginator(Paginator p) {
        paginator = p;
    }

    public void setApptype(String type) {
        appType = type;
    }
}
