package com.scrippsnetworks.wcm.parsys;

import com.scrippsnetworks.wcm.parsys.behavior.BehaviorEvent;
import org.apache.sling.api.resource.Resource;
import com.scrippsnetworks.wcm.page.SniPage;

import java.util.List;

/** Provides information to ParagraphManagerBehaviors on the paragraph system currently being processed.
 */
public interface ParagraphSystemContext {

    /** Returns the page containing the paragraph system. */
    public SniPage getCurrentPage();

    /** Returns the resource for the paragraph system. */
    public Resource getCurrentResource();

    /** Returns a List of BehaviorEvents for tracking the actions of ParagraphManagerBehaviors. */
    public List<BehaviorEvent> getBehaviorEvents();

    /** Update the behavior events log with a new event. */
    public void addBehaviorEvent(BehaviorEvent event);

}
