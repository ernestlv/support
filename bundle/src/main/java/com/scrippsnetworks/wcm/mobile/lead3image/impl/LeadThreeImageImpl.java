package com.scrippsnetworks.wcm.mobile.lead3image.impl;

import com.scrippsnetworks.wcm.mobile.lead3image.LeadThreeImage;
import com.scrippsnetworks.wcm.mobile.lead3image.LeadThreeImageItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dzmitry_Drepin on 1/30/14.
 */
public class LeadThreeImageImpl implements LeadThreeImage {
    private List<LeadThreeImageItem> items=new ArrayList<LeadThreeImageItem>(2);
    private LeadThreeImageItem firstItem;

    public List<LeadThreeImageItem> getItems() {
        return items;
    }

    public void setItems(List<LeadThreeImageItem> items) {
        this.items = items;
    }

    public LeadThreeImageItem getFirstItem() {
        return firstItem;
    }

    public void setFirstItem(LeadThreeImageItem firstItem) {
        this.firstItem = firstItem;
    }
}
