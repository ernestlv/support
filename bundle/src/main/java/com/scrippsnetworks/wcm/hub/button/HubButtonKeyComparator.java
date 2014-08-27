package com.scrippsnetworks.wcm.hub.button;

import com.scrippsnetworks.wcm.hub.HubPageTypeKeys;

import java.util.Comparator;

/**
 * @author Jason Clark
 *         Date: 5/13/13
 */
public class HubButtonKeyComparator implements Comparator<HubButton> {
    @Override
    public int compare(HubButton button1, HubButton button2) {
        HubPageTypeKeys key1 = button1==null?null:button1.getKey();
        HubPageTypeKeys key2 = button2==null?null:button2.getKey();
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
