package com.scrippsnetworks.wcm.metadata.impl.provider;

import java.lang.IllegalArgumentException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.scrippsnetworks.wcm.metadata.MetadataManagerFactory;
import org.apache.commons.lang.StringUtils;
import com.scrippsnetworks.wcm.metadata.MetadataProvider;
import com.scrippsnetworks.wcm.metadata.MetadataProperty;
import com.scrippsnetworks.wcm.page.SniPage;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;

import static com.scrippsnetworks.wcm.metadata.MetadataProperty.*;
import static com.scrippsnetworks.wcm.page.PagePropertyConstants.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseSearchMetadataProvider implements MetadataProvider {
    private SniPage page;

    ValueMap searchMetadata;

    Logger logger = LoggerFactory.getLogger(BaseSearchMetadataProvider.class);

    public BaseSearchMetadataProvider(SniPage page) {
        if (page == null) {
            throw new IllegalArgumentException("page must not be null");
        }

        logger.info("constructor");

        this.page = page;
        SlingHttpServletRequest request = page.getSlingRequest();
        if (request != null) {
            logger.info("request nonnull, setting searchMetadata property");
            searchMetadata = (ValueMap)request.getAttribute(MetadataManagerFactory.SEARCH_METADATA_REQUEST_ATTRIBUTE);
        }

        if (searchMetadata == null) {
            logger.info("setting searchMetadata to empty value map");
            searchMetadata = ValueMap.EMPTY;
        }
    }

    public List<MetadataProperty> provides() {
        return Arrays.asList(ADKEY1, ADKEY2, CATEGORYDSPNAME, CLASSIFICATION, DIMENSIONS, DIMENSIONVALUES, FILTER, INTERNALSEARCHTYPE,
                KEYTERM, NOSEARCHRESULTS, PAGENUMBER, SCTNDSPNAME, SEARCHTERMS, SORT, SPONSORSHIP, TYPE, UNIQUEID, URL,
                SPOTLIGHT_1, SPOTLIGHT_2, KEYWORDS);
    }

    public String getProperty(MetadataProperty prop) {
        String retVal = null;

        return searchMetadata.get(prop.getMetadataName(), String.class);
    }
}

