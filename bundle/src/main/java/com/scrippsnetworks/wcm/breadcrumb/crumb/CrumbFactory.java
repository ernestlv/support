package com.scrippsnetworks.wcm.breadcrumb.crumb;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.breadcrumb.crumb.Crumb;
import com.scrippsnetworks.wcm.breadcrumb.crumb.CrumbTypes;
import com.scrippsnetworks.wcm.breadcrumb.crumb.impl.CrumbImpl;

/**
 * @author Jonathan Bell
 *         Date: 10/2/2013
 */
public class CrumbFactory {

    private CrumbTypes type;
    private SniPage sniPage;
    private String title;
    private String url;

    public Crumb build() {
        if (type != null) {
            if (sniPage != null && title == null && url == null) {
                title = sniPage.getTitle();
                url = sniPage.getFriendlyUrl();
            }
            return new CrumbImpl(type, sniPage, title, url);
        }
    
        return null;
    }

    public CrumbFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }

    public CrumbFactory withType(CrumbTypes type) {
        this.type = type;
        return this;
    }

    public CrumbFactory withTitle(String title) {
        this.title = title;
        return this;
    }

    public CrumbFactory withUrl(String url) {
        this.url = url;
        return this;
    }
}
