package com.scrippsnetworks.wcm.mobile.schedule.export;

import org.apache.sling.api.resource.Resource;

import java.util.List;

public interface TVScheduleExport {

    /**
     * Using the current time as a starting point, find all schedule nodes from today forward
     * Stuff their properties into a Map and return as a List
     *
     * @param resource Sling Resource in hand
     * @return a List of schedules items
     */
    List<TVScheduleItem> findSchedules(final Resource resource);
    /**
     * Using the current time as a starting point, find all schedule nodes from today forward
     * Stuff their properties into a Map and return as a List
     *
     * @param resource Sling Resource in hand
     * @param countDays count days for schedule export
     * @return a List of schedules items
     */
    List<TVScheduleItem> findSchedules(final Resource resource, int countDays);
}
