package com.scrippsnetworks.wcm.freeform;

import com.scrippsnetworks.wcm.freeform.impl.FreeFormImpl;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * @author Venkata Naga Sudheer Donaboina
 * Date: 9/19/2013 
 */
public class FreeFormFactory {

    private SniPage page;

    /**
     * Can return null
     * @return
     */
    public FreeForm build() {
        if (page != null) {
            return new FreeFormImpl(page);
        }
        return null;
    }

    /**
     * Add SniPage to FreeFormText Builder
     * @param page SniPage in hand
     * @return this FreeFormTextFactory
     */
    public FreeFormFactory withSniPage(SniPage page) {
        this.page = page;
        return this;
    }

}
