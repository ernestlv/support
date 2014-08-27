package com.scrippsnetworks.wcm.mobile.schedule;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public interface ScheduleDay {
    public Date getDate();

    public Calendar getCalendar();


    public int getWeekDayIndex();

    public String getNameOfWeekDay();

    public Date getPreviousDate();

    public Date getNextDate();

    public TimeZone getTimeZone();
}
