package com.scrippsnetworks.wcm.parsys.behavior;

import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

import com.scrippsnetworks.wcm.parsys.*;
import org.apache.sling.api.resource.Resource;
import com.day.cq.wcm.foundation.Paragraph;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.snipackage.SniPackage;

import static com.scrippsnetworks.wcm.parsys.behavior.BehaviorTypes.PACKAGE_PREPENDER;
import static com.scrippsnetworks.wcm.parsys.behavior.BehaviorActions.ADD;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageInheritancePrepender extends ParagraphManagerBehavior {

    public static final String PACKAGE_PROPERTY = "sni:package";
    public static final String MODULES_PROPERTY = "sni:packageModules";
    public static final String HUB_PROPERTY = "sni:hub";
    public static final String HUB_PAGES_PROPERTY = "sni:hub";
    
    public static Logger logger = LoggerFactory.getLogger(PackageInheritancePrepender.class);

    public LinkedList<Paragraph> execute(ParagraphSystemContext context, LinkedList<Paragraph> paragraphs) {
        if (paragraphs == null) {
            paragraphs = new LinkedList<Paragraph>();
        }

        if (context == null) {
            return paragraphs;
        }

        SniPage currentPage = context.getCurrentPage();
        Resource resource = context.getCurrentResource();

        if (resource == null || currentPage == null) {
            logger.debug("need resource and current page");
            return paragraphs;
        }

        logger.debug("executing {} for {}", this.getClass().getName(), context.getCurrentPage().getPath());

        String parsysPath = getPageRelativePath(resource);

        SniPackage snipkg = currentPage.getSniPackage();

	    if (snipkg == null) {
            logger.debug("page not in package, doing nothing");
        	return paragraphs;
	    }

        if (snipkg.getPackageAnchor() != null) {
            if (currentPage.getPath().equals(snipkg.getPackageAnchor().getPath())) {
                logger.debug("page is the package anchor, don't share with itself.");
                return paragraphs;
            }
        }

        List<Resource> modules = snipkg.getModules();
        if (modules != null) {
            Collections.reverse(modules);
            for (Resource res : modules) {
                // use res.getParent to get the parsys the module is in
                String moduleParsysPath = getPageRelativePath(res.getParent());
                if (moduleParsysPath.equals(parsysPath)) {
                    Paragraph par = new NormalParagraph(res);
                    paragraphs.addFirst(par);

                    BehaviorEvent event = new BehaviorEventFactory()
                            .withBehaviorType(PACKAGE_PREPENDER)
                            .withBehaviorAction(ADD)
                            .withResource(par)
                            .build();
                    context.addBehaviorEvent(event);

                    logger.debug("added paragraph for res {}", res.getPath());
                } else {
                    logger.debug("rejecting module parsys {} != {}", moduleParsysPath, parsysPath);
                }
            }
        } else {
            logger.debug("no modules shared, doing nothing");
        }

        return paragraphs;
    }

}
