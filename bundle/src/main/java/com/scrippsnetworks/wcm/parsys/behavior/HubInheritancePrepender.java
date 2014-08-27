package com.scrippsnetworks.wcm.parsys.behavior;

import static com.scrippsnetworks.wcm.parsys.behavior.BehaviorActions.ADD;
import static com.scrippsnetworks.wcm.parsys.behavior.BehaviorTypes.*;
import com.day.cq.wcm.foundation.Paragraph;
import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.parsys.*;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This will add Resources of modules shared by a hub to eligible hub children.  This is done
 * by checking for the region name of the shared module and prepending shared resources into
 * the region of the same name in the eligible hub children.
 * @author Jason Clark
 *         Date: 6/19/13
 */
public class HubInheritancePrepender extends ParagraphManagerBehavior {

    private static final Logger log = LoggerFactory.getLogger(HubInheritancePrepender.class);

    private ParagraphSystemContext context;

    /** Inject hub behavior into paragraph manager. */
    @Override
    public LinkedList<Paragraph> execute(ParagraphSystemContext context, LinkedList<Paragraph> paragraphs) {
        if (paragraphs == null) {
            paragraphs = new LinkedList<Paragraph>();
        }
        if (context == null) {
            return paragraphs;
        }
        this.context = context;
        SniPage contextPage = context.getCurrentPage();
        Resource contextResource = context.getCurrentResource();
        if (contextPage == null || contextResource == null) {
            log.error("contextPage or contextResource were null.");
            return paragraphs;
        }
        Hub contextHub = contextPage.getHub();
        if (contextHub == null) {
            log.debug("contextPage not in hub, nothing to do here.");
            return paragraphs;
        }
        if (contextPage.getPath().equals(contextHub.getHubMaster().getPath())) {
            log.debug("page is the hub master, don't share with itself.");
            return paragraphs;
        }
        String contextResourceRelativePath = getPageRelativePath(contextResource);
        List<Resource> hubModules = contextHub.getSharedModules();
        if (hubModules != null && hubModules.size() > 0) {
            Collections.reverse(hubModules);
            for (Resource module : hubModules) {
                if (module != null) {
                    String moduleParsysPath = getPageRelativePath(module.getParent());
                    if (moduleParsysPath.equals(contextResourceRelativePath)
                            && isGoodModule(module)) {
                        Paragraph par = new NormalParagraph(module);
                        paragraphs.addFirst(par);
                        BehaviorEvent event = new BehaviorEventFactory()
                                .withBehaviorType(HUB_PREPENDER)
                                .withBehaviorAction(ADD)
                                .withResource(module)
                                .build();
                        context.addBehaviorEvent(event);
                    }
                }
            }
        }
        return paragraphs;
    }

    /** One method to rule them all... */
    private boolean isGoodModule(Resource module) {
        return !wasSharedByPackage(module) && !isDuplicate(module);
    }

    /** Convenience method to find out if a module of the same *type* was shared by a package. */
    protected boolean wasSharedByPackage(Resource module) {
        List<BehaviorEvent> events = context.getBehaviorEvents();
        for (BehaviorEvent event : events) {
            Resource eventResource = event.getResource();
            if (eventResource != null
                    && event.getBehaviorType() == PACKAGE_PREPENDER
                    && event.getBehaviorAction() == ADD
                    && eventResource.getResourceType().equals(module.getResourceType()))
            {
                return true;
            }
        }
        return false;
    }

    /** Convenience method for testing if this instance of the module is already being shared. */
    protected boolean isDuplicate(Resource module) {
        List<BehaviorEvent> events = context.getBehaviorEvents();
        for (BehaviorEvent event : events) {
            Resource eventResource = event.getResource();
            if (eventResource != null) {
                if (module.getPath().equals(eventResource.getPath())) {
                    return true;
                }
            }
        }
        return false;
    }
}
