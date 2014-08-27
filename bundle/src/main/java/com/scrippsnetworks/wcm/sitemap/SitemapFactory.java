package com.scrippsnetworks.wcm.sitemap;

import java.util.List;
import org.apache.sling.api.resource.ResourceResolver;
import com.day.cq.replication.Replicator;

import com.scrippsnetworks.wcm.sitemap.impl.SitemapImpl;

public class SitemapFactory {

    private List<String> exclusions;
    private List<String> paths;
    private ResourceResolver resolver;
    private Replicator replicator;
    private String destination;
    private String name;
    private String brand;

    public Sitemap build() {
        return new SitemapImpl(paths, exclusions, destination, name, replicator, resolver, brand);
    }

    public SitemapFactory withPaths(List<String> pathsToIndex) {
        this.paths = pathsToIndex;
        return this;
    }

    public SitemapFactory withExcludedPaths(List<String> pathsToExclude) {
        this.exclusions = pathsToExclude;
        return this;
    }

    public SitemapFactory withDestinationPath(String destination) {
        this.destination = destination;
        return this;
    }

    public SitemapFactory withName(String name) {
        this.name = name;
        return this;
    }

    public SitemapFactory withResolver(ResourceResolver resolver) {
        this.resolver = resolver;
        return this;
    }

    public SitemapFactory withReplicator(Replicator replicator) {
        this.replicator = replicator;
        return this;
    }

    public SitemapFactory withBrand(String brand) {
        this.brand = brand;
        return this;
    }
}

