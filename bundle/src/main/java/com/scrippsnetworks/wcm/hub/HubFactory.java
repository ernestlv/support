package com.scrippsnetworks.wcm.hub;

import com.scrippsnetworks.wcm.episode.Episode;
import com.scrippsnetworks.wcm.episode.EpisodeFactory;
import com.scrippsnetworks.wcm.hub.impl.HubImpl;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.util.PageTypes;

/**
 * @author Jason Clark
 *         Date: 5/16/13
 */
public class HubFactory {
    private SniPage sniPage;

    /**
     * Build a Hub.
     * @return Hub
     */
    public Hub build() {
        SniPage hubPage = getEffectivePage();
        Hub impl = new HubImpl(hubPage);
        boolean goodHub = (impl.getHubMaster() != null);
        return goodHub ? impl : null;
    }

    /**
     * Add an SniPage to your builder.
     * @param sniPage SniPage
     * @return this
     */
    public HubFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }

    private SniPage getEffectivePage() {
        SniPage realPage = sniPage;

        PageTypes type = PageTypes.findPageType(sniPage.getPageType());
        if (type != null) {
            switch (type) {
                case EPISODE:
                    Episode episode = new EpisodeFactory()
                        .withSniPage(sniPage)
                        .build();
                    if (episode != null) {
                        realPage = episode.getRelatedShowPage();
                    }
                    break;
                default:
                    break;
            }
        }

        return realPage; 
    }

}
