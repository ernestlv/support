package com.scrippsnetworks.wcm.export.snipage.content.impl;

import java.util.List;

import com.scrippsnetworks.wcm.episode.Episode;
import com.scrippsnetworks.wcm.episode.EpisodeFactory;
import com.scrippsnetworks.wcm.page.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.company.Company;
import com.scrippsnetworks.wcm.company.CompanyFactory;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * This class generates the Company page specific properties.
 * 
 * @author Venkata Naga Sudheer Donaboina
 * 
 */
public class CompanyExport extends SniPageExport {

    private static final Logger LOG = LoggerFactory.getLogger(CompanyExport.class);

    public enum ExportProperty {

        COMPANY_EMAIL(String.class),
        COMPANY_TOLLFREE(String.class),
        COMPANY_PHONE(String.class),
        COMPANY_FAX(String.class),
        COMPANY_LONGITUDE(String.class),
        COMPANY_LATITUDE(String.class),
        COMPANY_EPISODES(String[].class),
        COMPANY_ADDRESS1(String.class),
        COMPANY_ADDRESS2(String.class),
        COMPANY_ADDRESS3(String.class),
        COMPANY_CITY(String.class),
        COMPANY_STATE(String.class),
        COMPANY_STATE_FULL_NAME(String.class),
        COMPANY_ZIP(String.class),
        COMPANY_WEBSITE(String.class);

        final Class clazz;

        ExportProperty(Class clazz) {
            this.clazz = clazz;
        }

        public Class valueClass() {
            return clazz;
        }
    }

    private final Company company;

    public CompanyExport(SniPage sniPage) {
        super(sniPage);
        this.company = new CompanyFactory().withSniPage(sniPage).build();
        initialize();
    }

    protected CompanyExport(SniPage sniPage, Company company) {
        super(sniPage);
        this.company = company;
        initialize();
    }

    public void initialize() {

        LOG.debug("Started Company Export overrides");

        if (sniPage == null || !sniPage.hasContent() || company == null) {
            return;
        }

        setProperty(ExportProperty.COMPANY_EMAIL.name(), company.getEmail());

        setProperty(ExportProperty.COMPANY_TOLLFREE.name(), company.getTollFreePhone());

        setProperty(ExportProperty.COMPANY_PHONE.name(), company.getPhone());

        setProperty(ExportProperty.COMPANY_FAX.name(), company.getFax());

        setProperty(ExportProperty.COMPANY_LONGITUDE.name(), company.getLongitude());

        setProperty(ExportProperty.COMPANY_LATITUDE.name(), company.getLatitude());

        setProperty(ExportProperty.COMPANY_ADDRESS1.name(), company.getAddress1());
        
        setProperty(ExportProperty.COMPANY_ADDRESS2.name(), company.getAddress2());
        
        setProperty(ExportProperty.COMPANY_ADDRESS3.name(), company.getAddress3());
        
        setProperty(ExportProperty.COMPANY_CITY.name(), company.getCity());
        
        setProperty(ExportProperty.COMPANY_STATE.name(), company.getState());
        
        setProperty(ExportProperty.COMPANY_STATE_FULL_NAME.name(), company.getStateFullName());
        
        setProperty(ExportProperty.COMPANY_ZIP.name(), company.getZip());
        
        setProperty(ExportProperty.COMPANY_WEBSITE.name(), company.getCompanyUrl());

        List < SniPage > episodes = company.getEpisodePages();

        if (episodes != null && episodes.size() > 0) {
            List < String > episodePageIds = getSniPageIds(episodes);
            if (episodePageIds != null && episodePageIds.size() > 0) {
                setProperty(ExportProperty.COMPANY_EPISODES.name(),
                        episodePageIds.toArray(new String[episodePageIds.size()]));

                // use episode to find Show relationship
                Episode episode = new EpisodeFactory().withSniPage(episodes.get(0)).build();
                if(episode!= null){
                    SniPage show = episode.getRelatedShowPage();
                    if(show!=null)
                        setProperty(SniPageExport.ExportProperty.CORE_SHOW_ID.name(), show.getUid());
                }

            }
        }

        LOG.debug("Completed Company Export overrides");

    }
}
