package com.scrippsnetworks.wcm.episodelisting;

import com.scrippsnetworks.wcm.episodelisting.impl.EpisodeListingImpl;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * 
 * @author Mallik Vamaraju Date: 8/15/13
 */
public class EpisodeListingFactory {
	
	private SniPage sniPage;
	
	public EpisodeListing build() {
		if (sniPage != null) {
			return new EpisodeListingImpl(sniPage);
		}
		return null;
		
	}
	
	public EpisodeListingFactory withSniPage(SniPage sniPage) {
		this.sniPage = sniPage;
		return this;
	}
	
}
