package com.scrippsnetworks.wcm.breadcrumb;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.breadcrumb.Breadcrumb;
import com.scrippsnetworks.wcm.breadcrumb.impl.BreadcrumbImpl;

/**
 * @author Jonathan Bell
 *         Date: 10/1/2013
 */
public class BreadcrumbFactory {

    private SniPage sniPage;
    private Breadcrumb breadcrumb;

    public Breadcrumb build() {
        breadcrumb = null;

        if (sniPage != null) {
            breadcrumb = new BreadcrumbImpl(sniPage);
        }

        return breadcrumb;
    }

    public BreadcrumbFactory withSniPage(SniPage page) {
        this.sniPage = page;
        return this;
    }

}
