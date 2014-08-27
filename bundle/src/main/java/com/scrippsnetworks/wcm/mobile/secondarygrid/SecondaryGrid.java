package com.scrippsnetworks.wcm.mobile.secondarygrid;


import java.util.List;

public interface SecondaryGrid {

    String getTitleHead();

    void setTitleHead(String titleHead);

    String getMoreLink();

    void setMoreLink(String moreLink);

    List<SecondaryGridTab> getItems();

    void setItems(List<SecondaryGridTab> items);

    String getDataUrl();

    void setDataUrl(String dataUrl);

    String getUrlTemplate();

    void setUrlTemplate(String urlTemplate);

    boolean isShowTabs();

    void setShowTabs(boolean showTabs);
}
