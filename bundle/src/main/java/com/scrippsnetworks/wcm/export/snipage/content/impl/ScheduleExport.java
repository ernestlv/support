package  com.scrippsnetworks.wcm.export.snipage.content.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import javax.servlet.ServletException;

import com.day.cq.wcm.api.Page;
import com.day.cq.commons.jcr.JcrConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap; 
import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Value;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import com.scrippsnetworks.wcm.taglib.TagUtils;
import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import com.scrippsnetworks.wcm.taglib.Functions;
import com.scrippsnetworks.wcm.fnr.util.AssetSlingResourceTypes;
import com.scrippsnetworks.wcm.fnr.util.AssetRootPaths;
import com.scrippsnetworks.wcm.util.AssetPropertyNames;
import com.scrippsnetworks.wcm.util.ContentRootPaths;
import com.scrippsnetworks.wcm.util.PagePropertyNames;

/**
 * Servlet to output XML of Schedule Exports
 */

@SlingServlet(selectors = "export", methods = "GET", extensions = "xml", resourceTypes = {"sni-food/components/pagetypes/program-guide-daily","sni-food/components/pagetypes/program-guide-weekly"})
public class ScheduleExport extends SlingSafeMethodsServlet {

	@Reference
	private ResourceResolverFactory resourceResolverFactory;

	private static final long serialVersionUID = -3960692666512058119L;
	private static final String EXPORT = "export";
	private static final String CONTENT_ROOT = "/content";
	private static final String HTML = ".html";
	private static final String PIPE = "|";
    public static final String EMPTY_STRING = "";
    public static final String FORWARD_SLASH = "/";
    public static final String SORT_ORDER_ASC = "ASC";
    public static final String SORT_ORDER_DESC = "DESC";
    public static final String PROPERTY_NAME_JCR_PATH = "jcr:path";
    public static final String JCR_CONTENT = "jcr:content";
    
    private SlingHttpServletRequest request;
    private ResourceResolver resourceResolver;
    private StringBuilder exportXML;
	private Logger log = LoggerFactory.getLogger(ScheduleExport.class);
	
    //time code map
    private final Map<Integer, String> TIME_CODE_MAP = new HashMap<Integer, String>() {
        {
            put(1, "6:30");
            put(2, "7:00");
            put(3, "7:30");
            put(4, "8:00");
            put(5, "8:30");
            put(6, "9:00");
            put(7, "9:30");
            put(8, "10:00");
            put(9, "10:30");
            put(10, "11:00");
            put(11, "11:30");
            put(12, "12:00");
            put(13, "12:30");
            put(14, "13:00");
            put(15, "13:30");
            put(16, "14:00");
            put(17, "14:30");
            put(18, "15:00");
            put(19, "15:30");
            put(20, "16:00");
            put(21, "16:30");
            put(22, "17:00");
            put(23, "17:30");
            put(24, "18:00");
            put(25, "18:30");
            put(26, "19:00");
            put(27, "19:30");
            put(28, "20:00");
            put(29, "20:30");
            put(30, "21:00");
            put(31, "21:30");
            put(32, "22:00");
            put(33, "22:30");
            put(34, "23:00");
            put(35, "23:30");
            put(36, "00:00");
            put(37, "00:30");
            put(38, "1:00");
            put(39, "1:30");
            put(40, "2:00");
            put(41, "2:30");
            put(42, "3:00");
            put(43, "3:30");
            put(44, "4:00");
            put(45, "4:30");
            put(46, "5:00");
            put(47, "5:30");
            put(48, "6:00");
        }
    };
	
	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
		
		response.setHeader("Content-Type", "text/xml");
		try {
            exportXML = new StringBuilder();
            this.request = request;            
            resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
			String requestPath = request.getPathInfo();
			String[] selectors = request.getRequestPathInfo().getSelectors();
			int numSelectors = selectors==null?0:selectors.length;

            if(numSelectors<1) {
                response.sendError(500, "selectors not valid");
            }            
            if(selectors[0].equals(EXPORT))
            {
            	writeHeader();
            	List<Map<String, Object>> schedules = findSchedules(request.getResource());
            	log.debug("Schedule Size " + schedules.size());
            	
            	for(Map schedule : schedules){       
            		Map<String, String> dateMap = getDateMapFromSchedulePath((String)schedule.get(PagePropertyConstants.PROP_JCR_PATH));
            		String episodeAssetPath = (String)schedule.get(PagePropertyConstants.PROP_SNI_EPISODE);
            		log.debug("Epsidoe Path : " + episodeAssetPath);
            		if(episodeAssetPath != null){
	            		String episodeContentPath = null;
            			Resource episodeAssetResource = resourceResolver.getResource(episodeAssetPath + "/" + JcrConstants.JCR_CONTENT);
            			if(episodeAssetResource != null){
	            			Node assetNode = episodeAssetResource.adaptTo(Node.class);
	            			if(assetNode.hasProperty(AssetPropertyNames.SNI_PAGE_LINKS.propertyName())){
	            				Value[] values = assetNode.getProperty(AssetPropertyNames.SNI_PAGE_LINKS.propertyName()).getValues();
	            				if(values != null){
	            					episodeContentPath = values[0].getString();
	            				}
	            			}else {
	            				episodeContentPath = Functions.getBasePath(episodeAssetPath.replace(AssetRootPaths.ASSET_ROOT.path(), CONTENT_ROOT));
							}	        				
	            			log.debug("Episode Content Path:  " + episodeContentPath);
		            		Resource episodeContentResource = resourceResolver.getResource(episodeContentPath);	            		
		            		if (episodeContentResource != null) {
		            			Page episodePage = episodeContentResource.adaptTo(Page.class);
		                        if(episodePage==null) continue;
			            		ValueMap episodePageProperties = episodePage.getProperties();
			            		String showPath = (String)schedule.get(PagePropertyConstants.PROP_SNI_SHOW);
			            		exportXML.append("<PVAL><![CDATA[")
			            		.append(dateMap.get("month")+dateMap.get("day")+dateMap.get("year"))
			            		.append(PIPE)
			            		.append(dateMap.get("timeCode"))
			            		.append(PIPE)
			            		.append(getShowContentPathFromAssetPath(showPath, episodePage))
			            		.append(PIPE)
			            		.append(episodePageProperties.get(PagePropertyConstants.PROP_SNI_ASSETUID,""))
			            		.append(PIPE)
			            		.append(getEpisodeTuneInTime(TIME_CODE_MAP.get(Integer.parseInt(dateMap.get("timeCode")))))
			            		.append("]]></PVAL>");	
		            		}
            			}
            		}
            	}
            	writeFooter();            
            }            
           response.getOutputStream().print(exportXML.toString());			
		} catch (Exception e) {
			log.error("Exception in ScheduleExport: " + e.getMessage());
		}
	}

    /***
     * writes the header of the xml response
     * @return
     */
    protected void writeHeader() 
    {
    	exportXML.append("<RECORDS>").append("<RECORD>").append("<PROP NAME=\"SCHEDULE\">");
    }

    /***
     * writes the footer of the xml response
     * @return
     */
    protected void writeFooter() 
    {
    	exportXML.append("</PROP>").append("</RECORD>").append("</RECORDS>");
    }
    
    /**
     * This timestamp format used in the export for the given time slot
     * @return String of timestamp formatted for Episode Tune-In times of timeslot  (9PM/8C)
     */
    public String getEpisodeTuneInTime(String episodeTime){
    	StringBuilder output = new StringBuilder();
	    try{	
			SimpleDateFormat df = new SimpleDateFormat("kk:mm");
			Date d1 = df.parse(episodeTime);
			Calendar showTime = Calendar.getInstance();
			showTime.set(Calendar.HOUR_OF_DAY, d1.getHours());
			showTime.set(Calendar.MINUTE, d1.getMinutes());
						
			Calendar centralTime = (Calendar) showTime.clone();
			int currentHour = showTime.get(Calendar.HOUR);
			SimpleDateFormat showDate = new SimpleDateFormat();
			if(showTime.get(Calendar.MINUTE)==0) {
				showDate = new SimpleDateFormat("ha-");
			} else {
				showDate = new SimpleDateFormat("h:mma-");
			}
			output.append(showDate.format(showTime.getTime()));
			// add central time
			showTime.add(Calendar.HOUR_OF_DAY,-1);
			if(showTime.get(Calendar.MINUTE)==0) {
				showDate = new SimpleDateFormat("h'C'");
			} else {
				showDate = new SimpleDateFormat("h:mm'C'");
			}
			output.append(showDate.format(showTime.getTime()));
	    }catch(ParseException e){
	    	log.error("Parse Exception " + e);
	    }
	  return output.toString();
    }
    
    /**
     * Retrieve a Map containing the date parts of a path to a schedule asset,
     * including the translated time code.
     *
     * keys of map are:
     *  year
     *  month (zero padded)
     *  day   (zero padded)
     *  time (translated from timecode into 24 hour time HH:MM, with no zero padding)
     *
     * Removes the path up to the point where the schedule asset root path + brand name is found,
     * then splits the path, assumes the path looks like: year/month/day/timecode/jcr:content
     *
     * @param pathToScheduleAsset String of the path to the Schedule asset
     * @return Map of date parts from schedule path
     */
    private Map<String, String> getDateMapFromSchedulePath(final String pathToScheduleAsset) {
        if (pathToScheduleAsset == null) {
            return null;
        }
        String datePartOfPath = pathToScheduleAsset.replaceFirst(".*" + AssetRootPaths.SCHEDULES.path()
                + FORWARD_SLASH, EMPTY_STRING);
        String[] dateComponents = datePartOfPath.split(FORWARD_SLASH);
        Map<String, String> dateMap = new HashMap<String, String>();
        try {
            dateMap.put("year", dateComponents[0]);
            dateMap.put("month", String.format("%02d", Integer.valueOf(dateComponents[1])));
            dateMap.put("day", String.format("%02d", Integer.valueOf(dateComponents[2])));
            dateMap.put("time", TIME_CODE_MAP.get(Integer.valueOf(dateComponents[3])));
            dateMap.put("timeCode", dateComponents[3]);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
        return dateMap;
    }
    
    /**
     * Using the current time as a starting point, find all schedule nodes from today forward and 60 days old
     * Stuff their properties into a Map and return as a List
     * @param resource Sling Resource in hand
     * @return a List of Maps containing node properties of schedules
     */
    private List<Map<String, Object>> findSchedules(final Resource resource) {
        Calendar calendar = Calendar.getInstance();
        //below code added to get past 62 Days schedules
		calendar.add(Calendar.DAY_OF_MONTH, -30);
		calendar.add(Calendar.DAY_OF_MONTH, -30);
		calendar.add(Calendar.DAY_OF_MONTH, -2);
		
        //pathBuilder will contain the path down to the day for the desired schedule asset
        //priming its value here, it will be set to a new path from within a loop ahead
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(schedulePathFromCalendar(calendar));

        ResourceResolver resourceResolver = resource.getResourceResolver();
        List<Map<String, Object>> foundSchedules = new ArrayList<Map<String, Object>>(); //return value

        //we get the most outstanding day with schedule and then start with -62 days going forward till we reach it
        String farthestDay = getFarthestDay(resource);
        log.debug("Farthest schedule day is " + farthestDay);
        while(!pathBuilder.toString().equals(farthestDay)) {
            if (resourceResolver.resolve(pathBuilder.toString()) != null) {
                foundSchedules.addAll(findAssetsByPropertyValue(resource, pathBuilder.toString(),
                        AssetSlingResourceTypes.SCHEDULE.resourceType(), null, null, null));
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            pathBuilder.setLength(0);
            pathBuilder.append(schedulePathFromCalendar(calendar));
        }
        if (resourceResolver.resolve(pathBuilder.toString()) != null) {
            foundSchedules.addAll(findAssetsByPropertyValue(resource, pathBuilder.toString(),
                    AssetSlingResourceTypes.SCHEDULE.resourceType(), null, null, null));
        }

        return foundSchedules;
    }

    /**
     * subroutine method to get the farthest imported day with schedule
     * @param resource for resource resolution
     * @return path to the farthest day of imported schedule
     */
    private String getFarthestDay(final Resource resource) {
        Calendar now = Calendar.getInstance();
        String year = getMaxDate(AssetRootPaths.SCHEDULES.path(), now.get(Calendar.YEAR), resource);
        String month = getMaxDate(year, now.get(Calendar.MONTH)+1, resource);
        String day = getMaxDate(month, now.get(Calendar.DAY_OF_MONTH), resource);
        return day;
    }

    /**
     * subroutine method to get the farthest date folder on a certain level. in case parent points to a year folder like
     * /etc/sni-assets/schedule/2014 this method would return path to the most outstanding month folder
     * @param parent path to a folder which children we should look through
     * @param defaultDate in case for some reason nothing is found what should be returned by default
     * @param resource resource for resource resolution
     * @return path to the farthest year, month or day
     */
    private String getMaxDate(String parent, Integer defaultDate, final Resource resource) {
        ResourceResolver resolver = resource.getResourceResolver();
        Node parentNode = resolver.getResource(parent).adaptTo(Node.class);
        NodeIterator nodes = null;
        Integer maxDate = defaultDate;
        try {
            nodes = parentNode.getNodes();
            while (nodes.hasNext()) {
                Node dateNode = nodes.nextNode();
                Integer _maxDate = 0;
                try {
                    _maxDate = Integer.parseInt(dateNode.getName());
                } catch (NumberFormatException e) {
                    log.info(e.getMessage());
                }
                if (_maxDate!=null && _maxDate > maxDate) maxDate = _maxDate;
            }
        } catch (RepositoryException e) {
            log.error(e.getMessage());
        }
        return parent + FORWARD_SLASH + maxDate.toString();
    }
    
    /**
     * subroutine method for building strings, used by findAllFutureSchedules
     * returns path up to the day.
     * See also: {@link #timeCodeFromCalendar(Calendar)}
     * @param calendar Calendar from which you want to build a path to a schedule
     * @return String path to schedule
     */
    private String schedulePathFromCalendar(final Calendar calendar) {
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
     * Generic search utility that uses the CQ search API to locate nodes in the content repo using
     * the sling:resourceType of the desired asset and a string representing the value of an anonymous
     * property to identify some content within the properties of the node(s). Returns a List of Nodes.
     *
     *
     * @param resource Resource in hand
     * @param pathToAssetRoot String path to the root path of desired asset type 
     * @param resourceType String of sling:resourceType property to filter search by
     * @param searchTerm String of the search term you are using to retrieve the asset nodes
     * @param sortKey
     * @param sortOrder
     * @return List of Maps containing properties of found nodes
     */
    private List<Map<String, Object>> findAssetsByPropertyValue(final Resource resource,
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
                .append("SELECT * FROM [cq:PageContent] AS s WHERE ISDESCENDANTNODE([")
                .append(pathToAssetRoot)
                .append("]) AND s.[")
                .append(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName())
                .append("] = '")
                .append(resourceType)
                .append("'");
            if (isSorted) {
                query.append(" order by [" + sortKey + "] ");
                if (hasSortOrder && (sortOrder.equalsIgnoreCase(SORT_ORDER_ASC)
                        || sortOrder.equalsIgnoreCase(SORT_ORDER_DESC))) {
                    query.append(sortOrder);
                } else {
                    query.append(SORT_ORDER_ASC);
                }
            }
            log.debug("QUERY " + query.toString());
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
     * Munge the path to a show asset into a relative path to the show's page
     * @param showAssetPath String relative path to the show asset in JCR
     * @return String relative path to the show page
     */
    private String getShowContentPathFromAssetPath(final String showAssetPath, final Page episodePage) {
        if (showAssetPath == null) {
        	if(episodePage == null){
        		return null;
        	}else{
        		String parentShowPage = episodePage.getParent().getParent().getPath();
        		return parentShowPage+".html";
        	}        	
        }
        return TagUtils.completeHREF(Functions.getBasePath(showAssetPath
                .replaceFirst(AssetRootPaths.SHOWS.path(), ContentRootPaths.SHOWS.path())));
    }
    
}