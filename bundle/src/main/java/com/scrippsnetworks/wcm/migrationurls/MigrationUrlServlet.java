package com.scrippsnetworks.wcm.migrationurls;

import com.scrippsnetworks.wcm.fnr.util.PageSlingResourceTypes;
import com.scrippsnetworks.wcm.util.PagePropertyNames;
import com.scrippsnetworks.wcm.taglib.Functions;
import com.scrippsnetworks.wcm.taglib.TagUtils;
import com.scrippsnetworks.wcm.url.UrlMapper;
import com.scrippsnetworks.wcm.url.impl.UrlMapperImpl;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.Resource;

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Servlet to generate tab-delimited list of CQ URLs and their corresponding Fast Forward URLs
 * @author Jason Clark
 *         Date: 11/9/12
 */
@SlingServlet (paths = "/bin/fnr/getmigrationurls", methods = "GET")
public class MigrationUrlServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = -1L;
   	private static final Logger log = LoggerFactory.getLogger(GetMigrationUrls.class);

    private static final String EMPTY_STRING = "";
    private static final String FORWARD_SLASH = "/";
    private static final String COMMA = ",";
    private static final String TAB = "\t";
    private static final String NEW_LINE = "\n";
    private static final String FALSE = "false";
    private static final String TRUE = "true";

    private static final String REQUEST_PARAMETER_ASSET_TYPE = "type";
    private static final String REQUEST_PARAMETER_VERBOSE = "verbose";
    private static final String REQUEST_PARAMETER_ALL_CQ = "all"; //this param indicates you want all urls, cq only

    private static final String UNPUBLISHED = "Unpublished";

    private static final String NUTRITION_OID_PREFIX = "/recipes/nutrition/0,1001123,FOOD_41503_RECIPE-";
    private static final String NUTRITION_OID_SUFFIX = ",00.html";
    private static final String EPISODE_ARCHIVE_OID_PREFIX = "/episode_archive/0,1001125,FOOD_42042_";
    private static final String EPISODE_ARCHIVE_OID_SUFFIX = ",00.html";
    private static final String REVIEWS_URL_SUFFIX = "/reviews/index.html";
    private static final String RECIPES_URL_SUFFIX = "/recipes/index.html";

    private static final String TWO_UP_JCR_CONTENT = "../../jcr:content";
    private static final String SLASH_JCR_CONTENT = "/jcr:content";
    private static final String SLASH_NUTRITION = "/nutrition";
    private static final String CQ_PAGE_CONTENT = "cq:PageContent";
    private static final String CONTENT_ROOT_PATH = "/content/food";

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Content-Type", "text/plain");

        log.info("::starting getmigrationurls servlet::");

        String assetTypeParameter = request.getParameter(REQUEST_PARAMETER_ASSET_TYPE);
        String verbose = request.getParameter(REQUEST_PARAMETER_VERBOSE);
        String allUrls = request.getParameter(REQUEST_PARAMETER_ALL_CQ);

        boolean isVerbose = StringUtils.isNotEmpty(verbose) && !verbose.equalsIgnoreCase(FALSE);
        boolean isAllUrls = StringUtils.isNotEmpty(allUrls) && allUrls.equalsIgnoreCase(TRUE) && !isVerbose;

        try {
            ResourceResolver resolver = request.getResourceResolver();
            Resource resource = resolver.getResource(CONTENT_ROOT_PATH);

            QueryManager manager = resource.adaptTo(Node.class)
                    .getSession().getWorkspace().getQueryManager();

            Query compiledQuery = manager.createQuery(buildQuery(assetTypeParameter), Query.JCR_SQL2);

            NodeIterator nodeIterator = compiledQuery.execute().getNodes();

            StringBuilder urls = new StringBuilder();
            UrlMapper mapper = new UrlMapperImpl();

            log.info("::beginning iteration of getmigrationurl found nodes::");

            while (nodeIterator.hasNext()) {
                Node node = nodeIterator.nextNode();
                if (!node.hasProperty(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName())) {
                    continue;
                }
                String publishStatus;
                if (node.hasProperty(PagePropertyNames.CQ_LAST_REPLICATION_ACTION.propertyName())) {
                    publishStatus = node
                            .getProperty(PagePropertyNames.CQ_LAST_REPLICATION_ACTION.propertyName()).getString();
                } else {
                    publishStatus = UNPUBLISHED;
                }
                String pagePath = Functions.getBasePath(node.getPath());
                String resourceType = node
                        .getProperty(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName()).getString();
                String cqUrl = TagUtils.completeHREF(pagePath);
                String friendlyUrl = mapper.map(request, cqUrl).replaceFirst(CONTENT_ROOT_PATH, EMPTY_STRING);
                String fastFwdUrl = EMPTY_STRING;
                String sniAssetPath = EMPTY_STRING;

                if (!isAllUrls) {
                    //determine if page is "generated" type, e.g. talent top recipes or recipe reviews etc
                    PageSlingResourceTypes generatedType = getGeneratedType(resourceType);
                    if (generatedType != null) {
                        fastFwdUrl = getGeneratedTypeUrl(node, generatedType);
                        if (StringUtils.isBlank(fastFwdUrl)) {
                            continue;
                        }
                    } else if (node.hasProperty(PagePropertyNames.SNI_FASTFWD_URL.propertyName())) {
                        //fast fwd url is right on the page
                        fastFwdUrl = node.getProperty(PagePropertyNames.SNI_FASTFWD_URL.propertyName()).getString();
                    } else if (node.hasProperty(PagePropertyNames.SNI_ASSET_LINK.propertyName())) {
                        //fast fwd url is buried in the asset
                        sniAssetPath = node.getProperty(PagePropertyNames.SNI_ASSET_LINK.propertyName()).getString();
                        String assetContentPath = sniAssetPath + SLASH_JCR_CONTENT;
                        Resource assetResource = resolver.getResource(assetContentPath);
                        if (assetResource == null) {
                            continue;
                        }
                        Node assetNode = assetResource.adaptTo(Node.class);
                        if (assetNode.hasProperty(PagePropertyNames.SNI_FASTFWD_URL.propertyName())) {
                            fastFwdUrl = assetNode
                                    .getProperty(PagePropertyNames.SNI_FASTFWD_URL.propertyName()).getString();
                        } else {
                            continue;
                        }
                    } else {
                        //only got here if there is nothin' to do!
                        continue;
                    }
                }

                if (isVerbose) {
                    urls
                            .append(fastFwdUrl)
                            .append(TAB)
                            .append(cqUrl)
                            .append(TAB)
                            .append(friendlyUrl)
                            .append(TAB)
                            .append(sniAssetPath)
                            .append(TAB)
                            .append(publishStatus)
                            .append(TAB)
                            .append(resourceType)
                            .append(NEW_LINE);
                } else if (isAllUrls) {
                    urls
                            .append(friendlyUrl)
                            .append(TAB)
                            .append(resourceType)
                            .append(TAB)
                            .append(publishStatus)
                            .append(NEW_LINE);
                } else {
                    urls
                            .append(fastFwdUrl)
                            .append(TAB)
                            .append(friendlyUrl)
                            .append(NEW_LINE);
                }
            }

            response.getOutputStream().print(urls.toString());

            log.info("::finished getmigrationurls servlet::");

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * create a query
     * @param requestParams String parameter to use as sling:resourceType for query
     * @return String formatted query
     */
    private String buildQuery(final String requestParams) {
        StringBuilder query = new StringBuilder();

        query
                .append("select * from [nt:base] as s where isdescendantnode([")
                .append(CONTENT_ROOT_PATH)
                .append("]) and s.[")
                .append(PagePropertyNames.JCR_PRIMARY_TYPE.propertyName())
                .append("] = '")
                .append(CQ_PAGE_CONTENT)
                .append("'");

        if (StringUtils.isNotBlank(requestParams)) {
            log.info("::request params - " + requestParams + " - ::");
            String[] assetTypes = requestParams.split(COMMA);
            for (int i = 0; i < assetTypes.length; i++) {
                String resourceType = PageSlingResourceTypes.PAGE_TYPE_ROOT.resourceType()
                        + FORWARD_SLASH + assetTypes[i];
                if (i == 0) {
                    query
                            .append(" and (s.[sling:resourceType] = '")
                            .append(resourceType)
                            .append("'");
                } else {
                    query
                            .append(" or s.[sling:resourceType] = '")
                            .append(resourceType)
                            .append("'");
                }
            }
            query.append(")");
        } else {
            query.append(" and s.[sling:resourceType] is not null");
        }

        query.append(" order by s.[sling:resourceType]");

        return query.toString();
    }

    /**
     * Check if a resourceType is a generated type of page (like top-recipes, etc), return type
     * @param resourceType String sling:resourceType to check against generated types list
     * @return PageSlingResourceTypes or null
     */
    private static PageSlingResourceTypes getGeneratedType(final String resourceType) {
        EnumSet<PageSlingResourceTypes> typeEnumSet = PageSlingResourceTypes.GENERATED_TYPES;
        for (PageSlingResourceTypes type : typeEnumSet) {
            if (type.resourceType().equals(resourceType)) {
                return type;
            }
        }
        return null;
    }

    /**
     * If you have a generated page type (e.g. top-recipes) get the fast fwd URL for that type
     * @param node Node for the page you're on
     * @param type PageSlingResourceTypes Enum containing page type
     * @return String fast fwd url
     */
    private String getGeneratedTypeUrl(final Node node, final PageSlingResourceTypes type) {
        String generatedPageTypeUrl;
        switch (type) {
            case SHOW_TOP_RECIPES:
            case TALENT_TOP_RECIPES:
                generatedPageTypeUrl = topRecipeUrl(node);
                break;
            case RECIPE_NUTRITION:
                generatedPageTypeUrl = nutritionUrl(node);
                break;
            case RECIPE_REVIEWS:
                generatedPageTypeUrl = reviewsUrl(node);
                break;
            case EPISODE_ARCHIVE:
                generatedPageTypeUrl = episodeArchiveUrl(node);
                break;
            default:
                generatedPageTypeUrl = EMPTY_STRING;
                break;
        }
        return generatedPageTypeUrl;
    }

    /**
     * Get formatted fast fwd url for nutrition pages
     * @param node Node of page you're on
     * @return String formatted fast fwd URL
     */
    private String nutritionUrl(final Node node) {
        boolean hasNutrients = false;
        try {
            Node recipeContentNode = node.getNode(TWO_UP_JCR_CONTENT);
            if (recipeContentNode.hasProperty(PagePropertyNames.SNI_ASSET_LINK.propertyName())) {
                String assetPath = recipeContentNode
                        .getProperty(PagePropertyNames.SNI_ASSET_LINK.propertyName()).getString();
                Node nutritionDataNode = node.getSession()
                        .getNode(assetPath + SLASH_JCR_CONTENT + SLASH_NUTRITION);
                if (nutritionDataNode.hasNodes()) {
                    hasNutrients = true;
                }
            }
        } catch (PathNotFoundException e) {
            log.error("PathNotFoundException in nutritionUrl::" + e.getMessage());
        } catch (RepositoryException e) {
            log.error("RepositoryException in nutritionUrl::" + e.getMessage());
        }
        if (hasNutrients) {
            return formatOidUrl(node, NUTRITION_OID_PREFIX, NUTRITION_OID_SUFFIX);
        } else {
            return EMPTY_STRING;
        }
    }

    /**
     * Get formatted fast fwd url for recipe reviews page
     * @param node Node of page you're on
     * @return String formatted fast fwd URL
     */
    private String reviewsUrl(final Node node) {
        return formatFriendlyUrl(node, REVIEWS_URL_SUFFIX);
    }

    /**
     * Get formatted fast fwd URL for episode archive pages
     * @param node Node of page you're on
     * @return Formatted fast fwd URL
     */
    private String episodeArchiveUrl(final Node node) {
        return formatOidUrl(node, EPISODE_ARCHIVE_OID_PREFIX, EPISODE_ARCHIVE_OID_SUFFIX);
    }

    /**
     * Format fast fwd urls for show/talent top recipe pages
     * @param node Node for the page you're on
     * @return String formatted fast fwd URL
     */
    private String topRecipeUrl(final Node node) {
        return formatFriendlyUrl(node, RECIPES_URL_SUFFIX);
    }

    /**
     * Format fast fwd URLs for pages with a friendly url structure
     * @param node Node for the page you're on
     * @param urlSuffix String for the path to the page you want, e.g. "/reviews/index.html"
     * @return String formatted fast fwd URL
     */
    private String formatFriendlyUrl(final Node node, final String urlSuffix) {
        StringBuilder fastFwdUrl = new StringBuilder();
        try {
            Node parentPageContent = node.getNode(TWO_UP_JCR_CONTENT);
            if (parentPageContent.hasProperty(PagePropertyNames.SNI_FASTFWD_URL.propertyName())) {
                String parentFastFwdUrl = parentPageContent
                        .getProperty(PagePropertyNames.SNI_FASTFWD_URL.propertyName()).getString();
                fastFwdUrl.append(parentFastFwdUrl.replaceFirst("/index\\.html", urlSuffix));
            }
        } catch (PathNotFoundException e) {
            log.error(e.getMessage());
        } catch (RepositoryException e) {
            log.error(e.getMessage());
        }
        return fastFwdUrl.toString();
    }

    /**
     * For formatting the fast forward urls for page types that use OIDs
     * @param node Node for the page you're on
     * @param prefix String first part of URL (should never change for that page type)
     * @param suffix String last part of URL (should never change for that page type)
     * @return String formatted fast fwd URL
     */
    private String formatOidUrl(final Node node, final String prefix, final String suffix) {
        StringBuilder fastFwdUrl = new StringBuilder();
        try {
            Node parentPageContent = node.getNode(TWO_UP_JCR_CONTENT);
            if (parentPageContent.hasProperty(PagePropertyNames.SNI_FASTFWD_ID.propertyName())) {
                String fastfwdId = parentPageContent
                        .getProperty(PagePropertyNames.SNI_FASTFWD_ID.propertyName()).getString();
                if (StringUtils.isNotBlank(fastfwdId)) {
                    fastFwdUrl
                            .append(prefix)
                            .append(fastfwdId)
                            .append(suffix);
                }
            }
        } catch (PathNotFoundException e) {
            log.error(e.getMessage());
        } catch (RepositoryException e) {
            log.error(e.getMessage());
        }
        return fastFwdUrl.toString();
    }

}
