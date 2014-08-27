package com.scrippsnetworks.wcm.hub.button;

import com.scrippsnetworks.wcm.hub.HubPageTypeKeys;
import com.scrippsnetworks.wcm.hub.button.impl.HubButtonImpl;
import com.scrippsnetworks.wcm.page.SniPage;
import org.apache.commons.lang.StringUtils;

/**
 * @author Jason Clark
 *         Date: 5/9/13
 */
public class HubButtonFactory {

    private HubPageTypeKeys key;
    private String href;
    private String buttonLabel;
    private Integer count;

    private SniPage sniPage;

    /**
     * Build a new HubButton.
     * @return HubButton
     */
    public HubButton build() {
        if (sniPage != null
                && key != null
                && StringUtils.isNotBlank(buttonLabel)
                && count != null) {
            //if all fields are present, build a button deterministically
            return new HubButtonImpl(sniPage, key, buttonLabel, count);
        } else if (sniPage != null && StringUtils.isNotBlank(buttonLabel)) {
            //create a button with a page and a custom label
            return new HubButtonImpl(sniPage, buttonLabel);
        } else if (sniPage != null) {
            //create a button with just a page and defaults that result
            return new HubButtonImpl(sniPage);
        } else if (key != null
                && StringUtils.isNotBlank(href)
                && StringUtils.isNotBlank(buttonLabel)
                && count != null) {
            //if all fields are present, build a button deterministically
            return new HubButtonImpl(key, href, buttonLabel, count);
        }
        return null;
    }

    /**
     * Add an SniPage to your HubButton factory.
     * @param sniPage SniPage
     * @return this
     */
    public HubButtonFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }

    /**
     * Add a key to your HubButton.
     * @param key String key from Hub object.
     * @return this
     */
    public HubButtonFactory withKey(HubPageTypeKeys key) {
        this.key = key;
        return this;
    }

    /**
     * Add an HREF to your HubButton.
     * @param href String the HREF your HubButton points to.
     * @return this
     */
    public HubButtonFactory withHref(String href) {
        this.href = href;
        return this;
    }

    /**
     * Add a button label to your HubButton.
     * @param buttonLabel String label that shows on your button.
     * @return this
     */
    public HubButtonFactory withButtonLabel(String buttonLabel) {
        this.buttonLabel = buttonLabel;
        return this;
    }

    /**
     * Add a count to your HubButton.
     * @param count Integer count of like items that this button represents.
     * @return this
     */
    public HubButtonFactory withCount(Integer count) {
        this.count = count;
        return this;
    }

}
