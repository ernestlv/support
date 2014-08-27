package com.scrippsnetworks.wcm.relationship.impl;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.fnr.util.AssetRootPaths;
import com.scrippsnetworks.wcm.fnr.util.AssetSlingResourceTypes;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.relationship.RelationshipModel;

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

import com.day.cq.wcm.api.PageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jason Clark Date: 8/6/13
 */
public class EpisodeRelationshipModel extends RelationshipModelImpl implements RelationshipModel {

	private static final Logger log = LoggerFactory
			.getLogger(EpisodeRelationshipModel.class);

	private static final String SHOW_PAGE_TYPE = "show";
	private static final String SNI_RECIPES = "sni:recipes";
	private static final String SNI_PAGE_LINKS = "sni:pageLinks";
	private static final String JCR_CONTENT_PATH = "/jcr:content";
	private static final String SORT_ORDER_ASC = "ASC";
	private static final String SORT_ORDER_DESC = "DESC";
	private static final String SLING_RESOURCETYPE = "sling:resourceType";
	private static final String JCR_CONTENT = "jcr:content";
	private static final long Query_Limit= 10;

	/** Resource for convenience, because you need a Resource from time to time. */
	private Resource resource;

	/**
	 * ResourceResolver for convenience, because you need a resource resolver
	 * from time to time.
	 */
	private ResourceResolver resourceResolver;

	/** SniPage for Episode. */
	private SniPage episodePage;

	private SniPage sniPage;

	/** Merged ValueMap from Episode page and asset. */
	private ValueMap episodeProperties;

	/** Member for related Recipe pages. */
	private List<SniPage> recipePages;

	/** Member for related Show page(s). */
	private List<SniPage> showPages;
        
        /** Member for related Company page(s). */
        private List<SniPage> companyPages;
        
        /** Boolean variable to ensure the same query will not be run multiple times per page load with an empty data set. */
        private boolean queriedForCompanyPages = false;

	/** Create a new relationship model for Episode page. */
	public EpisodeRelationshipModel(SniPage sniPage) {
		if (sniPage != null) {
			episodePage = sniPage;
			episodeProperties = sniPage.getProperties();
			resource = sniPage.getContentResource();
                        companyPages = null;
			if (resource != null) {
				resourceResolver = resource.getResourceResolver();
			}
		}
	}

	private PageManager pageManager;

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

	/** {@inheritDoc} */
	@Override
	public List<SniPage> getRecipePages() {
		if (recipePages == null) {
			recipePages = new ArrayList<SniPage>();
			if (episodeProperties != null
					&& episodeProperties.containsKey(SNI_RECIPES)) {
				String[] episodeRecipeAssetPaths = episodeProperties.get(
						SNI_RECIPES, String[].class);
				if (episodeRecipeAssetPaths != null) {
					for (String path : episodeRecipeAssetPaths) {
						SniPage recipePage = getPageFromAsset(path);
						if (recipePage != null) {
							recipePages.add(recipePage);
						}
					}
				}
			}
		}
		return recipePages;
	}

	/** {@inheritDoc} */
	@Override
	public List<SniPage> getCompanyPages() {
		String episodePath = episodePage.getPath();
		String rootPath = AssetRootPaths.RESTAURANTS.path();
		String assetResourceType = AssetSlingResourceTypes.COMPANY
				.resourceType();
		ResourceResolver resolver = getResourceResolver();
		Resource episodeAsset = resolver.getResource(episodePath);
		List<Resource> episodeResources = searchForAssets(episodeAsset, rootPath,
				assetResourceType, episodePath, null, null);
		List<SniPage> foundEpisodes = new ArrayList<SniPage>();
		pageManager = episodePage.getPageManager();
		for(Resource item : episodeResources) {
			SniPage companyPage = PageFactory.getSniPage(pageManager,
                   item.getParent().getPath());
			foundEpisodes.add(companyPage);
		}
		return foundEpisodes;
	}

	@Override
	public List<Resource> getRecipeAssets() {
		return null; // To change body of implemented methods use File |
						// Settings | File Templates.
	}

	@Override
	public SniPage getPrimaryTalent() {
		return null; // To change body of implemented methods use File |
						// Settings | File Templates.
	}

	@Override
	public List<SniPage> getTalentPages() {
		return null; // To change body of implemented methods use File |
						// Settings | File Templates.
	}

	@Override
	public List<Resource> getTalentAssets() {
		return null; // To change body of implemented methods use File |
						// Settings | File Templates.
	}

	@Override
	public List<SniPage> getEpisodePages() {
		return null; // To change body of implemented methods use File |
						// Settings | File Templates.
	}

	@Override
	public List<Resource> getEpisodeAssets() {
		return null; // To change body of implemented methods use File |
						// Settings | File Templates.
	}

	/** {@inheritDoc} */
	@Override
	public List<SniPage> getShowPages() {
		if (showPages == null) {
			showPages = new ArrayList<SniPage>();
			if (episodePage != null) {
				Page series = episodePage.getParent();
				if (series != null) {
					Page show = series.getParent();
					if (show != null) {
						SniPage showSniPage = PageFactory.getSniPage(show);
						if (showSniPage != null) {
							String pageType = showSniPage.getPageType();
							if (StringUtils.isNotBlank(pageType)
									&& pageType.equals(SHOW_PAGE_TYPE)) {
								showPages.add(showSniPage);
							}
						}
					}
				}
			}
		}
		return showPages;
	}

	@Override
	public List<Resource> getShowAssets() {
		return null; // To change body of implemented methods use File |
						// Settings | File Templates.
	}

	@Override
	public List<Resource> getScheduleAssets() {
		return null; // To change body of implemented methods use File |
						// Settings | File Templates.
	}

	/**
	 * Convenience method for getting an SniPage from an asset path via the
	 * pageLinks.
	 */
	private SniPage getPageFromAsset(final String assetPath) {
		SniPage recipeSniPage = null;
		if (StringUtils.isNotBlank(assetPath)) {
			if (resourceResolver != null) {
				Resource assetResource = resourceResolver.resolve(assetPath+ JCR_CONTENT_PATH);
			if (assetResource != null) {
					ValueMap assetProperties = assetResource.adaptTo(ValueMap.class);
					if (assetProperties != null && assetProperties.containsKey(SNI_PAGE_LINKS)) {
						String[] pageLinks = assetProperties.get(SNI_PAGE_LINKS, String[].class);
						if (pageLinks != null) {
							String pagePath = pageLinks[0];
							if (StringUtils.isNotBlank(pagePath)) {
								Resource pageResource = resourceResolver.resolve(pagePath);
								if (pageResource != null) {
									Page recipePage = pageResource.adaptTo(Page.class);
								if (recipePage != null) {
									recipeSniPage = PageFactory.getSniPage(recipePage);
									}
								}
							}
						}
					}
				}
			}
		}
		return recipeSniPage;
	}

	@Override
	public String[] getMealTypeRecipes() {
		// TODO Auto-generated method stub
		return null;
	}

}
