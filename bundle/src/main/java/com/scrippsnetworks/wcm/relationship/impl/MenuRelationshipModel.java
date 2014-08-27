package com.scrippsnetworks.wcm.relationship.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.relationship.RelationshipModel;
import com.scrippsnetworks.wcm.util.ContentRegionNames;
import com.scrippsnetworks.wcm.util.DialogPropertyNames;

/**
 * @author Venkata Naga Sudheer Donaboina Date: 8/20/13
 * 
 */
public class MenuRelationshipModel implements RelationshipModel {

    private static final String SNI_CONTENT_PATH = "/content";
    private static final String SNI_ASSET_PATH = "/etc/sni-asset";
    private static final String JCR_CONTENT_PATH = "/jcr:content";
    private static final String SNI_PAGE_LINKS = "sni:pageLinks";
    private static final String SNI_PRIMARY_TALENT = "sni:primaryTalent";

    private static final String RESOURCE_TYPE_MENU_RECIPE_LISTING_ROW =
            "sni-food/components/modules/menu-recipe-listings-row";
    public static final String COMPOSITE_VALUE_DELIMITER = "|";

    private static final Logger LOG = LoggerFactory.getLogger(MenuRelationshipModel.class);

    /** Member for Sequence Number. */
    int value=0;
    
    Integer sequenceNumber = new Integer(value);
    
    /** Member for related Recipe pages. */
    private List < SniPage > recipePages;

    /** SniPage for menu. */
    private SniPage menuPage;

    /** PageManager for Menu. */
    private PageManager pageManager;

    /** Merged ValueMap from Menu page and asset. */
    private ValueMap menuProperties;

    /** Resource for convenience, because you need a Resource from time to time. */
    private Resource resource;

    /**
     * ResourceResolver for convenience, because you need a resource resolver from time to time.
     */
    private ResourceResolver resourceResolver;
    
    private List<String> mealTypeRecipeList = new ArrayList<String>();
    
    /** Create a new relationship model for Menu Page. */
    public MenuRelationshipModel(SniPage sniPage) {
        if (sniPage != null) {
            menuPage = sniPage;
            pageManager = sniPage.getPageManager();
            menuProperties = sniPage.getProperties(); 
            resource = sniPage.getContentResource();
            if (resource != null) {
                resourceResolver = resource.getResourceResolver();
            }

        }
    }
    
    /** {@inheritDoc} */
    @Override
    public List < SniPage > getRecipePages() {
        setRecipePagesAndMealType();
        return recipePages;
    }

    /** {@inheritDoc} */
    public String[] getMealTypeRecipes() {
    	setRecipePagesAndMealType();
		if (mealTypeRecipeList.size() > 0) {
			return mealTypeRecipeList.toArray(new String[mealTypeRecipeList.size()]);
		} else {
			return null;
		}
	}
    
    public void setRecipePagesAndMealType() {
    	if (recipePages == null && resource != null) {
            Resource contentWellResource = resource.getChild(ContentRegionNames.CONTENT_WELL.regionName());
            if (contentWellResource != null) {
                Iterator < Resource > childNodes = contentWellResource.listChildren();
                if (childNodes != null) {
                    recipePages = getRecipeFromComponents(childNodes);
                }
            }
        }
    }

	/**
	 * This method is used to retrieve recipe Paths and set mealTypeRecipe from
	 * the menu-recipe-listing components.
	 * 
	 * @param childNodes
	 * @return
	 */
    private List < SniPage > getRecipeFromComponents(Iterator < Resource > childNodes) {
        Resource childResource = null;
        List < SniPage > recipePages = new ArrayList < SniPage >();
        while (childNodes.hasNext()) {
            childResource = childNodes.next();
            if (childResource.getResourceType().equals(RESOURCE_TYPE_MENU_RECIPE_LISTING_ROW)) {
                setRecipeAndMealTypeFromResource(childResource, recipePages);
            }
        }
        if (recipePages.size() > 0) {
            return recipePages;
        } else {
            return null;
        }
    }
    
	/**
	 * Sets the recipes and mealTypeRecipes from the corresponding
	 * menu-recipe-listing component.
	 * 
	 * @param childResource
	 * @param recipePages
	 */
    private void setRecipeAndMealTypeFromResource(Resource childResource, List < SniPage > recipePages) {
        ValueMap componentProperties = ResourceUtil.getValueMap(childResource);
        if (componentProperties != null) {
            if (componentProperties.containsKey(DialogPropertyNames.SNI_RECIPE1.dialogPropertyName())) {
            	String recipePath1 = componentProperties.get(DialogPropertyNames.SNI_RECIPE1.dialogPropertyName(), String.class);
            	SniPage recipePage1 = getPageFromPath(recipePath1);
				if(recipePage1 != null) {
					recipePages.add(recipePage1);
					String mealType1 = null;
	            	if (componentProperties.containsKey(DialogPropertyNames.SNI_MEALTYPE1.dialogPropertyName())) {
						mealType1 = componentProperties.get(DialogPropertyNames.SNI_MEALTYPE1.dialogPropertyName(), String.class);
					}
					mealType1 = mealType1 != null ? mealType1 : "";
					sequenceNumber = sequenceNumber+1;
					mealTypeRecipeList.add(mealType1 + COMPOSITE_VALUE_DELIMITER + recipePage1.getUid() + COMPOSITE_VALUE_DELIMITER + recipePage1.getTitle() + COMPOSITE_VALUE_DELIMITER + sequenceNumber);
				}
            }
            
            if (componentProperties.containsKey(DialogPropertyNames.SNI_RECIPE2.dialogPropertyName())) {
            	String recipePath2 = componentProperties.get(DialogPropertyNames.SNI_RECIPE2.dialogPropertyName(), String.class);
            	SniPage recipePage2 = getPageFromPath(recipePath2);
				if(recipePage2 != null) {
					recipePages.add(recipePage2);
					String mealType2 = null;
	            	if (componentProperties.containsKey(DialogPropertyNames.SNI_MEALTYPE2.dialogPropertyName())) {
						mealType2 = componentProperties.get(DialogPropertyNames.SNI_MEALTYPE2.dialogPropertyName(), String.class);
	                }
					mealType2 = mealType2 != null ? mealType2 : "";
					sequenceNumber = sequenceNumber+1;
					mealTypeRecipeList.add(mealType2 + COMPOSITE_VALUE_DELIMITER + recipePage2.getUid() + COMPOSITE_VALUE_DELIMITER + recipePage2.getTitle()+ COMPOSITE_VALUE_DELIMITER + sequenceNumber);
				}
            }
            
            if (componentProperties.containsKey(DialogPropertyNames.SNI_RECIPE3.dialogPropertyName())) {
            	String recipePath3 = componentProperties.get(DialogPropertyNames.SNI_RECIPE3.dialogPropertyName(), String.class);
            	SniPage recipePage3 = getPageFromPath(recipePath3);
            	if(recipePage3 != null) {
					recipePages.add(recipePage3);
	            	String mealType3 = null;
	                if (componentProperties.containsKey(DialogPropertyNames.SNI_MEALTYPE3.dialogPropertyName())) {
						mealType3 = componentProperties.get(DialogPropertyNames.SNI_MEALTYPE3.dialogPropertyName(), String.class);
					}
	                mealType3 = mealType3 != null ? mealType3 : "";
	                sequenceNumber = sequenceNumber+1;
					mealTypeRecipeList.add(mealType3 + COMPOSITE_VALUE_DELIMITER + recipePage3.getUid() + COMPOSITE_VALUE_DELIMITER + recipePage3.getTitle() + COMPOSITE_VALUE_DELIMITER + sequenceNumber);
				}
            }
            
        }
    }
    

    /**
     * Convenience method for getting an SniPage from an asset path via the pageLinks.
     */
    private SniPage getPageFromPath(final String assetPath) {
        SniPage recipeSniPage = null;
        if (StringUtils.isNotBlank(assetPath)) {
            if (StringUtils.startsWith(assetPath, SNI_ASSET_PATH)) {
                if (resourceResolver != null) {
                    Resource assetResource = resourceResolver.resolve(assetPath + JCR_CONTENT_PATH);
                    if (assetResource != null) {
                        ValueMap assetProperties = assetResource.adaptTo(ValueMap.class);
                        if (assetProperties != null && assetProperties.containsKey(SNI_PAGE_LINKS)) {
                            String[] pageLinks = assetProperties.get(SNI_PAGE_LINKS, String[].class);
                            if (pageLinks != null) {
                                String pagePath = pageLinks[0];
                                if (StringUtils.isNotBlank(pagePath)) {
                                    Resource pageResource = resourceResolver.resolve(pagePath);
                                    if (pageResource != null) {
                                        Page recipePage = pageResource.adaptTo(Page.class);
                                        if (recipePage != null) {
                                            recipeSniPage = PageFactory.getSniPage(recipePage);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (StringUtils.startsWith(assetPath, SNI_CONTENT_PATH)) {
                Resource pageResource = resourceResolver.resolve(assetPath);
                if (pageResource != null) {
                    Page recipePage = pageResource.adaptTo(Page.class);
                    if (recipePage != null) {
                        recipeSniPage = PageFactory.getSniPage(recipePage);
                    }
                }
            }
        }
        return recipeSniPage;
    }

    @Override
    public List < Resource > getRecipeAssets() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SniPage getPrimaryTalent() {
        SniPage primaryTalent = null;
        if (menuPage != null) {
            Page primaryTalentPage = null;
            String talentPath = menuPage.getProperties().get(SNI_PRIMARY_TALENT, String.class);
            if (talentPath != null) {
                primaryTalentPage = pageManager.getPage(talentPath);
            }
            if (primaryTalentPage != null) {
                primaryTalent = PageFactory.getSniPage(primaryTalentPage);
            }
        }
        return primaryTalent;
    }

    @Override
    public List < SniPage > getTalentPages() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List < Resource > getTalentAssets() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List < SniPage > getEpisodePages() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List < Resource > getEpisodeAssets() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List < SniPage > getShowPages() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List < Resource > getShowAssets() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List < Resource > getScheduleAssets() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List < SniPage > getCompanyPages() {
        // TODO Auto-generated method stub
        return null;
    }

}
