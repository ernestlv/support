package com.scrippsnetworks.wcm.recipe.warnings.impl;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.warnings.Warning;
import com.scrippsnetworks.wcm.snitag.SniTag;
import com.scrippsnetworks.wcm.snitag.SniTagFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jason Clark
 *         Date: 7/16/13
 */
public class WarningImpl implements Warning {

    private static final String WARNINGS = "sni:warnings";

    /** Member for list of warning tags. */
    private List<SniTag> warnings;

    public WarningImpl(final SniPage sniPage) {
        if (sniPage != null) {
            ValueMap properties = sniPage.getProperties();
            if (properties.containsKey(WARNINGS)) {
                String[] rawWarnings = properties.get(WARNINGS, String[].class);
                if (rawWarnings != null) {
                    for (String warning : rawWarnings) {
                        if (StringUtils.isNotBlank(warning)) {
                            SniTag warningTag = new SniTagFactory()
                                    .withTagText(warning)
                                    .withSniPage(sniPage)
                                    .build();
                            if (warningTag != null) {
                                addWarning(warningTag);
                            }
                        }
                    }
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<SniTag> getWarnings() {
        return warnings;
    }

    /** Convenience method for adding a tag to warnings list. */
    private void addWarning(final SniTag tag) {
        if (warnings == null) {
            warnings = new ArrayList<SniTag>();
        }
        if (tag != null) {
            warnings.add(tag);
        }
    }
}
