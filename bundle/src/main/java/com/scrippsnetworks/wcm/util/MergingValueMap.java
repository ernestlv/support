package com.scrippsnetworks.wcm.util;

import java.util.Set;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.api.resource.ValueMap;

/** Merges the properties of two value maps into one.
 *
 * @author Scott Everett Johnson
 */
public class MergingValueMap extends ValueMapDecorator {
    private ValueMap primaryProps;
    private ValueMap secondaryProps;

    /** Constructs a new MergingValueMap.
     *
     * If either of the supplied arguments is null, an empty map is substituted.
     *
     * @param primaryProps this value map is consulted first when a property is requested
     * @param secondaryProps this value map is consulted after primaryProps when a property is requested
     */
    public MergingValueMap(ValueMap primaryProps, ValueMap secondaryProps) {
        super(primaryProps != null ? primaryProps : ValueMap.EMPTY);
        this.primaryProps = primaryProps != null ? primaryProps : ValueMap.EMPTY;
        this.secondaryProps = secondaryProps != null ? secondaryProps : ValueMap.EMPTY;
    }

    @Override
    public <T> T get(String name, Class<T> type) {
        T cVal = primaryProps.get(name, type);
        if (cVal != null) {
            return cVal;
        }

        return secondaryProps.get(name, type);
    }

    @Override
    public <T> T get(String name, T dflt) {
        Object cVal = primaryProps.get(name, dflt.getClass());
        if (cVal != null) {
            return (T)cVal;
        }

        Object aVal = secondaryProps.get(name, dflt.getClass());
        if (aVal != null) {
            return (T)aVal;
        }
        return dflt;
    }

    @Override
    public Object get(Object key) {
       Object cVal = null;
       if (primaryProps.containsKey(key)) {
           cVal = primaryProps.get(key);
           if (cVal != null) {
               return cVal;
           }
       }
       if (secondaryProps.containsKey(key)) {
           return secondaryProps.get(key);
       }
       return null;
    }

    @Override
    public boolean containsKey(Object key) {
        return (primaryProps.containsKey(key) || secondaryProps.containsKey(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return (primaryProps.containsValue(value) || secondaryProps.containsValue(value));
    }

    @Override
    public int size() {
        Set<String> primaryKeys = primaryProps.keySet();
        int uniqSecondaryProps = 0;
        for (String secondaryKey : secondaryProps.keySet()) {
            if (primaryKeys.contains(secondaryKey)) {
                continue;
            }
            uniqSecondaryProps++;
        }
        return primaryKeys.size() + uniqSecondaryProps;
    }
}
