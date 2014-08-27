package com.scrippsnetworks.wcm.mobile.schedule.impl;

import com.scrippsnetworks.wcm.asset.show.Schedule;
import com.scrippsnetworks.wcm.mobile.schedule.ScheduleDay;
import com.scrippsnetworks.wcm.mobile.schedule.WeekDays;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

public class WeekDaysImpl implements WeekDays{
    private List<ScheduleDay> weekDays;
    private Schedule.TimeZoneEnum timeZone;
    private int selected = 0;

    public WeekDaysImpl(Date date, Schedule.TimeZoneEnum timeZone) {
        TimeZone curTimeZone = TimeZone.getTimeZone(timeZone.getTimeZoneId());
        this.timeZone = timeZone;
        weekDays = new LinkedList<ScheduleDay>();
        ScheduleDay scheduleDay = new ScheduleDayImpl(date, curTimeZone);
        weekDays.add(scheduleDay);

        for (int i = 0; i < 7; i++){
            weekDays.add(0, new ScheduleDayImpl(weekDays.get(0).getPreviousDate(), curTimeZone));
            selected++;
            weekDays.add(new ScheduleDayImpl(weekDays.get(weekDays.size() - 1).getNextDate(), curTimeZone));
        }
    }

    @Override
    public List<ScheduleDay> getWeekDays() {
        return weekDays;
    }

    @Override
    public Schedule.TimeZoneEnum getTimeZone() {
        return timeZone;
    }

    @Override
    public int getSelected() {
        return selected;
    }
}
