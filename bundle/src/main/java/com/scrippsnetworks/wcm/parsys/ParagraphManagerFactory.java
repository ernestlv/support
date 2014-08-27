package com.scrippsnetworks.wcm.parsys;

import java.util.LinkedList;
import java.util.Iterator;
import org.apache.sling.api.resource.Resource;
import com.day.cq.wcm.foundation.ParagraphSystem;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.parsys.impl.BaseParagraphManager;
import com.scrippsnetworks.wcm.parsys.behavior.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Constructs ParagraphManager objects with behaviors appropriate for the given page and parsys resource.
 *
 * Behaviors are implemented by classes extending ParagraphManagerBehavior. The manager maintains a chain of such behaviors,
 * with each behavior receiving the output (a list of paragraphs) of the previous behavior.
 *
 * @author Scott Everett Johnson
 */
public class ParagraphManagerFactory {

    public static Logger logger = LoggerFactory.getLogger(ParagraphManagerFactory.class);

    SniPage currentPage = null;
    Resource resource = null;
    ParagraphSystem parsys = null;
    LinkedList<ParagraphManagerBehavior> behaviors = new LinkedList<ParagraphManagerBehavior>();

    /** Returns a ParagraphManager with behaviors appropriate for the current context.
     *
     * @param currentPage The CQ Page object for the page currently being rendered.
     * @param resource The Sling Resource for the parsys currently being rendered.
     * @param parsys The CQ ParagraphSystem object for the parsys currently being rendered.
     * @return ParagraphManager
     */
    public static ParagraphManager getParagraphManager(SniPage currentPage, Resource resource, ParagraphSystem parsys) {
        ParagraphManagerFactory builder = new ParagraphManagerFactory(currentPage, resource, parsys);
        // These values can be used to trigger behaviors.
        String brandStr = currentPage.getBrand();
        String pageTypeStr = currentPage.getPageType();
        //String regionStr = resource.getName();

        // Probably behaviors themselves should be mechanisms, with the policy here determining
        // when the mechanisms are used. For now this is simple enough, but if it gets complicated
        // a more elaborate mechanism for adding behaviors may be desired. Regardless, plugging
        // behaviors using OSGi would be beneficial for other reasons.
        if (!("recipe".equals(pageTypeStr) && "food".equals(brandStr))) { //yoda was here
            builder.addBehavior(new PackageInheritancePrepender());
        }
        builder.addBehavior(new HubInheritancePrepender());
        return builder.build();
    }

    private ParagraphManagerFactory(SniPage currentPage, Resource resource, ParagraphSystem parsys) {
        this.currentPage = currentPage;
        this.resource = resource;
        this.parsys = parsys;
        logger.debug("constructed paragraph manager factory for {}", currentPage.getPath());
    }

    /** Adds a behavior to the current ParagraphManager.
     */
    private void addBehavior(ParagraphManagerBehavior behavior) {
        if (behaviors.size() > 0) {
            behaviors.getLast().setNext(behavior);
        }
        behaviors.add(behavior);
    }

    /** Constructs and returns a ParagraphManager with all provided behaviors.
     */
    private ParagraphManager build() {
        if (logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            Iterator<ParagraphManagerBehavior> iter = behaviors.listIterator();
            while (iter.hasNext()) {
                ParagraphManagerBehavior b = iter.next();
                sb.append(b.getClass().getSimpleName());
                if (iter.hasNext()) {
                    sb.append(",");
                }
            }
            logger.debug("constructing ParagraphManager with behaviors {} for {}", sb.toString(), resource.getPath());
        }
        return new BaseParagraphManager(currentPage, resource, parsys, behaviors.getFirst());
    }
}
