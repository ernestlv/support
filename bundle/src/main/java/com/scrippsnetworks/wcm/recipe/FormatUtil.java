package com.scrippsnetworks.wcm.recipe;

import com.scrippsnetworks.wcm.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jason Clark
 *         Date: 6/13/13
 */
public class FormatUtil {

    private static final Logger log = LoggerFactory.getLogger(FormatUtil.class);

    private FormatUtil() {}

    /**
     * Creates ISO 8601 micro-formatted timestamps representing duration.
     * @param minutes String which represents time in minutes.
     * @return String formatted ISO 8601 timestamp value.
     */
    public static String getIso8601Duration(final String minutes) {
        if (minutes != null && minutes.length() > 0) {
            Integer totalMinutes;
            try {
                totalMinutes = Integer.valueOf(minutes);
            } catch (NumberFormatException e) {
                return null;
            }
            Integer iso8601hours = totalMinutes >= 60 ? totalMinutes / 60 : 0;
            Integer iso8601minutes = totalMinutes - (iso8601hours * 60);
            return "PT" + iso8601hours + "H" + iso8601minutes + "M";
        } else {
            return null;
        }
    }

    /**
     * For formatting time durations (minutes) as display-friendly Strings.
     * @param minutes String representing a duration in minutes
     * @return String representing time duration in plain english
     */
    public static String getDisplayTime(final String minutes) {
        if (StringUtils.isNotBlank(minutes) && !minutes.equals("0")) {
            Integer totalMinutes;
            try {
                totalMinutes = Integer.valueOf(minutes);
                return getDisplayTime(totalMinutes);
            } catch (NumberFormatException e) {
                log.error("Caught NumberFormatException: {}", e);
            }
        }
        return "";
    }

    /** For formatting time durations (minutes) as display-friendly Strings.
     * @param minutes Integer total minutes to convert.
     * @return String formatted time.
     */
    public static String getDisplayTime(final Integer minutes) {
        if (minutes != null && minutes > 0) {
            Integer parsedHours = minutes >= 60 ? minutes / 60 : 0;
            Integer parsedMinutes = minutes - (parsedHours * 60);
            StringBuilder timeBuilder = new StringBuilder();
            if (parsedHours > 0) {
                timeBuilder
                        .append(parsedHours)
                        .append(" hr");
            }
            if (parsedMinutes > 0) {
                if (timeBuilder.length() > 0) {
                    timeBuilder.append(" ");
                }
                timeBuilder
                        .append(parsedMinutes)
                        .append(" min");
            }
            return timeBuilder.toString();
        }
        return "";
    }

    /** Format a String for truncation.
     * The actual truncation happens in a JavaScript library elsewhere.
     * This prepares the String by inserting markdown to denote
     * truncation boundaries. The length denotes how many characters
     * before truncation will occur. This takes into account word boundaries,
     * so a word will not be truncated in the middle.
     * Will return null if input was null.
     * @param text String of text to markdown.
     * @param length Integer maximum length before truncation.
     * @return Mangled String.
     */
    public static String addTruncationMarkdown(final String text, final Integer length) {
        StringBuilder resultsBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(text) && length != null && length > 0) {
            String scrubbedText = StringUtil.cleanToPlainText(text);
            if (scrubbedText.length() > length) {
                if (scrubbedText.matches(".*\\b.*")) {
                    Pattern truncate = Pattern.compile("^(.{" + length + "}([a-zA-Z0-9])*)\\b(.*)");
                    Matcher truncateMatcher = truncate.matcher(scrubbedText);
                    if (truncateMatcher.matches()) {
                        String firstPart = truncateMatcher.group(1);
                        String secondPart = truncateMatcher.group(3);
                        if (StringUtils.isNotBlank(firstPart)) {
                            resultsBuilder.append(firstPart);
                            if (StringUtils.isNotBlank(secondPart)) {
                                resultsBuilder
                                        .append("[")
                                        .append(secondPart)
                                        .append("]");
                            }
                        }
                    }
                }
            }
        }
        String results = resultsBuilder.toString();
        return StringUtils.isNotBlank(results) ? results : text ;
    }

}
