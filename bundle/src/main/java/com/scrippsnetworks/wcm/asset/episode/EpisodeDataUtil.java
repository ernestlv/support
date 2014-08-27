package com.scrippsnetworks.wcm.asset.episode;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.asset.DataUtil;
import com.scrippsnetworks.wcm.taglib.Functions;
import com.scrippsnetworks.wcm.util.Constant;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.text.SimpleDateFormat;
import java.util.*;
/**
 * @author Pawan Gupta
 * Date: 6/29/12
 * 
 * udated by Danny Gordon
 * Date: 8/28/12
 */
@Deprecated
public class EpisodeDataUtil {
	
	/* logger initialization  **/
	private static final Logger log = LoggerFactory.getLogger(EpisodeDataUtil.class);
	/* class object global variables */
	Page page=null;
	PageManager pageManager;
	Calendar now;
	int count,year,day,month,days;
	List <Map<String,String>> list=new ArrayList<Map<String,String>>();
	
	/*
	 * Constructor initialized class variables
	 * @param currentPageManager
	 * @param currentPage
	 * @param items
	 */
	public EpisodeDataUtil(Page currentPage,String items){
		pageManager=currentPage.getPageManager();
		page=currentPage;
		count=Integer.parseInt(items);
		now=Calendar.getInstance();
		
		year = now.get(Calendar.YEAR);
		
		day = now.get(Calendar.DAY_OF_MONTH);
		//day = 26;
		
		month = now.get(Calendar.MONTH)+1;
		//month = 8;
		days=now.getActualMaximum(Calendar.DAY_OF_MONTH);

	}
	
	
	/*
	 * method to retrieve single recently aired, currently aired, and up coming episode in 2hrs time span (back and future)
	 * This method is ver convoluted because episodes can be either a half hour or an hour long, so we must do several checks to
	 * find the correct time slots, even if they appear null
	 * @param node
	 * @return
	 */
	public Map<String,Object> processJustOnTVEpisodes(boolean isMobile){
		
		//First get the current Node based off current time:
		int hr = now.get(Calendar.HOUR_OF_DAY);
		int min= now.get(Calendar.MINUTE);
		String key= "";
		Integer node;
		Map<String, Integer> timeAndNodeMap = DataUtil.REVERSE_TIME_CODE_MAP;
		
		if(hr == 0)
			key = "00";
		else
			key= "" + hr;
		
		if(min>29 && min<60)
			key= key + ":30";
		else
			key= key + ":00";
		
		
		node = timeAndNodeMap.get(key);
		
		//final map we will return
		Map <String,Object> episodesMap=new HashMap<String,Object>();
		
		//Give initial starting values to all three schedules
		Schedule onNow = new Schedule(node);
		Schedule justOn = new Schedule(onNow.node - 1);
		Schedule upNext = new Schedule(onNow.node + 1);
		
		
  
		/***
		 * Calculate On Now Section
		 */
		Map<String, Object> onNowMap = null;
	  	onNowMap = processEpisode(onNow.getSchedulePage(pageManager), false, isMobile);
	  	
	  	//successfully got the OnNow Episode
	  	if(onNowMap != null)
	  	{
	  		if(onNowMap.containsKey("sni_runtime")) {
	  			Integer runtime = (Integer)onNowMap.get("sni_runtime");
	  			
	  			//if current show is an hour block, move upNext
	  			if(runtime > 30)
	  				upNext.stepForward();
			} 
	  		
	  	}else{
	  		//in the case where current time slot is null, check one level back to see if we are the second half of an hour tv show
	  		 onNow.stepBack();
	  		 onNowMap = processEpisode(onNow.getSchedulePage(pageManager), false, isMobile);
		  	
		  	if(onNowMap != null) {
		  			//since we took a step back with the OnNow show we must update the JustOn show
		  			justOn.stepBack();
				} 
	  	}
	  	episodesMap.put("second", onNowMap);
		
		
	  	/***
	  	 * Next Calculate Just On episode
	  	 */
	  	//Stores the 'Just On' episode information
	 	Map<String, Object> justOnMap = null;
	 	
	 	//To check for the 'Just On' episode we need to traverse at least 2hrs backward
	 	for(int i=0; i < 4; i++)
	 	{
	  		justOnMap = processEpisode(justOn.getSchedulePage(pageManager), true, isMobile);
	  		if(justOnMap != null)
	  			break;
	  		else
	  			justOn.stepBack();
	  	}
	  	episodesMap.put("first", justOnMap);
	  	
	  	/***
	  	 * Calculate Up Next
	  	 */
	  	//Store 'Up Next' Episode information here
	  	Map<String, Object> onNextMap = null;
	  	
	  	//Must also check at least 2hrs forward to get the 'Up Next' episode
	  	for(int j=0; j<4; j++)
	  	{
	  		onNextMap = processEpisode(upNext.getSchedulePage(pageManager),false, isMobile);
	  		if(onNextMap != null)//found a valid episode entry
	  			break;
	  		else
	  			upNext.stepForward();
	  	}
	  	episodesMap.put("third", onNextMap);
	  	
	  	return episodesMap;
	  	
	}
	
	public static Map<String, Object> processEpisode(Page schedule, boolean getRecipes, boolean isMobile){
		
	
		boolean mobileRequest = isMobile;
		Map<String, Object> map=new HashMap<String,Object>();
		Map<String, String> recipeMap = new TreeMap<String, String>();
		String episodePath;
		Page talent;

			try{
				
				if(schedule != null)
				{
					PageManager pgManager = schedule.getPageManager();
					log.debug("Processing Schedule Slot: " + schedule.getPath());
				/*Get the episode asset page that is now playing from sni:episode property from the Schedule node*/
				episodePath=schedule.getProperties().get(Constant.SNI_EPISODE,String.class);
				Page episodeAsset=pgManager.getPage(episodePath);
				if(episodeAsset ==null)
					return null;
				
				/*Get the Episode Description from sni:abstract property from the episode asset page */
				String episodeDescription = episodeAsset.getProperties().get(Constant.SNI_ABSTRACT, "");
				map.put(Constant.EP_DESCRIPTION, episodeDescription);
				
				/*Get the Episode run time from sni:totalRunTime property from the episode asset page */
				String episodeRuntimeString = episodeAsset.getProperties().get(Constant.SNI_RUNTIME, "");
				if(episodeRuntimeString != null && !episodeRuntimeString.isEmpty()){
					Integer runtime = Integer.parseInt(episodeRuntimeString);
					map.put("sni_runtime", runtime);
				}
				
				/*Get the episode title from the jcr:title property on the asset page node */
				String episodeTitle = episodeAsset.getProperties().get(Constant.JCR_TITLE, "");
				map.put(Constant.EP_TITLE, episodeTitle);
				
				/*get episode content path either desktop or mobile */
				String episodeContentPath = getPageLink(episodeAsset);
				episodeContentPath = mobileRequest ? episodeContentPath.replaceFirst(Constant.SHOW_CONTENT_PATH, Constant.SHOW_MOBILE_CONTENT_PATH) : episodeContentPath;
				Page episodeContent = pgManager.getPage(episodeContentPath);
				
				/*save down the episode url */
				if(episodeContent != null)
				{
					map.put(Constant.EP_URL, episodeContent.getPath() + ".html");
				}
			
				
				/***
				 * Get Recipes for the current episode, only if specified
				 */
				if(getRecipes){
				/*Get all the recipes from the Episode */
				
				String[] recipeAssets=DataUtil.getRecipePathsFromEpisodeAssetPath(episodePath, episodeAsset.getContentResource());
				
			
				if(recipeAssets != null){
					//Arrays.sort(recipeAssets, new RecipePathComparator());
					
				for(String recipeAsset : recipeAssets)
				{
					Page recipeAssetPage = pgManager.getPage(recipeAsset);
					
					String recipeContentPath = getPageLink(recipeAssetPage);
					
					//check to see if we need to convert to mobile
					recipeContentPath = mobileRequest ? recipeContentPath.replaceFirst(Constant.RECIPE_CONTENT, Constant.RECIPE_MOBILE_CONTENT) : recipeContentPath;
					
					Page recipeContentPage = recipeContentPath != null ? pgManager.getPage(recipeContentPath) : null;
					if(recipeContentPage != null)
					{
						String recipeTitle = recipeContentPage.getTitle() != null ? recipeContentPage.getTitle() : recipeContentPage.getName();
						log.info("***Recipe Title: " + recipeTitle);
						recipeMap.put(recipeTitle, recipeContentPage.getPath() + ".html");
					}
				}
				}
				
				//add recipe map as an object to overall map
				map.put(Constant.EP_RECEIPES, recipeMap);
				}
				
				
				/*Get the Show page based on the Episode page (2 levels up) */
				String contentShowPath = "";
				Page assetShow= null;

				//no null check is made on episodeAsset because it was already checked way above
				assetShow =episodeAsset.getParent().getParent();
				contentShowPath = getPageLink(assetShow);


				//check to see if we need to convert to a mobile URL
				contentShowPath = mobileRequest ? contentShowPath.replaceFirst(Constant.SHOW_CONTENT_PATH, Constant.SHOW_MOBILE_CONTENT_PATH) : contentShowPath;
				Page showPage = pgManager.getPage(contentShowPath);

				if(assetShow != null && showPage != null)
				{
					map.put(Constant.SH_TITLE, assetShow.getTitle() != null ? assetShow.getTitle() : assetShow.getName());
					map.put(Constant.SH_URL, showPage.getPath() + ".html");
				}
				

				/*Get the Primary Talent of the Show based on the sni:primaryTalent property
				 *  (should resolve to content path to the chef or host) */
				if(assetShow != null && !mobileRequest){
				String primaryTalentPath = assetShow.getProperties().get(Constant.PRIMARYTALENT, "");
				
				if(!primaryTalentPath.isEmpty())
				{
					Page talentAsset = pgManager.getPage(primaryTalentPath);
					
					
				
					
					String talentContentPath = getPageLink(talentAsset);
					talent = pgManager.getPage(talentContentPath);
					if(talent != null){
						map.put(Constant.CH_TITLE, talent.getTitle() != null ? talent.getTitle() : talent.getName());
						map.put(Constant.CH_URL, talent.getPath() + ".html");
						
						/*Get the talent image url from the first image in sni:images*/
						String[] talent_images = talent.getProperties().get("sni:images", String[].class);
						if(talent_images != null && talent_images.length >0)
						{
							map.put("talent_imageURL",talent_images[0]);
						}
						else{
							talent_images = talentAsset.getProperties().get("sni:images", String[].class);
							if(talent_images != null && talent_images.length > 0)
								map.put("talent_imageURL", talent_images[0]);
						}
						
					}
				}
				}
				
				int nodeTime = Integer.parseInt(schedule.getName());
				String time=Constant.onTvTime[nodeTime-1];
				map.put(Constant.TIME,time.substring(0,time.length()-3));
				map.put(Constant.AM_PM,time.substring(time.length()-2,time.length()));

				return map;
				
				}
			}catch(Exception ex){
				
				log.error("exception occurred ********************"+ex.getMessage()
                        + " SchedulePath: "
                        + schedule.getPath() );
				
			}
			return null;
			
			

	}
	
    private static class RecipePathComparator implements Comparator<String> {
    	
    	@Override
    	public int compare(String path1, String path2) {
    		String[] tempArr = path1.split("/");
    		String name1 = tempArr[tempArr.length -1];
    		
    		tempArr = path2.split("/");
    		String name2 = tempArr[tempArr.length -1];
    		
    		log.info("Name1: " + name1 + " Name2: " + name2);
    		return name1.compareTo(name2);
    	}
    }
    
	/***
	 * Method to get corresponding content page from an sni:asset page
	 * Assumptions: will simply take the first page if multiple page links are present
	 * @param assetPage
	 * @return
	 */
	private static String getPageLink(Page assetPage)
	{
		if(assetPage !=null)
		{
			String[] pageLinks = assetPage.getProperties().get(Constant.PAGELINKS, String[].class);
			return pageLinks !=null && pageLinks.length > 0 ? pageLinks[0] : "";
		}
		return "";
	}
	
	
	private class Schedule {
		public int year;
		public int month;
		public int day;
		public int node;
		public Calendar cal;
		
		public Schedule (int _node)
		{
			cal= Calendar.getInstance();
			//cal.add(Calendar.HOUR, 9);
			//placed in constructor because only needs to be calculated once
			if(_node >= 36 && _node <= 48)
		  	{
		  		//in this case the node is between midnight and 6:00 am of the following day
		  		//however in CQ this node is stored in the previous day, so we step back
		  		cal.add(Calendar.DAY_OF_MONTH, -1);
		  	}
			update(_node);
		}
		
		public void stepBack()
		{
			update( node - 1);
		}
		
		public void stepForward()
		{
			update(node + 1);
		}
		
		private void update(int _node)
		{
			
			node = _node;
			
		  	if(node > 48)
		  	{
		  		node = 1;
		  		cal.add(Calendar.DAY_OF_MONTH, 1);
		  	}
		  	else if(node < 1)
		  	{
		  		//in this case we need to back up a day
		  		node = 48;
		  		cal.add(Calendar.DAY_OF_MONTH, -1);
		  	}
		  	
		  	year = cal.get(Calendar.YEAR);		
			day = cal.get(Calendar.DAY_OF_MONTH);			
			month = cal.get(Calendar.MONTH)+1;
			 
		}
		
		public Page getSchedulePage(PageManager pageManager)
		{
			return pageManager.getPage(getSchedulePath());
		}
		
		public String getSchedulePath()
		{
			return Constant.schedulePathPrefix+ year + Constant.SLASH + month + Constant.SLASH+ day + Constant.SLASH+ node;
			
		}
	}
	
	
	
	
	
	
	/*** OLD CODE. Did not delete because this was a utility class and was not sure which other components used these methods
	 * TODO: CLEAN UP
	 * 
	 * @param node
	 * @param isMobile
	 * @param slingRequest
	 * @param sling
	 * @return
	 */
	public Map<String,Map<String,String>> processTodayEpisodes(Integer node,String isMobile,SlingHttpServletRequest slingRequest,SlingScriptHelper sling){
	
		Map <String,Map<String,String>> episodesMap=new HashMap<String,Map<String,String>>();
		String schedulePath,episodePath,episodeDesc;
		int currentNode=node;
		Query query=null;
		SearchResult result=null;		
		Page schedule,episode,show;
		Resource chefContent,episodeContent,showContent;
		QueryBuilder builder = sling.getService(QueryBuilder.class);
	    Session session = slingRequest.getResourceResolver().adaptTo(Session.class);
	   	ValueMap chefPropMap,episdoePropMap,showPropMap;
	   	
	   	//processing just on episode
	   	if(node-1<1)
	   		node=48;
	   	else
	   		node--;
	   	
	  	processJustOnEpisode(node,episodesMap,isMobile,session,builder,query,result);
		//processing now on episode
		node=currentNode;
		Map <String,String> map=new HashMap<String,String>();
		if(node>=36 && node<=48){
			if(day==1){
				if(month==1){
					month=12;
					month-=1;
				}else{
					month-=1;
				}
				Calendar cal=Calendar.getInstance();
				cal.set(year,month-1,1);
				day=cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			}else{
				day--;
				
			}
		}
		schedulePath=Constant.schedulePathPrefix+ year + Constant.SLASH + month + Constant.SLASH+ day + Constant.SLASH+ node;
		
		try{
			
			schedule=pageManager.getPage(schedulePath);
			
			episodePath=schedule.getProperties().get(Constant.SNI_EPISODE,String.class);
			
			episode=pageManager.getPage(episodePath);
			if(isMobile.equalsIgnoreCase(Constant.FALSE))
				episodeContent=getContentPath(episodePath,Constant.SHOW_CONTENT_PATH,session,builder).getParent();
			else
				episodeContent=getContentPath(episodePath,Constant.SHOW_MOBILE_CONTENT_PATH,session,builder).getParent();
			
			map.put(Constant.EP_URL, episodeContent.getPath()+Constant.HTML);	
			
			episdoePropMap=pageManager.getPage(episodeContent.getPath()).getProperties();
			if(episdoePropMap.get(Constant.JCR_TITLE)!=null){
				map.put(Constant.EP_TITLE, episdoePropMap.get(Constant.JCR_TITLE,String.class));
			}else{
				episdoePropMap=episode.getProperties();
				map.put(Constant.EP_TITLE, episdoePropMap.get(Constant.JCR_TITLE,String.class));
			}
			
			episdoePropMap=pageManager.getPage(episodeContent.getPath()).getProperties();			
			if(episdoePropMap.get(Constant.JCR_DESCRIPTION)!=null){
				episodeDesc=episdoePropMap.get(Constant.JCR_DESCRIPTION,String.class);
				if(episodeDesc.indexOf(" ", 70)>-1)
					episodeDesc=episodeDesc.substring(0,episodeDesc.indexOf(" ", 70));
				map.put(Constant.EP_DESCRIPTION,episodeDesc);
			}else{
				episdoePropMap=episode.getProperties();
				episodeDesc=(episdoePropMap.get(Constant.JCR_DESCRIPTION,String.class)!=null?episdoePropMap.get(Constant.JCR_DESCRIPTION,String.class):"");
				if(episodeDesc.indexOf(" ", 70)>-1)
					episodeDesc=episodeDesc.substring(0,episodeDesc.indexOf(" ", 70));
				map.put(Constant.EP_DESCRIPTION,episodeDesc);
			}
			
			
			show=episode.getParent().getParent();
			if(isMobile.equalsIgnoreCase(Constant.FALSE))
				showContent=getContentPath(show.getPath(),Constant.SHOW_CONTENT_PATH,session,builder).getParent();
			else
				showContent=getContentPath(show.getPath(),Constant.SHOW_MOBILE_CONTENT_PATH,session,builder).getParent();
			
			showPropMap=pageManager.getPage(showContent.getPath()).getProperties();
			if(showPropMap.get(Constant.JCR_TITLE)!=null){
				map.put(Constant.SH_TITLE, showPropMap.get(Constant.JCR_TITLE,String.class));
			}else{
				showPropMap=episode.getProperties();
				map.put(Constant.SH_TITLE, showPropMap.get(Constant.JCR_TITLE,String.class));
			}
			
			map.put(Constant.SH_URL, showContent.getPath()+Constant.HTML);
			
			if(isMobile.equalsIgnoreCase(Constant.FALSE))
				chefContent=getContentPath(show.getProperties().get(Constant.PEOPLE_LINK,String.class),Constant.CHEF_CONTENT_PATH,session,builder);
			else
				chefContent=getContentPath(show.getProperties().get(Constant.PEOPLE_LINK,String.class),Constant.CHEF_MOBILE_CONTENT_PATH,session,builder);
			if(chefContent!=null){
				chefContent=chefContent.getParent();
				chefPropMap=pageManager.getPage(chefContent.getPath()).getProperties();
				if(chefPropMap.get(Constant.JCR_TITLE)!=null){
					map.put(Constant.CH_TITLE,chefPropMap.get(Constant.JCR_TITLE,String.class));
				}else{
					chefPropMap=pageManager.getPage(show.getProperties().get(Constant.PEOPLE_LINK,String.class)).getProperties();
					map.put(Constant.CH_TITLE,chefPropMap.get(Constant.JCR_TITLE,String.class));
				}
				if(chefPropMap.get(Constant.IMAGE_LINK)!=null)
					map.put(Constant.CH_IMAGE,chefPropMap.get(Constant.IMAGE_LINK,String.class));
				map.put(Constant.CH_URL, chefContent.getPath()+Constant.HTML);
			}
			String time=Constant.onTvTime[node-1];
			map.put(Constant.TIME,time.substring(0,time.length()-3));
			map.put(Constant.AM_PM,time.substring(time.length()-2,time.length()));
			episodesMap.put("second",map);
			
			
		}catch(NullPointerException ex){
			
			log.error("exception occer in processTodayEpisodes while processing current time   ********************"+ex.getMessage()+schedulePath);
			
		}
		//processing next episode
		if(node+1>48)
	   		node=1;
	   	else
	   		node++;
		
	   	processNextEpisode(node,episodesMap,isMobile,session,builder,query,result);
	   
		return episodesMap;
	}
	
	private void processJustOnEpisode(int node, Map episodesMap,String isMobile,Session session, QueryBuilder builder,Query query,SearchResult result){
		int scheduleDay=day,scheduelMonth=month,scheduelYear=year;
		Calendar cal=Calendar.getInstance();
		Map map=new HashMap<String,String>();
		String schedulePath,episodePath;
		Page schedule,episode,show;
		Resource chefContent,showContent;
		ValueMap chefPropMap,showPropMap;
		String recipeList[]=null;
		if(node>=35 && node<=48){
			if(scheduleDay==1){
				if(scheduelMonth==1){
					scheduelMonth=12;
					scheduelYear-=1;
				}else{
					scheduelMonth-=1;
				}
				cal.set(scheduelYear,scheduelMonth-1,1);
				scheduleDay=cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			}else{
				scheduleDay--;
				
			}
		}
		
		for(int i=4;i>=1;i--,node--){
			if(node<1){
				if(scheduleDay==1){
					if(scheduelMonth==1){
						scheduelMonth=12;
						scheduelYear-=1;
					}else{
						scheduelMonth-=1;
					}
					cal.set(scheduelYear,scheduelMonth-1,1);
					scheduleDay=cal.getActualMaximum(Calendar.DAY_OF_MONTH);
				}else{
					scheduleDay--;
					
				}
				node=48;
			}
			schedulePath=Constant.schedulePathPrefix+ scheduelYear + Constant.SLASH + scheduelMonth + Constant.SLASH+ scheduleDay + Constant.SLASH+ node;
			
			try{
				
				schedule=pageManager.getPage(schedulePath);
				episodePath=schedule.getProperties().get(Constant.SNI_EPISODE,String.class);
				episode=pageManager.getPage(episodePath);
				show=episode.getParent().getParent();
				if(isMobile.equalsIgnoreCase(Constant.FALSE))
					showContent=getContentPath(show.getPath(),Constant.SHOW_CONTENT_PATH,session,builder).getParent();
				else
					showContent=getContentPath(show.getPath(),Constant.SHOW_MOBILE_CONTENT_PATH,session,builder).getParent();
				
				showPropMap=pageManager.getPage(showContent.getPath()).getProperties();
				if(showPropMap.get(Constant.JCR_TITLE)!=null){
					map.put(Constant.SH_TITLE, showPropMap.get(Constant.JCR_TITLE,String.class));
				}else{
					showPropMap=episode.getProperties();
					map.put(Constant.SH_TITLE, showPropMap.get(Constant.JCR_TITLE,String.class));
				}
				
				map.put(Constant.SH_URL, showContent.getPath()+Constant.HTML);
				
				
				if(isMobile.equalsIgnoreCase(Constant.FALSE))
					chefContent=getContentPath(show.getProperties().get(Constant.PEOPLE_LINK,String.class),Constant.CHEF_CONTENT_PATH,session,builder);
				else
					chefContent=getContentPath(show.getProperties().get(Constant.PEOPLE_LINK,String.class),Constant.CHEF_MOBILE_CONTENT_PATH,session,builder);
							
				if(chefContent!=null){
					chefContent=chefContent.getParent();
					chefPropMap=pageManager.getPage(chefContent.getPath()).getProperties();
					if(chefPropMap.get(Constant.JCR_TITLE)!=null){
						map.put(Constant.CH_TITLE,chefPropMap.get(Constant.JCR_TITLE));
					}else{
						chefPropMap=pageManager.getPage(show.getProperties().get(Constant.PEOPLE_LINK,String.class)).getProperties();
						map.put(Constant.CH_TITLE,chefPropMap.get(Constant.JCR_TITLE));
					}
					map.put(Constant.CH_URL, chefContent.getPath()+Constant.HTML);
				}
				recipeList=DataUtil.getRecipePathsFromEpisodeAssetPath(episodePath, showContent);
				if(recipeList!=null && recipeList.length>0)
					map.put(Constant.EP_RECEIPES, getRecipeContents(recipeList,isMobile,session,builder));
				
				String time=Constant.onTvTime[node-1];
				map.put(Constant.TIME,time.substring(0,time.length()-3));
				map.put(Constant.AM_PM,time.substring(time.length()-2,time.length()));
				episodesMap.put("first",map);
				break;
				
			}catch(Exception ex){
				
				log.error("exception occer in processJustOnEpisode just on ********************"+ex.getMessage()+schedulePath);
				continue;
			}
		}
	}
	
	private void processNextEpisode(int node, Map episodesMap,String isMobile,Session session, QueryBuilder builder,Query query,SearchResult result){
		int scheduleDay=day,scheduelMonth=month,scheduelYear=year;
		
		Calendar cal=Calendar.getInstance();
		Map <String,String> map=new HashMap<String,String>();
		String schedulePath,episodePath;
		Page schedule,episode,show;
		Resource episodeContent,showContent;
		ValueMap episdoePropMap,showPropMap;
		
		if(node>36 && node<=48){
			if(scheduleDay==1){
				if(scheduelMonth==1){
					scheduelMonth=12;
					scheduelYear-=1;
				}else{
					scheduelMonth-=1;
				}
				cal.set(scheduelYear,scheduelMonth-1,1);
				scheduleDay=cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			}else{
				scheduleDay--;
				
			}
		}
		
		for(int i=4;i>=1;i--,node++){
			if(node>48){
				if(scheduleDay==days){
					if(scheduelMonth==12){
						scheduelMonth=1;
						scheduelYear+=1;
					}else{
						scheduelMonth+=1;
					}
					cal.set(scheduelYear,scheduelMonth-1,1);
					scheduleDay=cal.getActualMaximum(Calendar.DAY_OF_MONTH);
				}else{
					scheduleDay++;
					
				}
				node=1;
			}
			schedulePath=Constant.schedulePathPrefix+ scheduelYear + Constant.SLASH + scheduelMonth + Constant.SLASH+ scheduleDay + Constant.SLASH+ node;
			
			try{
				
				schedule=pageManager.getPage(schedulePath);
				episodePath=schedule.getProperties().get(Constant.SNI_EPISODE,String.class);
				episode=pageManager.getPage(episodePath);
				if(isMobile.equalsIgnoreCase(Constant.FALSE))
					episodeContent=getContentPath(episodePath,Constant.SHOW_CONTENT_PATH,session,builder).getParent();
				else
					episodeContent=getContentPath(episodePath,Constant.SHOW_MOBILE_CONTENT_PATH,session,builder).getParent();
				
				episdoePropMap=pageManager.getPage(episodeContent.getPath()).getProperties();
				if(episdoePropMap.get(Constant.JCR_TITLE)!=null){
					map.put(Constant.EP_TITLE, episdoePropMap.get(Constant.JCR_TITLE,String.class));
				}else{
					episdoePropMap=episode.getProperties();
					map.put(Constant.EP_TITLE, episdoePropMap.get(Constant.JCR_TITLE,String.class));
				}
				map.put(Constant.EP_URL, episodeContent.getPath()+Constant.HTML);
				show=episode.getParent().getParent();
				if(isMobile.equalsIgnoreCase(Constant.FALSE))
					showContent=getContentPath(show.getPath(),Constant.SHOW_CONTENT_PATH,session,builder).getParent();
				else
					showContent=getContentPath(show.getPath(),Constant.SHOW_MOBILE_CONTENT_PATH,session,builder).getParent();
				
				showPropMap=pageManager.getPage(showContent.getPath()).getProperties();
				if(showPropMap.get(Constant.JCR_TITLE)!=null){
					map.put(Constant.SH_TITLE, showPropMap.get(Constant.JCR_TITLE,String.class));
				}else{
					showPropMap=episode.getProperties();
					map.put(Constant.SH_TITLE, showPropMap.get(Constant.JCR_TITLE,String.class));
				}
				
				map.put(Constant.SH_URL, showContent.getPath()+Constant.HTML);
				map.put(Constant.TIME,Constant.onTvTime[node-1]);
				episodesMap.put("third",map);
				break;
				
			}catch(Exception ex){
				
				log.debug("exception occer in processJustOnEpisode next episode ********************"+ex.getMessage()+schedulePath);
				continue;
			}
		}
	}
	
	/*
	 * method to retrieve list of up coming episodes at given period or time
	 * @param noe
	 * @return
	 */
	
	public List<Map<String,String>> processUpComingEpisodes(Integer node,final List<String>showNames,final String pageType){
		
		String schedulePath,episodePath,title,episodeContentPagePath,showName,showPath;
		Page schedule,episode,show,episodeContentPage;
		int index=0;
		Map <String,String>map=null;
		
		int scheduleDay=day,scheduelMonth=month,scheduelYear=year;
		int k=day+6;
		boolean flag=true;
		Calendar cal=Calendar.getInstance();
		
		if(node==36){
			if(day==days){
				if(month==12){
					day=1;
					month=1;
					year+=1;
				}else{
					day=1;
					month+=1;
				}
			}else{
				day+=1;
				
			}
				
		}else if(node>36){
			if(scheduleDay==1){
				if(scheduelMonth==1){
					scheduelMonth=12;
					scheduelYear-=1;
				}else{
					scheduelMonth-=1;
				}
				cal.set(scheduelYear,scheduelMonth-1,1);
				scheduleDay=cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			}else{
				scheduleDay--;
				day--;
			}
			
			 
		}
		
		
		for(int j=day,counter=0;j<=k;j++,counter++){
			
			if(counter!=0){
				node=1;
				if(j>days){
					day=j-days;
					if(flag==true){
						if(month==12){
							month=1;
							year++;
							
						}else{
							month++;
						}
						flag=false;
					}
					scheduleDay=day;
				}else{
					if(scheduelMonth!=month){	
						
						day=j-1;
						scheduleDay=j-1;
					}else{
						day=j;
						scheduleDay=j;
					}
				}
				if(scheduelMonth!=month){
					scheduelMonth=month;
					scheduelYear=year;	
				}
			}
			
			for(int i=node;i<=48;i++){
				
				if(day==scheduleDay && i>=36){
					day++;
				}
				schedulePath=Constant.schedulePathPrefix+ scheduelYear + Constant.SLASH + scheduelMonth + Constant.SLASH+ scheduleDay + Constant.SLASH+ i;
				
				try{
					
					schedule=pageManager.getPage(schedulePath);
					episodePath=schedule.getProperties().get(Constant.SNI_EPISODE,String.class);
					episode=pageManager.getPage(episodePath); 
					show=episode.getParent().getParent();
					showName=show.getName();
					
					if((showName.equalsIgnoreCase(page.getName())||showNames.contains(showName)) && list.size()<count){
						
						if(pageType.equalsIgnoreCase(Constant.CHEF_PAGE_TYPE)){
												
							showPath=Functions.getBasePath(show.getPath().replaceFirst(Constant.SHOW_ASSET_PATH, Constant.SHOW_CONTENT_PATH));
							page=pageManager.getPage(showPath);
						}
							
						map=new HashMap<String,String>();
						String pageTitle=show.getProperties().get(Constant.JCR_TITLE,String.class);
						if(page.getProperties().get(Constant.JCR_TITLE,String.class)!=null)
							pageTitle=page.getProperties().get(Constant.JCR_TITLE,String.class);
						map.put(Constant.PG_TITLE, pageTitle);
						title=episode.getProperties().get(Constant.JCR_TITLE,String.class);
						episodeContentPagePath=page.getPath()+Constant.SLASH+episode.getParent().getName()+Constant.SLASH+episode.getName();
						episodeContentPage=pageManager.getPage(episodeContentPagePath);	
						if(episodeContentPage.getProperties().get(Constant.JCR_TITLE,String.class)!=null)
							title=episodeContentPage.getProperties().get(Constant.JCR_TITLE,String.class);
													
						map.put(Constant.EP_TITLE, title);
						map.put(Constant.URL, episodeContentPagePath+Constant.HTML);
						now.set(year,month-1,day);
						map.put(Constant.TIME, getDayTime(now,i-1));
						list.add(index,map);
						index++;
										
					}else if(list.size()==count)
						break;	
				}catch(NullPointerException ex){
					log.debug("exception occer in processUpComingEpisode continue"+ex.getMessage()+"  "+schedulePath);
					continue;
				}catch(Exception ex){
					log.error("exception occer in processUpComingEpisode breaking"+ex.getMessage()+"  "+schedulePath);
					break;
				}
				
				if(node==35)
					break;
				
			}
			if(list.size()==count)
				break;
		}
		
		
		return list;
	}
	
	
	/*
	 * method to retrieve list of recently aired episodes at given period or time
	 * @param noe
	 * @return
	 */
	public List<Map<String,String>> processRecentEpisodes(Integer node,final List<String>showNames,final String pageType){
	
		String schedulePath,episodePath,title,episodeContentPagePath,showName,showPath;
		Page schedule,episode,show,episodeContentPage;
		Calendar cal=Calendar.getInstance();
		
		int index=0;
		Map <String,String>map=null;
		int temp=day;
		int scheduleDay=day,scheduelMonth=month,scheduelYear=year;
		int k=(!(temp-8>-1))?0:1;
		if(k==1)
			k=temp-8;
		
		 if(node>=36){
				if(scheduleDay==1){
					if(scheduelMonth==1){
						scheduelMonth=12;
						scheduelYear-=1;
					}else{
						scheduelMonth-=1;
					}
					cal.set(scheduelYear,scheduelMonth-1,1);
					scheduleDay=cal.getActualMaximum(Calendar.DAY_OF_MONTH);
				}else{
					scheduleDay--;
				}
				
				
			}
		
		
		for(int j=day;j>=k;j--){
			
			if(j==0){
				
				if(month==1){
					month=12;
					year-=1;
				}else{
					month-=1;
				}
				
				
				cal.set(year,month-1,1);
				j=cal.getActualMaximum(Calendar.DAY_OF_MONTH);
				k=j-1+(temp-8);
				day=1;
				scheduleDay=j;
				scheduelMonth=month;
				scheduelYear=year;
				node=48;  
				
			}else if(day>j){
				
				node=48;
				day=j+1;
				scheduleDay=j; 
			} 
			
			
			
			for(int i=48;i>=1;i--,node--){
				if(node<1)
					break; 
				
				if(day!=scheduleDay && node<=35){
					day=scheduleDay;
					
				}
				schedulePath=Constant.schedulePathPrefix+ scheduelYear + Constant.SLASH + scheduelMonth + Constant.SLASH+ scheduleDay + Constant.SLASH+ node;
				
				try{
					
					schedule=pageManager.getPage(schedulePath);
					episodePath=schedule.getProperties().get(Constant.SNI_EPISODE,String.class);
					episode=pageManager.getPage(episodePath);
					show=episode.getParent().getParent();
					showName=show.getName();
					
					if((showName.equalsIgnoreCase(page.getName())||showNames.contains(showName)) && list.size()<count){
						
						if(pageType.equalsIgnoreCase(Constant.CHEF_PAGE_TYPE)){
							
							showPath=Functions.getBasePath(show.getPath().replaceFirst(Constant.SHOW_ASSET_PATH, Constant.SHOW_CONTENT_PATH));
							page=pageManager.getPage(showPath);
						}
						map=new HashMap<String,String>();
						String pageTitle=show.getProperties().get(Constant.JCR_TITLE,String.class);
						if(page.getProperties().get(Constant.JCR_TITLE,String.class)!=null)
							pageTitle=page.getProperties().get(Constant.JCR_TITLE,String.class);
						map.put(Constant.PG_TITLE, pageTitle);
						title=episode.getProperties().get(Constant.JCR_TITLE,String.class);
						episodeContentPagePath=page.getPath()+Constant.SLASH+episode.getParent().getName()+Constant.SLASH+episode.getName();
						episodeContentPage=pageManager.getPage(episodeContentPagePath);	
						if(episodeContentPage.getProperties().get(Constant.JCR_TITLE,String.class)!=null)
							title=episodeContentPage.getProperties().get(Constant.JCR_TITLE,String.class);
						
						map.put(Constant.EP_TITLE, title);
						map.put(Constant.URL, episodeContentPagePath+Constant.HTML);
						now.set(year,month-1,day);
						map.put(Constant.TIME, getDayTime(now,node-1));
						list.add(index,map);
						
						index++;
											
					}else if(list.size()==count)
						break;	
				}catch(NullPointerException ex){
					log.debug("exception occer in processRecentEpisode "+ex.getMessage()+"  "+schedulePath);
					continue;
				}catch(Exception ex){
					log.error("exception occer in processRecentEpisode "+ex.getMessage()+"  "+schedulePath);
					break;
				}
			}
			if(list.size()==count)
				break;
		}
		return list;
	}
	
	
	/*
	 * method to retrieve list of episodes for current week
	 * @param isMobile
	 * @param slingRequest
	 * @param sling
	 * @param pageType
	 * @return
	 */
	
	public Map<String,List> processEpisodesThisWeek(String isMobile,SlingHttpServletRequest slingRequest,SlingScriptHelper sling,String pageType){
		
		Calendar cal=now;
		int today=cal.get(cal.DAY_OF_WEEK);
		int futureDays=7-today;
		int backDays=7-(futureDays+1);
		Map <String,List> mainList=new TreeMap <String,List> ();
		QueryBuilder builder = sling.getService(QueryBuilder.class);
	    Session session = slingRequest.getResourceResolver().adaptTo(Session.class);
	    List <String> showNames;
	    
	    if(pageType.equalsIgnoreCase(Constant.CHEF_PAGE_TYPE)){
	    	 
	    	String chef=page.getProperties().get(Constant.ASSET_LINK,String.class);
	    	if(chef == null || chef.isEmpty())
	    		return mainList;
	    	
	    	showNames=getShowNames(chef,session,builder);
	    	processBackDays(backDays,cal,mainList,isMobile,session,builder,pageType,showNames);
	    	cal=Calendar.getInstance();
	    	processFutureDays(futureDays,today,cal,mainList,isMobile,session,builder,pageType,showNames);
	    }else if(pageType.equalsIgnoreCase(Constant.SHOW_PAGE_TYPE)){
	    	
			processBackDays(backDays,cal,mainList,isMobile,session,builder);
			cal=Calendar.getInstance();
			processFutureDays(futureDays,today,cal,mainList,isMobile,session,builder);
			
	    }
		
		return mainList;
		
	}
	
	/*
	 * method to retrieve list of episodes from past and current day of week (show page)
	 * @param backDays
	 * @param cal
	 * @param mainList
	 * @param isMobile
	 * @param session
	 * @param builder
	 * 
	 */
	
	private void processBackDays(int backDays,Calendar cal,Map mainList,String isMobile,Session session, QueryBuilder builder){
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH)+1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int scheduleDay=day,scheduelMonth=month,scheduelYear=year;
		String schedulePath,episodePath,title,episodeContentPagePath,description;
		String recipeList[]=null;
		Page schedule,episode,show,episodeContentPage;
		ValueMap pageContent,episodeAsset;
		Query query=null;
		SearchResult result=null;
		int temp=day;
		int temp1=backDays;
		
		int k=(!(temp-backDays>-1))?0:1;
		if(k==1)
			k=temp-backDays;
		if(scheduleDay==1){
			if(scheduelMonth==1){
				scheduelMonth=12;
				scheduelYear-=1;
			}else{
				scheduelMonth-=1;
			}
			cal.set(scheduelYear,scheduelMonth-1,1);
			scheduleDay=cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		}
		
		List <Map<String,String>> localList=new ArrayList<Map<String,String>>();
		Map map=new HashMap<String,String>();
		
		for(int j=day;j>=k;j--,temp1--){			
			if(j==0){
				if(month==1){
					month=12;
					year-=1; 
					
				}else{
					month-=1;
				}
				Calendar newCal=Calendar.getInstance();
				newCal.set(year,month-1,1);
				j=newCal.getActualMaximum(Calendar.DAY_OF_MONTH);
				k=j+(temp-backDays);
				scheduelMonth=month;
				scheduelYear=year;
							
			} 
			
			if(j!=1){
				scheduleDay=j-1;
			}
				
			
			
			
			
			for(int i=1,node=36,index=0;i<=48;i++,node++){
				if(node>48 && scheduleDay!=j){
					scheduelYear=year;
					scheduelMonth=month;
					scheduleDay=j;
					node=1;
				} 
				
				schedulePath=Constant.schedulePathPrefix+ scheduelYear + Constant.SLASH + scheduelMonth + Constant.SLASH+ scheduleDay + Constant.SLASH+ node;
								
				try{
					
					schedule=pageManager.getPage(schedulePath);
					episodePath=schedule.getProperties().get(Constant.SNI_EPISODE,String.class);
					episode=pageManager.getPage(episodePath);
					show=episode.getParent().getParent();
					
					if(show.getName().equalsIgnoreCase(page.getName())){
						
						episodeContentPagePath=page.getPath()+Constant.SLASH+episode.getParent().getName()+Constant.SLASH+episode.getName();
						episodeContentPage=pageManager.getPage(episodeContentPagePath);	
						pageContent=episodeContentPage.getProperties();
						episodeAsset=episode.getProperties();
						title=(null!=pageContent.get(Constant.JCR_TITLE,String.class))?pageContent.get(Constant.JCR_TITLE,String.class):episodeAsset.get(Constant.JCR_TITLE,String.class);
						description=(null!=pageContent.get(Constant.JCR_DESCRIPTION,String.class))?pageContent.get(Constant.JCR_DESCRIPTION,String.class):((episodeAsset.get(Constant.JCR_DESCRIPTION,String.class)!=null)?episodeAsset.get(Constant.JCR_DESCRIPTION,String.class):"");
						recipeList=DataUtil.getRecipePathsFromEpisodeAssetPath(episodePath, episode.getContentResource());
						
						if(recipeList!=null && recipeList.length>0)
							map.put(Constant.EP_RECEIPES, getRecipeContents(recipeList,isMobile,session,builder));
						map.put(Constant.WEEK_DAY,month+Constant.SLASH+j);
						map.put(Constant.EP_TITLE, title);
						if(description.indexOf(" ", 115)>-1 && isMobile.equalsIgnoreCase(Constant.FALSE))
							description=description.substring(0,description.indexOf(" ", 115));
						
						map.put(Constant.EP_DESCRIPTION, description);
						map.put(Constant.URL, episodeContentPagePath+Constant.HTML);
						map.put(Constant.TIME, getOnTVTime(node-1,isMobile));
						localList.add(index,map);
						index++;
						
											
					}	
				}catch(NullPointerException ex){
					
					log.debug("exception occer in show processBackDays continue ********************"+ex.getMessage()+schedulePath);
					continue;
				}catch(Exception ex){
					
					log.error("exception occer in show processBackDays break ************************ "+ex.getMessage()+schedulePath);
					break;
				}
				
				map=new HashMap<String,String>();
			}
			if(localList.size()>0){
				mainList.put(temp1,localList);
			}
			
			localList=new ArrayList<Map<String,String>>();
			
		}
		
		
	}
	
	/*
	 * method to retrieve list of episodes from past and current day of week (Talent page)
	 * @param backDays
	 * @param cal
	 * @param mainList
	 * @param isMobile
	 * @param session
	 * @param builder
	 * @param pageType
	 * @param showNames
	 * 
	 */
	private void processBackDays(int backDays,Calendar cal,Map mainList,String isMobile,Session session, QueryBuilder builder,String pageType,List showNames){
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH)+1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int scheduleDay=day,scheduelMonth=month,scheduelYear=year;
		String schedulePath,episodePath,title,description;
		Page schedule,episode;
		Resource episodeContent,showContent;
		String show;
		ValueMap episodeMap,episodeAsset,showMap;
		Query query=null;
		SearchResult result=null;
		int temp=day;
		int temp1=backDays;
		String recipeList[]=null;
		int k=(!(temp-backDays>-1))?0:1;
		if(k==1)
			k=temp-backDays;
		if(scheduleDay==1){
			if(scheduelMonth==1){
				scheduelMonth=12;
				scheduelYear-=1;
			}else{
				scheduelMonth-=1;
			}
			cal.set(scheduelYear,scheduelMonth-1,1);
			scheduleDay=cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		}
	    
		List <Map<String,String>> localList=new ArrayList<Map<String,String>>();
		
		Map map=new HashMap<String,String>();
		
		for(int j=day;j>=k;j--,temp1--){			
			if(j==0){
				if(month==1){
					month=12;
					year-=1; 
					
				}else{
					month-=1;
				}
				Calendar newCal=Calendar.getInstance();
				newCal.set(year,month-1,1);
				j=newCal.getActualMaximum(Calendar.DAY_OF_MONTH);
				k=j+(temp-backDays);
				scheduelMonth=month;
				scheduelYear=year;
							
			} 
			
			if(j!=1){
				scheduleDay=j-1;
			}
				
			
			
			
			
			for(int i=1,node=36,index=0;i<=48;i++,node++){
				if(node>48 && scheduleDay!=j){
					scheduelYear=year;
					scheduelMonth=month;
					scheduleDay=j;
					node=1;
				} 
				
				schedulePath=Constant.schedulePathPrefix+ scheduelYear + Constant.SLASH + scheduelMonth + Constant.SLASH+ scheduleDay + Constant.SLASH+ node;
				
				
				try{
					
					schedule=pageManager.getPage(schedulePath);
					episodePath=schedule.getProperties().get(Constant.SNI_EPISODE,String.class);
					
					episode=pageManager.getPage(episodePath);
					show=episode.getParent().getParent().getName();
					
					if(showNames.contains(show)){
						
						episodeAsset=episode.getProperties();
						
						if(isMobile.equalsIgnoreCase(Constant.FALSE))
							episodeContent=getContentPath(episodePath,Constant.SHOW_CONTENT_PATH,session,builder).getParent();
						else
							episodeContent=getContentPath(episodePath,Constant.SHOW_MOBILE_CONTENT_PATH,session,builder).getParent();
						episodeMap=pageManager.getPage(episodeContent.getPath()).getProperties();
						showContent=episodeContent.getParent().getParent();
						
						showMap=pageManager.getPage(showContent.getPath()).getProperties();
						title=(null!=episodeMap.get(Constant.JCR_TITLE,String.class))?episodeMap.get(Constant.JCR_TITLE,String.class):episodeAsset.get(Constant.JCR_TITLE,String.class);
						description=(null!=episodeMap.get(Constant.JCR_DESCRIPTION,String.class))?episodeMap.get(Constant.JCR_DESCRIPTION,String.class):((episodeAsset.get(Constant.JCR_DESCRIPTION,String.class)!=null)?episodeAsset.get(Constant.JCR_DESCRIPTION,String.class):"");
						
						map.put(Constant.SH_TITLE, showMap.get(Constant.JCR_TITLE,String.class));
						map.put(Constant.SH_URL,showContent.getPath()+Constant.HTML);
						
						recipeList=DataUtil.getRecipePathsFromEpisodeAssetPath(episodePath, episodeContent);
						
						if(recipeList!=null && recipeList.length>0)
							map.put(Constant.EP_RECEIPES, getRecipeContents(recipeList,isMobile,session,builder));
						map.put(Constant.WEEK_DAY,month+Constant.SLASH+j);
						map.put(Constant.EP_TITLE, title);
						if(description.indexOf(" ", 115)>-1 && isMobile.equalsIgnoreCase(Constant.FALSE))
							description=description.substring(0,description.indexOf(" ", 115));
						map.put(Constant.EP_DESCRIPTION, description);
						map.put(Constant.URL, episodeContent.getPath()+Constant.HTML);
						map.put(Constant.TIME, getOnTVTime(node-1,isMobile));
						localList.add(index,map);
						
						index++;
						
											
					}	
				}catch(NullPointerException ex){
					
					log.debug("exception occer in talent processBackDays continue ********************"+ex.getMessage()+schedulePath);
					continue;
				}catch(Exception ex){
					
					log.error("exception occer in talent processBackDays break************************ "+ex.getMessage()+schedulePath);
					break;
				}
				
				map=new HashMap<String,String>();
			}
			if(localList.size()>0){
				mainList.put(temp1,localList);
			}
			
			localList=new ArrayList<Map<String,String>>();
			
		}
		
		
	}
	
	/*
	 * method to retrieve list of episodes from furture days (talent page)
	 * @param forwardDays
	 * @param cal
	 * @param mainList
	 * @param isMobile
	 * @param session
	 * @param builder
	 * @param pageType
	 * @param showNames
	 * 
	 */
	
	private void processFutureDays(int forwardDays,int today,Calendar cal,Map mainList,String isMobile,Session session, QueryBuilder builder,String pageType,List showNames){
		int year = cal.get(Calendar.YEAR);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int month = cal.get(Calendar.MONTH)+1;
		int scheduleDay=day,scheduelMonth=month,scheduelYear=year;
		String recipeList[]=null;
		Query query=null;
		SearchResult result=null;
		ValueMap episodeMap,episodeAsset,showMap;
		int k=day+forwardDays;
		String schedulePath,episodePath,title,description;
		Page schedule,episode;
		Resource episodeContent,showContent;
		String show;
		List <Map<String,String>> localList=new ArrayList<Map<String,String>>();
		boolean flag=true;
		Map map=new HashMap<String,String>();
		
		for(int j=day+1;j<=k;j++,today++){			
			
				
				if(j>days){
					day=j-days;
					if(flag==true){
						if(month==12){
							month=1;
							year+=1;
						}else{
							month+=1;
						}
						flag=false; 
					}
				}else{
					day=j;
				}
			
			
			for(int i=1,node=36,index=0;i<=48;i++,node++){
				if(node>48 && scheduleDay!=day){
					scheduelYear=year;
					scheduelMonth=month;
					scheduleDay=day;
					node=1;
				} 
				schedulePath=Constant.schedulePathPrefix+ scheduelYear + Constant.SLASH + scheduelMonth + Constant.SLASH+ scheduleDay + Constant.SLASH + node;
				
				try{
					
					schedule=pageManager.getPage(schedulePath);
					episodePath=schedule.getProperties().get(Constant.SNI_EPISODE,String.class);
					episode=pageManager.getPage(episodePath);
					show=episode.getParent().getParent().getName();
					
					if(showNames.contains(show)){
						
						episodeAsset=episode.getProperties();
						
						if(isMobile.equalsIgnoreCase(Constant.FALSE))
							episodeContent=getContentPath(episodePath,Constant.SHOW_CONTENT_PATH,session,builder).getParent();
						else
							episodeContent=getContentPath(episodePath,Constant.SHOW_MOBILE_CONTENT_PATH,session,builder).getParent();
						episodeMap=pageManager.getPage(episodeContent.getPath()).getProperties();
						showContent=episodeContent.getParent().getParent();
						
						showMap=pageManager.getPage(showContent.getPath()).getProperties();
						title=(null!=episodeMap.get(Constant.JCR_TITLE,String.class))?episodeMap.get(Constant.JCR_TITLE,String.class):episodeAsset.get(Constant.JCR_TITLE,String.class);
						description=(null!=episodeMap.get(Constant.JCR_DESCRIPTION,String.class))?episodeMap.get(Constant.JCR_DESCRIPTION,String.class):((episodeAsset.get(Constant.JCR_DESCRIPTION,String.class)!=null)?episodeAsset.get(Constant.JCR_DESCRIPTION,String.class):"");
						
						map.put(Constant.SH_TITLE, showMap.get(Constant.JCR_TITLE,String.class));
						map.put(Constant.SH_URL,showContent.getPath()+Constant.HTML);
						recipeList=DataUtil.getRecipePathsFromEpisodeAssetPath(episodePath, episodeContent);
						if(recipeList!=null && recipeList.length>0)
							map.put(Constant.EP_RECEIPES, getRecipeContents(recipeList,isMobile,session,builder));
						map.put(Constant.WEEK_DAY,month+Constant.SLASH+day);
						map.put(Constant.EP_TITLE, title);
						if(description.indexOf(" ", 115)>-1 && isMobile.equalsIgnoreCase(Constant.FALSE))
							description=description.substring(0,description.indexOf(" ", 115));
						map.put(Constant.EP_DESCRIPTION, description);
						map.put(Constant.URL, episodeContent.getPath()+Constant.HTML);
						map.put(Constant.TIME, getOnTVTime(node-1,isMobile));
						localList.add(index,map);
						
						index++;
						
											
					}	
				}catch(NullPointerException ex){
					
					log.debug("exception occer in talent processFutureDays continue "+ex.getMessage()+schedulePath);
					continue;
				}catch(Exception ex){
					
					log.error("exception occer in talent processFutureDays break "+ex.getMessage()+schedulePath);
					break;
				}
				map=new HashMap<String,String>();
			}
			if(localList.size()>0)
				mainList.put(today,localList);
			
			localList=new ArrayList<Map<String,String>>();
			
		}
		
		
	}
	
	/*
	 * method to retrieve list of episodes from future days of week (show page)
	 * @param forwardDays
	 * @param cal
	 * @param mainList
	 * @param isMobile
	 * @param session
	 * @param builder
	 * 
	 */
	
	private void processFutureDays(int forwardDays,int today,Calendar cal,Map mainList,String isMobile,Session session, QueryBuilder builder){
		int year = cal.get(Calendar.YEAR);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int month = cal.get(Calendar.MONTH)+1;
		int scheduleDay=day,scheduelMonth=month,scheduelYear=year;
		Query query=null;
		SearchResult result=null;
		ValueMap pageContent,episodeAsset;
		int k=day+forwardDays;
		String schedulePath,episodePath,title,episodeContentPagePath,description;
		Page schedule,episode,show,episodeContentPage;
		boolean flag=true;
		List <Map<String,String>> localList=new ArrayList<Map<String,String>>();
		String recipeList[]=null;
		Map map=new HashMap<String,String>();
		
		for(int j=day+1;j<=k;j++,today++){			
			if(j>days){
				day=j-days;
				if(flag==true){
					if(month==12){
						month=1;
						year+=1;
					}else{
						month+=1;
					}
					flag=false; 
				}
			}else{
				day=j;
			}
			for(int i=1,node=36,index=0;i<=48;i++,node++){
				if(node>48 && scheduleDay!=day){
					scheduelYear=year;
					scheduelMonth=month;
					scheduleDay=day;
					node=1;
				} 
				schedulePath=Constant.schedulePathPrefix+ scheduelYear + Constant.SLASH + scheduelMonth + Constant.SLASH+ scheduleDay + Constant.SLASH + node;
				
				try{
					
					schedule=pageManager.getPage(schedulePath);
					episodePath=schedule.getProperties().get(Constant.SNI_EPISODE,String.class);
					episode=pageManager.getPage(episodePath);
					show=episode.getParent().getParent();
					
					if(show.getName().equalsIgnoreCase(page.getName())){
						
						episodeContentPagePath=page.getPath()+Constant.SLASH+episode.getParent().getName()+Constant.SLASH+episode.getName();
						episodeContentPage=pageManager.getPage(episodeContentPagePath);	
						pageContent=episodeContentPage.getProperties();
						episodeAsset=episode.getProperties();
						title=(null!=pageContent.get(Constant.JCR_TITLE,String.class))?pageContent.get(Constant.JCR_TITLE,String.class):episodeAsset.get(Constant.JCR_TITLE,String.class);
						description=(null!=pageContent.get(Constant.JCR_DESCRIPTION,String.class))?pageContent.get(Constant.JCR_DESCRIPTION,String.class):((episodeAsset.get(Constant.JCR_DESCRIPTION,String.class)!=null)?episodeAsset.get(Constant.JCR_DESCRIPTION,String.class):"");
						recipeList=DataUtil.getRecipePathsFromEpisodeAssetPath(episodePath, episode.getContentResource());
						if(recipeList!=null && recipeList.length>0)
							map.put(Constant.EP_RECEIPES, getRecipeContents(recipeList,isMobile,session,builder));
						map.put(Constant.WEEK_DAY,month+Constant.SLASH+day);
						map.put(Constant.EP_TITLE, title);
						if(description.indexOf(" ", 115)>-1 && isMobile.equalsIgnoreCase(Constant.FALSE))
							description=description.substring(0,description.indexOf(" ", 115));
						map.put(Constant.EP_DESCRIPTION, description);
						map.put(Constant.URL, episodeContentPagePath+Constant.HTML);
						map.put(Constant.TIME, getOnTVTime(node-1,isMobile));
						localList.add(index,map);
						index++;
						
											
					}	
				}catch(NullPointerException ex){
					
					log.debug("exception occer in show processFutureDays continue "+ex.getMessage()+schedulePath);
					continue;
				}catch(Exception ex){
					
					log.error("exception occer in show processFutureDays break "+ex.getMessage()+schedulePath);
					break;
				}
				map=new HashMap<String,String>();
			}
			if(localList.size()>0)
				mainList.put(today,localList);
			
			localList=new ArrayList<Map<String,String>>();
			
		}
		
		
	}
	
	
	
	
	
		
	
	/*
	 * method to retrieve time in "11:00 AM/10:00c" format
	 * @param hr
	 * @param isMobile
	 * @return
	 */
	String getOnTVTime(int hr,String isMobile){
		
		String currentTime=Constant.onTvTime[hr].substring(0,Constant.onTvTime[hr].length()-3);
		
		int hour;
		String minute;
		String episodeTime;
	
		if(isMobile.equalsIgnoreCase("false")){
			hour=Integer.parseInt(currentTime.substring(0,currentTime.indexOf(":")))-1;
			hour=(hour==0?12:hour);
			minute=currentTime.substring(currentTime.indexOf(":")+1,currentTime.length());
			episodeTime=Constant.onTvTime[hr]+Constant.SLASH+hour+":"+minute+"c";
		}else{
			
			if(Constant.onTvTime[hr].indexOf("AM")>-1)
				episodeTime=currentTime+Constant.OPEN_SUB+"A"+Constant.CLOSE_SUB;
			else
				episodeTime=currentTime+Constant.OPEN_SUB+"P"+Constant.CLOSE_SUB;
		}
			
			
		
		return episodeTime;
	}
	
	/*
	 * method to retrieve time in "11:30am/10:30" format
	 * @param hr
	 * @return
	 */
	String getTime(int hr){
		
		String currentTime=Constant.time[hr].substring(0,Constant.time[hr].length()-2);
		int hour,minute=0;
		String episodeTime;
		if(currentTime.indexOf(":")>-1){
			hour=Integer.parseInt(currentTime.substring(0,currentTime.indexOf(":")))-1;
			hour=(hour==0?12:hour);
			minute=Integer.parseInt(currentTime.substring(currentTime.indexOf(":")+1,currentTime.length()));
			episodeTime=Constant.time[hr]+Constant.SLASH+hour+":"+minute;
		}else{
			hour=Integer.parseInt(currentTime)-1;
			hour=(hour==0?12:hour);
				episodeTime=Constant.time[hr]+Constant.SLASH+hour;
		}
			
			
		
		return episodeTime;
	}
	
	/*
	 * method to retrieve time in "Month day, time" format
	 * @param cal
	 * @param hr
	 * @return
	 */
	String getDayTime(Calendar cal,int hr){
		
		String day=new SimpleDateFormat("MMMM").format(new Date(cal.getTimeInMillis()))+" "+cal.get(Calendar.DAY_OF_MONTH)+", "+Constant.time[hr];
		
		return day;
	}
	
	/*
	 * method to retrieve recipe assets content page based on recipe asset
	 * @param recipeAsset
	 * @param isMobile
	 * @param session
	 * @param builder
	 * @param query
	 * @param result
	 */
	private String[] getRecipeContents(String[] recipeAsset,String isMobile,Session session, QueryBuilder builder){
			Map<String, String> map = new HashMap<String, String>();
		     if(isMobile.equalsIgnoreCase(Constant.FALSE))
		    	 map.put(Constant.PATH, Constant.RECIPE_CONTENT);
		     else
		    	 map.put(Constant.PATH, Constant.RECIPE_MOBILE_CONTENT);
		     map.put(Constant.TYPE, Constant.PAGE_CONTENT);
		     map.put(Constant.PROPERTY,Constant.ASSET_LINK);
		     
		     for(int i=1;i<=recipeAsset.length;i++){
		    	 map.put(Constant.PROPERTY+Constant.DOT+i+Constant.VALUE,recipeAsset[i-1]);
		    	 
		     }

            Query query = builder.createQuery(PredicateGroup.create(map), session);
		   
		    query.setHitsPerPage(0);
            SearchResult result = query.getResult();
		    int count=0;
		    recipeAsset=new String[result.getHits().size()];
		    String path;
		    for (Hit hit : result.getHits()) {
		    		try{
		    			path=hit.getPath();
		    			recipeAsset[count]=path.substring(0,path.indexOf(Constant.JCR_CONTENT)-1);
		    			count++;
		    		}catch(Exception ex){
		    			continue;
		    		}
		      
		     }
		return recipeAsset;
	}
	
	
	
	/*
	 * method to retrieve show names associated to talent
	 * @param chef
	 * @param session
	 * @param builder
	 * return
	 */
	private List<String> getShowNames(String chef,Session session, QueryBuilder builder){
			
			Query query=null;
			SearchResult result=null;
		    Map<String, String> map = new HashMap<String, String>();
		    map.put(Constant.PATH, Constant.SHOW_ASSET_PATH);
		    map.put(Constant.TYPE, Constant.PAGE_CONTENT);
		    map.put(Constant.PROPERTY,Constant.SNI_PROP_PRIMARY_TALENT);
		    map.put(Constant.PROPERTY+Constant.DOT+1+Constant.VALUE,chef);
		    query = builder.createQuery(PredicateGroup.create(map), session);
		    query.setHitsPerPage(0);
		    result = query.getResult();
		    List <String>showNames= new ArrayList<String>();
		    
		    
		    for (Hit hit : result.getHits()) {
		    		try{
		    			
		    			showNames.add(hit.getResource().getParent().getName());
		    		}catch(Exception ex){
		    			continue;
		    		}		    		
		      
		     }
		return showNames;
	}
	
	/*
	 * method to retrieve page content for given asset path
	 * @param show
	 * @param path
	 * @param session
	 * @param builder
	 * return
	 */
	private Resource getContentPath(String episodePath,String path,Session session, QueryBuilder builder){
			
			Query query=null;
			SearchResult result=null;
		    Map<String, String> map = new HashMap<String, String>();
		    map.put(Constant.PATH, path);
		    map.put(Constant.TYPE, Constant.PAGE_CONTENT);
		    map.put(Constant.PROPERTY,Constant.ASSET_LINK);
		    map.put(Constant.PROPERTY+Constant.DOT+1+Constant.VALUE,episodePath);
		    query = builder.createQuery(PredicateGroup.create(map), session);
		    query.setHitsPerPage(1);
		    result = query.getResult();
		   
		   
		    
		    for (Hit hit : result.getHits()) {
		    		try{
		    			
		    			return hit.getResource();
		    			
		    		}catch(Exception ex){
		    			continue;
		    		}		    		
		      
		     }
		return null;
	}
}
