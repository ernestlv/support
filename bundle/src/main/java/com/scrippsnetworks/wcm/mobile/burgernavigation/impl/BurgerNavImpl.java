package com.scrippsnetworks.wcm.mobile.burgernavigation.impl;

import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.image.SniImageFactory;
import com.scrippsnetworks.wcm.mobile.burgernavigation.BurgerNav;
import com.scrippsnetworks.wcm.mobile.burgernavigation.BurgerNavLink;
import com.scrippsnetworks.wcm.mobile.burgernavigation.BurgerNavLinkFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BurgerNavImpl implements BurgerNav{
    private static final String FEATURE_SPOT_TITLE_PROP = "title";
    private static final String FEATURE_SPOT_DESCRIPTION_PROP = "description";
    private static final String FEATURE_SPOT_LINK_PROP = "link";
    private static final String IMAGE_NODE_NAME = "image";
    private static final String IMAGE_FILE_REFERENCE_PROP = "fileReference";
    private static final String LINKS_CONTAINER_NODE_NAME = "items";

    private static final BurgerNavLinkFactory burgerNavLinkFactory = new BurgerNavLinkFactory();

    private Resource resource;
    private SniPage currentPage;
    private ValueMap vm;

    private String featureSpotTitle;
    private String featureSpotDescription;
    private SniImage featureSpotImage;
    private String featureSpotLink;
    private List<BurgerNavLink> links;

    public BurgerNavImpl(Resource resource, SniPage currentPage) {
        this.resource = resource;
        this.currentPage = currentPage;
        if (resource != null) {
            this.vm = resource.adaptTo(ValueMap.class);
        }
    }

    @Override
    public String getFeatureSpotTitle() {
        if (featureSpotTitle == null && vm != null){
            featureSpotTitle = vm.get(FEATURE_SPOT_TITLE_PROP, "");
        }
        return featureSpotTitle;
    }

    @Override
    public String getFeatureSpotDescription() {
        if (featureSpotDescription == null && vm != null){
            featureSpotDescription = vm.get(FEATURE_SPOT_DESCRIPTION_PROP, "");
        }
        return featureSpotDescription;
    }

    @Override
    public SniImage getFeatureSpotImage() {
        if (featureSpotImage == null && vm != null){
            Resource imageRes = resource.getChild(IMAGE_NODE_NAME);

            if (imageRes != null){
                ValueMap imageVm = imageRes.adaptTo(ValueMap.class);
                String imagePath = imageVm.get(IMAGE_FILE_REFERENCE_PROP, "");
                featureSpotImage = new SniImageFactory().withPath(imagePath).build();
            }
        }
        return featureSpotImage;
    }

    @Override
    public List<BurgerNavLink> getLinks() {
        if (links == null) {
            links  = new ArrayList<BurgerNavLink>();

            String curPagePath = currentPage.getPath();

            Resource linkContainerRes = resource.getChild(LINKS_CONTAINER_NODE_NAME);

            if (linkContainerRes == null) {
                return links;
            }

            Iterator<Resource> linksRes = linkContainerRes.listChildren();

            while (linksRes.hasNext()) {
                Resource linkRes = linksRes.next();
                BurgerNavLink link = burgerNavLinkFactory.withResource(linkRes).withCurrentPagePath(curPagePath).build();
                links.add(link);
            }
        }
        return links;
    }

    @Override
    public String getFeatureSpotLink() {
        if (featureSpotLink == null && vm != null){
            featureSpotLink = vm.get(FEATURE_SPOT_LINK_PROP, "");

            if (featureSpotLink == ""){
                featureSpotLink = "#";
                return featureSpotLink;
            }
            if (featureSpotLink.charAt(0) != '/'){
                return featureSpotLink;
            }

            ResourceResolver resourceResolver = resource.getResourceResolver();
            Resource linkRes = resourceResolver.getResource(featureSpotLink);
            if (linkRes == null){
                featureSpotLink = "#";
                return featureSpotLink;
            }

            if (featureSpotLink.contains(".html")){
                return featureSpotLink;
            }

            featureSpotLink = featureSpotLink + ".html";
        }
        return featureSpotLink;
    }
}
