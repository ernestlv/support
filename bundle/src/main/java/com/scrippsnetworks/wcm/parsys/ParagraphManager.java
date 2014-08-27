package com.scrippsnetworks.wcm.parsys;

import java.util.List;
import com.day.cq.wcm.foundation.Paragraph;

/** The ParagraphManager provides a list of Paragraphs after processing by any ParagraphManagerBehaviors the
 * ParagraphManagerFactory has supplied for altering the contents of a ParagraphSystem.
 *
 */
public interface ParagraphManager {
    public List<Paragraph> getParagraphs();
}
