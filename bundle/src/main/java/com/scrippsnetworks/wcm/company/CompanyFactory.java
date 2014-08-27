/**
 * 
 */
package com.scrippsnetworks.wcm.company;

import com.scrippsnetworks.wcm.company.impl.CompanyImpl;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * @author veerinaiduj
 * 
 */
public class CompanyFactory {

    /** SniPage to build the Channel from. */
    private SniPage sniPage;

    /** Build a new Channel. */
    public Company build() {
        if (sniPage != null) {
            return new CompanyImpl(sniPage);
        }
        return null;
    }

    /** Add an SniPage to this builder. */
    public CompanyFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }

}
