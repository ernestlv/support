package com.scrippsnetworks.wcm.calendar;

import javax.jcr.Node;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Anatoli_Zapolski
 * Date: 9/6/13
 * Time: 11:25 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Calendar {
    public List<CalendarSlot> getCalendarSlots();
    public List<CalendarSlot> getCalendarSlotsFromSunday();
    public int getYear();
    public int getMonth();
    public int getCountOfDays();
    public Node getCurNode();
    public String getNextCalendarPath();
    public String getPrevCalendarPath();
    public String getMonthName();
    public List<String> getWeeksDays();

}
