package com.scrippsnetworks.wcm.export.page.content;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.asset.SponsorshipUtil;
import com.scrippsnetworks.wcm.asset.hub.Hub;
import com.scrippsnetworks.wcm.asset.hub.HubManager;
import com.scrippsnetworks.wcm.util.PagePropertyNames;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.sling.api.resource.ValueMap;
import com.day.cq.wcm.api.Page;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jason Clark
 *         Date: 2/20/13
 */
public class MetaData {
    Map<String, Object> properties;
    Hub hub;

    public MetaData(final Page page) {
        Validate.notNull(page);

        hub = HubManager.findHub(page);

        properties = new HashMap<String, Object>();
        ValueMap pageProperties = page.getProperties();
        for ( PagePropertyNames property : PagePropertyNames.COMMON_TYPES ) {
            if (!property.isMergedType() && pageProperties.containsKey(property.propertyName())) {
                properties.put(property.propertyName(), pageProperties.get(property.propertyName()));
            }
        }

        String sponsorship = SponsorshipUtil.getSponsorshipValue(page, hub);
        if (StringUtils.isNotBlank(sponsorship)) {
            properties.put(PagePropertyNames.SNI_SPONSORSHIP.propertyName(), sponsorship);
        }

        SniPage sniPage = PageFactory.getSniPage(page);
        String seoTitle = sniPage.getSeoTitle();
        if (StringUtils.isNotBlank(seoTitle)) {
            properties.put(PagePropertyNames.SNI_SEO_TITLE.propertyName(), seoTitle);
        }

        String seoDescription = sniPage.getSeoDescription();
        if (StringUtils.isNotBlank(seoDescription)) {
            properties.put(PagePropertyNames.SNI_SEO_DESCRIPTION.propertyName(), seoDescription);
        }
    }

    /**
     * Map containing the content found in all page XML exports (String, Object)
     * @return content map
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Hub object if page is in a hub
     * @return Hub from com.scrippsnetworks.wcm.asset.hub.Hub
     */
    public Hub getHub() {
        return hub;
    }
}
