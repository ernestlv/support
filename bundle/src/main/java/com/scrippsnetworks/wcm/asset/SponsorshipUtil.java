package com.scrippsnetworks.wcm.asset;

import java.lang.String;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;

import com.day.cq.wcm.api.Page;

import com.scrippsnetworks.wcm.asset.hub.HubManager;
import com.scrippsnetworks.wcm.asset.hub.Hub;
import com.scrippsnetworks.wcm.asset.SearchTermMetadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utility for retrieving sponsorship information.
 *
 * The sponsorship value depends on hub and package membership. Since the hub is used throughout the request, it is
 * made available as an attribute in request scope and thus is expected to be passed to methods in this class
 * so as not to have to execute the hub location logic again. Package membership is indicated on the current page
 * and so does not need to be passed.
 */
public class SponsorshipUtil {
    private static String PROP_PACKAGE = "sni:package";
    private static String PROP_SPONSORSHIP = "sni:sponsorship";
    private static String PROP_SNI_ASSET = "sni:assetLink";
    private static String PROP_CONTENT_SPONSORSHIP = "jcr:content/sni:sponsorship"; // convenience

    private static final Logger log = LoggerFactory.getLogger(SponsorshipUtil.class);

    /** Returns the sponsorship resource given a page's content resource and hub.
     *
     * @param resource a page content resource
     * @param hub the page's Hub, may be null
     * @return Resource the page's sponsorship resource, or null if not available
     */
    public static Resource getSponsorshipResource(Resource resource, Hub hub) {

        if (resource == null) {
            return null;
        }

        // Reusable
        Resource sponsorshipResource = null;
        String sponsorshipPath;
        ValueMap properties;

        ValueMap pageProperties = resource.adaptTo(ValueMap.class);
        ResourceResolver resourceResolver = resource.getResourceResolver();

        log.debug("handling path {}", resource.getPath());

        // package always trumps
        String packagePath = pageProperties.get(PROP_PACKAGE, String.class);
        if (packagePath != null) {
            log.debug("has package {}", packagePath);
            Resource packageResource = resourceResolver.getResource(packagePath);
            if (packageResource != null) {
                Page packagePage = packageResource.adaptTo(Page.class);
                if (packagePage != null) {
                    packageResource = packagePage.getContentResource();
                    properties = ResourceUtil.getValueMap(packageResource);
                    sponsorshipPath = properties.get(PROP_SPONSORSHIP, String.class);
                    if (sponsorshipPath != null && sponsorshipPath.length() > 0) {
                        log.debug("package has sponsorship path {}", sponsorshipPath);
                        sponsorshipResource = getSponsorshipResourceFromPath(sponsorshipPath, resourceResolver);
                    }
                } else {
                    log.debug("package resource {} does not adapt to page", packageResource.getPath());
                }
            } else {
                log.debug("package resource at path {} not found", packagePath);
            }
        } else {
            log.debug("no package path. packagePath is null");
        }

        // OK, do *we* have a sponsorship?
        if (sponsorshipResource == null) {
            sponsorshipPath = pageProperties.get(PROP_SPONSORSHIP, String.class);
            if (sponsorshipPath != null && sponsorshipPath.length() > 0) {
                log.debug("using page sponsorship {}", sponsorshipPath);
                sponsorshipResource = getSponsorshipResourceFromPath(sponsorshipPath, resourceResolver);
            } else {
                //The Sponsorship may be set on an SNI Asset -- use it if it is there.
                // This is a temporary fix as we need to be able to unset sponsorship.
                String sniAssetPath = pageProperties.get(PROP_SNI_ASSET, String.class);
                if (sniAssetPath != null && sniAssetPath.length() > 0) {
                    log.debug("using sni asset {}", sniAssetPath);
                    Resource sniAssetResource = resourceResolver.getResource(sniAssetPath);
                    properties = ResourceUtil.getValueMap(sniAssetResource);
                    sponsorshipPath = properties.get(PROP_CONTENT_SPONSORSHIP, String.class);
                    if (sponsorshipPath != null && sponsorshipPath.length() > 0) {
                        log.debug("using asset sponsorship {}", sponsorshipPath);
                        sponsorshipResource = getSponsorshipResourceFromPath(sponsorshipPath, resourceResolver);
                    }
                } else {
                    log.debug("no sni asset path for {}", resource.getPath());
                }
            }
        }
                
        // if we have a hub and no sponsorship
        if (sponsorshipResource == null && hub != null) {
            Page masterPage = hub.getMasterPage();
            Resource masterResource = masterPage.getContentResource();
            // If we aren't the hub master (in that case, we already got our sponsorship).
            if (!masterResource.getPath().equals(resource.getPath())) {
                log.debug("using hub master {}", masterPage.getPath());
                properties = ResourceUtil.getValueMap(masterResource);
                sponsorshipPath = properties.get(PROP_SPONSORSHIP, String.class);
                if (sponsorshipPath != null && sponsorshipPath.length() > 0) {
                    log.debug("using hub sponsorship {}", sponsorshipPath);
                    sponsorshipResource = getSponsorshipResourceFromPath(sponsorshipPath, resourceResolver);
                }
            }
        }

        log.debug("getSponsorshipResource returning {}", sponsorshipResource != null ? sponsorshipResource.getPath() : "null");

        return sponsorshipResource;
    }

    /** Returns the sponsorship value given a page's content resource and hub.
     *
     * The sponsorship value is the last path component of the sponsorship resource's path, uppercased.
     *
     * @param resource a page content resource
     * @param hub the page's Hub, may be null
     * @return String the sponsorship value, or null if not available
     */
    public static String getSponsorshipValue(Resource resource, Hub hub) {
        Resource sponsorshipResource = getSponsorshipResource(resource, hub);
        String sponsorshipValue = null;
        if (sponsorshipResource != null) {
            sponsorshipValue = getSponsorshipValueFromSponsorshipResource(sponsorshipResource);
        }
        return sponsorshipValue;
    }

    /** Returns the sponsorship value given a page and hub.
     *
     * The sponsorship value is the last path component of the sponsorship resource's path, uppercased.
     *
     * @param page the page to determine the sponsorship for
     * @param hub the page's Hub, may be null
     * @return String the sponsorship value, or null if not available
     */
    public static String getSponsorshipValue(Page page, Hub hub) {
        if (page == null) {
            return null;
        }
        Resource resource = page.getContentResource();
        return getSponsorshipValue(resource, hub);
    }

    /** Returns the sponsorship value from the sponsorship resource.
     *
     */
    public static String getSponsorshipValueFromSponsorshipResource(Resource sponsorshipResource) {
        if (sponsorshipResource == null) {
            return null;
        }
        return sponsorshipResource.getPath().substring(sponsorshipResource.getPath().lastIndexOf("/")+1).toUpperCase();
    }

    /** Returns the sponsorship path given a page and hub.
     *
     * @param page the page to determine the sponsorship for
     * @param hub the page's Hub, may be null
     * @return String of sponsorship path, or null if not available
     */
    public static String getSponsorshipPath(Page page, Hub hub) {
        if (page == null) {
            return null;
        }
        Resource resource = page.getContentResource();
        Resource sponsorshipResource = getSponsorshipResource(resource, hub);
        String sponsorshipPath = null;
        if (sponsorshipResource != null) {
            sponsorshipPath = sponsorshipResource.getPath();
        }
        return sponsorshipPath;
    }

    /** Returns the sponsorship value for a search term.
     *
     * @param brand The brand under which to look for the search term.
     * @param searchTerm The search term for which to retrieve a sponsorship value.
     * @param resourceResolver The resource resolver to use.
     * @return String Sponsorship value for search term, or null.
     */
    @Deprecated
    public static String getSponsorshipValueForSearchTerm(String brand, String searchTerm, ResourceResolver resourceResolver) {
        SearchTermMetadata stMd = SearchTermMetadata.getSearchTermMetadata(brand, searchTerm, resourceResolver);
        if (stMd != null) {
            return stMd.getSponsorshipValue();
        } else {
            return null;
        }
    }

    /** Returns the sponsorship resource given the path.
     *
     * The on/off time of the sponsorship is respected, and no resource is returned if the sponsorship page is invalid.
     *
     * @param sponsorshipPath the path to the sponsorship resource
     * @return Resource the sponsorshipResource if it exists and is valid ({@link Page#isValid})
     */
    public static Resource getSponsorshipResourceFromPath(String sponsorshipPath, ResourceResolver resourceResolver) {
        Resource tempResource = null;
        Resource sponsorshipResource = null;
        Page sponsorshipPage = null;
        log.info("getSponsorshipResourceFromPath({})", sponsorshipPath);
        if (sponsorshipPath != null && sponsorshipPath.length() > 0) {
            tempResource = resourceResolver.getResource(sponsorshipPath);
            if (tempResource != null) {
                sponsorshipPage = tempResource.adaptTo(Page.class);
                // If no on/off times specified, sponsorship page is valid.
                if (sponsorshipPage != null) {
                    if (sponsorshipPage.isValid()) {
                        sponsorshipResource = tempResource;
                    } else {
                        log.debug("Page#isValid() returns false for {}", sponsorshipPath);
                    }
                } else {
                    log.debug("could not adapt sponsorship to page");
                }
            } else {
                log.debug("could not get resource for path {}", sponsorshipPath);
            }

        }
        return sponsorshipResource;
    }
}
