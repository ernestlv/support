package com.scrippsnetworks.wcm.mobile.secondarygrid.impl;

import com.scrippsnetworks.wcm.mobile.secondarygrid.SecondaryGridItem;
import com.scrippsnetworks.wcm.mobile.secondarygrid.SecondaryGridTab;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dzmitry_Drepin on 1/29/14.
 */
public class SecondaryGridTabImpl implements SecondaryGridTab{
    private List<SecondaryGridItem> items = new ArrayList<SecondaryGridItem>(3);
    private String titleTab;
    private int size;
    private boolean showButton;
    private int number;


    public List<SecondaryGridItem> getItems() {
        return items;
    }

    public void setItems(List<SecondaryGridItem> items) {
        this.items = items;
    }

    public String getTitleTab() {
        return titleTab;
    }

    public void setTitleTab(String titleTab) {
        this.titleTab = titleTab;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setShowButton(boolean showButton) {
        this.showButton = showButton;
    }

    public boolean isShowButton() {
        return showButton;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
