package com.scrippsnetworks.wcm.asset;

import com.scrippsnetworks.wcm.fnr.util.AssetRootPaths;
import com.scrippsnetworks.wcm.fnr.util.AssetSlingResourceTypes;
import com.scrippsnetworks.wcm.fnr.util.PageSlingResourceTypes;
import com.scrippsnetworks.wcm.taglib.Functions;
import com.scrippsnetworks.wcm.taglib.TagUtils;
import com.scrippsnetworks.wcm.util.*;
import com.scrippsnetworks.wcm.asset.recipe.Recipe;

import com.day.cq.wcm.api.Page;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;

import org.apache.commons.lang.WordUtils;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;



/**
 * @author Jason Clark
 * Date: 6/4/12
 * Updated: 6/9/2012 Ken Shih
 * Updated: 7/18/2012 Jason Clark (static Strings)
 *          7/19/2012 Jason Clark (search utils)
 *          8/8/2012 Pawan Gupta (search utils)
 *          8/8/2012 Danny Gordon (search utils)
 */
@Deprecated
public final class DataUtil {

    /* STATIC STRINGS THAT CAN BE USED ANYWHERE */

    //miscellaneous strings
    public static final String FORWARD_SLASH = "/";
    public static final String EMPTY_STRING = "";
    public static final String EXTENSION_HTML = ".html";
    public static final String COLON = ":";
    public static final String SPACE = " ";
    public static final String SORT_ORDER_ASC = "ASC";
    public static final String SORT_ORDER_DESC = "DESC";

    //section names
    public static final String HOME_SECTION_NAME = "home";

    //property names
    public static final String PROPERTY_NAME_JCR_PATH = "jcr:path";
    public static final String SCHEDULE_SORT_TERM = "sni:sortTitle";

    //same as TIME_CODE_MAP, but with keys/values swapped, cos sometimes it's useful to look up by the formatted time
    public static final Map<String, Integer> REVERSE_TIME_CODE_MAP = new HashMap<String, Integer> () {
        {
            put("6:30", 1);
            put("7:00", 2);
            put("7:30", 3);
            put("8:00", 4);
            put("8:30", 5);
            put("9:00", 6);
            put("9:30", 7);
            put("10:00", 8);
            put("10:30", 9);
            put("11:00", 10);
            put("11:30", 11);
            put("12:00", 12);
            put("12:30", 13);
            put("13:00", 14);
            put("13:30", 15);
            put("14:00", 16);
            put("14:30", 17);
            put("15:00", 18);
            put("15:30", 19);
            put("16:00", 20);
            put("16:30", 21);
            put("17:00", 22);
            put("17:30", 23);
            put("18:00", 24);
            put("18:30", 25);
            put("19:00", 26);
            put("19:30", 27);
            put("20:00", 28);
            put("20:30", 29);
            put("21:00", 30);
            put("21:30", 31);
            put("22:00", 32);
            put("22:30", 33);
            put("23:00", 34);
            put("23:30", 35);
            put("00:00", 36);
            put("00:30", 37);
            put("1:00", 38);
            put("1:30", 39);
            put("2:00", 40);
            put("2:30", 41);
            put("3:00", 42);
            put("3:30", 43);
            put("4:00", 44);
            put("4:30", 45);
            put("5:00", 46);
            put("5:30", 47);
            put("6:00", 48);
        }
    };

    /**
     * don't instantiate me!
     */
    private DataUtil() {}
    
    /**
     * WARNING: this method is subject to change in behavior due to code review: COOKING-CR-96#CFR-485
     * in particular, currently, this really isn't a merge. that is, it overrides defaults, 
     * but if properties on the resourceToMerge exist that don't exist in "defaultResource",
     * it is not returned in the map. 
     * 
     * so, one of the following should happen:
     * 1. turn this and {@link #internalMerge(Resource, Resource, String[], Map)} into a real merge
     * 2. rename this and {@link #getAssetData(Resource, Resource)} into something that reflects what it does
     * 
     * 
     * {@link #getAssetData(Resource, Resource)} plus parameter "overrides" below
     * @param defaultResource Sling Resource containing the properties to treat as defaults
     * @param resourceToMerge Sling Resource for page requesting the data node
     * @param allowedOverrides name of properties that should be overridden if they exist in resourceToMerge
     * @return Map of properties
     */
    public static Map<String, Object> mergeResourceProperties(final Resource defaultResource, 
    		final Resource resourceToMerge, final String[] allowedOverrides) {
    	Map<String, Object> mergeMap = new HashMap<String, Object>();
    	return internalMerge(defaultResource, resourceToMerge, allowedOverrides, mergeMap);
    }
    
    

    /**
     * utility 
     * @see {@link #mergeResourceProperties(Resource, Resource, String[])}
     * @see {@link #getAssetData(Resource, Resource)})
     * @param defaultResource
     * @param resourceToMerge
     * @param allowedOverrides
     * @param mergeMap WARNING MUTABLE pass this in, get it back with the merged properties as a Map
     * @return basically returns a mutated mergeMap outParameter which is just 
     */
    static  Map<String, Object> internalMerge(final Resource defaultResource, 
    		final Resource resourceToMerge, final String[] allowedOverrides,
    		Map<String, Object> mergeMap){
    	
    	if (defaultResource != null) {
            ValueMap defaultMap = ResourceUtil.getValueMap(defaultResource);
            ValueMap toMergeMap = ResourceUtil.getValueMap(resourceToMerge);
            if (defaultMap != null) {
                for (Map.Entry<String,Object> entry : defaultMap.entrySet()) {
                    mergeMap.put(entry.getKey(), entry.getValue());
                }
                if (toMergeMap != null) {
                    for (String override : allowedOverrides) {
                        if (toMergeMap.containsKey(override))
                            mergeMap.put(override, toMergeMap.get(override));
                    }
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
        return mergeMap;
    }
   
    /**
     * This function is used to access a data node in CRX, given a resource for the data node and page in hand.
     * It returns a Map which contains all of the properties for the data resource at the given path.
     * There is a list of properties that are allowed to be overridden. Properties in this list
     * that are present on the referring node will override the data node's value.
     * @param dataResource Sling Resource for data node
     * @param pageResource Sling Resource for page requesting the data node
     * @return Map of properties
     */
    public static Map<String, Object> getAssetData(Resource dataResource, Resource pageResource) {

        Map<String, Object> mergeMap = new HashMap<String, Object>(40, 0.99f); //tuned for recipes

        //this list of overrides is specific to recipes for now, need to add all override fields
        //one big list of overrides would work in all asset types except for video...
        //video copyrights are not allowed to be overwritten, per the Jitterbug field mappings
        //need to abstract this to static string constants class or enum
        String[] overrides = {"dc:title", "dc:description", "copyright", "jcr:title", "jcr:description", "sni:copyright"};
        // merge using common internal util method
        return internalMerge(dataResource, pageResource, overrides, mergeMap);
    }

    /**
     * This will construct a data resource from the resource of the page in hand and a path to
     * the desired data resource.  Calls getDataFromNode with these two resources to return a
     * Map of the properties in the data resource.
     * @param resource Sling Resource of page calling for data
     * @param dataResourcePath String representing path to data node
     * @return Map of properties of data node
     */
    public static Map<String, Object> getAssetData(Resource resource, String dataResourcePath) {
        if (resource != null && dataResourcePath != null && dataResourcePath.length() > 0) {
            Resource dataResource = resource.getResourceResolver().getResource(dataResourcePath);
            return getAssetData(dataResource, resource);
        } else {
            return null;
        }
    }

    /**
     * String utility to obtain the show abbreviation from an episode number
     * @param episodeNumber String of episode number, the first part before the dash is show abbr
     * @return String for show abbreviation
     */
    public static String showAbbrFromEpisodeNumber(final String episodeNumber) {
        if (episodeNumber == null) {
            return null;
        }
        return episodeNumber.replaceFirst("-.*", "");
    }

    /**
     * Munge the path to a show asset into a relative path to the show's page
     * @param showAssetPath String relative path to the show asset in JCR
     * @return String relative path to the show page
     */
    public static String showPageUrlFromShowAssetPath(final String showAssetPath) {
        if (showAssetPath == null) {
            return null;
        }
        return TagUtils.completeHREF(Functions.getBasePath(showAssetPath
                .replaceFirst(AssetRootPaths.SHOWS.path(), ContentRootPaths.SHOWS.path())));
    }
    
    
    /**
     * Merge the path to a show asset into a relative path to the show's page
     * @param showAssetPath String relative path to the show asset in JCR
     * @return String relative path to the show page
     */
    public static String showPageUrlFromShowAssetPath(final String showAssetPath,String appType) {
        if (showAssetPath == null) {
            return null;
        }
        
        if(appType==null)
        	return TagUtils.completeHREF(Functions.getBasePath(showAssetPath
                .replaceFirst(AssetRootPaths.SHOWS.path(), ContentRootPaths.SHOWS.path())));
        else
        	return TagUtils.completeHREF(Functions.getBasePath(showAssetPath
                    .replaceFirst(AssetRootPaths.SHOWS.path(), Constant.SHOW_MOBILE_CONTENT_PATH)));
    }

    /**
     * String munger to convert a military timestamp value produced from
     * dateMapFromSchedulePath into Standard time
     *
     * Keys in the Map are:
     *  time : this is the time in 12 hour format
     *  period : this is the AM/PM indicator
     *
     * @param milTime String, expected to be 24 hour time in format like 6:30 or 15:00
     * @return Map with time and period in standard format
     */
    public static Map<String, String> convertMilTimeToStandard(final String milTime) {
        if (milTime == null) {
            return null;
        }
        String[] timeParts = milTime.split(":");
        try {
            Map<String, String> output = new HashMap<String, String>();
            Integer hours = Integer.valueOf(timeParts[0]);
            String period = hours >= 12 ? "PM" : "AM";
            output.put("period", period);
            String formattedHours = hours > 12 ? Integer.toString(hours - 12) : hours.toString();
            String standardHours = Integer.valueOf(formattedHours) == 0 ? "12" : formattedHours;
            output.put("time", standardHours + COLON + timeParts[1]);
            return output;
        } catch (IndexOutOfBoundsException ie) {
            return null;
        }
    }

    /**
     * This method accepts a path to an episode node and traverses the path to find it's
     * corresponding Show jcr:content node. Retrieves the node's values and stuffs them into a Map,
     * along with a property named "jcr:path" which is the path to the Show node.
     * Checks the sling:resourceType of the show node to ensure you are returning the type that you wanted.
     * @param resource Sling Resource in hand (any resource)
     * @param episodePath String of path to an episode node
     * @return Map containing the properties of the show node, plus a new one named "jcr:path"
     */
    public static Map<String, Object> showFromEpisodePath(final Resource resource,
                                                          final String episodePath) {
        if (resource == null || episodePath == null) {
            return null;
        }
        String[] pathParts = Functions.getBasePath(episodePath).split("/");
        int indicesToInclude = (pathParts.length - 1) - 2; //count back to the desired string part
        StringBuilder pathToShow = new StringBuilder();
        //reassemble path
        for (int i = 1; i <= indicesToInclude; i++) {
            pathToShow.append(FORWARD_SLASH).append(pathParts[i]);
        }
        pathToShow.append(FORWARD_SLASH).append(NodeNames.JCR_CONTENT.nodeName());
        ValueMap foundNodeProperties = ResourceUtil
                .getValueMap(resource.getResourceResolver().getResource(pathToShow.toString()));
        if (valueMapIsType(foundNodeProperties, AssetSlingResourceTypes.SHOW.resourceType())) {
            Map<String, Object> output = new HashMap<String, Object>();
            output.put(PROPERTY_NAME_JCR_PATH, Functions.getBasePath(pathToShow.toString()));
            for (Map.Entry<String,Object> entry : foundNodeProperties.entrySet()) {
                output.put(entry.getKey(), entry.getValue());
            }
            return output;
        } else {
            return null;
        }
    }

    /**
     * Returns a list of Recipe objects constructed from the sni:recipes property on an episode asset
     * @param resource Sling Resource of the page in hand (can be any resource)
     * @param episodeAssetPath String path to the episode asset node you wish to retrieve sni:recipes from
     * @return List of Recipe objects
     */
    public static List<Recipe> recipesFromEpisodeAsset(final Resource resource,
                                                       final String episodeAssetPath) {
        if ( resource == null || episodeAssetPath == null) {
            return null;
        }
       
        String[] recipePaths = getRecipePathsFromEpisodeAssetPath(episodeAssetPath,resource);
        if (recipePaths == null) {
            return null;
        }
        List<Recipe> recipes = new ArrayList<Recipe>();
        for (String recipePath : recipePaths) {
            recipes.add(new Recipe(resource, recipePath));
        }
        return recipes;
    }
    
    /*
     * Method to retrieve recipe paths from episode, if recipes overrides at content page level else from asset.
     * @param episodeAssetPath - episode asset path
     * @param resource 
     * @return list of recipe asset path
     */
    
    public static String[] getRecipePathsFromEpisodeAssetPath(String episodeAssetPath, final Resource resource){
    	String recipePaths[]=null;

    	Validate.notNull(episodeAssetPath);
    	try{
	    	episodeAssetPath = Functions.getBasePath(episodeAssetPath)
	                + FORWARD_SLASH + NodeNames.JCR_CONTENT.nodeName();
	    	ResourceResolver resolver=resource.getResourceResolver();
	    	Resource episodeAsset=resolver.getResource(episodeAssetPath);
	    	String episodeContentPath=ResourceUtil.getValueMap(episodeAsset)
	                .get(AssetPropertyNames.SNI_PAGE_LINKS.propertyName(), String.class)
	                + FORWARD_SLASH + NodeNames.JCR_CONTENT.nodeName();
	    	Resource episodeContent=resolver.getResource(episodeContentPath);
	    	String[] recipeContentPaths=ResourceUtil.getValueMap(episodeContent)
	                .get(AssetPropertyNames.SNI_RECIPES.propertyName(), String[].class);
	    	
	    	if(recipeContentPaths!=null && recipeContentPaths.length>0){
	    		
	    		String recipeContentPath=null;
	    		Resource recipeContent=null;
	    		recipePaths=new String[recipeContentPaths.length];
	    		ArrayList<String> validRecipePaths = new ArrayList<String>();
	    		for(String recipePath:recipeContentPaths){
	    			recipeContentPath=recipePath+ FORWARD_SLASH + NodeNames.JCR_CONTENT.nodeName();
	    			recipeContent=resolver.getResource(recipeContentPath);
	    			
	    			//only add the recipe content assetLink if the recipe content is not null!
	    			if(recipeContent != null) {
	    				validRecipePaths.add(ResourceUtil.getValueMap(recipeContent)
	    	                .get(Constant.ASSET_LINK, String.class));
	    			}

	    		}
	    		recipePaths = validRecipePaths.toArray(new String[validRecipePaths.size()]);
	    	}else{
	    		
	    		recipePaths = ResourceUtil.getValueMap(episodeAsset)
	                    .get(AssetPropertyNames.SNI_RECIPES.propertyName(), String[].class);
	    	}
    	}catch(Exception ex){
    		//Not logging error.
    	}
    	return recipePaths;
    }

    /**
     * Construct a list of episode asset paths from the same season (series) as a given episode asset path
     * @param resource Sling resource in hand (can be any resource)
     * @param episodeAssetPath String path to episode asset that you want to use to find other episode asset paths
     * @return ArrayList of Strings of episode asset paths
     */
    public static List<String> allEpisodesInSameSeason(final Resource resource,
                                                       final String episodeAssetPath) {
        if (episodeAssetPath == null) {
            return null;
        }
        String pathToSeriesNode = Functions.getBasePath(episodeAssetPath)
                .replaceFirst("/[a-zA-Z0-9-]+$", EMPTY_STRING);
        Resource series = resource.getResourceResolver().getResource(pathToSeriesNode);
        Iterator<Resource> episodeItr = series.listChildren();
        List<String> episodes = new ArrayList<String>();
        Map<String,String> episodesDummyMap=new TreeMap<String,String>();       
        while(episodeItr.hasNext()) {        
            Resource episode = episodeItr.next();
            try {
                if (episode.getName().equals(NodeNames.JCR_CONTENT.nodeName())
                        || episode.getName().equals(NodeNames.CRXDAO_META.nodeName())) {
                    continue;
                }
                Resource episodeContentResource = episode.getChild(NodeNames.JCR_CONTENT.nodeName());
                if (ResourceUtil.getValueMap(episodeContentResource)
                        .get(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName(), String.class)
                        .equals(AssetSlingResourceTypes.EPISODE.resourceType())) {
                    //episodes.add(episode.getPath());
                	episodesDummyMap.put(ResourceUtil.getValueMap(episodeContentResource)
                			.get(AssetPropertyNames.SNI_EPISODE_NO.propertyName(), String.class), episode.getPath());
                }
            } catch (NullPointerException npe) {
                continue;
            }
        }
        if(episodesDummyMap.size()>0){        	
        	String []a=new String[episodesDummyMap.size()];
        	episodes=Arrays.asList(episodesDummyMap.values().toArray(a));        	
        }
        return episodes;
    }

    /**
     * Checks if given ValueMap contains a sling:resourceType property that matches the given String type
     * @param valueMap ValueMap of that you want to check the sling:resourceType of
     * @param slingResourceType String constant of the sling:resourceType the ValueMap should contain
     * @return boolean if sling:resourceType matches the given type
     */
    public static boolean valueMapIsType(final ValueMap valueMap, final String slingResourceType) {
        return (!(valueMap == null || slingResourceType == null)
                && valueMap.containsKey(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName())
                && valueMap.get(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName())
                .toString().equals(slingResourceType));
    }

    /**
     * String utility to construct a path to a person's page based on the path to the person asset
     * @param personAssetPath String path to person asset
     * @return String path to person page node + html extension
     */
    public static String personPagePathFromAssetPath(final String personAssetPath) {
        if (personAssetPath == null || personAssetPath.length() == 0) {
            return null;
        }
        return Functions.getBasePath(personAssetPath)
                .replaceFirst(AssetRootPaths.CHEFS.path(), ContentRootPaths.CHEFS.path()) + EXTENSION_HTML;
    }

    /**
     * get the Scripps timecode value from a calendar object's time
     * rounds the minutes in increments of 30, by the nearest 15 minutes
     * @param calendar Calendar to get timecode from
     * @return Integer of the equivalent Scripps timecode
     */
    public static Integer timeCodeFromCalendar(final Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        int rawHour = calendar.get(Calendar.HOUR_OF_DAY);
        int rawMinute = calendar.get(Calendar.MINUTE);
        int calculatedHour;  //used for rounding hours forward
        String hour, minute; //these will be formatted values
        if (rawMinute < 15) {
            calculatedHour = rawHour;
            minute = "00";
        } else if (rawMinute >= 15 && rawMinute < 45) {
            calculatedHour = rawHour;
            minute = "30";
        } else {
            calculatedHour = rawHour == 23 ? 0 : rawHour + 1;
            minute = "00";
        }
        hour = calculatedHour == 0 ? "00" : String.valueOf(calculatedHour);
        String formattedTime = hour + COLON + minute;
        return REVERSE_TIME_CODE_MAP.containsKey(formattedTime) ? REVERSE_TIME_CODE_MAP.get(formattedTime) : null;
    }

    /**
     * subroutine method for building strings, used by findSchedulesFromTodayForward
     * returns path up to the day.
     * See also: {@link #timeCodeFromCalendar(Calendar)}
     * @param calendar Calendar from which you want to build a path to a schedule
     * @return String path to schedule
     */
    private static String schedulePathFromCalendar(final Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder
            .append(AssetRootPaths.SCHEDULES.path())
            .append(FORWARD_SLASH)
            .append(calendar.get(Calendar.YEAR))
            .append(FORWARD_SLASH)
            .append(calendar.get(Calendar.MONTH) + 1)
            .append(FORWARD_SLASH)
            .append(calendar.get(Calendar.DAY_OF_MONTH));
        return pathBuilder.toString();
    }

    /**
     * Retrieve the resource for the schedule asset that corresponds to the current time
     * @param resource Resource in hand
     * @return Resource for schedule asset node
     */
    public static Resource currentSchedule(final Resource resource) {
        Calendar calendar = Calendar.getInstance();
        return scheduleAssetFromCalendar(resource, calendar);
    }

    /**
     * Retreive resource for the schedule asset that corresponds to 10:00 pm today
     * @param resource Resource in hand
     * @return Resource of schedule asset
     */
    public static Resource tenPMSchedule(final Resource resource) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 22);
        calendar.set(Calendar.MINUTE, 0);
        return scheduleAssetFromCalendar(resource, calendar);
    }

    /**
     * Retrieve a schedule asset Resource give a Calendar object
     * @param resource Resource in hand
     * @param calendar Calendar that you want to base the schedule path on
     * @return Resource for schedule asset
     */
    private static Resource scheduleAssetFromCalendar(final Resource resource,
                                                      final Calendar calendar) {
        if (resource == null || calendar == null) {
            return null;
        }
        String schedulePath = schedulePathFromCalendar(calendar)
                + FORWARD_SLASH
                + timeCodeFromCalendar(calendar)
                + FORWARD_SLASH
                + NodeNames.JCR_CONTENT.nodeName();
        return resource.getResourceResolver().getResource(schedulePath);
    }

    /**
     * Return a list of episode Nodes by searching for a recipe path related to the episode
     * @param resource Sling Resource for the page in hand (can be any resource)
     * @param recipePath String path of the recipe asset you are searching for
     * @return List of Maps of properties for the episodes that contain the given recipe path
     */
    public static List<Map<String, Object>> findEpisodesByRecipePath(final Resource resource,
                                                                     final String recipePath) {
        if (resource == null || recipePath == null) {
            return null;
        }
        
        Resource recipeContent=resource.getResourceResolver().getResource(recipePath+ FORWARD_SLASH
                + NodeNames.JCR_CONTENT.nodeName());
        String recipeContentPath=ResourceUtil.getValueMap(recipeContent).get(AssetPropertyNames.SNI_PAGE_LINKS.propertyName(),String.class);
        List<Map<String, Object>> assets=findEpisodeAssetsByContentPath(resource, ContentRootPaths.SHOWS.path(),
        		PageSlingResourceTypes.EPISODE.resourceType(), recipeContentPath);
        
        if(assets!=null && assets.size()>0){
        	assets.addAll(findAssetsByPropertyValue(resource, AssetRootPaths.SHOWS.path(),
                AssetSlingResourceTypes.EPISODE.resourceType(), recipePath, null, null));
        	return assets;
        }else{
        	
        	return findAssetsByPropertyValue(resource, AssetRootPaths.SHOWS.path(),
                AssetSlingResourceTypes.EPISODE.resourceType(), recipePath, null, null);
        }
    }
    
    /*
     * Private method to support findEpisodesByRecipePath
     * @param resource
     * @param pathToAssetRoot
     * @param resourceType
     * @param searchTerm
     * @return list of episode objects
     */
    private static List<Map<String, Object>> findEpisodeAssetsByContentPath(final Resource resource,
															            final String pathToAssetRoot,
															            final String resourceType,
															            final String searchTerm) {
		if (resource == null || pathToAssetRoot == null || resourceType == null) {
			return null;
		}
		String querySearchTerm;
		if (searchTerm == null) {
			querySearchTerm = EMPTY_STRING;
		} else {
			querySearchTerm = "and CONTAINS(s.*,'" + searchTerm + "')";
		}
		QueryManager queryManager;
		try {
			queryManager = resource.adaptTo(Node.class).getSession().getWorkspace().getQueryManager();
			StringBuilder query = new StringBuilder();
			query
			.append("SELECT * FROM [nt:base] AS s WHERE ISDESCENDANTNODE([")
			.append(pathToAssetRoot)
			.append("]) AND NAME(s) = '")
			.append(NodeNames.JCR_CONTENT.nodeName())
			.append("' ")
			.append(querySearchTerm)
			.append(" and CONTAINS(s.'")
			.append(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName())
			.append("', '")
			.append(resourceType)
			.append("')");
			Query compiledQuery = queryManager.createQuery(query.toString(), Query.JCR_SQL2);
			NodeIterator nodeItr = compiledQuery.execute().getNodes();
			List<Map<String, Object>> assets = new ArrayList<Map<String, Object>>();
			Node node=null;
			Resource nodeResource=null;
			ValueMap nodeValues=null;
			Map<String, Object> returnValues = new HashMap<String, Object>();
			String assetPath=null;
			ResourceResolver resolver=resource.getResourceResolver();
			while (nodeItr.hasNext()) {
				node = nodeItr.nextNode();
				nodeResource = Functions.getResource(resolver, node.getPath());
				nodeValues = ResourceUtil.getValueMap(nodeResource);
				assetPath=nodeValues.get(PagePropertyNames.SNI_ASSET_LINK.propertyName(),String.class);
				nodeResource =  Functions.getResource(resolver, assetPath+ FORWARD_SLASH
		                + NodeNames.JCR_CONTENT.nodeName());
				nodeValues = ResourceUtil.getValueMap(nodeResource);
				returnValues.put(PROPERTY_NAME_JCR_PATH, Functions.getBasePath(assetPath));
				for (Map.Entry<String,Object> entry : nodeValues.entrySet()) {
					returnValues.put(entry.getKey(), entry.getValue());
				}
				assets.add(returnValues);
				returnValues = new HashMap<String, Object>();
			}
			return assets;
		} catch (RepositoryException re) {
			return null;
		} catch (NullPointerException npe) {
			return null;
		}
}
   
    /**
     * Return a List of show Nodes by searching for the show abbreviation in sni:showAbbr
     * @param resource Sling Resource for the page in hand (can be any resource)
     * @param showAbbr String of the show abbreviation property used to locate the show record
     * @return List of Maps of properties for show assets
     */
    public static List<Map<String, Object>> findShowsByShowAbbr(final Resource resource,
                                                                final String showAbbr) {
        if (resource == null || showAbbr == null) {
            return null;
        }
        return findAssetsByPropertyValue(resource, AssetRootPaths.SHOWS.path(),
                AssetSlingResourceTypes.SHOW.resourceType(), showAbbr, null, null);
    }

    /**
     * Return a list of show asset nodes by searching for the person asset path in sni:people
     * @param resource Sling Resource for page in hand (can be any Resource)
     * @param personPath String of the sni:person path you wish to search for
     * @return List of Maps of properties for show assets
     */
    public static List<Map<String, Object>> findShowsByPersonPath(final Resource resource,
                                                                  final String personPath) {
        if (resource == null || personPath == null) {
            return null;
        }
        return findAssetsByPropertyValue(resource, AssetRootPaths.SHOWS.path(),
                AssetSlingResourceTypes.SHOW.resourceType(), personPath, null, null);
    }

    /**
     * @deprecated
     * Return a List of person Nodes by searching for the show abbreviation attached to the person record.
     * @param resource Sling Resource for the page in hand (can be any resource)
     * @param showAbbr String of the show abbreviation property used to locate the person record
     * @return List of Maps of properties for person assets
     */
    public static List<Map<String, Object>> findPeopleByShowAbbr(final Resource resource,
                                                                 final String showAbbr) {
        if (resource == null || showAbbr == null) {
            return null;
        }
        return findAssetsByPropertyValue(resource, AssetRootPaths.PEOPLE.path(),
                AssetSlingResourceTypes.PERSON.resourceType(), showAbbr, null, null);
    }

    /**
     * Return a List of schedule Nodes by searching for their related episode paths
     * @param resource Sling Resource in hand
     * @param episodePath String path to the episode whose schedule you wish to find
     * @return List of Maps containing properties for schedule assets that match episode path
     */
    public static List<Map<String, Object>> findSchedulesByEpisodePath(final Resource resource,
                                                                       final String episodePath) {
        if (resource == null || episodePath == null) {
            return null;
        }
        List<Map<String, Object>> schedules = findAssetsByPropertyValue(resource,
                AssetRootPaths.SCHEDULES.path(),
                AssetSlingResourceTypes.SCHEDULE.resourceType(),
                episodePath,
                AssetPropertyNames.SNI_SORT_TITLE.propertyName(),
                SORT_ORDER_DESC);
        Collections.sort(schedules, new ScheduleSortTitleComparator());
        Collections.reverse(schedules);
        return schedules;
    }

    /**
     *
     * @param resource
     * @param recipePath
     * @return
     */
    public static List<Map<String, Object>> findRecipeContentPagesByRecipeAssetPath(final Resource resource,
                                                                                    final String recipePath) {
        if (resource == null || recipePath == null) {
            return null;
        }
        if(resource.getPath().contains("cook-mobile")) {
        	return findAssetsByPropertyValue(resource, Constant.RECIPE_MOBILE_CONTENT,
        			PageSlingResourceTypes.RECIPE_MOBILE.resourceType(), recipePath, null, null);
        } else {
        	return findAssetsByPropertyValue(resource, ContentRootPaths.RECIPES.path(),
                PageSlingResourceTypes.RECIPE.resourceType(), recipePath, null, null);
        }
    }

    /**
     * Return a List of video nodes by searching for their related video page content path
     * @param resource Sling Resource in hand
     * @param videoPagePath String path to video page to search on
     * @return List of Maps of properties representing different videos associated with the video content page path
     */
    public static List<Map<String, Object>> findVideosByVideoContentPath(final Resource resource,
                                                                         final String videoPagePath) {
        if (resource == null || videoPagePath == null) {
            return null;
        }
        return findAssetsByPropertyValue(resource, AssetRootPaths.VIDEOS.path(),
                AssetSlingResourceTypes.VIDEO.resourceType(), videoPagePath, null, null);
    }

    
    
    /**
     * Generic search utility that uses the CQ search API to locate nodes in the content repo using
     * the sling:resourceType of the desired asset and a string representing the value of an anonymous
     * property to identify some content within the properties of the node(s). Returns a List of Nodes.
     *
     *
     * @param resource Resource in hand
     * @param pathToAssetRoot String path to the root path of desired asset type (should come from DataUtils)
     * @param resourceType String of sling:resourceType property to filter search by
     * @param searchTerm String of the search term you are using to retrieve the asset nodes
     * @param sortKey
     * @param sortOrder
     * @return List of Maps containing properties of found nodes
     */
    public static List<Map<String, Object>> findAssetsByPropertyValue(final Resource resource,
                                                                      final String pathToAssetRoot,
                                                                      final String resourceType,
                                                                      final String searchTerm,
                                                                      final String sortKey,
                                                                      final String sortOrder) {
        if (resource == null || pathToAssetRoot == null
                || resourceType == null) {
            return null;
        }
        boolean isSorted = StringUtils.isNotEmpty(sortKey);
        boolean hasSortOrder = StringUtils.isNotEmpty(sortOrder);
        String querySearchTerm;
        if (searchTerm == null) {
            querySearchTerm = EMPTY_STRING;
        } else {
            querySearchTerm = "and CONTAINS(s.*,'" + searchTerm + "')";
        }
        QueryManager queryManager;
        try {
            queryManager = resource.adaptTo(Node.class).getSession().getWorkspace().getQueryManager();
            StringBuilder query = new StringBuilder();
            query
                .append("SELECT * FROM [nt:base] AS s WHERE ISDESCENDANTNODE([")
                .append(pathToAssetRoot)
                .append("]) AND NAME(s) = '")
                .append(NodeNames.JCR_CONTENT.nodeName())
                .append("' ")
                .append(querySearchTerm)
                .append(" and CONTAINS(s.'")
                .append(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName())
                .append("', '")
                .append(resourceType)
                .append("')");
            if (isSorted) {
                query.append(" order by [" + sortKey + "] ");
                if (hasSortOrder && (sortOrder.equalsIgnoreCase(SORT_ORDER_ASC)
                        || sortOrder.equalsIgnoreCase(SORT_ORDER_DESC))) {
                    query.append(sortOrder);
                } else {
                    query.append(SORT_ORDER_ASC);
                }
            }
            Query compiledQuery = queryManager.createQuery(query.toString(), Query.JCR_SQL2);
            NodeIterator nodeItr = compiledQuery.execute().getNodes();
            List<Map<String, Object>> assets = new ArrayList<Map<String, Object>>();
            while (nodeItr.hasNext()) {
                Node node = nodeItr.nextNode();
                Resource nodeResource = Functions.getResource(resource.getResourceResolver(), node.getPath());
                ValueMap nodeValues = ResourceUtil.getValueMap(nodeResource);
                Map<String, Object> returnValues = new HashMap<String, Object>();
                returnValues.put(PROPERTY_NAME_JCR_PATH, Functions.getBasePath(node.getPath()));
                for (Map.Entry<String,Object> entry : nodeValues.entrySet()) {
                    returnValues.put(entry.getKey(), entry.getValue());
                }
                assets.add(returnValues);
            }
            return assets;
        } catch (RepositoryException re) {
            return null;
        } catch (NullPointerException npe) {
            return null;
        }
    }
    
    
    /**
     * Method to retrieve node name list which matches the query term.
     * @param resource Resource in hand
     * @param pathToAssetRoot String path to the root path of desired asset type (should come from DataUtils)
     * @param resourceType String of sling:resourceType property to filter search by
     * @param searchTerm String of the search term you are using to retrieve the asset nodes
     * @return List of Maps containing properties of found nodes
     */
    public static List<String> findAssetsNameByPropertyValue(final Resource resource,
                                                                      final String pathToAssetRoot,
                                                                      final String resourceType,
                                                                      final String searchTerm) {
        if (resource == null || pathToAssetRoot == null
                || resourceType == null || searchTerm == null) {
            return null;
        }
        
        
        QueryManager queryManager;
        try {
            queryManager = resource.adaptTo(Node.class).getSession().getWorkspace().getQueryManager();
            StringBuilder query = new StringBuilder();
            query
                .append("SELECT * FROM [nt:base] AS s WHERE ISDESCENDANTNODE([")
                .append(pathToAssetRoot)
                .append("]) AND NAME(s) = '")
                .append(NodeNames.JCR_CONTENT.nodeName())
                .append("' and CONTAINS(s.*,'")
                .append(searchTerm)
                .append("') and CONTAINS(s.'")
                .append(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName())
                .append("', '")
                .append(resourceType)
                .append("')");
            Query compiledQuery = queryManager.createQuery(query.toString(), Query.JCR_SQL2);
            NodeIterator nodeItr = compiledQuery.execute().getNodes();
            List<String> assetsName = new ArrayList<String>();
            Node node=null;
            while (nodeItr.hasNext()) {
                node = nodeItr.nextNode();
                assetsName.add(node.getParent().getName());
            }
            return assetsName;
        } catch (RepositoryException re) {
            return null;
        } catch (NullPointerException npe) {
            return null;
        }
    }

    /**
     * Use JCR search to locate channel content pages that contain the path to a certain video asset
     * @param resource Sling Resource in hand (can be any resource)
     * @param videoAssetPath String of the path to the video asset you're searching for
     * @return List of paths to content pages (minus extension, selectors etc)
     */
    public static List<String> findChannelsByVideoAssetPath(final Resource resource,
                                                            final String videoAssetPath) {
        if (resource == null || videoAssetPath == null) {
            return null;
        }
        QueryManager queryManager;
        try {
            queryManager = resource.adaptTo(Node.class).getSession().getWorkspace().getQueryManager();
            StringBuilder query = new StringBuilder();
            query
                .append("SELECT * FROM [nt:base] AS s WHERE ISDESCENDANTNODE([")
                .append(ContentRootPaths.CONTENT_COOK.path())
                .append("]) and CONTAINS(s.[")
                .append(PagePropertyNames.SNI_VIDEOS.propertyName())
                .append("], '")
                .append(videoAssetPath)
                .append("')");
            Query compiledQuery = queryManager.createQuery(query.toString(), Query.JCR_SQL2);
            NodeIterator nodeItr = compiledQuery.execute().getNodes();
            List<String> pagePaths = new ArrayList<String>();
            while (nodeItr.hasNext()) {
                Node node = nodeItr.nextNode();
                pagePaths.add(Functions.getBasePath(node.getPath()));
            }
            return pagePaths;
        } catch (RepositoryException re) {
            return null;
        } catch (NullPointerException npe) {
            return null;
        }
    }

    /**
     * Return the term from a tag
     * @param tag String tag, usually like cook-tag:ingredient/chicken
     * @return String term found in tag, the last part of the tag (like chicken in the param example)
     */
    public static String termFromTag(final String tag) {
        if (tag == null) {
            return null;
        }
        Pattern pattern = Pattern.compile(".+:(.+/)*(.+)$");
        Matcher matcher = pattern.matcher(tag);
        if (matcher.matches()) {
            return matcher.group(2);
        } else {
            return "";
        }
    }

    /**
     * Accepts a raw tag and returns the term part of the tag in pretty print format, like:
     * "cook-tag:main-ingredient/butternut-squash" would return "Butternut Squash"
     * @param tag String raw content tag
     * @return String formatted tag term
     */
    public static String prettyPrintTagTerm(final String tag) {
        if (tag == null) {
            return null;
        }
        return WordUtils.capitalize(termFromTag(tag).replaceAll("[_-]", SPACE));
    }

    /**
     * Construct URL to a topic page given a tag. Checks if that page exists, and if so returns a relative URL to
     * the topic page. If not found, returns the URL to the topics-a-z page.
     * @param resource Sling Resource in hand, necessary for looking up the existence of the topic page
     * @param tag String tag from our controlled vocabulary
     * @return String URL
     */
    public static String topicUrlFromTag(final Resource resource, final String tag) {
        if (resource == null || tag == null) {
            return null;
        }
        String term = termFromTag(tag);
        if (term.length() == 0) {
            return ContentRootPaths.TOPICS_A_Z.path() + EXTENSION_HTML;
        }
        char firstLetter = term.charAt(0);
        String path = ContentRootPaths.TOPICS.path() + FORWARD_SLASH + firstLetter + FORWARD_SLASH + term;
        if (resource.getResourceResolver().getResource(path) != null) {
            return path + EXTENSION_HTML;
        } else {
            return ContentRootPaths.TOPICS_A_Z.path() + EXTENSION_HTML;
        }
    }

    /**
     * Return datestamp from a GregorianCalendar object
     * Results look like YYYYMMDD
     * @param calendar GregorianCalendar from a ValueMap
     * @return String datestamp
     */
    public static String dateStampFromCalendar(final Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        StringBuilder output = new StringBuilder();
        output
            .append(calendar.get(Calendar.YEAR))
            .append(String.format("%02d", calendar.get(calendar.get(Calendar.MONTH)) + 1))
            .append(String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)));
        return output.toString();
    }

    /**
     * Return a timestamp from a Calendar object
     *  HH:MM:SS
     * @param calendar Calendar to get timestamp from
     * @return String formatted timestamp
     */
    public static String timeStampFromCalendar(final Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        StringBuilder output = new StringBuilder();
        output
            .append(String.format("%02d",calendar.get(Calendar.HOUR_OF_DAY))).append(COLON)
            .append(String.format("%02d",calendar.get(Calendar.MINUTE))).append(COLON)
            .append(String.format("%02d",calendar.get(Calendar.SECOND)));
        return output.toString();
    }

    /**
     * String util that takes a content path and retrieves the section name from it,
     * assuming that sections live directly under /content/cook
     * @param contentPath String content path in hand
     * @return String name of section from content path
     */
    public static String sectionNameFromContentPath(final String contentPath) {
        if (contentPath == null) {
            return null;
        }
        Pattern homePattern = Pattern.compile( "("
                + ContentRootPaths.CONTENT_COOK.path()
                + "|"
                + ContentRootPaths.CONTENT_COOK_MOBILE.path()
                + ")/([a-zA-Z0-9-]+)");
        Matcher homeMatcher = homePattern.matcher(contentPath);
        if (homeMatcher.matches()) {
            return HOME_SECTION_NAME;
        } else {
            Pattern pattern = Pattern.compile( "("
                    + ContentRootPaths.CONTENT_COOK.path()
                    + "|"
                    + ContentRootPaths.CONTENT_COOK_MOBILE.path()
                    + ")/([a-zA-Z0-9-]+)(/.*)?");
            Matcher matcher = pattern.matcher(contentPath);
            return matcher.matches() ? matcher.group(2) : null;
        }
    }

    /**
     * Accepts a Resource for the content page you're on, gets a section name from it
     * @param resource Resource of the content page in hand
     * @return String section name (unformatted, no assumptions made about the section name's usage)
     */
    public static String sectionNameFromResource(final Resource resource) {
        if (resource == null) {
            return null;
        }
        String resourceType = resource.getResourceType();
        Pattern pattern = Pattern.compile( "("
                + PageSlingResourceTypes.PAGE_TYPE_ROOT.resourceType()
                + "|"
                + PageSlingResourceTypes.PAGE_TYPE_MOBILE_ROOT.resourceType()
                + ")/(cook-)?([a-zA-Z0-9-]+)-section(/.*)?");
        Matcher matcher = pattern.matcher(resourceType);
        return matcher.matches() ? matcher.group(3) : sectionNameFromContentPath(Functions.getBasePath(resource.getPath()));
    }

    /**
     *
     * @param startDate
     * @param endDate
     * @param resource
     * @param maxRows
     * @return
     */
    public static  List<Map<String, String>> findImageExpiration(final Date startDate, final Date endDate, final Resource resource)
    {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");        
      	     	     	 
 		StringBuffer xqueryBuffer = new StringBuffer();
 		xqueryBuffer.append("/jcr:root/content/dam/images/food/*//element(*,nt:unstructured)[@sni:fastfwdExpireDate >= xs:dateTime('")
             .append(sdf.format(startDate)+"-05:00')").append("and @sni:fastfwdExpireDate <= xs:dateTime('")
 			 .append(sdf.format(endDate)+"-05:00')]")
             .append(" order by @sni:fastfwdExpireDate ASC");    	 

    	  //  log.info("Executing query string {}", queryBuffer);
    	
    	    String qryTxt=xqueryBuffer.toString();
    	    
    	    try {

    	    	QueryManager queryManager;
                queryManager = resource
                        .adaptTo(Node.class)
                        .getSession()
                        .getWorkspace()
                        .getQueryManager();
                
                Query compiledQuery = queryManager
                        .createQuery(qryTxt, Query.XPATH);
                compiledQuery.setLimit(1000);

                NodeIterator nodes = compiledQuery.execute().getNodes();   
                
                List<Map<String, String>> imageList = new ArrayList<Map<String, String>>();
                
                while (nodes.hasNext()){
                	Node node = nodes.nextNode();     
                	
	                Node pNode = node.getNode("../../");
	                 
	                HashMap<String, String> hm = new HashMap<String, String>(); 
	                //dc:title field type if not consistent, for few images it's String and for others it's String[]
	                String dcTitle = "";
	                if(node.hasProperty("dc:title"))
	                {
	                	Property imageTitle = node.getProperty("dc:title");
	                	if(imageTitle.isMultiple())
	                		dcTitle = imageTitle.getValues()[0].getString();
	                	else
	                		dcTitle = imageTitle.getString();
	                }
	                hm.put("dc:title", dcTitle);

	                String creater = pNode.hasProperty("jcr:createdBy") ? pNode.getProperty("jcr:createdBy").getString() : null;
	                String createdDate = pNode.hasProperty("jcr:created") ? pNode.getProperty("jcr:created").getString() : null;
	                if(creater==null){
	                	creater = node.hasProperty("jcr:lastModified") ? node.getProperty("jcr:lastModified").getString() : "";
	                }
	                if(createdDate==null){
	                	createdDate = node.hasProperty("jcr:lastModifiedBy") ? node.getProperty("jcr:lastModifiedBy").getString() : "";
	                }
	                hm.put("creater", creater);
	                hm.put("createdDate", createdDate);
	                hm.put("filename", pNode.getName());
	                hm.put("dampath", pNode.getPath());
	                hm.put("sni:fastfwdExpireDate", node.hasProperty("sni:fastfwdExpireDate") ? 
	                node.getProperty("sni:fastfwdExpireDate").getString() : "");
	                 
	                String usageTerms="";

	                if (node.hasProperty("xmpRights:UsageTerms"))
	                {  	     	                	 
	                	Value[] values = node.getProperty("xmpRights:UsageTerms").getValues();    	                
	                	if (values.length>0)
	                	{
	                		usageTerms = values[0].getString();
	                	}
	                }
	                hm.put("xmpRights:UsageTerms", usageTerms );
	                     	                     	                 
	                imageList.add(hm);
                }
                
    	        return imageList;
    	    } catch (Exception e2) {
    	       // log.error(e2.getMessage(), e2);
    	    	return null;
    	    }
    }
    
    
    /**
    *
    * @param startDate
    * @param endDate
    * @param resource
    * @param maxRows
    * @return
    */
   public static  List<Map<String, String>> findVideoExpiration(final Date startDate, final Date endDate, final Resource resource)
   {
	   
   	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");        
     	     	     	 
		StringBuffer xqueryBuffer = new StringBuffer();
		xqueryBuffer.append("/jcr:root/etc/sni-asset/food/*//element(*,nt:unstructured)[@sni:fastfwdExpireDate >= xs:dateTime('")
            .append(sdf.format(startDate)+"-05:00')").append("and @sni:fastfwdExpireDate <= xs:dateTime('")
			.append(sdf.format(endDate)+"-05:00')")
			.append(" and @sni:assetType='VIDEO' ] ")
            .append(" order by @sni:fastfwdExpireDate ASC");    	 
   	
   	    String qryTxt=xqueryBuffer.toString();
   	    
   	    try {
   	        
   	        List<Map<String, String>> videoList = new ArrayList<Map<String, String>>();
   	        
            QueryManager queryManager;
            queryManager = resource
                    .adaptTo(Node.class)
                    .getSession()
                    .getWorkspace()
                    .getQueryManager();
            
            Query compiledQuery = queryManager
                    .createQuery(qryTxt, Query.XPATH);
            compiledQuery.setLimit(1000);

            NodeIterator nodes = compiledQuery.execute().getNodes();
            
            
            while (nodes.hasNext()){
            	
            	Node node = nodes.nextNode();
            	
	            Node pNode = node.getNode("../");
   	                 
	            HashMap<String, String> hm = new HashMap<String, String>(); 
	            //dc:title field type if not consistent, for few images it's String and for others it's String[]
	            String videoTitle = "";
	            if(node.hasProperty("sni:title"))
	            {
	            	Property assetTitle = node.getProperty("sni:title");
	                if(assetTitle.isMultiple())
	                	videoTitle = assetTitle.getValues()[0].getString();
	                else
	                	videoTitle = assetTitle.getString();
	            }
	            hm.put("sni:title", videoTitle);

	            String creater = pNode.hasProperty("jcr:createdBy") ? pNode.getProperty("jcr:createdBy").getString() : null;
	            String createdDate = pNode.hasProperty("jcr:created") ? pNode.getProperty("jcr:created").getString() : null;   	                 

	            hm.put("creater", creater);
	            hm.put("createdDate", createdDate);
	            hm.put("assetname", pNode.getName());
	            hm.put("assetpath", pNode.getPath());
	            hm.put("sni:fastfwdExpireDate", node.hasProperty("sni:fastfwdExpireDate") ? 
	            node.getProperty("sni:fastfwdExpireDate").getString() : "");
	                    	                 
	            String pageLinks="";

	            if (node.hasProperty("sni:pageLinks"))
	            {  	     	                	 
	            	Value[] values = node.getProperty("sni:pageLinks").getValues();    	                
	            	if (values.length>0)
	            	{
	            		pageLinks = values[0].getString();
	                }
	            }
	            hm.put("sni:pageLinks", pageLinks );
	                     	                     	                 
	            videoList.add(hm);
           
            }
            
   	        return videoList;
   	    } catch (Exception e2) {
   	       // log.error(e2.getMessage(), e2);
   	    	return null;
   	    }
   }
    

    /**
     * The page type is the last path component of the sling resource type.
     */
    public static String getPageType(Page page) {
        if (page == null) {
            return EMPTY_STRING; // why not null?
        }

        ValueMap properties = page.getProperties();
        if (properties.containsKey(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName())) {
            return getPageType(properties.get(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName()).toString());
        } else {
            return EMPTY_STRING;
        }
    }

    /**
     * Accepts a Sling Resource Type and returns the last part of the resourceType path
     * @param resourceType String sling:resourceType
     * @return Last part of resource type
     */
    public static String getPageType(final String resourceType) {
        if (StringUtils.isNotBlank(resourceType)) {
            String[] chunks = resourceType.split(FORWARD_SLASH);
            return chunks.length > 0 ? chunks[chunks.length - 1] : resourceType;
        } else {
            return EMPTY_STRING;
        }
    }
    
    /**
     * Accepts a Sling Resource and returns the last part of the resourceType path
     * @param resource Resource
     * @return Last part of resource type
     */
    public static String getPageType(final Resource resource) {
        String pageType = EMPTY_STRING;
        if (resource != null) {
            ValueMap properties = ResourceUtil.getValueMap(resource);
            if (properties != ValueMap.EMPTY) {
                pageType = getPageType(properties.get(Constant.JCR_CONTENT + FORWARD_SLASH + PagePropertyNames.SLING_RESOURCE_TYPE.propertyName()).toString());
            }
        } 
        return pageType;
    }

    /**
     * Returns the content node path given by resource type.
     * 
     * @param currentPage
     * @param resourceType - We need to pass the entire path of the resource type (Ex: 'sni-wcm/components/pagetypes/program-guide-daily')
     * @param root - You can pass either blank (Ex: '') or desired path to filter (Ex: '/content/cook/shows/')
     * @return
     */
    public static String getContentNodeGivenResourceType(Page currentPage, String resourceType, String root)
    {
    	if(null == currentPage || StringUtils.isBlank(resourceType))
    	{
    		return null;
    	}
    	
    	if(!StringUtils.isBlank(root))
    	{
    		root = "/content/cook/";
    	}
    	
    	Node queryRoot = currentPage.adaptTo(Node.class);
    	Node contentNode = null;
    	
    	try 
		{
			QueryManager queryMgr = queryRoot.getSession().getWorkspace().getQueryManager();
			
			if(null == queryMgr)
			{
				return null;
			}
			 
			/*
			 * Building Query statement using the 'root' and 'resourceType'
			 */
			String queryStatement = "SELECT * FROM [nt:base] AS s WHERE ISDESCENDANTNODE(["+root+"]) AND s.[sling:resourceType]='"+ resourceType +"'";
			
			Query query = queryMgr.createQuery(queryStatement, Query.JCR_SQL2);
			
			QueryResult queryResForEpisode = query.execute();
			
			if(null != queryResForEpisode)
			{
				NodeIterator nodeItr = queryResForEpisode.getNodes();
				
				while(nodeItr.hasNext())
				{
					contentNode = nodeItr.nextNode();
					
					if(null != contentNode && null != contentNode.getName() && "jcr:content".equals(contentNode.getName()))
					{
						Node parentNode = contentNode.getParent();
						
						if(null != parentNode && StringUtils.isNotBlank(parentNode.getPath()))
						{
							return parentNode.getPath();
						}
					}
				}
			}
		} 
		catch (RepositoryException e) 
		{
			// log.error(e.getMessage(), e);
		}
    	
    	return null;
    }
    
    
}