package com.scrippsnetworks.wcm.asset.show;

import java.util.ArrayList;
import java.util.List;

import org.apache.sling.api.resource.Resource;

import com.scrippsnetworks.wcm.asset.DataUtil;
import com.scrippsnetworks.wcm.asset.recipe.Recipe;

/**
 * Object representing the Episode resource
 * 
 * @author mei-yichang
 * 
 */

public class Episode extends AbstractResourceObject {

	public static final String PROPERTY_EPISODE_ID = "sni:episodeNo";
	public static final String PROPERTY_SHORT_TITLE = "sni:shortTitle";
	public static final String PROPERTY_RUN_TIME = "sni:totalRunTime";
	public static final String PROPERTY_SUMMARY = "sni:abstract";
	public static final String PROPERTY_RECIPES = "sni:recipes";

	/**
	 * Asset Type Constant
	 */
	public static final String ASSET_TYPE = "EPISODE";

	private String description;
	private String summary;
	private String episodeId;
	private String shortTitle;

	private double totalRunTime;
	private List<Recipe> recipes;

	/**
	 * Constructor
	 * 
	 * @param resource
	 * @param depts
	 */
	public Episode(Resource resource, int depts) {
		super(resource);
		recipes = new ArrayList<Recipe>();
		// properties of episode under /etc/sni-assets/shows
		if (assetProperties != null) {
			summary = assetProperties.get(PROPERTY_SUMMARY, "");
			episodeId = assetProperties.get(PROPERTY_EPISODE_ID, "");
			shortTitle = assetProperties.get(PROPERTY_SHORT_TITLE, "");
			totalRunTime = 0;
			try {
				totalRunTime = Double.parseDouble(assetProperties.get(
						PROPERTY_RUN_TIME, "0"));
			} catch (NumberFormatException e) {
				log.error(e.getMessage(), e);
			}
			
			/* only load the recipes resource when it's necessary */
			if (depts > 0) {
				Object[] recipesPaths =DataUtil.getRecipePathsFromEpisodeAssetPath(resource.getPath(), resource);
				
				if (recipesPaths != null) {
					
					for (Object path : recipesPaths) {
						
						recipes.add(new Recipe(resource, (String) path));
					}
				}
			}
		}

		// properties of episode under /content/.../shows
		if (contentProperties != null) {
			description = contentProperties.get(PROPERTY_DESCRIPTION,
					assetProperties.get(PROPERTY_DESCRIPTION, ""));
		}
	}

	public String getSummary() {
		return summary;
	}

	/**
	 * Get the series object which the episode belongs
	 * 
	 * @return Series
	 */
	public Series getSeries() {
		if (resource != null) {
			Resource parent = resource.getParent();
			if (checkResourceAssetType(parent, Series.ASSET_TYPE)) {
				return new Series(parent, 0);
			}
		}
		return null;
	}

	public String getDescription() {
		return description;
	}

	public String getEpisodeId() {
		return episodeId;
	}

	public String getShortTitle() {
		return shortTitle;
	}

	public double getTotalRunTime() {
		return totalRunTime;
	}

	public List<Recipe> getRecipes() {
		return recipes;
	}
}
