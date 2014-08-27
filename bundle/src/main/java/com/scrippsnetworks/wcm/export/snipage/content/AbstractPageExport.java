package com.scrippsnetworks.wcm.export.snipage.content;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Calendar;

import com.scrippsnetworks.wcm.page.SniPage;

import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

/** Support for classes assembling an export property ValueMap.
 *
 * Provides methods for setting values of acceptable types to the final export ValueMap. The setProperty signatures
 * correspond to types the adapter knows how to adapt to the JAXB bind classes for XML serialization.
 */
public abstract class AbstractPageExport implements PageExport {

    private final Map<String, Object> properties;
    protected final SniPage sniPage;

    protected AbstractPageExport(SniPage sniPage) {
        this.sniPage = sniPage;
        this.properties = new HashMap<String, Object>();
    }

    /** Set a property whose value is a Calendar.
     *
     * @param name name of property in ValueMap
     * @param prop property value
     */
    protected void setProperty(String name, Calendar prop) {
        if (prop != null) {
            properties.put(name, prop);
        }
    }

    /** Set a property whose value is a String.
     *
     * @param name name of property in ValueMap
     * @param prop property value
     */
    protected void setProperty(String name, String prop) {
        if (prop != null && !prop.isEmpty()) {
            properties.put(name, prop);
        }
    }

    /** Set a property whose value is an array of String values.
     *
     * @param name name of property in ValueMap
     * @param prop property value
     */
    protected void setProperty(String name, String[] prop) {
        if (prop != null && prop.length > 0) {
            properties.put(name, prop);
        }
    }
    
    /** Set a property whose value is a list of values.
    *
    * @param name name of property in ValueMap
    * @param prop property value
    */
    protected void setProperty(String name, List prop) {
        if (prop != null && prop.size() > 0) {
            properties.put(name, prop);
        }
    }
    
    /** Set a property whose value is an Integer.
     *
     * @param name name of property in ValueMap
     * @param prop property value
     */
    protected void setProperty(String name, Integer prop) {
        if (prop != null) {
            properties.put(name, prop);
        }
    }

    /** Set a property whose value is an array of Integers.
     *
     * @param name name of property in ValueMap
     * @param prop property value
     */
    protected void setProperty(String name, Integer[] prop) {
        if (prop != null && prop.length > 0) {
            properties.put(name, prop);
        }
    }

    /** Set a property whose value is an Integer.
     *
     * @param name name of property in ValueMap
     * @param prop property value
     */
    protected void setProperty(String name, Boolean prop) {
        if (prop != null) {
            properties.put(name, prop);
        }
    }

    /** Unset a property.
     *
     * @param name name of property in ValueMap
     */
    protected void unsetProperty(String name) {
        if (name != null) {
            properties.remove(name);
        }
    }
    
    /* considering these
    protected void setProperty(String name, SniPage prop) {
        if (prop != null) {
            properties.put(name, prop);
        }
    }

    protected void setProperty(String name, SniPage[] prop) {
        if (prop != null && prop.length > 0) {
            properties.put(name, prop);
        }
    }
    */

    /**
     * @{inheritDoc}
     */
    public ValueMap getValueMap() {
        return new ValueMapDecorator(Collections.<String, Object>unmodifiableMap(properties));
    }
}
