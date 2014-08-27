package com.scrippsnetworks.wcm.fnr.servlets;

import com.scrippsnetworks.wcm.url.UrlMapper;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.*;
import org.apache.sling.settings.SlingSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.query.Query;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(label="SNI WCM Legacy Recipe Link Servlet",description="Links to recipes using legacy ids",enabled=true,immediate=true,metatype=false)
@Service(value=Servlet.class)
@Property(name="alias", value= LegacyRecipeLinkServlet.ALIAS)
public class LegacyRecipeLinkServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(LegacyRecipeLinkServlet.class);

    private static final String URL_TOKEN = "%URL%";
    private static final String RESPONSE_TEMPLATE = "<a class='recipe-source' target='_blank' href='" + URL_TOKEN + "'>Originally from FoodNetwork.com</a>";
    protected static final String PUBLISH_RUN_MODE = "publish";
    protected static final String PAGE_TYPE = "RECIPE";
    protected static final String RECIPES_ROOT = "/content/food/recipes";

    @SuppressWarnings("UnusedDeclaration")
    @Reference
    protected ResourceResolverFactory resourceResolverFactory;

    @SuppressWarnings("UnusedDeclaration")
    @Reference
    protected UrlMapper urlMapper;

    @SuppressWarnings("UnusedDeclaration")
    @Reference
    protected SlingSettingsService slingSettings;

    private static final Pattern REQUEST_PATTERN = Pattern.compile("/0,,([0-9]+),00.html");
    protected static final String ALIAS = "/food/cda/recipe_links";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        if (slingSettings.getRunModes().contains(PUBLISH_RUN_MODE) && path != null && path.charAt(0) == '/' && path.length() > 1) {
            Matcher m = REQUEST_PATTERN.matcher(path);
            if (m.matches()) {
                String id = m.group(1);
                String body = getResponseBody(id);
                if (body != null) {
                    response.getWriter().print(body);
                }
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private String getResponseBody(String id) {
        ResourceResolver resourceResolver = null;
        String body = null;
        if (id != null) {
            try {
                resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
                String url = getRecipeUrl(resourceResolver, id);
                if (url != null) {
                    body = RESPONSE_TEMPLATE.replace(URL_TOKEN, url);
                }
            } catch (Exception e) {
                logger.warn("could not retrieve recipe with id {}", id, e);
            } finally {
                if (resourceResolver != null) {
                    resourceResolver.close();
                }
            }
        }
        return body;
    }

    private String getRecipeUrl(ResourceResolver resourceResolver, String fastfwdId) {
        String pageUrl = null;
        if (resourceResolver != null) {
            String jcrPath = findAssetPathByLegacyTuple(resourceResolver, fastfwdId);
            if (jcrPath != null) {
                if (urlMapper != null) {
                    String mappedPath = urlMapper.map(resourceResolver, null, jcrPath);
                    if (mappedPath != null) {
                        pageUrl = mappedPath + (mappedPath.endsWith(".html") ? "" : ".html");
                    }
                }
            }
        }
        return pageUrl;
    }

    private String findAssetPathByLegacyTuple(ResourceResolver resourceResolver, String fastfwdId) {
        Iterator<Resource> resources;
        String path = null;
        StringBuilder querySb = new StringBuilder();
        querySb.append("select * from [cq:PageContent] as pageContent where ISDESCENDANTNODE(pageContent, \"")
        		.append(RECIPES_ROOT)
        		.append("\") ")
                .append("and pageContent.[sni:assetType] = \"")
                .append(PAGE_TYPE).append("\" ")
                .append("and pageContent.[sni:fastfwdId] = ")
                .append(fastfwdId)
                .append(".0");

        resources = resourceResolver.findResources(querySb.toString(), Query.JCR_SQL2);

        if (resources != null && resources.hasNext()) {
            path = resources.next().getParent().getPath();
        }

        return path;
    }




}
