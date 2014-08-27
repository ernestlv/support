package com.scrippsnetworks.wcm.recipe.instructions.impl;

import static com.scrippsnetworks.wcm.recipe.data.DataReader.RECIPE_BODY;
import static com.scrippsnetworks.wcm.recipe.data.DataReader.RANK_ORDER;
import com.scrippsnetworks.wcm.recipe.instructions.Instruction;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jason Clark
 *         Date: 6/18/13
 */
public class InstructionImpl implements Instruction {

    private static final Logger log = LoggerFactory.getLogger(InstructionImpl.class);

    /** Member for block title. */
    private String title;

    /** Member for block body text. */
    private String text;

    /** sni:rankOrder of the instruction block. */
    private int rankOrder;

    /** Make instructions out of an instruction block Resource. */
    public InstructionImpl(final Resource resource) {
        if (resource != null) {
            ValueMap properties = resource.adaptTo(ValueMap.class);
            if (properties.containsKey(RECIPE_BODY)) {
                String recipeBody = properties.get(RECIPE_BODY, String.class);
                parseRmaText(recipeBody);
            }
            if (properties.containsKey(RANK_ORDER)) {
                String rawRankOrder = properties.get(RANK_ORDER, String.class);
                if (StringUtils.isNotBlank(rawRankOrder)) {
                    try {
                        rankOrder = Integer.valueOf(rawRankOrder);
                    } catch (NumberFormatException e) {
                        log.error("NumberFormatException in InstructionImpl: {}", e);
                        rankOrder = 0;
                    }
                }
            }
        }
    }

    /** Build an Instruction given the text of a data block.
     * @param rmaText*/
    public InstructionImpl(final String rmaText, final int rankOrder) {
        parseRmaText(rmaText);
        this.rankOrder = rankOrder;
    }

    /** Deterministically build an Instruction. */
    public InstructionImpl(final String title, final String text, final int rankOrder) {
        this.title = title;
        this.text = text;
        this.rankOrder = rankOrder;
    }

    /** Go over text line by line and parse stuff out. */
    private void parseRmaText(final String rmaText) {
        if (StringUtils.isNotBlank(rmaText)) {
            String[] lines = rmaText.split("\n");
            StringBuilder textBuilder = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                String rawLine = lines[i];
                if (StringUtils.isNotBlank(rawLine)) {
                    String line = rawLine.replaceAll("\\r|\\n", "");
                    if (line.matches("^<BR>$")) {
                        continue;
                    }

                    if (i == 0 && (line.equals(line.toUpperCase()) || line.matches(".*:$"))) {
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

    /** {@inheritDoc} */
    public int getRankOrder() {
        return rankOrder;
    }

}
