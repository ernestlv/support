package com.scrippsnetworks.wcm.recipe.notes;

import com.scrippsnetworks.wcm.recipe.notes.impl.NoteImpl;
import org.apache.sling.api.resource.Resource;

/**
 * Build a Note object out of a Resource for recipe data block.
 * @author Jonathan Bell
 *         Date: 9/23/2013
 */
public class NoteFactory {

    private Resource resource;
    private String rmaText;
    private String title;
    private String text;

    /** Construct an Note out of the pieces we have. */
    public Note build() {
        if (resource != null) {
            return new NoteImpl(resource);
        } else if (rmaText != null) {
            return new NoteImpl(rmaText);
        } else if (title != null || text != null) {
            return new NoteImpl(title, text);
        }
        return null;
    }

    /** Add a note block Resource to this builder. */
    public NoteFactory withResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    /** Add raw RMA text to your note. */
    public NoteFactory withRmaText(String rmaText) {
        this.rmaText = rmaText;
        return this;
    }

    /** Add a title to your note. */
    public NoteFactory withTitle(String title) {
        this.title = title;
        return this;
    }

    /** Add text to your note. */
    public NoteFactory withText(String text) {
        this.text = text;
        return this;
    }

}
