package com.scrippsnetworks.wcm.export.snipage.content.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.episode.Episode;
import com.scrippsnetworks.wcm.episode.EpisodeFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.series.Series;

/**
 * This class generates the Episode page specific properties.
 * 
 * @author Venkata Naga Sudheer Donaboina
 */
public class EpisodeExport extends SniPageExport {

	private static final Logger LOG = LoggerFactory
			.getLogger(EpisodeExport.class);

	public enum ExportProperty {

		EPISODE_SHOW_ID(String.class),
		EPISODE_RECIPES(String[].class),
		EPISODE_COMPANIES(String[].class),
		EPISODE_NUMBER(String.class),
		EPISODE_TYPE(String.class),
		EPISODE_SERIES_ID(String.class);

		final Class clazz;

		ExportProperty(Class clazz) {
			this.clazz = clazz;
		}

		public Class valueClass() {
			return clazz;
		}
	}

	private final Episode episode;

	public EpisodeExport(SniPage sniPage) {
		super(sniPage);
		this.episode = new EpisodeFactory().withSniPage(sniPage).build();
		initialize();
	}
	
	protected EpisodeExport(SniPage sniPage, Episode episode) {
		super(sniPage);
		this.episode = episode;
		initialize();
	}
	
	public void initialize() {

		LOG.debug("Started Episode Export overrides");
		
		if (sniPage == null || !sniPage.hasContent() || episode == null) {
            return;
        }
		
		List<SniPage> companyPages = episode.getCompanyPages();
		List<String> companyIds = getSniPageIds(companyPages);
		if(companyIds != null && companyIds.size() > 0) {
			setProperty(ExportProperty.EPISODE_COMPANIES.name(), companyIds.toArray(new String[companyIds.size()]));
		}

		List<SniPage> recipePages = episode.getRecipePages();
		List<String> recipeIds = getSniPageIds(recipePages);
		if(recipeIds != null && recipeIds.size() > 0) {
			setProperty(ExportProperty.EPISODE_RECIPES.name(), recipeIds.toArray(new String[recipeIds.size()]));
		}

		setProperty(ExportProperty.EPISODE_NUMBER.name(), episode.getEpisodeNumber());
		
		setProperty(ExportProperty.EPISODE_TYPE.name(), episode.getEpisodeType());
		
		SniPage showPage = episode.getRelatedShowPage();
		if(showPage != null) {
		    setProperty(ExportProperty.EPISODE_SHOW_ID.name(), showPage.getUid());
            setProperty(SniPageExport.ExportProperty.CORE_SHOW_ID.name(), showPage.getUid());
		}
		
		Series series = episode.getSeries();
		if(series != null && series.getSniPage() != null) {
			setProperty(ExportProperty.EPISODE_SERIES_ID.name(), series.getSniPage().getUid());	
		}
	}

	/**
	 * The Method loops through the page list and returns the assetUid List.
	 * 
	 * @param pageList
	 * @return
	 */
	public List<String> getSniPageIds(List<SniPage> pageList) {
		List<String> pageIds = null;
		if (pageList != null && pageList.size() > 0) {
			pageIds = new ArrayList<String>();
			for (SniPage sniPage : pageList) {
				if (sniPage.getUid() != null) {
					pageIds.add(sniPage.getUid());
				}
			}
		}
		if (pageIds != null && pageIds.size() > 0) {
			return pageIds;
		}
		return null;
	}

}
