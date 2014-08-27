package com.scrippsnetworks.wcm.asset.show;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import org.apache.commons.lang.exception.ExceptionUtils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;

/**
 * Class representing the Schedule asset resource
 * 
 * @author mei-yichang
 * 
 */

public class Schedule {

	/**
	 * Enum representing the Time Zone
	 * 
	 * @author mei-yichang
	 * 
	 */
	public enum TimeZoneEnum {
		E("US/Eastern", "Eastern Time"), C("US/Central", "Central Time"), M("US/Mountain", "Mountain Time"), 
		P("US/Pacific", "Pacific Time");
		

		/* the ID used for setting the timezone in java.util.TimeZone */
		private String timeZoneId;

		/* the title of the time zone used in the drop down menu */
		private String timeZoneName;

		private TimeZoneEnum(String id, String name) {
			this.timeZoneId = id;
			this.timeZoneName = name;
		}

		public String getTimeZoneId() {
			return timeZoneId;
		}

		public String getTimeZoneName() {
			return timeZoneName;
		}
	}

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private String excludeNodes = "crxdao:meta";
	
	public static final String SCHEDULE_ROOT = AbstractResourceObject.SNI_ASSET_PREFIX
			+ "/schedules/food";

	public static final int DEFAULT_TIMESLOTS = 48;

	ArrayList<TimeSlot> entries;
	Calendar calendar;

	public Schedule(Resource resource, Calendar calendar1, int depts) {

		calendar = Calendar.getInstance();

		entries = new ArrayList<TimeSlot>();
		if (resource != null) {
			if (calendar1 != null) {
				calendar = (Calendar) calendar1.clone();
			} else {
				loadCalendar(resource);
			}

			Iterator<Resource> iter = resource.listChildren(); 
			while (iter.hasNext()) {
				Resource currResource = iter.next();
				
				if(currResource.getName() != null && !currResource.getName().equals(excludeNodes)){
					entries.add(new TimeSlot(currResource, calendar, depts));
				}
			}

			// time slots are to be ordered by index
			Collections.sort(entries, new Comparator<TimeSlot>() {
				public int compare(TimeSlot o1, TimeSlot o2) {
					return o1.index - o2.index;
				}
			});
		}
	}

	/**
	 * Set the calendar time to be 6:30 EASTERN
	 * 
	 * @param resource
	 */
	private void loadCalendar(Resource resource) {
		String dateStr = resource.getPath().substring(SCHEDULE_ROOT.length());

		SimpleDateFormat sdf = new SimpleDateFormat("/yyyy/M/d");

		Date date;

		try {
			date = sdf.parse(dateStr);
			calendar.setTime(date);
			calendar.setTimeZone(TimeZone.getTimeZone(TimeZoneEnum.E
					.getTimeZoneId()));
			calendar.set(Calendar.HOUR_OF_DAY, 6);
			calendar.set(Calendar.MINUTE, 30);
		} catch (ParseException e) {
		    log.error(ExceptionUtils.getFullStackTrace(e));
		}
	}



	public ArrayList<TimeSlot> getEntries() {
		return entries;
	}

	public Calendar getCalendar() {
		return calendar;
	}
}
