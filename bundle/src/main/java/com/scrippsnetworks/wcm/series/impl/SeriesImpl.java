package com.scrippsnetworks.wcm.series.impl;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.episode.Episode;
import com.scrippsnetworks.wcm.episode.EpisodeFactory;
import com.scrippsnetworks.wcm.episode.EpisodeNumberComparator;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.series.Series;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Jason Clark Date: 8/12/13
 */
public class SeriesImpl implements Series {

	private static final String EPISODE_TYPE = "episode";

	/** Episodes in this Series. */
	private List<Episode> episodes;
	
	/** SniPage of Series used to create this object. */
    private SniPage sniPage;

	public SeriesImpl(final SniPage sniPage) {
		
		this.sniPage = sniPage;
		if (sniPage != null) {
			Iterator<Page> children = sniPage.listChildren();
			if (children != null) {
				episodes = new ArrayList<Episode>();
				while (children.hasNext()) {
					Page page = children.next();
					if (page != null) {
						SniPage episodePage = PageFactory.getSniPage(page);
						if (episodePage != null
								&& episodePage.getPageType().equals(
										EPISODE_TYPE)) {
							Episode episode = new EpisodeFactory().withSniPage(
									episodePage).build();
							episodes.add(episode);
						}
					}
				}
				if (episodes.size() > 0) {
					Collections.sort(episodes, new EpisodeNumberComparator());
				}
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public List<Episode> getEpisodes() {
		return episodes;
	}

	@Override
	public SniPage getSniPage() {
		return sniPage;
	}

}
