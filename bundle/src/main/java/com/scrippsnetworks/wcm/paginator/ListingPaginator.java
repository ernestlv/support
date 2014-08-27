package com.scrippsnetworks.wcm.paginator;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.util.PageTypes;
import org.apache.commons.lang.math.IntRange;

import java.util.EnumSet;
import java.util.List;

/**
 * @author Jonathan Bell
 *         Date: 8/31/2013
 */
public interface ListingPaginator extends Paginator {
    public static final EnumSet<PageTypes> LISTING_PAGES =
        EnumSet.of(PageTypes.MENU_LISTING, PageTypes.RECIPE_LISTING, PageTypes.PHOTOGALLERY_LISTING);

    public <T> List<T> getCurrentItems();
    public List<SniPage> getCurrentPages();
    public int getListingSize();
    public IntRange getCurrentRange();
}
