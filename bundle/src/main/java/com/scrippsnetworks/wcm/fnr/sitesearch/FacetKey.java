package com.scrippsnetworks.wcm.fnr.sitesearch;

import org.apache.commons.lang3.text.WordUtils;

public enum FacetKey {
    TALENT("TALENT"),
    SHOW("SHOW"),
    CUISINE("CUISINE"),
    COURSE("COURSE"),
    MAIN_INGREDIENT("MAIN_INGREDIENT"),
    DISH("DISH"),
    SOURCE("SOURCE"),
    AIRDATE("DATE_RANGE");

    String propertyName;

    FacetKey(String propertyName) {
        this.propertyName = propertyName;
    }

    String getPropertyName() {
        return propertyName;
    }

    public static FacetKey valueForPropertyName(String propertyName) {
        for (FacetKey fk : values()) {
            String fkName = fk.getPropertyName();
            if (fkName != null && fkName.equals(propertyName)) {
                return fk;
            }
        }
        return null;
    }

    public String getFacetDisplayName(SearchType searchType) {
        String retVal;
        if (this.equals(TALENT) && SearchType.recipes.equals(searchType)) {
            retVal = "Chef";
        } else if (this.equals(TALENT) && SearchType.episodes.equals(searchType)) {
            retVal = "Chef or Host";
        } else if (this.equals(TALENT) && SearchType.mobile_episodes.equals(searchType)) {
            retVal = "Chef or Host"; 
	    } else if (this.equals(TALENT) && SearchType.mobile_recipes.equals(searchType)) {
	        retVal = "Chef or Host"; 
	    }
        else if (this.equals(MAIN_INGREDIENT) && (SearchType.recipes.equals(searchType) || SearchType.mobile_recipes.equals(searchType))) {
            retVal = "Ingredient";
        } else {
            retVal = WordUtils.capitalizeFully(name().toLowerCase().replace("_", " "));
        }
        return retVal;
    }
}
