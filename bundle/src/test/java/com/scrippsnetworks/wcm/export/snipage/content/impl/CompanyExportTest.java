package com.scrippsnetworks.wcm.export.snipage.content.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.company.Company;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * This class is used for testing Company Exports.
 * @author Venkata Naga Sudheer Donaboina
 */
public class CompanyExportTest {

    public static final String PAGE_PATH = "/content/food/company/a-company";
    public static final String PAGE_TYPE = "company";
    public static final String COMPANY_EMAIL = "abc@abc.com";
    public static final String COMPANY_TOLLFREE = "123-456-7890";
    public static final String COMPANY_PHONE = "987-654-3210";
    public static final String COMPANY_LONGITUDE = "-84.05847929999999";
    public static final String COMPANY_LATITUDE = "35.89565169999999";
    public static final String COMPANY_FAX = "987-098-0987";
    public static final String COMPANY_WEBSITE = "http://www.foodnetwork.com";

    public static final String COMPANY_ADDRESS1 = "123 Lincoln Street";
    public static final String COMPANY_ADDRESS2 = "Apt 12";
    public static final String COMPANY_ADDRESS3 = "West";

    public static final String COMPANY_CITY = "Knoxville";
    public static final String COMPANY_STATE = "TN";
    public static final String COMPANY_STATE_FULL_NAME = "Tennessee";
    public static final String COMPANY_ZIP = "37912";

    public static final String EPISODE_PAGE1_UID = "aaaa-bbbb-cccc-dddd";
    public static final String EPISODE_PAGE2_UID = "aaaa-cccc-bbbb-dddd";
    public static final String EPISODE_PAGE3_UID = "aaaa-bbbb-dddd-cccc";

    @Mock
    SniPage companyPage;
    @Mock
    Company company;

    @Mock
    Resource companyPageCR;
    @Mock
    ValueMap companyPageProperties;

    @Mock
    PageManager pageManager;
    @Mock
    ResourceResolver resourceResolver;

    @Mock
    SniPage episodePage1;
    @Mock
    SniPage episodePage2;
    @Mock
    SniPage episodePage3;

    List < SniPage > episodePages;

    @Before
    public void before() {

        MockitoAnnotations.initMocks(this);

        when(companyPage.hasContent()).thenReturn(true);
        when(companyPage.getProperties()).thenReturn(companyPageProperties);
        when(companyPage.getContentResource()).thenReturn(companyPageCR);
        when(companyPage.getPath()).thenReturn(PAGE_PATH);
        when(companyPage.getPageType()).thenReturn(PAGE_TYPE);

        when(companyPage.getPageManager()).thenReturn(pageManager);

        when(company.getEmail()).thenReturn(COMPANY_EMAIL);

        when(company.getTollFreePhone()).thenReturn(COMPANY_TOLLFREE);

        when(company.getPhone()).thenReturn(COMPANY_PHONE);

        when(company.getFax()).thenReturn(COMPANY_FAX);

        when(company.getLongitude()).thenReturn(COMPANY_LONGITUDE);

        when(company.getLatitude()).thenReturn(COMPANY_LATITUDE);

        when(company.getCompanyUrl()).thenReturn(COMPANY_WEBSITE);

        when(company.getAddress1()).thenReturn(COMPANY_ADDRESS1);

        when(company.getAddress2()).thenReturn(COMPANY_ADDRESS2);

        when(company.getAddress3()).thenReturn(COMPANY_ADDRESS3);

        when(company.getCity()).thenReturn(COMPANY_CITY);

        when(company.getState()).thenReturn(COMPANY_STATE);
        
        when(company.getStateFullName()).thenReturn(COMPANY_STATE_FULL_NAME);

        when(company.getZip()).thenReturn(COMPANY_ZIP);

    }

    /** set up episode pages, episode page uid rlated to companies. */
    private void setupEpisodePages() {
        episodePages = Arrays.asList(episodePage1, episodePage2, episodePage3);
        when(company.getEpisodePages()).thenReturn(episodePages);

        when(episodePage1.getUid()).thenReturn(EPISODE_PAGE1_UID);
        when(episodePage2.getUid()).thenReturn(EPISODE_PAGE2_UID);
        when(episodePage3.getUid()).thenReturn(EPISODE_PAGE3_UID);

    }

    @Test
    public void testCompanyPropertyValues() {
        CompanyExport companyExport = new CompanyExport(companyPage, company);
        ValueMap exportProps = companyExport.getValueMap();

        assertEquals(CompanyExport.ExportProperty.COMPANY_EMAIL.name(), COMPANY_EMAIL, exportProps.get(
                CompanyExport.ExportProperty.COMPANY_EMAIL.name(),
                CompanyExport.ExportProperty.COMPANY_EMAIL.valueClass()));

        assertEquals(CompanyExport.ExportProperty.COMPANY_TOLLFREE.name(), COMPANY_TOLLFREE, exportProps.get(
                CompanyExport.ExportProperty.COMPANY_TOLLFREE.name(),
                CompanyExport.ExportProperty.COMPANY_TOLLFREE.valueClass()));

        assertEquals(CompanyExport.ExportProperty.COMPANY_PHONE.name(), COMPANY_PHONE, exportProps.get(
                CompanyExport.ExportProperty.COMPANY_PHONE.name(),
                CompanyExport.ExportProperty.COMPANY_PHONE.valueClass()));

        assertEquals(CompanyExport.ExportProperty.COMPANY_FAX.name(), COMPANY_FAX, exportProps.get(
                CompanyExport.ExportProperty.COMPANY_FAX.name(), CompanyExport.ExportProperty.COMPANY_FAX.valueClass()));

        assertEquals(CompanyExport.ExportProperty.COMPANY_LONGITUDE.name(), COMPANY_LONGITUDE, exportProps.get(
                CompanyExport.ExportProperty.COMPANY_LONGITUDE.name(),
                CompanyExport.ExportProperty.COMPANY_LONGITUDE.valueClass()));

        assertEquals(CompanyExport.ExportProperty.COMPANY_LATITUDE.name(), COMPANY_LATITUDE, exportProps.get(
                CompanyExport.ExportProperty.COMPANY_LATITUDE.name(),
                CompanyExport.ExportProperty.COMPANY_LATITUDE.valueClass()));

        assertEquals(CompanyExport.ExportProperty.COMPANY_WEBSITE.name(), COMPANY_WEBSITE, exportProps.get(
                CompanyExport.ExportProperty.COMPANY_WEBSITE.name(),
                CompanyExport.ExportProperty.COMPANY_WEBSITE.valueClass()));

        assertEquals(CompanyExport.ExportProperty.COMPANY_ADDRESS1.name(), COMPANY_ADDRESS1, exportProps.get(
                CompanyExport.ExportProperty.COMPANY_ADDRESS1.name(),
                CompanyExport.ExportProperty.COMPANY_ADDRESS1.valueClass()));

        assertEquals(CompanyExport.ExportProperty.COMPANY_ADDRESS2.name(), COMPANY_ADDRESS2, exportProps.get(
                CompanyExport.ExportProperty.COMPANY_ADDRESS2.name(),
                CompanyExport.ExportProperty.COMPANY_ADDRESS2.valueClass()));

        assertEquals(CompanyExport.ExportProperty.COMPANY_ADDRESS3.name(), COMPANY_ADDRESS3, exportProps.get(
                CompanyExport.ExportProperty.COMPANY_ADDRESS3.name(),
                CompanyExport.ExportProperty.COMPANY_ADDRESS3.valueClass()));

        assertEquals(CompanyExport.ExportProperty.COMPANY_CITY.name(), COMPANY_CITY, exportProps.get(
                CompanyExport.ExportProperty.COMPANY_CITY.name(),
                CompanyExport.ExportProperty.COMPANY_CITY.valueClass()));

        assertEquals(CompanyExport.ExportProperty.COMPANY_STATE.name(), COMPANY_STATE, exportProps.get(
                CompanyExport.ExportProperty.COMPANY_STATE.name(),
                CompanyExport.ExportProperty.COMPANY_STATE.valueClass()));
        
        assertEquals(CompanyExport.ExportProperty.COMPANY_STATE_FULL_NAME.name(), COMPANY_STATE_FULL_NAME, exportProps.get(
                CompanyExport.ExportProperty.COMPANY_STATE_FULL_NAME.name(),
                CompanyExport.ExportProperty.COMPANY_STATE_FULL_NAME.valueClass()));

        assertEquals(CompanyExport.ExportProperty.COMPANY_ZIP.name(), COMPANY_ZIP, exportProps.get(
                CompanyExport.ExportProperty.COMPANY_ZIP.name(), CompanyExport.ExportProperty.COMPANY_ZIP.valueClass()));

    }

    @Test
    public void testCompanyEpisodes() {
        setupEpisodePages();
        CompanyExport companyExport = new CompanyExport(companyPage, company);
        ValueMap exportProps = companyExport.getValueMap();

        String[] episodes = exportProps.get(CompanyExport.ExportProperty.COMPANY_EPISODES.name(), String[].class);

        assertEquals(CompanyExport.ExportProperty.COMPANY_EPISODES.name(), episodePages.size(), episodes.length);

        int i = 0;
        for (SniPage sniPage : company.getEpisodePages()) {
            assertEquals("Episode Page", sniPage.getUid(), episodes[i++]);
        }

    }

}
