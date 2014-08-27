package com.scrippsnetworks.wcm.relationship.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.fnr.util.AssetRootPaths;
import com.scrippsnetworks.wcm.fnr.util.AssetSlingResourceTypes;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.relationship.RelationshipModel;

/**
 * @author Jason Clark
 *         Date: 6/29/13
 */
public class RecipeRelationshipModel extends RelationshipModelImpl implements RelationshipModel {

    private static final Logger log = LoggerFactory.getLogger(RecipeRelationshipModel.class);

    private static final String SNI_PAGELINKS = "sni:pageLinks";
    private static final String SNI_ASSETLINK = "sni:assetLink";
    private static final String SNI_PEOPLE = "sni:people";
    private static final String SLING_RESOURCETYPE = "sling:resourceType";
    private static final String SNI_PRIMARY_TALENT = "sni:primaryTalent";
    private static final String SNI_MIGRATED_PRIMARY_TALENT = "sni:assetPrimaryTalent";
    private static final String JCR_CONTENT = "jcr:content";
    private static final String SORT_ORDER_ASC = "ASC";
    private static final String SORT_ORDER_DESC = "DESC";

    /** Keep these objects around for working with Resources & Pages. */
    private PageManager pageManager;
    private Resource resource;
    private ResourceResolver resourceResolver;
    private SniPage sniPage;

    /** Member variables. */
    private List<SniPage> recipePages = new ArrayList<SniPage>();
    private List<Resource> recipeAssets = new ArrayList<Resource>();
    private List<SniPage> talentPages = new ArrayList<SniPage>();
    private List<Resource> talentAssets = new ArrayList<Resource>();
    private List<SniPage> episodePages = new ArrayList<SniPage>();
    private List<Resource> episodeAssets = new ArrayList<Resource>();
    private List<SniPage> showPages = new ArrayList<SniPage>();
    private List<Resource> showAssets = new ArrayList<Resource>();
    /** Boolean variable to ensure the same query will not be run multiple times per page load with an empty data set. */
    private boolean queriedForEpisodeAssets = false;

// constructors

    /** Build relationship model from SniPage. */
    public RecipeRelationshipModel(final SniPage sniPage) {
        if (sniPage != null) {
            this.sniPage = sniPage;
            this.pageManager = sniPage.getPageManager();
            recipePages.add(sniPage);
        }
    }

// private utility methods

    /** Convenience method for retrieving PageManager. */
    private PageManager getPageManager() {
        if (pageManager == null) {
            SniPage page = getSniPage();
            if (page != null) {
                pageManager = page.getPageManager();
            }

            if (pageManager == null) {
                Resource resource = getResource();
                if (resource != null) {
                    pageManager = resource.adaptTo(PageManager.class);
                }
            }

            if (pageManager == null) {
                ResourceResolver resolver = getResourceResolver();
                if (resolver != null) {
                    pageManager = resolver.adaptTo(PageManager.class);
                }
            }
        }
        return pageManager;
    }

    /** Convenience method for grabbing Resource. */
    private Resource getResource() {
        if (resource == null) {
            SniPage page = getSniPage();
            if (page != null) {
                resource = page.getContentResource();
            }

            if (resource == null) {
                ResourceResolver resolver = getResourceResolver();
                if (resolver != null) {
                    resource = resolver.adaptTo(Resource.class);
                }
            }
        }
        return resource;
    }

    /** Convenience method for grabbing ResourceResolver. */
    private ResourceResolver getResourceResolver() {
        if (resourceResolver == null) {
            Resource resource = getResource();
            if (resource != null) {
                resourceResolver = resource.getResourceResolver();
            }

            if (resourceResolver == null) {
                SniPage page = getSniPage();
                if (page != null) {
                    Resource pageResource = page.getContentResource();
                    if (pageResource != null) {
                        resourceResolver = pageResource.getResourceResolver();
                    }
                }
            }
        }
        return resourceResolver;
    }

    /** Convenience method for getting SniPage. */
    private SniPage getSniPage() {
        if (sniPage == null) {
            Resource resource = getResource();
            if (resource != null) {
                Page page = resource.adaptTo(Page.class);
                if (page != null) {
                    sniPage = PageFactory.getSniPage(page);
                }
            }
        }
        return sniPage;
    }

    /** Convenience method for retrieving talent pages from talent assets. */
    private SniPage talentPageFromTalentAsset(final Resource talentAsset) {
        SniPage talentPage = null;
        if (talentAsset != null) {
            Resource content = talentAsset.getChild(JCR_CONTENT);
            if (content != null) {
                ValueMap talentProps = content.adaptTo(ValueMap.class);
                if (talentProps != null && talentProps.containsKey(SNI_PAGELINKS)) {
                    String[] pageLinks = talentProps.get(SNI_PAGELINKS, String[].class);
                    if (pageLinks != null && pageLinks.length > 0) {
                        String pageLink = pageLinks[0];
                        if (StringUtils.isNotBlank(pageLink)) {
                            PageManager manager = getPageManager();
                            if (manager != null) {
                                Page page = manager.getPage(pageLink);
                                if (page != null) {
                                    talentPage = PageFactory.getSniPage(page);
                                }
                            }
                        }
                    }
                }
            }
        }
        return talentPage;
    }

// interface implementation methods

    /** {@inheritDoc} */
    @Override
    public List<SniPage> getRecipePages() {
        return recipePages;
    }

    /** {@inheritDoc */
    @Override
    public List<Resource> getRecipeAssets() {
        if (recipePages.size() > 0) {
            ValueMap recipeProps = recipePages.get(0).getProperties();
            if (recipeProps != null && recipeProps.containsKey(SNI_ASSETLINK)) {
                String assetLink = recipeProps.get(SNI_ASSETLINK, String.class);
                if (StringUtils.isNotBlank(assetLink)) {
                    ResourceResolver resolver = getResourceResolver();
                    if (resolver != null) {
                        Resource recipeAsset = resolver.getResource(assetLink);
                        if (recipeAsset != null) {
                            recipeAssets.add(recipeAsset);
                        }
                    }
                }
            }
        }
        return recipeAssets;
    }

    /** {@inheritDoc */
    @Override
    public List<SniPage> getTalentPages() {
        if (talentPages.size() == 0) {
            if (talentAssets.size() > 0) {
                Resource talentAsset = talentAssets.get(0);
                SniPage talentPage = talentPageFromTalentAsset(talentAsset);
                if (talentPage != null) {
                    talentPages.add(talentPage);
                }
            }
            if (talentPages.size() == 0 && recipePages.size() > 0) {
                SniPage recipe = recipePages.get(0);
                ValueMap recipeProps = recipe.getProperties();
                if (recipeProps.containsKey(SNI_PEOPLE)) {
                    String[] people = recipeProps.get(SNI_PEOPLE, String[].class);
                    if (people != null && people.length > 0) {
                        String personPath = people[0].trim();
                        ResourceResolver resolver = getResourceResolver();
                        if (resolver != null) {
                            Resource personResource = resolver.getResource(personPath);
                            if (personResource != null) {
                                if (!talentAssets.contains(personResource)) {
                                    talentAssets.add(personResource);
                                }
                                SniPage talentPage = talentPageFromTalentAsset(personResource);
                                if (talentPage != null) {
                                    talentPages.add(talentPage);
                                }
                            }
                        }
                    }
                }
            }
        }
        return talentPages;
    }

    /** {@inheritDoc */
    @Override
    public List<Resource> getTalentAssets() {
        return talentAssets;
    }

    /** {@inheritDoc */
    @Override
    public List<SniPage> getEpisodePages() {
        if (episodePages.size() == 0) {
            List<Resource> episodeAssets = getEpisodeAssets();
            PageManager manager = getPageManager();
            if (episodeAssets != null && episodeAssets.size() > 0 && manager != null) {
                for (Resource asset : episodeAssets) {
                    ValueMap props = asset.adaptTo(ValueMap.class);
                    if (props.containsKey(SNI_PAGELINKS)) {
                        String[] pageLinks = props.get(SNI_PAGELINKS, String[].class);
                        if (pageLinks != null && pageLinks.length > 0) {
                            for (String link : pageLinks) {
                                Page page = manager.getPage(link);
                                if (page != null) {
                                    SniPage sniPage = PageFactory.getSniPage(page);
                                    if (sniPage != null) {
                                        episodePages.add(sniPage);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return episodePages;
    }

    /** {@inheritDoc */
    @Override
    public List<Resource> getEpisodeAssets() {
        if (!queriedForEpisodeAssets && episodeAssets.isEmpty()) {
            queriedForEpisodeAssets = true;
            List<Resource> recipeAssets = getRecipeAssets();
            if (recipeAssets.size() > 0) {
                Resource recipeAsset = recipeAssets.get(0);
                String recipePath = recipeAsset.getPath();
                String rootPath = AssetRootPaths.SHOWS.path();
                String assetResourceType = AssetSlingResourceTypes.EPISODE.resourceType();
                List<Resource> foundEpisodes = searchForAssets(
                        recipeAsset, rootPath, assetResourceType, recipePath, null, null
                );
                if (foundEpisodes != null) {
                    episodeAssets.addAll(foundEpisodes);
                }
            }
        }
        return episodeAssets;
    }

    /** {@inheritDoc */
    @Override
    public List<SniPage> getShowPages() {
        if (showPages.size() == 0) {
            List<SniPage> episodePages = getEpisodePages();
            if (episodePages.size() > 0) {
                for (SniPage episode : episodePages) {
                    Page series = episode.getParent();
                    if (series != null) {
                        Page show = series.getParent();
                        if (show != null) {
                            SniPage showSniPage = PageFactory.getSniPage(show);
                            if (showSniPage != null && showSniPage.getPageType().equals("show")) {
                                showPages.add(showSniPage);
                            }
                        }
                    }
                }
            }
        }
        return showPages;
    }

    /** {@inheritDoc */
    @Override
    public List<Resource> getShowAssets() {
        return showAssets;
    }

    /** {@inheritDoc */
    @Override
    public List<Resource> getScheduleAssets() {
        return null;
    }
    
    /** {@inheritDoc */
    @Override
    public List<SniPage> getCompanyPages() {
        return null;
    }
    
    @Override
    public SniPage getPrimaryTalent() {
        SniPage primaryTalent = null;
        if (sniPage != null) {
            Page primaryTalentCqPage = null;
            String propTalent = sniPage.getProperties().get(SNI_PRIMARY_TALENT, sniPage.getProperties().get(SNI_MIGRATED_PRIMARY_TALENT, String.class));
            if (propTalent != null) {
                primaryTalentCqPage = pageManager.getPage(propTalent);
            }
            if (primaryTalentCqPage != null) {
                primaryTalent = PageFactory.getSniPage(primaryTalentCqPage);
            }
        }
        return primaryTalent;
    }

	@Override
	public String[] getMealTypeRecipes() {
		// TODO Auto-generated method stub
		return null;
	}
}
