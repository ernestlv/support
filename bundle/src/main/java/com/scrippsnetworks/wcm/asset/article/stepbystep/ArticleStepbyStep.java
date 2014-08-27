package com.scrippsnetworks.wcm.asset.article.stepbystep;

import org.apache.sling.api.resource.Resource;
import org.jsoup.helper.Validate;
import java.util.*;

/**
 * Data structure that represents the interesting info from a Article Step by Step.
 * @author Sreeni Dumpa
 * Date: 7/19/12
 */
@Deprecated
public class ArticleStepbyStep {

    private String imgPath;
    private String thumbPath;    
    private String caption;       
    private String articleText;
    private String imageRenditionPath;

	/**
     * In case, for some reason, you want to populate these values manually...
     */
    public ArticleStepbyStep() {}

    /**
     * Constructor for ArticleStepbyStep object, takes a Sling Resource for a article-step-by-step component
     * and populates fields based on that Resource.
     * @param resource Sling Resource, must resolve to a article-step-by-step component or will throw an exception.
     */
    public ArticleStepbyStep(Resource resource) {
        Validate.notNull(resource);

        if (!ArticleStepbyStepUitl.isArticleStepbyStepImageComponent(resource)) {
            throw new InputMismatchException("Resource must be a Article Step by Step Image component.");
        }

        Resource imageResource = ArticleStepbyStepUitl.imageFromArticleStepbyStep(resource);
        imgPath = ArticleStepbyStepUitl.pathFromImage(imageResource);
        articleText = ArticleStepbyStepUitl.textFromArticleStepbyStep(imageResource);
        thumbPath = ArticleStepbyStepUitl.thumbPathFromImage(imageResource);
        imageRenditionPath = ArticleStepbyStepUitl.renditionPathFromImage(imageResource);      
        caption = ArticleStepbyStepUitl.captionFromArticleStepbyStep(resource);
       
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }
    public String getImgPath() {
        return imgPath;
    }
    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }
    public String getThumbPath() {
        return thumbPath;
    }
   
    public void setCaption(String caption) {
        this.caption = caption;
    }
    public String getCaption() {
        return caption;
    }
   
   
   
    public String getArticleText() {
		return articleText;
	}

	public void setArticleText(String articleText) {
		this.articleText = articleText;
	}
	
	public String getImageRenditionPath() {
		return imageRenditionPath;
	}

	public void setImageRenditionPath(String imageRenditionPath) {
		this.imageRenditionPath = imageRenditionPath;
	}

}
