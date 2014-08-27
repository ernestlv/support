package com.scrippsnetworks.wcm.mobile.burgernavigation;

import com.scrippsnetworks.wcm.image.SniImage;

import java.util.List;

public interface BurgerNav {
    public String getFeatureSpotTitle();
    public String getFeatureSpotDescription();
    public SniImage getFeatureSpotImage();
    public List<BurgerNavLink> getLinks();
    public String getFeatureSpotLink();
}
