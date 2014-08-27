package com.scrippsnetworks.wcm.hub.button.impl;

import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.hub.HubPageTypeKeys;
import com.scrippsnetworks.wcm.hub.button.*;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.snipackage.SniPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.sling.api.resource.Resource;

/**
 * @author Jason Clark
 *         Date: 5/13/13
 */
public class HubButtonContainerImpl implements HubButtonContainer {

    private static final Logger log = LoggerFactory.getLogger(HubButtonContainerImpl.class);
    
    private List<HubButton> hubButtons;

    /** Default constructor. */
    public HubButtonContainerImpl() {}

    /**
     * Construct a set of Hub Buttons for a hub.
     * @param sniPage SniPage from which the request originated.
     */
    public HubButtonContainerImpl(final SniPage sniPage) {
        if (sniPage != null) {
            Hub hub = sniPage.getHub();
            if (hub != null) {
                SniPage mainPage = hub.getHubMaster();
                hubButtons = new ArrayList<HubButton>();
                if (mainPage != null) {
                    if (hubButtons != null) {
                        HubButton mainButton;
                        mainButton = new HubButtonFactory()
                                .withKey(HubPageTypeKeys.MAIN)
                                .withButtonLabel(HubPageTypeKeys.MAIN.keyName())
                                .withSniPage(mainPage)
                                .withCount(0)
                                .build();
                        hubButtons.add(mainButton);
                        List<SniPage> hubChildren = hub.getHubChildren();
                        for (SniPage page : hubChildren) {
                            HubButton childButton = new HubButtonFactory()
                                    .withSniPage(page)
                                    .build();
                            if (childButton != null) {
                                hubButtons.add(childButton);
                            }
                        }
                        Collections.sort(hubButtons, new HubButtonKeyComparator());
                    }
                }
            }
        }
    }

    /**
     * Returns a List of HubButtons, sorted by their keys.
     * @return List of HubButtons
     */
    @Override
    public List<HubButton> getHubButtons() {
        return this.hubButtons;
    }
}
