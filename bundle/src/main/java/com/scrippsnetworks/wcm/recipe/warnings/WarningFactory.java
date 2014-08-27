package com.scrippsnetworks.wcm.recipe.warnings;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.warnings.impl.WarningImpl;

/**
 * @author Jason Clark
 *         Date: 7/16/13
 */
public class WarningFactory {

    /** SniPage for Recipe used to build these warnings. */
    private SniPage sniPage;

    /** Construct a new Warning from a Recipe Snipage. */
    public Warning build() {
        if (sniPage != null) {
            return new WarningImpl(sniPage);
        }
        return null;
    }

    /** Add an SniPage to this builder. */
    public WarningFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }

}
