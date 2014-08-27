package com.scrippsnetworks.wcm.recipe.categories;

import com.scrippsnetworks.wcm.snitag.SniTag;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;

/**
 * @author Jason Clark
 *         Date: 12/10/13
 */
public class TagNameAlphabeticalComparator implements Comparator<SniTag> {

    /** Compare tags by their display value to sort alphabetically. */
    public int compare(final SniTag o1, final SniTag o2) {
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 != null && o2 == null) {
            return Integer.MIN_VALUE;
        } else if (o1 == null) {
            return Integer.MAX_VALUE;
        } else {
            String val1 = o1.getDisplayValue();
            String val2 = o2.getDisplayValue();
            if (StringUtils.isBlank(val1) && StringUtils.isBlank(val2)) {
                return 0;
            } else if (StringUtils.isNotBlank(val1) && StringUtils.isBlank(val2)) {
                return Integer.MIN_VALUE;
            } else if (StringUtils.isBlank(val1)) {
                return Integer.MAX_VALUE;
            } else {
                return val1.compareToIgnoreCase(val2);
            }
        }
    }

}
