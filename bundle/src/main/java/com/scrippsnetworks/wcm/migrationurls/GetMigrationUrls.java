package com.scrippsnetworks.wcm.migrationurls;

import com.day.cq.replication.ReplicationStatus;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import java.io.IOException;
import java.util.Iterator;
import javax.servlet.ServletException;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A simple servlet that returns a text map of fastfwd urls to cq urls.
 *
 *
 * @author Jon Williams
 */
@SlingServlet(paths = "/bin/cctv/migrationurls", methods = "GET")
public class GetMigrationUrls extends SlingSafeMethodsServlet {
	private static final long serialVersionUID = -3960692666512058118L;
	private static final Logger log = LoggerFactory.getLogger(GetMigrationUrls.class);

    private PageManager pageManager;
    private ResourceResolver resourceResolver;
    private Integer urlCount = 0;
    private Integer maxUrlCount = 0;
    private StringBuilder urls;
    private Integer MAX_NUM_OF_URLS = 10000000;
    private String requestAssetType;

    private static final String RESOURCE_PATH_BASE = "sni-wcm/components/pagetypes/";
    private static final String SNI_ASSET_BASE_PATH = "/etc/sni-asset/";
    private static final String CONTENT_PAGE_BASE_PATH = "/content/cook/";

    private static final String ASSET_TYPE_RECIPE = "recipe";
    private static final String ASSET_TYPE_RECIPE_NUTRITION = "recipe-nutrition";
    private static final String ASSET_TYPE_SHOW = "show";
    private static final String ASSET_TYPE_EPISODE = "episode";
    private static final String ASSET_TYPE_SIMPLE_ARTICLE = "article-simple";
    private static final String ASSET_TYPE_TALENT = "talent";
    private static final String ASSET_TYPE_BIO = "bio";
    private static final String ASSET_TYPE_TALENT_RECIPES = "talent-recipes";
    private static final String ASSET_TYPE_SHOW_RECIPES = "show-recipes";
    private static final String ASSET_TYPE_RECIPE_REVIEWS = "recipe-reviews";
    private static final String ASSET_TYPE_PHOTO_GALLERY = "photo-gallery";
    private static final String ASSET_TYPE_CHANNEL = "video-channel";
    private static final String ASSET_TYPE_VIDEO = "video";
    private static final String ASSET_TYPE_PLAYER = "video-player";

    private static final String PAGE_RESOURCE_TYPE = "cq:Page";

    private static final String PROP_FASTFWD_URL = "sni:fastfwdUrl";
    private static final String PROP_SLING_RESOURCE_TYPE = "sling:resourceType";
    private static final String PROP_SNI_ASSET_LINK = "sni:assetLink";
    private static final String PROP_CONTENT_PAGE_PATH = "sni:pageLinks";

    private static final String RQ_PARAM_ASSET_TYPE = "at";
    private static final String RQ_PARAM_RESULT_COUNT = "rcnt";
    private static final String RQ_ASSET_TYPE_DEFAULT = "all";

    @Override
	protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Content-Type", "text/plain");

        log.info("::starting migrationurls servlet::");

        urls = new StringBuilder();

        //Setup the Asset Type(s) to be processed
        requestAssetType = request.getParameter(RQ_PARAM_ASSET_TYPE);
        if (requestAssetType == null || requestAssetType.length() == 0) {
            requestAssetType = RQ_ASSET_TYPE_DEFAULT;
        } else {
            requestAssetType = RESOURCE_PATH_BASE + requestAssetType;
        }

        //Setup the Max # of URLs to return
        String reqCount = request.getParameter(RQ_PARAM_RESULT_COUNT);
        if (reqCount != null && reqCount.length() > 0) {
            maxUrlCount = Integer.getInteger(reqCount);
        }

        //Determine if only published Assets should be processed
        //String reqShouldProcessPublished = request.getParameter(RQ_PARAM_RESULT_COUNT);
        //if (reqCount != null && reqCount.length() > 0) {
        //    maxUrlCount = Integer.getInteger(reqCount);
        //}

        //Setup the process
        pageManager = request.getResource().getResourceResolver().adaptTo(PageManager.class);
        resourceResolver = request.getResourceResolver();
        Resource startingPage = resourceResolver.getResource(CONTENT_PAGE_BASE_PATH);

        log.info("Starting to Process Recipes");
        walkRepository(startingPage);
        log.info("Finished processing Recipes");

        response.getOutputStream().print(urls.toString());

        log.info("::finished migrationurls servlet::");
	}

    /**
     * A method meant to be called recursively to process a resource and then walk down the children.
     *
     * @param targetResource   The resource to be processed
     */
    private void walkRepository(final Resource targetResource) {

        if (urlCount >= MAX_NUM_OF_URLS) {
            return;
        }

        processResource(targetResource);

        Iterator<Resource> childPages = targetResource.listChildren();
        while(childPages.hasNext() && urlCount < MAX_NUM_OF_URLS)
		{
			Resource childResource = childPages.next();

            walkRepository(childResource);
        }
	}

    /**
     * A method meant to process the resource passed in and print out the FastFwdURL and CQ URL for the resource
     *
     * @param targetResource   The resource to be processed
     */
    private void processResource(final Resource targetResource) {

        if (targetResource.getResourceType().equals(PAGE_RESOURCE_TYPE)) {
            Page targetPage = pageManager.getPage(targetResource.getPath());
            String pageType = targetPage.getProperties().get(PROP_SLING_RESOURCE_TYPE, "");
            String fastFwdUrl;
            String contentPageUrl;
            String sniAssetLink = "";
            Boolean isPageValid = isContentPageValid(targetResource);

            if (! isPageValid) {
                return;
            }

            if (pageType == null
                    || (!pageType.equalsIgnoreCase(requestAssetType)
                    && !requestAssetType.equalsIgnoreCase(RQ_ASSET_TYPE_DEFAULT))) {
                return;
            }

            fastFwdUrl = targetPage.getProperties().get(PROP_FASTFWD_URL, "");
            contentPageUrl = targetPage.getPath();

            if (fastFwdUrl == null || fastFwdUrl.length() == 0) {
                //Get Properties
                sniAssetLink = targetPage.getProperties().get(PROP_SNI_ASSET_LINK, "");

                if (sniAssetLink != null && sniAssetLink.length() > 0) {
                    Page sniPage = pageManager.getPage(sniAssetLink);
                    if (sniPage != null) {
                        fastFwdUrl = sniPage.getProperties().get(PROP_FASTFWD_URL, "");
                    }
                }
            }

            if (fastFwdUrl != null && fastFwdUrl.length() > 0 && contentPageUrl.length() > 0) {
                contentPageUrl = formatContentPageUrl(contentPageUrl);
                urls
                        .append(fastFwdUrl)
                        .append("\t")
                        .append(contentPageUrl)
                        .append("\t")
                        .append(sniAssetLink)
                        .append("\n");
                urlCount++;
            }
        }
    }

    /**
     * Insures the content page is activated.  No need to worry about pages that aren't published.
     *
     * @param contentResource   The path to the content page
     */
    private static Boolean isContentPageValid(final Resource contentResource) {
        ReplicationStatus contentReplicationStatus = contentResource.adaptTo(ReplicationStatus.class);

        return contentReplicationStatus.isActivated();
    }

    /**
     * Formats the Content Page Url to be as friendly as possible.
     *
     * @param contentPageUrl   The path to the content page that needs to be dressed up
     */
    private static String formatContentPageUrl(final String contentPageUrl) {
        return contentPageUrl + ".html";
    }
}
