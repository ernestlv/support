package com.scrippsnetworks.wcm.mobile.burgernavigation;


import com.scrippsnetworks.wcm.mobile.burgernavigation.impl.BurgerNavImpl;
import com.scrippsnetworks.wcm.page.SniPage;
import org.apache.sling.api.resource.Resource;

public class BurgerNavFactory {
    private Resource resource;
    private SniPage currentPage;

    public BurgerNav build(){
        if(resource == null){
            return null;
        }

        return new BurgerNavImpl(resource, currentPage);
    }

    public BurgerNavFactory withResource(Resource resource){
        this.resource = resource;
        return this;
    }
    public BurgerNavFactory withCurrentPage(SniPage currentPage){
        this.currentPage = currentPage;
        return this;
    }
}
