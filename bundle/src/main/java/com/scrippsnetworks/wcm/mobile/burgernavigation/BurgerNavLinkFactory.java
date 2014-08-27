package com.scrippsnetworks.wcm.mobile.burgernavigation;

import com.scrippsnetworks.wcm.mobile.burgernavigation.impl.BurgerNavImpl;
import com.scrippsnetworks.wcm.mobile.burgernavigation.impl.BurgerNavLinkImpl;
import org.apache.sling.api.resource.Resource;

public class BurgerNavLinkFactory {
    private Resource resource;
    private String pagePath;

    public BurgerNavLink build(){
        if(resource == null){
            return null;
        }

        return new BurgerNavLinkImpl(resource, pagePath);
    }

    public BurgerNavLinkFactory withResource(Resource resource){
        this.resource = resource;
        return this;
    }

    public BurgerNavLinkFactory withCurrentPagePath(String pagePath){
        this.pagePath = pagePath;
        return this;
    }
}
