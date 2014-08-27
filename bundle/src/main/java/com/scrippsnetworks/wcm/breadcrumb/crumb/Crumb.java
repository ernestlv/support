package com.scrippsnetworks.wcm.breadcrumb.crumb;

import com.scrippsnetworks.wcm.breadcrumb.crumb.CrumbTypes;
import com.scrippsnetworks.wcm.page.SniPage;

public interface Crumb {
    public CrumbTypes getType();
    public SniPage getSniPage();
    public String getTitle();
    public String getUrl();
}
