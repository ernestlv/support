package com.scrippsnetworks.wcm.episode;

import com.scrippsnetworks.wcm.episode.impl.EpisodeImpl;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * @author Jason Clark
 *         Date: 8/6/13
 */
public class EpisodeFactory {

    /** SniPage of the episode in hand. */
    private SniPage sniPage;

    /** Build a new Episode given an SniPage. */
    public Episode build() {
        if (sniPage != null) {
            return new EpisodeImpl(sniPage);
        }
        return null;
    }

    /** Add an SniPage to this builder. */
    public EpisodeFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }
}
