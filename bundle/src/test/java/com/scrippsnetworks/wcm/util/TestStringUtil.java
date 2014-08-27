package com.scrippsnetworks.wcm.util;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.scrippsnetworks.wcm.util.StringUtil;
import java.lang.StringBuilder;
import org.apache.commons.lang3.StringEscapeUtils;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class TestStringUtil {

    // This is too generous, but it does exclude xml entities.
    public final Pattern nonXmlEntities = Pattern.compile("&(?!amp;|quot;|lt;|gt;|apos;)[a-zA-Z0-9#]+;");
    public final Pattern doubleEscaping = Pattern.compile("&amp;[a-zA-Z0-9#]+;");
    public final Pattern entities = Pattern.compile("&[a-zA-Z0-9#]+;");

    String xmlEntities = "&lt; &gt; &amp; &quot; &apos;";
    String xmlCharCodeEntities = "&#60; &#62; &#38; &#34; &#39;";
    String xmlZeroCharCodeEntities = "&#060; &#062; &#038; &#034; &#039;";
    String xmlLiterals = "< > & \" '";
    String testMarkup = "<i>italic</i> <br><br /> This & That &amp; theother <a href=\"linked.html?foo=bar&amp;one=two\">escaped amp</a> <a href=\"linked.html?foo=bar&one=two\">literal amp</a> <script type='text/javascript'>ascript()</script>";
    // see the characters unique to the character set at http://en.wikipedia.org/wiki/Windows-1252
    String windows1252UnicodeLiterals = "\u20ac \u201a \u0192 \u201e \u2026 \u2020 \u2021 \u02c6 \u2030 \u0160 \u2039 \u0152 \u017d \u2018 \u2019 \u201c \u201d \u2012 \u2013 \u2014 \u02dc \u2122 \u0161 \u203a \u0153 \u017e \u0178";

    private String buildTestString() {
        StringBuilder sb = new StringBuilder();
        sb.append("charcode entities: ").append(generateCharacterCodeEntities(0,256)).append(" ");
        sb.append("hex charcode entities: ").append(generateHexCharacterCodeEntities(0,256)).append(" ");
        sb.append("html4 entities: ").append(generateHtmlEntities()).append(" ");
        sb.append("html4 unicode literals: ").append(generateLatinUnicodeLiterals()).append(" ");
        sb.append("xml entities: ").append(xmlEntities).append(" ");
        sb.append("xml charcode entities: ").append(xmlCharCodeEntities).append(" ");
        sb.append("xml leading-zero charcode entities: ").append(xmlZeroCharCodeEntities).append(" ");
        sb.append("xml literals: ").append(xmlLiterals).append(" ");
        sb.append("markup ").append(testMarkup).append(" ");
        return sb.toString();
    }

    private String generateCharacterCodeEntities(int min, int max) {
        StringBuilder sb = new StringBuilder();
        for (int i = min; i < max; i++) {
            sb.append("&#" + String.valueOf(i) + ";").append(" ");
        }
        return sb.toString();
    }

    private String generateHexCharacterCodeEntities(int min, int max) {
        StringBuilder sb = new StringBuilder();
        for (int i = min; i < max; i++) {
            sb.append("&#x" + Integer.toHexString(i) + ";").append(" ");
        }
        return sb.toString();
    }

    private String generateHtmlEntities() {
        return StringEscapeUtils.escapeHtml4(generateLatinUnicodeLiterals());
    }

    private String generateLatinUnicodeLiterals() {
        StringBuilder sb = new StringBuilder();
        for (char c = 160; c < 255; c++) {
            sb.append(Character.toString(c)).append(" ");
        }
        return sb.toString();
    }

    @Test
    public void testEntityRegexPositive() {
        // The strings we're testing contain the entity and some junk.
        for (int i = 128; i < 160; i++) {
            String n = String.valueOf(i);
            // Entity followed by additional entity-like characters that shouldn't match.
            Matcher m = StringUtil.windows1252CharCodeEntityPattern.matcher("&#" + n + ";#" + n + ";");
            assertTrue(m.find());
            assertTrue(n.equals(m.group(1)));

            // again with leading 0
            m =  StringUtil.windows1252CharCodeEntityPattern.matcher("&#0" + n + ";#0" + n + ";");
            assertTrue(m.find());
            assertTrue(("0" + n).equals(m.group(1)));

            // again with leading 0
            m =  StringUtil.windows1252CharCodeEntityPattern.matcher("&#00" + n + ";#00" + n + ";");
            assertTrue(m.find());
            assertTrue(("00" + n).equals(m.group(1)));

            n = Integer.toHexString(i);

            m = StringUtil.windows1252CharCodeEntityPattern.matcher("&#x" + n + ";#x" + n + ";");
            assertTrue(m.find());
            assertTrue(("x" + n).equals(m.group(1)));

            // again with leading 0
            m = StringUtil.windows1252CharCodeEntityPattern.matcher("&#x0" + n + ";#x0" + n + ";");
            assertTrue(m.find());
            assertTrue(("x0" + n).equals(m.group(1)));
            
            // again with leading 0s
            m = StringUtil.windows1252CharCodeEntityPattern.matcher("&#x00" + n + ";#x00" + n + ";");
            assertTrue(m.find());
            assertTrue(("x00" + n).equals(m.group(1)));

        }
    }

    @Test
    public void testEntityRegexNegative() {
        for (int i = 0; i < 256; i++) {

            // These control character codes match so we can strip them.
            if (i < 32 || i == 127) {
                continue;
            }

            // These are the ones the regex is supposed to match.
            if (i >= 128 && i < 256) {
                continue;
            }

            String n = String.valueOf(i);

            // Entity followed by additional entity-like characters that shouldn't match.
            Matcher m = StringUtil.windows1252CharCodeEntityPattern.matcher("&#" + n + ";#" + n + ";");
            assertFalse(m.find());

            // again with leading 0
            m =  StringUtil.windows1252CharCodeEntityPattern.matcher("&#0" + n + ";#0" + n + ";");
            assertFalse(m.find());

            // again with leading 0s
            m =  StringUtil.windows1252CharCodeEntityPattern.matcher("&#00" + n + ";#00" + n + ";");
            assertFalse(m.find());

            n = Integer.toHexString(i);

            m = StringUtil.windows1252CharCodeEntityPattern.matcher("&#x" + n + ";#x" + n + ";");
            assertFalse(m.find());

            // again with leading 0
            m = StringUtil.windows1252CharCodeEntityPattern.matcher("&#x0" + n + ";#x0" + n + ";");
            assertFalse(m.find());
            
            // again with leading 0s
            m = StringUtil.windows1252CharCodeEntityPattern.matcher("&#x00" + n + ";#x00" + n + ";");
            assertFalse(m.find());
        }
    }

    @Test
    public void testCleanHtmlForNoNonXmlEntitites() {
        String testString = buildTestString();
        String cleanString = StringUtil.cleanToHtml(testString);
        assertFalse(nonXmlEntities.matcher(cleanString).find());
    }

    @Test
    public void testCleanHtmlForNoDoubleEscaping() {
        String testString = buildTestString();
        String cleanString = StringUtil.cleanToHtml(testString);
        assertFalse(doubleEscaping.matcher(cleanString).find());
    }

    @Test
    public void testCleanPlainTextForEntities() {
        String testString = buildTestString();
        String cleanString = StringUtil.cleanToPlainText(testString);
        assertFalse(entities.matcher(cleanString).find());
    }

    @Test
    public void testCleanPlainTextForNoDoubleEscaping() {
        String testString = buildTestString();
        String cleanString = StringUtil.cleanToPlainText(testString);
        assertFalse(doubleEscaping.matcher(cleanString).find());
    }

    @Test
    public void testCleanEscapedTextForNoNonXmlEntitites() {
        String testString = buildTestString();
        String cleanString = StringUtil.cleanToEscapedText(testString);
        assertFalse(nonXmlEntities.matcher(cleanString).find());
    }

    @Test
    public void testCleanEscapedTextForNoDoubleEscaping() {
        String testString = buildTestString();
        String cleanString = StringUtil.cleanToEscapedText(testString);
        assertFalse(doubleEscaping.matcher(cleanString).find());
    }

    @Test
    public void testRemovesControlCharacters() {
        String testString = generateCharacterCodeEntities(0,31);
        assertTrue(StringUtil.cleanToPlainText(testString).trim().isEmpty());
        assertTrue(StringUtil.cleanToEscapedText(testString).trim().isEmpty());
        assertTrue(StringUtil.cleanToHtml(testString).trim().isEmpty());
    }

    @Test
    public void testLiteralizesWindows1252() {
        String testString = generateCharacterCodeEntities(128,160); // generates some undefined ones, that's OK
        String cleanString = StringUtil.cleanToPlainText(testString);
        assertTrue(cleanString.equals(windows1252UnicodeLiterals));
    }

    @Test
    public void testPlainStripsInvalidMarkup() {
        String testString = "This <i>string</i> has <a href=\"foo.html\">markup";
        String target = "This string has markup";
        String cleanString = StringUtil.cleanToPlainText(testString);
        assertTrue(cleanString.equals(target));
    }

    @Test
    public void testEscapedStripsInvalidMarkup() {
        String testString = "This <i>string</i> has <a href=\"foo.html\">markup";
        String target = "This string has markup";
        String cleanString = StringUtil.cleanToEscapedText(testString);
        assertTrue(cleanString.equals(target));
    }

    @Test
    public void testHtmlWithInvalidMarkup() {
        // No right answer, but don't barf.
        String testString = "This <i>string has <a href=\"foo.html\">markup";
        String target = "This <i>string has <a href=\"foo.html\">markup</a></i>";
        String cleanString = StringUtil.cleanToHtml(testString).replace("\n","");
        assertTrue(cleanString.replace("\n"," ").equals(target));
    }

}
