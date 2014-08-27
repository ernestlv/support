package com.scrippsnetworks.wcm.snipackage.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.snipackage.SniPackage;
import com.scrippsnetworks.wcm.util.CompoundProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** SniPackage implementation that understands both direct and hub package relationships.
 *
 * A @{link Hub} package relationship (where the package anchor is the page's Hub master's package) takes precedence over a
 * relationship provided directly for the page. When both relations are present, the effective package anchor is the one
 * provided by the Hub master.
 *
 * @author Scott Everett Johnson
 */
public class BaseSniPackage implements SniPackage {
    protected static Logger logger = LoggerFactory.getLogger(BaseSniPackage.class);

    protected SniPage packageAnchor = null;
    protected SniPage currentPage = null;
    protected SniPackage.PackageRelation relation;

    protected Map<String, Resource> regions;
    protected List<Resource> modules;
    protected Map<SniPackage.PackageRelation, SniPage> allPackageRelations = new HashMap<SniPackage.PackageRelation, SniPage>();

    /** Declares what the package relation preference order for this implementation. */
    public static final SniPackage.PackageRelation[] relationPreferenceOrder = {
        SniPackage.PackageRelation.HUB, SniPackage.PackageRelation.DIRECT
    };

    public BaseSniPackage(SniPage currentPage, SniPage directPackageAnchor, SniPage hubPackageAnchor) {

        if (currentPage == null) {
            throw new IllegalArgumentException("current page must not be null");
        }

        if (directPackageAnchor == null && hubPackageAnchor == null) {
            throw new IllegalArgumentException("need at least one nonnull package anchor");
        }

        this.currentPage = currentPage;

        if (hubPackageAnchor != null) {
            allPackageRelations.put(SniPackage.PackageRelation.HUB, hubPackageAnchor);
            this.packageAnchor = hubPackageAnchor;
            this.relation = SniPackage.PackageRelation.HUB;
        }

        if (directPackageAnchor != null) {
            allPackageRelations.put(SniPackage.PackageRelation.DIRECT, directPackageAnchor);
            if (this.packageAnchor == null) {
                this.packageAnchor = directPackageAnchor;
                this.relation = SniPackage.PackageRelation.DIRECT;
            }
        }

        if (this.packageAnchor == null) {
            // This should not happen as we already checked for all-null anchor arguments.
            // However, leave this here in case the logic is changed above.
            throw new RuntimeException("null package anchor");
        }

    }

    /**
     * {@inheritDoc}
     */
    public SniPage getPackageAnchor() {
        return packageAnchor;
    }

    /**
     * {@inheritDoc}
     */
    public SniPackage.PackageRelation getPackageRelation() {
        return relation;
    }

    /**
     * {@inheritDoc}
     */
    public Map<SniPackage.PackageRelation, SniPage> getAllPackageRelations() {
        return allPackageRelations;
    }

    /**
     * {@inheritDoc}
     */
    public SniPackage.PackageRelation[] getRelationPreferenceOrder() {
        return relationPreferenceOrder;
    }

    /**
     * {@inheritDoc}
     */
    public String getPackageName() {
        if (packageAnchor == null) {
            return null;
        }
        return packageAnchor.getTitle();
    }

    /**
     * {@inheritDoc}
     */
    public String getPackageTheme() {
        if (packageAnchor == null) {
            return null;
        }
        return packageAnchor.getProperties().get(SniPackage.THEME_PROPERTY, String.class);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Resource> getRegions() {
        if (regions == null) {
            extractRegions();
        }
        return regions;
    }

    /**
     * {@inheritDoc}
     */
    public List<Resource> getModules() {
        if (modules == null) {
            extractModules();
        }
        return modules;
    }

    /** Extracts regions exported by the package anchor page.
     *
     * Only regions targeting the current page (directly, or via the wildcard page type) are provided in the constructor are
     * made available. This method expects the paths to point to a region editor page where the region parsys indicates the
     * name of the region (i.e., if the path to the parsys is jcr:content/right-rail, the region name is right-rail) where the
     * region name then becomes the key for retrieval from the Map<String, Resource> returned by SniPackage#getRegions. The
     * region resource must also have the type indicated by SniPackage.REGION_RESOURCE_TYPE. The region editor page may supply
     * more than one region parsys, but the region parsys resources must live directly under jcr:content.
     */
    private void extractRegions() {
        regions = new HashMap<String, Resource>();
        if (packageAnchor == null) {
            return;
        }

        String[] packageRegionsProp = packageAnchor.getProperties().get(SniPackage.REGIONS_PROPERTY, String[].class);
        if (packageRegionsProp != null) {
            PageManager pm = currentPage.getPageManager();
            List<CompoundProperty> propList = CompoundProperty.fromArray(packageRegionsProp);

            // Save entries for the wildcard page type here, then fill in the regions map in a second step.
            Map<String, Resource> anyRegions = new HashMap<String, Resource>();

            String pageType = currentPage.getPageType();

            for (CompoundProperty prop : propList) {
                String propKey = prop.getKey();
                boolean isWildcardType = SniPackage.WILDCARD_PAGE_TYPE.equals(propKey);

                logger.debug("handling compound region property {} = {}", prop.getKey(), prop.getValue());
                if (!isWildcardType && !pageType.equals(propKey)) {
                    // Don't bother extracting regions for other page types
                    logger.debug("skipping, key is not wildcard nor does {}={}", propKey, pageType);
                    continue;
                }

                String propValue = prop.getValue();
                if (propValue != null
                        && !propValue.isEmpty()) {
                    Page regionEditorPage = pm.getPage(propValue);
                    if (regionEditorPage != null) {
                        Resource contentResource = regionEditorPage.getContentResource();
                        if (contentResource != null) {
                            Iterator<Resource> iter = contentResource.listChildren();
                            while (iter.hasNext()) {
                                Resource child = iter.next();
                                if (SniPackage.REGION_RESOURCE_TYPE.equals(child.getResourceType())) {
                                    if (isWildcardType) {
                                        anyRegions.put(child.getName(), child);
                                        logger.debug("added {} to any", regionEditorPage.getPath());
                                    } else {
                                        regions.put(child.getName(), child);
                                        logger.debug("added {} to regions", regionEditorPage.getPath());
                                    }
                                } else {
                                    logger.debug("child resource type {} != current page type {}, skipping", child.getResourceType(), REGION_RESOURCE_TYPE);
                                }
                            }
                        } else {
                            logger.debug("region page {} does not have content resource", prop.getValue());
                        }
                    } else {
                        logger.debug("region page {} not available", prop.getValue());
                    }
                }

                // Now that we've collected regions addressed to our page type, fill in any provided for "any"
                for (Map.Entry<String, Resource> entry : anyRegions.entrySet()) {
                    if (!regions.containsKey(entry.getKey())) {
                        regions.put(entry.getKey(), entry.getValue());
                        logger.debug("added any entry {} to regions", entry.getValue());
                    }
                }
            }
        }
    }

    /** Extracts shared module component resources indicated by the package anchor page.
     *
     * The paths are expected to be direct paths to the resource (like those provided by the paragraphreference widget xtype.)
     */
    private void extractModules() {
        modules = new ArrayList<Resource>();
        if (packageAnchor == null) {
            return;
        }

        String[] packageModulesProp = packageAnchor.getProperties().get(SniPackage.MODULES_PROPERTY, String[].class);
        Resource contentResource = packageAnchor.getContentResource();

        if (contentResource == null) {
            return;
        }

        ResourceResolver resourceResolver = contentResource.getResourceResolver();
        if (resourceResolver != null) {
            if (packageModulesProp != null) {
                for (String modPath : packageModulesProp) {
                   Resource res = resourceResolver.getResource(modPath);
                   if (res != null) {
                       modules.add(res);
                   }
                }
            }
        }
    }
}
