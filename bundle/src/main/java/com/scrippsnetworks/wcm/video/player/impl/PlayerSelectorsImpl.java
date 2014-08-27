/**
 * 
 */
package com.scrippsnetworks.wcm.video.player.impl;

import java.util.List;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.video.player.PlayerSelectors;

/**
 * @author Mallik Vamaraju
 * 
 */
public class PlayerSelectorsImpl implements PlayerSelectors {
	
	/** SniPage used to construct this Player with selectors. */
	private SniPage sniPage;
	
	/** Page path with deeplink parameters. */
	private String pagePath;
	
	/** selectors passed to this Player page */
	private List<String> selectors;
	
	/** selectors passed to this Player page */
	private String[] pagePathInfo;
	
	/** channel id passed as selector to this Player page */
	private String channelAssetId;
	
	/** video id passed as selector to this Player page */
	private String videoId;
	
	/** Constructor for accessing selectors for player page. */
	public PlayerSelectorsImpl(final SniPage page) {
		this.sniPage = page;
		selectors = sniPage.getSelectors();
	}
	
	/** Constructor for accessing deep link parameters for player page. */
	public PlayerSelectorsImpl(final String pagePath) {
		this.pagePath = pagePath;
		// escape char passed for the period(.) for split function.
		pagePathInfo = pagePath.split("\\.");
		
	}
	
	/** {@inheritDoc} */
	@Override
	public String getVideoId() {
		
		if (!selectors.isEmpty() && selectors.size() > 1)
			videoId = selectors.get(1);
		
		return videoId;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getChannelAssetId() {
		if (!selectors.isEmpty() && selectors.size() > 0)
			channelAssetId = selectors.get(0);
		
		return channelAssetId;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getAssetIdFromPagePath() {
		
		String assetIdFromPagePath = null;
		
		if (pagePathInfo != null && pagePathInfo.length > 1)
			
			assetIdFromPagePath = pagePathInfo[1];
		
		return assetIdFromPagePath;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getVideoIdFromPagePath() {
		
		String videoIdFromPagePath = null;
		
		if (pagePathInfo != null && pagePathInfo.length > 2)
			
			videoIdFromPagePath = pagePathInfo[2];
		
		return videoIdFromPagePath;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getPageFromPagePath() {
		
		String pagePath = null;
		
		if (pagePathInfo != null && pagePathInfo.length > 0)
			
			pagePath = pagePathInfo[0];
		
		return pagePath;
	}
	
}
