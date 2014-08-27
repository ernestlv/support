package com.scrippsnetworks.wcm.video.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.json.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.video.Video;
import com.scrippsnetworks.wcm.video.VideoUtil;

/**
 * @author Jason Clark Date: 5/10/13
 */
public class VideoImpl implements Video {
    
    private static final Logger log = LoggerFactory.getLogger(VideoImpl.class);
    
    /** If editor overrides the default video on a page. */
    private static final String SELECTED_VIDEO = "./content-well/video_player/selectedVideo";
    
    private String thumbnailUrl;
    private String runTime;
    private String description;
    private String title;
    private String playerTitle;
    private String videoUrl;
    private String snapPlayerPath;
    private String posterUrl;
    private String tzImgUrl;
    private String mediumImageUrl;
    private String alImageUrl;
    private String leadImageUrl;
    private String largeImageUrl;
    private String videoId;
    private String showPath;
    private String promoUrl;
    private String thumbnail16X9;
    private boolean hasSponsorshipSource;
    private String relatedUrl;
    private String relatedUrlText;
    private String abstractText;
    
    /** SniPage of the video used to create this object. */
    private SniPage sniPage;
    
    /** ValueMap for page properties. */
    private ValueMap pageProperties;
    
    /** Construct a new instance of Video from an SniPage. */
    public VideoImpl(final SniPage sniPage) {
        this.sniPage = sniPage;
        if (sniPage != null) {
            pageProperties = sniPage.getProperties();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public SniPage getSniPage() {
        return sniPage;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        if (title == null) {
            String jcrTitle = getStringProp(TITLE);
            String sniTitle = getStringProp(PagePropertyConstants.PROP_SNI_TITLE);
            title = (sniTitle != null && !sniTitle.isEmpty()) ? sniTitle : jcrTitle;
        }
        return title;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getPlayerTitle() {
        if (playerTitle == null) {
            playerTitle = getStringProp(PSATITLE);
        }
        return playerTitle;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getVideoDescription() {
        if (description == null) {
            String jcrDescription = getStringProp(DESCRIPTION);
            String sniDescription = getStringProp(PagePropertyConstants.PROP_SNI_DESCRIPTION);
            description = (sniDescription != null && !sniDescription.isEmpty()) ? sniDescription : jcrDescription;
        }
        return description;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getVideoRunTime() {
        if (runTime == null) {
            String rawRunTime = getStringProp(RUNTIME);
            if (StringUtils.isNotBlank(rawRunTime)) {
                String[] runTimeParts = rawRunTime.split(":");
                if (runTimeParts.length >= 3 && runTimeParts[0].equals("00")) {
                    runTime = StringUtils.join(runTimeParts, ":", 1, runTimeParts.length);
                } else {
                    runTime = rawRunTime;
                }
            }
        }
        return runTime;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getVideoUrl() {
        if (videoUrl == null) {
            videoUrl = getStringProp(VIDEOURL);
        }
        return videoUrl;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getThumbnailUrl() {
        if (thumbnailUrl == null) {
            thumbnailUrl = getStringProp(THUMBNAILURL);
        }
        return thumbnailUrl;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getPosterUrl() {
        // this is currently based on the thumbnailUrl. need to change this when
        // the property is directly available on asset.
        if (posterUrl == null) {
            posterUrl = swapImageSize(POSTER_IMAGE_DIMENSIONS);
        }
        return posterUrl;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getTzImageUrl() {
        if (tzImgUrl == null) {
            tzImgUrl = swapImageSize(TZ_IMAGE_DIMENSIONS);
        }
        return tzImgUrl;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getMediumImageUrl() {
        if (mediumImageUrl == null) {
            mediumImageUrl = swapImageSize(MEDIUM_IMAGE_DIMENSIONS);
        }
        return mediumImageUrl;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getAlImageUrl() {
        if (alImageUrl == null) {
            alImageUrl = swapImageSize(AL_IMAGE_DIMENSIONS);
        }
        return alImageUrl;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getLeadImageUrl() {
        if (leadImageUrl == null) {
            leadImageUrl = swapImageSize(LEAD_IMAGE_DIMENSIONS);
        }
        return leadImageUrl;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getLargeImageUrl() {
        if (largeImageUrl == null) {
            largeImageUrl = swapImageSize(LARGE_IMAGE_DIMENSIONS);
        }
        return largeImageUrl;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getSnapPlayerPath() {
        if (snapPlayerPath == null) {
            String selectedVideo = getStringProp(SELECTED_VIDEO);
            if (StringUtils.isNotBlank(selectedVideo)) {
                snapPlayerPath = VideoUtil.formatVideoSnapPath(selectedVideo);
            } else {
                snapPlayerPath = VideoUtil.formatVideoSnapPath(sniPage
                        .getPath());
            }
        }
        return snapPlayerPath;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getShow() {
        if (showPath == null) {
            showPath = getStringProp(VIDEO_SHOW_RELATION);
        }
        return showPath;
    }
    
    /** Convenience method for checking properties and returning a value. */
    private String getStringProp(final String propName) {
        if (pageProperties != null && StringUtils.isNotBlank(propName)
                && pageProperties.containsKey(propName)) {
            return pageProperties.get(propName, String.class);
        }
        return null;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getVideoId() {
        if (videoId == null) {
            videoId = getStringProp(VIDEOID);
        }
        return videoId;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getPromoUrl() {
        if (promoUrl == null) {
            promoUrl = getStringProp(PROMO_URL);
        }
        return promoUrl;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getThumbnailImage16X9() {
        if (thumbnail16X9 == null) {
            thumbnail16X9 = getStringProp(THUMBNAIL16X9);
        }
        return thumbnail16X9;
    }
    
    /*
     * Convenience method for swapping out image dimensions when requesting from
     * vidya team.
     */
    private String swapImageSize(final String imageSize) {
        String thumbnail = getThumbnailUrl();
        return StringUtils.isNotBlank(thumbnail) ? thumbnail.replaceFirst(
                THUMBNAIL_IMAGE_DIMENSIONS, imageSize) : null;
    }
    
    
    /* Call to utility method to determine whether or not a video is sponsored */

    public boolean getHasSponsorshipSource() {
        
        hasSponsorshipSource = VideoUtil.hasSponsorshipSource(sniPage);
        return hasSponsorshipSource;
        
    }
    
    /** {@inheritDoc} */
    @Override
    public String getRelatedUrl() {
        if(relatedUrl == null) {
            relatedUrl = getStringProp(RELATED_URL);
        }
        return relatedUrl;
    }

    /** {@inheritDoc} */
    @Override
    public String getRelatedUrlText() {
        if(relatedUrlText == null) {
            relatedUrlText = getStringProp(RELATED_URL_TEXT);
        }
        return relatedUrlText;
    }

    /** {@inheritDoc} */
    @Override
    public String getAbstractText() {
        if(abstractText == null) {
            abstractText = getStringProp(PagePropertyConstants.PROP_SNI_ABSTRACT);
        }
        return abstractText;
    }

    @Override
    public JSONObject getJSONobj() {
			JSONObject videoJson = new JSONObject();
			try {
				videoJson.put("assetId", getVideoId());
				videoJson.put("videoId", getVideoId());
				videoJson.put("clipName", getTitle());
				videoJson.put("length", getVideoRunTime());
				videoJson.put("videoUrl", getVideoUrl());
				videoJson.put("thumbnailUrl", getThumbnailUrl());
				videoJson.put("abstract", getAbstractText());
				videoJson.put("relatedTitle", getRelatedUrlText());
				videoJson.put("relatedUrl", getRelatedUrl());
	/*			
				videoJson.put("sourceNetwork", "TODO ????");
				videoJson.put("showName": "",
				videoJson.put("showUrl": "",
				videoJson.put("sponsorshipValue": "",
				videoJson.put("averageRating": "",
				videoJson.put("adLevel": "",
				videoJson.put("permalinkUrl": "www.foodnetwork.com/videos/giadas-easiest-pasta-sauces-0148890.html",
				videoJson.put("cmpnRunTimes": "00:05:09",
				videoJson.put("synchrLinkUrl": "",
				videoJson.put("synchrLinkText": "",
	*/
			}
			catch (JSONException e) {
				log.error(e.getMessage());
			}
			finally {
				return videoJson;
			}
    }
    
    @Override
    public String getJSONstr() {
    	String jsonstr = "";
			try {
				JSONObject playlistJson = new JSONObject();
				playlistJson.put("title", "TODO a single video");
				// array of one video element, conforming to same playlist used for channels
				playlistJson.put("video", new JSONArray().put(getJSONobj()));
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
