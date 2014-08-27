package com.scrippsnetworks.wcm.fnr.sitesearch;

import java.lang.String;

import java.util.*;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.AbstractSearchComponent;
import com.scrippsnetworks.wcm.asset.SearchTermMetadata;
import com.scrippsnetworks.wcm.metadata.MetadataProperty;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.search.SearchResponse;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Encapsulates Food Network Site Search functionality.
 * 
 * The object is intended to be constructed as a bean via taglib. On construction the request is inspected,
 * parameters extracted from it, and a service request is prepared and executed.
 *
 * Support for presentation elements such as tabs, filters, and pagination are provided by this class and
 * its inner supporting classes.
 */
public class SiteSearchComponent extends AbstractSearchComponent {

    private static final Logger logger = LoggerFactory.getLogger(SiteSearchComponent.class);

    private static final String REFINEMENT_COUNT_PROPERTY = "count";
    private static final String REFINEMENT_ID_PROPERTY = "id";
    private static final String REFINEMENT_NAME_PROPERTY = "name";
    private static final String MAP_DYM_PATH = "response/didYouMean";
    private static final String MAP_TOTAL_ASSETS_PATH = "response/totalAssets";
    private static final String MAP_REFINEMENTS_PATH = "response/refinements";

    private static final String MDVAL_NO_SEARCH_RESULTS = "no results";
    private static final String MDVAL_DYM = "dym";
    private static final String MDVAL_SEARCH = "search";
    private static final String MDVAL_DELIMITER = ":";
    private static final String MDVAL_SEPARATOR = ",";
    private static final String MDVAL_TYPE = "SEARCH";
    private static final String MDVAL_CATEGORYDSPNAME = "search";

    private static final String PARAM_FILTER = "filter";
    private static final String PARAM_OFFSET = "offset";
    private static final String PARAM_NUM_OF_RESULTS = "numOfResults";
    private static final String PARAM_SEARCH_TERM = "searchTerm";
    private static final String PARAM_SORT_BY = "sortBy";


    private static final Map<String, String> acceptableSources = new HashMap<String, String>();
    static {
        acceptableSources.put("fnmagazine", "Food Network Magazine");
        acceptableSources.put("food-sources:fnmagazine", "Food Network Magazine");
        acceptableSources.put("food network magazine", "Food Network Magazine");
    }

    private static final int DEFAULT_ITEMS_PER_PAGE = 10;

    /** The map of search parameters extracted from the request and sent to the service endpoint. */
    final Map<String, String> serviceParameters = new HashMap<String, String>();


    protected SearchType currentSearchType = SearchType.all;
    protected SortKey currentSortKey = SortKey.relevancy;
    int currentPageNumber = 1;
    Set<String> dimensions;
    String searchTerm = null;

    /** What the user's last filtering action was.
     *
     * Each filter or pagination link contains a parameter indicating what the user's last filtering action was,
     * so it can be recorded in the metadata on the target page. This property contains the value of that parameter.
     */
    String lastFilter;

    int itemsPerPage = DEFAULT_ITEMS_PER_PAGE;
    List<Facet> facets;
    PageContainer pageContainer;
    protected List<SearchTab> tabs;
    protected List<SortOption> sortOptions;
    List<Dimension> selectedDimensions;
    SearchForm searchForm;
    CanonicalUrl canonicalUrl;

    int totalNumberOfResults = 0;
    int currentNumberOfResults = 0;
    int offset = 0;
    String dymTerm = null;
    String basePath;

    protected UrlHelper urlHelper;

    @Override
    public void doAction() {

        /*
        Map<String, Object> dynamicAttributes = getDynamicAttributes();
        if (dynamicAttributes == null) {
            throw new RuntimeException("could not retrieve dynamic attributes");
        }
        for (Map.Entry<String, Object> entry : dynamicAttributes.entrySet()) {
            if (entry.getValue() instanceof String) {
                serviceParameters.put(entry.getKey(), (String) entry.getValue());
            } else {
                throw new RuntimeException("dynamic attribute values must be of type String");
            }
        }
        */

        basePath = getCurrentPage().getPath();

        urlHelper = getUrlHelper();
        currentSearchType = urlHelper.getSearchType();
        setServiceName(currentSearchType.getServiceName());
        logger.debug("set current search type to {}", currentSearchType != null ? currentSearchType.getLabel() : "null");
        itemsPerPage = currentSearchType.getItemsPerPage();
        dimensions = urlHelper.getDimensions();
        currentPageNumber = urlHelper.getPageNumber();
        searchTerm = urlHelper.getSearchTerm();
        currentSortKey = urlHelper.getSortKey();
        logger.debug("set current sort key to {}", currentSortKey != null ? currentSortKey.getLabel() : "null");
        lastFilter = urlHelper.getFilter();
        searchForm = urlHelper.getSearchForm();

        offset = (currentPageNumber - 1) * itemsPerPage;

        // Sets the parameters to be returned when the superclass calls getSearchParameters();
        serviceParameters.putAll(getServiceRequestParameters());

        SearchResponse searchResponse = getSearchResponse();
        if (searchResponse != null && searchResponse.isValid()) {
            Map<String, Object> map = getSearchResponseMap();
            if (map != null && !map.isEmpty()) {
                dymTerm = MapUtil.getValueFromMap(map, MAP_DYM_PATH, String.class);

                String totalAssetsStr = MapUtil.getValueFromMap(map, MAP_TOTAL_ASSETS_PATH, String.class);
                if (totalAssetsStr != null) {
                    // yes, can throw number format exception
                    totalNumberOfResults = Integer.valueOf(totalAssetsStr);
                } else {
                    throw new RuntimeException("total assets property not available in response");
                }
                logger.debug("totalNumberOfResults {}", totalNumberOfResults);

                if (totalNumberOfResults > 0) {
                    int resultsLeft = totalNumberOfResults - offset;
                    currentNumberOfResults = resultsLeft > itemsPerPage ? itemsPerPage : resultsLeft;
                }
                logger.debug("currentNumberOfResults {}", currentNumberOfResults);
            }
        }
    }

    @Override
    public Map<String, String> getSearchParameters() {
        return serviceParameters;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public int getTotalNumberOfResults () {
        return totalNumberOfResults;
    }

    public int getCurrentNumberOfResults() {
        return currentNumberOfResults;
    }

    public int getResultStartIndex() {
        return offset + 1;
    }

    public int getResultEndIndex() {
        return offset + currentNumberOfResults;
    }

    public String getResultTypeSingular() {
        return currentSearchType.getResultTypeSingular();
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public String getDymTerm() {
        return dymTerm;
    }

    public String getCurrentTabName() {
        return currentSearchType.name();
    }

    public String getClearSandboxUrl() {
        return urlHelper.getSearchUrlRemoveAllDimensions();
    }

    /** Returns a list of facets to be used for presenting the filtering options. */
    public List<Facet> getFacets() {
        if (facets == null) {
            facets = new ArrayList<Facet>();
            Map<FacetKey, Facet> map = getFilterKeysFromMap();
            SearchType searchType = currentSearchType != null ? currentSearchType : currentSearchType.all;
            for (FacetKey fk : searchType.getFacetKeys()) {
                if (map.containsKey(fk)) {
                    facets.add(map.get(fk));
                } else {
                    Facet newFacet =  new Facet(null, fk.getFacetDisplayName(currentSearchType));
                    facets.add(newFacet);
                }
            }
        }
        return facets;
    }

    public List<Dimension> getSelectedDimensions() {
        if (selectedDimensions == null) {
            selectedDimensions = new ArrayList<Dimension>();
            if (dimensions != null) {
                for (String dim : dimensions) {
                    // This split is here to support packing facetName, dimensionName, dimensionId into a single
                    // string using a separator character.
                    String[] dimArray = dim.split(UrlHelper.UNIT_SEPARATOR);
                    String facetName;
                    String dimensionName;
                    String dimensionId;
                    if (dimArray.length == 3) {
                        facetName = dimArray[0];
                        dimensionName = dimArray[1];
                        dimensionId = dimArray[2];
                    } else {
                        facetName = dim;
                        dimensionName = dim;
                        dimensionId = dim;
                    }
                    selectedDimensions.add(new Dimension(dimensionName, facetName, dimensionId, null,
                                                urlHelper.getSearchUrlRemovingDimension(dim)));
                }
            }
        }

        return selectedDimensions;
    }

    /** Returns the pagination information for use in presenting the pagination interface. */
    public PageContainer getPageContainer() {
        if (pageContainer == null) {
            pageContainer = new PageContainer(currentPageNumber, totalNumberOfResults, itemsPerPage, urlHelper);
        }

        return pageContainer;
    }

    /** Returns a list of tabs for presenting the tab interface. */
    public List<SearchTab> getSearchTabs() {
        if (tabs == null) {
            tabs = new ArrayList<SearchTab>();
            List<SearchType> order = Arrays.asList(SearchType.all, SearchType.recipes, SearchType.videos, SearchType.episodes,
                    SearchType.articles, SearchType.galleries);

            for (SearchType tab : order) {
                tabs.add(new SearchTab(tab, null,
                        currentSearchType != tab
                        ? urlHelper.getSearchUrlForSearchType(tab)
                        : null));
            }
        }
        return tabs;
    }

    public List<SortOption> getSortOptions() {
        if (sortOptions == null) {
            sortOptions = new ArrayList<SortOption>();
            if (currentSearchType == SearchType.recipes) {
                logger.debug("build sort options with current sort key {}", currentSortKey.name());
                sortOptions.add(new SortOption(SortKey.relevancy.getLabel(),
                        currentSortKey != SortKey.relevancy
                                ? urlHelper.getSearchUrlForSortKey(SortKey.relevancy)
                                : null));
                sortOptions.add(new SortOption(SortKey.rating.getLabel(),
                        currentSortKey != SortKey.rating
                            ? urlHelper.getSearchUrlForSortKey(SortKey.rating)
                            : null));
                sortOptions.add(new SortOption(SortKey.popular.getLabel(),
                        currentSortKey != SortKey.popular
                            ? urlHelper.getSearchUrlForSortKey(SortKey.popular)
                            : null));
           }
        }
        return sortOptions;
    }

    public ValueMap getSearchMetadataMap() {
        Map<String, Object> mdMap = new HashMap<String, Object>();
        SniPage sniPage = getCurrentSniPage();
        Page page = getCurrentPage();
        String brand = "";
        String mdSearchTerm = searchTerm != null ? searchTerm.toLowerCase(Locale.US) : "";
        mdSearchTerm = mdSearchTerm.replaceAll("&", "and");

        if (page != null && sniPage != null) {
            brand = sniPage.getBrand();
            Resource pageResource = page.getContentResource();

            if (pageResource != null) {

                ResourceResolver resourceResolver = pageResource.getResourceResolver();
                if (resourceResolver !=null){
                    String metadataTerm = dymTerm != null && !dymTerm.trim().isEmpty() ? dymTerm : mdSearchTerm;
                    SearchTermMetadata searchTermMetadata = SearchTermMetadata.getSearchTermMetadata(brand, metadataTerm, resourceResolver);

                    if (searchTermMetadata != null) {
                        String sponsorship = searchTermMetadata.getSponsorshipValue();
                        String adKey1 = searchTermMetadata.getAdKey1();
                        String adKey2 = searchTermMetadata.getAdKey2();

                        if (sponsorship != null && !sponsorship.trim().isEmpty()) {
                            mdMap.put(MetadataProperty.SPONSORSHIP.getMetadataName(), sponsorship);
                        }

                        if (adKey1 != null && !adKey1.trim().isEmpty()) {
                            mdMap.put(MetadataProperty.ADKEY1.getMetadataName(), adKey1);
                        }

                        if (adKey2 != null && !adKey2.trim().isEmpty()) {
                            mdMap.put(MetadataProperty.ADKEY2.getMetadataName(), adKey2);
                        }
                    }
                }
            }
        }
        String tabPrefix = MDVAL_TYPE + "-" + currentSearchType.getLabel().toLowerCase(Locale.US);

        mdMap.put(MetadataProperty.UNIQUEID.getMetadataName(),
                brand.toLowerCase(Locale.US) + "-"
                + tabPrefix + "-"
                + mdSearchTerm + "-"
                + currentPageNumber);
        mdMap.put(MetadataProperty.PAGENUMBER.getMetadataName(), String.valueOf(currentPageNumber));

        if (lastFilter == null) {
            mdMap.put(MetadataProperty.SEARCHTERMS.getMetadataName(), mdSearchTerm);
            mdMap.put(MetadataProperty.FILTER.getMetadataName(), "search:" + mdSearchTerm);
        } else {
            StringBuilder filterValue = new StringBuilder();
            if (UrlHelper.FILTER_PAGE.equals(lastFilter)) {
                filterValue.append(lastFilter).append(MDVAL_DELIMITER).append(currentPageNumber);
            } else if (UrlHelper.FILTER_REFINEMENT.equals(lastFilter)) {
                filterValue.append(lastFilter).append(MDVAL_DELIMITER);
                boolean firstFilter = true;
                for (Dimension dim : getSelectedDimensions()) {
                    if (!firstFilter) {
                        filterValue.append(MDVAL_SEPARATOR);
                    }
                    filterValue.append(dim.getFacetName()).append(MDVAL_DELIMITER).append(dim.getName());
                }
            } else if (UrlHelper.FILTER_TAB.equals(lastFilter)) {
                filterValue.append(lastFilter).append(MDVAL_DELIMITER).append(currentSearchType.getLabel());
            } else if (UrlHelper.FILTER_SORT.equals(lastFilter)) {
                filterValue.append(lastFilter).append(MDVAL_DELIMITER).append(currentSortKey.getLabel());
            } else {
                filterValue.append(lastFilter);
            }
            mdMap.put(MetadataProperty.FILTER.getMetadataName(), filterValue.toString());
        }

        List<Dimension> dimensions = getSelectedDimensions();
        if (dimensions != null) {
            StringBuilder dimensionsSb = new StringBuilder();
            StringBuilder dimensionValuesSb = new StringBuilder();
            boolean first = true;
            for (Dimension d : dimensions) {
                if (!first) {
                    dimensionsSb.append(",");
                    dimensionValuesSb.append(",");
                }
                dimensionsSb.append(d.getFacetName());
                dimensionValuesSb.append(d.getFacetName() + ":" +  d.getName());
                first = false;
            }

            mdMap.put(MetadataProperty.DIMENSIONS.getMetadataName(), dimensionsSb.toString().toLowerCase(Locale.US));
            mdMap.put(MetadataProperty.DIMENSIONVALUES.getMetadataName(), dimensionValuesSb.toString().toLowerCase(Locale.US));
        }

        mdMap.put(MetadataProperty.KEYTERM.getMetadataName(), mdSearchTerm);
        mdMap.put(MetadataProperty.SORT.getMetadataName(), currentSortKey != null ? currentSortKey.getKey() : null);
        mdMap.put(MetadataProperty.CATEGORYDSPNAME.getMetadataName(), MDVAL_CATEGORYDSPNAME);
        mdMap.put(MetadataProperty.SCTNDSPNAME.getMetadataName(), tabPrefix.toUpperCase());
        mdMap.put(MetadataProperty.CLASSIFICATION.getMetadataName(), (MDVAL_TYPE + MDVAL_SEPARATOR + brand).toLowerCase(Locale.US));
        mdMap.put(MetadataProperty.TYPE.getMetadataName(), MDVAL_TYPE.toLowerCase(Locale.US));

        if (searchForm != null) {
            mdMap.put(MetadataProperty.INTERNALSEARCHTYPE.getMetadataName(), searchForm.getMetadataValue());
            logger.debug("added search form {} to mdMap", searchForm.getMetadataValue());
        } else {
            logger.debug("searchForm not set");
        }

        SlingHttpServletRequest slingRequest = getSlingRequest();
        if (slingRequest != null) {
            StringBuilder url = new StringBuilder(slingRequest.getPathInfo().toString());
            String queryString = slingRequest.getQueryString();
            if (queryString != null && !queryString.trim().isEmpty()) {
                url.append("?").append(queryString);
            }
            mdMap.put(MetadataProperty.URL.getMetadataName(), url.toString());
        }

        /* DYMTERM is defunct according to https://wiki.scrippsnetworks.com/display/REQMGMT/Site+Search+Omniture+Requirements
        if (dymTerm != null && !dymTerm.trim().isEmpty()) {
            mdMap.put(MetadataProperty.DYMTERM.getMetadataName(), MDVAL_DYM + MDVAL_DELIMITER + dymTerm + MDVAL_DELIMITER + mdSearchTerm);
        }
        */

        if (dymTerm != null && !dymTerm.trim().isEmpty()) {
            mdMap.put(MetadataProperty.NOSEARCHRESULTS.getMetadataName(), MDVAL_DYM + MDVAL_DELIMITER + mdSearchTerm + MDVAL_DELIMITER + dymTerm);
        } else if (totalNumberOfResults == 0) {
            mdMap.put(MetadataProperty.NOSEARCHRESULTS.getMetadataName(), MDVAL_NO_SEARCH_RESULTS + MDVAL_DELIMITER + searchTerm);
        }



        return new ValueMapDecorator(mdMap);
    }

    /** Returns a JSON object string containing search-specific metadata properties for the current request.
     *
     * The values are extracted and calculated from the request and the service response.
     *
     * See https://wiki.scrippsnetworks.com/display/REQMGMT/Site+Search+Omniture+Requirements
     */
    public String getSearchMetadataJson() throws JSONException {
        ValueMap mdMap = getSearchMetadataMap();
        JSONObject json = new JSONObject();
        for (String key : mdMap.keySet()) {
            json.put(key, mdMap.get(key, String.class));
        }
        return json.toString();
    }

    /** Responsible for preparing the map of parameters for the service endpoint request. */
    private Map<String, String> getServiceRequestParameters() {
        Map<String, String> retVal = new HashMap<String, String>();
        if (dimensions != null && dimensions.size() > 0) {
            StringBuilder dimValue = new StringBuilder();
            boolean firstDimension = true;
            for (String dim : dimensions) {

                if (!firstDimension) {
                    dimValue.append(",");
                }
                dimValue.append(UrlHelper.getDimensionIdFromPackedDimension(dim));
                firstDimension = false;
            }
            retVal.put(PARAM_FILTER, dimValue.toString());
        }

        retVal.put(PARAM_OFFSET, String.valueOf((currentPageNumber - 1) * itemsPerPage));
        retVal.put(PARAM_NUM_OF_RESULTS, String.valueOf(itemsPerPage));
        retVal.put(PARAM_SEARCH_TERM, searchTerm);
        if (currentSortKey != null) {
            String sortBy = currentSortKey.getKey();
            if (sortBy != null && !sortBy.trim().isEmpty()) {
                retVal.put(PARAM_SORT_BY, sortBy);
            }
        }

        if (logger.isDebugEnabled()) {
            for (Map.Entry<String, String> entry : retVal.entrySet()) {
                logger.debug("service param {} = {}", entry.getKey(), entry.getValue());
            }
        }

        return retVal;
    }

    private Map<FacetKey, Facet> getFilterKeysFromMap() {
        Map<String, Object> map = getSearchResponseMap();
        List<Facet> retVal = new ArrayList<Facet>();
        Map<FacetKey, Facet> retMap = new HashMap<FacetKey, Facet>();

        Map<String, Object> facets = MapUtil.getObjectFromMap(map, MAP_REFINEMENTS_PATH);
        if (facets != null) {
            for (Map.Entry<String, Object> facetEntry : facets.entrySet()) {
                FacetKey facetKey = FacetKey.valueForPropertyName(facetEntry.getKey());
                if (facetKey != null) {
                    Facet facet = new Facet(facetEntry.getKey(), facetKey.getFacetDisplayName(currentSearchType));
                    List<Object> dimensionList = (List<Object>) facetEntry.getValue();
                    boolean isSelected = false;
                    for (Object dimensionObject : dimensionList) {
                        Map<String, String> dimensionMap = (Map<String, String>) dimensionObject;
                        String dimensionId = dimensionMap.get(REFINEMENT_ID_PROPERTY);
                        String dimensionName = dimensionMap.get(REFINEMENT_NAME_PROPERTY);
                        String dimensionCount = dimensionMap.get(REFINEMENT_COUNT_PROPERTY);

                        // Without a dimension id the dimension is worthless. Without a name, we can't label it correctly.
                        if (dimensionId == null || dimensionName == null) {
                            continue;
                        }

                        String facetName = facet.getDisplayName();

                        // Yes, this is ridiculous.
                        if (FacetKey.SOURCE.equals(facetKey)) {
                            String sourceKey = dimensionName == null ? "" : dimensionName.toLowerCase();
                            if (acceptableSources.containsKey(dimensionName.toLowerCase())) {
                                dimensionName = acceptableSources.get(sourceKey);
                            } else {
                                continue;
                            }
                        }

                        String packedDimension = UrlHelper.getPackedDimension(facetName, dimensionName, dimensionId);
                        String url = urlHelper.getSearchUrlAddingDimension(packedDimension);

                        Dimension dimension = new Dimension(
                                dimensionName,
                                facetName,
                                dimensionId,
                                Integer.valueOf(dimensionCount),
                                url);

                        isSelected = isSelected || this.dimensions.contains(packedDimension);

                        logger.debug("dimension {} isSelected {}", dimension.getId(), isSelected);
                        facet.addDimension(dimension);
                    }
                    if (!isSelected) {
                        logger.debug("adding facet {}", facet.getDisplayName());
                        retVal.add(facet);
                        retMap.put(facetKey, facet);
                    }
                }
            }
        }

        return retMap;
    }

    public UrlHelper getUrlHelper() {
        if (urlHelper == null) {
            urlHelper = new UrlHelper(getSlingRequest());
        }
        return urlHelper;
    }

    public CanonicalUrl getCanonicalUrl() {
        if (canonicalUrl == null) {
            canonicalUrl = new CanonicalUrl(getCurrentSniPage(), getPageContainer(), getUrlHelper());
        }
        return canonicalUrl;
    }

}
