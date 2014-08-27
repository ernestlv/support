package com.scrippsnetworks.wcm.taglib.articlestepbystep;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import com.scrippsnetworks.wcm.asset.article.stepbystep.ArticleStepbyStep;


/**
 * Writes out markup for article thumbnail , for use within article step by step pages
 * Uses 92x69 rendition of the image to create article step by step images
 * @author Sreeni Dumpa
 * Date: 7/20/12
 */
public class ArticleStepbyStepThumbnailTag extends TagSupport {

    private List<ArticleStepbyStep> articleStepbyStepImages;
    private Integer currentPageNum;

    public int doStartTag() throws JspException {

        if (articleStepbyStepImages == null || currentPageNum == null) {
            return SKIP_BODY;
        }

        JspWriter out = pageContext.getOut();
        
        //Thumbnail mark up for multiple images
        StringBuilder thumbNail = new StringBuilder();
        
        //ImageTaggler mark up for multiple images
        StringBuilder imageTaggler = new StringBuilder();
        
       // mark up for Single image
        StringBuilder singleImage = new StringBuilder();

        //List<ArticleStepbyStep> articleImages = (List<ArticleStepbyStep>) request.getAttribute("articleImages");

        //out.println("imageCount.."+imageCount);
        Iterator<ArticleStepbyStep> articleIterator = articleStepbyStepImages.iterator();
        
        //int pageNumber = (Integer) request.getAttribute("pageNum");
        //int pageNum = (Integer)request.getAttribute("pageNum");
        
        int imageCount = articleStepbyStepImages.size();
        
        //out.println("imageCount.."+imageCount);
        //Iterator<ArticleStepbyStep> articleIterator = articleImages.iterator();
        int loopCounter = 0;
       
        imageTaggler.append("<ul class=\"tab-cont\">");
        thumbNail.append("<div class=\"image-tabs ui-tabs ui-widget ui-widget-content ui-corner-all\" id=\"image-tabs-1\">");
        thumbNail.append("<div class=\"image-tabs ui-tabs ui-widget ui-widget-content ui-corner-all\" id=\"image-tabs-1\">");
        thumbNail.append("<ul class=\"tab-nav clrfix ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all\">");
        
         while (articleIterator.hasNext()) {
        	 loopCounter++;
             ArticleStepbyStep article = articleIterator.next();           
             String href =  "href=\"#image-tabs-1-image-"+loopCounter+"\"";          
             String liClass = loopCounter == 1 ? " class=\"imgwrap ui-state-default ui-corner-top ui-tabs-selected ui-state-active\"" : " class=\"imgwrap ui-state-default ui-corner-top\"";
             thumbNail.append("<li");
             thumbNail.append(liClass);
             thumbNail.append(">");
             thumbNail.append("<a ");
             thumbNail.append(href);
             //String onClick = "onclick='s_objectID=\""+hostName+""+pagePath+".page-"+pageNumber+".html#image-tabs-1-image-"+loopCounter+"_1&quot;;return this.s_oc?this.s_oc(e):true' ";
             //thumbNail.append(onClick);
             thumbNail.append(" alt=\"");
             thumbNail.append("\"><img src=\"");
             thumbNail.append(article.getThumbPath());
             thumbNail.append("\"></a>");
             thumbNail.append("</li>"); 
             if(loopCounter ==1)
                 imageTaggler.append("<li class=\"imgwrap ui-tabs-panel ui-widget-content ui-corner-bottom\"");
                 else{
                 imageTaggler.append("<li class=\"imgwrap ui-tabs-panel ui-widget-content ui-corner-bottom ui-tabs-hide\" ");
                 //imageTaggler.append("style=\"width: 616px;\"");
                 }
             String id = " id =\"image-tabs-1-image-"+loopCounter+"\">";
             imageTaggler.append(id);
             imageTaggler.append("<img src=\"");
             imageTaggler.append(article.getThumbPath());
             //imageTaggler.append("\" hegiht=\"92\" width=\"69\">");
             imageTaggler.append("\">");
             imageTaggler.append("<div class=\"caption\">");
             imageTaggler.append(article.getCaption());
             imageTaggler.append("</div><div class=\"pg-toggler\"> ");
             imageTaggler.append("<div class=\"pg-enlarge clrfix\"> ");
             imageTaggler.append("<span class=\"pg-toggler-button\"> ");
             imageTaggler.append("<div>+</div></span><span class=\"pg-toggler-label\">Enlarge Photo</span></div> ");
             imageTaggler.append("<div class=\"pg-shrink clrfix\"><span class=\"pg-toggler-button\"><div>&ndash;</div></span> ");
             imageTaggler.append("<span class=\"pg-toggler-label\">Shrink Photo</span> ");
             imageTaggler.append("</div></div> ");
             imageTaggler.append("</li> ");
             if(imageCount == 1){  
             	if(currentPageNum == 1)
             		singleImage.append("<div valign=\"middle\" class=\"beauty-img clrfix\">");
             	else
             	    singleImage.append("<div  class=\"stepbystep-img clrfix\">");
             	
             	singleImage.append("<img src=\"");
             	singleImage.append(article.getImageRenditionPath());
             	singleImage.append("\">");
 	            singleImage.append("<div class=\"caption\">");
 	            singleImage.append(article.getCaption());
 	            singleImage.append("</div>");
 	            singleImage.append("</div>");
             }
             
         }	 // End While loop
         thumbNail.append("</ui>");
         imageTaggler.append("</ui>");
         thumbNail.append("</div>");
         imageTaggler.append("</div>");
        
         try{
             if(imageCount > 1){
             out.println(thumbNail);
             out.println(imageTaggler);
             }
             else
           	  out.println(singleImage);   
             } catch (IOException ioe) {
                 throw new JspException(ioe.getMessage());
             }
        
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        return SKIP_BODY;
    }

    public void setArticleStepbyStepImages(List<ArticleStepbyStep> articleStepbyStepImages) {
        this.articleStepbyStepImages = articleStepbyStepImages;
    }

    public void setCurrentPageNum(Integer currentPageNum) {
        this.currentPageNum = currentPageNum;
    }
}
