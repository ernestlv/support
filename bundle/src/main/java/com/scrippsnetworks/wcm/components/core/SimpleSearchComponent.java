package com.scrippsnetworks.wcm.components.core;

import java.lang.String;
import java.util.HashMap;
import java.util.Map;
import com.scrippsnetworks.wcm.AbstractSearchComponent;

/** Search component bean encapsulating simple search needs.
 * 
 * This simple class passes through all dynamic attributes to the search service.
 */
public class SimpleSearchComponent extends AbstractSearchComponent {

    public Map<String, String> searchParameters = new HashMap<String, String>();

    @Override
    public void doAction() {
        Map<String, Object> dynamicAttributes = getDynamicAttributes();
        if (dynamicAttributes == null) {
            throw new RuntimeException("could not retrieve dynamic attributes");
        }
        for (Map.Entry<String, Object> entry : dynamicAttributes.entrySet()) {
            if (entry.getValue() instanceof String) {
                searchParameters.put(entry.getKey(), (String)entry.getValue());
            } else {
                throw new RuntimeException("dynamic attribute values must be of type String");
            }
        }
    }

    @Override
    public Map<String, String> getSearchParameters() {
        return searchParameters;
    }

}
