package com.scrippsnetworks.wcm.episode.impl;

import com.day.cq.wcm.api.Page;

import com.scrippsnetworks.wcm.episode.Episode;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.relationship.RelationshipModel;
import com.scrippsnetworks.wcm.relationship.RelationshipModelFactory;
import com.scrippsnetworks.wcm.series.Series;
import com.scrippsnetworks.wcm.series.SeriesFactory;
import com.scrippsnetworks.wcm.show.Show;
import com.scrippsnetworks.wcm.show.ShowFactory;
import com.scrippsnetworks.wcm.snipackage.SniPackage;
import com.scrippsnetworks.wcm.hub.Hub;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jason Clark
 *         Date: 8/6/13
 */
public class EpisodeImpl implements Episode {


    private static final String SNI_EPISODE_NO = "sni:episodeNo";
    private static final String SNI_DESCRIPTION = "sni:description";
    private static final String SNI_IMAGE = "sni:image";
    private static final String SNI_SHORT_TITLE = "sni:shortTitle";
    private static final String SNI_EPISODE_TYPE = "sni:episodeType";

    private static final String BANNER_SIMPLE_RES_TYPE = "/apps/sni-food/components/modules/banner-simple";
    private static final String BANNER_CUSTOM_RES_TYPE = "/apps/sni-food/components/modules/banner-custom";
    private static final String REFERENCE_RES_TYPE = "foundation/components/reference";
    private static final String REFERENCE_PATH = "path";
    public static final String SUPERLEAD_SUB_NAVIGATION = "jcr:content/superlead/sub_navigation";
    

    /** ResourceResolver for convenience. */
    private ResourceResolver resourceResolver;

    /** SniPage of episode used to create this object. */
    private SniPage sniPage;

    /** Member for list of recipe pages related to this episode. */
    private List<SniPage> relatedRecipePages;

    /** Member for Show to which this Episode belongs. */
    private SniPage relatedShowPage;

    /** Member for previous episode SniPage. */
    private SniPage previousEpisodePage;

    /** Member for next episode SniPage. */
    private SniPage nextEpisodePage;

    /** Episode number from the asset, sni:episodeNo. */
    private String episodeNumber;

    /** For storing the RelationshipModel for this Episode. */
    private RelationshipModel episodeRelationshipModel;

    /** ValueMap of properties merged from episode page and asset. */
    private ValueMap episodeProperties;

    /** Member for Series which contains this Episode. */
    private Series series;

    /** Member for Title of this Episode. */
    private String title;

    /** Member for Description of this Episode. */
    private String description;

    /** List of Company SniPages associated to this Episode. */
    private List<SniPage> companyPages;

    /** The primary talent associated to this Episode. */
    private SniPage primaryTalentPage;

    /** Path to image in the DAM for this Episode. */
    private String imagePath;
    
    /** Short Title from the asset, sni:shortTitle */
    private String shortTitle;
    
    /** Episdoe Type from the asset, sni:episodeType */
    private String episodeType;
    
    /** Tracks if this episode inherited a show banner via package */
    private boolean showBannerInherited = false;
    private boolean checkedForShowBannerInheritance = false;

    /** Tracks if this episode inherited a subNav */
    private boolean showSubNavInherited = false;
    private boolean checkedForShowSubNavInheritance = false;

    /** Construct a new EpisodeImpl given an SniPage. */
    public EpisodeImpl(SniPage sniPage) {
        this.sniPage = sniPage;
        this.episodeProperties = sniPage.getProperties();
        Resource resource = sniPage.getContentResource();
        if (resource != null) {
            resourceResolver = resource.getResourceResolver();
        }
    }

    /** Convenience method for retrieving relationship model. */
    private RelationshipModel getEpisodeRelationshipModel() {
        if (episodeRelationshipModel == null) {
            episodeRelationshipModel = new RelationshipModelFactory()
                    .withSniPage(sniPage)
                    .build();
        }
        return episodeRelationshipModel;
    }

    /** Convenience method for setting previousEpisode and nextEpisode. */
    private void setNextAndPreviousEpisodes() {
        Series series = getSeries();
        if (series != null) {
            List<Episode> seriesEpisodes = series.getEpisodes();
            String episodeNo = getEpisodeNumber();
            if (seriesEpisodes != null && episodeNo != null) {
                int seriesSize = seriesEpisodes.size();
                for (int i = 0; i < seriesSize; i++) {
                    if (seriesEpisodes.get(i).getEpisodeNumber() != null && seriesEpisodes.get(i).getEpisodeNumber().equals(episodeNo)) {
                        if (i != 0) {
                            previousEpisodePage = seriesEpisodes.get(i - 1).getSniPage();
                        }
                        if (nextEpisodePage == null && (seriesSize - 1) >= (i + 1)) {
                            nextEpisodePage = seriesEpisodes.get(i + 1).getSniPage();
                        }
                    }
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public SniPage getSniPage() {
        return sniPage;
    }

    /** {@inheritDoc} */
    @Override
    public List<SniPage> getRecipePages() {
        if (relatedRecipePages == null) {
            relatedRecipePages = new ArrayList<SniPage>();
            RelationshipModel episodeModel = getEpisodeRelationshipModel();
            if (episodeModel != null) {
                List<SniPage> recipes = episodeModel.getRecipePages();
                if (recipes != null) {
                    relatedRecipePages.addAll(recipes);
                }
            }

        }
        return relatedRecipePages;
    }

    /** {@inheritDoc} */
    @Override
    public SniPage getRelatedShowPage() {
        if (relatedShowPage == null) {
            RelationshipModel episodeModel = getEpisodeRelationshipModel();
            if (episodeModel != null) {
                List<SniPage> showPages = episodeModel.getShowPages();
                if (showPages != null && showPages.size() > 0) {
                    relatedShowPage = showPages.get(0);
                }
            }
        }
        return relatedShowPage;
    }

    /** {@inheritDoc} */
    @Override
    public SniPage getPreviousEpisodePage() {
        if (previousEpisodePage == null) {
            setNextAndPreviousEpisodes();
        }
        return previousEpisodePage;
    }

    /** {@inheritDoc} */
    @Override
    public SniPage getNextEpisodePage() {
        if (nextEpisodePage == null) {
            setNextAndPreviousEpisodes();
        }
        return nextEpisodePage;
    }

    /** {@inheritDoc} */
    @Override
    public String getEpisodeNumber() {
        if (episodeNumber == null) {
            if (episodeProperties != null && episodeProperties.containsKey(SNI_EPISODE_NO)) {
                episodeNumber = episodeProperties.get(SNI_EPISODE_NO, String.class);
            }
        }
        return episodeNumber;
    }

    /** {@inheritDoc} */
    @Override
    public SniPage getPrimaryTalentPage() {
        if (primaryTalentPage == null) {
            SniPage showPage = getRelatedShowPage();
            if (showPage != null) {
                Show show = new ShowFactory()
                        .withSniPage(showPage)
                        .build();
                if (show != null) {
                    primaryTalentPage = show.getTalentPage();
                }
            }
        }
        return primaryTalentPage;
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        if (title == null) {
            title = sniPage.getTitle();
        }
        return title;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        if (description == null) {
            if (episodeProperties != null
                    && episodeProperties.containsKey(SNI_DESCRIPTION)) {
                description = episodeProperties.get(SNI_DESCRIPTION, String.class);
            }
        }
        return description;
    }

    /** {@inheritDoc} */
    @Override
    public String getImagePath() {
        if (imagePath == null) {
            if (episodeProperties != null && episodeProperties.containsKey(SNI_IMAGE)) {
                imagePath = episodeProperties.get(SNI_IMAGE, String.class);
            }
        }
        return imagePath;
    }

    
    /** {@inheritDoc} */
  @Override
  public List<SniPage> getCompanyPages() {
    if (companyPages == null) {
      companyPages = new ArrayList<SniPage>();
      RelationshipModel episodeModel = getEpisodeRelationshipModel();
      if (episodeModel != null) {
        companyPages = episodeModel.getCompanyPages();
      }

    }
    return companyPages;
  }

    /** {@inheritDoc} */
    @Override
    public Series getSeries() {
        if (series == null) {
            Page parentPage = sniPage.getParent();
            if (parentPage != null) {
                SniPage parentSniPage = PageFactory.getSniPage(parentPage);
                series = new SeriesFactory()
                        .withSniPage(parentSniPage)
                        .build();
            }
        }
        return series;
    }

    /** {@inheritDoc} */
    @Override
    public String getShortTitle() {
        if (shortTitle == null) {
            if (episodeProperties != null && episodeProperties.containsKey(SNI_SHORT_TITLE)) {
                shortTitle = episodeProperties.get(SNI_SHORT_TITLE, String.class);
            }
        }
        return shortTitle;
    }

    /** {@inheritDoc} */
    @Override
    public String getEpisodeType() {
        if (episodeType == null) {
            if (episodeProperties != null && episodeProperties.containsKey(SNI_EPISODE_TYPE)) {
                episodeType = episodeProperties.get(SNI_EPISODE_TYPE, String.class);
            }
        }
        return episodeType;
    }
    
    @Override
    public boolean isShowBannerInherited() {
        List<Resource> modules;
        Resource res;
        if (!checkedForShowBannerInheritance) {
            checkedForShowBannerInheritance = true;
            SniPackage sniPackage = sniPage.getSniPackage();
            if (sniPackage != null) {
                modules = sniPackage.getModules();
                for (int i = 0; i < modules.size() && !showBannerInherited; i++) {
                    res = modules.get(i);
                    if (isResourceType(res, BANNER_SIMPLE_RES_TYPE) || isResourceType(res, BANNER_CUSTOM_RES_TYPE)) {
                        showBannerInherited = true;
                    }
                }
            } 
            if (!showBannerInherited) {
                Hub hub = getSniPage().getHub();
                if (hub != null) {
                    modules = hub.getSharedModules();
                    for (int i = 0; i < modules.size() && !showBannerInherited; i++) {
                        res = modules.get(i);
                        if (isResourceType(res, BANNER_SIMPLE_RES_TYPE) || isResourceType(res, BANNER_CUSTOM_RES_TYPE)) {
                            showBannerInherited = true;
                        }
                    }
                }
            }
        }
        return showBannerInherited;
    }

    /**
     * discovers whether or not a parent show already has a hub navigation element
     *
     * implementation note: this does that by checking for a known structure of data, with specific name
     * this is not ideal, but specifying a structure of types too would be fragile.
     *
     * @return true if the parent show has navigation, hence any episode-page defined navigation should be supressed
     */
    @Override
    public boolean isSubNavInherited() {
        if(!checkedForShowSubNavInheritance) {
            checkedForShowSubNavInheritance = true;
            SniPage showPage = getRelatedShowPage();
            if(showPage != null && showPage.hasChild(SUPERLEAD_SUB_NAVIGATION)){
                showSubNavInherited = true;
            }
        }
        return showSubNavInherited;
    }
    
    /**Convenience method, could be refactored to a util class.
       If the resource is a reference component, it checks the resource type of what is being referenced.
       The exception to that is when you are checking if the resource is a reference component. In that case it proceeds normally. */
    private boolean isResourceType(Resource res, String resType) {
        String altType = resType.startsWith("/apps/") ? resType.replace("/apps/", "") : "/apps/" + resType;
        //If the module is a reference (but we aren't looking for one), follow it and then use that resource for the type checking.
        if (!resType.equals(REFERENCE_RES_TYPE) && res.isResourceType(REFERENCE_RES_TYPE)) { 
            ValueMap resProperties = res.adaptTo(ValueMap.class);
            if (resProperties != null && resProperties.containsKey(REFERENCE_PATH)) {
                String referencedPath = resProperties.get(REFERENCE_PATH, String.class);
                if (referencedPath != null && resourceResolver != null) {
                    Resource referencedRes = resourceResolver.getResource(referencedPath);
                    if (referencedRes != null) {
                        return referencedRes.isResourceType(resType) || referencedRes.isResourceType(altType);
                    }
                }
            }
        }
        return res.isResourceType(resType) || res.isResourceType(altType);
    }
}
