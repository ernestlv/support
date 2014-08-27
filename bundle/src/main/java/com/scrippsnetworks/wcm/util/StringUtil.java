package com.scrippsnetworks.wcm.util;

import java.util.HashMap;
import java.lang.StringBuilder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Contains utility methods for massaging text data.
 *
 */
public class StringUtil {

    private static final String EMPTY = "";
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static Logger logger = LoggerFactory.getLogger(StringUtil.class);

    /* Because I love you, maintainer. */

    /** Pattern for matching decimal and hexidecimal codes to match. */
    protected static final Pattern windows1252CharCodeEntityPattern = Pattern.compile("&#"
            + "(x{0,1}0{0,3}"
            + "(?:"
                + "(?<=#x0{0,3})(?:[0-9a-fA-F]|1[0-9a-fA-F]|8[0-9a-fA-F]|9[0-9a-fA-F])"
                + "|"
                + "(?<=#0{0,3})(?:[0-9]|[1-2][0-9]|3[0-1]|12[7-9]|1[3-4][0-9]|15[0-9])"
            + ")"
            + ");");

    /** Map of character code entities to character literals. */
    //public static final HashMap<String, Character> map = new HashMap<String, Character>();
    public static final HashMap<Integer, Character> map = new HashMap<Integer, Character>();
    static {
        map.put(128, '\u20ac');
        // 129 no character defined
        map.put(130, '\u201a');
        map.put(131, '\u0192');
        map.put(132, '\u201e');
        map.put(133, '\u2026');
        map.put(134, '\u2020');
        map.put(135, '\u2021');
        map.put(136, '\u02c6');
        map.put(137, '\u2030');
        map.put(138, '\u0160');
        map.put(139, '\u2039');
        map.put(140, '\u0152');
        // 141 no character defined
        map.put(142, '\u017d');
        // 143 no character defined
        // 144 no character defined
        map.put(145, '\u2018');
        map.put(146, '\u2019');
        map.put(147, '\u201c');
        map.put(148, '\u201d');
        map.put(149, '\u2012');
        map.put(150, '\u2013');
        map.put(151, '\u2014');
        map.put(152, '\u02dc');
        map.put(153, '\u2122');
        map.put(154, '\u0161');
        map.put(155, '\u203a');
        map.put(156, '\u0153');
        // 157 no character defined
        map.put(158, '\u017e');
        map.put(159, '\u0178');
    }

    /** Replaces character code entities for Windows-1252 to literal characters. */
    public static String replaceWindows1252CharacterCodeEntities(String str) {
        StringBuilder sb = new StringBuilder();
        Matcher m = windows1252CharCodeEntityPattern.matcher(str);

        int i = 0;

        while (m.find()) {
            sb.append(str.substring(i, m.start()));

            String entStr = m.group(1);
            Integer lookup = null;

            // NumberFormatException can technically be thrown, but the regex controls input values.
            try {
                if (entStr.charAt(0) == 'x') {
                    lookup = Integer.parseInt(entStr.substring(1), 16);
                } else {
                    lookup = Integer.parseInt(entStr);
                }

                Character u = map.get(lookup);
                if (u != null) {
                    sb.append(u.toString());
                }
            } catch (NumberFormatException e) {
                // If we don't know what this is, just skip it and strip it.
                logger.error("error parsing number", e);
            }
            i = m.end();
        }
        sb.append(str.substring(i));
   
        return sb.toString();
    }

    /** Returns string with all markup stripped and all entities resolved to literals. */
    public static String cleanToPlainText(String str) {
        return cleanText(str, false);
    }

    /** Returns string with all markup stripped and all entities resolved to literals except the XML specials. */
    public static String cleanToEscapedText(String str) {
        return cleanText(str, true);
    }

    /** Returns a string with all markup stripped, and all entities resolved except optionally the XML special entities.
     *
     * If escapeXml is true, the returned string is suitable for treatment as an HTML string, it just happens not to have any
     * markup in it.
     */
    public static String cleanText(String str, boolean escapeXml) {
        if (str == null || str.isEmpty()) {
            return EMPTY;
        }

        String cleanedStr = StringEscapeUtils.unescapeHtml4(
                    Jsoup.clean(
                        replaceWindows1252CharacterCodeEntities(str),
                        Whitelist.none()
                    )
                );

        if (escapeXml) {
            return StringEscapeUtils.escapeXml(cleanedStr);
        } else {
            return cleanedStr;
        }
    }

    /** Returns a string with markup preserved, all entities resolved except the XML special entitites. */
    public static String cleanToHtml(String str) {
        if (str == null || str.isEmpty()) {
            return EMPTY;
        }
        return escapeTextInMarkup(
                replaceWindows1252CharacterCodeEntities(str)
            );
    }

    /** Returns a string with markup preserved; optionally escape XML and HTML specials. */
    public static String cleanText(String str, boolean escapeXml, boolean escapeHtml) {
        if (str == null || str.isEmpty()) {
            return EMPTY;
        }

        String cleanedStr = StringEscapeUtils.unescapeHtml4(
                    Jsoup.clean(
                        replaceWindows1252CharacterCodeEntities(str),
                        Whitelist.none()
                    )
                );

        if (escapeXml) {
            cleanedStr = StringEscapeUtils.escapeXml(cleanedStr);
        }

        if (escapeHtml) {
            cleanedStr = StringEscapeUtils.escapeHtml4(cleanedStr);
        }

        return cleanedStr;
    }

    /** Removes markup in text, keeping anchors.
     * @param input String dirty html-ridden text.
     * @return String plain text with any anchor tags intact.
     */
    public static String removeMarkupExceptAnchors(final String input) {
        if (StringUtils.isEmpty(input)) {
            return "";
        }
        Whitelist whitelist = Whitelist
                .none()
                .addTags("a")
                .addAttributes("a","href", "class", "debug");
        Cleaner cleaner = new Cleaner(whitelist);
        return cleaner.clean(Jsoup.parse(input)).body().html();
    }

    /** Returns a string with markup preserved, all named entities resolved except the XML special entitites.
     *
     * Does not resolve character code entities.
     */
    private static String escapeTextInMarkup(String str) {
        if (str == null || str.isEmpty()) {
            return EMPTY;
        }
        // Leveraging Jsoup to translate any literal xml special characters in HTML text.
        // Just parsing and writing out the document is enough using the xhtml escape mode
        // to prevent entification of non-ascii characters into HTML 4 entities.
        Document doc = Jsoup.parse(str, "UTF-8");
        doc.outputSettings().escapeMode(Entities.EscapeMode.valueOf("xhtml"));
        Element body = doc.body();
        return body.html();
    }

}
