package com.scrippsnetworks.wcm.util;

/**
 * @author Jason Clark
 *         Date: 11/16/12
 */
public enum AssetPropertyNames {

    JCR_TITLE ("jcr:title"),
    JCR_DESCRIPTION ("jcr:description"),
    SNI_FASTFWD_URL ("sni:fastfwdUrl"),
    SNI_FASTFWD_STATUS ("sni:fastfwdStatus"),
    SNI_FASTFWD_ID ("sni:fastfwdId"),
    SNI_RECIPE_BODY ("sni:recipeBody"),
    SNI_ASSET_TYPE ("sni:assetType"),
    SNI_RECIPES ("sni:recipes"),
    SNI_PRIMARY_TALENT ("sni:primaryTalent"),
    SNI_RANK_ORDER ("sni:rankOrder"),
    SNI_PAGE_LINKS ("sni:pageLinks"),
    SNI_PEOPLE ("sni:people"),
    SNI_SORT_TITLE ("sni:sortTitle"),
    SNI_ABSTRACT ("sni:abstract"),
    SNI_ASSET_UID ("sni:assetUId"),
    SNI_VALUE ("sni:value"),
    SNI_EPISODE_NO ("sni:episodeNo"),
    SLING_RESOURCE_TYPE ("sling:resourceType");
    

    private String propertyName;

    private AssetPropertyNames(final String name) {
        this.propertyName = name;
    }

    public String propertyName() {return this.propertyName;}
}
