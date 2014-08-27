package com.scrippsnetworks.wcm.recipe.promotion.impl;

import com.scrippsnetworks.wcm.fnr.util.OsgiHelper;
import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.Recipe;
import com.scrippsnetworks.wcm.recipe.promotion.Promotion;
import com.scrippsnetworks.wcm.search.SearchService;
import com.scrippsnetworks.wcm.search.SearchUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jonathan Bell
 *         07/23/2013
 */

public class PromotionImpl implements Promotion {

    private static final Logger log = LoggerFactory.getLogger(PromotionImpl.class);

    private SniPage packageAnchor;
    private SniPage sniPage;
    private Boolean isPackaged;
    private String recipeTitle;
    private String recipeUrl;
    private String recipeTalentName;
    private String recipeTalentUrl;
    private String recipeImageUrl;
    private Recipe nextRecipe;

    private static final Logger LOG = LoggerFactory.getLogger(PromotionImpl.class);
    private static final String EMPTY_STRING = "";

    public PromotionImpl(final SniPage sniPage) {
        this.sniPage = sniPage;
        this.packageAnchor = sniPage.getPackageAnchor();
        this.isPackaged = packageAnchor != null;
        this.nextRecipe = getNextRecipe();
    }

    public String getUrl() {
        String promoUrl = null;

        if (isPackaged) {
            promoUrl = packageAnchor.getUrl();
        } else {
            promoUrl = recipeUrl;
        }

        return promoUrl == null ? EMPTY_STRING : promoUrl;
    }

    public String getRecipeUrl(){
        return recipeUrl == null ? EMPTY_STRING : recipeUrl;
    }

    public String getTitle() {
        String promoTitle;

        if (isPackaged) {
            promoTitle = packageAnchor.getTitle();
        } else {
            promoTitle = recipeTitle;
        }

        return promoTitle == null ? EMPTY_STRING : promoTitle;
    }

    public String getImageUrl() {
        String promoImagePath;

        if (isPackaged) {
            promoImagePath = packageAnchor.getCanonicalImagePath();
        } else {
            promoImagePath = recipeImageUrl;
        }

        return promoImagePath == null ? EMPTY_STRING : promoImagePath;

    }

    public String getTalentName() {
        return recipeTalentName == null ? EMPTY_STRING : recipeTalentName;
    }

    public String getTalentUrl() {
        return recipeTalentUrl == null ? EMPTY_STRING : recipeTalentUrl;
    }

    public SniPage getPackageSniPage() {
        return packageAnchor;
    }

    public Recipe getNextRecipe() {
        if (nextRecipe == null) {
            HashMap<String,String> params = new HashMap<String,String>();            
            OsgiHelper osgiHelper = new OsgiHelper();
            try {
                SearchService searchService = osgiHelper.getOsgiService(SearchService.class.getName());

                params.put("assetId", sniPage.getProperties().get(PagePropertyConstants.PROP_SNI_ASSETUID, ""));
                params.put("numOfResults", "1");

                Map<String, Object> map = SearchUtil.getSearchResponseMap(searchService, "similarRecipes", params);
                if (map != null) {
                    Map<String, Object> response = (Map)map.get("response");
                    if (response != null) {
                        Map<String, Object> results = (Map)response.get("results");
                        if (results != null) {
                            ArrayList assets = (ArrayList)results.get("assets");
                            if (assets != null && assets.size() > 0) {
                                Map<String, Object> recipe = (Map)assets.get(0);
                                recipeTitle = (String)recipe.get("CORE_TITLE");
                                recipeUrl = (String)recipe.get("CORE_URL");
                                recipeTalentName = (String)recipe.get("CORE_TALENT_NAME");
                                recipeTalentUrl = (String)recipe.get("CORE_TALENT_URL");
                                recipeImageUrl = (String)recipe.get("CORE_IMAGE_PATH");
                            }
                        }
                    }
                }
            } catch (NullPointerException npe) {
                log.error("Caught NullPointerException in PromotionImpl: {}", npe);
            }
        }

        return nextRecipe;        
    }

    public Boolean getIsPackaged() {
        return isPackaged;
    }

}

