package com.scrippsnetworks.wcm.util;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * This Comparator is specifically to sort schedule assets using their sni:sortTitle property.
 *
 * The collection should be a Map of String, Object where one of those Strings is sni:sortTitle
 * and the Object is the raw value of that property (should be a String).
 *
 * At the time of writing the format of that property is:
 *  MMDDYYYY TIME CODE ##
 *
 * @author Jason Clark
 *         Date: 1/9/13
 */
public class ScheduleSortTitleComparator implements Comparator<Map<String, Object>> {

    private static final String SORT_TITLE_PATTERN = "([0-9]{8}) TIME CODE ([0-9]{1,2})";
    private static final String SIMPLE_DATE_FORMAT = "MMddyyyy HH:mm";
    private static final String SPACE = " ";

    //this data also exists in assets.DataUtil, but duped here for zero-padding
    private static final Map<Integer, String> TIME_CODE_MAP = new HashMap<Integer, String>() {
        {
            put(1, "06:30");
            put(2, "07:00");
            put(3, "07:30");
            put(4, "08:00");
            put(5, "08:30");
            put(6, "09:00");
            put(7, "09:30");
            put(8, "10:00");
            put(9, "10:30");
            put(10, "11:00");
            put(11, "11:30");
            put(12, "12:00");
            put(13, "12:30");
            put(14, "13:00");
            put(15, "13:30");
            put(16, "14:00");
            put(17, "14:30");
            put(18, "15:00");
            put(19, "15:30");
            put(20, "16:00");
            put(21, "16:30");
            put(22, "17:00");
            put(23, "17:30");
            put(24, "18:00");
            put(25, "18:30");
            put(26, "19:00");
            put(27, "19:30");
            put(28, "20:00");
            put(29, "20:30");
            put(30, "21:00");
            put(31, "21:30");
            put(32, "22:00");
            put(33, "22:30");
            put(34, "23:00");
            put(35, "23:30");
            put(36, "00:00");
            put(37, "00:30");
            put(38, "01:00");
            put(39, "01:30");
            put(40, "02:00");
            put(41, "02:30");
            put(42, "03:00");
            put(43, "03:30");
            put(44, "04:00");
            put(45, "04:30");
            put(46, "05:00");
            put(47, "05:30");
            put(48, "06:00");
        }
    };

    /**
     *
     * @param m1
     * @param m2
     * @return int
     */
    @Override
    public int compare(final Map<String, Object> m1, final Map<String, Object> m2) {
        if (!m1.containsKey(AssetPropertyNames.SNI_SORT_TITLE.propertyName())) {
            if (!m2.containsKey(AssetPropertyNames.SNI_SORT_TITLE.propertyName())) {
                return 0;
            } else {
                return Integer.MIN_VALUE;
            }
        } else {
            if (!m2.containsKey(AssetPropertyNames.SNI_SORT_TITLE.propertyName())) {
                return Integer.MAX_VALUE;
            } else {
                String t1 = m1.get(AssetPropertyNames.SNI_SORT_TITLE.propertyName()).toString();
                String t2 = m2.get(AssetPropertyNames.SNI_SORT_TITLE.propertyName()).toString();

                Calendar c1 = convertSortTitleToCalendar(t1);
                Calendar c2 = convertSortTitleToCalendar(t2);

                if (c1 == null || c2 == null) {
                    return 0;
                }

                return c1.compareTo(c2);
            }
        }
    }

    /**
     * Convert an sni:sortTitle from a Schedule asset into a Calendar
     * @param s String from sni:sortTitle
     * @return Calendar
     */
    public static Calendar convertSortTitleToCalendar(final String s) {
        Pattern p = Pattern.compile(SORT_TITLE_PATTERN);
        Matcher m = p.matcher(s);
        if (m.matches()) {
            String date = m.group(1);
            String timeCode = m.group(2);
            String formattedTimeStamp;
            Calendar c = Calendar.getInstance();
            TimeZone tz = TimeZone.getTimeZone("GMT-5:00");
            c.setTimeZone(tz);
            try {
                formattedTimeStamp = TIME_CODE_MAP.get(Integer.valueOf(timeCode));
                c.setTime(new SimpleDateFormat(SIMPLE_DATE_FORMAT)
                        .parse(date + SPACE + formattedTimeStamp));
            } catch (Exception e) {
                return null;
            }
            return c;
        } else {
            return null;
        }
    }

}
