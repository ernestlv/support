package com.scrippsnetworks.wcm.asset.article.simple;

import org.apache.sling.api.resource.Resource;
import org.apache.commons.lang.Validate;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;

/**
 * Abstract representation of an Article Simple Title component
 * @author Jason Clark
 * Date: 7/24/12
 */
@Deprecated
public class ArticleSimpleTitleComponent {

    /* STATIC STRINGS */
    public static final String COMPONENT_NAME = "article-simple-title";
    public static final String COMPONENT_SLING_RESOURCE_TYPE =
            "sni-wcm/components/pagetypes/article-simple/components/" + COMPONENT_NAME;
    public static final String PROPERTY_NAME_TITLE = "title";
    public static final String PROPERTY_NAME_DESCRIPTION = "description";
    public static final String PROPERTY_NAME_BYLINE = "byline";

    /* MEMBER FIELDS */
    private String articleTitle;
    private String articleByline;
    private String articleDescription;

    /**
     * empty constructor
     */
    public ArticleSimpleTitleComponent() {}

    /**
     * construct an abstract ArticleSimpleTitleComponent given a Sling Resource of the
     * article-simple-title component you want to abstract
     * @param resource Sling Resource of the article-simple-title component
     */
    public ArticleSimpleTitleComponent(final Resource resource) {
        if (resource != null && resource.isResourceType(COMPONENT_SLING_RESOURCE_TYPE)) {
            ValueMap titleValues = ResourceUtil.getValueMap(resource);
            articleTitle = titleValues.containsKey(PROPERTY_NAME_TITLE) ? titleValues.get(PROPERTY_NAME_TITLE).toString() : null;
            articleByline = titleValues.containsKey(PROPERTY_NAME_BYLINE) ? titleValues.get(PROPERTY_NAME_BYLINE).toString() : null;
            articleDescription = titleValues.containsKey(PROPERTY_NAME_DESCRIPTION) ? titleValues.get(PROPERTY_NAME_DESCRIPTION).toString() : null;
        }
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }

    public String getArticleByline() {
        return articleByline;
    }

    public void setArticleByline(String articleByline) {
        this.articleByline = articleByline;
    }

    public String getArticleDescription() {
        return articleDescription;
    }

    public void setArticleDescription(String articleDescription) {
        this.articleDescription = articleDescription;
    }
}
