package com.scrippsnetworks.wcm.seo.impl;

import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import com.scrippsnetworks.wcm.seo.SeoTitle;
import com.scrippsnetworks.wcm.seo.SeoProperty;
import com.scrippsnetworks.wcm.snitag.SniTag;
import com.scrippsnetworks.wcm.snitag.SniTagFactory;
import com.scrippsnetworks.wcm.util.PageTypes;
import org.apache.commons.lang3.text.StrSubstitutor;

import com.day.cq.wcm.api.Page;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;

import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.EnumSet;

/**
 * @author Jonathan Bell
 * Date: 8/15/2013
 */
public class SeoTitleImpl implements SeoTitle {

    private static final Logger log = LoggerFactory.getLogger(SeoTitle.class);    

    private static final String EMPTY_STRING = "";
    private static final String FORWARD_SLASH = "/";
    private static final String TAG_NAMESPACE_SUFFIX = "-strings";
    private static final String TAG_FACET = "page-title";
    private static final String NAMESPACE_SEPARATOR = ":";
    private static final String EMPTY_CRUMB = ": :";
    private static final String EMPTY_CRUMB_START = "^: ";
    private static final String EMPTY_CRUMB_END = " :$";
    private static final String EMPTY_COMMA = ", :";
    private static final String COLLAPSED_COMMA = " :";
    private static final String COLLAPSED_CRUMB = ":";

    private SniPage page;
    private HashMap pageValues;
    private StrSubstitutor subber;
    private TagManager tagManager;

    public SeoTitleImpl(SniPage page) {
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

    public String getTitle() {
        if (page == null) {
            return EMPTY_STRING;
        }

        String tagLocalId = TAG_FACET + FORWARD_SLASH + page.getPageType();
        String title = EMPTY_STRING;

        ValueMap pageProperties = page.getProperties();
        if (pageProperties.containsKey(PagePropertyConstants.PROP_SNI_SEOTITLE)) {
            String pageTitle = pageProperties.get(PagePropertyConstants.PROP_SNI_SEOTITLE).toString();
            if (StringUtils.isNotEmpty(pageTitle)) {
                title = pageTitle;
            }
        }

        if (StringUtils.isEmpty(title) && tagManager != null) {        
            PageTypes type = PageTypes.findPageType(page.getPageType());
            if (type != null) {
                switch (type) {
                    case ASSET_RECIPES:
                        tagLocalId = getAssetRecipesTag();
                        break;
                    default:
                        tagLocalId = TAG_FACET + FORWARD_SLASH + page.getPageType();
                        break;
                }
                title = subber.replace(getSubstitutionFormat(tagLocalId));
            }
        }

        if (StringUtils.isEmpty(title)) {
            title = page.getTitle();
        }

        return cleanString(title);
    }

    /**
     * Clean a string:
     * - Remove HTML markup
     * - Replace empty blocks (ex: "Great Things : : Big Scripps" with "Great Things : Big Scripps")
     */
    private String cleanString(String dirtyText) {
        String cleanText;

        if (StringUtils.isNotEmpty(dirtyText)) {
            cleanText = Jsoup.parse(dirtyText).text()
                .replace(EMPTY_CRUMB, COLLAPSED_CRUMB)
                .replace(EMPTY_COMMA, COLLAPSED_COMMA)
                .replaceFirst(EMPTY_CRUMB_START, EMPTY_STRING)
                .replaceFirst(EMPTY_CRUMB_END, EMPTY_STRING);
        } else {
            cleanText = EMPTY_STRING;
        }

        return cleanText;
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
     * Request format string given a local ID,
     * typically provided as "page-title/pagetype"
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

}
