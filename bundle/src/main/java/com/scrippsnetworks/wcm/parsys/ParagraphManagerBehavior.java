package com.scrippsnetworks.wcm.parsys;

import java.util.List;
import java.util.LinkedList;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.foundation.Paragraph;
import org.apache.sling.api.resource.Resource;

/** Base class providing ParagraphManager integration for ParagraphManagerBehavior implementations.
 *
 * Provides the haining and execution mechanism for ParagraphManagerBehaviors.
 */
public abstract class ParagraphManagerBehavior {

    private ParagraphManagerBehavior next = null;

    /** Get a Resource's path relative to the jcr:content node of its containing page. */
    public static String getPageRelativePath(Resource resource) {
        String path = resource.getPath();
        return path.substring(path.indexOf(JcrConstants.JCR_CONTENT)
                        + JcrConstants.JCR_CONTENT.length() + 1);
    }

    public void setNext(ParagraphManagerBehavior next) {
        this.next = next;
    }

    public List<Paragraph> getParagraphs(ParagraphSystemContext context, LinkedList<Paragraph> paragraphs) {
        LinkedList<Paragraph> newParagraphs = execute(context, paragraphs);

        if (next != null) {
            return next.getParagraphs(context, newParagraphs);
        } else {
            return newParagraphs;
        }
    }
    
    abstract public LinkedList<Paragraph> execute(ParagraphSystemContext context, LinkedList<Paragraph> paragraphs);
}
