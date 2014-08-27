package com.scrippsnetworks.wcm.mobile.lead3imagerecipe;


import com.scrippsnetworks.wcm.mobile.lead3imagerecipe.impl.LeadThreeImageRecipeItemImpl;
import com.scrippsnetworks.wcm.util.CarouselMobileHelper;
import com.scrippsnetworks.wcm.util.HtmlUtil;
import com.scrippsnetworks.wcm.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LeadThreeImageRecipeFactory {
    private static final Logger logger = LoggerFactory.getLogger(LeadThreeImageRecipeFactory.class);

    public static final String PAGE_TYPE = "CORE_PAGE_TYPE";
    public static final String VIDEO_INDICATOR = "VIDEO_INDICATOR";
    public static final String IMAGE_PATH = "CORE_IMAGE_PATH";
    public static final String CORE_TITLE = "CORE_TITLE";
    public static final String CORE_URL = "CORE_URL";
    public static final String TALENT_URL = "CORE_TALENT_URL";
    public static final String TALENT_NAME = "CORE_TALENT_NAME";
    public static final String RECIPE_COPYRIGHT = "RECIPE_COPYRIGHT";
    public static final String VIDEO = "video";
    public static final String PHOTO = "photo";
    public static final String CSS_TO_VIDEO = "link-to-video";
    public static final String CSS_TO_GALLERY = "link-to-gallery";
    public static final String RESPONSE = "response";
    public static final String RESULTS = "results";
    public static final String LEAD_ASSETS = "leadAssets";
    private Map<String, Object> searchResponseMap;
    private Resource resource;


    public LeadThreeImageRecipeFactory withResourceAndSearchMap(Map<String, Object> searchResponseMap, Resource resource) {
        this.searchResponseMap = searchResponseMap;
        this.resource = resource;
        return this;
    }

    public List<LeadThreeImageItemRecipe> build() {
        if (searchResponseMap == null || searchResponseMap.isEmpty()) {
            return null;
        }

        List<LeadThreeImageItemRecipe> items = null;

        try {
            if (searchResponseMap != null) {
                Map<String, Object> response = (Map) searchResponseMap.get(RESPONSE);
                if (response != null) {
                    Map<String, Object> results = (Map) response.get(RESULTS);
                    if (results != null) {
                        ArrayList assets = (ArrayList) results.get(LEAD_ASSETS);
                        if (assets != null && assets.size() > 2) {
                            items = new ArrayList<LeadThreeImageItemRecipe>(3);
                            for (Object obj : assets) {
                                if (obj != null && items.size() < 4) {
                                    Map<String, Object> itemFromSearch = (Map) obj;
                                    LeadThreeImageItemRecipe leadThreeImageItem = setItem(itemFromSearch);
                                    if (leadThreeImageItem != null) {
                                        items.add(leadThreeImageItem);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (ClassCastException ex) {
            logger.error("Wrong searchMap", ex);
        }

        return items;
    }

    private String getProperty(Map<String, Object> leadImageItem, String param) throws ClassCastException {

        Object obj = leadImageItem.get(param);
        if (obj != null) {
            return (String) obj;
        }

        return "";
    }

    private LeadThreeImageItemRecipe setItem(Map<String, Object> leadImageItem) {

        LeadThreeImageItemRecipe item = new LeadThreeImageRecipeItemImpl();

        String pageType = getProperty(leadImageItem, PAGE_TYPE);
        String indicator = getProperty(leadImageItem, VIDEO_INDICATOR);

        if (StringUtils.containsIgnoreCase(pageType, VIDEO) || StringUtils.containsIgnoreCase(indicator, "true")) {
            item.setCssClassName(CSS_TO_VIDEO);
        } else if (StringUtils.containsIgnoreCase(pageType, PHOTO)) {
            item.setCssClassName(CSS_TO_GALLERY);
        }

        item.setImageDamPath(getProperty(leadImageItem, IMAGE_PATH));

        String title = getProperty(leadImageItem, CORE_TITLE);
        String url = getProperty(leadImageItem, CORE_URL);
        String descrUrl = getProperty(leadImageItem, TALENT_URL);
        String description1 = getProperty(leadImageItem, TALENT_NAME);
        String description2 = getProperty(leadImageItem, RECIPE_COPYRIGHT);
        boolean notBlankDescrUrl = StringUtils.isNotBlank(descrUrl);

        if (StringUtils.isBlank(url)) {
            url = HtmlUtil.getHrefFromLink(title);
            if (StringUtils.isBlank(url)) {
                if (notBlankDescrUrl) {
                    url = descrUrl;
                } else {
                    url = HtmlUtil.getHrefFromLink(description2);
                }
            }
        }
        item.setUrl(url);

        if (notBlankDescrUrl) {
            item.setDescription(StringUtil.cleanToPlainText(description1));
            item.setContainRecipeCourtesy(true);
        } else {
            item.setDescription(StringUtil.cleanToPlainText(description2));
        }

        item.setTitle(StringUtil.cleanToPlainText(title));

        if (StringUtils.isEmpty(title) && StringUtils.isEmpty(item.getDescription()) && !CarouselMobileHelper.isValidDamImagePath(resource.getResourceResolver(), item.getImageDamPath())) {
            item = null;//disabled
        }

        return item;
    }

}
