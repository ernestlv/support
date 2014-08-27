package com.scrippsnetworks.wcm.relationship.impl;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.relationship.RelationshipTypes;
import com.scrippsnetworks.wcm.relationship.RelationshipModel;
import com.scrippsnetworks.wcm.fnr.util.AssetRootPaths;
import com.scrippsnetworks.wcm.fnr.util.AssetSlingResourceTypes;
import com.scrippsnetworks.wcm.util.PageTypes;
import com.scrippsnetworks.wcm.cache.RelationshipModelCacheService;
import com.scrippsnetworks.wcm.fnr.util.OsgiHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jason Clark
 *         Date: 6/29/13
 * Updated Venkata Naga Sudheer Donaboina
 * Date: 8/20/2013
 */
public class RelationshipModelImpl implements RelationshipModel {

    private static final Logger log = LoggerFactory.getLogger(RelationshipModelImpl.class);

    private static final String SNI_PAGELINKS = "sni:pageLinks";
    private static final String SNI_ASSETLINK = "sni:assetLink";
    private static final String SNI_PEOPLE = "sni:people";
    private static final String SLING_RESOURCETYPE = "sling:resourceType";
    private static final String SNI_PRIMARY_TALENT = "sni:primaryTalent";
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
    private List<SniPage> menuPages = new ArrayList<SniPage>();
    private List<Resource> menuAssets = new ArrayList<Resource>();
    /** Boolean variable to ensure the same query will not be run multiple times per page load with an empty data set. */
    private boolean queriedForEpisodeAssets = false;

// constructors

	public RelationshipModelImpl() {

	}

    /** Build relationship model on either asset path or page path. */
    public RelationshipModelImpl(final ResourceResolver resolver,
                                 final String path,
                                 final RelationshipTypes type) {
        if (resolver != null) {
            this.resourceResolver = resolver;
        }
    }

    /** Build relationship model from SniPage. */
    public RelationshipModelImpl(final SniPage sniPage) {
        if (sniPage != null) {
            this.sniPage = sniPage;
            this.pageManager = sniPage.getPageManager();
            String pageTypeText = sniPage.getPageType();
            PageTypes pageType = PageTypes.findPageType(pageTypeText);
            if (pageType != null) {
                switch (pageType) {
                    case RECIPE:
                        recipePages.add(sniPage);
                        break;
                    case EPISODE:
                        episodePages.add(sniPage);
                        break;
                    case SHOW:
                        showPages.add(sniPage);
                        break;
                    case TALENT:
                        showPages.add(sniPage);
                        break;
                    case MENU:
                    	menuPages.add(sniPage);
                    default:
                        break;
                }
            }
        }
    }

    /** Build relationship model with a Resource and type. */
    public RelationshipModelImpl(final Resource resource, final RelationshipTypes type) {
        if (resource != null) {
            this.resource = resource;
        }
    }

// private utility methods

    /** Dig around in JCR for assets.
     *
     * @param resource
     * @param pathToAssetRoot
     * @param resourceType
     * @param searchTerm
     * @param sortKey
     * @param sortOrder
     * @return
     */
    protected List<Resource> searchForAssets(final Resource resource,
                                           final String pathToAssetRoot,
                                           final String resourceType,
                                           final String searchTerm,
                                           final String sortKey,
                                           final String sortOrder) {
        if (resource == null
                || pathToAssetRoot == null
                || resourceType == null) {
            return null;
        }
        String altResourceType = resourceType.startsWith("/apps/") ? resourceType.replace("/apps/", "") : "/apps/" + resourceType;
        OsgiHelper osgiHelper = new OsgiHelper();
        try {
            RelationshipModelCacheService relationCacheService = osgiHelper.getOsgiService(RelationshipModelCacheService.class.getName());
			List<String> paths = relationCacheService.searchForAssets(resource,pathToAssetRoot,resourceType,searchTerm,sortKey,sortOrder);
            List<Resource> assets = new ArrayList<Resource>();
            for (String path : paths) {
				Resource nodeResource = resource.getResourceResolver().getResource(path);
                if (nodeResource != null) {
                    assets.add(nodeResource);
                }
            }
            return assets;
        } catch (Exception e) {
            log.error("Exception caught: ", e);
            return null;
        }
    }

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
            ValueMap talentProps = talentAsset.adaptTo(ValueMap.class);
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
            ValueMap recipeProps = recipePages.get(0).adaptTo(ValueMap.class);
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
                        String personPath = people[0];
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
        return episodePages;
    }

    /** {@inheritDoc */
    @Override
    public List<Resource> getEpisodeAssets() {
        if (!queriedForEpisodeAssets && episodeAssets.isEmpty()) {
            queriedForEpisodeAssets = true;
            if (recipePages.size() > 0) {
                Resource resource = getResource();
                if (resource != null) {
                    SniPage recipePage = recipePages.get(0);
                    String recipePath = recipePage.getPath();
                    String rootPath = AssetRootPaths.SHOWS.path();
                    String assetResourceType = AssetSlingResourceTypes.EPISODE.resourceType();
                    List<Resource> foundEpisodes = searchForAssets(
                            resource, rootPath, assetResourceType, recipePath, null, null
                    );
                    if (foundEpisodes != null) {
                        episodeAssets.addAll(foundEpisodes);
                    }
                }
            }
        }
        return episodeAssets;
    }
    
    /** {@inheritDoc */
    @Override
    public List<SniPage> getShowPages() {
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
            String propTalent = sniPage.getProperties().get(SNI_PRIMARY_TALENT, String.class);
            if (propTalent != null) {
                primaryTalentCqPage = pageManager.getPage(propTalent);
            }
            if (primaryTalentCqPage != null) {
                primaryTalent = PageFactory.getSniPage(primaryTalentCqPage);
            }
        }
        return primaryTalent;
    }

    /** {@inheritDoc} */
	@Override
	public String[] getMealTypeRecipes() {
		return null;
	}
}
