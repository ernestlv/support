package com.scrippsnetworks.wcm.show;

import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.series.Series;
import com.scrippsnetworks.wcm.talent.Talent;
import com.scrippsnetworks.wcm.episode.Episode;

import java.util.List;

/**
 * 
 * @author Patrick Armstrong
 */
public interface Show {

	/** Primary Talent for this show. */
	public Talent getPrimaryTalent();

	/** Talent page for this Show. */
	public SniPage getTalentPage();

	/** Featured Banner for this show. */
    public SniImage getFeatureBanner();

	/** The SniPage wrapped by the Show object. */
	public SniPage getSniPage();

	/** Retrieve a List of episode pages for this show. */
	public List<Episode> getEpisodePages();

	/** List of all Series for this Show. */
	public List<Series> getAllSeries();

	/** Get the asset-recipes page for this show (search driven). */
	public SniPage getRecipeListingPage();

	/** Reverses the series objects returned by getAllSeries method. */
	public List<Series> getAllLatestSeries();

    /** Get the override host from an included show-info module. */
    public String getOverrideHost();
    
    /** Get the TuneIn time for this show. */
    public String getTuneInTime();
}
