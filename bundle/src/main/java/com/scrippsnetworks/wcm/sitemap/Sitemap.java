package com.scrippsnetworks.wcm.sitemap;

import java.util.List;

public interface Sitemap {

    public boolean generate();
    public List<String> getPaths();
    public List<String> getUrls();
    public String getDate();
}
