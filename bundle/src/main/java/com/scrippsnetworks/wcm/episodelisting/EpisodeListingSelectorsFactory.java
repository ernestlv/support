/**
 * 
 */
package com.scrippsnetworks.wcm.episodelisting;

import com.scrippsnetworks.wcm.episodelisting.impl.EpisodeListingSelectorsImpl;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * @author Mallik Vamaraju Date : 10/22/2013
 */
public class EpisodeListingSelectorsFactory {
	
	private SniPage sniPage;
	
	public EpisodeListingSelectorsFactory withSniPage(SniPage sniPage) {
		this.sniPage = sniPage;
		return this;
	}
	
	public EpisodeListingSelectors build() {
		if (sniPage != null) {
			return new EpisodeListingSelectorsImpl(sniPage);
		}
		return null;
		
	}
	
}
