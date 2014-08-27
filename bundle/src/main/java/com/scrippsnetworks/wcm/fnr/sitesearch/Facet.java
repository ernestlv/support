package com.scrippsnetworks.wcm.fnr.sitesearch;

import java.util.*;

import org.apache.commons.lang3.text.WordUtils;

/**
 * A grouping of related dimensions, e.g. Cuisine, Main Ingredient, etc.
 */
public class Facet implements Comparable<Facet> {

    private final String displayName;
    private final String key;
    private final Set<Dimension> dimensions = new TreeSet<Dimension>();

    public static final int GROUP_SIZE = 10;
    public static final int MAX_GROUPS = 3;

    public Facet(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public int compareTo(Facet f2) {
        return this.displayName.compareTo(f2.getDisplayName());
    }

    public void addDimension(Dimension dimension) {
        dimensions.add(dimension);
    }

    public Set<Dimension> getDimensions() {
        return dimensions;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getKey() {
        return key;
    }

    public List<Set<Dimension>> getGroupedDimensions() {
        return internalGroupedDimensions(GROUP_SIZE, MAX_GROUPS);
    }

    private List<Set<Dimension>> internalGroupedDimensions(int groupSize, int maxGroups) {
        List<Set<Dimension>> retVal = new ArrayList<Set<Dimension>>();
        List<Dimension> dimensions = Arrays.asList(getDimensions().toArray(new Dimension[getDimensions().size()]));
        int nDimensions = dimensions.size();
        if (nDimensions == 0) {
            // do nothing
        } else if (groupSize < 0 || groupSize >= nDimensions) {
            retVal.add(getDimensions());
        } else {
            int runningCount = 0;
            int buckets = (nDimensions / groupSize) + (nDimensions % groupSize > 0 ? 1 : 0);
            for (int i = 0; i < buckets && i < maxGroups; i++) {
                Set<Dimension> bucket = new TreeSet<Dimension>();
                for (int j = 0; j < groupSize && runningCount < nDimensions; j++) {
                    bucket.add(dimensions.get(runningCount++));
                }
                retVal.add(bucket);
            }

        }

        return retVal;
    }
}
