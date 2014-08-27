package com.scrippsnetworks.wcm.metadata.impl.provider;

import com.scrippsnetworks.wcm.section.Section;
import java.lang.String;
import java.util.List;
import java.util.Arrays;
import java.lang.IllegalArgumentException;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.metadata.MetadataProvider;
import com.scrippsnetworks.wcm.metadata.MetadataProperty;
import com.scrippsnetworks.wcm.metadata.impl.MetadataUtil;
import static com.scrippsnetworks.wcm.metadata.MetadataProperty.*;

public class BaseLocationMetadataProvider implements MetadataProvider {
    public static final String SECTIONTYPE = "section";
    public static final String MOBILE_SUFFIX = "-mobile";
    public static final String DEFAULT_SITE = "unknown";

    private SniPage page = null;
    private String siteName = null;
    private String sectionName = null;
    private String sectionDisplayName = null;
    private String deliveryChannel = null;

    public BaseLocationMetadataProvider(SniPage page) {
        if (page == null) {
            throw new IllegalArgumentException("page must not be null");
        }

        this.page = page;
        this.siteName = MetadataUtil.getSiteName(page);
        if (this.siteName == null) {
            this.siteName = DEFAULT_SITE;
        }
        this.deliveryChannel = MetadataUtil.getDeliveryChannel(page);
        Section section = page.getSection();
        this.sectionName = section.getSectionName();
        if (sectionName == null) {
            sectionName = siteName;
        }
        this.sectionDisplayName = section.getSectionDisplayName();
        if (sectionDisplayName == null) {
            sectionDisplayName = siteName;
        }
    }

    public List<MetadataProperty> provides() {
        return Arrays.asList(SITE, SECTION, CATEGORYDSPNAME, SCTNDSPNAME, CLASSIFICATION, DELIVERYCHANNEL);
    }

    public String getProperty(MetadataProperty prop) {
        String retVal = null;
        switch (prop) {
            case SITE:
                retVal = siteName;
                break;
            case SECTION:
                retVal = sectionName;
                break;
            case CATEGORYDSPNAME:
            case SCTNDSPNAME:
                retVal = sectionDisplayName;
                break;
            case CLASSIFICATION:
                retVal = sectionName + ", " + siteName;
                break;
            case DELIVERYCHANNEL:
                retVal = deliveryChannel;
                break;
            default:
                throw new IllegalArgumentException("invalid property");
        }
        return retVal;
    }
}
