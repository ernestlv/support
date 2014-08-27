package com.scrippsnetworks.wcm.video;

import org.apache.commons.lang3.StringUtils;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * @author Jason Clark Date: 7/24/13
 */
public class VideoUtil {
	
	/** Selector to call snap player xml in video component. */
	private static final String VIDEO_PLAYER_SELECTOR = "videoplayer";
	
	/** Selector to call snap player xml in video channel component. */
	private static final String VIDEO_CHANNEL_SELECTOR = "videochannel";
	
	private static boolean hasSponsorshipSource = false;
	
	private static final String SPONSORSHIP_SOURCE = "sni:source";
	
	private static final String UP_FEED = "up_feed";
	
	private static final String FN_MAGAZINE = "fnmagazine";
	
	private static final String ENTWINE = "entwine";
	
	/**
	 * Format Snap Player path for a single video page.
	 * 
	 * @param pagePath
	 *            String path to video page.
	 * @return String formatted snap player path.
	 */
	public static String formatVideoSnapPath(final String pagePath) {
		return formatSnapPlayerPath(pagePath, VIDEO_PLAYER_SELECTOR);
	}
	
	/**
	 * Format Snap Player Path for a channel page.
	 * 
	 * @param pagePath
	 *            String path to channel page.
	 * @return String formatted snap player path.
	 */
	public static String formatChannelSnapPath(final String pagePath) {
		return formatSnapPlayerPath(pagePath, VIDEO_CHANNEL_SELECTOR);
	}
	
	/** Convenience method to format snap player path. */
	private static String formatSnapPlayerPath(final String pagePath,
			final String selector) {
		if (StringUtils.isNotBlank(pagePath)) {
			if (StringUtils.isNotBlank(pagePath)) {
				StringBuilder playerPathBuilder = new StringBuilder();
				String[] pathParts = pagePath.split("/");
				if (pathParts != null && pathParts.length > 3) {
					for (int i = 3; i < pathParts.length; i++) {
						if (StringUtils.isNotBlank(pathParts[i])) {
							playerPathBuilder.append(pathParts[i]);
							if (i != pathParts.length - 1) {
								playerPathBuilder.append("/");
							}
						}
					}
				}
				playerPathBuilder.append(".").append(selector);
				return playerPathBuilder.toString();
			}
		}
		return null;
	}
	
	/**
	 * Utility method to determine whether or not a video/channel is sponsored.
	 * 
	 * @param sniPage
	 * @return
	 */
	public static boolean hasSponsorshipSource(final SniPage sniPage) {
		
		String source = null;
		
		if (sniPage != null) {
			source = sniPage.getProperties().get(SPONSORSHIP_SOURCE,
					String.class);
		}
		
		String videoSource[] = source != null ? source.split(":") : null;
		
		if (videoSource != null && videoSource.length > 1) {
			
			source = videoSource[1];
			
			if (source != null && !source.equals(UP_FEED)
					&& !source.equals(FN_MAGAZINE) && !source.equals(ENTWINE)) {
				
				return true;
				
			}
			
		}
		
		return hasSponsorshipSource;
	}
	
}
