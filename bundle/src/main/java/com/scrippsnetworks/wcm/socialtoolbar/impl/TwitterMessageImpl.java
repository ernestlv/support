package com.scrippsnetworks.wcm.socialtoolbar.impl;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.socialtoolbar.TwitterMessage;

/**
 * 
 * @author Mallik Vamaraju Date: 09/16/13
 */

public class TwitterMessageImpl implements TwitterMessage {
	
	/** SniPage used to create this object. */
	private SniPage sniPage;
	
	private String twitterHandle;
	
	private String displaySiteName;
	
	private static final String FOOD_DISPLAY_NAME = "FoodNetwork.com";
	
	private static final String TWITTER_HANDLER_TEXT = "@FoodNetwork";
	
	private static final String FOOD_BRAND_NAME = "food";
	
	/** Construct a new TwitterMessageImpl given an SniPage. */
	public TwitterMessageImpl(SniPage sniPage) {
		this.sniPage = sniPage;
		
	}
	
	/**
	 * return override in the page properties and the default message if the
	 * override isn't exist
	 **/
	
	/** {@inheritDoc} */
	@Override
	public String getTwitterHandle() {
		if (twitterHandle == null) {
			String brandName = sniPage.getBrand();
			if (brandName != null) {
				if (brandName.equalsIgnoreCase(FOOD_BRAND_NAME))
					twitterHandle = TWITTER_HANDLER_TEXT;
			}
			
		}
		return twitterHandle;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getSiteDisplayName() {
		if (displaySiteName == null) {
			String brandName = sniPage.getBrand();
			if (brandName != null) {
				if (brandName.equalsIgnoreCase(FOOD_BRAND_NAME))
					displaySiteName = FOOD_DISPLAY_NAME;
			}
			
		}
		return displaySiteName;
	}
	
}