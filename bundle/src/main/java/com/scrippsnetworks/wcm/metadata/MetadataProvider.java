package com.scrippsnetworks.wcm.metadata;

import java.util.List;
import com.scrippsnetworks.wcm.metadata.MetadataProperty;

public interface MetadataProvider {
    List<MetadataProperty> provides();
    String getProperty(MetadataProperty prop);
}
