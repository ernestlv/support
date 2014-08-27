package com.scrippsnetworks.wcm.mobile.hubnavigation;

import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.hub.button.HubButton;
import com.scrippsnetworks.wcm.hub.button.HubButtonContainer;
import com.scrippsnetworks.wcm.mobile.hubnavigation.impl.HubNavElemImpl;
import com.scrippsnetworks.wcm.page.SniPage;

import java.util.ArrayList;
import java.util.List;

public class HubNavigationFactory {
    private SniPage currentPage;
    private Integer size;

    public List<HubNavElem> build(){
        List<HubNavElem> hubNavElems = new ArrayList<HubNavElem>();

        Hub hub = currentPage.getHub();
        if (hub != null && hub.isPageInHub(currentPage)) {
            HubButtonContainer hubButtonContainer = hub.getHubButtonContainer();
            if (hubButtonContainer != null) {
                List<HubButton> hubButtons = hubButtonContainer.getHubButtons();
                for (HubButton button : hubButtons) {
                    if (button == null) {
                        continue;
                    }
                    hubNavElems.add(new HubNavElemImpl(button, currentPage));

                    if (size != null && size <= hubNavElems.size()){
                        break;
                    }
                }
            }
        }

        return hubNavElems;
    }

    public HubNavigationFactory withCurrentPage(SniPage currentPage){
        this.currentPage = currentPage;
        return this;
    }

    public HubNavigationFactory withSize(Integer size){
        this.size = size;
        return this;
    }
}
