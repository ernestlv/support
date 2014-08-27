package com.scrippsnetworks.wcm.mobile.schedule.export.impl;

import com.day.cq.commons.jcr.JcrConstants;
import com.scrippsnetworks.wcm.fnr.util.AssetRootPaths;
import com.scrippsnetworks.wcm.fnr.util.AssetSlingResourceTypes;
import com.scrippsnetworks.wcm.mobile.schedule.export.TVScheduleExport;
import com.scrippsnetworks.wcm.mobile.schedule.export.TVScheduleItem;
import com.scrippsnetworks.wcm.taglib.Functions;
import com.scrippsnetworks.wcm.util.PagePropertyNames;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.*;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.servlet.ServletException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SlingServlet(selectors = "mobileexport", methods = "GET", extensions = "xml", resourceTypes = {"sni-food/components/pagetypes/universal-landing"})
public class TVScheduleExportImpl extends SlingSafeMethodsServlet {
    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private ResourceResolver resourceResolver;
    private StringBuilder exportXML;
    private Logger log = LoggerFactory.getLogger(TVScheduleExportImpl.class);

    private static final String MOBILE_EXPORT = "mobileexport";
    public static final String NODE_META_PATH = "crxdao:meta/foreignIdentifier";
    public static final String PROPNAME_ID = "crxdao:assetId";
    public static final String PROPNAME_TITLE = "jcr:title";
    public static final String PROPNAME_DESCRIPTION = "sni:body";
    public static final String PROPNAME_DURATION = "sni:totalRunTime";
    public static final String PROPNAME_SCHEDULE_DATE = "sni:scheduleDate";
    public static final String PROPNAME_RELATED_EPISODE = "sni:episode";
    public static final String PROPNAME_RELATED_SHOW = "sni:show";

    //schedule time code map
    public final Map<Integer, String> TIME_CODE_MAP = new HashMap<Integer, String>() {
        {
            put(1, "6:30:00 AM");
            put(2, "7:00:00 AM");
            put(3, "7:30:00 AM");
            put(4, "8:00:00 AM");
            put(5, "8:30:00 AM");
            put(6, "9:00:00 AM");
            put(7, "9:30:00 AM");
            put(8, "10:00:00 AM");
            put(9, "10:30:00 AM");
            put(10, "11:00:00 AM");
            put(11, "11:30:00 AM");
            put(12, "12:00:00 PM");
            put(13, "12:30:00 PM");
            put(14, "13:00:00 PM");
            put(15, "13:30:00 PM");
            put(16, "14:00:00 PM");
            put(17, "14:30:00 PM");
            put(18, "15:00:00 PM");
            put(19, "15:30:00 PM");
            put(20, "16:00:00 PM");
            put(21, "16:30:00 PM");
            put(22, "17:00:00 PM");
            put(23, "17:30:00 PM");
            put(24, "18:00:00 PM");
            put(25, "18:30:00 PM");
            put(26, "19:00:00 PM");
            put(27, "19:30:00 PM");
            put(28, "20:00:00 PM");
            put(29, "20:30:00 PM");
            put(30, "21:00:00 PM");
            put(31, "21:30:00 PM");
            put(32, "22:00:00 PM");
            put(33, "22:30:00 PM");
            put(34, "23:00:00 PM");
            put(35, "23:30:00 PM");
            put(36, "00:00:00 PM");
            put(37, "00:30:00 AM");
            put(38, "1:00:00 AM");
            put(39, "1:30:00 AM");
            put(40, "2:00:00 AM");
            put(41, "2:30:00 AM");
            put(42, "3:00:00 AM");
            put(43, "3:30:00 AM");
            put(44, "4:00:00 AM");
            put(45, "4:30:00 AM");
            put(46, "5:00:00 AM");
            put(47, "5:30:00 AM");
            put(48, "6:00:00 AM");
        }
    };

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        response.setHeader("Content-Type", "text/xml");
        try {
            exportXML = new StringBuilder();
            resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);

            String[] selectors = request.getRequestPathInfo().getSelectors();
            int numSelectors = selectors == null ? 0 : selectors.length;

            if (numSelectors < 1) {
                response.sendError(500, "selectors not valid");
            }
            if (selectors[0].equals(MOBILE_EXPORT)) {
                writeHeader();
                List<TVScheduleItem> schedules;
                Integer countDays = 1;
                String daysRequestParam = request.getParameter("days");
                if (StringUtils.isNotBlank(daysRequestParam)) {
                    countDays = Integer.parseInt(daysRequestParam);
                }
                if (countDays > 1) {
                    schedules = findSchedules(request.getResource(), countDays);
                } else {
                    schedules = findSchedules(request.getResource());
                }
                log.debug("Schedule Size " + schedules.size());

                for (TVScheduleItem schedule : schedules) {
                    if (schedule != null & schedule.isValid()) {
                        exportXML.append("<episode>")
                                .append("<id><![CDATA[").append(schedule.getId()).append("]]></id>")
                                .append("<date><![CDATA[").append(schedule.getStartDate()).append("]]></date>")
                                .append("<duration><![CDATA[").append(schedule.getDuration()).append(" min]]></duration>");
                        if (StringUtils.isNotBlank(schedule.getDescription())) {
                            exportXML.append("<description><![CDATA[").append(schedule.getDescription()).append("]]></description>");
                        }
                        exportXML.append("<show><![CDATA[").append(schedule.getShowTitle()).append("]]></show>")
                                .append("<title><![CDATA[").append(schedule.getTitle()).append("]]></title>")
                                .append("<day><![CDATA[").append(schedule.getDayOfWeek()).append("]]></day>")
                                .append("</episode>");
                    }
                }
                writeFooter();
            }
            response.getOutputStream().print(exportXML.toString());
        } catch (Exception e) {
            log.error("Exception in TVScheduleExport: " + e.getMessage());
        }
    }

    /**
     * writes the header of the xml response
     *
     * @return
     */
    private void writeHeader() {
        exportXML.append("<episodes>");
    }

    /**
     * writes the footer of the xml response
     *
     * @return
     */
    private void writeFooter() {
        exportXML.append("</episodes>");
    }

    public List<TVScheduleItem> findSchedules(final Resource resource) {
        return findSchedules(resource, 1);
    }

    public List<TVScheduleItem> findSchedules(final Resource resource, int countDays) {
        if (countDays > 0) {
            List<TVScheduleItem> foundSchedules = new ArrayList<TVScheduleItem>();

            if (resourceResolver == null) {
                resourceResolver = resource.getResourceResolver();
            }

            Calendar calendar = Calendar.getInstance();
            String searchSchedulePath = schedulePathFromCalendar(calendar);

            int nDays = 0;
            while (nDays < countDays) {
                if (resourceResolver.resolve(searchSchedulePath) != null) {
                    List<Map<String, Object>> assetsForDay = findAssetsByPropertyValue(resource, searchSchedulePath,
                            AssetSlingResourceTypes.SCHEDULE.resourceType());
                    for (Map<String, Object> properties : assetsForDay) {
                        TVScheduleItem scheduleItem = getScheduleItemFromMap(properties, calendar);
                        foundSchedules.add(scheduleItem);
                    }
                }
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                searchSchedulePath = schedulePathFromCalendar(calendar);
                nDays++;
            }
            return foundSchedules;
        }
        return Collections.emptyList();
    }

    /**
     * Get ScheduleItem by properties map.
     *
     * @param scheduleValues Map with schedule item properties
     * @param scheduleDate
     * @return ScheduleItem bean with filled values
     */
    private TVScheduleItem getScheduleItemFromMap(Map<String, Object> scheduleValues, Calendar scheduleDate) {
        TVScheduleItem scheduleItem = new TVScheduleItem();
        //return empty item
        if (scheduleValues == null) return scheduleItem;

        if (scheduleValues.containsKey(PROPNAME_SCHEDULE_DATE)) {
            String scheduleDateProp = (String) scheduleValues.get(PROPNAME_SCHEDULE_DATE);
            String formattingDate = getScheduleStartTime(scheduleDateProp);
            scheduleItem.setStartDate(formattingDate);
        }
        if (scheduleDate != null) {
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);
            String weekDay = dayFormat.format(scheduleDate.getTime());
            scheduleItem.setDayOfWeek(weekDay);
        }
        if (scheduleValues.containsKey(PROPNAME_RELATED_EPISODE)) {
            String relatedEpisodePath = (String) scheduleValues.get(PROPNAME_RELATED_EPISODE);
            if (StringUtils.isNotBlank(relatedEpisodePath)) {
                Resource relatedEpisode = resourceResolver.getResource(relatedEpisodePath + "/" + JcrConstants.JCR_CONTENT);
                if (relatedEpisode != null) {
                    ValueMap episodeValues = ResourceUtil.getValueMap(relatedEpisode);
                    if (episodeValues.containsKey(PROPNAME_TITLE)) {
                        scheduleItem.setTitle((String) episodeValues.get(PROPNAME_TITLE));
                    }
                    if (episodeValues.containsKey(PROPNAME_DESCRIPTION)) {
                        scheduleItem.setDescription((String) episodeValues.get(PROPNAME_DESCRIPTION));
                    }
                    if (episodeValues.containsKey(PROPNAME_DURATION)) {
                        Integer duration_min = Integer.valueOf((String) episodeValues.get(PROPNAME_DURATION));
                        if (duration_min > 0) {
                            scheduleItem.setDuration(duration_min);
                        }
                    }
                }
                Resource metaEpisode = resourceResolver.getResource(relatedEpisodePath + "/" + NODE_META_PATH);
                int episodeId = 0;
                int num = 0;
                while (metaEpisode != null && episodeId < 1) {
                    ValueMap metaEpisodeValues = ResourceUtil.getValueMap(metaEpisode);
                    if (metaEpisodeValues.containsKey(PROPNAME_ID)) {
                        BigDecimal sourceId = (BigDecimal) metaEpisodeValues.get(PROPNAME_ID);
                        if (sourceId != null) {
                            episodeId = sourceId.intValue();
                        }
                    }
                    if (episodeId < 1) {
                        metaEpisode = resourceResolver.getResource(relatedEpisodePath + "/" + NODE_META_PATH + num);
                        num++;
                    }
                }
                scheduleItem.setId(episodeId);
            }
        }
        if (scheduleValues.containsKey(PROPNAME_RELATED_SHOW)) {
            String relatedShowPath = (String) scheduleValues.get(PROPNAME_RELATED_SHOW);
            if (StringUtils.isNotBlank(relatedShowPath)) {
                Resource relatedShow = resourceResolver.getResource(relatedShowPath + "/" + JcrConstants.JCR_CONTENT);
                if (relatedShow != null) {
                    ValueMap showValues = ResourceUtil.getValueMap(relatedShow);
                    if (showValues.containsKey(PROPNAME_TITLE)) {
                        scheduleItem.setShowTitle((String) showValues.get(PROPNAME_TITLE));
                    }
                }
            }
        }
        return scheduleItem;
    }

    /**
     * @param scheduleDateProp has 'yyyyMMddNN' format, where NN - time code
     * @return string with following format 'MM/dd/yyyy hh:mm:ss [PM|AM]'
     */
    private String getScheduleStartTime(String scheduleDateProp) {
        if (StringUtils.isNotBlank(scheduleDateProp) && scheduleDateProp.length() > 8) {
            String startDate = scheduleDateProp.substring(4, 6) + "/" + scheduleDateProp.substring(6, 8) + "/" + scheduleDateProp.substring(0, 4);
            Integer timeCode = Integer.parseInt(scheduleDateProp.substring(8));
            String scheduleTime = "";
            if (timeCode != null) {
                scheduleTime = TIME_CODE_MAP.get(timeCode);
            }
            return startDate + " " + scheduleTime;
        }
        return "";
    }

    /**
     * subroutine method for building strings, used by findAllFutureSchedules
     * returns path up to the day.
     *
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
                .append("/")
                .append(calendar.get(Calendar.YEAR))
                .append("/")
                .append(calendar.get(Calendar.MONTH) + 1)
                .append("/")
                .append(calendar.get(Calendar.DAY_OF_MONTH));
        return pathBuilder.toString();
    }


    /**
     * TODO: many sql query
     * Generic search utility that uses the CQ search API to locate nodes in the content repo using
     * the sling:resourceType of the desired asset and a string representing the value of an anonymous
     * property to identify some content within the properties of the node(s). Returns a List of Nodes.
     *
     * @param resource        Resource in hand
     * @param pathToAssetRoot String path to the root path of desired asset type
     * @param resourceType    String of sling:resourceType property to filter search by
     * @return List of Maps containing properties of found nodes
     */
    private List<Map<String, Object>> findAssetsByPropertyValue(final Resource resource,
                                                                final String pathToAssetRoot,
                                                                final String resourceType) {
        if (resource == null || pathToAssetRoot == null
                || resourceType == null) {
            return null;
        }
        QueryManager queryManager;
        try {
            queryManager = resource.adaptTo(Node.class).getSession().getWorkspace().getQueryManager();
            StringBuilder query = new StringBuilder();
            query.append("SELECT * FROM [cq:PageContent] AS s WHERE ISDESCENDANTNODE([")
                    .append(pathToAssetRoot)
                    .append("]) AND s.[")
                    .append(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName())
                    .append("] = '")
                    .append(resourceType)
                    .append("'");

            log.debug("QUERY " + query.toString());
            Query compiledQuery = queryManager.createQuery(query.toString(), Query.JCR_SQL2);
            NodeIterator nodeItr = compiledQuery.execute().getNodes();
            List<Map<String, Object>> assets = new ArrayList<Map<String, Object>>();
            while (nodeItr.hasNext()) {
                Node node = nodeItr.nextNode();
                Resource nodeResource = Functions.getResource(resource.getResourceResolver(), node.getPath());
                ValueMap nodeValues = ResourceUtil.getValueMap(nodeResource);
                assets.add(nodeValues);
            }
            return assets;
        } catch (RepositoryException re) {
            return null;
        } catch (NullPointerException npe) {
            return null;
        }
    }
}
