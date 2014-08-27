package com.scrippsnetworks.wcm.episodelisting;

import com.scrippsnetworks.wcm.show.Show;

/**
 * @author Mallik Vamaraju Date: 8/15/13
 */

public interface EpisodeListing {
	
	/** Find the Show page associated with this episode listing. */
	
	public Show getShow();
	
}
