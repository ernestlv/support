package com.scrippsnetworks.wcm.fnr.sitesearch;

import org.apache.commons.lang.time.DateUtils;

import java.util.Date;

public enum AirDateKey {
    nextweek("Next 7 days", 0L, 7 * DateUtils.MILLIS_PER_DAY),
    lastday("Last 24 hours", -1L * DateUtils.MILLIS_PER_DAY, 0L),
    lastweek("Last 7 days", -7L * DateUtils.MILLIS_PER_DAY, 0L),
    lasttwoweeks("Last 14 days", -14L * DateUtils.MILLIS_PER_DAY, 0L),
    older("Last 30 days", -30L * DateUtils.MILLIS_PER_DAY, 0L);

    String label;
    Long fromDelta;
    Long toDelta;

    AirDateKey(String label, Long fromDelta, Long toDelta) {
        this.label = label;
        this.fromDelta = fromDelta;
        this.toDelta = toDelta;
    }

    public String getLabel() {
        return label;
    }

    /** Returns the start for this range and the given time as a long value since the epoch.
     *
     * @param now Time in milliseconds since the epoch.
     * @return Long Range start in seconds since the epoch for the given current time in milliseconds since the epoch.
     */
    public Long getStart(long now) {
        if (fromDelta == null) {
            return null;
        }

        return (now + fromDelta) / 1000;
    }

    /** Returns the end for this range and the given time as a long value since the epoch.
     *
     * @param now
     * @return Long Range start in seconds since the epoch for the given current time in milliseconds since the epoch.
     */
    public Long getEnd(long now) {
        if (toDelta == null) {
            return null;
        }

        return (now + toDelta) / 1000;
    }
}
