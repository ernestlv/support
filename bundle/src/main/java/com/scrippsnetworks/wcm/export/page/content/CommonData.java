package com.scrippsnetworks.wcm.export.page.content;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.asset.SponsorshipUtil;
import com.scrippsnetworks.wcm.util.PagePropertyNames;
import org.apache.commons.lang.StringUtils;

/**
 * Base class for gathering page content into an exportable object. Contains content that is common to all
 * page types in the Cooking Channel CQ5 project.
 * @author Jason Clark
 *         Date: 12/3/12
 */
public class CommonData extends MetaData {
    /**
     * Page Properties that can be found on any page.
     * @param page The page you want to export
     */
    public CommonData(final Page page) {
        super(page);
        String sponsorship = SponsorshipUtil.getSponsorshipPath(page, hub);
        if (StringUtils.isNotBlank(sponsorship)) {
            if(properties.containsKey(PagePropertyNames.SNI_SPONSORSHIP.propertyName())){
                properties.remove(PagePropertyNames.SNI_SPONSORSHIP.propertyName());
            }
            properties.put(PagePropertyNames.SNI_SPONSORSHIP.propertyName(), sponsorship);
        }
    }
}
