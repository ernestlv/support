package com.scrippsnetworks.wcm.mobile.lead3image;

import java.util.List;

public interface LeadThreeImage {
    List<LeadThreeImageItem> getItems();

    void setItems(List<LeadThreeImageItem> items);

    LeadThreeImageItem getFirstItem();

    void setFirstItem(LeadThreeImageItem firstItem);

}
