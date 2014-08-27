package com.scrippsnetworks.wcm.asset.article.stepbystep;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.commons.lang.Validate;

import org.apache.sling.api.resource.ValueMap;
import org.slf4j.LoggerFactory;

/**
 * Object representation of an Article Step By Step Text/Image component
 * @author Jason Clark
 * Date: 10/17/12
 */
@Deprecated
public class ArticleStepByStepTextImageComponent {

    private static final String EMPTY_STRING = "";
    private static final String SLING_RESOURCE_TYPE_TEXT_IMAGE =
            "sni-wcm/components/pagetypes/article-step-by-step/components/article-step-by-step-text-image";
    private static final String NODE_NAME_IMAGE_COMPONENT = "image";
    private static final String NODE_NAME_SMART_IMAGE = "sni:articleImage";
    private static final String PROPERTY_NAME_FILE_REFERENCE = "fileReference";
    private static final String PROPERTY_NAME_IMAGE_CAPTION = "imageCaption";
    private static final String NODE_NAME_TEXT = "text";
    private static final String PROPERTY_NAME_ARTICLE_BODY = "sni:articleBody";

    private String imagePath;
    private String articleBody;
    private String imageCaption;

    public ArticleStepByStepTextImageComponent(final Resource resource) {
        Validate.notNull(resource);
        Validate.isTrue(ResourceUtil.isA(resource, SLING_RESOURCE_TYPE_TEXT_IMAGE));
        try {
            Resource imageResource = resource.getChild(NODE_NAME_IMAGE_COMPONENT);
            Resource smartImageResource;
            if (imageResource != null) {
                smartImageResource = imageResource.getChild(NODE_NAME_SMART_IMAGE);
                if (smartImageResource != null) {
                    ValueMap imageProperties = ResourceUtil.getValueMap(imageResource);
                    ValueMap smartImageProperties = ResourceUtil.getValueMap(smartImageResource);
                    if (imageProperties.containsKey(PROPERTY_NAME_IMAGE_CAPTION)) {
                        imageCaption = imageProperties.get(PROPERTY_NAME_IMAGE_CAPTION).toString();
                    } else {
                        imageCaption = EMPTY_STRING;
                    }
                    if (smartImageProperties.containsKey(PROPERTY_NAME_FILE_REFERENCE)) {
                        imagePath = smartImageProperties.get(PROPERTY_NAME_FILE_REFERENCE).toString();
                    } else {
                        imagePath = EMPTY_STRING;
                    }
                }
            } else {
                imagePath = EMPTY_STRING;
                imageCaption = EMPTY_STRING;
            }
            Resource textResource = resource.getChild(NODE_NAME_TEXT);
            if (textResource != null) {
                ValueMap textProperties = ResourceUtil.getValueMap(textResource);
                if (textProperties.containsKey(PROPERTY_NAME_ARTICLE_BODY)) {
                    articleBody = textProperties.get(PROPERTY_NAME_ARTICLE_BODY).toString();
                } else {
                    articleBody = EMPTY_STRING;
                }
            } else {
                articleBody = EMPTY_STRING;
            }

        } catch (NullPointerException npe) {
            LoggerFactory.getLogger(getClass()).error(npe.getMessage());
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
}
