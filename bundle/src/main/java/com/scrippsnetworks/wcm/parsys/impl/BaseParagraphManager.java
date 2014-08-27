package com.scrippsnetworks.wcm.parsys.impl;

import java.util.*;

import com.scrippsnetworks.wcm.parsys.*;
import com.scrippsnetworks.wcm.parsys.behavior.BehaviorEvent;
import org.apache.sling.api.resource.Resource;
import com.day.cq.wcm.foundation.Paragraph;
import com.day.cq.wcm.foundation.ParagraphSystem;
import com.scrippsnetworks.wcm.page.SniPage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** ParagraphManager implementation which delegates behavior to a chain of ParagraphManagerBehaviors.
 *
 * The ParagraphManagerFactory is responsible for constructing the chain of behaviors and passing the head of the chain to
 * this class. Implements ParagraphManager for users of the API, and ParagraphSystemContext for passing context information to
 * the behaviors themselves.
 */
public class BaseParagraphManager implements ParagraphManager, ParagraphSystemContext {

    public static Logger logger = LoggerFactory.getLogger(BaseParagraphManager.class);

    private ParagraphSystem paragraphSystem = null;
    private SniPage currentPage = null;
    private Resource resource = null;
    private ParagraphManagerBehavior behavior = null;

    /** This is for creating an audit trail for what's been added/removed from a paragraph. */
    private List<BehaviorEvent> behaviorEvents;

    public BaseParagraphManager(SniPage currentPage, Resource resource, ParagraphSystem paragraphSystem, ParagraphManagerBehavior behavior) {
        this.paragraphSystem = paragraphSystem;
        this.currentPage = currentPage;
        this.resource = resource;
        this.behavior = behavior;
        this.behaviorEvents = new ArrayList<BehaviorEvent>();
    }

    /**
     * {@inheritDoc}
     */
    public SniPage getCurrentPage() {
        return currentPage;
    }

    /**
     * {@inheritDoc}
     */
    public Resource getCurrentResource() {
        return resource;
    }

    /** {@inheritDoc} */
    @Override
    public List<BehaviorEvent> getBehaviorEvents() {
        return behaviorEvents;
    }

    /** {@inheritDoc} */
    @Override
    public void addBehaviorEvent(BehaviorEvent event) {
        behaviorEvents.add(event);
    }

    /**
     * {@inheritDoc}
     *
     * The implementation gets the unaltered list of paragraphs from the paragraph system, and either returns that directly if
     * there are no behaviors supplied, or passes the list to the head of the chain of behaviors and returns the result.
     */
    public List<Paragraph> getParagraphs() {
        if (paragraphSystem == null) {
            return Collections.EMPTY_LIST;
        }

        if (currentPage == null)  {
            return paragraphSystem.paragraphs();
        }

        LinkedList<Paragraph> paragraphs = new LinkedList<Paragraph>(paragraphSystem.paragraphs());
        if (behavior != null) {
            return behavior.getParagraphs(this, paragraphs);
        } else {
            return paragraphs;
        }
    }
}
