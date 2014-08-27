package com.scrippsnetworks.wcm.metadata.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

import com.scrippsnetworks.wcm.metadata.MetadataManager;
import com.scrippsnetworks.wcm.metadata.MetadataProperty;
import com.scrippsnetworks.wcm.metadata.MetadataProvider;

public class BaseMetadataManager extends HashMap<MetadataProperty, String> implements MetadataManager {

    private List<MetadataProvider> providers = null;
    private Map<MetadataProperty, MetadataProvider> map = new HashMap<MetadataProperty, MetadataProvider>();

    public BaseMetadataManager(List<MetadataProvider> providers) {
        this.providers = providers;
        init();
    }

    private void init() {
        for (MetadataProvider provider : providers) {
            List<MetadataProperty> provides = provider.provides();
            for (MetadataProperty prop : provides) {
                map.put(prop, provider);
            }
        }

        for (MetadataProperty prop : MetadataProperty.values()) {
            if (map.containsKey(prop)) {
                MetadataProvider provider = map.get(prop);
                String value = provider.getProperty(prop);
                if (value != null) {
                    put(prop, value);
                }
            }
        }
    }

    public String getMetadataManagerJson() {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<MetadataProperty, String> entry : this.entrySet()) {
        	//FNRHL-1159 Fix.
			try {
				MetadataProperty metadataProperty = entry.getKey();
				String metadataPropertyName = metadataProperty.getMetadataName();
				if (metadataProperty.isUpperCaseProperty()) {
					jsonObject.put(metadataPropertyName, entry.getValue()
							.toUpperCase(Locale.US));
				} else if (metadataProperty.isNoCaseChangeProperty()) {
					jsonObject.put(metadataPropertyName, entry.getValue());
				} else {
					jsonObject.put(metadataPropertyName, entry.getValue()
							.toLowerCase(Locale.US));
				}
			} catch (JSONException e) {
                // throws JSONException if the value is a non-finite number.
            }
        }
        return jsonObject.toString();
    }
}
