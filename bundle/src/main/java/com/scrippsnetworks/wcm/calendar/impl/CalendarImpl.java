package com.scrippsnetworks.wcm.calendar.impl;

import com.day.cq.wcm.api.Page;
import com.day.text.ISO9075;
import com.scrippsnetworks.wcm.calendar.Calendar;
import com.scrippsnetworks.wcm.calendar.CalendarSlot;
import com.scrippsnetworks.wcm.calendar.CalendarSlotFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.taglib.Functions;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.text.DateFormatSymbols;
import java.util.*;

import static org.apache.sling.jcr.resource.JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;
import static org.apache.jackrabbit.JcrConstants.NT_UNSTRUCTURED;


/**
 * Created with IntelliJ IDEA.
 * User: Anatoli_Zapolski
 * Date: 9/6/13
 * Time: 11:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class CalendarImpl implements Calendar {
    private final static Logger log = LoggerFactory.getLogger(CalendarImpl.class);

    private final static String YEAR_PROP = "year";
    private final static String MONTH_PROP = "month";
    private final static String CALENDAR_SLOT_TYPE = "sni-food/components/modules/calendar/calendar-slot";
    private final static String CALENDAR_RESOURCE_TYPE = "sni-food/components/modules/calendar";
    private final static int MAX_DAY_COUNT = 31;
    private final static String CALENDAR_SLOT_NAME = "slot_";
    private final static int DEFAULT_YEAR = 2014;
    private final static int DEFAULT_MONTH = 0;

    private Node curNode;
    private Resource resource;
    private List<CalendarSlot> calendarSlots;
    private List<CalendarSlot> calendarSlotsFromSunday;
    private Integer year;
    private Integer month;
    private Integer countOfDays;
    private String monthName;
    private List weeksDays;

    private String nextCalendarPath;
    private String prevCalendarPath;


    public CalendarImpl(Resource resource) {
        this.resource = resource;
        this.curNode = resource.adaptTo(Node.class);
        initPrevNextNavigation();
    }

    private List<CalendarSlot> getCurCalendarSlots() {
        List<CalendarSlot> curCalendarSlots = new ArrayList<CalendarSlot>();
        if (curNode == null) {
            return curCalendarSlots;
        }

        try {
            //get calendar slot nodes and add they to calendarSlotPaths
            NodeIterator nodes = curNode.getNodes();
            while (nodes.hasNext()) {
                Node node = nodes.nextNode();
                if (!node.hasProperty(SLING_RESOURCE_TYPE_PROPERTY)) {
                    continue;
                }

                String curNodeType = node.getProperty(SLING_RESOURCE_TYPE_PROPERTY).getString();
                if (!CALENDAR_SLOT_TYPE.equals(curNodeType)) {
                    continue;
                }

                CalendarSlot newSlot = new CalendarSlotFactory().withNode(node).withResourceResolver(resource.getResourceResolver()).withDayIndex(curCalendarSlots.size() + 1).build();
                curCalendarSlots.add(newSlot);
                if (curCalendarSlots.size() >= MAX_DAY_COUNT) {
                    break;
                }
            }
        } catch (RepositoryException re) {
            log.error("RepositoryException caught: ", re);
            curCalendarSlots = new ArrayList<CalendarSlot>();
        }
        return curCalendarSlots;
    }

    @Override
    public List<CalendarSlot> getCalendarSlots() {
        if (curNode == null) {
            return null;
        }
        if (calendarSlots != null) {
            return calendarSlots;
        }
        try {
            calendarSlots = new ArrayList<CalendarSlot>(getCurCalendarSlots());

            //if count of slots in calendar less than MAX_DAY_COUNT, then created new nodes
            if (calendarSlots.size() < MAX_DAY_COUNT) {
                try {
                    Integer nodeNumber = 1;

                    while (calendarSlots.size() < MAX_DAY_COUNT) {
                        while (curNode.hasNode(CALENDAR_SLOT_NAME + nodeNumber.toString())) {
                            nodeNumber++;
                        }
                        Node node = curNode.addNode(CALENDAR_SLOT_NAME + nodeNumber.toString(), NT_UNSTRUCTURED);
                        nodeNumber++;
                        node.setProperty(SLING_RESOURCE_TYPE_PROPERTY, CALENDAR_SLOT_TYPE);
                        CalendarSlot newSlot = new CalendarSlotFactory().withNode(node).withResourceResolver(resource.getResourceResolver()).withDayIndex(calendarSlots.size() + 1).build();
                        calendarSlots.add(newSlot);
                    }
                } catch (RepositoryException re) {
                    log.error("RepositoryException caught: ", re);
                    curNode.getSession().refresh(false);
                }

                curNode.getSession().save();
            }


            calendarSlots = calendarSlots.subList(0, getCountOfDays());


        } catch (RepositoryException re) {
            log.error("RepositoryException caught: ", re);
            calendarSlots = new ArrayList<CalendarSlot>();
        }

        return calendarSlots;
    }


    @Override
    public List<CalendarSlot> getCalendarSlotsFromSunday() {
        if (calendarSlotsFromSunday != null) {
            return calendarSlotsFromSunday;
        }

        if (getCurCalendarSlots().size() != MAX_DAY_COUNT) {
            return null;
        }

        calendarSlotsFromSunday = new ArrayList<CalendarSlot>();
        java.util.Calendar cal = new GregorianCalendar(getYear(), getMonth(), 1);

        while (cal.get(java.util.Calendar.DAY_OF_WEEK) != java.util.Calendar.SUNDAY) {
            cal.add(java.util.Calendar.DAY_OF_MONTH, -1);
            calendarSlotsFromSunday.add(null);
        }

        calendarSlotsFromSunday.addAll(getCurCalendarSlots().subList(0, getCountOfDays()));

        cal = new GregorianCalendar(getYear(), getMonth(), countOfDays);

        while (cal.get(java.util.Calendar.DAY_OF_WEEK) != java.util.Calendar.SATURDAY) {
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
            calendarSlotsFromSunday.add(null);
        }

        return calendarSlotsFromSunday;
    }

    @Override
    public int getYear() {
        if (year != null) {
            return year;
        }

        try {
            if (curNode.hasProperty(YEAR_PROP)) {
                year = Integer.parseInt(curNode.getProperty(YEAR_PROP).getString());
            } else {
                year = DEFAULT_YEAR;
            }
        } catch (RepositoryException re) {
            log.error("RepositoryException caught: ", re);
            year = DEFAULT_YEAR;
        }

        return year;
    }

    @Override
    public int getMonth() {
        if (month != null) {
            return month;
        }

        try {
            if (curNode.hasProperty(MONTH_PROP)) {
                month = Integer.parseInt(curNode.getProperty(MONTH_PROP).getString());
            } else {
                month = DEFAULT_MONTH;
            }
        } catch (RepositoryException re) {
            log.error("RepositoryException caught: ", re);
            month = DEFAULT_MONTH;
        }

        return month;
    }

    @Override
    public int getCountOfDays() {
        if (countOfDays != null) {
            return countOfDays;
        }
        java.util.Calendar cal = new GregorianCalendar(getYear(), getMonth(), 1);
        countOfDays = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
        return countOfDays;
    }

    @Override
    public Node getCurNode() {
        return curNode;
    }

    @Override
    public String getNextCalendarPath() {

        return nextCalendarPath;
    }

    @Override
    public String getPrevCalendarPath() {
        return prevCalendarPath;
    }

    @Override
    public String getMonthName() {
        if (monthName != null) {
            return monthName;
        }

        DateFormatSymbols dfs = new DateFormatSymbols();
        if (month >= 0 && month < 12) {
            monthName = dfs.getMonths()[month];
        } else {
            monthName = "wrong month";
        }

        return monthName;
    }

    @Override
    public List<String> getWeeksDays() {
        if (weeksDays != null) {
            return weeksDays;
        }

        weeksDays = new ArrayList<String>();

        java.util.Calendar cal = new GregorianCalendar(getYear(), getMonth(), 1);

        int firstSunday;
        while (cal.get(java.util.Calendar.DAY_OF_WEEK) != java.util.Calendar.SUNDAY) {
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
        firstSunday = cal.get(java.util.Calendar.DAY_OF_MONTH);

        if (firstSunday == 2) {
            weeksDays.add("1");
        } else if (firstSunday != 1) {
            weeksDays.add("1 - " + Integer.toString(firstSunday - 1));
        }

        for (int sundayInd = firstSunday; sundayInd <= getCountOfDays(); sundayInd += 7) {
            if (sundayInd == getCountOfDays()) {
                weeksDays.add(Integer.toString(sundayInd));
            } else {
                weeksDays.add(Integer.toString(sundayInd) + " - " + Integer.toString(Math.min(getCountOfDays(), sundayInd + 6)));
            }
        }


        return weeksDays;
    }

    private void initPrevNextNavigation() {
        if (resource != null) {
            SniPage currentPage = Functions.getContainingPage(resource);
            if (currentPage != null) {
                Page calendarRootPage = currentPage.getParent();
                try {
                    QueryManager queryManager;
                    queryManager = curNode.getSession().getWorkspace().getQueryManager();
                    StringBuilder query = new StringBuilder();
                    query
                            .append("/jcr:root")
                            .append(ISO9075.encodePath(calendarRootPage.getPath()))
                            .append("/element(*,cq:Page)/jcr:content//*[@sling:resourceType='")
                            .append(CALENDAR_RESOURCE_TYPE)
                            .append("']  order by @year, @month");

                    Query compiledQuery = queryManager.createQuery(query.toString(), Query.XPATH);
                    //calendar iterator should have right ordering
                    NodeIterator nodeItr = compiledQuery.execute().getNodes();
                    Node node = null;

                    Node previousCalendarNode = null;
                    Node currentCalendarNode = null;
                    Node nextCalendarNode = null;

                    while (nodeItr.hasNext()) {
                        //save previous calendar
                        if (node != null) {
                            previousCalendarNode = node;
                        }
                        node = nodeItr.nextNode();
                        //get next calendar
                        if (node.getPath().equals(curNode.getPath())) {
                            currentCalendarNode = node;
                            if (nodeItr.hasNext()) {
                                node = nodeItr.nextNode();
                                nextCalendarNode = node;
                                break;
                            }
                        }
                    }
                    java.util.Calendar previousCalendar = getCalendarfromNode(previousCalendarNode);
                    java.util.Calendar currentCalendar = getCalendarfromNode(currentCalendarNode);
                    java.util.Calendar nextCalendar = getCalendarfromNode(nextCalendarNode);

                    //check if previous month is valid (goes right before current month)
                    if (previousCalendar != null && currentCalendar != null) {
                        previousCalendar.add(java.util.Calendar.MONTH, 1);
                        if (previousCalendar.get(java.util.Calendar.MONTH) == currentCalendar.get(java.util.Calendar.MONTH) &&
                                previousCalendar.get(java.util.Calendar.YEAR) == currentCalendar.get(java.util.Calendar.YEAR)) {
                            prevCalendarPath = Functions.getBasePath(previousCalendarNode.getPath());
                        }
                    }

                    //check if next month is valid (goes right after current month)
                    if (nextCalendar != null && currentCalendar != null) {
                        nextCalendar.add(java.util.Calendar.MONTH, -1);
                        if (nextCalendar.get(java.util.Calendar.MONTH) == currentCalendar.get(java.util.Calendar.MONTH) &&
                                nextCalendar.get(java.util.Calendar.YEAR) == currentCalendar.get(java.util.Calendar.YEAR)) {
                            nextCalendarPath = Functions.getBasePath(nextCalendarNode.getPath());
                        }
                    }

                } catch (RepositoryException re) {
                    log.error("Exception during Next/Prev Links initialization", re);
                }
            }
        }
    }

    private java.util.Calendar getCalendarfromNode(Node node) throws RepositoryException {
        if (node == null) {
            return null;
        }

        java.util.Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        calendar.setTimeInMillis(0);

        if (node.hasProperty(MONTH_PROP)) {
            Long month = node.getProperty(MONTH_PROP).getLong();
            calendar.set(java.util.Calendar.MONTH, month.intValue());
        }

        if (node.hasProperty(MONTH_PROP)) {
            Long year = node.getProperty(YEAR_PROP).getLong();
            calendar.set(java.util.Calendar.YEAR, year.intValue());
        }

        return calendar;
    }
}