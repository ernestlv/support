package com.scrippsnetworks.wcm.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Jason Clark
 *         Date: 5/22/13
 */
public class CompoundProperty {

    Logger log = LoggerFactory.getLogger(CompoundProperty.class);

    /** This is the property from the JCR Node, untouched */
    private String property;

    /** First part of serialized property */
    private String key;

    /** Second part of serialized property */
    private String value;

    /**
     * Construct a CompoundProperty object given a String property that is
     * serialized in the format:  key|value.
     * You can then retrieve the key/value with named getters.
     * @param rawProperty String in format key|value
     */
    public CompoundProperty(final String rawProperty) {
        try {
            if (StringUtils.isNotBlank(rawProperty)) {
                property = rawProperty;
                String[] parts = rawProperty.split(Pattern.quote("|"));
                if (parts != null && parts.length > 0) {
                    if (parts.length > 1) {
                        key = parts[0];
                        value = parts[1];
                    } else {
                        value = parts[0];
                    }
                }
            }
        } catch (Exception e) {
            log.error("Caught Exception in CompoundProperty: " + e.getMessage());
        }
    }

    /**
     * Get the key part of the serialized property.
     * @return String
     */
    public String getKey() {
        return key;
    }

    /**
     * Get the value part of the serialized property.
     * @return String
     */
    public String getValue() {
        return value;
    }

    /**
     * Get the original property that was passed into the constructor.
     * @return String
     */
    public String getOriginalProperty() {
        return property;
    }

    /**
     * Get a list of CompoundProperties from a string array.
     * @param values An array of strings from which to create CompoundProperty objects
     * @return List<CompoundProperty>, or null if the passed string array is null
     */
    public static List<CompoundProperty> fromArray(String[] values) {
        if (values == null) {
            return null;
        }

        List retVal = new ArrayList<CompoundProperty>();

        for (String value : values) {
            retVal.add(new CompoundProperty(value));
        }

        return retVal;
    }
}
