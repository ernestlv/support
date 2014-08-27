package com.scrippsnetworks.wcm.asset;

import java.lang.String;
import java.lang.StringBuilder;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import com.day.cq.wcm.api.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Encapsulates metadata for search terms.
 *
 * Search term metadata is applied to search term pages which aren't rendered but exist to be a site on which to place the
 * metadata. This class contains the logic to locate the search term page and extract the metadata.
 */
public class SearchTermMetadata {

    private String sponsorshipValue = null;
    private String adkey1 = null;
    private String adkey2 = null;
    private String searchTermPath = null;

    private static Logger log = LoggerFactory.getLogger(SearchTermMetadata.class);

    /** Create new object based on search term page attributes
     *
     * @param searchTermPage a search term page from which to extract metadata
     */
    public SearchTermMetadata(Resource searchTermPage) {
        if (searchTermPage != null) {
            Resource content = searchTermPage.getResourceResolver().getResource(searchTermPage, "jcr:content");
            searchTermPath = searchTermPage.getPath();
            String sponsorshipPath = ResourceUtil.getValueMap(content).get("sni:sponsorship", String.class);
            Resource sponsorshipResource = null;
            if (sponsorshipPath != null && !sponsorshipPath.isEmpty()) {
                log.debug("using sponsorship path {}", sponsorshipPath);
                sponsorshipResource = SponsorshipUtil.getSponsorshipResourceFromPath(sponsorshipPath, searchTermPage.getResourceResolver());
                if (sponsorshipResource != null) {
                    sponsorshipValue = ResourceUtil.getValueMap(sponsorshipResource).get("jcr:title", String.class);
                    if (sponsorshipValue == null) {
                        sponsorshipValue = sponsorshipResource.getPath().substring(sponsorshipResource.getPath().lastIndexOf("/") + 1).toUpperCase();
                    }
                } else {
                    log.debug("could not retrieve sponsorship resource from {}", sponsorshipPath);
                }
            }
            String adkeys = ResourceUtil.getValueMap(content).get("sni:adkey", String.class);
            if (adkeys != null && !adkeys.isEmpty()) {
                log.debug("using raw adkeys {}", adkeys);
                String[] adKeyArray = adkeys.replaceAll("[a-z]+-adkeys:", "").split("/");
                adkey1 = adKeyArray.length > 0 ? adKeyArray[0] : adkey1;
                adkey2 = adKeyArray.length > 1 ? adKeyArray[1] : adkey2;
            }
        }
    }

    /** Return sponsorship value, null if not set */
    public String getSponsorshipValue() {
        return sponsorshipValue;
    }

    /** Return primary ad key, null if not set. */
    public String getAdKey1() {
        return adkey1;
    }

    /** Return secondary ad key, null if not set */
    public String getAdKey2() {
        return adkey2;
    }


    /** Returns the metadata for a search term.
     *
     * @param brand The brand under which to look for the search term.
     * @param searchTerm The search term for which to retrieve a sponsorship value.
     * @param resourceResolver The resource resolver to use.
     * @return String Sponsorship value for search term, or null.
     */
    public static SearchTermMetadata getSearchTermMetadata(String brand, String searchTerm, ResourceResolver resourceResolver) {

        if (brand == null || searchTerm == null || resourceResolver == null) {
            log.warn("null argument, returning null");
            return null; // MU!
        }

        if (brand.trim().isEmpty()) {
            log.warn("getSponsorshipValueForSearchTerm: empty brand");
            return null;
        }

        // String will contain [a-z0-9-]. No effort is made to translate, say, unicode.
        // For those cases we would need to know how a search containing unicode is translated,
        // and translate it the same way, ideally using the same API/utility.
        String normalizedSearchTerm = searchTerm.toLowerCase()
            .trim()
            .replaceAll("&", "and")
            .replaceAll("\\s+", "-")
            .replaceAll("[^\\w\\-]", "")
            .replaceAll("-{2,}", "-")
            .replaceAll("^-", "")
            .replaceAll("-$", "");

        if (normalizedSearchTerm.isEmpty()) {
            log.warn("search term \"{}\" empty after replacements", searchTerm);
            return null;
        }

        String[] termArray = searchTerm.trim().split("\\s+");
        StringBuilder searchTermPath = new StringBuilder("/content/");
        searchTermPath.append(brand)
            .append("/search-terms/")
            .append(String.valueOf(termArray.length))
            .append("-word/")
            .append(String.valueOf(normalizedSearchTerm.length()));

        for (int i = 1; i <= 3 && i <= normalizedSearchTerm.length(); i++) {
            searchTermPath.append("/")
                .append(normalizedSearchTerm.substring(0,i));
        }
        searchTermPath.append("/").append(normalizedSearchTerm);

        log.debug("getSearchTermMetadata: using search term path {}", searchTermPath);

        Resource termResource = resourceResolver.getResource(searchTermPath.toString());

        if (termResource == null) {
            log.debug("getSearchTermMetadata: search term resource not found for {}", searchTermPath);
            return null;
        }

        return new SearchTermMetadata(termResource);
    }

    protected String getSearchTermPath() {
        return searchTermPath;
    }
}
