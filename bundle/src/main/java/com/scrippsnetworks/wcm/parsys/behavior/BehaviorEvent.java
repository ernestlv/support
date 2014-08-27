package com.scrippsnetworks.wcm.parsys.behavior;

import org.apache.sling.api.resource.Resource;

/**
 * For tracking exactly what is going on in these ParagraphManagerBehavior classes.
 * Having a List of these could be handy for passing around as you're constructing
 * a paragraph, for determining what behaviors have performed what actions on the
 * same paragraph.
 * @author Jason Clark
 *         Date: 6/21/13
 */
public interface BehaviorEvent {

    /** Type of ParagraphManagerBehavior in this event. */
    public BehaviorTypes getBehaviorType();

    /** Action performed in this event. */
    public BehaviorActions getBehaviorAction();

    /** Resource affected by this event. */
    public Resource getResource();

}
