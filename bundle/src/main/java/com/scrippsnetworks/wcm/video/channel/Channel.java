package com.scrippsnetworks.wcm.video.channel;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.video.Video;

import java.util.List;

/**
 * @author Jason Clark Date: 5/10/13
 */
public interface Channel {
	
	/** Returns SniPage used to construct Channel. */
	public SniPage getSniPage();
	
	/** Returns a List of Video objects. */
	public List<Video> getVideos();
	
	/** Returns first video from the Channel. */
	public Video getFirstVideo();
	
	/** Path used for Snap Player. */
	public String getSnapPlayerPath();
	
	/** Exclusion Video Ids - Used in Video Grid Module. */
	public String getExclusionVideoIds();
	
	/** JSON string for this channel **/
	public String getJSONstr();
	
}
