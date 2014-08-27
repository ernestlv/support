package com.scrippsnetworks.wcm.menu.impl;

import java.util.ArrayList;
import java.util.List;

import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.menu.Menu;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.relationship.RelationshipModel;
import com.scrippsnetworks.wcm.relationship.RelationshipModelFactory;

/**
 * @author Jason Clark 
 *             Date: 5/11/13 
 * Updated Venkata Naga Sudheer Donaboina
 * Date: 8/20/13
 * 
 */
public class MenuImpl implements Menu {

    private static final Logger LOG = LoggerFactory.getLogger(MenuImpl.class);
    
    private static final String SNI_DESCRIPTION = "sni:description";

    /** SniPage of menu used to create this object. */
    private SniPage sniPage;

    /** ValueMap of properties merged from menu page and asset. */
    private ValueMap menuProperties;

    /** ResourceResolver for convenience. */
    private ResourceResolver resourceResolver;

    /** Title of this Menu. */
    private String title;

    /** Description of this Menu. */
    private String description;

    /** Primary Talent page for this Menu. */
    private SniPage primaryTalentPage;

    /** Member for list of recipe pages related to this menu. */
    private List<SniPage> relatedRecipePages;
    
    /** Member for list of mealType, recipe page id's related to this menu. */
    private String[] mealTypeRecipes;

    /** For storing the RelationshipModel for this menu. */
    private RelationshipModel menuRelationshipModel;

    /** Construct a new MenuImpl given an SniPage. */
    public MenuImpl(SniPage sniPage) {
        this.sniPage = sniPage;
        this.menuProperties = sniPage.getProperties();
        Resource resource = sniPage.getContentResource();
        if (resource != null) {
            resourceResolver = resource.getResourceResolver();
        }
    }

    public MenuImpl(Resource resource) {
        this.sniPage = PageFactory.getSniPage(resource.adaptTo(Page.class));
        if (sniPage != null) {
            this.menuProperties = sniPage.getProperties();
        }
        this.resourceResolver = resource.getResourceResolver();
    }

    /** {@inheritDoc} */
    @Override
    public SniPage getSniPage() {
        return sniPage;
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        if (title == null) {
            title = sniPage.getTitle();
        }
        return title;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        if (description == null) {
            if (menuProperties != null
                    && menuProperties.containsKey(SNI_DESCRIPTION)) {
                description = menuProperties.get(SNI_DESCRIPTION, String.class);
            }
        }
        return description;
    }

    /** {@inheritDoc} */
    @Override
    public List<SniPage> getRecipePages() {
        if (relatedRecipePages == null) {
            relatedRecipePages = new ArrayList<SniPage>();
            RelationshipModel menuModel = getMenuRelationshipModel();
            LOG.info("menuRelationshipModel is :: " + menuModel);
            if (menuModel != null) {
                List<SniPage> recipes = menuModel.getRecipePages();
                if (recipes != null) {
                    relatedRecipePages.addAll(recipes);
                }
            }
        }
        return relatedRecipePages;
    }
    
    /** {@inheritDoc} */
    @Override
    public String[] getMealTypeRecipes() {
        if (mealTypeRecipes == null) {
            RelationshipModel menuModel = getMenuRelationshipModel();
            if (menuModel != null) {
                mealTypeRecipes = menuModel.getMealTypeRecipes();
            }
        }
        return mealTypeRecipes;
    }
    
    /** Convenience method for retrieving relationship model. */
    private RelationshipModel getMenuRelationshipModel() {
        if (menuRelationshipModel == null) {
            menuRelationshipModel = new RelationshipModelFactory()
                    .withSniPage(sniPage)
                    .build();
        }
        return menuRelationshipModel;
    }

    /** {@inheritDoc} */
    @Override
    public SniPage getPrimaryTalentPage() {
        if (primaryTalentPage == null) {
            RelationshipModel menuModel = getMenuRelationshipModel();
            if (menuModel != null) {
                SniPage talentPage = menuModel.getPrimaryTalent();
                if (talentPage != null) {
                    primaryTalentPage = talentPage;
                }
            }
        }
        return primaryTalentPage;
    }

}
