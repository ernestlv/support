/**
 * 
 */
package com.scrippsnetworks.wcm.recipe.asset;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.asset.impl.AssetRecipeSelectorsImpl;

/**
 * @author Mallik Vamaraju Date : 10/22/2013
 */
public class AssetRecipeSelectorsFactory {
	
	private SniPage sniPage;
	
	public AssetRecipeSelectorsFactory withSniPage(SniPage sniPage) {
		this.sniPage = sniPage;
		return this;
	}
	
	public AssetRecipeSelectors build() {
		if (sniPage != null) {
			return new AssetRecipeSelectorsImpl(sniPage);
		}
		return null;
		
	}
	
}
