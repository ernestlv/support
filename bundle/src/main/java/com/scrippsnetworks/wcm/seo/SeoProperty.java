package com.scrippsnetworks.wcm.seo;

import com.scrippsnetworks.wcm.config.SiteConfigService;
import com.scrippsnetworks.wcm.episode.Episode;
import com.scrippsnetworks.wcm.episode.EpisodeFactory;
import com.scrippsnetworks.wcm.fnr.util.OsgiHelper;
import com.scrippsnetworks.wcm.metadata.MetadataProperty;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.Recipe;
import com.scrippsnetworks.wcm.recipe.RecipeFactory;
import com.scrippsnetworks.wcm.relationship.RelationshipModelFactory;
import com.scrippsnetworks.wcm.util.PageTypes;
import org.apache.commons.lang.StringUtils;
import java.util.EnumSet;
import java.util.List;

public enum SeoProperty {
    city("CITY"),
    description(""),
    effectiveSearchTerm(""),
    inheritedTitle(""),
    keywords("KEYWORDS"),
    packageName("PACKAGENAME"),
    searchTerms("SEARCHTERMS"),
    section("SECTION"),
    showName(""),
    siteTitle(""),
    source("SOURCE"),
    sponsorship("SPONSORSHIP"),
    state("STATE"),
    talentName("TALENTNAME"),
    title("TITLE");

    public static final EnumSet<SeoProperty> METADATA_PROPERTIES = EnumSet.of(city, keywords, packageName, searchTerms,
        section, source, sponsorship, state, talentName, title);
    public static final EnumSet<SeoProperty> DERIVED_PROPERTIES = EnumSet.of(description, inheritedTitle, showName,
        siteTitle, effectiveSearchTerm);

    private String metadataName;
    private SiteConfigService siteConfig;

    private static final String EMPTY_STRING = "";
    private static final String PROP_BRAND_SITE_TITLE = "brand.siteTitle";

    SeoProperty(String metadataName) {
        this.metadataName = metadataName;
    }

    public String getMetadataName() {
        return metadataName;
    }

    public String getPropertyValue(SniPage page) {
        String seoProperty = EMPTY_STRING;
        setSiteConfigService(page);

        if (page != null && siteConfig != null) {
            if (METADATA_PROPERTIES.contains(this)) {
                seoProperty = page.getMetadataManager().get(MetadataProperty.valueOf(metadataName));
            } else if (DERIVED_PROPERTIES.contains(this)) {
                switch (this) { 
                    case description:
                        seoProperty = page.getDescription();
                        break;
                    case inheritedTitle:
                        seoProperty = getInheritedTitle(page);
                        if (StringUtils.isEmpty(seoProperty)) {
                            seoProperty = page.getTitle();
                        }
                        break;
                    case showName:
                        seoProperty = getShowName(page);
                        break;
                    case siteTitle:
                        seoProperty = siteConfig.getBrandSiteTitle();
                        break;
                    case effectiveSearchTerm:
                        seoProperty = getEffectiveSearchTerm(page);
                        break;
                    default:
                        break;
                }
            }
        }

        return seoProperty != null ? seoProperty : EMPTY_STRING;
    }

    private void setSiteConfigService(SniPage page) {
        OsgiHelper osgiHelper;

        if (siteConfig == null) {
            osgiHelper = new OsgiHelper();
            if (osgiHelper != null) {
                siteConfig = osgiHelper.getOsgiServiceBySite(SiteConfigService.class.getName(), page.getBrand());
            }
        }
    }

    private String getInheritedTitle(SniPage page) {
        String hubTitle = EMPTY_STRING;
        String packageTitle = EMPTY_STRING;

        if (page.getHub() != null && page.getHub().getHubMaster() != null) {
            hubTitle = page.getHub().getHubMaster().getTitle();
        }
        packageTitle = page.getMetadataManager().get(MetadataProperty.PACKAGENAME);

        return StringUtils.isEmpty(hubTitle) ? packageTitle : hubTitle;
    }

    private String getShowName(SniPage page) {
        String showName = EMPTY_STRING;
        SniPage assetPage = null;
        SniPage showPage = null;

        PageTypes type = PageTypes.findPageType(page.getPageType());
        if (type != null) {
            switch (type) {
                case ASSET_RECIPES:
                    assetPage = PageFactory.getSniPage(page.getParent());
                    showName = getShowName(assetPage);
                    break;
                case EPISODE:
                    Episode episode = new EpisodeFactory()
                        .withSniPage(page)
                        .build();
                    showPage = episode.getRelatedShowPage();
                    if (showPage != null) {
                        showName = showPage.getTitle();
                    }
                    break;
                case RECIPE:
                    Recipe recipe = new RecipeFactory()
                        .withSniPage(page)
                        .build();
                    showPage = recipe.getRelatedShowPage();
                    if (showPage != null) {
                        showName = showPage.getTitle();
                    }
                    break;
                case SHOW:
                    showName = page.getTitle();
                    break;
                default:
                    break;
            }
        }

        return showName;
    }

    /** Returns the effective search term, after any DYM corrections.
     *
     * Searches can be made using corrections made on the search backend. In this case, the "no search results" metadata
     * property will indicate a DYM response and provide the requested and corrected terms.
     *
     * @param page The current page.
     * @return String the effective search term
     */
    private String getEffectiveSearchTerm(SniPage page) {
        // MetadataProperty.SEARCHTERMS is only set on the first request with a search term.
        // Metadataproperty.KEYTERM is maintained as the user clicks through pages, adds/removes filters, etc.
        String retVal = page.getMetadataManager().get(MetadataProperty.KEYTERM);
        String noSearchResults = page.getMetadataManager().get(MetadataProperty.NOSEARCHRESULTS);
        if (noSearchResults != null) {
            if (noSearchResults.startsWith("dym")) {
                String[] noSearchResultsParts = noSearchResults.split(":");
                if (noSearchResultsParts.length == 3) {
                    retVal = noSearchResultsParts[2];
                }
            }
        }
        return retVal;
    }
}

