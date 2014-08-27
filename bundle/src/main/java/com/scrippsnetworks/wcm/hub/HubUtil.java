package com.scrippsnetworks.wcm.hub;

import com.scrippsnetworks.wcm.hub.count.HubCountFactory;
import com.scrippsnetworks.wcm.page.SniPage;

/** Static utilities for working with Hubs.
 * @author Jason Clark
 */
public class HubUtil {

    /** Static method to return hub-related content count for a given SniPage.
     * This is the count that appears on Hub Buttons and other places.
     * @return Integer hub count
     */
    public static Integer getHubCount(final SniPage sniPage) {
        return new HubCountFactory()
                .withSniPage(sniPage)
                .build()
                .getCount();
    }

    /** Static method for use in EL. */
    public static boolean isHubMaster(final Hub hub, final SniPage sniPage) {
        return hub != null && sniPage != null && hub.isPageHubMaster(sniPage);
    }

    /** Static method for use in EL. */
    public static boolean isHubChild(final Hub hub, final SniPage sniPage) {
        return hub != null && sniPage != null && hub.isPageHubChild(sniPage);
    }

    /** Static method for use in EL. */
    public static boolean isHubbed(final Hub hub, final SniPage sniPage) {
        return hub != null && sniPage != null && hub.isPageInHub(sniPage);
    }

}
