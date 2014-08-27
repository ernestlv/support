package com.scrippsnetworks.wcm.search;

import org.codehaus.jackson.map.ObjectMapper;
import com.scrippsnetworks.wcm.search.SearchResponse;
import java.util.Map;
import java.util.HashMap;

/**
 * A utility class for deserializing JSON string responses from search requests.
 */
public class SearchObjectMapper {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    /** Currently unused, maps service names to POJO classes. */
    public final static Map<String, Class> objectMap = new HashMap<String, Class>();
    static {
        objectMap.put("topRecipes",Map.class);
        objectMap.put("weRecommend",Map.class);
        objectMap.put("episodeFinder",Map.class);
        objectMap.put("mealFinder",Map.class);
        objectMap.put("exploreMoreOn",Map.class);
        objectMap.put("filterByTopic",Map.class);
        objectMap.put("packageSimilarRecipes",Map.class);
        objectMap.put("photoGalleryEndCap",Map.class);
        objectMap.put("similarRecipes",Map.class);
        objectMap.put("topicFeaturedShows",Map.class);
        objectMap.put("topicVideos",Map.class);
        objectMap.put("topTechniques",Map.class);
        objectMap.put("photoGalleryEndCap",Map.class);
    }

    /* NOTE: use static block to configure object mapper
     * static {
     *   objectMapper.configure(...);
     * }
     */

    /**
     * Gets a generic Map-based object constructed from payload of the search response.
     *
     * @param searchResponse SearchResponse object returned by the SearchRequestHandler.
     * @return A Map following the rules for "Simple Data Binding" described in the 
     * <a href="http://wiki.fasterxml.com/JacksonInFiveMinutes#Simple_Data_Binding_Example">Jackson documentation</a>,
     * or null if the SearchResponse could not be deserialized.
     */
    public static Map<String,Object> getAsMap(SearchResponse searchResponse) {
        if (searchResponse.isValid()) {
            return (Map<String,Object>) fromJson(searchResponse.getPayload(), Map.class);
        } else {
            return null;
        }
    }

    /**
     * Gets a custom POJO constructed from the payload of the search response.
     *
     * @param searchResponse SearchResponse object returned from SearchRequestHandler
     * @param pojoClass Class of custom POJO type
     * @return Custom POJO, or null if deserialization fails.
     */
    public static <T> Object getAsPojo(SearchResponse searchResponse, Class<T> pojoClass) {
        if (searchResponse.isValid()) {
            return fromJson(searchResponse.getPayload(), pojoClass);
        } else {
            return null;
        }
    }

    /**
     * Gets a custom POJO constructed using from the provided string.
     *
     * @param jsonAsString String value containing JSON to deserialize
     * @param pojoClass Class of custom POJO type
     * @return Custom POJO, or null if deserialization fails.
     */
    private static <T> Object fromJson(String jsonAsString, Class<T> pojoClass) {
        try {
            return objectMapper.readValue(jsonAsString, pojoClass);
        } catch (Exception e) {
            return null;
        }
    }

}
