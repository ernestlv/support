package com.scrippsnetworks.wcm.components;

import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.asset.show.TimeSlot;
import com.scrippsnetworks.wcm.programdata.impl.ProgramDataQueryImpl;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import com.scrippsnetworks.wcm.AbstractComponent;
import com.scrippsnetworks.wcm.asset.show.Schedule;
import com.scrippsnetworks.wcm.asset.show.Schedule.TimeZoneEnum;


/**
 * Class to load the data for the Program Guide Application
 * 
 * @author mei-yichang, ranand 
 * 
 */
public class ProgramGuideApplication extends AbstractComponent {
	
	private final Logger log = LoggerFactory.getLogger(ProgramGuideApplication.class);

	private ResourceResolver resolver;

	private Calendar defaultDate;

    private Calendar startDate;

    private Calendar endDate;

    private int limit=1;
    
    private int days=1;

    private String[] searchPages;

    private String featuredShowsPath;

    private boolean includeNonPrimaryPeople;

    private boolean sortDesc=false;

	private TimeZoneEnum timeZone;

	private boolean weekly = false;
	
	private String feedType;

	private static final String WEEKLY_SELECTOR = "weekly";

	@Override
	public void doAction() throws Exception {

		resolver = getSlingRequest().getResourceResolver();
		String[] selectors = getSlingRequest().getRequestPathInfo()
				.getSelectors();

		defaultDate = Calendar.getInstance();
		timeZone = TimeZoneEnum.E;

		int index = 0;
		boolean foundDate = false;
		defaultDate.setTime(new Date());

		while (selectors != null && (!foundDate || !weekly)
				&& index < selectors.length) {
			String selector = selectors[index];
			if (selector.equals(WEEKLY_SELECTOR)) {
				weekly = true;
			}
			// selector for current date should be in the format of
			// YYYYM(M?)D(D?)T where T being the time zone code (E, P, C, M) to
			// determine the target date of the program guide, if no suffix is
			// found, the date will be set to the current date and eatern time
			// zone
			if (selector.matches("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}[E/C/P/M]{1}")) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
				Date date = sdf.parse(selector.substring(0,
						selector.length() - 1));
				defaultDate.setTime(date);
				timeZone = TimeZoneEnum.valueOf(selector.substring(selector
						.length() - 1));
				foundDate = true;
			}
			index++;
		}

		// start time is always 6:30 EASTERN
		defaultDate.setTimeZone(TimeZone.getTimeZone(TimeZoneEnum.E
				.getTimeZoneId()));
		defaultDate.set(Calendar.HOUR_OF_DAY, 6);
		defaultDate.set(Calendar.MINUTE, 30);
	}

    public List<TimeSlot> getTimeslots() {
        List<String> pages = null;
        if(searchPages!=null) {
            pages = Arrays.asList(searchPages);
        }
        if(feedType != null){
	        if(feedType.equals("future")){
	        	endDate = Calendar.getInstance();
	        	endDate.setTime(new Date());  
	        	endDate.add(Calendar.DATE, days); 
	        }else if(feedType.equals("past")){
	        	startDate = Calendar.getInstance();
	        	startDate.setTime(new Date());  
	        	startDate.add(Calendar.DATE, days);
	        }
        }
        return new ProgramDataQueryImpl().getTimeslots((SlingHttpServletRequest) getPageContext().getRequest(),startDate,endDate,pages,includeNonPrimaryPeople,limit, sortDesc);
    }

    public void setStartDate(Calendar date) {
        this.startDate = date;
    }

    public void setEndDate(Calendar date) {
        this.endDate = date;
    }

    public void setFeaturedShowsPath(String path) {
        this.featuredShowsPath = path;
    }

    public void setSearchPages(String[] pages) {
        this.searchPages = pages;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setSortDesc(boolean desc) {
        this.sortDesc = desc;
    }

    public void setIncludeNonPrimaryPeople(boolean people) {
        this.includeNonPrimaryPeople = people;
    }

    public void setFeedType(String type) {
        this.feedType = type;
    }
    
    public void setDays(int days) {
        this.days = days;
    }
    
	public List<Schedule> getSchedules() {
		List<Schedule> schedules = new ArrayList<Schedule>();

		Calendar starting = Calendar.getInstance();
		// return daily program schedule by default
		if (weekly) {
			// return schedules for all the days in the week of the target date
			// for weekly program guide
			for (Iterator<Calendar> iter = getListOfDaysInWeek().iterator(); iter
					.hasNext();) {
				Calendar next = iter.next();
				Resource scheduleResource = getScheduleNode(next);
				if (scheduleResource != null) {
					schedules.add(new Schedule(scheduleResource, next, 0));
				}
			}
		} else {
			Resource scheduleResource = getScheduleNode(defaultDate);
			if (scheduleResource != null) {
				schedules.add(new Schedule(scheduleResource, defaultDate, 1));
			}
		}
        return schedules;
	}

	/**
	 * Get Schedule Node by Date
	 * 
	 * @param cal
	 * @return resource of the schedule node
	 */
	private Resource getScheduleNode(Calendar cal) {
		// the schedule node will be in the path
		// /[root of schedule root path]/yyyy/m/d
		if (cal != null) {
			String path = Schedule.SCHEDULE_ROOT + "/" + cal.get(Calendar.YEAR)
					+ "/" + (cal.get(Calendar.MONTH) + 1) + "/"
					+ cal.get(Calendar.DAY_OF_MONTH);
		
			return resolver.getResource(path);
		} else {
			return null;
		}
	}

	public TimeZoneEnum getTimeZone() {
		return timeZone;
	}

	/**
	 * Get Schedule Node by Month
	 * 
	 * @param cal
	 * @return resource of the schedule month node
	 */
	private Resource getScheduleMonthNode(Calendar cal) {
		// the schedule node will be in the path
		// /[root of schedule root path]/yyyy/m/d
		if (cal != null) {
			String path = Schedule.SCHEDULE_ROOT + "/" + cal.get(Calendar.YEAR)
					+ "/" + (cal.get(Calendar.MONTH) + 1);
		
			return resolver.getResource(path);
		} else {
			return null;
		}
	}
	
	/**
	 * Get last available day having the program data
	 * 
	 * @return Calendar
	 */
	public Calendar getCalendarEndDate() {
		Calendar cal = null;
		Resource monthResource = null;
		for (int i=2;i>0;i--){
			cal = (Calendar) defaultDate.clone();
			cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) +i);
			monthResource = getScheduleMonthNode(cal);
			if(monthResource != null){
				Iterator<Resource> iter = resolver.listChildren(monthResource);
				int lastDay = getLastAvailableDayNumber(iter);
				cal.set(Calendar.DAY_OF_MONTH, lastDay);
				break;
			}else{
				continue;
			}
		}	
		if(monthResource ==  null){
			cal = (Calendar) defaultDate.clone();
			monthResource = getScheduleMonthNode(cal);
			if(monthResource != null){
				Iterator<Resource> iter = resolver.listChildren(monthResource);
				int lastDay = getLastAvailableDayNumber(iter);
				cal.set(Calendar.DAY_OF_MONTH, lastDay);
			}
		}
		return cal;
	}	
	
	/**
	 * Get last available day having the program data
	 * 
	 * @return Calendar
	 */
	private int getLastAvailableDayNumber(Iterator<Resource> iter) {
		List<String> allDays = new ArrayList<String>();
		int size=0;
		if(iter != null){
			while (iter.hasNext()) {
	            Resource currResource = iter.next();
	            allDays.add(currResource.getName());
			}
			size = allDays.size();
		}
		return size;
	}
	
	/**
	 * Get Calendar start date
	 * 
	 * @return Calendar for last 28 days only
	 */
	public Calendar getCalendarStartDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) -28);
		return cal;
	}		
	
	/**
	 * Get the list of the days in a week
	 * 
	 * @return List<Calendar>
	 */
	public List<Calendar> getListOfDaysInWeek() {
		Calendar cal = (Calendar) defaultDate.clone();
		List<Calendar> list = new ArrayList<Calendar>();
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		// set calendar to first day of week;
		if (dayOfWeek > 1) {
			cal.add(Calendar.DAY_OF_MONTH, (dayOfWeek - 1) * (-1));
		}

		list.add((Calendar) cal.clone());
		// then iterate through week
		for (int i = 0; i < 6; i++) {
			cal.add(Calendar.DAY_OF_MONTH, 1);
			list.add((Calendar) cal.clone());
		}
		return list;
	}

	/**
	 * Get previous day
	 * 
	 * @return Calendar
	 */
	public Calendar getPreviousDay() {
		Calendar cal = (Calendar) defaultDate.clone();
		//cal.set(Calendar.DAY_OF_WEEK, cal.get(Calendar.DAY_OF_WEEK) -1);
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) -1);
		return cal;
	}	
	
	/**
	 * Get next day
	 * 
	 * @return Calendar
	 */
	public Calendar getNextDay() {
		Calendar cal = (Calendar) defaultDate.clone();
		//cal.set(Calendar.DAY_OF_WEEK, cal.get(Calendar.DAY_OF_WEEK) +1);
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) +1);
		return cal;
	}	

	/**
	 * Get Last Day of the week
	 * 
	 * @return Calendar
	 */
	public Calendar getLastDayOfTheWeek() {
		Calendar cal  = Calendar.getInstance();
		int currentDay = cal.get(Calendar.DAY_OF_WEEK);
		int leftDays= Calendar.SATURDAY - currentDay;
		cal.add(Calendar.DATE, leftDays);
		return cal;
	}	
	
	/**
	 * Get First Day of the week
	 * 
	 * @return Calendar
	 */
	public Calendar getFirstDayOfTheWeek() {
		Calendar cal  = Calendar.getInstance();
		int currentDay = cal.get(Calendar.DAY_OF_WEEK);
		int oldDays= currentDay - Calendar.SUNDAY;
		cal.add(Calendar.DATE, - oldDays);
		return cal;
	}	
	
	
	public List<Date> getListOfTimeSlots() {
		Calendar cal = (Calendar) defaultDate.clone();
		List<Date> list = new ArrayList<Date>();
		list.add(cal.getTime());
		for (int i = 0; i < Schedule.DEFAULT_TIMESLOTS; i++) {
			cal.add(Calendar.MINUTE, 30);
			Date tempDate = cal.getTime();
			list.add(tempDate);
		}
		return list;
	}

	public Calendar getDefaultDate() {
		return defaultDate;
	}

	public int getOnAir() {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(TimeZoneEnum.E
				.getTimeZoneId()));
		cal.setTime(new Date());

		/* assuming  a 30 mins is 1, the start time is 13 (6 * 2 + 1) for 6:30AM */
		int start = 13; 
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int num = hour * 2;
		if (cal.get(Calendar.MINUTE) > 30) {
			num++;
		}

		num = num - start;
		if (num < 0) {
			num += 48;
		}
		return num + 1;
	}

    private Calendar TimeslotStartFromCurrentTime(Calendar date) {
        Calendar returnDate = (Calendar) date.clone();
        int currentMinute = returnDate.get(Calendar.HOUR_OF_DAY);
        if(currentMinute<30) {
            returnDate.set(Calendar.MINUTE,0);
        }else {
            returnDate.set(Calendar.MINUTE,30);
        }
        return returnDate;
    }
}
