package com.scrippsnetworks.wcm.metadata;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.metadata.impl.BaseSponsorshipManager;

public class SponsorshipManagerFactory {

    public static SponsorshipManager getSponsorshipManager(SniPage page) {
        if (page == null) {
            return null;
        }

        return new BaseSponsorshipManager(page);

    }

}
