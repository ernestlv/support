package com.scrippsnetworks.wcm.recipe.notes;

/**
 * Recipe notes.
 * @author Jonathan Bell
 *         Date: 9/23/2013
 */
public interface Note {

    /** Title above the note text, if any. */
    public String getTitle();

    /** Text from the note. */
    public String getText();

}
