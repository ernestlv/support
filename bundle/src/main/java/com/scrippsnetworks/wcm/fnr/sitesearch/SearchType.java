package com.scrippsnetworks.wcm.fnr.sitesearch;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum SearchType {
    all("siteSearch", "All", "", 10, Collections.EMPTY_LIST),
    recipes("recipeSearch", "Recipes", "recipe", 10, Arrays.asList(FacetKey.TALENT, FacetKey.SHOW, FacetKey.CUISINE, FacetKey.COURSE, FacetKey.MAIN_INGREDIENT, FacetKey.DISH, FacetKey.SOURCE)),
    episodes("episodeSearch", "Episodes", "episode", 10, Arrays.asList(FacetKey.SHOW, FacetKey.TALENT, FacetKey.AIRDATE)),
    articles("articleAndBlogSearch", "Articles & Blog Posts", "article & blog post", 10, Collections.EMPTY_LIST),
    videos("videoSearch", "Videos", "video", 10, Collections.EMPTY_LIST),
    galleries("photoGallerySearch", "Photo Galleries", "photo gallery", 12, Collections.EMPTY_LIST),
    mobile_recipes("recipeSearch", "Recipes", "recipe", 10, Arrays.asList(FacetKey.TALENT, FacetKey.SHOW, FacetKey.CUISINE, FacetKey.MAIN_INGREDIENT, FacetKey.DISH)),
    mobile_episodes("episodeSearch", "Episodes", "episode", 10, Arrays.asList(FacetKey.SHOW, FacetKey.TALENT, FacetKey.AIRDATE)),
    mobile_videos("videoSearch", "Videos", "video", 10, Collections.EMPTY_LIST);

    final String serviceName;
    final String label;
    final String resultTypeSingular;
    final int itemsPerPage;
    List<FacetKey> facetKeys;

    SearchType(String serviceName, String label, String resultTypeSingular, int itemsPerPage, List<FacetKey> facetKeys) {
        this.serviceName = serviceName;
        this.label = label;
        this.resultTypeSingular = resultTypeSingular;
        this.itemsPerPage = itemsPerPage;
        this.facetKeys = facetKeys;
    }

    String getServiceName() {
        return serviceName;
    }

    String getLabel() {
        return label;
    }

    String getResultTypeSingular() {
        return resultTypeSingular;
    }

    int getItemsPerPage() {
        return itemsPerPage;
    }

    public List<FacetKey> getFacetKeys() {
        return facetKeys;
    }
}
