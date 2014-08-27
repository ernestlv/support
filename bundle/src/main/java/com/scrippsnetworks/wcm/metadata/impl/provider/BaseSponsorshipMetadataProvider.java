package com.scrippsnetworks.wcm.metadata.impl.provider;

import java.lang.String;
import java.util.List;
import java.util.Arrays;
import java.lang.IllegalArgumentException;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.metadata.MetadataProvider;
import com.scrippsnetworks.wcm.metadata.MetadataProperty;
import com.scrippsnetworks.wcm.metadata.SponsorshipManager;
import com.scrippsnetworks.wcm.hub.Hub;

public class BaseSponsorshipMetadataProvider implements MetadataProvider {

    private static final String EMPTY_SPONSOR = "";
    private SniPage page;
    private SniPage masterPage;
    private String sponsorship = null;
    private String hubsponsor = null;
    private Hub hub;

    public BaseSponsorshipMetadataProvider(SniPage page) {
        if (page == null) {
            throw new IllegalArgumentException("page must not be null");
        }

        if (page.getDepth() < 1) {
            throw new IllegalArgumentException("page must be at least at site level");
        }

        SponsorshipManager sm = page.getSponsorshipManager();
        if (sm != null) {
            sponsorship = sm.getEffectiveSponsorshipValue();
            hubsponsor = sm.getHubSponsorshipValue();
        }
    }

    public List<MetadataProperty> provides() {
        return Arrays.asList(MetadataProperty.SPONSORSHIP, MetadataProperty.HUBSPONSOR);
    }

    public String getProperty(MetadataProperty prop) {
        if (prop == null) {
            return null;
        }
        String retVal = null;

        switch (prop) {
            case SPONSORSHIP:
                retVal = sponsorship != null ? sponsorship : EMPTY_SPONSOR;
                break;
            case HUBSPONSOR:
                retVal = hubsponsor != null ? hubsponsor : EMPTY_SPONSOR;
                break;
            default:
                throw new IllegalArgumentException("invalid property");
        }

        return retVal;

    }
}
