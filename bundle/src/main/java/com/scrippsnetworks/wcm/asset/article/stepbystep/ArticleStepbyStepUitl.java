package com.scrippsnetworks.wcm.asset.article.stepbystep;

import com.scrippsnetworks.wcm.taglib.Functions;
import org.apache.sling.api.resource.Resource;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 * Utility stuff for article step by step content rendering
 * Also contains any static strings that could be abstracted and used for other purposes
 * @author Sreeni Dumpa
 * Date: 7/19/12
 */
@Deprecated
public class ArticleStepbyStepUitl {

    /* STATIC STRINGS */
    public final static String ARTICLE_STEP_BY_STEP_RESOURCE_TYPE = "sni-wcm/components/pagetypes/article-step-by-step/components/article-step-by-step-image";
    public final static String IMAGE_RESOURCE_NAME = "sni:articleImage";    
    public final static String CAPTION_PROPERTY_NAME = "imageCaption";
    public final static String FILE_REFERENCE_PARAMETER_NAME = "fileReference";
    public final static String ARTICLE_BODY_REFERENCE_PARAMETER_NAME = "sni:articleBody";
    public final static String RESOURCE_TYPE_PROPERTY_NAME = "sling:resourceType";
    
    //the following are relative to the path of the main "fullset" image node
    public final static String THUMBNAIL_RENDITION_PATH = "/jcr:content/renditions/cq5dam.thumbnail.92.69.png";
    public final static String IMAGE_RENDITION_PATH = "/jcr:content/renditions/cq5dam.thumbnail.616.462.png";
    
    /**
     * don't instantiate me!
     */
    private ArticleStepbyStepUitl() {}

    /**
     * Checks if a given Resource is a article step by step image component
     * @param resource Sling Resource that you wish to check
     * @return boolean true if resource is a article step by step image component, otherwise false
     */
    public static boolean isArticleStepbyStepImageComponent(final Resource resource) {
        if (resource == null) {
            return false;
        }
        Node componentNode = resource.adaptTo(Node.class);
        try {
        if (componentNode.hasProperty(RESOURCE_TYPE_PROPERTY_NAME)) {
            String type = componentNode.getProperty(RESOURCE_TYPE_PROPERTY_NAME).getString();
            return type.equals(ARTICLE_STEP_BY_STEP_RESOURCE_TYPE);
        } else {
            return false;
        }
        } catch (RepositoryException e) {
            return false;
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
            if (imageNode.hasProperty(FILE_REFERENCE_PARAMETER_NAME)) {
                return imageNode.getProperty(FILE_REFERENCE_PARAMETER_NAME).getString();
            } else {
                return null;
            }
        } catch (RepositoryException e) {
            return null;
        }
    }
    
   

    /**
     * Retrieve the Resource for an image contained in a article step by step component
     * @param resource Sling Resource of the article step by step component you wish to retrieve an image from
     * @return Resource for the image that you are retrieving
     */
    public static Resource imageFromArticleStepbyStep(final Resource resource) {
        if (resource == null) {
            return null;
        }
        return Functions.getResourceChild(resource, IMAGE_RESOURCE_NAME);
    }
    
    /**
     * Retrieve the caption property from a photo gallery component resource
     * @param resource Sling Resource of the photo gallery you a retrieving the caption from
     * @return String value of the caption, without markup
     */
    public static String textFromArticleStepbyStep(final Resource resource) {
        if (resource == null) {
            return null;
        }
        return Functions.removeMarkup(Functions.getResourceProperty(resource, ARTICLE_BODY_REFERENCE_PARAMETER_NAME));
    }
    
    /**
     * Return the path to the image's thumb nail rendition, given the Resource of an image
     * @param resource Sling Resource of the image whose thumbnail path you want
     * @return String path to the thumb nail rendition
     */
    public static String thumbPathFromImage(final Resource resource) {
        if (resource == null) {
            return null;
        }
        return pathFromImage(resource) + THUMBNAIL_RENDITION_PATH;
    }
    
    /**
     * Retrieve the caption property from a photo gallery component resource
     * @param resource Sling Resource of the photo gallery you a retrieving the caption from
     * @return String value of the caption, without markup
     */
    public static String captionFromArticleStepbyStep(final Resource resource) {
        if (resource == null) {
            return null;
        }
        return Functions.removeMarkup(Functions.getResourceProperty(resource, CAPTION_PROPERTY_NAME));
    }
    
    
    /**
     * Return the path to the image's  rendition, given the Resource of an image
     * @param resource Sling Resource of the image whose image's  rendition path you want
     * @return String path to the image's  rendition
     */
    public static String renditionPathFromImage(final Resource resource) {
        if (resource == null) {
            return null;
        }
        return pathFromImage(resource) + IMAGE_RENDITION_PATH;
    }

    /**
     * Generate a List of ArticleStepbyStep objects from a ArticlePage. This retrieves all of the article step by step
     * components without page-break components and article text.
     * @param paginator ArticlePage object from which to retrieve ArticleStepbyStep objects
     * @return List of ArticleStepbyStep objects
     */
    public static List<ArticleStepbyStep> articleImagesFromArticlePage(final List<Resource> articlePage) {
        if (articlePage == null) {
            return null;
        }

        List<ArticleStepbyStep> articleImages = new ArrayList<ArticleStepbyStep>();
        Iterator<Resource> pageIterator = articlePage.iterator();
        while (pageIterator.hasNext()) {
            Resource resource = pageIterator.next();
            //skip if we're on a article step by step text 
            if (!ArticleStepbyStepUitl.isArticleStepbyStepImageComponent(resource)) {
                continue;
            }
            ArticleStepbyStep articleSterpbyStep = new ArticleStepbyStep(resource);
            articleImages.add(articleSterpbyStep);

        }
        return articleImages;
    }

    
}
