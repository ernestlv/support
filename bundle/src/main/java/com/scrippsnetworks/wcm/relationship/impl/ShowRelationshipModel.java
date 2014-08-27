package com.scrippsnetworks.wcm.relationship.impl;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.relationship.RelationshipModel;

/**
 * @author Jason Clark
 *         Date: 8/7/13
 */
public class ShowRelationshipModel implements RelationshipModel {

    private static final Logger log = LoggerFactory.getLogger(ShowRelationshipModel.class);

    private static final String SNI_ASSET_TYPE_EPISODE = "EPISODE";
    private static final String SNI_PRIMARY_TALENT = "sni:primaryTalent";
    private static final String SNI_PAGELINKS = "sni:pageLinks";
    private static final String SNI_CONTENT_PATH = "/content";
    private static final String SNI_ASSET_PATH = "/etc/sni-asset";
    private static final String JCR_CONTENT = "jcr:content";

    private SniPage showSniPage;
    private Resource resource;
    private ResourceResolver resourceResolver;
    private PageManager pageManager;

    /** Episode pages for this show. */
    private List<SniPage> episodePages;
    
    /** Boolean variable to ensure the same query will not be run multiple times per page load with an empty data set. */
    private boolean queriedForEpisodePages = false;

    /** Construct a new ShowRelationshipModel from an SniPage. */
    public ShowRelationshipModel(final SniPage showSniPage) {
        this.showSniPage = showSniPage;
        if (showSniPage != null) {
            pageManager = showSniPage.getPageManager();
            resource = showSniPage.getContentResource();
            if (resource != null) {
                resourceResolver = resource.getResourceResolver();
            }
        }
    }


    @Override
    public List<SniPage> getRecipePages() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Resource> getRecipeAssets() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /** {@inheritDoc} */
    @Override
    public SniPage getPrimaryTalent() {
        SniPage primaryTalent = null;
        if (showSniPage != null) {
            String propTalent = showSniPage.getProperties().get(SNI_PRIMARY_TALENT, String.class);
            if (propTalent != null) {
                primaryTalent = getPageFromPath(propTalent);
            }
        }
        return primaryTalent;
    }

    @Override
    public List<SniPage> getTalentPages() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Resource> getTalentAssets() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /** {@inheritDoc} */
    @Override
    public List<SniPage> getEpisodePages() {
        if (!queriedForEpisodePages && episodePages == null && showSniPage != null) {
            queriedForEpisodePages = true;
            QueryManager queryManager;
            try {
                queryManager = resource.adaptTo(Node.class).getSession().getWorkspace().getQueryManager();
                StringBuilder query = new StringBuilder();
                query
                    .append("/jcr:root")
                    .append(showSniPage.getPath().trim())
                    .append("//element(*, cq:PageContent) [@sni:assetType='")
                    .append(SNI_ASSET_TYPE_EPISODE)
                    .append("']");
                Query compiledQuery = queryManager.createQuery(query.toString(), Query.XPATH);
                NodeIterator nodeItr = compiledQuery.execute().getNodes();
                while (nodeItr.hasNext()) {
                    Node node = nodeItr.nextNode();
                    if (node != null) {
                        SniPage episodePage = PageFactory.getSniPage(pageManager, node.getParent().getPath());
                        if (episodePage != null) {
                            episodePages.add(episodePage);
                        }
                    }
                }
            } catch (RepositoryException re) {
                log.error("RepositoryException caught: {}", re);
            } catch (Exception e) {
                log.error("Exception caught: {}", e);
            }
        }
        return episodePages;
    }

    @Override
    public List<Resource> getEpisodeAssets() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<SniPage> getShowPages() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Resource> getShowAssets() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Resource> getScheduleAssets() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
    /** {@inheritDoc */
    @Override
    public List<SniPage> getCompanyPages() {
        return null;
    }


	@Override
	public String[] getMealTypeRecipes() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
     * Convenience method for getting an SniPage from an asset path via the pageLinks.
     */
    public SniPage getPageFromPath(String assetPath) {
        SniPage sniPage = null;
        if (StringUtils.isNotBlank(assetPath)) {
        	Resource pageResource = null;
            if (StringUtils.startsWith(assetPath, SNI_ASSET_PATH) && resourceResolver != null) {
                Resource assetResource = resourceResolver.resolve(assetPath + "/" + JCR_CONTENT);
                if (assetResource != null) {
                    ValueMap assetProperties = assetResource.adaptTo(ValueMap.class);
                    if (assetProperties != null && assetProperties.containsKey(SNI_PAGELINKS)) {
                        String[] pageLinks = assetProperties.get(SNI_PAGELINKS, String[].class);
                        if (pageLinks != null && pageLinks.length > 0) {
                            String pagePath = pageLinks[0];
                            if (StringUtils.isNotBlank(pagePath)) {
                                pageResource = resourceResolver.resolve(pagePath);
                            }
                        }
                    }
                }
            } else if (StringUtils.startsWith(assetPath, SNI_CONTENT_PATH) && resourceResolver != null) {
               pageResource = resourceResolver.resolve(assetPath);
            }
            if (pageResource != null) {
                Page page = pageResource.adaptTo(Page.class);
                if (page != null) {
                    sniPage = PageFactory.getSniPage(page);
                }
            }
        }
        return sniPage;
    }
}
