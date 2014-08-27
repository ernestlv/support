package com.scrippsnetworks.wcm.snipackage;

import java.util.Map;
import java.util.List;
import org.apache.sling.api.resource.Resource;
import com.scrippsnetworks.wcm.page.SniPage;

/** Provides access to package relationships for a page.
 *
 * A package is a group of pages all designating another page a package anchor. The SniPackage provides access to a page's
 * package anchor, and to resources shared by the package anchor.
 *
 * @author Scott Everett Johnson
 */
public interface SniPackage {
    public static final String PACKAGE_PROPERTY = "sni:package";
    public static final String HUB_PROPERTY = "sni:hub";
    public static final String HUB_PAGES_PROPERTY = "hubPages";
    public static final String REGIONS_PROPERTY = "sni:packageRegions";
    public static final String MODULES_PROPERTY = "sni:packageModules";
    public static final String THEME_PROPERTY = "sni:packageTheme";
    public static final String WILDCARD_PAGE_TYPE = "any";
    public static final String REGION_RESOURCE_TYPE = "foundation/components/parsys";

    /** Describes the relation of a page to a package anchor. */
    public enum PackageRelation { DIRECT, HUB };

    /** Returns the package anchor page. */
    public SniPage getPackageAnchor();
    /** Returns an enum value describing the relationship of the page to the package anchor. */
    public SniPackage.PackageRelation getPackageRelation();
    /** Returns all eligible package anchors, not just the effective one. */
    public Map<SniPackage.PackageRelation, SniPage> getAllPackageRelations();
    /** Returns an array describing the order of package relationship preference. */
    public SniPackage.PackageRelation[] getRelationPreferenceOrder();
    /** Returns the package name, generally the title of the anchor page. */
    public String getPackageName();
    /** Returns the package theme, which informs rendering on member pages. */
    public String getPackageTheme();
    /** Returns regions (CQ paragraph systems) being shared by the package anchor. */
    public Map<String, Resource> getRegions();
    /** Returns all components being shared by the package anchor. */
    public List<Resource> getModules();
}
