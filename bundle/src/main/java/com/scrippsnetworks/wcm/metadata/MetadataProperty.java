package com.scrippsnetworks.wcm.metadata;

import java.util.EnumSet;

public enum MetadataProperty {
    ADKEY1("AdKey1", "adkey1"),
    ADKEY2("AdKey2", "adkey2"),
    ALLTAGS("AllTags", "allTags"),
    CATEGORYDSPNAME("CategoryDspName", "categoryDisplayName"),
    CLASSIFICATION("Classification", "classification"),
    CONTENTTAG1("ContentTag1", "contentTag1"),
    CONTENTTAG2("ContentTag2", "contentTag2"),
    CROSSLINKTERMS("CrosslinkTerms", "crosslinkTerms"),
    CUISINE("Cuisine", "cuisine"),
    DETAILID("DetailId", "detailId"),
    DELIVERYCHANNEL("Delivery_Channel", "deliveryChannel"),
    DIFFICULTY("Difficulty", "difficulty"),
    DIMENSIONS("dimensions", "dimensions"),
    DIMENSIONVALUES("dimensionValues", "dimensionValues"),
    DISH("Dish", "dish"),
    FASTFWDID("FastFwdID", "fastFwdId"),
    FILTER("FILTER", "searchFilter"),
    HUBID("HubID", "hubId"),
    HUBPATH("HubPath", "hubPath"),
    HUBSPONSOR("HubSponsor", "hubSponsor"),
    HUBTYPE("HubType", "hubType"),
    IMGURL("ImgURL", "imgUrl"),
    INTERNALSEARCHTYPE("internalSearchType", "internalSearchType"),
    KEYWORDS("KEYWORDS", "searchKeywords"),
    KEYTERM("KEYTERM", "keyterm"),
    MEALTYPE("MealType", "mealType"),
    MEALPART("MealPart", "mealPart"),
    RESTRICTED("Restricted","restricted"),
    MAININGREDIENT("MainIngredient", "mainIngredient"),
    NOSEARCHRESULTS("NOSEARCHRESULTS", "noSearchResults"),
    NUTRITION("Nutrition", "nutrition"),
    OCCASIONS("Occasions", "occasions"),
    PACKAGENAME("PackageName", "packageName"),
    PAGENUMBER("PageNumber", "pageNumber"),
    PREPTIME("PrepTime", "prepTime"),
    PUBLISHTIME("PublishTime", "publishTime"),
    SCTNDSPNAME("SctnDspName", "sectionDisplayName"),
    SITE("Site", "siteName"),
    SEARCHTERMS("searchTerms", "searchTerms"),
    SECTION("Section", "section"),
    SHOWABBR("Show_Abbr", "showAbbreviation"),
    SORT("Sort", "searchSort"),
    SOURCE("Source", "source"),
    SPONSORSHIP("Sponsorship", "sponsorship"),
    SPOTLIGHT_1("Spotlight_1", "spotlight1"),
    SPOTLIGHT_2("Spotlight_2", "spotlight2"),
    /* "SPOTLIGHT_CENTER_1_NAME", "spotlightCenter1Name"),
    "SPOTLIGHT_CENTER_1_STYLE", "spotlightCenter1Style"),
    "SPOTLIGHT_CENTER_2_NAME", "spotlightCenter2Name"),
    "SPOTLIGHT_CENTER_2_STYLE", "spotlightCenter2Style", */
    TALENTNAME("talentName", "talentName"),
    TASTE("Taste", "taste"),
    TECHNIQUE("Technique", "technique"),
    TITLE("Title", "title"),
    TYPE("Type", "type"),
    UNIQUEID("UniqueID", "uniqueId"),
    URL("Url", "url"),
    CANONICALURL("CanonicalUrl", "canonicalUrl"),
    USERID("UserId", "userId"),
    /*Company properties*/
    ADDRESS1("Address1","address1"),
    ADDRESS2("Address2","address2"),
    ADDRESS3("Address3","address3"),
    CITY("City","city"),
    STATE("State","state"),
    ZIP("Zip","zip"),
    PHONE("Phone","phone"),
    TOLLFREEPHONE("TollFreePhone","tollFreePhone"),
    WEBSITE("Website","website"),
    EMAIL("Email","email"),
    FAX("Fax","fax"),
    LONGITUDE("Longitude","longitude"),
    LATITUDE("Latitude","Latitude"),
    SPECIALDISH("SpecialDish","specialDish");


    private String mdName;
    private String pdName;

    MetadataProperty(String mdName, String pdName) {
        this.mdName = mdName;
        this.pdName = pdName;
    }

    public String getMetadataName() {
        return mdName;
    }

    public String getPageDataName() {
        return pdName;
    }
    
    /*Unlike all the other properties the following properties' values has to be in the UPPER CASE */ 
    public static final EnumSet<MetadataProperty> UPPERCASE_PROPERTIES = EnumSet.of(SPONSORSHIP,PUBLISHTIME);
    
    /* Following properties' values has to be in the SAME CASE as it gets. No case change required  */ 
    public static final EnumSet<MetadataProperty> NO_CASE_CHANGE_PROPERTIES = EnumSet.of(IMGURL);
    
    /*Utility method to determine whether or not a given property belongs to the declared enum set */
    public final boolean isUpperCaseProperty() { return UPPERCASE_PROPERTIES.contains(this); }
    
    /*Utility method to determine whether or not a given property belongs to the declared enum set */
    public final boolean isNoCaseChangeProperty() { return NO_CASE_CHANGE_PROPERTIES.contains(this); }
    
}
