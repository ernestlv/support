package com.scrippsnetworks.wcm.mobile.schedule.impl;

import com.scrippsnetworks.wcm.mobile.schedule.ScheduleDay;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class ScheduleDayImpl implements ScheduleDay{
    private static String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private Date date;
    private Calendar calendar;
    private Integer weekDayIndex;
    private TimeZone timeZone;

    public ScheduleDayImpl(Date date, TimeZone timeZone) {
        this.date = date;
        this.calendar = new GregorianCalendar();
        this.calendar.setTime(date);
        this.calendar.setTimeZone(timeZone);

    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public Calendar getCalendar() {
        return calendar;
    }

    @Override
    public int getWeekDayIndex() {
        if (weekDayIndex == null){
            weekDayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        }
        return weekDayIndex;
    }

    @Override
    public String getNameOfWeekDay(){
        return daysOfWeek[getWeekDayIndex()];
    }

    @Override
    public Date getPreviousDate(){
        return new Date(date.getTime() - 24 * 3600 * 1000);
    }

    @Override
    public Date getNextDate(){
        return new Date(date.getTime() + 24 * 3600 * 1000);
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }
}
