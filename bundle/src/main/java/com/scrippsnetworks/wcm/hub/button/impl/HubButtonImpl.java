package com.scrippsnetworks.wcm.hub.button.impl;

import com.scrippsnetworks.wcm.hub.HubUtil;
import com.scrippsnetworks.wcm.hub.button.HubButton;
import com.scrippsnetworks.wcm.hub.HubPageTypeKeys;
import com.scrippsnetworks.wcm.page.SniPage;
import org.apache.commons.lang3.StringUtils;

public class HubButtonImpl implements HubButton {

    private HubPageTypeKeys key;
    private String href;
    private String buttonLabel;
    private Integer count;
    private String pagePath;

    private SniPage sniPage;

    /** Default constructor */
    public HubButtonImpl() {}

    /**
     * Construct a new HubButtonImpl with a key, href, buttonLabel and count.  All fields
     * a button may contain are provided by the Factory method.
     * @param key HubButtonKey key Enum value
     * @param href String href
     * @param buttonLabel String label
     * @param count Integer count
     */
    public HubButtonImpl(final HubPageTypeKeys key,
                         final String href,
                         final String buttonLabel,
                         final Integer count) {
        this.key = key;
        this.href = href;
        this.buttonLabel = buttonLabel;
        this.count = count;
    }

    /**
     * Construct a new HubButtonImpl with a sniPage, key, buttonLabel and count.  All fields
     * a button may contain are provided by the Factory method.
     * @param key HubButtonKey key Enum value
     * @param sniPage SniPage for page in hub
     * @param buttonLabel String label
     * @param count Integer count
     */
    public HubButtonImpl(final SniPage sniPage,
                         final HubPageTypeKeys key,
                         final String buttonLabel,
                         final Integer count) {
        this.key = key;
        this.sniPage = sniPage;
        this.buttonLabel = buttonLabel;
        this.count = count;
        this.href = sniPage.getUrl();
    }

    /**
     * Construct a new HubButtonImpl when given an SniPage and a button label.
     * @param sniPage SniPage for page in hub
     * @param buttonLabel String custom label for hub button
     */
    public HubButtonImpl(final SniPage sniPage, final String buttonLabel) {
        if (sniPage != null) {
            this.sniPage = sniPage;
            href = sniPage.getUrl();
            count = HubUtil.getHubCount(sniPage);
            key = HubPageTypeKeys.getKeyForSniPage(sniPage);
            if (StringUtils.isNotBlank(buttonLabel)) {
                this.buttonLabel = buttonLabel;
            } else {
                if (key != null) {
                    boolean isPlural = (count != null && count > 1);
                    this.buttonLabel = isPlural ? key.pluralKeyName() : key.keyName();
                }
            }
        }
    }

    /**
     * Construct a HubButton with the SniPage that the button will link to. This
     * method is not recommended for use in building the "Main" button; instead,
     * use the more deterministic constructor which takes all arguments at once.
     * @param sniPage SniPage
     */
    public HubButtonImpl(final SniPage sniPage) {
        if (sniPage != null) {
            this.sniPage = sniPage;
            href = sniPage.getUrl();
            count = HubUtil.getHubCount(sniPage);
            key = HubPageTypeKeys.getKeyForSniPage(sniPage);
            String navTitle = sniPage.getNavigationTitle();
            if (StringUtils.isNotBlank(navTitle)) {
                buttonLabel = navTitle;
            } else if (key != null) {
                boolean isPlural = (count != null && count > 1);
                buttonLabel = isPlural ? key.pluralKeyName() : key.keyName();
            }
        }
    }

    @Override
    public HubPageTypeKeys getKey() {
        return key;
    }

    @Override
    public String getHref() {
        return href;
    }

    @Override
    public String getButtonLabel() {
        return buttonLabel;
    }

    @Override
    public Integer getCount() {
        return count;
    }

    @Override
    public String getPagePath() {
        if (pagePath == null) {
            pagePath = sniPage != null ? sniPage.getPath() : null;
        }
        return pagePath;
    }
}
