package com.scrippsnetworks.wcm.snipackage.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import javax.servlet.ServletException;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.SlingException;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import com.day.cq.wcm.api.WCMMode;
import com.scrippsnetworks.wcm.snipackage.SniPackage;

import com.scrippsnetworks.wcm.util.SelectionOption;

/** Servlet responsible for returning available page types to use as keys when sharing package regions.
 *
 * It's possible this could just be a generic servlet returning templates for the current site, but it will be easier to
 * contextually filter the returned data if this selector is specific to pages.
 */
@SlingServlet(
    resourceTypes = { "sling/servlet/default" },
    selectors = { "packageregionoptions" },
    extensions = { "json" },
    methods = { "GET" }
)
public class PackageRegionOptionsServlet extends SlingSafeMethodsServlet
{
    public static final Set<String> blacklist = new HashSet<String>();
    static {
        blacklist.add("robot");
        blacklist.add("error-handler");
        blacklist.add("free-form-text");
    }

    String resPath = null;
    String brand = null;
    
    @Reference
    ResourceResolverFactory resourceResolverFactory;

    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws ServletException, IOException
    {
        this.resPath = request.getRequestPathInfo().getResourcePath();
        if ((this.resPath == null) || (!this.resPath.startsWith("/content"))) {
            response.sendError(SlingHttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (WCMMode.fromRequest(request) != WCMMode.EDIT) {
            response.sendError(SlingHttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        String[] pathElements = this.resPath.split("/");
        if (pathElements.length < 3) {
            throw new ServletException("Not enough path elements");
        }
        
        this.brand = pathElements[2];
        String templatesPath = "/apps/sni-" + this.brand + "/templates/pagetypes";
        
        Resource templates = null;
        ResourceResolver resourceResolver = null;
        List<SelectionOption> list = new ArrayList<SelectionOption>();
        list.add(new SelectionOption(SniPackage.WILDCARD_PAGE_TYPE, "Any", "Region for pages in the package"));
        try {
            resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
            templates = resourceResolver.getResource(templatesPath);
            if (templates != null) {
                Iterator<Resource> iter = templates.listChildren();
                while (iter.hasNext()) {
                    Resource r = iter.next();
                    String name = r.getName();
                    if (blacklist.contains(name)) {
                        continue;
                    }
                    String text = WordUtils.capitalizeFully(name.replace("-", " ").replace("_", " ").toLowerCase());
                    list.add(new SelectionOption(name, text, "Region for " + text + " pages in the package"));
                }
            }
        } catch (LoginException e) {
            throw new ServletException("Exception getting resource resolver", e);
        } catch (SlingException e) {
            throw new ServletException("Error loading templates resource", e);   
        } finally {
            if (resourceResolver != null) {
                resourceResolver.close();
            }
        }

        if (templates == null) {
            throw new ServletException("could not retrieve templates resource at " + templatesPath);
        }

        try
        {
            PrintWriter printWriter = response.getWriter();
            writeOptions(printWriter, list);
        } catch (Exception e) {
            throw new ServletException(e.getClass().getName() + " " + e.getMessage());
        }
    }

    private void writeOptions(PrintWriter writer, List<SelectionOption> options) throws JSONException, IOException
    {
        if (writer == null) {
            throw new RuntimeException("must pass nonnull writer to writeOutput");
        }

        if (options == null) {
            options = new ArrayList<SelectionOption>();
        }

        JSONWriter json = new JSONWriter(writer);
        json.array();

        for (SelectionOption opt : options) {
            json.object();
            json.key("value").value(opt.getName());
            json.key("text").value(opt.getText());
            json.key("qtip").value(opt.getQtip());
            json.endObject();
        }

        json.endArray();
    }
}
