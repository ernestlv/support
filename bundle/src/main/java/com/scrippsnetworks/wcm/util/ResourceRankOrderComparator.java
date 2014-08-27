package com.scrippsnetworks.wcm.util;


import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

/**
 * Sort Resources based on sni:rankOrder property
 * @author Jason Clark
 *         Date: 9/15/12
 */
public class ResourceRankOrderComparator implements Comparator<Resource> {
    
    private static final String SNI_RANK_ORDER = "sni:rankOrder";

    private static final Logger log = LoggerFactory.getLogger(ResourceRankOrderComparator.class);

    /** {@inheritDoc} */
    @Override
    public int compare(final Resource r1, final Resource r2) {
        ValueMap vm1 = r1.adaptTo(ValueMap.class);
        ValueMap vm2 = r2.adaptTo(ValueMap.class);
        if (!vm1.containsKey(SNI_RANK_ORDER)) {
            if (!vm2.containsKey(SNI_RANK_ORDER)) {
                return 0;
            } else {
                return Integer.MIN_VALUE;
            }
        } else {
            if (!vm2.containsKey(SNI_RANK_ORDER)) {
                return Integer.MAX_VALUE;
            } else {
                try {
                    Integer rank1 = Integer.valueOf(vm1.get(SNI_RANK_ORDER).toString());
                    Integer rank2 = Integer.valueOf(vm2.get(SNI_RANK_ORDER).toString());
                    return rank1.compareTo(rank2);
                } catch (Exception e) {
                    log.error("Exception: ", e);
                }
            }
        }
        log.error("Couldn't compare Resources.");
        return 0;
    }
}
