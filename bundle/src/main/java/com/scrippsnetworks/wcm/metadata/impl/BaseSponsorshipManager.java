package com.scrippsnetworks.wcm.metadata.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import com.scrippsnetworks.wcm.metadata.SponsorshipManager;
import com.scrippsnetworks.wcm.metadata.SponsorshipSource;
import com.scrippsnetworks.wcm.metadata.SponsorshipProvider;
import com.scrippsnetworks.wcm.snipackage.SniPackage;
import com.scrippsnetworks.wcm.hub.Hub;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.page.PagePropertyConstants;

public class BaseSponsorshipManager implements SponsorshipManager {

    public static List<SponsorshipSource> baseSources = Arrays.asList(SponsorshipSource.HUBPACKAGE, SponsorshipSource.PACKAGE, SponsorshipSource.HUB, SponsorshipSource.PAGE);

    SniPage page;
    private List<SponsorshipSource> sources;
    private HashMap<SponsorshipSource, SponsorshipProvider> sponsorshipMap = new HashMap<SponsorshipSource, SponsorshipProvider>();


    public BaseSponsorshipManager(SniPage page) {
        this(page, baseSources);
    }

    protected BaseSponsorshipManager(SniPage page, List<SponsorshipSource> sources) {
        if (page == null) {
            throw new IllegalArgumentException("must provide a page");
        }

        if (sources == null) {
            throw new IllegalArgumentException("must provide sources");
        }

        this.page = page;
        this.sources = sources;

        init();
    }

    private void init() {

        Page pageSponsor;
        Page hubSponsor;
        Page hubPackageSponsor;
        Page packageSponsor;


        pageSponsor = getSponsorship(page);
        if (pageSponsor != null) {
            sponsorshipMap.put(SponsorshipSource.PAGE,
                    new SponsorshipProvider(SponsorshipSource.PAGE, page, pageSponsor));
        }

        SniPackage sniPackage = page.getSniPackage();
        if (sniPackage != null) {
            Map<SniPackage.PackageRelation, SniPage> packageRelations = sniPackage.getAllPackageRelations();

            SniPage anchor;

            if (packageRelations.containsKey(SniPackage.PackageRelation.HUB)) {
                anchor = packageRelations.get(SniPackage.PackageRelation.HUB);
                hubPackageSponsor = getSponsorship(anchor);
                if (hubPackageSponsor != null) {
                    sponsorshipMap.put(SponsorshipSource.HUBPACKAGE, new SponsorshipProvider(SponsorshipSource.HUBPACKAGE, anchor, hubPackageSponsor));
                }
            }

            if (packageRelations.containsKey(SniPackage.PackageRelation.DIRECT)) {
                anchor = packageRelations.get(SniPackage.PackageRelation.DIRECT);
                packageSponsor = getSponsorship(anchor);
                if (packageSponsor!= null) {
                    sponsorshipMap.put(SponsorshipSource.PACKAGE, new SponsorshipProvider(SponsorshipSource.PACKAGE, anchor, packageSponsor));
                }
            }
        }

        Hub hub = page.getHub();
        if (hub != null) {
            SniPage hubMaster = hub.getHubMaster();
            hubSponsor = getSponsorship(hubMaster);
            if (hubSponsor != null) {
                sponsorshipMap.put(SponsorshipSource.HUB,
                    new SponsorshipProvider(SponsorshipSource.HUB, hubMaster, hubSponsor));
            }
        }
    }

    protected static Page getSponsorship(Page thisPage) {
        Page retVal = null;

        if (thisPage == null) {
            return null;
        }

        String path = thisPage.getProperties().get(PagePropertyConstants.PROP_SNI_SPONSORSHIP, String.class);

        if (path == null || path.isEmpty()) {
            return null;
        }

        PageManager pm = thisPage.getPageManager();
        if (pm != null) {
            retVal = pm.getPage(path);
            // isValid checks startTime/endTime, used for campaign management
            if (retVal != null && !retVal.isValid()) {
                retVal = null;
            }
        }
        return retVal;
    }

    public String getEffectiveSponsorshipValue() {
        String retVal = null;
        SponsorshipProvider p = getEffectiveSponsorshipProvider();
        if (p != null) {
            retVal = p.getSponsorshipValue();
        }
        return retVal;
    }

    public SponsorshipProvider getEffectiveSponsorshipProvider() {
        SponsorshipProvider retVal = null;
        for (SponsorshipSource s : sources) {
            SponsorshipProvider p = sponsorshipMap.get(s);
            if (p != null) {
                retVal = p;
                break;
            }
        }
        return retVal;
    }

    public String getHubSponsorshipValue() {
        String retVal = null;
        for (SponsorshipSource s : Arrays.asList(SponsorshipSource.HUBPACKAGE, SponsorshipSource.HUB)) {
            SponsorshipProvider p = sponsorshipMap.get(s);
            if (p != null) {
                retVal = p.getSponsorshipValue();
            }
        }
        return retVal;
    }

    public SponsorshipProvider getSponsorshipProvider(SponsorshipSource source) {
        return sponsorshipMap.get(source);
    }

    public List<SponsorshipProvider> getAllSponsorshipProviders() {
        List<SponsorshipProvider> retVal = new ArrayList<SponsorshipProvider>();
        for (SponsorshipSource s : sources) {
            SponsorshipProvider p = sponsorshipMap.get(s);
            if (p != null) {
                retVal.add(p);
            }
        }
        return retVal;
    }

    public List<SponsorshipSource> getOrderedSources() {
        return Collections.unmodifiableList(sources);
    }
}
