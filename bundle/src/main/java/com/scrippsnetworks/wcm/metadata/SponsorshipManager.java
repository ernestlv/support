package com.scrippsnetworks.wcm.metadata;

import java.util.List;

public interface SponsorshipManager {
    public String getEffectiveSponsorshipValue();
    public SponsorshipProvider getEffectiveSponsorshipProvider();
    public String getHubSponsorshipValue();
    public List<SponsorshipProvider> getAllSponsorshipProviders();
    public List<SponsorshipSource> getOrderedSources();
    public SponsorshipProvider getSponsorshipProvider(SponsorshipSource source);
}
