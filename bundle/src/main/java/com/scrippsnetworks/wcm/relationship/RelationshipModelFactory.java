package com.scrippsnetworks.wcm.relationship;

import static com.scrippsnetworks.wcm.relationship.RelationshipTypes.*;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.relationship.impl.EpisodeRelationshipModel;
import com.scrippsnetworks.wcm.relationship.impl.RecipeRelationshipModel;
import com.scrippsnetworks.wcm.relationship.impl.RelationshipModelImpl;
import com.scrippsnetworks.wcm.relationship.impl.ShowRelationshipModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import com.scrippsnetworks.wcm.relationship.impl.MenuRelationshipModel;

/**
 * @author Jason Clark
 *         Date: 6/29/13
 */
public class RelationshipModelFactory {

    private SniPage sniPage;
    private String assetPath;
    private String pagePath;
    private Resource assetResource;
    private Resource pageResource;
    private ResourceResolver resourceResolver;

    /** Builder a new Relationship model based on the given inputs. */
    public RelationshipModel build() {
        if (resourceResolver != null) {
            if (StringUtils.isNotBlank(assetPath)) {
                return new RelationshipModelImpl(resourceResolver, assetPath, ASSET);
            }
            if (StringUtils.isNotBlank(pagePath)) {
                return new RelationshipModelImpl(resourceResolver, pagePath, PAGE);
            }
        }
        if (sniPage != null) {
            String pageType = sniPage.getPageType();
            if (StringUtils.isNotBlank(pageType)) {
                if (pageType.equals("recipe")) {
                    return new RecipeRelationshipModel(sniPage);
                } else if (pageType.equals("menu")) {
                    return new MenuRelationshipModel(sniPage);
                }else if (pageType.equals("episode")) {
                    return new EpisodeRelationshipModel(sniPage);
                } else if (pageType.equals("show")) {
                    return new ShowRelationshipModel(sniPage);
                }
            }
            return new RelationshipModelImpl(sniPage);
        }
        if (assetResource != null) {
            return new RelationshipModelImpl(assetResource, ASSET);
        }
        if (pageResource != null) {
            return new RelationshipModelImpl(pageResource, PAGE);
        }
        return null;
    }

    /** Add an SniPage to this builder. */
    public RelationshipModelFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }

    /** Add an asset path to this builder. */
    public RelationshipModelFactory withAssetPath(String assetPath) {
        this.assetPath = assetPath;
        return this;
    }

    /** Add a page path to this builder. */
    public RelationshipModelFactory withPagePath(String pagePath) {
        this.pagePath = pagePath;
        return this;
    }

    /** Add an asset Resource to this builder. */
    public RelationshipModelFactory withAssetResource(Resource resource) {
        this.assetResource = resource;
        return this;
    }

    /** Add a page Resource to this builder. */
    public RelationshipModelFactory withPageResource(Resource resource) {
        this.pageResource = resource;
        return this;
    }

    /** Add a ResourceResolver to this factory.  Used to turn paths into usable objects. */
    public RelationshipModelFactory withResourceResolver(ResourceResolver resolver) {
        this.resourceResolver = resolver;
        return this;
    }

}
