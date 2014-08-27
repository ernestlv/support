package com.scrippsnetworks.wcm.recipe.promotion;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.Recipe;

/**
 * @author Jonathan Bell
 *         07/23/2013
 */

public interface Promotion {
    public Boolean getIsPackaged();

    public String getUrl();
    public String getTitle();
    public String getImageUrl();
    public String getTalentName();
    public String getTalentUrl();
    public String getRecipeUrl();

    public SniPage getPackageSniPage();
    public Recipe getNextRecipe();
}
