package com.scrippsnetworks.wcm.snitag;

import com.scrippsnetworks.wcm.page.SniPage;
import org.apache.sling.api.resource.ValueMap;

/**
 * @author Jason Clark
 *         Date: 6/29/13
 */  
public interface SniTag {

    /** Namespace is the prefix of the tag. ex: food-tags:cuisines/asian/thai food-tags is the namespace. */
    public String getNamespace();

    /** Facet is the first part of the tag. ex: food-tags:cuisines/asian/thai cuisines is the facet. */
    public String getFacet();

    /** Classification is the second part of the tag. Optional. ex: food-tags:cuisines/asian/thai asian is the classification. */
    public String getClassification();

    /** Value is the last part of the tag.  ex: food-tags:cuisines/asian/thai thai is the value. */
    public String getValue();

    /** Value, uppercased and without dashes. */
    public String getDisplayValue();

    /** Description on tag node. */
    public String getDescription();

    /** The ValueMap of properties from the tag node. */
    public ValueMap getTagProperties();

    /** The SniPage related to this Tag value, if any. */
    public SniPage getTopicPage();

    /** Raw tag. ex: food-tags:cuisines/asian/thai */
    public String getRawTag();

    /** Tag Metadata. ex: Attribution logo, URL and text. */
    public SniPage getTagMetadataPage();
}
