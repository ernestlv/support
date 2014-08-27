package com.scrippsnetworks.wcm.impl;
 
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
import java.io.IOException;
import java.lang.String;
import java.lang.Integer;
import java.util.Dictionary;

import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.QuerySyntaxException;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.commons.lang.StringUtils;

import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.QueryManager;
import javax.jcr.query.RowIterator;
import javax.jcr.query.Row;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.NodeIterator;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Implements the Legacy Video XML Forwarding Filter Service which provides a servlet filter intercepting requests for legacy
 * channel xml requests and either internally forwarding them or redirecting them to CQ urls.
 *
 * @author Scott Everett Johnson
 */
@Component(label="SNI WCM Legacy Video Xml Forwarding Filter Service",description="Filter for rewriting legacy channel xml urls to new URLs",enabled=true,immediate=true,metatype=true)
@Service(value=Filter.class)
public class VideoXmlForwardingFilter implements Filter {

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

        public String getPathInfo() {
            if (this.pathInfo != null) {
                return this.pathInfo;
            } else {
                return super.getPathInfo();
            }
        }
    }

    private static Logger logger = LoggerFactory.getLogger(VideoXmlForwardingFilter.class);

    private FilterConfig filterConfig;

    Boolean doRedirect = false;
    Integer redirectStatus = 302;

    public static final Pattern requestPattern = Pattern.compile("/([a-z]+)/channel/xml/0,,([0-9]+)(-VIDEO|-video|-PSA|-psa)?,00.xml");

    @Reference
    ResourceResolverFactory resourceResolverFactory;

    @Reference
    SlingRepository slingRepository;

    // The &comma; is a pseudo-xml entity used to put a comma in a string property value without triggering array // interpretation.
    @Property(label="Filter pattern",description="Regular expression determining requests that are handled. Set private due to problems with commas in config interface.",cardinality=0,propertyPrivate=true,value="^/[a-z]{4}/channel/xml/0,,[0-9]+(-VIDEO|-video|-PSA|-psa)?,00[.]xml")
    static final String PATTERN = "pattern";

    @Property(label="Filter ranking",description="Rank among other servlet filters. See the HTTP Whiteboard tab in the Felix console for a list of filters.",propertyPrivate=true,intValue=1)
    static final String RANKING = "service.ranking";

    @Property(label="Issue Redirect",description="Issue a redirect instead of internally forwarding.",boolValue=false)
    static final String DO_REDIRECT = "redirect";

    @Property(label="Redirect Status",description="Status for redirect.",intValue=301,options={@PropertyOption(name="301", value="Permanent (301)"), @PropertyOption(name="302", value="Temporary (302)")})
    static final String REDIRECT_STATUS = "redirect.status";

    /** Indicates errors during component configuration. */
    private static class VideoXmlForwardingFilterConfigException extends Exception {
        public VideoXmlForwardingFilterConfigException(String msg, Throwable cause) {
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
        } catch (VideoXmlForwardingFilterConfigException e) {
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
    protected void internalActivate(Dictionary props) throws VideoXmlForwardingFilterConfigException {
        logger.info("activate");
        try {
            doRedirect = (Boolean)props.get(DO_REDIRECT);
            redirectStatus = (Integer)props.get(REDIRECT_STATUS);
            String pattern = (String)props.get(PATTERN);
            logger.info("pattern is " + pattern);
        } catch (Exception e) {
            throw new VideoXmlForwardingFilterConfigException("caught exception setting properties", e);
        }
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
        } catch (VideoXmlForwardingFilterConfigException e) {
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
 
    /**
     * @see javax.servlet.Filter#doFilter
     */
    public void doFilter(ServletRequest servletRequest,ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest=(HttpServletRequest)servletRequest;
        HttpServletResponse httpResponse=(HttpServletResponse)servletResponse;

        logger.debug("doFilter {}", httpRequest.getPathInfo());

        if (httpRequest.getMethod().equals("GET")) {
            String oldPath = httpRequest.getPathInfo();
            String mappedPath = getVideoPath(oldPath);
            if (mappedPath != null) {
                if (doRedirect) {
                    try {
                        httpResponse.setStatus(redirectStatus);
                        httpResponse.setHeader("Location", mappedPath + ".xml");
                    } catch (IllegalStateException e) {
                        logger.error("Got IllegalStateException when sending redirect", e);
                    } catch (Exception e) {
                        logger.error("Got unexpected exception when sending redirect", e);
                    } finally {
                        return;
                    }
                } else {
                    logger.debug("forwarding {} to {}", oldPath, mappedPath);
                    try {
                        ForwardingHttpServletRequestWrapper  newRequest = new ForwardingHttpServletRequestWrapper(httpRequest, mappedPath + ".xml");
                        RequestDispatcher forwardDispatcher = newRequest.getRequestDispatcher(mappedPath + ".xml");
                        forwardDispatcher.forward(newRequest, httpResponse);
                        return;
                    } catch (IllegalStateException e) {
                        logger.error("Got IllegalStateException when forwarding request", e);
                    } catch (Exception e) {
                        logger.error("Got unexpected exception when forwarding request", e);
                    } finally {
                        return;
                    }
                } 
            } else {
                // The assumption here is that there is no real resource at a url matching the pattern.
                // Save some cycles, bail now.
                try {
                    httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "not found");
                    return;
                } catch (IllegalStateException e) {
                    logger.error("Got IllegalStateException when sending not found", e);
                } catch (Exception e) {
                    logger.error("Got unexpected exception when sending not found", e);
                } finally {
                    return;
                }
            }
        }

        // fall-through
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * Accepts the url from a legacy xml request, extracts the id and type information, and searches for the matching channel
     * or video page node, returning its path.
     *
     * @param url The request URL to extract information from.
     * @return String path to video or channel page, or null if none was found.
     */
    public String getVideoPath(String url) {
        logger.debug("getVideoPath {}", url);
        long start = System.currentTimeMillis();
        String retVal = null;
        String typeSelector = "";
        Matcher m = requestPattern.matcher(url);
        boolean isVideo = false;
        if (m.matches()) {
            logger.debug("getVideoPath matched url");
            String brand = m.group(1);
            String id = m.group(2);
            String type = m.group(3);
            if (brand != null && id != null) {
                Session session = null;
                QueryManager queryManager = null;
                try {
                    session = slingRepository.loginAdministrative(null);
                    queryManager = session.getWorkspace().getQueryManager();
                } catch (RepositoryException e) {
                    logger.warn("getVideoPath: caught RepositoryException", e);
                }

                String queryStr = null;
                String queryType = null;
                Iterator<Resource> iterator = null;
                if (queryManager != null) {
                    if (type == null) {
                        // id is a channel id
                        // queryStr = "select * from [cq:Page] as page inner join [cq:PageContent] as pageContent on ISCHILDNODE(pageContent, page) where ISDESCENDANTNODE(page, '/content/cook/videos/channels') and pageContent.[sling:resourceType] = 'sni-wcm/components/pagetypes/video-channel' and pageContent.[sni:fastfwdId] = " + id + ".0";
                        queryStr = "select * from [cq:PageContent] as pageContent where ISDESCENDANTNODE(pageContent, '/content/" + brand + "') and pageContent.[sling:resourceType] LIKE '%/components/pagetypes/video-channel' and pageContent.[sni:fastfwdId] = " + id + ".0";
                        queryType = Query.JCR_SQL2;
                        typeSelector = "videochannel";
                        logger.info("channel xml query {}", queryStr);
                    } else if (type.equalsIgnoreCase("-VIDEO")) {
                        // id is a video id
                        // queryStr = "select * from [cq:Page] as page inner join [cq:PageContent] as pageContent on ISCHILDNODE(pageContent, page) where ISDESCENDANTNODE(page, '/content/cook/videos') and pageContent.[sling:resourceType] = 'sni-wcm/components/pagetypes/video' and pageContent.[sni:fastfwdId] = " + id + ".0";
                        queryStr = "select * from [cq:PageContent] as pageContent where ISDESCENDANTNODE(pageContent, '/content/" + brand + "/videos') and pageContent.[sling:resourceType] LIKE '%/components/pagetypes/video' and pageContent.[sni:fastfwdId] = " + id + ".0";
                        queryType = Query.JCR_SQL2;
                        typeSelector = "videoplayer";
                        logger.info("video xml query {}", queryStr);
                    } else if (type.equalsIgnoreCase("-PSA")) {
                        // id is a video source id
                        queryStr = "/jcr:root/etc/sni-asset/" + brand + "/videos//element(*, cq:Page)[jcr:content/@sni:sourceId='" + id + "']";
                        logger.info("getVideoPath: PSA first query {}", queryStr);
                        try {
                            Query query = queryManager.createQuery(queryStr, Query.XPATH);
                            QueryResult queryResult = query.execute();
                            NodeIterator nodeIterator = queryResult.getNodes();
                            if (nodeIterator.hasNext()) {
                                String assetPath = nodeIterator.nextNode().getPath();
                                queryStr = "/jcr:root/content/" + brand + "//element(jcr:content, cq:PageContent)[@sni:assetLink='" + assetPath + "']";
                                queryType = Query.XPATH;
                                typeSelector = "videoplayer";
                            } else {
                                queryStr = null;
                            }
                        } catch (InvalidQueryException e) {
                            queryStr = null;
                            logger.warn("getVideoPath: caught InvalidQueryException", e);
                        } catch (RepositoryException e) {
                            queryStr = null;
                            logger.warn("getVideoPath: caught RepositoryException", e);
                        }
                    }

                    if (queryStr != null) {
                        try {
                            Query query = queryManager.createQuery(queryStr, queryType);
                            //iterator = resourceResolver.findResources(query, queryType);
                            QueryResult queryResult = query.execute();

                            if (queryType.equals(Query.JCR_SQL2) && queryResult.getSelectorNames().length > 0) {
                                RowIterator rowIterator = queryResult.getRows();
                                if (rowIterator.hasNext()) {
                                    // Selector is always pageContent
                                    retVal = rowIterator.nextRow().getNode("pageContent").getParent().getPath();
                                }
                            } else {
                                NodeIterator nodeIterator = queryResult.getNodes();
                                if (nodeIterator.hasNext()) {
                                    retVal = nodeIterator.nextNode().getParent().getPath();
                                }
                            }

                            if (! StringUtils.isEmpty(retVal)) {
                                retVal += "." + typeSelector;
                            }
                        } catch (InvalidQueryException e) {
                            logger.warn("getVideoPath: caught InvalidQueryException", e);
                        } catch (RepositoryException e) {
                            logger.warn("getVideoPath: caught RepositoryException", e);
                        }
                    }
                } else {
                    logger.info("getVideoPath: couldn't get resource resolver");
                }

                if (session != null) {
                    session.logout();
                }
            }
        } else {
            logger.debug("getVideoPath {} did not match pattern {}", url, requestPattern.pattern());
        }

        long end = System.currentTimeMillis();
        logger.info("getVideoPath returning {} in {} ms", retVal, end - start);
        return retVal;
    }

}
