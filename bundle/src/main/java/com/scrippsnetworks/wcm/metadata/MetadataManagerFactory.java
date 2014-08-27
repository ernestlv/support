package com.scrippsnetworks.wcm.metadata;

import java.util.List;
import java.util.ArrayList;
import com.scrippsnetworks.wcm.metadata.impl.BaseMetadataManager;
import com.scrippsnetworks.wcm.metadata.impl.provider.*;
import com.scrippsnetworks.wcm.page.SniPage;

import org.apache.commons.lang.exception.ExceptionUtils;

public class MetadataManagerFactory {
    public static final String SEARCH_METADATA_REQUEST_ATTRIBUTE = "com.scrippsnetworks.wcm.metadata.searchMetadataMap";

    public static MetadataManager getMetadataManager(SniPage page) {

        if (page == null) {
            return null;
        }

        List<MetadataProvider> providers = new ArrayList<MetadataProvider>();
        try {
            MetadataProvider content = new BaseContentMetadataProvider(page);
            providers.add(content);
        } catch (Exception e) {}

        try {
            MetadataProvider location = new BaseLocationMetadataProvider(page);
            providers.add(location);
        } catch (Exception e) {}

        try {
            MetadataProvider tags = new BaseTagMetadataProvider(page);
            providers.add(tags);
        } catch (Exception e) {}

        try {
            MetadataProvider sponsor = new BaseSponsorshipMetadataProvider(page);
            providers.add(sponsor);
        } catch (Exception e) {}
        
        try {
            MetadataProvider company = new CompanyMetadataProvider(page);
            providers.add(company);
        } catch (Exception e) {}
        
        try {
            MetadataProvider recipe = new RecipeMetadataProvider(page);
            providers.add(recipe);
        } catch (Exception e) {}

        try {
            if ("search-results".equals(page.getPageType())) {
                MetadataProvider search = new BaseSearchMetadataProvider(page);
                providers.add(search);
            }
        } catch (Exception e) {}

        return new BaseMetadataManager(providers);
    }

}
