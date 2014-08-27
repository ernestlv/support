package com.scrippsnetworks.wcm.mobile.schedule;

import com.scrippsnetworks.wcm.asset.show.Schedule;

import java.util.List;

public interface WeekDays {
    public List<ScheduleDay> getWeekDays();
    public Schedule.TimeZoneEnum getTimeZone();
    public int getSelected();
}
