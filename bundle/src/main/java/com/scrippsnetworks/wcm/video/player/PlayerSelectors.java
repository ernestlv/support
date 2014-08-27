/**
 * 
 */
package com.scrippsnetworks.wcm.video.player;

/**
 * @author Mallik Vamaraju
 * 
 */
public interface PlayerSelectors {
	
	/** Return channel id selector for player page. */
	public String getChannelAssetId();
	
	/** Return video id selector for player page. */
	public String getVideoId();
	
	/** Return asset id from page path info. */
	public String getAssetIdFromPagePath();
	
	/** Return video id from page path info. */
	public String getVideoIdFromPagePath();
	
	/** Return Page from page path info. */
	public String getPageFromPagePath();
	
}
