package com.scrippsnetworks.wcm.menu;

import java.util.List;

import com.scrippsnetworks.wcm.page.SniPage;

/**
 * @author Jason Clark
 * 			Date: 5/11/13
 * Updated Venkata Naga Sudheer Donaboina
 * Date: 8/20/13
 */
public interface Menu {

	/** Returns the SniPage wrapped by this Menu. */
	public SniPage getSniPage();

	/** Title for this Menu. */
	public String getTitle();

	/** Menu description from sni:description. */
	public String getDescription();

	/** Find the Recipe pages associated with this Menu. */
	public List<SniPage> getRecipePages();
	
	/** Primary Talent Page for the menu. */
	public SniPage getPrimaryTalentPage();
	
	/** Retrives mealtype and recipes in the format:
	 * mealType,recipeId
	 */
	public String[] getMealTypeRecipes();
}
