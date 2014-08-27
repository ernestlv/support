/**
 * 
 */
package com.scrippsnetworks.wcm.recipe.asset.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.asset.AssetRecipeSelectors;

/**
 * @author Mallik Vamaraju Date : 10/22/2013
 */
public class AssetRecipeSelectorsImpl implements AssetRecipeSelectors

{
	
	private static final Logger log = LoggerFactory
			.getLogger(AssetRecipeSelectorsImpl.class);
	
	/** SniPage used to construct this asset recipe with selectors. */
	private SniPage sniPage;
	
	/** selectors passed to the Asset Recipe page */
	
	private List<String> selectors;
	
	/* default the pageIndex to 1 */
	private int pageIndex = 1;
	
	private String sortSelector;
	
	/** Constructor for accessing selectors for asset recipe page. */
	public AssetRecipeSelectorsImpl(final SniPage page) {
		this.sniPage = page;
		selectors = sniPage.getSelectors();
	}
	
	/** {@inheritDoc} */
	public int getPageIndex() {
		
		if (!selectors.isEmpty() && selectors.size() > 1) {
			String[] pageSelector = selectors.get(1).split("-");
			if (pageSelector.length > 1) {
				try {
					pageIndex = Integer.parseInt(pageSelector[1]);
				} catch (NumberFormatException e) {
					log.error("Number Format Exception in PageIndex of Asset Recipe Page.");
				}
			}
			
		}
		
		return pageIndex;
		
	}
	
	/** {@inheritDoc} */
	public String getSortSelector()
	
	{
		
		if (!selectors.isEmpty() && selectors.size() > 0) {
			sortSelector = selectors.get(0);
			return sortSelector;
			
		}
		
		return sortSelector;
	}
	
}
