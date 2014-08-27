package com.scrippsnetworks.wcm.export.snipage.content.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.Recipe;
import com.scrippsnetworks.wcm.recipe.listing.RecipeListing;
import com.scrippsnetworks.wcm.recipe.listing.RecipeListingFactory;

public class RecipeListingExport extends SniPageExport {
	private static final Logger LOG = LoggerFactory.getLogger(RecipeListingExport.class);

	public enum ExportProperty {

		RECIPELISTING_RECIPES(String[].class);

		final Class clazz;

		ExportProperty(Class clazz) {
			this.clazz = clazz;
		}

		public Class valueClass() {
			return clazz;
		}
	}

	private final RecipeListing recipeListing;

	public RecipeListingExport(SniPage sniPage) {
		super(sniPage);
		this.recipeListing = new RecipeListingFactory().withSniPage(sniPage)
				.build();
		initialize();
	}

	protected RecipeListingExport(SniPage sniPage, RecipeListing recipeListing) {
		super(sniPage);
		this.recipeListing = recipeListing;
		initialize();
	}

	public void initialize() {

		LOG.debug("Started RecipeListing Export overrides");

		if (sniPage == null || !sniPage.hasContent() || recipeListing == null) {
			return;
		}

		List<Recipe> recipes = recipeListing.getRecipes();
		if (recipes != null) {
			List<String> recipeIds = new ArrayList<String>();
			SniPage recipePage = null;
			for (Recipe recipe : recipes) {
				recipePage = recipe.getRecipePage();
				if (recipePage != null) {
					recipeIds.add(recipePage.getUid());
				}
			}
			if (recipeIds.size() > 0) {
				setProperty(ExportProperty.RECIPELISTING_RECIPES.name(), recipeIds.toArray(new String[recipeIds.size()]));
			}
		}
		
		LOG.debug("Completed RecipeListing Export overrides");

	}
}
