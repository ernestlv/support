package com.scrippsnetworks.wcm.menu.listing;

import com.scrippsnetworks.wcm.menu.Menu;

import java.util.List;

/**
 * @author Jason Clark
 *         Date: 5/11/13
 */
public interface MenuListing {
    public static final int ITEMS_PER_PAGE = 12;

    public List<Menu> getMenus();
    public int getTotalSize();
}

