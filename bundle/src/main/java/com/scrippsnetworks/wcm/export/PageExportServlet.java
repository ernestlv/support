package com.scrippsnetworks.wcm.export;

import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.servlet.Servlet;
import javax.jcr.query.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import com.scrippsnetworks.wcm.export.snipage.ExportWriter;
import com.scrippsnetworks.wcm.export.snipage.PageExportException;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;

import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;

/* In case we want to make this thing a Sling servlet.
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
*/


import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.wcm.api.Page;

import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Produces export xml for pages.
 *
 */
@Component(label="SNI WCM Page Export Servlet",enabled=true,immediate=true,metatype=false)
@Service(Servlet.class)
@Property(name="alias", value=PageExportServlet.ALIAS)
public class PageExportServlet extends HttpServlet {
 
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(PageExportServlet.class);

    public static final String ALIAS = "/services/export";
    
    /** Default Page path. */
    public static final String DEFAULT_PAGE_PATH = "/content/food";

    @Reference
    protected ResourceResolverFactory resourceResolverFactory; // protected for test, could use bind method, but IDE complains

    private static final Pattern uidPattern = Pattern.compile("^/id/([a-zA-Z0-9-]+)[.]xml$");

    private static final Pattern legacyPattern = Pattern.compile("^/brand/([a-zA-Z]+)/pageType/([a-zA-Z_-]+)/fastfwdId/([0-9]+)[.]xml$");
	
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        if (path != null && path.charAt(0) == '/' && path.length() > 1) {
            switch (path.charAt(1)) {
                case 'i':
                    doExport(request, response);
                    break;
                case 'b':
                    doRedirect(request, response);
                    break;
                default:
                    logger.debug("cannot determine request type from {}", path);
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void doExport(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        Matcher m = uidPattern.matcher(path);
        String id;

        if (m.matches()) {
            id = m.group(1);
        } else {
            logger.debug("path {} did not match pattern {}", path, uidPattern.pattern());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        ResourceResolver resourceResolver = null;
        try {
            resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
            if (resourceResolver != null) {
                Page page = findPageById(resourceResolver, id);
                if (page != null && page.hasContent()) {
                    SniPage sniPage = PageFactory.getSniPage(page);
                    ExportWriter.writeExportXml(sniPage, response.getWriter());
                } else {
                    logger.debug("page not found with assetUId {}", id);
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                // Mostly for the benefit of tests. If a ResourceResolver couldn't be acquired, Sling would throw an exception.
                throw new ServletException("could not acquire ResourceResolver");
            }
        } catch (PageExportException e) {
            throw new ServletException(e);
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            if (resourceResolver != null) {
                resourceResolver.close();
            }
        }

    }

    private void doRedirect(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        Matcher m = legacyPattern.matcher(path);
        String brand;
        String type;
        String fastfwdId;

        if (m.matches()) {
            brand = m.group(1);
            type = m.group(2);
            fastfwdId = m.group(3);
        } else {
            logger.debug("path {} did not match pattern {}", path, legacyPattern.pattern());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        ResourceResolver resourceResolver = null;
        try {
            resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
            String assetUid = findAssetUidByLegacyTuple(resourceResolver, brand, type, Integer.valueOf(fastfwdId));
            if (assetUid != null) {
                logger.debug("could not find assetUid for page using legacy tuple");
                response.sendRedirect(request.getServletPath() + "/id/" + assetUid + ".xml");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            if (resourceResolver != null) {
                resourceResolver.close();
            }
        }
    }

    private Page findPageById(ResourceResolver resourceResolver, String id) throws ServletException {
        Iterator<Resource> resources;
        Resource pageResource;
        Page page = null;

        String xpath = "/jcr:root/content//element(*, cq:Page)[@jcr:content/sni:assetUId='" + id + "']";
        try {
            //noinspection deprecation
            resources = resourceResolver.findResources(xpath, Query.XPATH);
        } catch (Exception e) {
            throw new ServletException("Error searching for page", e);
        }

        if (resources != null && resources.hasNext()) {
            pageResource = resources.next();
            page = pageResource.adaptTo(Page.class);
        }

        return page;
    }

    private String findAssetUidByLegacyTuple(ResourceResolver resourceResolver, String brand, String pageType, Integer fastfwdId) throws ServletException {
        Iterator<Resource> resources;
        Resource pageResource;
        String assetUid = null;
        StringBuilder querySb = new StringBuilder();
        querySb.append("select * from [cq:PageContent] as pageContent where ISDESCENDANTNODE(pageContent, \"")
        		.append(DEFAULT_PAGE_PATH)
        		.append("\") ")
                .append("and pageContent.[sni:assetType] = \"")
                .append(pageType).append("\" ")
                .append("and pageContent.[sni:fastfwdId] = ")
                .append(String.valueOf(fastfwdId))
                .append(".0");
        
        try {
            resources = resourceResolver.findResources(querySb.toString(), Query.JCR_SQL2);
        } catch (Exception e) {
            throw new ServletException("Error searching for page", e);
        }

        if (resources != null && resources.hasNext()) {
            pageResource = resources.next();
            ValueMap properties = ResourceUtil.getValueMap(pageResource);
            if (properties != null) {
                assetUid = properties.get("sni:assetUId", String.class);
            }
        }

        return assetUid;
    }

}
