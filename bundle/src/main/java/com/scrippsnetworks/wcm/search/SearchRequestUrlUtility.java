package com.scrippsnetworks.wcm.search;

import org.apache.http.client.utils.URIBuilder;

import java.lang.String;
import java.lang.StringBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility class for building search service request URLs.
 */
public class SearchRequestUrlUtility {

    /** Struct to bundle search service request parameter metadata. */
    public static class SearchParameter {
        /** Name of parameter, as it would appear in the service request URL. */
        public String paramName;
        /** Whether the parameter is optional or not. */
        public Boolean optional = false;
        /** Whether the param is passed as a query parameter. */
        public Boolean queryParam = false;
        // Could validate type by class

        public SearchParameter(String paramName, Boolean optional, Boolean queryParam) {
            this.paramName = paramName;
            this.optional = optional;
            this.queryParam = queryParam;
        }
    }

    /** Default parameter list for search service requests. Most services accept these. */
    private static final List<SearchRequestUrlUtility.SearchParameter> defaultParams = Arrays.asList(
                    new SearchRequestUrlUtility.SearchParameter("pageType",false,false),
                    new SearchRequestUrlUtility.SearchParameter("uid",false,false),
                    new SearchRequestUrlUtility.SearchParameter("numOfResults",true,true),
                    new SearchRequestUrlUtility.SearchParameter("offset",true,true));

    private static Map<String, Map<String,List<SearchRequestUrlUtility.SearchParameter>>> requestParameters =
            new HashMap<String, Map<String,List<SearchRequestUrlUtility.SearchParameter>>>();

    /** Map defining parameters used in service requests */
    private static Map<String,List<SearchRequestUrlUtility.SearchParameter>> cookRequestParameters
        = new HashMap<String,List<SearchRequestUrlUtility.SearchParameter>>();

    private static Map<String,List<SearchRequestUrlUtility.SearchParameter>> foodRequestParameters
        = new HashMap<String,List<SearchRequestUrlUtility.SearchParameter>>();
    static {
        cookRequestParameters.put("topRecipes", defaultParams);
        cookRequestParameters.put("weRecommend", defaultParams);
        cookRequestParameters.put("episodeFinder", new ArrayList<SearchRequestUrlUtility.SearchParameter>());
        cookRequestParameters.put("mealFinder", new ArrayList<SearchRequestUrlUtility.SearchParameter>());
        cookRequestParameters.put("exploreMoreOn", defaultParams);
        cookRequestParameters.put("filterByTopic",
                Arrays.asList(new SearchRequestUrlUtility.SearchParameter("searchTerm", false, false)));
        cookRequestParameters.put("packageSimilarRecipes", defaultParams);
        cookRequestParameters.put("photoGalleryEndCap", defaultParams);
        cookRequestParameters.put("nextRecipe", defaultParams);
        cookRequestParameters.put("similarRecipes",
                Arrays.asList(new SearchRequestUrlUtility.SearchParameter("nextRecipe", false, false))
        );
        cookRequestParameters.put("topicFeaturedShows",
                Arrays.asList(new SearchRequestUrlUtility.SearchParameter("topicTags", false, false),
                        new SearchRequestUrlUtility.SearchParameter("offset", true, true),
                        new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true))
        );
        cookRequestParameters.put("topTechniques", defaultParams);
        cookRequestParameters.put("search",
                Arrays.asList(new SearchRequestUrlUtility.SearchParameter("searchTerm", true, true),
                        new SearchRequestUrlUtility.SearchParameter("offset", true, true),
                        new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true),
                        new SearchRequestUrlUtility.SearchParameter("dimensions", true, true),
                        new SearchRequestUrlUtility.SearchParameter("sortBy", true, true),
                        new SearchRequestUrlUtility.SearchParameter("talentAssetUid", true, true),
                        new SearchRequestUrlUtility.SearchParameter("showAssetUid", true, true))
        );
        cookRequestParameters.put("mostPopularSearchTerms", new ArrayList<SearchRequestUrlUtility.SearchParameter>());
        cookRequestParameters.put("topic",
                Arrays.asList(new SearchRequestUrlUtility.SearchParameter("topicTags", false, false),
                        new SearchRequestUrlUtility.SearchParameter("offset", true, true),
                        new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true),
                        new SearchRequestUrlUtility.SearchParameter("dimensions", true, true),
                        new SearchRequestUrlUtility.SearchParameter("searchTerm", true, true))
        );
        cookRequestParameters.put("topicVideos",
                Arrays.asList(new SearchRequestUrlUtility.SearchParameter("topicTags", false, false),
                        new SearchRequestUrlUtility.SearchParameter("offset", true, true),
                        new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true))
        );
        cookRequestParameters.put("typeAhead",
                Arrays.asList(new SearchRequestUrlUtility.SearchParameter("searchTerm", false, false))
        );
        cookRequestParameters.put("episodeSearch",
                Arrays.asList(new SearchRequestUrlUtility.SearchParameter("searchTerm", true, true),
                        new SearchRequestUrlUtility.SearchParameter("showDim", true, true),
                        new SearchRequestUrlUtility.SearchParameter("talentDim", true, true),
                        new SearchRequestUrlUtility.SearchParameter("startDate", true, true),
                        new SearchRequestUrlUtility.SearchParameter("endDate", true, true),
                        new SearchRequestUrlUtility.SearchParameter("offset", true, true),
                        new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true))
        );
        cookRequestParameters.put("episodeFinder",
                new ArrayList<SearchRequestUrlUtility.SearchParameter>()
        );
        requestParameters.put("cook", cookRequestParameters);

        foodRequestParameters.put("local/inBoundaryDetails", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("upLatitude", false, true),
        	new SearchRequestUrlUtility.SearchParameter("downLatitude", false, true),
        	new SearchRequestUrlUtility.SearchParameter("leftLongitude", false, true),
        	new SearchRequestUrlUtility.SearchParameter("rightLongitude", false, true),
        	new SearchRequestUrlUtility.SearchParameter("showName", true, true), // TODO: What's optional here?
        	new SearchRequestUrlUtility.SearchParameter("talentName", true, true),
        	new SearchRequestUrlUtility.SearchParameter("restaurantName", true, true),
        	new SearchRequestUrlUtility.SearchParameter("offset", true, true),
        	new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true),
        	new SearchRequestUrlUtility.SearchParameter("sortBy", true, true)));
        foodRequestParameters.put("recipeBySource", Arrays.asList(
			new SearchRequestUrlUtility.SearchParameter("source", false, false),
        	new SearchRequestUrlUtility.SearchParameter("filter", true, true),
        	new SearchRequestUrlUtility.SearchParameter("offset", true, true),
        	new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true)));
        foodRequestParameters.put("recipesFromShowOrTalent", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("assetType", false, false),
        	new SearchRequestUrlUtility.SearchParameter("assetId", false, false),
        	new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true)));
        foodRequestParameters.put("local/nearByCompany", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("lat", true, true),
        	new SearchRequestUrlUtility.SearchParameter("lng", true, true),
        	new SearchRequestUrlUtility.SearchParameter("zipcode", true, true),
        	new SearchRequestUrlUtility.SearchParameter("distance", true, true),
        	new SearchRequestUrlUtility.SearchParameter("companyId", true, true), // TODO: what's optional here?
        	new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true)));
        foodRequestParameters.put("foodTermsSearch", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("searchTerm", false, false),
        	new SearchRequestUrlUtility.SearchParameter("offset", true, true),
        	new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true)));
        foodRequestParameters.put("episodeSearch", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("searchTerm", false, true),
        	new SearchRequestUrlUtility.SearchParameter("filter", true, true),
        	new SearchRequestUrlUtility.SearchParameter("offset", true, true),
        	new SearchRequestUrlUtility.SearchParameter("airDateFrom", true, true),
        	new SearchRequestUrlUtility.SearchParameter("airDateTo", true, true),
        	new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true)));
        foodRequestParameters.put("photoGallerySearch", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("searchTerm", false, true),
        	new SearchRequestUrlUtility.SearchParameter("offset", true, true),
        	new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true)));
        foodRequestParameters.put("siteSearch", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("searchTerm", false, true),
        	new SearchRequestUrlUtility.SearchParameter("offset", true, true),
        	new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true)));
        foodRequestParameters.put("topicSearch", Arrays.asList(
            new SearchRequestUrlUtility.SearchParameter("assetId", true, false),
            new SearchRequestUrlUtility.SearchParameter("keywords", true, false),
            new SearchRequestUrlUtility.SearchParameter("topicTags", true, false),
        	new SearchRequestUrlUtility.SearchParameter("resultType", true, true),
        	new SearchRequestUrlUtility.SearchParameter("excludeAssets", true, true),
        	new SearchRequestUrlUtility.SearchParameter("offset", true, true),
        	new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true)));
        foodRequestParameters.put("relatedTopics", Arrays.asList(
            new SearchRequestUrlUtility.SearchParameter("assetId", true, false),
        	new SearchRequestUrlUtility.SearchParameter("offset", true, true),
            new SearchRequestUrlUtility.SearchParameter("sortBy", true, true),
        	new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true)));
        foodRequestParameters.put("local/companyCount", new ArrayList<SearchRequestUrlUtility.SearchParameter>());
        foodRequestParameters.put("local/inBoundaryMap", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("upLatitude", false, true),
        	new SearchRequestUrlUtility.SearchParameter("downLatitude", false, true),
        	new SearchRequestUrlUtility.SearchParameter("leftLongitude", false, true),
        	new SearchRequestUrlUtility.SearchParameter("rightLongitude", false, true),
        	new SearchRequestUrlUtility.SearchParameter("showName", true, true), // TODO: What's optional here?
        	new SearchRequestUrlUtility.SearchParameter("talentName", true, true),
        	new SearchRequestUrlUtility.SearchParameter("restaurantName", true, true)));
        foodRequestParameters.put("syndication", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("assetType", false, false),
        	new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true),
        	new SearchRequestUrlUtility.SearchParameter("vtr", true, true)));
        foodRequestParameters.put("local/companyShowsAndTalent", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("searchTerm", true, true)));
        foodRequestParameters.put("showTopRecipes", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("showId", false, false),
        	new SearchRequestUrlUtility.SearchParameter("sortBy", true, true),
        	new SearchRequestUrlUtility.SearchParameter("offset", true, true),
        	new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true)));
        foodRequestParameters.put("articleAndBlogSearch", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("searchTerm", false, true),
        	new SearchRequestUrlUtility.SearchParameter("offset", true, true),
        	new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true)));
        foodRequestParameters.put("relatedTopics", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("assetId", false, false)));
        foodRequestParameters.put("similarRecipes", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("assetId", false, false),
        	new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true)));
        foodRequestParameters.put("local/citySuggest", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("searchTerm", true, true),
        	new SearchRequestUrlUtility.SearchParameter("zip", true, true)));
        foodRequestParameters.put("relatedVideos", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("assetId", false, true),
        	new SearchRequestUrlUtility.SearchParameter("excludeAssets", true, true),
        	new SearchRequestUrlUtility.SearchParameter("sortBy", true, true),
        	new SearchRequestUrlUtility.SearchParameter("videoType", true, true), // TODO, optional or not?
        	new SearchRequestUrlUtility.SearchParameter("offset", true, true),
        	new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true)));
        foodRequestParameters.put("episodeList", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("searchTerm", false, true)));
        foodRequestParameters.put("videoSearch", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("searchTerm", false, true),
        	new SearchRequestUrlUtility.SearchParameter("offset", true, true),
        	new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true)));
        foodRequestParameters.put("talentTopRecipes", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("talentId", false, false),
        	new SearchRequestUrlUtility.SearchParameter("sortBy", true, true),
        	new SearchRequestUrlUtility.SearchParameter("offset", true, true),
        	new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true)));
        foodRequestParameters.put("recipeList", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("assetType", false, false),
        	new SearchRequestUrlUtility.SearchParameter("assetId", false, false)));
        foodRequestParameters.put("cityList", new ArrayList<SearchRequestUrlUtility.SearchParameter>());
        foodRequestParameters.put("local/companySuggest", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("searchTerm", true, true)));
        foodRequestParameters.put("similarTopics", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("assetId", false, false),
        	new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true)));
        foodRequestParameters.put("foodTermsRecipe", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("assetId", false, false)));
        foodRequestParameters.put("searchTerm", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("searchTerm", false, false)));
        foodRequestParameters.put("itkRecipeSearch", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("searchTerm", true, true),
        	new SearchRequestUrlUtility.SearchParameter("dimensionId", false, true),
        	new SearchRequestUrlUtility.SearchParameter("talentId", false, true),
        	new SearchRequestUrlUtility.SearchParameter("offset", false, true),
        	new SearchRequestUrlUtility.SearchParameter("numOfResults", false, true)));
        foodRequestParameters.put("recipeSearch", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("searchTerm", false, true),
        	new SearchRequestUrlUtility.SearchParameter("filter", true, true),
            new SearchRequestUrlUtility.SearchParameter("sortBy", true, true),
        	new SearchRequestUrlUtility.SearchParameter("offset", true, true),
        	new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true)));
        foodRequestParameters.put("showList", new ArrayList<SearchRequestUrlUtility.SearchParameter>());
        foodRequestParameters.put("recipesOnTVThisWeek", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("assetType", false, false),
        	new SearchRequestUrlUtility.SearchParameter("assetId", false, false)));
        foodRequestParameters.put("local/cityCount", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("searchTerm", true, true)));
        foodRequestParameters.put("videoSearch", Arrays.asList(
        	new SearchRequestUrlUtility.SearchParameter("searchTerm", false, true),
        	new SearchRequestUrlUtility.SearchParameter("offset", true, true),
        	new SearchRequestUrlUtility.SearchParameter("numOfResults", true, true)));
        foodRequestParameters.put("typeAhead", Arrays.asList(
            new SearchRequestUrlUtility.SearchParameter("searchTerm", true, true)));
        foodRequestParameters.put("local/cityList", new ArrayList<SearchRequestUrlUtility.SearchParameter>());
        foodRequestParameters.put("local/showList", new ArrayList<SearchRequestUrlUtility.SearchParameter>());
        foodRequestParameters.put("local/talentList", new ArrayList<SearchRequestUrlUtility.SearchParameter>());
        requestParameters.put("food", foodRequestParameters);

    }

    /**
     * Builds the service request url using the provided parameters, in addition
     * to the host and service context provided in the constructor.
     *
     * @param host String URL of service endpoint with scheme, host, and port (e.g. http://foo.bar.baz:80)
     * @param serviceContext Context path for service requests (e.g., /cook-wcm/service)
     * @param serviceName Name of service, appears as path element in request (e.g. topRecipes)
     * @param params Map of parameter names and values
     * @return URL String for service request.
     * @throws SearchRequestException
     */
    public static String buildURL(String siteName, String host, String serviceContext, String serviceName,
                Map<String,String> params) throws SearchRequestException {

        if (siteName == null || !requestParameters.containsKey(siteName)) {
            throw new SearchRequestException("cannot retrieve request parameter definition");
        }

        List<SearchRequestUrlUtility.SearchParameter> defParams = requestParameters.get(siteName).get(serviceName);

        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(host);
        } catch (URISyntaxException e) {
            throw new SearchRequestException("cannot build URI for host");
        }

        StringBuilder path = new StringBuilder(255);
        path.append(serviceContext).append("/");

        if (defParams == null) {
            throw new SearchRequestException("unknown service name " + serviceName);
        } else {
            path.append(serviceName).append("/");
        }

        for (SearchRequestUrlUtility.SearchParameter p : defParams) {
            String value = params.get(p.paramName);

            // no value for param...bail if required, or skip
            if (value == null) {
                if (!p.optional) {
                    throw new SearchRequestException("required parameter " + p.paramName + " not provided");
                }
                continue;
            }

            if (p.queryParam) {
                uriBuilder.addParameter(p.paramName, value);
            } else {
                path.append(p.paramName).append("/").append(value).append("/");
            }
        }

        uriBuilder.setPath(path.toString());

        return uriBuilder.toString();
    }
}
