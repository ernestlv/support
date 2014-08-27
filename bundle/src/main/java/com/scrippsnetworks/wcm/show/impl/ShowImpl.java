package com.scrippsnetworks.wcm.show.impl;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.image.SniImageFactory;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.episode.Episode;
import com.scrippsnetworks.wcm.relationship.RelationshipModel;
import com.scrippsnetworks.wcm.relationship.RelationshipModelFactory;
import com.scrippsnetworks.wcm.series.Series;
import com.scrippsnetworks.wcm.series.SeriesFactory;
import com.scrippsnetworks.wcm.show.Show;
import com.scrippsnetworks.wcm.talent.Talent;
import com.scrippsnetworks.wcm.talent.TalentFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import com.scrippsnetworks.wcm.util.PagePropertyNames;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author Patrick
 */
public class ShowImpl implements Show {
	private SniImage featureBanner;
	
	private static final String SERIES_TYPE = "series";
	private static final String ASSET_RECIPES_TYPE = "asset-recipes";
	private static final String FEATURE_BANNER = "sni:featureBannerImage";
	private static final String SHOWINFO_RESOURCE_TYPE = "show-info";
    private static final String TUNE_IN_TIME="sni:tuneInTime";
	
	/** Cannonical Image from primary talent. */
	private SniImage primaryImage;
	
	/** Primary Talent. */
	private Talent talent;
	
	/** SniPage wrapped by this object. */
	private SniPage sniPage;
	
	/** SniPage for primary talent. */
	private SniPage talentPage;
	
	/** List of SniPages for episodes in this show. */
	private List<Episode> episodePages;
	
	/** List of Episodes for each series. */
	private List<Episode> episodeList;
	
	/** RelationshipModel for this Show, stored for convenience. */
	private RelationshipModel relationshipModel;
	
	/** Member for Series in this show. */
	private List<Series> series;
	
	/** Member for Series in this show. */
	private List<Series> allLatestSeries;
	
	/** Asset Recipe page under this Show, if any. */
	private SniPage recipeListingPage;
	
	/** Override host */
	private String overrideHost;
    
    /** TuneIn time of this show */
    private String tuneInTime;
	
	/** Construct a new Show from a given SniPage. */
	public ShowImpl(SniPage page) {
         this.sniPage = page;
         RelationshipModel model = new RelationshipModelFactory()
             .withSniPage(page)
             .build();
         if (model != null) {
            this.talentPage = model.getPrimaryTalent();
            if (talentPage != null) {
                this.talent = new TalentFactory()
                    .withSniPage(talentPage)
                    .build();
            }
        }
    }
	
	/** Convenience method for getting RelationshipModel. */
	private RelationshipModel getRelationshipModel() {
		if (relationshipModel == null) {
			relationshipModel = new RelationshipModelFactory().withSniPage(
					sniPage).build();
		}
		return relationshipModel;
	}
	
	/** {@inheritDoc} */
	@Override
	public SniPage getTalentPage() {
		if (talentPage == null) {
			RelationshipModel model = getRelationshipModel();
			if (model != null) {
				talentPage = model.getPrimaryTalent();
			}
		}
		return talentPage;
	}
	
	/** {@inheritDoc} */	
	@Override
	public Talent getPrimaryTalent() {
        if (talent == null && talentPage != null) {
			talent = new TalentFactory().withSniPage(talentPage).build();
		}
		return talent;
	}
	
    /** {@inheritDoc} */
	
	public SniImage getCanonicalImage() {
        if (primaryImage == null && talent != null) {
            primaryImage = talent.getCanonicalImage();
        }
        return primaryImage;
    }
    
    @Override
	public SniImage getFeatureBanner() {
		if (featureBanner == null) {
            String featureBannerPath = sniPage.getProperties().get(FEATURE_BANNER, String.class);
					
			LoggerFactory.getLogger(ShowImpl.class).info("FB: {}",
					featureBannerPath);
			if (!StringUtils.isEmpty(featureBannerPath)) {
				featureBanner = new SniImageFactory().withPath(
						featureBannerPath).build();
			}
			LoggerFactory.getLogger(ShowImpl.class).info(
					"img is null? " + (featureBanner == null));
			if (featureBanner != null) {
				LoggerFactory.getLogger(ShowImpl.class).info("path: {}",
						featureBanner.getPath());
			}
		}
		return featureBanner;
	}
	
	/** {@inheritDoc} */
	@Override
	public SniPage getSniPage() {
		return sniPage;
	}
	
	/** {@inheritDoc} */
	@Override
	public List<Episode> getEpisodePages() {
		if (episodePages == null) {
			episodePages = new ArrayList<Episode>();
			if (allLatestSeries == null) {
				allLatestSeries = getAllLatestSeries();
				episodePages = getAllEpisodes();
			} else {
				episodePages = getAllEpisodes();
			}
			
		}
		return episodePages;
	}
	
	/** {@inheritDoc} */
	@Override
	public List<Series> getAllSeries() {
		if (series == null) {
			series = new ArrayList<Series>();
			if (sniPage != null) {
				Iterator<Page> children = sniPage.listChildren();
				if (children != null) {
					while (children.hasNext()) {
						Page child = children.next();
						if (child != null) {
							SniPage childSniPage = PageFactory
									.getSniPage(child);
							if (childSniPage != null
									&& childSniPage.getPageType() != null
									&& childSniPage.getPageType().equals(
											SERIES_TYPE)) {
								Series childSeries = new SeriesFactory()
										.withSniPage(childSniPage).build();
								series.add(childSeries);
							}
						}
					}
				}
			}
		}
		return series;
	}
	
	/** Iterates through each series and get the total list of episodes. */
	
	public List<Episode> getAllEpisodes() {
		episodeList = new ArrayList<Episode>();
		for (int i = 0; i < allLatestSeries.size(); i++) {
			episodeList = allLatestSeries.get(i).getEpisodes();
			if (!episodeList.isEmpty()) {
				for (int j = 0; j < episodeList.size(); j++) {
					episodePages.add(episodeList.get(j));
				}
			}
			
		}
		
		return episodePages;
	}
	
	/** {@inheritDoc} */
	@Override
	public List<Series> getAllLatestSeries() {
		if (allLatestSeries == null) {
			if (series == null) {
				allLatestSeries = getAllSeries();
			} else {
				allLatestSeries = series;
			}
			
			Collections.reverse(allLatestSeries);
		}
		return allLatestSeries;
	}
	
	/** {@inheritDoc} */
	@Override
	public SniPage getRecipeListingPage() {
		if (recipeListingPage == null) {
			if (sniPage != null) {
				Iterator<Page> children = sniPage.listChildren();
				if (children != null) {
					while (children.hasNext()) {
						Page child = children.next();
						if (child != null) {
							SniPage childSniPage = PageFactory
									.getSniPage(child);
							String pageType = childSniPage.getPageType();
							if (StringUtils.isNotBlank(pageType)
									&& pageType.equals(ASSET_RECIPES_TYPE)) {
								recipeListingPage = childSniPage;
							}
						}
					}
				}
			}
		}
		return recipeListingPage;
	}
	
	public String getOverrideHost() {
		if (overrideHost == null) {
			Iterator<Resource> childItr = sniPage.getContentResource()
					.listChildren();
			findHost: while (childItr.hasNext()) {
				Resource childRes = childItr.next();
				ValueMap childVm = ResourceUtil.getValueMap(childRes);
				Iterator<Resource> grandItr = childRes.listChildren();
				while (grandItr.hasNext()) {
					Resource grandRes = grandItr.next();
					ValueMap grandVm = ResourceUtil.getValueMap(grandRes);
					if (grandVm
							.containsKey(PagePropertyNames.SLING_RESOURCE_TYPE
									.propertyName())) {
						String rt = grandVm.get(
								PagePropertyNames.SLING_RESOURCE_TYPE
										.propertyName(), String.class);
						if (rt.endsWith("/" + SHOWINFO_RESOURCE_TYPE)) {
							overrideHost = grandVm
									.get("hostedBy", String.class);
							break findHost;
						}
					}
				}
			}
			
		}
		return overrideHost;
	}
    
    /** {@inheritDoc} */
    @Override
    public String getTuneInTime() {
        if (tuneInTime == null) {
            if (sniPage.getProperties() != null
                    && sniPage.getProperties().containsKey(TUNE_IN_TIME)) {
                tuneInTime = sniPage.getProperties().get(TUNE_IN_TIME,
                        String.class);
            }
        }
        return tuneInTime;
    }
}
