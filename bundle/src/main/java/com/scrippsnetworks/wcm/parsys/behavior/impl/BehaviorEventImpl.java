package com.scrippsnetworks.wcm.parsys.behavior.impl;

import com.scrippsnetworks.wcm.parsys.behavior.BehaviorActions;
import com.scrippsnetworks.wcm.parsys.behavior.BehaviorEvent;
import com.scrippsnetworks.wcm.parsys.behavior.BehaviorTypes;
import org.apache.sling.api.resource.Resource;

/**
 * @author Jason Clark
 *         Date: 6/21/13
 */
public class BehaviorEventImpl implements BehaviorEvent {

    /** What type of ParagraphManagerBehavior in this event. */
    private BehaviorTypes type;

    /** Which action was performed in this event. */
    private BehaviorActions action;

    /** What Resource was affected during this event, if any. */
    private Resource resource;

    /** Add the bits that make this an event worth remembering. */
    public BehaviorEventImpl(BehaviorTypes type, BehaviorActions action, Resource resource) {
        this.type = type;
        this.action = action;
        this.resource = resource;
    }

    /** {@inheritDoc} */
    @Override
    public BehaviorTypes getBehaviorType() {
        return type;
    }

    /** {@inheritDoc} */
    @Override
    public BehaviorActions getBehaviorAction() {
        return action;
    }

    /** {@inheritDoc} */
    @Override
    public Resource getResource() {
        return resource;
    }
}
