package com.scrippsnetworks.wcm.fnr.servlets;

import com.scrippsnetworks.wcm.url.UrlMapper;

import java.io.IOException;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.query.Query;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.ComponentContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(enabled=true, immediate=true, metatype=true,
    label="SNI WCM Legacy Recipe CURL Filter Service",
    description="Recipes using legacy CURLs")
@Service(value=Filter.class)
public class LegacyRecipeCurlFilter implements Filter {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(LegacyRecipeCurlFilter.class);

    private static final String PAGE_TYPE = "RECIPE";
    private static final String RECIPES_ROOT = "/content/food/recipes";
    private static final String REQUEST_PATTERN_STRING = "^/[a-z]+/recipes/recipe/0,[0-9]*,[A-Z]+_9936_([0-9]+),00.html";
    private static final Pattern REQUEST_PATTERN = Pattern.compile(REQUEST_PATTERN_STRING);

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private UrlMapper urlMapper;

    private Integer redirectStatus = 301;

    @Property(label = "Filter pattern",
        description = "Regular expression determining requests that are handled. Set private due to problems with commas in config interface.",
        cardinality = 0,
        propertyPrivate = true,
        value = REQUEST_PATTERN_STRING)
    static final String PATTERN = "pattern";

    @Property(label = "Filter ranking",
        description = "Rank among other servlet filters. See the HTTP Whiteboard tab in the Felix console for a list of filters.",
        propertyPrivate = true,
        intValue = 1)
    static final String RANKING = "service.ranking";

    @Property(label = "Redirect Status",
        description = "HTTP Redirection Status Code.",
        intValue = 301,
        options = {
            @PropertyOption(name="301", value="301 Moved Permanently"),
            @PropertyOption(name="302", value="302 Moved Temporarily"),
            @PropertyOption(name="303", value="303 See Other HTTP/1.1")
        })
    static final String REDIRECT_STATUS = "redirect.status";

    private FilterConfig filterConfig;

    @Activate 
    protected void activate(ComponentContext ctx) {
        configure(ctx);
    }

    @Modified
    protected void modified(ComponentContext ctx) {
        configure(ctx);
    }

    public void destroy() {
        filterConfig = null;
    }

    public void init(FilterConfig pFilterConfig) throws ServletException {
        filterConfig = pFilterConfig;
    }

    public void configure(ComponentContext ctx) {
        Dictionary props = ctx.getProperties();
        redirectStatus = (Integer)props.get(REDIRECT_STATUS);
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse)servletResponse;

        LOG.debug("doFilter {}", httpRequest.getPathInfo());

        if (httpRequest.getMethod().equals("GET")) {
            String requestPath = httpRequest.getPathInfo();
            Matcher matcher = REQUEST_PATTERN.matcher(requestPath);
            if (matcher.matches()) {
                String mappedPath = getRecipeUrl(matcher.group(1));
                try {
                    if (mappedPath != null) {
                        httpResponse.setStatus(redirectStatus);
                        httpResponse.setHeader("Location", mappedPath);
                    } else {
                        httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "not found");
                    }
                } catch (Exception e) {
                    LOG.error("Got unexpected exception when sending " + mappedPath != null ? "redirect" : "not found", e);
                } finally {
                    return;
                }
            } else {
                return;
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private String getRecipeUrl(String fastfwdId) {
        String pageUrl = null;

        try {
            ResourceResolver resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
            if (resourceResolver != null) {
                String jcrPath = findContentPathByLegacyId(resourceResolver, fastfwdId);
                if (jcrPath != null && urlMapper != null) {
                    String mappedPath = urlMapper.map(resourceResolver, null, jcrPath);
                    if (mappedPath != null) {
                        pageUrl = mappedPath + (mappedPath.endsWith(".html") ? "" : ".html");
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Unable to query JCR for recipe URL.", e);
        }

        return pageUrl;
    }

    private String findContentPathByLegacyId(ResourceResolver resourceResolver, String fastfwdId) {
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
