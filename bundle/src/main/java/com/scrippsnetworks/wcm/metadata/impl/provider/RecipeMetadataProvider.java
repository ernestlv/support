package com.scrippsnetworks.wcm.metadata.impl.provider;

import java.lang.IllegalArgumentException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import com.scrippsnetworks.wcm.metadata.MetadataProvider;
import com.scrippsnetworks.wcm.metadata.MetadataProperty;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.Recipe;
import com.scrippsnetworks.wcm.recipe.RecipeFactory;
import com.scrippsnetworks.wcm.snitag.SniTag;
import com.scrippsnetworks.wcm.util.PageTypes;
import static com.scrippsnetworks.wcm.metadata.MetadataProperty.*;
import static com.scrippsnetworks.wcm.page.PagePropertyConstants.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecipeMetadataProvider implements MetadataProvider {
    private static final Logger LOG = LoggerFactory.getLogger(RecipeMetadataProvider.class);

    private static final String VALUE_DELIMITER = ",";
    private static final String CROSSLINK_LEADIN = ";";

    private Map<String, MetadataProperty> facetPropertyMap;
    private Recipe recipe;
    private SniPage page;

    /* Metadata expressed as CQ prooperty */
    private String difficulty;
    private String prepTime;
    private String restricted;

    /* Metadata in all page tags (cq:tags, sni:primaryTag, sni:secondaryTag, sni:categories) */
    private Multimap<MetadataProperty, SniTag> categories;

    /* Metadata read from recipe implementation */
    private String crosslinkedTerms;

    public RecipeMetadataProvider(SniPage page) {
        if (page == null) {
            throw new IllegalArgumentException("page must not be null");
        }

        this.page = page;

        this.difficulty = page.getProperties().get(PROP_SNI_DIFFICULTY, String.class);
        this.prepTime = page.getProperties().get(PROP_SNI_PREPTIME, String.class);

        this.restricted = page.getProperties().get(PROP_SNI_RESTRICTED, "false");

        this.categories = Multimaps.synchronizedMultimap(HashMultimap.<MetadataProperty, SniTag>create());
        this.facetPropertyMap = Collections.synchronizedMap(new HashMap());

        this.facetPropertyMap.put("cuisine", CUISINE);
        this.facetPropertyMap.put("dish", DISH);
        this.facetPropertyMap.put("main-ingredient", MAININGREDIENT);
        this.facetPropertyMap.put("meal-part", MEALPART);
        this.facetPropertyMap.put("meal-type", MEALTYPE);
        this.facetPropertyMap.put("nutrition", NUTRITION);
        this.facetPropertyMap.put("occasions", OCCASIONS);
        this.facetPropertyMap.put("taste", TASTE);
        this.facetPropertyMap.put("technique", TECHNIQUE);

        if (page.getPageType().equals(PageTypes.RECIPE.pageType())) {
            recipe = new RecipeFactory()
                .withSniPage(page)
                .build();
            if (recipe != null) {
                for (SniTag tag : recipe.getAllCategories()) {
                    if (facetPropertyMap.containsKey(tag.getFacet())) {
                        categories.put(facetPropertyMap.get(tag.getFacet()), tag);
                    }
                }
            }
        }
    }

    private String semiJoin(List<String> rawTerms) {
        StringBuilder sb = new StringBuilder();

        for (int sj = 0; sj < rawTerms.size(); sj++) {
            sb.append(CROSSLINK_LEADIN).append(rawTerms.get(sj));
            if (sj+1 < rawTerms.size()) {
                sb.append(VALUE_DELIMITER);
                
            }
        }

        return sb.toString();
    }

    public List<MetadataProperty> provides() {
        return Arrays.asList(CROSSLINKTERMS, CUISINE, DIFFICULTY, DISH, MEALTYPE,
            MEALPART, MAININGREDIENT, NUTRITION, OCCASIONS, PREPTIME, RESTRICTED, TASTE, TECHNIQUE);
    }

    public String getProperty(MetadataProperty prop) {
        String retVal = null;
        Collection<SniTag> sniTags;
        List<String> tags = new ArrayList<String>();

        switch (prop) {
            case CUISINE:
            case DISH:
            case MAININGREDIENT:
            case MEALPART:
            case MEALTYPE:
            case NUTRITION:
            case OCCASIONS:
            case TASTE:
            case TECHNIQUE:
                if (categories.containsKey(prop)) {
                    sniTags = categories.get(prop);
                    for (SniTag tag : sniTags) {
                        tags.add(tag.getValue());
                    }
                    retVal = StringUtils.join(tags, VALUE_DELIMITER);
                }
                break;
            case CROSSLINKTERMS:
                if (crosslinkedTerms == null && recipe != null) {
                    crosslinkedTerms = semiJoin(recipe.getCrosslinkedTerms());
                }
                retVal = crosslinkedTerms;
                break;
            case DIFFICULTY:
                retVal = difficulty;
                break;
            case PREPTIME:
                retVal = prepTime;
                break;
            case RESTRICTED:
            	retVal = restricted;
            	break;
            default:
                break;
        }

        return retVal;
    }

}

