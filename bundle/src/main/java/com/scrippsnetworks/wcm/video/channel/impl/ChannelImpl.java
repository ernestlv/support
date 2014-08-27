package com.scrippsnetworks.wcm.video.channel.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.json.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.util.CompoundProperty;
import com.scrippsnetworks.wcm.util.Constant;
import com.scrippsnetworks.wcm.video.Video;
import com.scrippsnetworks.wcm.video.VideoFactory;
import com.scrippsnetworks.wcm.video.VideoUtil;
import com.scrippsnetworks.wcm.video.channel.Channel;

/**
 * @author Jason Clark Date: 5/10/13
 */
public class ChannelImpl implements Channel {
	
	private static final Logger log = LoggerFactory
			.getLogger(ChannelImpl.class);
	
	/** Property containing video paths in channel. */
	private static final String SNI_VIDEOS = "sni:videos";
	
	/** Videos in this Channel. */
	private List<Video> videos;
	
	/** First Video from the full list of Videos. */
	private Video firstVideo;
	
	/** SniPage passed into constructor. */
	private SniPage sniPage;
	
	private String assetUId;
	
	/** ValueMap of page properties from the channel. */
	private ValueMap pageProperties;
	
	/** String path used for Snap Player. */
	private String snapPlayerPath;
	
	/** associated Videos to the channel. */
	private String[] associateVideos;
	
	/** boolean value to check for sponsorship */
	private boolean hasSponsorshipSource;
	
	/** pageManager of the sniPage. */
	private PageManager pageManager;
	
	/**
	 * Exclusion video Ids (Concatinated Associated videos to Channel with a
	 * delimeter ",".
	 */
	private String exclusionVideoIds;
	
	/** Construct a Channel when given an SniPage for the channel page. */
	public ChannelImpl(final SniPage sniPage) {
		this.sniPage = sniPage;
		if (sniPage != null) {
			pageProperties = sniPage.getProperties();
			pageManager = sniPage.getPageManager();
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public List<Video> getVideos() {
		if (videos == null) {
			videos = new ArrayList<Video>();
			if (sniPage != null) {
				pageProperties = sniPage.getProperties();
				if (pageProperties != null
						&& pageProperties.containsKey(SNI_VIDEOS)) {
					String[] sniVideos = pageProperties.get(SNI_VIDEOS,
							String[].class);
					if (sniVideos != null) {
						for (String compoundVal : sniVideos) {
							// sni:videos is a compound value of
							// videoPath|videoThumbnailUrl
							if (StringUtils.isNotBlank(compoundVal)) {
								CompoundProperty videoProp = new CompoundProperty(
										compoundVal);
								String videoPath;
								if (StringUtils.isNotBlank(videoProp.getKey())) {
									videoPath = videoProp.getKey();
								} else {
									videoPath = videoProp.getValue();
								}
								if (StringUtils.isNotBlank(videoPath)) {
									Resource videoPageResource = sniPage
											.getContentResource()
											.getResourceResolver()
											.getResource(videoPath);
									if (videoPageResource != null) {
										Page videoPage = videoPageResource
												.adaptTo(Page.class);
										SniPage videoSniPage = PageFactory
												.getSniPage(videoPage);
										if (videoSniPage != null) {
											Video video = new VideoFactory()
													.withSniPage(videoSniPage)
													.build();
											if (video != null) {
												videos.add(video);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return videos;
	}
	
	/** {@inheritDoc} */
	@Override
	public Video getFirstVideo() {
		if (firstVideo == null) {
			List<Video> videoList = getVideos();
			if (videoList != null && videoList.size() > 0) {
				firstVideo = videoList.get(0);
			}
		}
		return firstVideo;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getSnapPlayerPath() {
		if (snapPlayerPath == null) {
			if (sniPage != null) {
				snapPlayerPath = VideoUtil.formatChannelSnapPath(sniPage
						.getPath());
			}
		}
		return snapPlayerPath;
	}
	
	/** {@inheritDoc} */
	@Override
	public SniPage getSniPage() {
		return sniPage;
	}
	
	/** Convenience method for checking properties and returning a value. */
	private String getStringProp(final String propName) {
		if (pageProperties != null && StringUtils.isNotBlank(propName)
				&& pageProperties.containsKey(propName)) {
			return pageProperties.get(propName, String.class);
		}
		return null;
	}
	
	/* Call to utility method to determine whether or not a channel is sponsored */
	public boolean getHasSponsorshipSource() {
		
		hasSponsorshipSource = VideoUtil.hasSponsorshipSource(sniPage);
		return hasSponsorshipSource;
		
	}

	/**
	 * This method is used to get the associated videos to the Video Channel.
	 * 
	 * @return
	 */
	public String[] getAssocVideosFromChannelComponent() {
		if (associateVideos == null && pageProperties != null
				&& pageProperties.containsKey(Constant.SNI_VIDEOS)) {
			associateVideos = pageProperties.get(Constant.SNI_VIDEOS, String[].class);
		}
		return associateVideos;
	}
	
	/**
	 * This method concatinates associated videos to the Video Channel with a
	 * delimeter "," and returns as a string.
	 * 
	 * @return
	 */
	public String getExclusionVideoIds() {
		if (exclusionVideoIds == null) {
			String[] associatedVideos = getAssocVideosFromChannelComponent();
			if (associatedVideos != null) {
				SniPage videoPage = null;
				String exclusionIds = "";
				boolean firstId = true;
				for (String videoPath : associatedVideos) {
					videoPage = PageFactory.getSniPage(pageManager, videoPath);
					if (videoPage != null && videoPage.getUid() != null) {
						if (!firstId) {
							exclusionIds = exclusionIds + ",";
						}
						exclusionIds = exclusionIds + videoPage.getUid();
						firstId = false;
					}
				}
				exclusionVideoIds = exclusionIds;
			}
		}
		return exclusionVideoIds;
	}
	
	
	public String getJSONstr() { 
   	String jsonstr = "";
		try {
			SniPage chpage = getSniPage();
			JSONObject playlistJson = new JSONObject();
			playlistJson.put("title", chpage.getTitle());   
			// many of these fields are blank in the XML rendering so may not be used by player
			playlistJson.put("sponsorshipValue", "");
			playlistJson.put("adLevel", "");
			playlistJson.put("isSponsored", getHasSponsorshipSource());
			playlistJson.put("playerId", "");
			playlistJson.put("psaPlayerTitle", "");
			playlistJson.put("dartId", "");
			playlistJson.put("sponsorName", "");
			// construct JSON array of video elements
			JSONArray aVideosJson = new JSONArray();
			for (Video v: getVideos())  aVideosJson.put(v.getJSONobj());
			playlistJson.put("video", aVideosJson);
			// "channel" is the root name
			playlistJson = new JSONObject().put("channel", playlistJson);
			jsonstr = playlistJson.toString(2);
		}
		catch (JSONException e) {
			log.error(e.getMessage());
		}
		finally {
			return jsonstr;
		}
	}

}
