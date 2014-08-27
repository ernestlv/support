package com.scrippsnetworks.wcm.menu.listing.impl;

import com.scrippsnetworks.wcm.menu.Menu;
import com.scrippsnetworks.wcm.menu.listing.MenuListing;
import com.scrippsnetworks.wcm.page.SniPage;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.menu.Menu;
import com.scrippsnetworks.wcm.menu.MenuFactory;
import com.scrippsnetworks.wcm.menu.listing.MenuListing;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jonathan Bell
 *         Date: 9/1/2013
 */
public class MenuListingImpl implements MenuListing {

    private static final String ITEMS_HOME = "menus";

    private Resource resource;
    private ValueMap vm;
    private int total;
    private List<Menu> menus;

    /**
     *
     * @param sniPage
     */
    public MenuListingImpl(final SniPage sniPage) {
        this.resource = sniPage.adaptTo(Resource.class);
        this.vm = sniPage.getContentResource().adaptTo(ValueMap.class);
        this.menus = new ArrayList<Menu>();
        initializeMenus();
    }

    private void initializeMenus() {
        String[] menuPaths = vm.get(ITEMS_HOME, String[].class);
        if (menuPaths != null) {

            MenuFactory mf = new MenuFactory();

            for (String menuPath : menuPaths) {
                if (!StringUtils.isEmpty(menuPath)) {
                    ResourceResolver rr = resource.getResourceResolver();
                    Resource res = rr.getResource(menuPath);
                    Menu menu = mf.withResource(res).build();
                    if (menu != null) {
                        menus.add(menu);
                    }
                }
            }

            total = menus.size();
        }
    }

    @Override
    public List<Menu> getMenus() {
        return menus;
    }

    @Override
    public int getTotalSize() {
        return total;
    }

}
