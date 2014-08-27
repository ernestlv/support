package com.scrippsnetworks.wcm.asset.video;

import java.util.*;

import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.resource.Resource;

import org.apache.commons.lang.Validate;

import com.scrippsnetworks.wcm.asset.show.AbstractResourceObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a representation of video data for consumption by web display
 * formatters
 * 
 * Incomplete, can still put in more getters and setters...
 * 
 * @author Danny Gordon 7.31.12
 * 
 */
@Deprecated
public class Video {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());

	public static final String VIDEO_CONTENT_ROOT = AbstractResourceObject.CONTENT_ROOT
			+ "/video";
	protected static final String ASSETLINK = "sni:assetLink";
	protected static final String RESOURCETYPE = "sni-wcm/components/pagetypes/video";
	protected static final String ASSETUID = "sni:assetUId";
	protected static final String PLAYERID = "sni:playerId";
	protected static final String PSATITLE = "sni:psaPlayerTitle";
	protected static final String SOURCEID = "sni:sourceId";
	protected static final String RUNTIME = "sni:totalRunTime";
	protected static final String SOURCENETWORK = "sni:source";
	protected static final String VIDEOURL = "sni:videoUrl";
	protected static final String THUMBNAILURL = "sni:thumbnailUrl";
	protected static final String CLIPNAME="jcr:title";
	protected static final String ABSTRACT = "sni:abstract";
	protected static final String RELATEDTITLE = "sni:relatedTitle";
	protected static final String RELATEDURLTEXT = "sni:relatedUrlText";
	protected static final String RELATEDURL = "sni:relatedUrl";
	protected static final String SHOWNAME = "sni:showName";
	protected static final String SHOWURL = "sni:showUrl";
	protected static final String AVGRATING = "sni:averageRating";
	protected static final String PERMALINKURL = "sni:permalinkUrl";
	protected static final String COMPRUNTIMES = "sni:componentRunTimes";
	protected static final String SYNCHRLINKURL = "sni:synchrLinkUrl";
	protected static final String SYNCHRLINKTXT = "sni:synchrLinkText";
	protected static final String SPONSORSHIP = "sni:sponsorShip";
	protected static final String ADLEVEL = "sni:adLevel";
	
    //overrides from the person data node located under content
    private static final String[] allowedOverrides = {CLIPNAME};
    
	private String videoContentPath; //path to video page (under /content/cook/videos ...
	private Resource videoResource;  
	private Map<String, Object> mergeMap; //map of properties of a video merged from content/videos and etc/sni:asset/videos
	private String videoPageURL;

	/**
	 * Construct an empty Video object
	 */
	public Video() {
	}

	/**
	 * Construct a Video object given a Sling Resource of the calling page and
	 * the String representing a path to the content page containing the Video.
	 * 
	 * @param resource
	 *            Sling Resource of page calling for data.
	 * @param path
	 *            String representing a path to the Video page (single Video)
	 *            
	 */
	public Video(final Resource resource, final String path) {
		
		Validate.notNull(resource);
		Validate.notNull(path);
		videoResource = resource;
		videoContentPath = path + "/jcr:content";
		videoPageURL = path + ".html";
		setVideoValueMap();
	
	}
	
	/***
	 * 
	 */
	private void setVideoValueMap()
	{
			 //get properties under /content/video..
			 ValueMap contentVideoValues = ResourceUtil
					 					.getValueMap(videoResource.getResourceResolver().getResource(videoContentPath));
			 
			 	//check to make sure we have a valid video and assetlink property
		        if (valueMapIsType(contentVideoValues, ASSETLINK))
		        {	        	
		        
		        	String assetLinkLoc = (String)contentVideoValues.get(ASSETLINK) + "/jcr:content";
		      
		        		//get properties under /etc/sni-asset/video...
		        		ValueMap assetVideoValues = 
		        				ResourceUtil.getValueMap(videoResource.getResourceResolver().getResource(assetLinkLoc));
		        		
		  
		        		//merge properties
		        		mergeValueMaps(contentVideoValues, assetVideoValues);
		        		
		        	}
		        	
		}

	/***Merges properties of a person from /content/people and from /etc/sni-asset/people
	 * uses a static array allowedOverrides to determine which properties from /content/people are pushed in
	 * @param contentVideoValues
	 * @param assetVideoValues
	 */
	private void mergeValueMaps(ValueMap contentVideoValues, ValueMap assetVideoValues)
	{
		mergeMap = new HashMap<String, Object>();
        if (assetVideoValues != null) {
        	
        	  for (Map.Entry<String,Object> entry : assetVideoValues.entrySet()) {
                  mergeMap.put(entry.getKey(), entry.getValue());
              }
            if (contentVideoValues != null) {
                for (String override : allowedOverrides) {
                    if (contentVideoValues.containsKey(override)){
                    	mergeMap.put(override, contentVideoValues.get(override));
                    }
                }
            }
        } 
	}
	
	/***
	 * returns a full map of properties of a person merged from both /content/people and /etc/sni-asset/people
	 * @return
	 */
	public Map<String, Object> getMergedValues()
	{
		if(mergeMap != null)
			return mergeMap;
		else
			return null;
	}
	/***
	 * 
	 * @return a persons name from the jcr:title property. 
	 * the title property under /content/people will override the property under /etc/sni-asset/people
	 * 					
	 */
	public String getSourceId()
	{
		return safeStringVal(SOURCEID);
	}
	
	public String getClipName()
	{
		return safeStringVal(CLIPNAME);
	}
	
	public String getLength()
	{
		return safeStringVal(RUNTIME);
	}
	
	public String getSourceNetwork()
	{
		return safeStringVal(SOURCENETWORK);
	}
	
	public String getVideoUrl()
	{
		return safeStringVal(VIDEOURL);
	}
	
	public String getThumbnailUrl()
	{
		return safeStringVal(THUMBNAILURL);
	}
	
	public String getAbstract()
	{
		return safeStringVal(ABSTRACT);
	}
	
	public String getRelatedTitle()
	{
		return safeStringVal(RELATEDTITLE);
	}
	
	public String getRelatedUrlText() {
		return safeStringVal(RELATEDURLTEXT);
	}
	
	public String getRelatedUrl()
	{
		return safeStringVal(RELATEDURL);
	}
	
	public String getShowName()
	{
		return safeStringVal(SHOWNAME);
	}
	
	public String getShowUrl()
	{
		return safeStringVal(SHOWURL);
	}
	
	public String getSponsorshipValue()
	{
		return safeStringVal(SPONSORSHIP);
	}
	
	public String getAverageRating()
	{
		return safeStringVal(AVGRATING);
	}
	
	public String getAdLevel()
	{
		return safeStringVal(ADLEVEL);
	}
	
	public String getPermalinkUrl()
	{
		return safeStringVal(PERMALINKURL);
	}
	
	public String getSynchrLinkUrl()
	{
		return safeStringVal(SYNCHRLINKURL);
	}
	
	public String getSynchrLinkText()
	{
		return safeStringVal(SYNCHRLINKTXT);
	}

	public Object[] getCmpnRunTimes()
	{
		if(mergeMap != null && mergeMap.containsKey(COMPRUNTIMES) && mergeMap.get(COMPRUNTIMES) instanceof Object[])
		{
			return (Object[])mergeMap.get(COMPRUNTIMES);
		}
		Object[] emptyObjArr = {};
		return emptyObjArr;
	}
	
	public String getPageURL()
	{
		if(videoPageURL != null)
			return videoPageURL;
		
		return "";
	}
	
	
	public String safeStringVal(String key)
	{
		if(mergeMap != null && mergeMap.containsKey(key) && mergeMap.get(key) instanceof String)
		{
			return (String)mergeMap.get(key);
		}
		
		return "";
		
	}
	
	
	private static boolean valueMapIsType(final ValueMap valueMap, final String assetLinkProperty) {
	        return (!(valueMap == null) && valueMap.containsKey(assetLinkProperty));
	    }

	
	
}
