package com.scrippsnetworks.wcm.analytics;


import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.webservicesupport.Configuration;
import com.day.cq.wcm.webservicesupport.ConfigurationConstants;
import com.day.cq.wcm.webservicesupport.ConfigurationManager;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.analytics.sitecatalyst.Framework;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import com.scrippsnetworks.wcm.cache.SitecatalystFrameworkCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import javax.servlet.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@org.apache.felix.scr.annotations.Component
@Service(Filter.class)
@Properties({
        @Property(name="sling.filter.scope", value="COMPONENT"),
        @Property(name="service.ranking", intValue=1)
})
public class AnalyticsAnnotationFilter implements Filter {

    public static final String SITECATALYST_SERVICE_NAME = "sitecatalyst";
    public static final String ANNOTATION_CLASS = "clicktracking";
    public static final String FILTER_ROOT = "/content";
    public static final String ANALYTICS_NODE = "analytics";
    public static final String PROP_TRACKEVENTS = "cq:trackevents";
    public static final String TRACKEVENTS_PATH = ANALYTICS_NODE + "/" + PROP_TRACKEVENTS;
    public static final String ANNOTATION_TAG_NAME = "span";

    public static Set<String> Events = new HashSet<String>();
    static {
        Events.add("linkclicked");
    }

    public enum DataAttributes {
        componentPath("data-component-path"),
        resourcePath("data-resource-path"),
        resourceType("data-resource-type");

        final String attrName;

        DataAttributes(String attrName) {
            this.attrName = attrName;
        }

        public String getAttrName() {
            return attrName;
        }
    }

    @Reference
    ConfigurationManager configurationManager;

    @Reference
    SitecatalystFrameworkCacheService sitecatalystFrameworkCacheService;

    private final Logger log;

    public AnalyticsAnnotationFilter() {
        this.log = LoggerFactory.getLogger(AnalyticsAnnotationFilter.class);
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException
    {
        WCMMode mode = WCMMode.fromRequest(request);
        if (WCMMode.EDIT.equals(mode)) {
            log.debug("declining to annotate in edit mode");
            filterChain.doFilter(request, response);
            return;
        }

        // The WCMComponentFilter does this, presumably for a reason. In that case, there would have been no
        // prologue or epilogue written, so annotation wouldn't work. Probably best to respect this.
        if (request.getAttribute(ComponentContext.BYPASS_COMPONENT_HANDLING_ON_INCLUDE_ATTRIBUTE) != null) {
            log.debug("skipping resource, bypass attribute set");
            filterChain.doFilter(request, response);
            return;
        }

        SlingHttpServletRequest req = (SlingHttpServletRequest)request;
        SlingHttpServletResponse resp = (SlingHttpServletResponse)response;

        String reqResPath = req.getRequestPathInfo().getResourcePath();
        if (reqResPath == null || reqResPath.isEmpty() || !reqResPath.startsWith(FILTER_ROOT)) {
            log.debug("skipping {}, not under root {}", reqResPath, FILTER_ROOT);
            filterChain.doFilter(request, response);
            return;
        }

        ComponentContext componentContext = (ComponentContext)req.getAttribute(ComponentContext.CONTEXT_ATTR_NAME);
        if (componentContext == null || componentContext.isRoot()) {
            log.debug("skipping {}, componentContext is null or root", reqResPath);
            filterChain.doFilter(request, response);
            return;
        }

        if (!componentContext.hasDecoration() ||
                (componentContext.getDecorationTagName() != null && componentContext.getDecorationTagName().equals(""))) {
            log.debug("skipping {}, component not decorated", reqResPath);
            filterChain.doFilter(request, response);
            return;
        }

        Page page = componentContext.getPage();
        // If the resource isn't on a page, skip.
        if (page == null || !page.hasContent()) {
            log.debug("skipping {}, could not get page from component context", reqResPath);
            filterChain.doFilter(request, response);
            return;
        }

        if (componentContext.getComponent() == null
                || componentContext.getComponent().getResourceType() == null
                || page.getContentResource() == null
                || page.getContentResource().getResourceType() == null) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // If the context component resource type is the same as the page's, skip.
        // We're assuming that 1) if we got this far rendering a page it *must* have a content resource and type; and
        // 2) If we have a component context, it must have a component.
        if (page.getContentResource().getResourceType().equals(componentContext.getComponent().getResourceType())) {
            log.debug("skipping {}, resource type {} same as page", reqResPath, componentContext.getComponent().getResourceType());
            filterChain.doFilter(request, response);
            return;
        }

        Resource requestResource = req.getResource();
        if (requestResource == null) {
            log.debug("skipping {}, could not get resource from request", reqResPath);
            filterChain.doFilter(request, response);
            return;
        }

        Resource contextResource = componentContext.getResource();
        if (contextResource == null) {
            log.debug("skipping {}, could not get resource from component context", reqResPath);
            filterChain.doFilter(request, response);
            return;
        }

        if (!requestResource.getPath().equals(contextResource.getPath())) {
            log.debug("skipping {}, context and request resource paths differ", reqResPath);
            filterChain.doFilter(request, response);
            return;
        }

        InheritanceValueMap pageProperties = new HierarchyNodeInheritanceValueMap(page.getContentResource());
        String[] services = pageProperties.getInherited(ConfigurationConstants.PN_CONFIGURATIONS, new String[]{});
        Configuration configuration = configurationManager.getConfiguration(SITECATALYST_SERVICE_NAME, services);
        if (configuration == null) {
            log.debug("skipping {}, no analytics configuration found", reqResPath);
            filterChain.doFilter(request, response);
            return;
        }

        Framework framework = getFramework(req, configuration);
        Set<String> frameworkComponents;
        if (framework != null) {
            frameworkComponents = framework.getAllComponents().keySet();
        } else {
            log.debug("no framework found for configuration {}", configuration.getPath());
            filterChain.doFilter(request, response);
            return;
        }

        if (frameworkComponents.size() == 0) {
            log.debug("no framework components found");
            filterChain.doFilter(request, response);
            return;
        }

        Component analyticsComponent = null;
        Component walker = componentContext.getComponent();
        while (walker != null) {
            String walkerResType = walker.getResourceType();
            log.debug("checking resource type {} in frameworkComponents", walkerResType);
            if (walkerResType != null) {
                if (frameworkComponents.contains(walkerResType)) {
                    analyticsComponent = walker;
                    break;
                }
            }
            walker = walker.getSuperComponent();
        }

        if (analyticsComponent == null) {
            log.debug("skipping {}, no analytics component for {} or supertype found", reqResPath, componentContext.getComponent().getResourceType());
            filterChain.doFilter(request, response);
            return;
        }

        String trackEvents = analyticsComponent.getProperties().get(TRACKEVENTS_PATH, String.class);
        if (trackEvents == null || trackEvents.isEmpty()) {
            log.debug("no trackEvents on analytics component {}", analyticsComponent.getPath());
            filterChain.doFilter(request, response);
            return;
        }

        boolean tracksEvent = false;
        for (String event : trackEvents.split(",")) {
            tracksEvent = Events.contains(event.trim());
            if (tracksEvent) {
                break;
            }
        }

        if (!tracksEvent) {
            log.debug("skipping {}, analytics component {} does not track click event", reqResPath, analyticsComponent.getPath());
            filterChain.doFilter(request, response);
            return;
        }

        try {
            PrintWriter out = resp.getWriter();
            StringBuilder sb = new StringBuilder();
            sb.append("<").append(ANNOTATION_TAG_NAME).append(" style=\"display: none\" class=\"");
            sb.append(ANNOTATION_CLASS);
            sb.append("\" ").append(DataAttributes.componentPath.getAttrName()).append("=\"");
            sb.append(analyticsComponent.getResourceType());
            sb.append("\" ").append(DataAttributes.resourcePath.getAttrName()).append("=\"");
            sb.append(requestResource.getPath());
            sb.append("\" ").append(DataAttributes.resourceType.getAttrName()).append("=\"");
            sb.append(requestResource.getResourceType());
            sb.append("\"></").append(ANNOTATION_TAG_NAME).append(">");
            out.write(sb.toString());
        } catch (IOException e) {
          log.warn("IOException writing analytics annotation");
        } finally {
            filterChain.doFilter(request, response);
        }
    }

    private Framework getFramework(SlingHttpServletRequest request, Configuration configuration) {
        if (configuration == null) {
            return null;
        }
        String key = Framework.class.getName();
        Framework framework = (Framework) request.getAttribute(key);
        if (framework == null) {
            if (sitecatalystFrameworkCacheService != null) {
                log.debug("aquiring framework from cache service");
                framework = sitecatalystFrameworkCacheService.getFramework(configuration.getResource().getPath() + "/jcr:content");
            } else {
                Resource configurationResource = configuration.getResource();
                if (configurationResource != null) {
                    Resource configurationContentResource = configurationResource.getChild(JcrConstants.JCR_CONTENT);
                    if (configurationContentResource != null) {
                        framework = configurationContentResource.adaptTo(Framework.class);
                        if (framework != null) {
                            request.setAttribute(key, framework);
                            log.debug("setting framework request attribute");
                        }
                    }
                }
            }

            if (framework != null) {
                request.setAttribute(key, framework);
                log.debug("setting framework request attribute");
            }
        } else {
            log.debug("reusing framework from request");
        }
        return framework;
    }

}
