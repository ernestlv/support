package com.scrippsnetworks.wcm.url.impl;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentException;
import org.osgi.framework.Constants;

import org.apache.felix.scr.annotations.*;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.lang.String;
import java.lang.Integer;
import java.util.Dictionary;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.LoginException;

import com.day.cq.commons.Externalizer;
import org.apache.sling.settings.SlingSettingsService;
import com.scrippsnetworks.wcm.url.UrlMapper;


/**
 * Implements the Search Service, which provides search request handler objects using pooled persistent HTTP connections.
 * @author Scott Everett Johnson
 */
@Component(label="SNI WCM URL Rewrite Filter Service",description="Filter for rewriting external urls to internal resource paths",enabled=true,immediate=true,metatype=true)
@Service(value=Filter.class)
public class UrlRewriteFilter implements Filter {

    /** Extends HttpServletRequest so we can override handling of path info when forwarding.
     * 
     * In this OSGI/Sling/CQ context, the entire request path is pathInfo.
     * For some reason when forwarding using the request dispatcher the path info on the
     * HttpServletRequest is not being set with the forward path. This wrapper provides an
     * override of getPathInfo so it returns the forward path.
     *
     */
    public class ForwardingHttpServletRequestWrapper extends HttpServletRequestWrapper {

        String pathInfo;

        public ForwardingHttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        public ForwardingHttpServletRequestWrapper(HttpServletRequest request, String pathInfo) {
            super(request);
            this.pathInfo = pathInfo;
        }

        public void setPathInfo(String pathInfo) {
            this.pathInfo = pathInfo;
        }

        @Override
        public String getPathInfo() {
            if (this.pathInfo != null) {
                return this.pathInfo;
            } else {
                return super.getPathInfo();
            }
        }
    }

    /** Extends HttpServletResponse in order to capture redirect responses and rewrite the location.
     *
     * Wraps the response in methods overriding the set/addLocation methods, providing a hook to
     * rewrite redirects using the UrlMapper.
     */
    public class RewritingHttpServletResponseWrapper extends HttpServletResponseWrapper {

        UrlMapper urlMapper;
        HttpServletRequest request;

        public RewritingHttpServletResponseWrapper(HttpServletResponse response, HttpServletRequest request, UrlMapper urlMapper) {
            super(response);
            this.urlMapper = urlMapper;
            this.request = request;
            logger.debug("initialized response wrapper for {}", request.getRequestURI());
        }

        @Override
        public void setHeader(String name, String value) {
            if (name.equalsIgnoreCase("location")) {
                String newLocation = rewriteLocation(value);
                logger.info("rewriting location from {} to {}", value, newLocation);
                super.setHeader(name, newLocation);
            } else {
                super.setHeader(name, value);
            }
        }

        @Override
        public void addHeader(String name, String value) {
            if (name.equalsIgnoreCase("location")) {
                String newLocation = rewriteLocation(value);
                logger.info("rewriting location from {} to {}", value, newLocation);
                super.setHeader(name, newLocation);
            } else {
                super.setHeader(name, value);
            }
        }

        /** Use the UrlMapper to rewrite the URI in the given string.
         *
         * For a rewrite to occur, if a host is set in the URI, it must match the host of the current request.
         *
         * @param value String containing a URI
         * @return String The mapped URI, or the original URI if there's no change to make or if an exception occurs.
         */
        public String rewriteLocation(String value) {
            URI uri = null;
            URI mappedUri = null;
            URI returnUri = null;

            try {
                uri = new URI(value);
            } catch (URISyntaxException e) {
                // This should never happen.
                logger.error("rewriteLocation: got URISyntax exception", e);
                return value;
            }

            // If the location value is not on this host, we shouldn't touch it.
            String origHost = uri.getHost();
            if (origHost != null) {
                if (!origHost.equals(request.getServerName())) {
                    logger.debug("rewriteLocation: declining to rewrite, location host {} != request host {}", origHost, request.getServerName());
                    return value;
                }
            }

            String path = uri.getPath();
            if (path == null) {
                return value;
            }

            try {
                String mappedResult = urlMapper.map(request, path);
                mappedUri = new URI(mappedResult);
            } catch (URISyntaxException e) {
                // This should never happen.
                logger.error("rewriteLocation: got URISyntaxException from mappedURI", e);
                return value;
            } catch (Exception e) {
                logger.error("rewriteLocation: unexpected exception creating URI", e);
            }

            try {
                returnUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
                        mappedUri==null?null:mappedUri.getPath(),
                        uri.getQuery(), uri.getFragment());
            } catch (URISyntaxException e) {
                // This should never happen.
                logger.error("rewriteLocation: got URISyntaxException from returnURI");
                return value;
            }

            return returnUri.toString();
        }
    }

    private static Logger logger = LoggerFactory.getLogger(UrlRewriteFilter.class);

    private FilterConfig filterConfig;

    @Reference
    UrlMapper urlMapper;

    @Reference
    SlingSettingsService slingSettingsService;

    /** Controls whether the filter will redirect urls that map to a different location. */
    boolean redirectBucketedUrls = false;

    @Property(label="Filter pattern",description="Regular expression determining requests that are filtered",value="^(?!/services|/etc|/content/dam|/libs|/bin|/system|/crx|/apps|/cf|/home|/siteadmin|/crxde)/.*")
    static final String PATTERN = "pattern";

    @Property(label="Filter ranking",description="Rank among other servlet filters. See the HTTP Whiteboard tab in the Felix console for a list of filters.",intValue=0)
    static final String RANKING = "service.ranking";

    static final String ORIGINAL_REQUEST_URL_ATTRIBUTE = "com.scrippsnetworks.wcm.url.UrlMapper.originalRequestUrl";

    /** Indicates errors during component configuration. */
    private static class UrlRewriteFilterConfigException extends Exception {
        public UrlRewriteFilterConfigException(String msg, Throwable cause) {
            super(msg, cause);
        }
    };

    /**
     * Activates this OSGi component, setting its properties from the ComponentContext and
     * initializing its state.
     */
    @Activate
    protected void activate(ComponentContext ctx) {
        Dictionary props = ctx.getProperties();
        try {
            internalActivate(props);
        } catch (UrlRewriteFilterConfigException e) {
            String pid = (String)props.get(Constants.SERVICE_PID);
            ctx.disableComponent(pid);
            logger.error("disabling {} due to error during activation", pid);
            // rethrowing since there's no other way to signal failure of this method;
            throw new ComponentException("disabling search service due to activation failure",e);
        }
    }

    /**
     * Activation method that doesn't rely on OSGi ComponentContext for parameters,
     * instead allowing them to be passed in directly.
     *
     * @param props Dictionary of properties to set.
     */
    protected void internalActivate(Dictionary props) throws UrlRewriteFilterConfigException {
        if (slingSettingsService != null) {
            redirectBucketedUrls = slingSettingsService.getRunModes().contains("publish");
        }
        logger.info("activate");
    }

    /**
     * Deactivates this OSGi component, cleaning up any state.
     */
    @Deactivate
    protected void deactivate(ComponentContext ctx) {
        internalDeactivate();
    }

    /**
     * Deactivation method that doesn't rely on OSGi ComponentContext
     */
    protected void internalDeactivate() {
        logger.info("deactivate");
    }

    /**
     * Updates the state of this OSGi component when the ComponentContext has changed.
     */
    @Modified
    protected void modified(ComponentContext ctx) {
        Dictionary props = ctx.getProperties();
        internalDeactivate();
        try {
            internalActivate(props);
        } catch (UrlRewriteFilterConfigException e) {
            String pid = (String)props.get(Constants.SERVICE_PID);
            ctx.disableComponent(pid);
            logger.error("disabling {} due to error during activation", pid);
            // rethrowing since there's no other way to signal failure of this method;
            throw new ComponentException("disabling service due to activation failure",e);
        }
    }

    public void init(FilterConfig pFilterConfig) throws ServletException {
        filterConfig = pFilterConfig;
    }

    public void destroy() {
        logger.info("destroy");
    }
 
    public void doFilter(ServletRequest servletRequest,ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest=(HttpServletRequest)servletRequest;
        HttpServletResponse httpResponse=(HttpServletResponse)servletResponse;

        if (urlMapper != null && !httpRequest.getMethod().equals("POST")) {
            RewritingHttpServletResponseWrapper  wrappedResponse = new RewritingHttpServletResponseWrapper(httpResponse, httpRequest, urlMapper);
            String oldPath = httpRequest.getPathInfo();
            String mappedPath = urlMapper.resolvePath(httpRequest, oldPath);
            if (mappedPath != null) {
                if (httpRequest.getAttribute(ORIGINAL_REQUEST_URL_ATTRIBUTE) == null) {
                    logger.debug("forwarding {} to {}", oldPath, mappedPath);
                    try {
                        httpRequest.setAttribute(ORIGINAL_REQUEST_URL_ATTRIBUTE, httpRequest.getPathInfo());
                        ForwardingHttpServletRequestWrapper  newRequest = new ForwardingHttpServletRequestWrapper(httpRequest, mappedPath);
                        RequestDispatcher forwardDispatcher = newRequest.getRequestDispatcher(mappedPath);
                        forwardDispatcher.forward(newRequest, wrappedResponse);
                        return;
                    } catch (IllegalStateException e) {
                        logger.error("Got IllegalStateException: {}", e.getMessage());
                    } catch (Exception e) {
                        logger.error("got unexpected exception {}", e.getMessage());
                    }
                }
            } else if (redirectBucketedUrls) {
                mappedPath = urlMapper.map(httpRequest, oldPath);
                // The mappedPath is URI-escaped, so we need to re-escape the path we have for comparison.
                String escapedPath = null;
                try {
                    URI uri = new URI(null, oldPath, null);
                    escapedPath = uri.toASCIIString();
                } catch (URISyntaxException e) {
                    // This shouldn't be possible, since of course the only way this request made it here was a URI.
                    escapedPath = oldPath;
                }
                // The path should be relative if the request host is the same as the resolver map host.
                // If that isn't the case, we don't want to redirect; we should only redirect on the web domain.
                if (mappedPath.charAt(0) == '/' && !escapedPath.equals(mappedPath)) {
                    String redirectUrl = httpRequest.getQueryString() == null ? mappedPath : mappedPath + "?" + httpRequest.getQueryString();
                    httpResponse.setStatus(301);
                    httpResponse.setHeader("Location", redirectUrl);
                    httpResponse.setHeader("Connection", "close");
                    logger.debug("Sent redirect to {}", redirectUrl);
                    return; // no need to execute the rest of the filter chain, eh?
                }
            }
        } else {
            logger.warn("doFilter() urlMapper unavailable");
        }

        // fall-through
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
