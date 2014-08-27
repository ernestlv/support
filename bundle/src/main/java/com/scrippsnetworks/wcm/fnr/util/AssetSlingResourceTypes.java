package com.scrippsnetworks.wcm.fnr.util;

/**
 * Enum for sling:resourceType properties of sni-asset nodes
 * @author Jason Clark
 *         Date: 11/16/12
 */
public enum AssetSlingResourceTypes {

    ASSET_TYPE_ROOT ("sni-core/components/assets"),
    EPISODE ("sni-food/components/assets/episode"),  //assets are brand-specific; need a better solution than an Enum
    SCHEDULE ("sni-food/components/assets/schedule"),
    PERSON ("sni-food/components/assets/talent"),
    RECIPE ("sni-core/components/assets/recipe"),
    SHOW ("sni-food/components/assets/show"),
    VIDEO ("sni-food/components/assets/video"),
    COMPANY("sni-food/components/pagetypes/company");

    private String resourceType;

    private AssetSlingResourceTypes(final String type) {
        this.resourceType = type;
    }

    public String resourceType() {return this.resourceType;}

}
