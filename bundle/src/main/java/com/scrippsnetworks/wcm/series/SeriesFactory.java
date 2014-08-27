package com.scrippsnetworks.wcm.series;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.series.impl.SeriesImpl;

/**
 * @author Jason Clark
 *         Date: 8/12/13
 */
public class SeriesFactory {

    /** SniPage of a Series. */
    private SniPage sniPage;


    /** Build a Series object given an SniPage. */
    public Series build() {
        if (sniPage != null) {
            return new SeriesImpl(sniPage);
        }
        return null;
    }


    /** Add an SniPage to this builder. */
    public SeriesFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }
}
