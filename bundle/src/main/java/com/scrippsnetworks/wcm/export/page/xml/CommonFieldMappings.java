package com.scrippsnetworks.wcm.export.page.xml;

import java.util.EnumSet;

/**
 * @author Jason Clark
 *         Date: 12/7/12
 */
public enum CommonFieldMappings {
    JCR_TITLE ("CORE_TITLE", "jcr:title"),
    JCR_DESCRIPTION ("CORE_DESCRIPTION", "jcr:description"),
    SNI_FASTFWD_ID ("CORE_FFID", "sni:fastfwdId"),
    SNI_ADKEY ("CORE_ADKEY", "sni:adkey"),
    SNI_SPONSORSHIP ("CORE_SPONSOR_CODE", "sni:sponsorship"),
    SNI_SOURCE ("CORE_SOURCE", "sni:source"),
    SNI_PRIMARY_TAG ("CORE_PRIMARY_TAG", "sni:primaryTag"),
    SNI_SECONDARY_TAG ("CORE_SECONDARY_TAG", "sni:secondaryTag"),
    SNI_PACKAGE ("CORE_PACKAGE_ID", "sni:package"),
    SNI_SEO_TITLE ("CORE_SEO_TITLE", "sni:seoTitle"),
    SNI_SEO_DESCRIPTION ("CORE_SEO_DESCRIPTION", "sni:seoDesc"),
    SNI_ASSET_UID ("CORE_ASSETUID", "sni:assetUId"),
    SLING_RESOURCE_TYPE ("CORE_PAGE_TYPE", "sling:resourceType"),
    CQ_LAST_REPLICATION_ACTION ("CQ_LAST_REPLICATION_ACTION", "cq:lastReplicationAction"),
    CQ_LAST_MODIFIED ("CQ_LAST_MODIFIED", "cq:lastModified"),
    CQ_LAST_REPLICATED ("CORE_RELEASE_DATE", "cq:lastReplicated"),
    CQ_TAGS ("CORE_TAG_CRX_PATH", "cq:tags"),
    SNI_PEOPLE ("CORE_PEOPLE", "sni:people");

    private String xmlProperty, jcrProperty;

    private CommonFieldMappings(final String xmlProperty, final String jcrProperty) {
        this.xmlProperty = xmlProperty;
        this.jcrProperty = jcrProperty;
    }

    public String xmlProperty() { return this.xmlProperty; }
    public String jcrProperty() { return this.jcrProperty; }

    public static final EnumSet<CommonFieldMappings> STRING_TYPES = EnumSet.of(JCR_TITLE, JCR_DESCRIPTION, SNI_FASTFWD_ID,
            SNI_ADKEY, SNI_SPONSORSHIP, SNI_SOURCE, SNI_PRIMARY_TAG, SNI_SECONDARY_TAG, SNI_PACKAGE, SNI_SEO_TITLE,
            SNI_SEO_DESCRIPTION, SNI_ASSET_UID, SLING_RESOURCE_TYPE, CQ_LAST_REPLICATION_ACTION);

    public static final EnumSet<CommonFieldMappings> CALENDAR_TYPES = EnumSet.of(CQ_LAST_MODIFIED, CQ_LAST_REPLICATED);

    public static final EnumSet<CommonFieldMappings> STRING_ARRAY_TYPES = EnumSet.of(CQ_TAGS, SNI_PEOPLE);

    public final boolean isStringType() { return STRING_TYPES.contains(this); }
    public final boolean isCalendarType() { return CALENDAR_TYPES.contains(this); }
    public final boolean isStringArrayType() { return STRING_ARRAY_TYPES.contains(this); }

    /**
     * Return a CommonFieldMappings instance based on the jcr property name given
     * @param propertyName String property name to find
     * @return CommonFieldMappings that match the jcr property name given
     */
    public static CommonFieldMappings getFieldMapping(final String propertyName) {
        for (CommonFieldMappings mapping : CommonFieldMappings.values()) {
            if (mapping.jcrProperty().equals(propertyName)) {
                return mapping;
            }
        }
        return null;
    }
}
