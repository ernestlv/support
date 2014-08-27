/**
 * 
 */
package com.scrippsnetworks.wcm.episodelisting;

/**
 * @author Mallik Vamaraju Date : 10/22/2013
 */
public interface EpisodeListingSelectors {
	
	/** return pageIndex for the episode listing page */
	public int getPageIndex();
	
	/** return seriesIndex for the episode listing page */
	public int getSeriesIndex();
	
	/**
	 * return true when the selector has all-seasons for the episode listing
	 * page
	 */
	public boolean isAllSeasons();
	
}
