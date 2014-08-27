package com.scrippsnetworks.wcm.export.snipage.content;

import org.apache.sling.api.resource.ValueMap;

/** Provides the properties for a page export.
 *
 * Properties are exported in a map of values keyed by export property name.
 */
public interface PageExport {

    /** Returns the exported properties for a page.
     *
     * @return ValueMap the page properties keyed by property name
     */
    public ValueMap getValueMap();
}
