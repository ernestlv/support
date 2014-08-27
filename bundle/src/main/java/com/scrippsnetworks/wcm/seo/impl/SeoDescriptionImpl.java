package com.scrippsnetworks.wcm.seo.impl;

import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.Recipe;
import com.scrippsnetworks.wcm.recipe.RecipeFactory;
import com.scrippsnetworks.wcm.seo.SeoDescription;
import com.scrippsnetworks.wcm.seo.SeoProperty;
import com.scrippsnetworks.wcm.snitag.SniTag;
import com.scrippsnetworks.wcm.snitag.SniTagFactory;
import com.scrippsnetworks.wcm.util.PageTypes;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.EnumSet;

import org.jsoup.Jsoup;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;

/**
 * @author Jonathan Bell
 * Date: 8/15/2013
 */
public class SeoDescriptionImpl implements SeoDescription {

    private static final Logger log = LoggerFactory.getLogger(SeoDescription.class);    

    private static final String EMPTY_STRING = "";
    private static final String FORWARD_SLASH = "/";
    private static final String TAG_NAMESPACE_SUFFIX = "-strings";
    private static final String TAG_FACET = "meta-description";
    private static final String NAMESPACE_SEPARATOR = ":";

    private SniPage page;
    private HashMap pageValues;
    private StrSubstitutor subber;
    private TagManager tagManager;

    public SeoDescriptionImpl(SniPage page) {
        this.page = page;
        this.pageValues = getPageValues();
        this.subber = new StrSubstitutor(pageValues); 
        this.tagManager = getTagManager();
    }

    /**
     * Retrieve SEO-required properties from Metadata Manager
     * or generate them if unavailable.
     */
    private HashMap getPageValues() {
        HashMap map = new HashMap();

        EnumSet<SeoProperty> seoProperties = EnumSet.copyOf(SeoProperty.METADATA_PROPERTIES);
        seoProperties.addAll(SeoProperty.DERIVED_PROPERTIES);

        for (SeoProperty seoProp : seoProperties) { 
            map.put(seoProp.name(), seoProp.getPropertyValue(page));
        }

        return map;
    }

    public String getDescription() {
        String tagLocalId;
        String description = EMPTY_STRING;

        if (page == null) {
            return EMPTY_STRING;
        }

        ValueMap pageProperties = page.getProperties();
        if (pageProperties.containsKey(PagePropertyConstants.PROP_SNI_SEODESCRIPTION)) {
            String pageDescription = pageProperties.get(PagePropertyConstants.PROP_SNI_SEODESCRIPTION).toString();
            if (StringUtils.isNotEmpty(pageDescription)) {
                description = pageDescription;
            }
        }

        if (StringUtils.isEmpty(description) && tagManager != null) {        
            PageTypes type = PageTypes.findPageType(page.getPageType());
            if (type != null) {
                switch (type) {
                    case ASSET_RECIPES:
                        tagLocalId = getAssetRecipesTag();
                        break;
                    case RECIPE:
                        tagLocalId = getRecipeTag();
                        break;
                    default:
                        tagLocalId = TAG_FACET + FORWARD_SLASH + page.getPageType();
                        break;
                }
                description = subber.replace(getSubstitutionFormat(tagLocalId));
            }
        }

        if (StringUtils.isEmpty(description)) {
            description = page.getDescription();
        }

        return cleanString(description);
    }

    /**
     * Clean a string to remove HTML markup
     */
    private String cleanString(String dirtyText) {
        if (dirtyText != null && dirtyText.length() > 0) {
            return Jsoup.parse(dirtyText).text();
        } else {
            return "";
        }
    }

    /**
     * Set up a CQ Tag Manager instance.
     */
    private TagManager getTagManager() {
        if (page == null || page.getContentResource() == null ||
            page.getContentResource().getResourceResolver() == null) {
            return null;
        }

        return page.getContentResource().getResourceResolver().adaptTo(TagManager.class);
    }

    /**
     * Request format string from TagManager given 
     * a local ID, typically provided as "meta-description/pagetype"
     */
    private String getSubstitutionFormat(String tagLocalId) {
        Tag tag = null;
        StringBuilder baseSb = new StringBuilder()
            .append(page.getBrand())
            .append(TAG_NAMESPACE_SUFFIX + NAMESPACE_SEPARATOR);

        StringBuilder sb = new StringBuilder()
            .append(baseSb)
            .append(tagLocalId);

        tag = tagManager.resolve(sb.toString()); 
        if (tag == null) {
            sb.setLength(0);
            sb.append(baseSb)
                .append(TAG_FACET);
            tag = tagManager.resolve(sb.toString());
        }

        return tag != null ? tag.getDescription() : EMPTY_STRING;
    }

    /**
     * Determine tag Local ID based upon parent page.
     */
    private String getAssetRecipesTag() {
        SniPage parentPage = PageFactory.getSniPage(page.getParent());

        StringBuilder baseSb = new StringBuilder()
            .append(page.getBrand())
            .append(TAG_NAMESPACE_SUFFIX).append(NAMESPACE_SEPARATOR);
        StringBuilder facetSb = new StringBuilder()
            .append(baseSb)
            .append(TAG_FACET).append(FORWARD_SLASH)
            .append(page.getPageType()).append(FORWARD_SLASH)
            .append(parentPage.getPageType());

        return facetSb.delete(0, baseSb.length()).toString();
    }

    /**
     * Determine tag Local ID based upon talent, show and source, if any.
     */
    private String getRecipeTag() {
        Tag talentTag = null;
        Tag showTag = null;
        Tag sourceTag = null;

        StringBuilder baseSb = new StringBuilder()
            .append(page.getBrand())
            .append(TAG_NAMESPACE_SUFFIX).append(NAMESPACE_SEPARATOR);
        StringBuilder facetSb = new StringBuilder()
            .append(baseSb)
            .append(TAG_FACET).append(FORWARD_SLASH)
            .append(page.getPageType());

        StringBuilder peopleSb = new StringBuilder();
        StringBuilder talentSb = new StringBuilder();
        StringBuilder showSb = new StringBuilder();
        StringBuilder sourceSb = new StringBuilder();

        StringBuilder tagSb = new StringBuilder();

        Recipe recipe = new RecipeFactory()
            .withSniPage(page)
            .build();
        SniPage talentPage = recipe.getRelatedTalentPage();
        SniPage showPage = recipe.getRelatedShowPage();
        String source = page.getProperties().get(PagePropertyConstants.PROP_SNI_SOURCE, String.class);

        if (source != null) {
            SniTag sourceSniTag = new SniTagFactory()
                .withSniPage(page)
                .withTagText(source)
                .build();
            if (sourceSniTag != null) {
                sourceSb.append(facetSb)
                    .append(FORWARD_SLASH).append("sources").append(FORWARD_SLASH)
                    .append(sourceSniTag.getFacet());
                sourceTag = tagManager.resolve(sourceSb.toString());
            }
        }
        if (talentPage != null) {
            peopleSb.append(facetSb)
                .append(FORWARD_SLASH).append(talentPage.getPageType());
            talentSb.append(peopleSb)
                .append(FORWARD_SLASH)
                .append(talentPage.getName());
            talentTag = tagManager.resolve(talentSb.toString());
        }
        if (showPage != null) {
            showSb.append(facetSb)
                .append(FORWARD_SLASH).append(showPage.getPageType());
            showTag = tagManager.resolve(showSb.toString());
        }

        /* Talent-specific tag? (e.g. Food Network Kitchens) */
        if (talentTag != null) {
            tagSb = talentSb;
        /* Source-specific tag? (e.g. Food Netowrk Magazine */
        } else if (sourceTag != null) {
            tagSb = sourceSb;
        /* Any related talent? */
        } else if (talentPage != null) {
            tagSb = peopleSb;
        /* Any related show? */
        } else if (showTag != null) {
            tagSb = showSb;
        /* Associated with no talent, source or show? */
        } else {
            tagSb = facetSb;
        }

        return tagSb.delete(0, baseSb.length()).toString();
    }
}
