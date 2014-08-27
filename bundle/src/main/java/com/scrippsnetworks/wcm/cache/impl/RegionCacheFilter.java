package com.scrippsnetworks.wcm.cache.impl;

import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentContext;
import com.scrippsnetworks.wcm.cache.RegionCacheService;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;

@org.apache.felix.scr.annotations.Component
@Service(Filter.class)
@Properties({
        @Property(name = "sling.filter.scope", value = "INCLUDE"),
        @Property(name = "service.ranking", intValue = 2)
})
public class RegionCacheFilter implements javax.servlet.Filter {

    @Reference
    RegionCacheService regionCacheService;

    private final static Logger log = LoggerFactory.getLogger(RegionCacheFilter.class);

    private static final String SNI_CACHEABLE = "cacheable";


    @Override
    public void init(FilterConfig paramFilterConfig) throws ServletException {
        log.debug("in RegionCacheFilter init");
    }

    @Override
    public void destroy() {
        log.debug("in RegionCacheFilter destroy");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        SlingHttpServletRequest req = (SlingHttpServletRequest) request;
        SlingHttpServletResponse resp = (SlingHttpServletResponse) response;

        String reqResPath = req.getRequestPathInfo().getResourcePath();

        WCMMode wcmMode = WCMMode.fromRequest(request);
        boolean inEditMode = wcmMode == WCMMode.EDIT || wcmMode == WCMMode.DESIGN;

        if (inEditMode || reqResPath == null || reqResPath.isEmpty() || !StringUtils.contains(reqResPath,"/regions/")) {
            if (inEditMode) {
                log.debug("skipping {} in EDIT mode", reqResPath);
            } else {
                log.debug("skipping {}, not under root {}", reqResPath, "/regions/");
            }
            filterChain.doFilter(request, response);
            return;
        }

        Resource resource = req.getResource();

        // exit early if no resource
        if (resource == null) {
            log.debug("resource was null, returning");
            filterChain.doFilter(request, response);
            return;
        }

        String resourcePath = resource.getPath();

        ComponentContext componentContext = (ComponentContext)req.getAttribute(ComponentContext.CONTEXT_ATTR_NAME);
        Component component = componentContext.getComponent();

        // exit early if component can't be resolved
        if(component == null
                || component.getResourceType() == null) {
            log.debug("component in componentContext was null, returning");
            filterChain.doFilter(request, response);
            return;

        }


        String resourceTypePath = component.getPath();
        // exit early if no resourcetype path defined
        if (resourceTypePath == null) {
            log.debug("resourcetypepath was null, returning");
            filterChain.doFilter(request, response);
            return;
        }

        // if this is a relative path to an sni component, we need to make it an absolute path
        if (resourceTypePath.startsWith("sni-")) {
            resourceTypePath = "/apps/" + resourceTypePath;
        }

        log.debug("resourcetype path is:" + resourceTypePath);

        if (!resourceTypePath.startsWith("/apps/sni-")) {
            // this isn't an sni component, we won't be caching it, return early
            log.debug(String.format("component at path %s is a %s component NOT an sni component exiting", resourcePath, resourceTypePath));
            filterChain.doFilter(request, response);
            return;
        }


        ValueMap properties = component.getProperties();

        // if the valuemap couldn't be adapted, exit early
        if (properties == null) {
            log.debug(String.format("component at path %s couldn't locate properties for resource %s", resourcePath, resourceTypePath));
            filterChain.doFilter(request, response);
            return;
        }

        // if it isn't a cachable component, exit
        if (!properties.containsKey(SNI_CACHEABLE)) {
            log.debug(String.format("component at path %s is not cacheable %s", resourcePath, resourceTypePath));
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // this is a component we want to cache
            log.debug(String.format("component resourcetype at path %s is cachable", resourcePath));

            String renderedComponent = regionCacheService.get(resourcePath);

            if (renderedComponent == null) {
                //we need to render this component and store it in cache
                log.debug(String.format("component at path %s was NOT cached", resourcePath));
                OutputResponseWrapper wrapper = new OutputResponseWrapper(resp);
                filterChain.doFilter(req, wrapper);
                renderedComponent = wrapper.toString();
                regionCacheService.put(resourcePath, renderedComponent);
            } else {
                log.debug(String.format("component at path %s was cached", resourcePath));
            }
            // output the component html into the response writer
            resp.getWriter().write(renderedComponent);
        } catch (Exception ex) {
            //fail gracefully, we still want to render the component
            log.error(ex.getMessage(), ex);
            filterChain.doFilter(request, response);
        }
    }

}
