package com.scrippsnetworks.wcm.contentfinder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.day.cq.commons.servlets.AbstractPredicateServlet;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.result.SearchResult;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.collections.Predicate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.ResourceResolver;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.WCMMode;

/**
 *
 * @author ranand
 *
 */
@Component(metatype = false, label = "SNI Content Finder Query Handler",
           description = "Options to add path and search by title to the content finder query")
@Service
@Property (name = "sling.servlet.paths", value = "/bin/wcm/contentfinder/snipage/sniview")
public class ContentFinderPageQueryServlet extends AbstractPredicateServlet {
    private static final long serialVersionUID = 5964360462202342062L;

    private static Logger logger = LoggerFactory.getLogger(ContentFinderPageQueryServlet.class);

    /** Full text query parameter. */
    public static final String QUERY = "query";

    /** Page path parameter. */
    public static final String PAGE_PATH = "path";

    /** Search by title parameter */
    public static final String JCR_TITLE = "jcrTitle";
    
    /** type search. */
    public static final String TYPE = "type";

    /** search option */
    public static final String OPTION = "searchOption";    
    
    /** Default Page path. */
    public static final String DEFAULT_PAGE_PATH = "/content/food";
    
    /** Default Module path. */
    public static final String DEFAULT_MODULE_PATH = "/content/modules/food";    

    /**
     * Process query predicate and return result in HTTP response.
     * @param request HTTP Get request
     * @param response HTTP Get response
     * @param predicate HTTP Query predicate
     * @throws ServletException Servlet exception
     * @throws IOException IO exception
     */
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response,
                         Predicate predicate) throws ServletException, IOException {
        RequestPathInfo pathInfo = request.getRequestPathInfo();
        boolean isAuthorMode = WCMMode.fromRequest(request) != WCMMode.DISABLED;
        if(isAuthorMode){
	        if ("json".equals(pathInfo.getExtension())) {  
	            
	        	response.setContentType("application/json");
	            response.setCharacterEncoding("utf-8");

	            //  Get the query parameters
	            String path = getRequestParameterValue(PAGE_PATH, request);
	            String queryString = getRequestParameterValue(QUERY, request);
	            String jcrTitle = getRequestParameterValue(JCR_TITLE, request);
	            String type = getRequestParameterValue(TYPE, request);
	            //boolean isTextSearchSpecified = queryString.length() > 0;
	            boolean isFullTextSearch = (getRequestParameterValue(OPTION, request).equalsIgnoreCase("false")) ? true : false;
	            String pagePath = null;
	
	            try {
	            	if(StringUtils.isNotBlank(queryString)){
		                //  Build the query statement
		                Map<String, String> map = new HashMap<String, String>();
		                
		                if(type.equals("Page"))
		                	pagePath = (!path.equals("")) ? path : DEFAULT_PAGE_PATH;
		                else
		                	pagePath = (!path.equals("")) ? path : DEFAULT_MODULE_PATH;
		                
		                map.put("path", pagePath);                
		                map.put("type", "cq:Page");
		                
		                if (isFullTextSearch) {
		                    map.put("fulltext", queryString);
		                }else{
		                    map.put("property", "fn:lower-case(jcr:content/jcr:title)");
		                    map.put("property.operation", "like");
		                    map.put("property.value", queryString);
		                }
		                	
		                map.put("p.limit", "100");
		
		                //  Build the sort specification
		                map.put("orderby", "@jcr:content/cq:lastModified");
		                map.put("orderby.index", "true");
		                map.put("orderby.sort", "desc");
		
			            logger.debug("Custom Content Finder query for (" + queryString + ","
	                            + pagePath + ","
	                            + type + ","
	                            + isFullTextSearch + ","
	                            + jcrTitle + ")");	                
		                
		                //  Execute the query
		                ResourceResolver resolver = request.getResourceResolver();
		                Session session = resolver.adaptTo(Session.class);
		                QueryBuilder queryBuilder = resolver.adaptTo(QueryBuilder.class);
		                Query query = queryBuilder.createQuery(PredicateGroup.create(map), session);
		                SearchResult searchResult = query.getResult();
		
		                //  Write the query result as JSON in the response
		                writeHits(searchResult, response);
	
	            	}else{
	            		emptyResults(response);
	            	}
	            } catch (JsonGenerationException je) {
	                logger.error("Unable to convert Content Finder query results to JSON", je);
	                throw new ServletException(je);
	            } catch (IOException ioe) {
	                logger.error("Unable to process Content Finder query.", ioe);
	                throw new ServletException(ioe);
	            } catch (RepositoryException e) {
	                logger.error("Repository issue while processing Content Finder query.", e);
	                throw new ServletException(e);
	            }
	        }
        }//Run only in Author mode
    }

    /**
     * Get parameter from HTTP request, dealing with null and trimming if necessary.
     * @param parameterName The name of the parameter.
     * @param request The HTTP request object
     * @return The parameter value, which will always be a string with no trailing blanks.
     */
    private String getRequestParameterValue(String parameterName, SlingHttpServletRequest request) {
        String parameterValue = request.getParameter(parameterName);
        if (parameterValue == null) {
            parameterValue = "";
        }
        parameterValue = parameterValue.trim();
        return parameterValue;
    }

    /**
     * Convert SearchResult output into JSON-encoded "hit" data and write it to the response
     * PrintWriter.
     * @param searchResult Nodes in search result
     * @param response SlingHttpServletResponse response
     * @throws JsonGenerationException JSON exception
     * @throws IOException IO exception
     * @throws RepositoryException Repository exception
     */
    protected void writeHits(SearchResult searchResult, SlingHttpServletResponse response)
                       throws JsonGenerationException, IOException, RepositoryException {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JsonGenerator writer = new JsonFactory().createJsonGenerator(response.getWriter());
        writer.writeStartObject();
        writer.writeArrayFieldStart("hits");

        Iterator<Node> resultNodesIterator = searchResult.getNodes();
        while (resultNodesIterator.hasNext()) {
            Node resultNode = resultNodesIterator.next();
            writer.writeStartObject();
            Date lastModifiedDate = resultNode.getProperty("jcr:content/cq:lastModified").getDate().getTime();
            writer.writeStringField("lastModified", formatter.format(lastModifiedDate));
            writer.writeStringField("name", resultNode.getName());
            writer.writeStringField("path", resultNode.getPath());
            writer.writeStringField("title", resultNode.getProperty("jcr:content/jcr:title").getString());
            writer.writeEndObject();
        }
        writer.writeEndArray();
        writer.writeEndObject();
        writer.close();
        writer.flush();
    }
    
    /**
     * Empty results on intial page load 
     * @param response SlingHttpServletResponse response
     * @throws JsonGenerationException JSON exception
     * @throws IOException IO exception
     * @throws RepositoryException Repository exception
     */
    protected void emptyResults(SlingHttpServletResponse response)
                       throws JsonGenerationException, IOException, RepositoryException {

        JsonGenerator writer = new JsonFactory().createJsonGenerator(response.getWriter());
        writer.writeStartObject();
        writer.writeArrayFieldStart("hits");
        writer.writeEndArray();
        writer.writeEndObject();
        writer.close();
        writer.flush();
    }
}