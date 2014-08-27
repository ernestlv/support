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
import com.scrippsnetworks.wcm.episode.Episode;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.series.Series;

/**
 * @author Venkata Naga Sudheer Donaboina
 * 
 *  Tests the EpisodeExport class, whose job it is to extract
 *         export values from an SniPage specific to episodes.
 */
public class EpisodeExportTest {
	
	public static final String EPISODE_NUMBER ="BM0103H";
	public static final String PAGE_PATH = "/content/food/shows/series/a-episode";
	public static final String PAGE_TYPE = "episode";
	public static final String EPISODE_EPISODE_TYPE = "episodic";
	public static final String COMPANY_PAGE1_UID = "aaaa-bbbb-cccc-dddd";
	public static final String COMPANY_PAGE2_UID = "aaaa-cccc-bbbb-dddd";
	public static final String COMPANY_PAGE3_UID = "aaaa-bbbb-dddd-cccc";
	
	public static final String RECIPE_PAGE1_UID = "bbbb-aaaa-cccc-dddd";
	public static final String RECIPE_PAGE2_UID = "bbbb-cccc-aaaa-dddd";
	public static final String RECIPE_PAGE3_UID = "bbbb-cccc-dddd-aaaa";
	
	public static final String EPISODE_SHOW_ID = "show-asset-uuid-value";
	public static final String EPISODE_SERIES_ID = "series-asset-uuid-value";
	
	@Mock Episode episode;
	@Mock Series series;
	
	@Mock Resource episodePageCR;
	@Mock ValueMap episodePageProperties;

    @Mock PageManager pageManager;
    @Mock ResourceResolver resourceResolver;
    
    @Mock SniPage companyPage1;
    @Mock SniPage companyPage2;
    @Mock SniPage companyPage3;
    
    @Mock SniPage recipePage1;
    @Mock SniPage recipePage2;
    @Mock SniPage recipePage3;
    
    @Mock SniPage episodePage;
    @Mock SniPage showPage;
    @Mock SniPage seriesPage;
    
    List<SniPage> companyPages;
    
    List<SniPage> recipePages;

    @Before
    public void before() {
    	MockitoAnnotations.initMocks(this);
    	
    	when(episodePage.hasContent()).thenReturn(true);
    	when(episodePage.getProperties()).thenReturn(episodePageProperties);
    	when(episodePage.getContentResource()).thenReturn(episodePageCR);
    	when(episodePage.getPath()).thenReturn(PAGE_PATH);
    	when(episodePage.getPageType()).thenReturn(PAGE_TYPE);
    	
    	when(episodePage.getPageManager()).thenReturn(pageManager);
    	
    	when(episode.getEpisodeNumber()).thenReturn(EPISODE_NUMBER);
    	
    	when(episode.getEpisodeType()).thenReturn(EPISODE_EPISODE_TYPE);
    	
    	when(episode.getRelatedShowPage()).thenReturn(showPage);
    	
    	when(episode.getRelatedShowPage().getUid()).thenReturn(EPISODE_SHOW_ID);
    	
    	when(episode.getSeries()).thenReturn(series);
    	
    	when(series.getSniPage()).thenReturn(seriesPage);
    	
    	when(seriesPage.getUid()).thenReturn(EPISODE_SERIES_ID);
    	
    }
    
    /** set up company pages, company page uid. */
    private void setupCompanyPages() {
    	companyPages = Arrays.asList(companyPage1, companyPage2, companyPage3);
    	when(episode.getCompanyPages()).thenReturn(companyPages);
    	
    	when(companyPage1.getUid()).thenReturn(COMPANY_PAGE1_UID);
    	when(companyPage2.getUid()).thenReturn(COMPANY_PAGE2_UID);
    	when(companyPage3.getUid()).thenReturn(COMPANY_PAGE3_UID);
    }
    
    /** set up recipe pages, recipe page uid. */
    private void setupRecipePages() {
    	recipePages = Arrays.asList(recipePage1, recipePage2, recipePage3);
    	when(episode.getRecipePages()).thenReturn(recipePages);
    	
    	when(recipePage1.getUid()).thenReturn(RECIPE_PAGE1_UID);
    	when(recipePage2.getUid()).thenReturn(RECIPE_PAGE2_UID);
    	when(recipePage3.getUid()).thenReturn(RECIPE_PAGE3_UID);
    }
    
    @Test
    public void testEpisodeNumber() {
    	
    	EpisodeExport episodeExport = new EpisodeExport(episodePage, episode);
    	ValueMap exportProps = episodeExport.getValueMap();
    	
		assertEquals(EpisodeExport.ExportProperty.EPISODE_NUMBER.name(),
				EPISODE_NUMBER, exportProps.get(
						EpisodeExport.ExportProperty.EPISODE_NUMBER.name(),
						EpisodeExport.ExportProperty.EPISODE_NUMBER
								.valueClass()));
    	
    }
    
    @Test
    public void testEpisodeType() {
    	EpisodeExport episodeExport = new EpisodeExport(episodePage, episode);
    	ValueMap exportProps = episodeExport.getValueMap();
    	
		assertEquals(EpisodeExport.ExportProperty.EPISODE_TYPE.name(),
				EPISODE_EPISODE_TYPE, exportProps.get(
						EpisodeExport.ExportProperty.EPISODE_TYPE.name(),
						EpisodeExport.ExportProperty.EPISODE_TYPE
								.valueClass()));
    	
    }

    // two show_ids are reported. that's because EPISODE_SHOW_ID should be deprecated in favor of CORE_SHOW_ID
    @Test
	public void testShowId() {
		EpisodeExport episodeExport = new EpisodeExport(episodePage, episode);
		ValueMap exportProps = episodeExport.getValueMap();

		assertEquals(EpisodeExport.ExportProperty.EPISODE_SHOW_ID.name(),
				EPISODE_SHOW_ID, exportProps.get(
						EpisodeExport.ExportProperty.EPISODE_SHOW_ID.name(),
						EpisodeExport.ExportProperty.EPISODE_SHOW_ID
								.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_SHOW_ID.name(),
                EPISODE_SHOW_ID, exportProps.get(
                SniPageExport.ExportProperty.CORE_SHOW_ID.name(),
                SniPageExport.ExportProperty.CORE_SHOW_ID
                        .valueClass()));
	}
    
    @Test
	public void testSeriesId() {
		EpisodeExport episodeExport = new EpisodeExport(episodePage, episode);
		ValueMap exportProps = episodeExport.getValueMap();

		assertEquals(EpisodeExport.ExportProperty.EPISODE_SERIES_ID.name(),
				EPISODE_SERIES_ID, exportProps.get(
						EpisodeExport.ExportProperty.EPISODE_SERIES_ID.name(),
						EpisodeExport.ExportProperty.EPISODE_SERIES_ID
								.valueClass()));
	}
    
    @Test
    public void testEpisodeCompanies() {
    	setupCompanyPages();
    	EpisodeExport episodeExport = new EpisodeExport(episodePage, episode);
    	ValueMap exportProps = episodeExport.getValueMap();
    	
    	String[] companies = exportProps.get(EpisodeExport.ExportProperty.EPISODE_COMPANIES.name(), String[].class);
    	
    	assertEquals(EpisodeExport.ExportProperty.EPISODE_COMPANIES.name(), companyPages.size(), companies.length);

    	int i = 0;
    	for(SniPage sniPage : episode.getCompanyPages()) {
    		assertEquals("Company Page", sniPage.getUid(), companies[i++]);
    	}
    }
    
    @Test
    public void testEpisodeRecipes() {
    	setupRecipePages();
    	EpisodeExport episodeExport = new EpisodeExport(episodePage, episode);
    	ValueMap exportProps = episodeExport.getValueMap();
    	
    	String[] recipes = exportProps.get(EpisodeExport.ExportProperty.EPISODE_RECIPES.name(), String[].class);
    	
    	assertEquals(EpisodeExport.ExportProperty.EPISODE_RECIPES.name(), recipePages.size(), recipes.length);

    	int i = 0;
    	for(SniPage sniPage : episode.getRecipePages()) {
    		assertEquals("Recipe Page", sniPage.getUid(), recipes[i++]);
    	}
    }
}
