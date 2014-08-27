package com.scrippsnetworks.wcm.recipe.notes.impl;

import static com.scrippsnetworks.wcm.recipe.data.DataReader.RECIPE_BODY;
import com.scrippsnetworks.wcm.recipe.notes.Note;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jonathan Bell
 *         Date: 9/23/2013
 */
public class NoteImpl implements Note {

    private static final Logger log = LoggerFactory.getLogger(NoteImpl.class);

    /** Member for block title. */
    private String title;

    /** Member for block body text. */
    private String text;

    /** Make notes out of a note block Resource. */
    public NoteImpl(final Resource resource) {
        if (resource != null) {
            ValueMap properties = resource.adaptTo(ValueMap.class);
            if (properties.containsKey(RECIPE_BODY)) {
                String recipeBody = properties.get(RECIPE_BODY, String.class);
                parseRmaText(recipeBody);
            }
        }
    }

    /** Build a Note given the text of a data block.
     * @param rmaText */
    public NoteImpl(final String rmaText) {
        parseRmaText(rmaText);
    }

    /** Deterministically build an Note. */
    public NoteImpl(final String title, final String text) {
        this.title = title;
        this.text = text;
    }

    /** Go over text line by line and parse stuff out. */
    private void parseRmaText(final String rmaText) {
        if (StringUtils.isNotBlank(rmaText)) {
            String[] lines = rmaText.split("\n");
            StringBuilder textBuilder = new StringBuilder();
            for (String rawLine : lines) {
                if (StringUtils.isNotBlank(rawLine)) {
                    String line = rawLine.replaceAll("\\r|\\n", "");
                    if (line.matches("^<BR>$")) {
                        continue;
                    }

                    if (line.equals(line.toUpperCase()) || line.matches(".*:$")) {
                        title = line;
                        continue;
                    }

                    Pattern pattern = Pattern.compile("^(<BR>|<P>)(.+)");
                    Matcher matcher = pattern.matcher(line);

                    if (matcher.matches()) {
                        if (matcher.groupCount() == 2) {
                            textBuilder.append(wrapWithParagraphTags(matcher.group(2)));
                        } else {
                            log.error("string cleaner matched, but couldn't clean the string... {}", line);
                            textBuilder.append(line);
                        }
                    } else {
                        textBuilder.append(wrapWithParagraphTags(line));
                    }
                }
            }
            if (textBuilder.length() > 0) {
                this.text = textBuilder.toString();
            }
        }
    }

    /** Convenience method for wrapping a line in paragraph tags. */
    private String wrapWithParagraphTags(String line) {
        return StringUtils.isNotBlank(line) ? "<p>" + line + "</p>" : line;
    }

    /** Title from the block. */
    public String getTitle() {
        return title;
    }

    /** Main body of text from the block. */
    public String getText() {
        return text;
    }
}
