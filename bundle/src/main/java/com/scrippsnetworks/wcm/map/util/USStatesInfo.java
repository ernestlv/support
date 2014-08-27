package com.scrippsnetworks.wcm.map.util;

import java.io.IOException;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.TidyJSONWriter;
import com.scrippsnetworks.wcm.url.UrlMapper;
import com.scrippsnetworks.wcm.util.ContentRootPaths;
import com.day.cq.commons.jcr.JcrUtil;


/**
 * This servlet return state name and state code mapping in json object
 *
 * @author Ivan Dzemiashkevich 09/17/2012
 */
@SlingServlet(methods = "GET", extensions = "json",
        paths = {"/bin/getstates"})
public class USStatesInfo extends SlingAllMethodsServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(USStatesInfo.class);


    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        try {

            TidyJSONWriter tidyJSONWriter = new TidyJSONWriter(response.getWriter());
            tidyJSONWriter.object();

            StateHelper stateHelper = new StateHelper();

            for (Map.Entry<String, String> state : stateHelper.getAllStates()) {
                tidyJSONWriter
                        .key(state.getKey())
                        .value(state.getValue());
            }


            tidyJSONWriter.endObject();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
        } catch (JSONException e) {
            logger.error("Error in doGet", e);
        }
    }
}