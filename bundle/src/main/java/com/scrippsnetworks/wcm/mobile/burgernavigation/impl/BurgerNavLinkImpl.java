package com.scrippsnetworks.wcm.mobile.burgernavigation.impl;

import com.scrippsnetworks.wcm.mobile.burgernavigation.BurgerNavLink;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

public class BurgerNavLinkImpl implements BurgerNavLink{
    private static final String TITLE_PROP = "title";
    private static final String URL_PROP = "path";
    private static final String IS_PRIMARY_PROP = "isPrimary";
    private static final String ACTIVE_CLASS = "active";


    private Resource resource;
    private String pagePath;
    private ValueMap vm;

    private String title;
    private String url;
    private Boolean isPrimary;
    private String activeClass;

    public BurgerNavLinkImpl(Resource resource, String pagePath){
        this.resource = resource;
        this.pagePath = pagePath;
        if (resource != null){
            this.vm = resource.adaptTo(ValueMap.class);
        }
    }

    @Override
    public String getTitle() {
        if (title == null && vm != null){
            title = vm.get(TITLE_PROP, "");
        }
        return title;
    }

    @Override
    public String getUrl() {
        if (url == null && vm != null){
            url = vm.get(URL_PROP, "");
        }
        return url;
    }

    @Override
    public boolean isPrimary() {
        if (isPrimary == null && vm != null){
            isPrimary = vm.get(IS_PRIMARY_PROP, Boolean.FALSE);
        }
        if (isPrimary == null){
            return false;
        }
        return isPrimary;
    }

    @Override
    public String getActiveClass() {
        if (activeClass == null && vm != null){
            String path = vm.get(URL_PROP, "");
            if (path.equals(pagePath)){
                activeClass = ACTIVE_CLASS;
            } else{
                activeClass = "";
            }
        }
        return activeClass;
    }
}
