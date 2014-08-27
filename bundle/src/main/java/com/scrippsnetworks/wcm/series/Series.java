package com.scrippsnetworks.wcm.series;

import java.util.List;

import com.scrippsnetworks.wcm.episode.Episode;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * @author Jason Clark Date: 8/12/13
 */
public interface Series {

	/** A List of Episodes in this Series. */
	public List<Episode> getEpisodes();
	
	/** Returns the SniPage wrapped by this Series. */
    public SniPage getSniPage();

}
