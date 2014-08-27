package com.scrippsnetworks.wcm.impl;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.ServletException;
import org.apache.felix.scr.annotations.Component;
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


/***
 * This servlet will check to see if a duplicate url mapping could occur. Takes a path and potential name and checks against the url mapper
 * to see if there are any other pages that will get mapped to the same name. For efficiency sake this check only occurs against sections
 * that are known to be 'bucketed'. Returns the first conflict found, otherwise returns an empty json array.
 * 
 * @author Danny Gordon 12/5/2012
 *
 */
//@Component(metatype = false, immediate = true)
@SlingServlet(selectors = "program-data", methods = "GET", extensions = "json",
paths = {"/bin/duplicateurlcheck"})
public class UrlDuplicateCheck extends SlingAllMethodsServlet{
 
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(UrlDuplicateCheck.class);
	
	@Reference
	private UrlMapper urlMapper;
 
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
 
        try{
            Session session = request.getResourceResolver().adaptTo(Session.class);
            
            final String pagePath = request.getParameter("pagePath");
            String pageName = request.getParameter("pageName");
            String pageTitle = request.getParameter("pageTitle");
            
            if(pageName == null) {
            	if(pageTitle != null) {
            		pageName = JcrUtil.createValidName(pageTitle);
            		pageName = pageName.replace("_", "-");
            	}
            }
            String absolutePath = pagePath + "/" + pageName;
            
            logger.debug("Page name parameter passed is {}", pageName);
            logger.debug("page path parameter passed is {}", pagePath);
            
            /***
             * Initialize return json
             */
            response.setHeader("Content-Type", "text/html");
            TidyJSONWriter tidyJSONWriter = new TidyJSONWriter(response.getWriter());
			tidyJSONWriter.object();
			tidyJSONWriter.key("existingpaths").array();
			
			
	
			
			/***
			 * check to see if page path falls in one of the bucketed sections
			 */
            if(pageName != null && isBucketedPath(pagePath))
            {
                String shortenedPath = urlMapper.map(request, absolutePath);
                logger.debug("Mapped path is {}", shortenedPath);
            	String basePath = getBasePath(absolutePath, shortenedPath);
            	logger.debug("Base path is: {}", basePath);
            
            	try {
            		QueryManager qm = session.getWorkspace().getQueryManager();
            		NodeIterator resultNodes = performSearchWithSQL(qm, pageName, basePath);
			
            		while (resultNodes.hasNext()) {
            			Node node = resultNodes.nextNode();
            			logger.debug("Node path is {}", node==null?null:node.getPath());
            			if(node != null && node.getPath().startsWith(basePath))
            			{
            				String existingPath = node.getPath();
            				String mappedPath = urlMapper.map(request, existingPath);
            				logger.debug("existing mapped path: {}", mappedPath);
            				if(mappedPath != null && mappedPath.equals(shortenedPath))
            				{
            					logger.info("Mapping conflict found. " + absolutePath + " and " + existingPath + " map to the same url: " + mappedPath);
            					tidyJSONWriter.value(existingPath);
            					//if we find a single match break and return result
            					break;
            				}
            			}
            		}
            	} catch (RepositoryException e)
            	{
            		logger.error("A repository exception occured while checking for duplicate urls: {}", e.getMessage());
            	}
			
            }
 
			tidyJSONWriter.endArray();
			tidyJSONWriter.endObject();
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
        } catch (JSONException e) {
            logger.error( "Error in doGet", e );
        }
    }
    
    private NodeIterator performSearchWithSQL(QueryManager qm, String pageName, String basePath) throws RepositoryException {
    	//QueryManager qm = queryRoot.getSession().getWorkspace ().getQueryManager();
    	String queryString = "SELECT * FROM [cq:Page] AS node WHERE ISDESCENDANTNODE([" 
				+ basePath + "]) and name(node)='" + pageName + "'";
    	Query query = qm.createQuery( queryString, Query.JCR_SQL2);
		return query.execute().getNodes();

    }

    private String getBasePath(String absolutePath, String shortenedPath)
    {
    	StringBuilder returnString = new StringBuilder();
    	String[] absolutePieces = absolutePath.split("/");
    	String[] shortenedPieces = shortenedPath.split("/");
    	
    	for(int i = 1; i < shortenedPieces.length; i++)
    	{
    		if(shortenedPieces[i].equals(absolutePieces[i]) && i < (absolutePieces.length -1))
    		{
    			returnString.append("/").append(shortenedPieces[i]);
    		}
    	}
    	
    	return returnString.toString();
    }
    
    /**
     * Returns true if the pagePath is in one of the sections that uses bucketing
     * The bucketed sections are Shows, Recipes, People, and Videos. If the path does not fall in one of these 
     * sections return false
     * @param pagePath
     * @return
     */
    private boolean isBucketedPath(String pagePath) {
    	if(pagePath.startsWith(ContentRootPaths.CHEFS.path()) || pagePath.startsWith(ContentRootPaths.RECIPES.path()) 
    			|| pagePath.startsWith(ContentRootPaths.SHOWS.path()) || pagePath.startsWith(ContentRootPaths.VIDEOS.path())  ) {
    		return true;
    	}
    	
    	return false;
    }
}