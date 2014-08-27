package com.scrippsnetworks.wcm.mobile.lead3imagerecipe.impl;

import com.scrippsnetworks.wcm.mobile.lead3image.LeadThreeImageItem;
import com.scrippsnetworks.wcm.mobile.lead3imagerecipe.LeadThreeImageItemRecipe;

/**
 * Created by Dzmitry_Drepin on 1/29/14.
 */
public class LeadThreeImageRecipeItemImpl implements LeadThreeImageItemRecipe {
    private boolean containRecipeCourtesy = false;
    private LeadThreeImageItem item = new com.scrippsnetworks.wcm.mobile.lead3image.impl.LeadThreeImageItemImpl();

    public boolean isContainRecipeCourtesy() {
        return containRecipeCourtesy;
    }

    public void setContainRecipeCourtesy(boolean containRecipeCourtesy) {
        this.containRecipeCourtesy = containRecipeCourtesy;
    }

    public String getTitle() {
        return item.getTitle();
    }

    public void setTitle(String title) {
        item.setTitle(title);
    }

    public String getImageDamPath() {
        return item.getImageDamPath();
    }

    public void setImageDamPath(String imageDamPath) {
        item.setImageDamPath(imageDamPath);
    }

    public String getUrl() {
        return item.getUrl();
    }

    public void setUrl(String url) {
        item.setUrl(url);
    }

    public String getCssClassName() {
        return item.getCssClassName();
    }

    public void setCssClassName(String cssClassName) {
        item.setCssClassName(cssClassName);
    }

    public String getDescription() {
        return item.getDescription();
    }

    public void setDescription(String description) {
        item.setDescription(description);
    }
}
