/**
 * 
 */
package com.scrippsnetworks.wcm.recipe.asset;

/**
 * @author Mallik Vamaraju Date : 10/22/2013
 */
public interface AssetRecipeSelectors {
	
	/** return pageIndex for the asset recipe page */
	public int getPageIndex();
	
	/** return true when the selector has most-popular for the show recipe page */
	public String getSortSelector();
	
}
