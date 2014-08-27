package com.scrippsnetworks.wcm.mobile.hubnavigation.impl;

import com.scrippsnetworks.wcm.hub.HubPageTypeKeys;
import com.scrippsnetworks.wcm.hub.button.HubButton;
import com.scrippsnetworks.wcm.mobile.hubnavigation.HubNavElem;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.util.PageTypes;
import org.apache.commons.lang.StringUtils;

public class HubNavElemImpl implements HubNavElem{
    private String title;
    private boolean isActive;
    private String path = "";
    private String href;

    public HubNavElemImpl(HubButton button, SniPage currentPage){

        path = button.getPagePath();
        href = button.getHref();

        title = StringUtils.capitalize(button.getButtonLabel());

        PageTypes pageType = PageTypes.findPageType(currentPage.getPageType());
        isActive = isButtonCurrentPage(path, currentPage.getPath());
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getHref() {
        return href;
    }

    private boolean isButtonCurrentPage(String buttonPath, String currentPath) {
        return StringUtils.isNotBlank(buttonPath)
                && StringUtils.isNotBlank(currentPath)
                && buttonPath.equals(currentPath);
    }

    private boolean highlightEpisodePage(final PageTypes pageType, final HubButton button) {
        return pageType == PageTypes.EPISODE && button.getKey() == HubPageTypeKeys.EPISODES;
    }
}
