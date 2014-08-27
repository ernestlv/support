package com.scrippsnetworks.wcm.mobile.secondarygrid.impl;

import com.scrippsnetworks.wcm.mobile.secondarygrid.SecondaryGrid;
import com.scrippsnetworks.wcm.mobile.secondarygrid.SecondaryGridTab;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dzmitry_Drepin on 1/30/14.
 */
public class SecondaryGridImpl implements SecondaryGrid {
    private String titleHead;
    private String moreLink;
    private String dataUrl="";
    private String urlTemplate="";
    private boolean showTabs=true;
    private List<SecondaryGridTab> items=new ArrayList<SecondaryGridTab>(7);


    public String getTitleHead() {
        return titleHead;
    }

    public void setTitleHead(String titleHead) {
        this.titleHead = titleHead;
    }

    public String getMoreLink() {
        return moreLink;
    }

    public void setMoreLink(String moreLink) {
        this.moreLink = moreLink;
    }

    public List<SecondaryGridTab> getItems() {
        return items;
    }

    public void setItems(List<SecondaryGridTab> items) {
        this.items = items;
    }

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    public String getUrlTemplate() {
        return urlTemplate;
    }

    public void setUrlTemplate(String urlTemplate) {
        this.urlTemplate = urlTemplate;
    }


    public boolean isShowTabs() {
        return showTabs;
    }

    public void setShowTabs(boolean showTabs) {
        this.showTabs = showTabs;
    }
}
