package com.scrippsnetworks.wcm.fnr.sitesearch;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.http.client.utils.URIBuilder;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestParameterMap;
import org.apache.sling.api.request.RequestPathInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.PathNotFoundException;
import com.scrippsnetworks.wcm.util.StringUtil;

/** Helper for interpreting and constructing search URLs with varying parameters.
 *
 * After construction from a SlingHttpServletRequest the UrlHelper is initialized with a state representing all
 * the parameters acquired from the current request. The UrlHelper provides
 * <ul>
 *     <li>access to parameter values</li>
 *     <li>methods for construction of new URLs with varying parameter values</li>
 * </ul>	
 * while encapsulating knowledge of how the URL is structured.
 */
public class UrlHelper {

    public static final String UNIT_SEPARATOR = "\u001f";

    private final Logger logger = LoggerFactory.getLogger(UrlHelper.class);

    static final String PARAM_FILTERS = "filters";
    static final String PARAM_PAGE = "page";
    static final String PARAM_SEARCH_TERM = "searchTerm";
    static final String PARAM_SORT_BY = "sortBy";
    static final String PARAM_LAST_FILTER = "lastFilter";
    static final String PARAM_FORM = "form";
    static final String PARAM_AIRDATE = "airdate";
    static final String PARAM_CHARSET = "_charset_";
    static final String PARAM_WCMMODE = "wcmmode";
    static final String SELECTOR_MOBILE = "mobile";
    static final String SELECTOR_MORE_BUTTON_MOBILE = "more";
    
    

    static final String FILTER_REFINEMENT = "refinement";
    static final String FILTER_PAGE = "pagination";
    static final String FILTER_SORT = "sort";
    static final String FILTER_REMOVE = "remove";
    static final String FILTER_TAB = "tab";
    static final String FILTER_SEARCH = "search";
    private static final Pattern sanitizePattern = Pattern.compile("[<>]", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern spaceCruncherPattern = Pattern.compile("[\\s]+", Pattern.UNICODE_CHARACTER_CLASS);
    static final String SEARCH_CONTENT_NODE = "content-well/search_content";
    

    /* For parameter suffixes.
    static final Pattern pagePattern = Pattern.compile("/page/([0-9]+)[/.]");
    static final Pattern dimensionsPattern = Pattern.compile("/dimensions/([0-9,]+)[/.]");
    static final Pattern searchTermPattern = Pattern.compile("/searchTerm/([^/]+)(?=.html|/)");
    */

    String path;
    SearchType searchType;
    String searchTerm;
    Set<String> dimensions = new TreeSet<String>();
    int pageNumber = 1;
    SortKey sortKey = SortKey.relevancy;
    String filter;
    SearchForm form;
    String charset = "UTF-8";
    String wcmMode;
    AirDateKey airDateKey;
    String pathForDynamicLoading;
    boolean isMobile;


    /** Constructs a new UrlHelper from a request.
     *
     * @param request A SlingHttpServletRequest from which to extract parameter values.
     * @throws IllegalArgumentException if a parameter constrained by an enum has an invalid value
     */
    public UrlHelper(SlingHttpServletRequest request) {
        if (request == null) {
            return;
        }

        path = request.getResource().getParent().getPath();
        try{
	        Node jcrContentOfPage = request.getResource().adaptTo(Node.class);
	        if(jcrContentOfPage.hasNode(SEARCH_CONTENT_NODE)){
	        	pathForDynamicLoading = jcrContentOfPage.getNode(SEARCH_CONTENT_NODE).getPath()+"."+SELECTOR_MORE_BUTTON_MOBILE;
	        }
        }catch(RepositoryException exception){
        	logger.info("Cannot create dynamic loading", exception);
        }
        RequestPathInfo requestPathInfo = request.getRequestPathInfo();
        String[] selectors = requestPathInfo.getSelectors();
        List<String> selectorsList = Arrays.asList(selectors);
        for (String selector : selectorsList) {
            boolean selectorKnown = false;
            try {
            	if(selectorsList.contains(SELECTOR_MOBILE)){
            		isMobile=true;
            		searchType = SearchType.valueOf(SELECTOR_MOBILE+"_"+selector);
                    selectorKnown = true;
                }else{
                	searchType = SearchType.valueOf(selector);
                    selectorKnown = true;
                }
            } catch (IllegalArgumentException e) {
                // not a search type, nothing to do
            }
        }

        // tried all selectors, set to all if null
        if (searchType == null) {
            searchType = SearchType.all;
        }

        RequestParameterMap requestParameterMap = request.getRequestParameterMap();
        if (requestParameterMap.containsKey(PARAM_FILTERS)) {
            RequestParameter[] params = requestParameterMap.getValues(PARAM_FILTERS);
            for (RequestParameter param: params) {
                logger.debug("adding dimension {}", param.getString());
                dimensions.add(param.getString());
            }
        }

        if (requestParameterMap.containsKey(PARAM_AIRDATE)) {
            RequestParameter param = requestParameterMap.getValue(PARAM_AIRDATE);
            airDateKey = AirDateKey.valueOf(param.getString());
        }

        if (requestParameterMap.containsKey(PARAM_PAGE)) {
            RequestParameter param = requestParameterMap.getValue(PARAM_PAGE);
            pageNumber = Integer.valueOf(param.getString());
        }

        if (requestParameterMap.containsKey(PARAM_SEARCH_TERM)) {
            RequestParameter param = requestParameterMap.getValue(PARAM_SEARCH_TERM);
            searchTerm = param.getString();
            // logger.debug("searchTerm original {}", searchTerm);
            if (searchTerm != null) {
                searchTerm = StringUtil.cleanToPlainText(searchTerm);
                // logger.debug("searchTerm after cleanToPlainText {}", searchTerm);
                Matcher sanitizeMatcher = sanitizePattern.matcher(searchTerm);
                searchTerm = sanitizeMatcher.replaceAll(" ");
                // logger.debug("searchTerm after sanitizing {}", searchTerm);
                Matcher spaceCruncherMatcher = spaceCruncherPattern.matcher(searchTerm);
                searchTerm = spaceCruncherMatcher.replaceAll(" ").trim();
                // logger.debug("searchTerm after spaceCruncher {}", searchTerm);
            }
        }

        if (requestParameterMap.containsKey(PARAM_SORT_BY)) {
            RequestParameter param = requestParameterMap.getValue(PARAM_SORT_BY);
            String sortByStr = param.getString();
            if (sortByStr != null && !sortByStr.trim().isEmpty()) {
                sortKey = SortKey.valueOf(sortByStr); // can throw exception if value not in enum...let it, bad request
            }
        }

        if (requestParameterMap.containsKey(PARAM_LAST_FILTER)) {
            RequestParameter param = requestParameterMap.getValue(PARAM_LAST_FILTER);
            filter = param.getString();
        }

        if (requestParameterMap.containsKey(PARAM_FORM)) {
            RequestParameter param = requestParameterMap.getValue(PARAM_FORM);
            form = SearchForm.valueOf(param.getString());
        }

        if (requestParameterMap.containsKey(PARAM_CHARSET)) {
            RequestParameter param = requestParameterMap.getValue(PARAM_CHARSET);
            charset = param.getString();
        }

        if (requestParameterMap.containsKey(PARAM_WCMMODE)) {
            RequestParameter param = requestParameterMap.getValue(PARAM_WCMMODE);
            wcmMode = param.getString();
        }
    }

    public SearchType getSearchType() {
        return searchType;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public Set<String> getDimensions() {
        return dimensions;
    }

    public SortKey getSortKey() {
        return sortKey;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public String getFilter() {
        return filter;
    }

    public AirDateKey getAirDateKey() {
        return airDateKey;
    }

    public SearchForm getSearchForm() {
        return form;
    }

    public String getSearchUrl() {
        return getSearchUrl(path, searchType, searchTerm, dimensions, airDateKey, pageNumber, sortKey, null, wcmMode, charset);
    }

    public String getCanonicalSearchUrl(String customPath) {
        if (customPath == null) {
            customPath = path;
        }
        return getSearchUrl(customPath, searchType, searchTerm, dimensions, airDateKey, pageNumber, sortKey, null, null, charset);
    }

    public String getCanonicalSearchUrlForPage(String customPath, int pageNumber) {
        if (customPath == null) {
            customPath = path;
        }

        return getSearchUrl(customPath, searchType, searchTerm, dimensions, airDateKey, pageNumber, sortKey, null, null, charset);
    }

    public String getSearchUrlForPage(int pageNumber) {
    	if(isMobile){
    		return getSearchUrl(pathForDynamicLoading, searchType, searchTerm, dimensions, airDateKey, 1, sortKey, FILTER_PAGE, wcmMode, charset);
    	}else{
    		return getSearchUrl(path, searchType, searchTerm, dimensions, airDateKey, pageNumber, sortKey, FILTER_PAGE, wcmMode, charset);
    	}
    }
    
    public String getSearchUrlAddingDimension(String dimension) {
        Set<String> newDimensions = new TreeSet<String>(dimensions);
        newDimensions.add(dimension);
        return getSearchUrl(path, searchType, searchTerm, newDimensions, airDateKey, 1, sortKey, FILTER_REFINEMENT, wcmMode, charset);
    }

    public String getSearchUrlRemovingDimension(String dimension) {
        Set<String> newDimensions = new TreeSet<String>(dimensions);
        newDimensions.remove(dimension);

        StringBuilder filter = new StringBuilder(FILTER_REMOVE).append(":");
        filter.append(getFilterValueFromPackedDimension(dimension));
        return getSearchUrl(path, searchType, searchTerm, newDimensions, airDateKey, 1, sortKey, filter.toString(), wcmMode, charset);
    }

    public String getSearchUrlForSearchType(SearchType searchType) {
        return getSearchUrl(path, searchType, searchTerm, Collections.<String>emptySet(), null, 1, null, FILTER_TAB, wcmMode, charset);
    }

    public String getSearchUrlForSortKey(SortKey sortKey) {
        return getSearchUrl(path, searchType, searchTerm, dimensions, airDateKey, 1, sortKey, FILTER_SORT, wcmMode, charset);
    }

    public String getSearchUrlForAirDateKey(AirDateKey paramAirDateKey) {
        StringBuilder filter = new StringBuilder(paramAirDateKey == null ? FILTER_REMOVE : FILTER_REFINEMENT);
        AirDateKey filterKey = paramAirDateKey == null ? airDateKey : paramAirDateKey;
        filter.append(":").append(filterKey != null ? filterKey.getLabel() : "Air Date");
        return getSearchUrl(path, searchType, searchTerm, dimensions, paramAirDateKey, 1, sortKey, filter.toString(), wcmMode, charset);
    }

    public String getSearchUrlRemoveAllDimensions() {
        StringBuilder filter = new StringBuilder(FILTER_REMOVE);
        filter.append(":clear");
        return getSearchUrl(path, searchType, searchTerm, null, null, 1, sortKey, filter.toString(), wcmMode, charset);
    }

    /** Returns a search URL for the given parameters.
     *
     * @param path the path to the search results page itself
     * @param searchType the SearchType for the new search request
     * @param searchTerm the search term for the request
     * @param dimensions the list of dimensions for the request
     * @param pageNumber the page number for the request
     * @param sortKey sort key to use in the search request
     * @param filter string indicating what the last filter action was (adding/removing filter, pagination)
     * @return String the url for a request with the given parameters
     */
    private static String getSearchUrl(String path, SearchType searchType, String searchTerm,
                                       Set<String> dimensions, AirDateKey airDateKey, Integer pageNumber, SortKey sortKey, String filter,
                                       String wcmMode, String charset) {

        URIBuilder uriBuilder = new URIBuilder();
        StringBuilder url = new StringBuilder(path);
        if (searchType != SearchType.all) {
            url.append(".").append(searchType.name().replaceAll("mobile_", ""));
        }
        url.append(".html");

        uriBuilder.setPath(url.toString());
        uriBuilder.addParameter(PARAM_SEARCH_TERM, searchTerm);

        if (dimensions != null) {
            for (String dim : dimensions) {
                uriBuilder.addParameter(PARAM_FILTERS, dim);
            }
        }

        if (airDateKey != null) {
            uriBuilder.addParameter(PARAM_AIRDATE, airDateKey.name());
        }

        if (pageNumber > 1) {
            uriBuilder.addParameter(PARAM_PAGE, String.valueOf(pageNumber));
        }

        if (sortKey != null) {
            if (sortKey != SortKey.relevancy) {
                uriBuilder.addParameter(PARAM_SORT_BY, sortKey.name());
            }
        }

        if (filter != null) {
            uriBuilder.addParameter(PARAM_LAST_FILTER, filter);
        }

        if (charset != null) {
            uriBuilder.addParameter(PARAM_CHARSET, charset);
        }

        if (wcmMode != null) {
            uriBuilder.addParameter(PARAM_WCMMODE, wcmMode);
        }

        String retVal;
        try {
            retVal = uriBuilder.build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("error building URI", e);
        }
        return retVal;
    }

    public static String getFilterValueFromPackedDimension(String packedDimension) {
        String retVal = null;
        if (packedDimension != null) {
            String[] dimArray = packedDimension.split(UNIT_SEPARATOR);
            if (dimArray.length == 3) {
                String facetName = dimArray[0];
                String dimName = dimArray[1];
                retVal = facetName + ":" + dimName;
            } else {
                retVal = packedDimension;
            }
        }
        return retVal;
    }

    public static String getDimensionIdFromPackedDimension(String packedDimension) {
        String retVal = null;
        if (packedDimension != null) {
            String[] dimArray = packedDimension.split(UNIT_SEPARATOR);
            if (dimArray.length == 3) {
                retVal = dimArray[2];
            } else {
                retVal = packedDimension;
            }
        }
        return retVal;
    }

    public static String getPackedDimension(String filterName, String dimensionName, String dimensionId) {
        return filterName + UNIT_SEPARATOR + dimensionName + UNIT_SEPARATOR + dimensionId;
    }
}
