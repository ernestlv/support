package com.scrippsnetworks.wcm.episode;

import com.scrippsnetworks.wcm.page.SniPage;
import org.apache.sling.api.resource.ValueMap;

import java.util.Comparator;

/**
 * @author Jason Clark
 *         Date: 8/7/13
 */
public class EpisodeNumberComparator implements Comparator<Episode> {

    private static final String SNI_EPISODE_NO = "sni:episodeNo";

    /** {@inheritDoc} */
    @Override
    public int compare(final Episode episode, final Episode episode2) {
        if (episode != null && episode2 != null) {
            String no1 = episode.getEpisodeNumber();
            String no2 = episode2.getEpisodeNumber();
            if (no1 != null && no2 != null) {
                return no1.compareTo(no2);
            } else if (no1 == null && no2 != null) {
                return Integer.MAX_VALUE;
            } else {
                return Integer.MIN_VALUE;
            }
        } else if (episode == null && episode2 != null) {
            return Integer.MAX_VALUE;
        } else {
            return Integer.MIN_VALUE;
        }
    }

}
