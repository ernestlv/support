package com.scrippsnetworks.wcm.asset.article.simple;

import org.apache.sling.api.resource.Resource;
import org.apache.commons.lang.Validate;
import org.apache.sling.api.resource.ResourceUtil;

/**
 * Abstract representation of an Article Simple image/text component
 * @author Jason Clark
 * Date: 7/24/12
 */
@Deprecated
public class ArticleSimpleTextImageComponent {

    /* STATIC STRINGS */
    public static final String COMPONENT_SLING_RESOURCE_TYPE =
            "sni-wcm/components/pagetypes/article-simple/components/article-simple-text-image";
    public static final String COMPONENT_SLING_RESOURCE_TYPE_BIO =
            "sni-wcm/components/pagetypes/bio/components/text-image";
    public static final String PROPERTY_NAME_ARTICLE_BODY = "sni:articleBody";
    public static final String PROPERTY_NAME_IMAGE_CAPTION = "imageCaption";
    public static final String PROPERTY_NAME_IMAGE_FILE_REFERENCE = "fileReference";
    public static final String NODE_NAME_ARTICLE_IMAGE_COMPONENT = "image";
    public static final String NODE_NAME_ARTICLE_TEXT_COMPONENT = "text";
    public static final String NODE_NAME_ARTICLE_IMAGE = "sni:articleImage";

    /* MEMBER FIELDS */
    private String imagePath;
    private String articleBody;
    private String imageCaption;

    /**
     * empty constructor
     */
    public ArticleSimpleTextImageComponent() {}

    /**
     * Construct an ArticleSimpleComponent object from the Resource of an article
     * simple text/image component
     * @param resource Sling Resource of the Article Simple text/image component
     */
    public ArticleSimpleTextImageComponent(final Resource resource) {
        Validate.notNull(resource);
        Validate.isTrue(resource.isResourceType(COMPONENT_SLING_RESOURCE_TYPE)
                || resource.isResourceType(COMPONENT_SLING_RESOURCE_TYPE_BIO));

        try {
            Resource imageComponent = resource.getChild(NODE_NAME_ARTICLE_IMAGE_COMPONENT);
            Resource smartImage = imageComponent.getChild(NODE_NAME_ARTICLE_IMAGE);
            Resource articleBodyText = resource.getChild(NODE_NAME_ARTICLE_TEXT_COMPONENT);

            imagePath = ResourceUtil.getValueMap(smartImage)
                    .get(PROPERTY_NAME_IMAGE_FILE_REFERENCE).toString();
            imageCaption = ResourceUtil.getValueMap(imageComponent)
                    .get(PROPERTY_NAME_IMAGE_CAPTION).toString();
            articleBody = ResourceUtil.getValueMap(articleBodyText)
                    .get(PROPERTY_NAME_ARTICLE_BODY).toString();
        } catch (NullPointerException npe) {
            imagePath = "";
            imageCaption = "";
            articleBody = "";
        }
    }

    public String getImagePath() {
        return imagePath;
    }
    public String getArticleBody() {
        return articleBody;
    }
    public String getImageCaption() {
        return imageCaption;
    }
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    public void setArticleBody(String articleBody) {
        this.articleBody = articleBody;
    }
    public void setImageCaption(String imageCaption) {
        this.imageCaption = imageCaption;
    }

}
