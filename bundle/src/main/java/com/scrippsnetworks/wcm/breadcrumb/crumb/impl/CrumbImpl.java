package com.scrippsnetworks.wcm.breadcrumb.crumb.impl;

import com.scrippsnetworks.wcm.breadcrumb.crumb.Crumb;
import com.scrippsnetworks.wcm.breadcrumb.crumb.CrumbTypes;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * @author Jonathan Bell
 *         Date: 10/2/2013
 * 
 */
public class CrumbImpl implements Crumb {

    private CrumbTypes type;
    private SniPage sniPage;
    private String title;
    private String url;
    
    public CrumbImpl(final CrumbTypes type,
                     final SniPage sniPage,
                     final String title,
                     final String url) {
        this.type = type;
        this.sniPage = sniPage;
        this.title = title;
        this.url = url;
    }

    @Override
    public CrumbTypes getType() {
        return type;
    }

    @Override
    public SniPage getSniPage() {
        return sniPage;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getUrl() {
        return url;
    }

}
