package com.scrippsnetworks.wcm.video;

import com.scrippsnetworks.wcm.page.SniPage;

/**
 * A single video page.
 * @author Jason Clark
 *         Date: 5/10/13
 */
public interface Video {
    
   	public static final String PSATITLE = "sni:psaPlayerTitle";
   	public static final String RUNTIME = "sni:totalRunTime";
   	public static final String VIDEOURL = "sni:videoUrl";
   	public static final String THUMBNAILURL = "sni:thumbnailUrl";
   	public static final String TITLE = "jcr:title";
    public static final String DESCRIPTION = "jcr:description";
		public static final String VIDEO_SHOW_RELATION = "sni:show";
    public static final String PROMO_URL = "sni:promoUrl";
    public static final String THUMBNAIL16X9 = "sni:thumbnailUrl16x9";
		public static final String VIDEOID = "sni:sourceId";
    public static final String THUMBNAIL_IMAGE_DIMENSIONS = "92x69";
    public static final String TZ_IMAGE_DIMENSIONS = "120x90";
    public static final String MEDIUM_IMAGE_DIMENSIONS = "160x120";
    public static final String AL_IMAGE_DIMENSIONS = "266x200";
    public static final String LEAD_IMAGE_DIMENSIONS = "400x300";
    public static final String POSTER_IMAGE_DIMENSIONS = "480x360";
    public static final String LARGE_IMAGE_DIMENSIONS = "616x462";
    public static final String RELATED_URL = "sni:relatedUrl";
    public static final String RELATED_URL_TEXT = "sni:relatedUrlText";

    /** SniPage for the single video player. */
    public SniPage getSniPage();

    /** Clip Title. Can return null. */
    public String getTitle();

    /** PSA Player Title. Can return null. */
    public String getPlayerTitle();

    /** MAM Video description. Can return null. */
    public String getVideoDescription();

    /** MAM Video run-time. Can return null. */
    public String getVideoRunTime();

    /** URL to the video. Can return null. */
    public String getVideoUrl();
    
    /** Thumbnail (92x69) for the video. Can return null. */
    public String getThumbnailUrl();

    /** TZ image (120x90) URL for this video. Can return null. */
    public String getTzImageUrl();

    /** Medium image (160x120) URL for this video. Can return null. */
    public String getMediumImageUrl();

    /** AL Image (266x200) URL for this video. Can return null. */
    public String getAlImageUrl();

    /** Lead image (400x300) for this video. Can return null. */
    public String getLeadImageUrl();

    /** Poster image (480x360) URL for video. Can return null. */
    public String getPosterUrl();

    /** Large image (616x462) URL for this video. Can return null. */
    public String getLargeImageUrl();

    /** Path to video for use in Snap Player. Can return null. */
    public String getSnapPlayerPath();
    
    /** Return video id. Can return null. */
    public String getVideoId();
    
    /** Show associated to the video. */
    public String getShow();
    /** Promo Url. */
    public String getPromoUrl();
    
    /** 16x9 Thumbnail Image. */
    public String getThumbnailImage16X9();
    
    /** Related Url for this video. can return null. */
    public String getRelatedUrl();
    
    /** Related Url Text for this video. can return null. */
    public String getRelatedUrlText();
    
    /** Abstract text for this video. can return null. */
    public String getAbstractText();

		/** JSON object for this video -- for composing channel JSON as object **/
    public Object getJSONobj();

		/** JSON string for this video **/
		public String getJSONstr();
    
}
