package com.scrippsnetworks.wcm.hub;

import com.scrippsnetworks.wcm.hub.button.HubButton;
import com.scrippsnetworks.wcm.page.SniPage;

import java.util.Comparator;

/**
 * @author Scott Johnson
 *         Date: 7/23/13
 */
public class HubPageKeyComparator implements Comparator<SniPage> {
    @Override
    public int compare(SniPage page1, SniPage page2) {
        HubPageTypeKeys key1 = page1 == null ? null : HubPageTypeKeys.getKeyForSniPage(page1);
        HubPageTypeKeys key2 = page2 == null ? null : HubPageTypeKeys.getKeyForSniPage(page2);
        if (key1 == null && key2 == null) {
            return 0;
        } else if (key1 != null && key2 == null) {
            return Integer.MIN_VALUE;
        } else if (key2 != null && key1 == null) {
            return Integer.MAX_VALUE;
        } else if (key1 == HubPageTypeKeys.MAIN) {
            return Integer.MIN_VALUE;
        } else if (key2 == HubPageTypeKeys.MAIN) {
            return Integer.MAX_VALUE;
        } else {
            return key1.compareTo(key2);
        }
    }
}
