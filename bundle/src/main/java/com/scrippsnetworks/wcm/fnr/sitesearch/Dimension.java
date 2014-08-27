package com.scrippsnetworks.wcm.fnr.sitesearch;

/**
 * Represents a selectable filter.
 */
public class Dimension implements Comparable<Dimension> {
    private final String name;
    private final Integer count;
    private final String id;
    private final String url;
    private final String facetName;

    public Dimension(String name, String facetName, String id, Integer count, String url) {
        this.name = name;
        this.count = count;
        this.id = id;
        this.url = url;
        this.facetName = facetName;
    }

    @Override
    public int compareTo(Dimension d2) {

        if (d2 == null) {
            return 1;
        }

        int cmp = 0;
        // reverse sort by count
        
        Integer d1Count = this.count != null ? this.count : 0;
        Integer d2Count = d2.getCount() != null ? d2.getCount() : 0;

        cmp = d2Count.compareTo(d1Count);
        if (cmp != 0) {
            return cmp;
        }

        if (name != null && d2.getName() != null) {
            cmp = name.toLowerCase().compareTo(d2.getName().toLowerCase());
            if (cmp != 0) {
                return cmp;
            }
        }

        if (id != null && d2.getId() != null) {
            cmp = id.compareTo(d2.getId());
            if (cmp != 0) {
                return cmp;
            }
        }

        return 1;
    }

    public boolean equals(Dimension d2) {
        if (this.id == null || d2 == null || d2.getId() == null) {
            return false;
        } else {
            return this.id.equals(d2.getId());
        }
    }

    public String getName() {
        return name;
    }

    public Integer getCount() {
        return count;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getFacetName() {
        return facetName;
    }
}
