package com.scrippsnetworks.wcm.parsys.behavior;

import com.scrippsnetworks.wcm.parsys.behavior.impl.BehaviorEventImpl;
import org.apache.sling.api.resource.Resource;

/**
 * @author Jason Clark
 *         Date: 6/21/13
 */
public class BehaviorEventFactory {

    private BehaviorTypes behaviorType;
    private BehaviorActions behaviorAction;
    private Resource resource;

    public BehaviorEvent build() {
        return new BehaviorEventImpl(behaviorType, behaviorAction, resource);
    }

    public BehaviorEventFactory withBehaviorType(BehaviorTypes behaviorType) {
        this.behaviorType = behaviorType;
        return this;
    }

    public BehaviorEventFactory withBehaviorAction(BehaviorActions behaviorAction) {
        this.behaviorAction = behaviorAction;
        return this;
    }

    public BehaviorEventFactory withResource(Resource resource) {
        this.resource = resource;
        return this;
    }
}
