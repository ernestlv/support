package com.scrippsnetworks.wcm.mobile.subnavigation;

import com.scrippsnetworks.wcm.mobile.burgernavigation.BurgerNavLink;
import com.scrippsnetworks.wcm.mobile.subnavigation.impl.SubNavElemImpl;
import com.scrippsnetworks.wcm.page.SniPage;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SubNavigationFactory {
    private Resource resource;
    private SniPage currentPage;

    public List<SubNavElem> build(){
        if(resource == null){
            return null;
        }

        List<SubNavElem> subNavElems = new ArrayList<SubNavElem>();

        Iterator<Resource> linksRes = resource.listChildren();

        while (linksRes.hasNext()) {
            Resource linkRes = linksRes.next();

            SubNavElem link = new SubNavElemImpl(linkRes, currentPage);
            if (StringUtils.isNotEmpty(link.getTitle())){
                subNavElems.add(link);
            }
        }


        return subNavElems;
    }

    public SubNavigationFactory withResource(Resource resource){
        this.resource = resource;
        return this;
    }

    public SubNavigationFactory withCurrentPage(SniPage currentPage){
        this.currentPage = currentPage;
        return this;
    }
}
