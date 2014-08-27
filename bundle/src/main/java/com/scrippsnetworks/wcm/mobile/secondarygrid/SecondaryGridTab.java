package com.scrippsnetworks.wcm.mobile.secondarygrid;



import java.util.List;

public interface SecondaryGridTab {
    List<SecondaryGridItem> getItems();

    void setItems(List<SecondaryGridItem> items);

    String getTitleTab();

    void setTitleTab(String titleTab);

    int getSize();

    void setSize(int size);

    void setShowButton(boolean showButton);

    boolean isShowButton();

    int getNumber();

    void setNumber(int number);
}
