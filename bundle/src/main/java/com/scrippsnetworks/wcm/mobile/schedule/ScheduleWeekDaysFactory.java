package com.scrippsnetworks.wcm.mobile.schedule;

import com.scrippsnetworks.wcm.asset.show.Schedule;
import com.scrippsnetworks.wcm.mobile.schedule.impl.WeekDaysImpl;
import org.apache.sling.api.SlingHttpServletRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

public class ScheduleWeekDaysFactory {
    SlingHttpServletRequest slingRequest;

    public WeekDays build(){
        Schedule.TimeZoneEnum timeZone  = Schedule.TimeZoneEnum.E;
        Date date = new Date();

        if (slingRequest == null){
            return new WeekDaysImpl(date, timeZone);
        }

        String[] selectors = slingRequest.getRequestPathInfo().getSelectors();

        for (String selector: selectors) {
            if (selector.matches("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}[E/C/P/M]{1}")) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
                timeZone = Schedule.TimeZoneEnum.valueOf(selector.substring(selector.length() - 1));
                sdf.setTimeZone(TimeZone.getTimeZone(timeZone.getTimeZoneId()));
                try {
                    date = sdf.parse(selector.substring(0, selector.length() - 1));
                } catch (ParseException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                break;
            }
        }

        return new WeekDaysImpl(date, timeZone);
    }
    public ScheduleWeekDaysFactory withCurrentPage(SlingHttpServletRequest slingRequest){
        this.slingRequest = slingRequest;
        return this;
    }
}
