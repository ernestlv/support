package com.scrippsnetworks.wcm.recipe.data;

import java.util.EnumSet;

/**
 * These are the types of recipe asset data blocks, as determined by their jcr:title
 * property.  This Enum also contains the crosslinked version of the jcr:title.
 * @author Jason Clark
 *         Date: 6/11/13
 */
public enum DataBlockTypes {

    INGREDIENTS("ingredients", "crosslinked ingredients"),
    INSTRUCTIONS("instructions", "crosslinked instructions"),
    DIRECTIONS("directions", "crosslinked directions"),
    NUTRITION("nutrition", "crosslinked nutrition"),
    NOTES("notes", "crosslinked notes"),
    OTHER("", "");

    private String title;
    private String crosslinkedTitle;

    private DataBlockTypes(String title, String crosslinkedTitle) {
        this.title = title;
        this.crosslinkedTitle = crosslinkedTitle;
    }

    public String title() {
        return this.title;
    }

    public String crosslinkedTitle() {
        return this.crosslinkedTitle;
    }

    /** This represents all of the named, known types of blocks.  Handy for identifying oddly-named blocks. */
    public static final EnumSet<DataBlockTypes> KNOWN_TYPES = EnumSet.range(INGREDIENTS, NOTES);
}
