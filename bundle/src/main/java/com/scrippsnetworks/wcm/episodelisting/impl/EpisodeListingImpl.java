package com.scrippsnetworks.wcm.episodelisting.impl;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.episodelisting.EpisodeListing;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.show.Show;
import com.scrippsnetworks.wcm.show.ShowFactory;

/**
 * 
 * @author Mallik Vamaraju Date: 8/15/13
 */

public class EpisodeListingImpl implements EpisodeListing {
	
	private static final Logger log = LoggerFactory
			.getLogger(EpisodeListingImpl.class);
	
	/** ValueMap of properties merged from episode listing page and asset. */
	private ValueMap episodelistingProperties;
	
	/** ResourceResolver for convenience. */
	private ResourceResolver resourceResolver;
	
	/** SniPage of episode listing used to create this object. */
	private SniPage sniPage;
	
	/** Member for Show which is parent of this Episode listing. */
	private Show show;
	
	private static final String SHOW = "show";
	
	/** Construct a new EpisodeImpl given an SniPage. */
	public EpisodeListingImpl(SniPage sniPage) {
		this.sniPage = sniPage;
		Resource resource = sniPage.getContentResource();
		if (resource != null) {
			resourceResolver = resource.getResourceResolver();
		}
		
	}
	
	/** {@inheritDoc} */
	@Override
	public Show getShow() {
		if (show == null) {
			Page parentPage = sniPage.getParent();
			if (parentPage != null) {
				SniPage parentSniPage = PageFactory.getSniPage(parentPage);
				if (parentSniPage.getPageType().equals(SHOW))
					show = new ShowFactory().withSniPage(parentSniPage).build();
			}
			
		}
		return show;
		
	}
	
}
