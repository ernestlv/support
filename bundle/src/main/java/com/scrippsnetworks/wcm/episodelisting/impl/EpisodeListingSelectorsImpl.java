/**
 * 
 */
package com.scrippsnetworks.wcm.episodelisting.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.episodelisting.EpisodeListingSelectors;

/**
 * @author Mallik Vamaraju Date : 10/22/2013
 * 
 */
public class EpisodeListingSelectorsImpl implements EpisodeListingSelectors

{
	
	private static final Logger log = LoggerFactory
			.getLogger(EpisodeListingSelectorsImpl.class);
	
	/** SniPage used to construct this Player with selectors. */
	private SniPage sniPage;
	
	/** selectors passed to the Episode listing page */
	
	private List<String> selectors;
	
	/* default the pageIndex to 1 */
	private int pageIndex = 1;
	
	/* default the seriesIndex to 0 */
	private int seriesIndex = 0;
	
	private boolean isAllSeasons = false;
	
	private static final String ALL_SEASONS = "all-seasons";
	
	/** Constructor for accessing selectors for episode listing page. */
	public EpisodeListingSelectorsImpl(final SniPage page) {
		this.sniPage = page;
		selectors = sniPage.getSelectors();
	}
	
	/** {@inheritDoc} */
	@Override
	public int getPageIndex() {
		
		if (!selectors.isEmpty() && selectors.size() > 1) {
			String[] pageSelector = selectors.get(1).split("-");
			if (pageSelector.length > 1) {
				try {
					pageIndex = Integer.parseInt(pageSelector[1]);
				} catch (NumberFormatException e) {
					log.error("Number Format Exception in PageIndex of Episode Listing Page.");
				}
			}
			
		}
		
		return pageIndex;
		
	}
	
	/** {@inheritDoc} */
	@Override
	public int getSeriesIndex() {
		
		if (!selectors.isEmpty() && selectors.size() > 0) {
			String[] seriesSelector = selectors.get(0).split("-");
			if (seriesSelector.length > 1) {
				try {
					seriesIndex = Integer.parseInt(seriesSelector[1]);
				} catch (NumberFormatException e) {
					log.error("Number Format Exception in Series Index of Episode Listing Page.");
				}
			}
			
		}
		
		return seriesIndex;
		
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean isAllSeasons()
	
	{
		if (!selectors.isEmpty() && selectors.size() > 0) {
			String allSeasons = selectors.get(0);
			if (allSeasons.equals(ALL_SEASONS))
				isAllSeasons = true;
		}
		
		return isAllSeasons;
	}
	
}
