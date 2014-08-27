package com.scrippsnetworks.wcm.snitag;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.snitag.impl.SniTagImpl;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Jason Clark
 *         Date: 6/29/13
 */
public class SniTagFactory {

    private String tagText;
    private SniPage sniPage;

    /** Construct a new Tag with the given input. */
    public SniTag build() {
        if (StringUtils.isNotBlank(tagText)) {
            if (sniPage != null) {
                return new SniTagImpl(tagText, sniPage);
            }
            return new SniTagImpl(tagText);
        }
        return null;
    }

    /** Add tag text to this builder. */
    public SniTagFactory withTagText(String tagText) {
        this.tagText = tagText;
        return this;
    }

    /** Add an SniPage to this builder. */
    public SniTagFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }

}
