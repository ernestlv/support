package com.scrippsnetworks.wcm.parsys;

import org.apache.commons.lang.StringUtils;

/**
 * This contains sling:resourceTypes of components that have page-break functionality built in.
 * Used by Paginator object to attribute page-break behavior without a separate page-break component.
 * @author Jason Clark
 *         Date: 3/14/13
 * @deprecated see {@link com.scrippsnetworks.wcm.paginator.ParagraphPaginator}
 */
@Deprecated
public enum PageBreakTypes {
    PHOTO_GALLERY ("sni-wcm/components/pagetypes/photo-gallery/components/photo-gallery");

    private String resourceType;

    private PageBreakTypes(final String resourceType) {
        this.resourceType = resourceType;
    }

    public String resourceType() { return this.resourceType; }

    /**
     *
     * @param resourceType
     * @return
     */
    public static boolean isPageBreakResourceType(final String resourceType) {
        if (StringUtils.isEmpty(resourceType)) {
            return false;
        }
        for (PageBreakTypes type : PageBreakTypes.values()) {
            if (type.resourceType().equals(resourceType)) {
                return true;
            }
        }
        return false;
    }
}
