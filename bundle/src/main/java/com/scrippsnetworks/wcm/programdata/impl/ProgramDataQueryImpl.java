package com.scrippsnetworks.wcm.programdata.impl;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.asset.show.TimeSlot;
import com.scrippsnetworks.wcm.config.SiteConfigService;
import com.scrippsnetworks.wcm.config.impl.SiteConfigUtil;
import com.scrippsnetworks.wcm.fnr.util.PageSlingResourceTypes;
import com.scrippsnetworks.wcm.fnr.util.AssetRootPaths;
import com.scrippsnetworks.wcm.fnr.util.AssetSlingResourceTypes;
import com.scrippsnetworks.wcm.util.*;
import org.apache.commons.lang.time.DateUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class ProgramDataQueryImpl {

    private static final Logger log = LoggerFactory.getLogger(ProgramDataQueryImpl.class);

    private String excludeNodes = "crxdao:meta";

    private static final String SLASH_JCR_CONTENT = "/jcr:content";

    private static final String SCHEDULE_SORT_DATE = "sni:scheduleDate";


    /***
     * retrieves timeslots using node iteration or a jcr query based on the search parameters specificed
     * @param request the http request that is part of this request, so we can look at the path and figure out which site this is for
     * @param startDate the start date of the query
     * @param endDate the end date of the query
     * @param searchPages a list of pages (talent, show, recipe, episode) to use in the query
     * @param includeNonPrimaryPeople should secondary talent be included in the search (sni:people)
     * @param limit the number of results to return
     * @param sortDesc should results be sorted desc
     * @return
     */
    public List<TimeSlot> getTimeslots(SlingHttpServletRequest request, Calendar startDate, Calendar endDate, List<String> searchPages, boolean includeNonPrimaryPeople, Integer limit, boolean sortDesc) {
        ArrayList<TimeSlot> results = new ArrayList<TimeSlot>();
        try {
            ArrayList<String> showPaths = null;
            ArrayList<String> episodePaths = null;
            List<String> validEpisodes = null;
            String schedulePath=null;
            String startTime=null;

            //based on the path of this request, get the config service for this site, then get the properties we need for queries
            SiteConfigService siteConfigService = SiteConfigUtil.getSiteConfigService(request);
            if(siteConfigService!=null) {
                schedulePath=siteConfigService.getScheduleRootAssetPath();
                startTime=siteConfigService.getScheduleStartTime();
            }

            //limit results returned if not set in query so we don't get traversing everything
            if(limit==null) {
                limit=10;
            }

            if(schedulePath==null || startTime==null || schedulePath.isEmpty() || startTime.isEmpty()) {
                log.error("exiting getTimeslots, no schedule path or start time defined");
                return results;
            }
            Date timer = new Date();

            PageManager pm = request.getResource().getResourceResolver().adaptTo(PageManager.class);
            // if we are filtering results to only specific page types,
            // we need to get the assets associated with the pages that were passed in
            if(searchPages!=null) {
                log.trace("searching for pages for talent/recipes");
                //we are looking for schedule data pertaining to a specific talent, show, episode, or recipe

                for(String searchPage : searchPages) {
                    log.debug("getting filter page for path:" + searchPage);
                    Page page = pm.getPage(searchPage);

                    ArrayList<String> recipePaths = new ArrayList<String>();
                    ArrayList<String> talentPaths = new ArrayList<String>();
                    if(page!=null) {
                        String resourceType = page.getProperties().get(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName(),"");
                        //TODO: use a switch statement since java7 support switch with strings?
                        if(resourceType.equals(PageSlingResourceTypes.TALENT.resourceType())) {
                            talentPaths.add(page.getProperties().get("sni:assetLink","/na"));

                        } else if(resourceType.equals(PageSlingResourceTypes.EPISODE.resourceType())) {
                            // store these episodes so we can see if we match a query
                            if(episodePaths==null) {
                                episodePaths = new ArrayList<String>();
                            }
                            episodePaths.add(page.getProperties().get("sni:assetLink","/na"));

                        } else if(resourceType.equals(PageSlingResourceTypes.SHOW.resourceType())) {
                            if(showPaths==null) {
                                showPaths = new ArrayList<String>();
                            }
                            log.debug("adding show path");
                            showPaths.add(page.getProperties().get("sni:assetLink","/na"));

                        } else if(resourceType.equals(PageSlingResourceTypes.RECIPE.resourceType())) {
                            recipePaths.add(page.getProperties().get("sni:assetLink","/na"));
                        }
                    } else {
                        log.error("invalid page path used in query");
                        return  results;
                    }

                    // get the valid episodes to use if we had talent or recipes as part of the query, if the list isn't null
                    // we'll compare episodes we find in the schedule to this list
                    if(talentPaths.size()>0 || recipePaths.size() > 0) {
                        validEpisodes = findEpisodes(request,talentPaths,recipePaths,includeNonPrimaryPeople);
                    }
                    //no matches for recipes or chefs, we don't even need to search the schedule
                    if(validEpisodes!=null && validEpisodes.size()==0) {
                        log.debug("no valid episodes or shows found for recipes and/or talent searched on, returning.");
                        return  results;
                    } else if (validEpisodes!=null) {
                        // if user passed in shows or episodes, then remove shows/episodes found from recipe/talent search that shouldn't be included
                        int episodesSize = validEpisodes.size()-1;

                        for(int i=0;i<=episodesSize;i++) {
                            boolean valid=true;
                            String path = validEpisodes.get(i);
                            log.debug("checking:" + path);
                            if((showPaths!=null && !showPaths.contains(path))) {
                                valid=false;
                            }
                            if(episodePaths!=null && !episodePaths.contains(path)) {
                                if(showPaths==null) {
                                    valid=false;
                                }
                            } else if(episodePaths!=null) {
                                valid = true;
                            }
                            if(!valid) {
                                validEpisodes.remove(path);
                                log.debug("removing");
                                i-=1;
                                episodesSize-=1;
                            }
                        }
                    }
                }
            }
            log.debug("*****time to get paths to include in query:" + (new Date().getTime() - timer.getTime()));
            timer = new Date();

            /**************  IF WE WANT TO USE NODE TRAVERSAL, USE THIS LOGIC ***************************/

            // based on our start and end date, find shows in that time frame matching
            if(episodePaths!=null && episodePaths.size()>0) {
                if(validEpisodes==null) {
                    validEpisodes = new ArrayList<String>();
                }
                validEpisodes.addAll(episodePaths);
            }

            // split up the shows/episodes array into seperate shows and episodes
            if(validEpisodes!=null) {
                log.debug("valid episodes found");
                for(String item : validEpisodes) {
                    Node asset = request.getResourceResolver().adaptTo(Session.class).getNode(item + SLASH_JCR_CONTENT);
                    if(asset!=null) {
                        log.debug("asset isn't null");
                        if(asset.hasProperty(AssetPropertyNames.SLING_RESOURCE_TYPE.propertyName())) {
                            String resourceType = asset.getProperty(AssetPropertyNames.SLING_RESOURCE_TYPE.propertyName()).getString();
                            if(resourceType.equals(AssetSlingResourceTypes.EPISODE.resourceType())) {
                                // store these episodes so we can see if we match a query
                                if(episodePaths==null) {
                                    episodePaths = new ArrayList<String>();
                                }
                                episodePaths.add(item);

                            } else if(resourceType.equals(AssetSlingResourceTypes.SHOW.resourceType())) {
                                // store these episodes so we can see if we match a query
                                if(showPaths==null) {
                                    showPaths = new ArrayList<String>();
                                }
                                showPaths.add(item);

                            }
                        }

                    }
                }
            }
            List<TimeSlot> timeslots = getTimeslotsByDateRange(request.getResource(), startDate, endDate, schedulePath, startTime,limit,episodePaths,showPaths, sortDesc);
            if(timeslots!=null) {
                results.addAll(timeslots);
            } /*
            } else {

            results = new ArrayList<TimeSlot>();

            // split up the shows/episodes array into seperate shows and episodes

            List<TimeSlot> items = getTimeslotsByDateRangeQuery(request.getResource(),startDate,endDate,showPaths,episodePaths,startTime,limit, sortDesc);
            if(items!=null) {
                results.addAll(items);
            }

            */
            log.debug("*****time to find schedule:" + (new Date().getTime() - timer.getTime()));
        }  catch (Exception ex) {
            log.error(ex.getMessage(),ex);
        }
        log.trace("total timeslots returned" + results.size());
        return  results;
    }

    /***
     * gets the default featured show time based on the path defined in site config for the default location of the component that defines the featured time each day
     * @param request the http request that is part of this request, so we can look at the path and figure out which site this is for
     * @return Calendar of featured time for today
     */
    public Calendar getDefaultFeaturedTime(SlingHttpServletRequest request) {
        SiteConfigService siteConfigService = SiteConfigUtil.getSiteConfigService(request);
        if(siteConfigService!=null) {
            return getFeaturedTime(siteConfigService.getFeaturedSchedulePath(),request);
        }
        return null;

    }

    /***
     * gets the  featured show time for today based on the path to featured time component that is passed in.  If current time is past featured time for today, gives you tomorrow's featured time
     * @param componentPath path to the featured time component to use for finding the featured time
     * @param request the http request that is part of this request, so we can look at the path and figure out which site this is for
     * @return Calendar of featured time for today
     */
    private Calendar getFeaturedTime(String componentPath, SlingHttpServletRequest request) {
        Calendar currentTime = Calendar.getInstance();
        Calendar featuredTime = Calendar.getInstance();
        int day = featuredTime.get(Calendar.DAY_OF_WEEK);
        Resource defaultComponent = request.getResourceResolver().getResource(componentPath);
        if(defaultComponent!=null) {
            Node node = defaultComponent.adaptTo(Node.class);
            String[] featureSplit = getFeaturedTimeForDay(day,node);
            if(featureSplit!=null) {
                // add 12 hours to hour since it was set using PM, and now we are using 24 hour clock
                featuredTime.set(Calendar.HOUR_OF_DAY,Integer.parseInt(featureSplit[0])+12);
                featuredTime.set(Calendar.MINUTE,Integer.parseInt(featureSplit[1]));

                // even if show was already on today and we are past it's showtime, we still display it
                return featuredTime;
            }
        }
        return null;
    }

    /***
     * gets the time value for a given feature time node
     * @param day day of week we are looking for
     * @param node the featured time node to examine
     * @return String array of hour and minute of featured time
     */
    private String[] getFeaturedTimeForDay(int day, Node node) {
        try {
            log.debug("featured time for day" + day + "path " + node.getPath());
            if(node.hasProperty("day_" + day)) {
                return node.getProperty("day_" + day).getString().replace(" PM","").split(":");
            }
        } catch (Exception ex) {

        }
        return null;
    }


    /***
     * retrieves timeslots using node iteration from the schedule nodes, will filter out results based on params
     * @param resource the resource form the request, so we can look at the path and figure out which site this is for
     * @param startDate the start date of the query
     * @param endDate the end date of the querythe path to the schedule day we are examining
     * @param schedulePath the path to the day's schedule node that timeslots live under
     * @param startTime the daily start time of this channel (from site config)
     * @param limit number of results to return
     * @param episodePaths paths to episodes to include in qeury
     * @param showPaths paths to shows to include in query
     * @param sortDesc should results be sorted desc
     * @return
     */
    private List<TimeSlot> getTimeslotsByDateRange(Resource resource, Calendar startDate, Calendar endDate, String schedulePath, String startTime, Integer limit, List<String> episodePaths, List<String> showPaths, boolean sortDesc) {
        List<TimeSlot> results = new ArrayList<TimeSlot>();
        Calendar currentDate;
        if(startDate!=null && endDate==null) {
            // default end date to 1 month from now
            endDate = (Calendar) startDate.clone();
            endDate.add(Calendar.MONTH,1);
        }
        if(endDate!=null && startDate==null) {
            // default start date to 14 days ago
            startDate = (Calendar) endDate.clone();
            startDate.add(Calendar.DAY_OF_YEAR,-14);
        }

        //based on sort order, set day we start looking for content on
        if(sortDesc) {
            currentDate = (Calendar)endDate.clone();
        } else {
            currentDate = (Calendar)startDate.clone();
        }

        Calendar endDateStartTime;
        endDateStartTime = (Calendar)endDate.clone();


        String[] startTimeHourMin = startTime.split(":");
        endDateStartTime.set(Calendar.HOUR_OF_DAY,Integer.parseInt(startTimeHourMin[0]));
        endDateStartTime.set(Calendar.MINUTE,Integer.parseInt(startTimeHourMin[1]));
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        boolean onFirst = true;
        boolean onLast = true;
        int totalMissing = 0;
        int daysDiff = daysBetween(startDate,endDate);
        int counter = 0;
        log.debug("begin date:" + dateFormat.format(startDate.getTime()));
        log.debug("end date:" + dateFormat.format(endDate.getTime()));
        log.debug("current date:" + dateFormat.format(currentDate.getTime()));
        log.debug("sort:" +  sortDesc);
        log.debug("days diff:" +  daysDiff);


        while (counter<daysDiff) {

            // compare date is the start time of the day we are currently on
            Calendar compareDate = (Calendar)currentDate.clone();

            compareDate.set(Calendar.HOUR_OF_DAY,Integer.parseInt(startTimeHourMin[0]));
            compareDate.set(Calendar.MINUTE,Integer.parseInt(startTimeHourMin[1]));
            log.debug("on day:" + dateFormat.format(currentDate.getTime()));

            int beginTimeslot = 1;
            int endTimeslot = 48;
            log.debug("compare date:" + dateFormat.format(compareDate.getTime()));
            // below, we calculate the start and end timeslots for the day if it is the first or last day
            // if this is the first or last day in the range, set the begin and end timeslots based on the dates passed in
            if(DateUtils.isSameDay(startDate,compareDate) && onFirst) {

                log.trace("on start date");
                dateFormat.setCalendar(compareDate);
                log.trace("compare date:" + dateFormat.format(compareDate.getTime()));
                dateFormat.setCalendar(startDate);
                log.trace("start date:" + dateFormat.format(startDate.getTime()));
                if(compareDate.getTime().after(startDate.getTime())) {
                    // need to move back a day and set timeslot to correct location
                    long minutesDiff = (compareDate.getTime().getTime() - startDate.getTime().getTime()) / 1000;
                    int timeslotsBack = (int)(minutesDiff / (30 * 60) - 1);
                    beginTimeslot = endTimeslot - timeslotsBack;
                    currentDate.add(Calendar.DAY_OF_YEAR,-1);

                } else {
                    long minutesDiff = (startDate.getTime().getTime() - compareDate.getTime().getTime()) / 1000;
                    beginTimeslot = (int)(minutesDiff / (30 * 60) + 1);
                }
                onFirst = false;
            }else if(DateUtils.isSameDay(endDate,compareDate)) {
                if(compareDate.getTime().after(endDate.getTime()) && onFirst) {
                    onFirst=false;
                    // need to set end timeslot based on end time
                    long minutesDiff = (endDate.getTime().getTime() - compareDate.getTime().getTime()) / 1000;
                    beginTimeslot = (int)(minutesDiff / (30 * 60) + 1);
                    log.debug("on last day, setting begin timeslot to:" + beginTimeslot);
                } else if(compareDate.getTime().after(endDate.getTime())) {
                    // the end time was before the start time for the current day, so we've already processed all the episodes we need,
                    // since the episodes we are querying for were in yesterdays
                    break;


                } else if(compareDate.getTime().before(endDate.getTime())) {
                    // set the end timeslot
                    // need to move back a day and set timeslot to correct location
                    long minutesDiff = (endDate.getTime().getTime() - compareDate.getTime().getTime()) / 1000;
                    int timeslotsBack = (int)(minutesDiff  / (30 * 60));
                    endTimeslot = timeslotsBack + 1;
                    log.debug("on last day, setting end timeslot to:" + endTimeslot);
                }
            } else {


                if(endDate.before(endDateStartTime)) {
                    log.trace("end date's time is before start time for that date");
                    Calendar dayBeforeLast = (Calendar) endDate.clone();
                    dayBeforeLast.add(Calendar.DAY_OF_YEAR,-1);
                    // if we are on the day before the last day, and the last day end time is before the start time for the day
                    // then we actually want to trim timeslots from the prior day
                    if(DateUtils.isSameDay(compareDate,dayBeforeLast)) {
                        long minutesDiff = (endDate.getTime().getTime() - compareDate.getTime().getTime()) / 1000;
                        int timeslotsBack = (int)(minutesDiff / (30 * 60)+1);
                        endTimeslot = timeslotsBack;
                    }
                }
            }
            log.debug("begin timeslot:" + beginTimeslot);
            log.debug("end timeslot:" + endTimeslot);
            Resource daySchedule = getScheduleNode(resource,currentDate,schedulePath);
            if(daySchedule!=null) {
                totalMissing=0;
                Iterator<Resource> iter = daySchedule.listChildren();
                while (iter.hasNext()) {
                    Resource currResource = iter.next();
                    if(currResource.getName() != null && !currResource.getName().equals(excludeNodes)) {
                        int currentTimeslot = Integer.parseInt(currResource.getName());
                        log.trace("current timeslot is:" + currentTimeslot);
                        if(currentTimeslot>=beginTimeslot && currentTimeslot <= endTimeslot) {

                            //TODO: should we only load recipes (the 1 param) if we need them?
                            Calendar timeslotDate = (Calendar) currentDate.clone();
                            timeslotDate.set(Calendar.HOUR_OF_DAY,Integer.parseInt(startTimeHourMin[0]));
                            timeslotDate.set(Calendar.MINUTE,Integer.parseInt(startTimeHourMin[1]));
                            TimeSlot timeslot = new TimeSlot(currResource,timeslotDate,1);

                            if(validEpisode(timeslot,showPaths,episodePaths)) {
                                results.add(timeslot);
                            }
                        }
                    }
                }
            } else {
                totalMissing+=1;
            }

            //move to next date in range
            if(sortDesc) {
                daysDiff-=1;
                currentDate.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                currentDate.add(Calendar.DAY_OF_YEAR, 1);
            }
            counter+=1;


            if(results.size()>=limit) {
                break;
            }
            log.debug("finished loop, days diff:" + daysDiff);
            log.debug("finished loop, counter:" + counter);
        }
        // time slots are to be ordered by start time
        if(sortDesc) {
            Collections.sort(results, new Comparator<TimeSlot>() {
                public int compare(TimeSlot o1, TimeSlot o2) {
                    return (int)(o2.getStartTime().getTime().getTime()-o1.getStartTime().getTime().getTime());
                }
            });
        } else {
            Collections.sort(results, new Comparator<TimeSlot>() {
                public int compare(TimeSlot o1, TimeSlot o2) {
                    return (int)(o1.getStartTime().getTime().getTime()-o2.getStartTime().getTime().getTime());
                }
            });
        }

        log.debug("results size:" + results.size());

        // if we got more results in one day than we wanted total, trim the end of them
        if(results.size()>=limit) {
            results = results.subList(0,limit);
        }

        return results;

    }


    /***
     * retrieves timeslots using jcr query
     * @param resource the resource form the request, so we can look at the path and figure out which site this is for
     * @param startDate the start date of the query
     * @param endDate the end date of the querythe path to the schedule day we are examining
     * @param showPaths paths to shows to include in query
     * @param episodePaths paths to episodes to include in qeury
     * @param startTime the daily start time of this channel (from site config)
     * @param limit the total number of results to include
     * @param sortDesc should results be sorted desc
     * @return
     */
    private List<TimeSlot> getTimeslotsByDateRangeQuery(Resource resource, Calendar startDate, Calendar endDate, List<String> showPaths, List<String> episodePaths, String startTime, Integer limit, boolean sortDesc) {
        List<TimeSlot> results = new ArrayList<TimeSlot>();
        try {

            String startTimeslotTime = convertDateToTimeslotDate(startDate,startTime);
            String endTimeslotTime = convertDateToTimeslotDate(endDate,startTime);

            QueryManager qm = resource.getResourceResolver().adaptTo(Session.class).getWorkspace().getQueryManager();
            StringBuilder query = new StringBuilder();

            // setup query by path and resource type
            query.append("/jcr:root/etc/sni-asset/schedules//element(*,cq:PageContent)[@sni:assetType='SCHEDULE']");

            // add start and end time to query if defined
            if(startTimeslotTime!=null || endTimeslotTime!=null) {
                query.append("[");
                if(startTimeslotTime!=null) {
                    query.append("@")
                            .append(SCHEDULE_SORT_DATE)
                            .append(">='")
                            .append(startTimeslotTime)
                            .append("'");
                    if(endTimeslotTime!=null) {
                        query.append(" and ");
                    }
                }
                if(endTimeslotTime!=null) {
                    query.append("@")
                            .append(SCHEDULE_SORT_DATE)
                            .append("<='")
                            .append(endTimeslotTime)
                            .append("'");
                }
                query.append("]");
            }

            // add shows/episodes we are filtering by
            if(episodePaths!=null && episodePaths.size()>0) {
                query.append("[");
                int count=0;
                for(String path : episodePaths) {
                    if(count>0 && count<episodePaths.size()) {
                        query.append(" or @");
                    }
                    query.append(Constant.SNI_EPISODE)
                            .append("='")
                            .append(path)
                            .append("'");
                    count+=1;
                }
                query.append("]");
            }
            if(showPaths!=null && showPaths.size()>0) {
                query.append("[");
                int count=0;
                for(String path : showPaths) {
                    if(count>0 && count<showPaths.size()) {
                        query.append(" or @");
                    }
                    query.append(Constant.SNI_SHOW)
                            .append("='")
                            .append(path)
                            .append("'");
                    count+=1;
                }
                query.append("]");
            }
            //show and episode must be populated
            query.append("[@sni:show and @sni:episode]");
            query.append(" order by @")
                    .append(SCHEDULE_SORT_DATE);
            if(sortDesc) {
                query.append(" descending");
            } else {
                query.append(" ascending");
            }


            log.debug(String.format("search query %s",query.toString()));


            Query xpathQuery = qm.createQuery(query.toString(), Query.XPATH);

            if(limit!=null) {
                xpathQuery.setLimit(limit);
            } else {
                log.info("no limit set on query, defaulting to 10");
                // hard code a limit so we don't try to read thousands of nodes at once if that is what the query would return
                xpathQuery.setLimit(10);
            }
            Date timer = new Date();
            NodeIterator resultNodes = xpathQuery.execute().getNodes();
            log.debug("*****query execution time:" + (new Date().getTime() - timer.getTime()));
            timer = new Date();
            while(resultNodes.hasNext()) {
                Node result = resultNodes.nextNode();

                // to create a timeslot, the constructor requires the date of this show,
                // we need to figure out what the time is based on the time
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR,Integer.parseInt(result.getParent().getParent().getParent().getParent().getName()));
                cal.set(Calendar.MONTH,Integer.parseInt(result.getParent().getParent().getParent().getName())-1);
                cal.set(Calendar.DAY_OF_MONTH,Integer.parseInt(result.getParent().getParent().getName()));
                String[] startTimeHourMin = startTime.split(":");
                cal.set(Calendar.HOUR_OF_DAY,Integer.parseInt(startTimeHourMin[0]));
                cal.set(Calendar.MINUTE,Integer.parseInt(startTimeHourMin[1]));
                cal.set(Calendar.SECOND,0);
                results.add(new TimeSlot(resource.getResourceResolver().getResource(result.getParent().getPath()),cal, 1));
            }
            log.debug("*****added results to timeslot list:" + (new Date().getTime() - timer.getTime()));

        } catch (Exception ex) {
            log.error(ex.getMessage(),ex);
        }
        return results;

    }


    /**
     * Convert a date into a timeslot date, used for jcr queries against schedule data
     *
     * @param date the date to create the query for
     * @param startTime the start time for this channel in a HH:mm format
     * @return string in yyyyMMddtt format, with 'tt' being 2 char timeslot with leading 0 if needed
     */
    private String convertDateToTimeslotDate(Calendar date, String startTime) {
        if(date==null) {
            return null;
        }
        DateFormat formatter = new SimpleDateFormat("yyyy_M_d_HH_mm");
        log.debug("convert date is:" + formatter.format(date.getTime()));
        // compare time passed in to time at which timeslot 1 begins, and if it is before it,
        // set the date back one since the timeslot for the previous date will actually match this start date
        String[] startTimeHourMin = startTime.split(":");
        Calendar compareDate = (Calendar)date.clone();
        compareDate.set(Calendar.HOUR_OF_DAY,Integer.parseInt(startTimeHourMin[0]));
        compareDate.set(Calendar.MINUTE,Integer.parseInt(startTimeHourMin[1]));
        if(compareDate.after(date)) {
            date.add(Calendar.DAY_OF_MONTH,-1);
        }
        log.debug("convert date after hour add:" + formatter.format(date.getTime()));
        // create padded strings for year month day of date we are processing
        String year = Integer.toString(date.get(Calendar.YEAR));
        String month = String.format("%02d",date.get(Calendar.MONTH) + 1);
        String day = String.format("%02d",date.get(Calendar.DAY_OF_MONTH));

        //get the current time in string format
        String currentTime = String.format("%02d",date.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d",date.get(Calendar.MINUTE));
        log.debug("current time:" + currentTime);
        // get the string of the timeslot ID for the current time
        String timeslot = getTimecodesByStartTime(startTime).get(currentTime);

        return year + month + day + timeslot;
    }

    /**
     * Get map of the 48 timeslots with key of time based on start date of this channel
     *
     * @param startTime the start time for this channel in a HH:mm format
     * @return map of start times and associated timeslots in HH:mm and timeslots returned with 2 chars using leading 0 if needed
     */
    private Map<String, String> getTimecodesByStartTime(String startTime) {
        HashMap<String,String> timeslots = new HashMap<String, String>();
        Calendar date = Calendar.getInstance();
        String[] startTimeHourMin = startTime.split(":");
        date.set(Calendar.HOUR_OF_DAY,Integer.parseInt(startTimeHourMin[0]));
        date.set(Calendar.MINUTE,Integer.parseInt(startTimeHourMin[1]));
        for(int i=1;i<=48;i++) {
            timeslots.put(String.format("%02d",date.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d",date.get(Calendar.MINUTE)),String.format("%02d",i));
            date.add(Calendar.MINUTE,30);
        }
        return timeslots;
    }

    /**
     * Get Schedule Node by Date
     *
     * @param cal
     * @return resource of the schedule node
     */
    private Resource getScheduleNode(Resource resource, Calendar cal, String schedulePath) {
        // the schedule node will be in the path
        // /[root of schedule root path]/yyyy/m/d
        if (cal != null) {
            String path = schedulePath + "/" + cal.get(Calendar.YEAR)
                    + "/" + (cal.get(Calendar.MONTH) + 1) + "/"
                    + cal.get(Calendar.DAY_OF_MONTH);
            log.trace("schedule node path is:" + path);

            return resource.getResourceResolver().getResource(path);
        } else {
            return null;
        }
    }

    /**
     * See if episode tied to timeslot matches list of shows/episodes to filter by
     *
     * @param timeslot timeslot to examine
     * @param shows list of shows to check against
     * @param episodes list of episodes to check against
     * @return boolean if episode is valid
     */
    private boolean validEpisode(TimeSlot timeslot, List<String> shows, List<String> episodes) {
        if(timeslot.getEpisodePath()!=null) {
            if(shows==null && episodes==null) {
                return true;
            }
            String timeslotEpisode = timeslot.getEpisodePath();
            if(episodes!=null) {
                if(episodes.contains(timeslotEpisode)) {
                    return true;
                }
            }
            String timeslotShow = timeslot.getShowPath();

            if(timeslotShow!=null) {
                if(shows!=null) {
                    if(shows.contains(timeslotShow)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Based on talents or recipes, do a jcr query to find all shows/episodes they appear in
     *
     * @param request request used for session
     * @param people talent page paths to query for
     * @param recipes recipe page paths to query on
     * @param includeNonPrimaryPeople include sni:people in results for talent searches
     * @return List of paths that are valid
     */
    private List<String> findEpisodes(SlingHttpServletRequest request, List<String> people, List<String> recipes, boolean includeNonPrimaryPeople) {
        List<String> episodes = new ArrayList<String>();
        if(request==null || (people==null && recipes==null)) {
            return null;
        }
        try {
            QueryManager queryManager = request.getResource().getResourceResolver().adaptTo(Session.class).getWorkspace().getQueryManager();
            StringBuilder query = new StringBuilder();
            query
                    .append("/jcr:root")
                    .append(AssetRootPaths.SHOWS.path())
                    .append("//element(*,cq:PageContent)");
            if(people!=null && people.size() > 0) {
                query.append("[");
                int count = 0;
                for(String talent : people) {
                    if(count>0 && count<people.size()) {
                        query.append(" or ");
                    }
                    //query.append("@sni:assetPrimaryTalent='")
                    query.append("@sni:primaryTalent='")
                            .append(talent)
                            .append("'");
                    if(includeNonPrimaryPeople) {
                        query.append(" or @sni:people='")
                                .append(talent)
                                .append("'");
                    }
                    count++;
                }
                query.append("]");
            }
            if(recipes!=null && recipes.size() > 0) {
                query.append("[");
                int count = 0;
                for(String recipe : recipes) {
                    if(count>0 && count<recipes.size()) {
                        query.append(" or ");
                    }
                    query.append("@sni:recipes='")
                            .append(recipe)
                            .append("'");
                    count++;
                }
                query.append("]");
            }
            query.append("[@sling:resourceType='sni-food/components/assets/show' or @sling:resourceType='sni-food/components/assets/episode']");
            log.debug(String.format("xpath query to find shows and episodes by talent/recipe: %s",query.toString()));

            Query compiledQuery = queryManager.createQuery(query.toString(), Query.XPATH);
            NodeIterator nodeItr = compiledQuery.execute().getNodes();
            while (nodeItr.hasNext()) {
                Node node = nodeItr.nextNode();
                episodes.add(node.getPath().replace(SLASH_JCR_CONTENT,""));
            }


        } catch (Exception ex) {
            log.error(ex.getMessage(),ex);
        }

        return episodes;
    }


    /**
     * find number of days between two days
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return number of days difference
     */
    public static int daysBetween(Calendar startDate, Calendar endDate) {
        Calendar date = (Calendar) startDate.clone();
        int daysBetween = 0;
        while (date.before(endDate)) {
            date.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }
        return daysBetween;
    }



}
