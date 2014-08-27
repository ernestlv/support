package com.scrippsnetworks.wcm.asset.hub;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.fnr.util.PageSlingResourceTypes;
import com.scrippsnetworks.wcm.asset.DataUtil;
import com.scrippsnetworks.wcm.search.SearchUtil;

/** Provides a numeric value for specific elements based on a page type in the hub
 * for example when passed a video channel page the getCount function will return the number
 * of videos included on the page. 
 * 
 * For a photo gallery the getCount function will return the number of photos on the page...
 */
@Deprecated
public class HubCount {

    /**
     * Returns the page's Hub.
     */
	private static final Logger log = LoggerFactory.getLogger(HubCount.class);
	
    public static int getHubCount(Page page) {
    	
    	try {
		if(page != null){
			
			String resourceType = page.getProperties().get("sling:resourceType") != null ? 
					page.getProperties().get("sling:resourceType").toString() :null;
				
			if(resourceType != null){
	
				if(resourceType.equals(PageSlingResourceTypes.VIDEO.resourceType()))
					return 1;
				else if(resourceType.equals(PageSlingResourceTypes.VIDEO_CHANNEL.resourceType())
						|| resourceType.equals(PageSlingResourceTypes.VIDEO_CHANNEL_MOBILE.resourceType()))
					return getCountVideoChannel(page);
				else if(resourceType.equals(PageSlingResourceTypes.VIDEO_PLAYER.resourceType())
						|| resourceType.equals(PageSlingResourceTypes.VIDEO_PLAYER_MOBILE.resourceType()))
					return getCountVideoPlayer(page);
				else if(resourceType.equals(PageSlingResourceTypes.PHOTO_GALLERY.resourceType())
						|| resourceType.equals(PageSlingResourceTypes.PHOTO_GALLERY_MOBILE.resourceType()))
					return getCountPhotoGallery(page);
				
			}
		}
		
    	}catch (Exception e)
    	{
    		
    		return 0;
    	}
		return 0;
    }
    
    /***
     * Returns an integer value of the number of videos within this channel based off of the sni:videos property
     * @param videoChannel
     * @return
     * @throws ValueFormatException
     * @throws RepositoryException
     */
    public static int getCountVideoChannel(Page videoChannel) throws ValueFormatException, RepositoryException
    {
    	if(videoChannel != null)
    	{
			Resource channelResource = videoChannel.getContentResource("channel-component/sni:videos");
			if(channelResource != null)
			{
					Property videoListProp = channelResource.adaptTo(Property.class);
					if(videoListProp.isMultiple())
						return videoListProp.getValues().length;
					else
						return 1;
					
			}
		}
    	return 0;
    }
    
    /***
     * 
     * @param videoChannel
     * @return
     * @throws ValueFormatException
     * @throws RepositoryException
     */
    public static int getCountVideoPlayer(Page videoPlayer) throws ValueFormatException, RepositoryException
    {
    	if(videoPlayer != null)
    	{
			Resource channelResource = videoPlayer.getContentResource("player-component/sni:channels");
			if(channelResource != null)
			{
					Property channelListProp = channelResource.adaptTo(Property.class);
					if(channelListProp.isMultiple())
						return channelListProp.getValues().length;
					else
						return 1;
					
			}
		}
    	return 0;
    }
    
    /***
     * 
     * @param photoGallery
     * @return
     * @throws ValueFormatException
     * @throws RepositoryException
     */
    public static int getCountPhotoGallery(Page photoGallery) throws ValueFormatException, RepositoryException
    {
    	int photoCount = 0;
    	if(photoGallery != null)
    	{
			Resource galleryContentParsys = photoGallery.getContentResource("gallery-contents/parsys");
			if(galleryContentParsys != null)
			{
				Iterator<Resource> it = galleryContentParsys.listChildren();
				while(it.hasNext())
				{
					Resource galleryContent = it.next();
					//not using PhotoGalleryUtil.isPhotoGalleryComponent because call will be costlier
					if(galleryContent.getName().startsWith("photo-gallery") || galleryContent.getName().startsWith("photo_gallery"))
						photoCount++;
				}
					
			}
		}
    	
    	return photoCount;
    }
    
    /***
     * For both Talent Top recipes and Show Top recipes we base the search parameters off of the Hub Master 
     * (either a Talent page or Show page)
     * 
     * @param hubMaster
     * @return
     * @throws ValueFormatException
     * @throws RepositoryException
     */
    public static int getHubCountTopRecipes(Page hubMaster, SlingScriptHelper sling) throws ValueFormatException, RepositoryException
    {
    	HashMap<String,String> params = new HashMap<String,String>();            
    	params.put("pageType", DataUtil.getPageType(hubMaster));
    	params.put("uid", hubMaster.getProperties().get("sni:assetUId", ""));
    	params.put("numOfResults","1");
    	params.put("offset","0");
    	Map<String, Object> map = SearchUtil.getSearchResponseMap(sling, "topRecipes", params);
    	if(map != null)
    	{
    		//{map.ServiceResponse.AssetUsage.Parameters[0].TotalAssets}
    		Map response = (Map)map.get("ServiceResponse");
    		if(response != null){
    			Map assetUsage = (Map)response.get("AssetUsage");
    			if(assetUsage != null){
    				ArrayList parameters = (ArrayList)assetUsage.get("Parameters");
    				if(parameters != null && parameters.size() > 0)
    				{
    					Map paramMap = (Map)parameters.get(0);
    					String totalNum = (String)paramMap.get("TotalAssets");
    					if(totalNum != null)
    						return Integer.parseInt(totalNum);
    				}
    			}
    		}
    		
    		return 0;
    	}
    		
    	
    	return 0;
    }

  
}
