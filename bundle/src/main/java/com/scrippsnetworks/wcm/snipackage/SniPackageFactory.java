package com.scrippsnetworks.wcm.snipackage;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.snipackage.SniPackage;
import com.scrippsnetworks.wcm.snipackage.impl.BaseSniPackage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Responsible for creating SniPackage objects.
 *
 * The factory knows about the eligible package relationships (the page's own, the hub master's) and constructs the
 * appropriate SniPackage object.
 *
 * @author Scott Everett Johnson
 */
public class SniPackageFactory {

    public static Logger logger = LoggerFactory.getLogger(SniPackageFactory.class);

    /** Returns an SniPackage appropriate for the given page. */
    public static SniPackage getSniPackage(SniPage page) {
        if (page == null) {
            return null;
        }

        Hub hub = page.getHub();
        SniPage hubPackageAnchor = null;
        if (hub != null) {
            SniPage hubMaster = hub.getHubMaster();
            hubPackageAnchor = getPackageAnchor(hubMaster);
        }

        SniPage directPackageAnchor = getPackageAnchor(page);

        if (directPackageAnchor != null || hubPackageAnchor != null) {
            return new BaseSniPackage(page, directPackageAnchor, hubPackageAnchor);
        }

        // If we want to return a package object for the current page if the current page is an anchor, we need to know how to
        // determine that this page really is a package anchor.

        return null;
    }

    /** Returns the direct package anchor for the given page.
     *
     * The package anchor is determined either by the package property on the current page.
     */
    private static SniPage getPackageAnchor(Page currentPage) {
        if (currentPage == null) {
            return null;
        }

        SniPage retVal = null;
        String packagePath = null;
        PageManager pageManager = currentPage.getPageManager();

        if (pageManager == null) {
            logger.warn("could not retrieve page manager");
            return null;
        }

	    packagePath = currentPage.getProperties().get(SniPackage.PACKAGE_PROPERTY, String.class);
        if (packagePath != null) {
            Page tmpAnchor = getAnchorPageFromPath(pageManager, packagePath);
            if (tmpAnchor != null) {
                retVal = PageFactory.getSniPage(tmpAnchor);
            }
        }

        return retVal;
    }

    /** Retrieves the package anchor at path if it exists and is valid. */
    private static Page getAnchorPageFromPath(PageManager pageManager, String path) {
        Page retVal = null;
        if (path != null && path.length() > 0 && path.charAt(0) == '/') {
            Page tmpPage = pageManager.getPage(path);
            if (tmpPage != null && tmpPage.isValid()) {
                retVal = tmpPage;
            }
        } else {
            logger.debug("path {} not a valid package anchor path", path);
        }
        return retVal;
    }
}
