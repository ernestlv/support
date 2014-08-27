package com.scrippsnetworks.wcm.util;

import java.util.EnumSet;

/**
 * Enum for property names found on pages
 * @author Jason Clark
 *         Date: 11/16/12
 */
public enum PagePropertyNames {

    JCR_PRIMARY_TYPE ("jcr:primaryType"),
    JCR_TITLE ("jcr:title"),
    JCR_CREATED ("jcr:created"),
    JCR_DESCRIPTION ("jcr:description"),
    SNI_FASTFWD_URL ("sni:fastfwdUrl"),
    SNI_FASTFWD_STATUS ("sni:fastfwdStatus"),
    SNI_FASTFWD_ID ("sni:fastfwdId"),
    SNI_RECIPES ("sni:recipes"),
    SNI_RANK_ORDER ("sni:rankOrder"),
    SNI_ADKEY ("sni:adkey"),
    SNI_SPONSORSHIP ("sni:sponsorship"),
    SNI_TOPIC_TAGS ("sni:topicTags"),
    SNI_SOURCE ("sni:source"),
    SNI_PRIMARY_TAG ("sni:primaryTag"),
    SNI_SECONDARY_TAG ("sni:secondaryTag"),
    SNI_VIDEOS ("sni:videos"),
    SNI_ASSET_LINK ("sni:assetLink"),
    SNI_PACKAGE ("sni:package"),
    SNI_IMAGE ("sni:image"),
    SNI_IMAGES ("sni:images"),
    SNI_SEO_TITLE ("sni:seoTitle"),
    SNI_SEO_DESCRIPTION ("sni:seoDesc"),
    SNI_CALCULATED_SEO_TITLE ("sni:calculatedSeoTitle"),
    SNI_ASSET_UID ("sni:assetUId"),
    SNI_VALUE ("sni:value"),
    SLING_RESOURCE_TYPE ("sling:resourceType"),
    CQ_LAST_REPLICATION_ACTION ("cq:lastReplicationAction"),
    CQ_LAST_MODIFIED ("cq:lastModified"),
    CQ_LAST_REPLICATED ("cq:lastReplicated"),
    CQ_TAGS ("cq:tags");

    private String propertyName;

    private PagePropertyNames(final String propertyName) {
        this.propertyName = propertyName;
    }

    public String propertyName() {return this.propertyName;}

    /**
     * These properties can be commonly found on any page type.
     */
    public final static EnumSet<PagePropertyNames> COMMON_TYPES = EnumSet.of(JCR_TITLE, JCR_CREATED, JCR_DESCRIPTION,
            SNI_ADKEY, SNI_ASSET_LINK, SNI_ASSET_UID, SNI_CALCULATED_SEO_TITLE, SNI_FASTFWD_ID, SNI_FASTFWD_STATUS,
            SNI_FASTFWD_URL, SNI_PRIMARY_TAG, SNI_SECONDARY_TAG, SNI_RANK_ORDER, SNI_SEO_DESCRIPTION, SNI_SEO_TITLE,
            SNI_PACKAGE, SNI_TOPIC_TAGS, SNI_SOURCE, CQ_TAGS, CQ_LAST_REPLICATION_ACTION, CQ_LAST_REPLICATED,
            CQ_LAST_MODIFIED, SLING_RESOURCE_TYPE, SNI_SPONSORSHIP);

    /**
     * These are props that require logic to merge the page property with data from another source,
     * often an asset or a calculated value. Add a property to this EnumSet if you cannot simply
     * use the data from the page property without futzing with it first.
     */
    public final static EnumSet<PagePropertyNames> MERGED_TYPES = EnumSet.of(SNI_SPONSORSHIP, SNI_SEO_TITLE,
            SNI_SEO_DESCRIPTION, SNI_CALCULATED_SEO_TITLE);

    /**
     * Check if this is a merged property type
     * @return boolean if this is a merged property type
     */
    public boolean isMergedType() { return MERGED_TYPES.contains(this); }

}
