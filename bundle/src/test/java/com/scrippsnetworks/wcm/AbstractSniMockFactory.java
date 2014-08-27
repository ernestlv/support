package com.scrippsnetworks.wcm;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractSniMockFactory<T> implements SniMockFactory {
    
    protected String path;
    protected Map<String, Object> properties = new HashMap<String, Object>();
    
    /**
     * Sets the path of the Page which will be exposed via the returned MockPage.
     * @param path
     * @return
     */
    public T withPath(String path) {
        this.path = path;
        return (T) this;
    }
    
    /**
     * Sets the properties of the Page and Resources which will be exposed via the returned MockPage.
     * @param properties
     * @return
     */
    public T withProperties(Map<String, Object> properties) {
        this.properties = properties;
        return (T) this;
    }

    /**
     * Convenience method for setting a single property of the Page and Resources which will be exposed 
     * via the returned MockPage.
     * 
     * @param key
     * @param value
     * @return
     */
    public T withProperty(String key, Object value) {
        properties.put(key, value);
        return (T) this;
    }

}
