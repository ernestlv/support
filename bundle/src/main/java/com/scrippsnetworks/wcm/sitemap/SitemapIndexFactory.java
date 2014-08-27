package com.scrippsnetworks.wcm.sitemap;

import java.util.List;
import org.apache.sling.api.resource.ResourceResolver;
import com.day.cq.replication.Replicator;

import com.scrippsnetworks.wcm.sitemap.impl.SitemapIndexImpl;

public class SitemapIndexFactory {

    private List<Sitemap> sitemaps;
    private ResourceResolver resolver;
    private String destination;
    private String name;

    public SitemapIndex build() {
        return new SitemapIndexImpl(sitemaps, destination, name, resolver);
    }

    public SitemapIndexFactory withSitemaps(List<Sitemap> sitemaps) {
        this.sitemaps = sitemaps;
        return this;
    }

    public SitemapIndexFactory withName(String name) {
        this.name = name;
        return this;
    }

    public SitemapIndexFactory withDestinationPath(String destination) {
        this.destination = destination;
        return this;
    }

    public SitemapIndexFactory withResolver(ResourceResolver resolver) {
        this.resolver = resolver;
        return this;
    }

}




