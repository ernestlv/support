package com.scrippsnetworks.wcm.util;

/**
 * @author Jason Clark
 *         Date: 12/3/12
 */
public enum MonthNames {
    JANUARY ("January"),
    FEBRUARY ("February"),
    MARCH ("March"),
    APRIL ("April"),
    MAY ("May"),
    JUNE ("June"),
    JULY ("July"),
    AUGUST ("August"),
    SEPTEMBER ("September"),
    OCTOBER ("October"),
    NOVEMBER ("November"),
    DECEMBER ("December");

    private String monthName;

    private MonthNames(final String month) { this.monthName = month; }

    public String monthName() { return this.monthName; }
}
