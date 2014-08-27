package com.scrippsnetworks.wcm.url.impl;

import com.scrippsnetworks.wcm.url.UrlMapper;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.rmi.ServerException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet utility to get a url from a friendlyUrl (passed in as a parameter)
 * However, the friendlyUrl must be prepended with context (e.g. "/content/cook/some-friendly-url" where "/content/cook" is the context)
 *
 * Note this is used by IMP and needs to be cleaned up for general use.
 *
 * @author Ken Shih (156223)
 * @created 3/12/13 11:11 AM
 */
@SlingServlet(paths="/bin/url", methods = "GET", metatype=true)
public class UrlMapperServlet extends SlingSafeMethodsServlet {
    private static final long serialVersionUID = 1l;
    @SuppressWarnings("unused")
    @Reference
    private UrlMapper urlMapper;

    //for formatting servlet response in json
    ObjectMapper jsonObjectMapper = new ObjectMapper();

    /**
     * Given a parameter "friendlyUrl" this servlet uses {@link UrlMapper} to attempt to make
     * a resolution to a full CQ path. It is not guaranteed to exist however, so user should
     * check the validity of the path returned before relying on it
     *
     * {@inheritDoc}
     */
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
        throws ServerException, IOException {
        String friendlyUrl = request.getParameter("friendlyUrl");
        String fullUrl = urlMapper.resolvePath(null,friendlyUrl);
        Map<String,String> outMap = new HashMap<String,String>();
        outMap.put("fullUrl",fullUrl);
        outMap.put("friendlyUrl",friendlyUrl);

        response.setContentType("application/json");
        response.getOutputStream().print(jsonObjectMapper.writeValueAsString(outMap));
    }
}
