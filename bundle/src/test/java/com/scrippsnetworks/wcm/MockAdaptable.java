package com.scrippsnetworks.wcm;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.ValueMap;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>MockAdaptable</code> class is an (abstract) default
 * implementation of the <code>Adaptable</code> interface. It uses a fluent API
 * to add items to a simple backing map, which holds the objects which should be
 * returned via the adaptTo calls.
 */
public abstract class MockAdaptable {

    private static final Logger log = LoggerFactory.getLogger(MockAdaptable.class);
    
    @Mock protected ValueMap properties;
    
    @SuppressWarnings("rawtypes")
    protected Map<Class, Object> mockReturns = new HashMap<Class, Object>();
    
    public MockAdaptable() {
        initMocks(this);
    }
 
    public <AdapterType> MockAdaptable adaptsTo(Class<AdapterType> type, Object value) {
        mockReturns.put(type, value);
        return this;
    }

    protected void setupProperties(Map<String, Object> props) {
        for (Map.Entry<String, Object> entry : props.entrySet()) {
            //TODO: Add more converters, especially for primitives 
            when(this.properties.get(entry.getKey())).thenReturn(entry.getValue());
            when(this.properties.containsKey(entry.getKey())).thenReturn(true);
            if (entry.getValue() != null) {
                when(this.properties.get(entry.getKey(), String.class)).thenReturn(entry.getValue().toString());
            } else {
                when(this.properties.get(entry.getKey(), String.class)).thenReturn(null);
            }
        }
        mockReturns.put(ValueMap.class, properties);
        when(getBackingObject().adaptTo(ValueMap.class)).thenReturn(properties);
    }
    
    protected abstract Adaptable getBackingObject();
    
    protected void setupMockReturnValues() {
        Adaptable adaptable = getBackingObject();
        for (Entry<Class, Object> entry : mockReturns.entrySet()) {
            when(adaptable.adaptTo(entry.getKey())).thenReturn(entry.getValue());
        }
    }
}
