package com.scrippsnetworks.wcm.paginator.impl;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.paginator.ListingPaginator;
import com.scrippsnetworks.wcm.menu.listing.MenuListing;
import com.scrippsnetworks.wcm.menu.listing.MenuListingFactory;
import com.scrippsnetworks.wcm.photogallery.listing.PhotoGalleryListing;
import com.scrippsnetworks.wcm.photogallery.listing.PhotoGalleryListingFactory;
import com.scrippsnetworks.wcm.recipe.Recipe;
import com.scrippsnetworks.wcm.recipe.listing.RecipeListing;
import com.scrippsnetworks.wcm.recipe.listing.RecipeListingFactory;
import com.scrippsnetworks.wcm.util.PageTypes;
import org.apache.commons.lang.math.IntRange;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jonathan Bell
 *         Date: 8/31/2013
 *
 */
public class ListingPaginatorImpl implements ListingPaginator {

    private SniPage sniPage;
    private PageTypes pageType;
    private MenuListing menuListing;
    private PhotoGalleryListing photoGalleryListing;
    private RecipeListing recipeListing;
    private int itemsPerPage = 0;
    private int totalPages = 0;
    private int totalItems = 0;
    private List<?> allItems = null;
    private IntRange currentRange;
    private List<SniPage> currentPages;

    public ListingPaginatorImpl(SniPage page) {
        this.sniPage = page;
        this.pageType = PageTypes.findPageType(page.getPageType()); 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getPrevPageNumber() {
        Integer currentPageNumber = getCurrentPageNumber();
        if (currentPageNumber != null
                && currentPageNumber > 1
                && currentPageNumber <= getTotalPageCount()) {
            return currentPageNumber-1;
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getNextPageNumber() {
        Integer currentPageNumber = getCurrentPageNumber();
        if (currentPageNumber != null && currentPageNumber < getTotalPageCount()) {
            return currentPageNumber+1;
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getCurrentPageNumber() {
        Integer deepLinkPageNumber = sniPage.getDeepLinkPageNumber();
        Integer total = getTotalPageCount();
        return deepLinkPageNumber != null && total != null ?
            (total >= deepLinkPageNumber && total > 0 ? deepLinkPageNumber : null)
                : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getTotalPageCount() {
        if (totalPages == 0) {
            Double pcc = new Double(Math.ceil(
                (getListingSize() + 0.0) / getItemsPerPage()
            ));
            totalPages = pcc.intValue();
        }

        return totalPages > 0 ? totalPages : 1;
    }

    private int getItemsPerPage() {
        if (itemsPerPage == 0) {
            switch (pageType) {
                case MENU_LISTING:
                    itemsPerPage = MenuListing.ITEMS_PER_PAGE;
                    break;
                case PHOTOGALLERY_LISTING:
                    itemsPerPage = PhotoGalleryListing.ITEMS_PER_PAGE;
                    break;
                case RECIPE_LISTING:
                    itemsPerPage = RecipeListing.ITEMS_PER_PAGE;
                    break;
                default:
                    break;
            }
        }

        return itemsPerPage;
    }

    @Override
    public int getListingSize() {
        if (totalItems == 0) {
            switch (pageType) {
                case MENU_LISTING:
                    totalItems = getMenuListing().getTotalSize();
                    break;
                case PHOTOGALLERY_LISTING:
                    totalItems = getPhotoGalleryListing().getTotalSize();
                    break;
                case RECIPE_LISTING:
                    totalItems = getRecipeListing().getTotalSize();
                    break;
                default:
                    break;
            }
        }

        return totalItems;
    }

    private <T> List<T> getAllItems() {
        if (allItems == null) {
            switch (pageType) {
                case MENU_LISTING:
                    allItems = (List<T>) getMenuListing().getMenus();
                    break;
                case PHOTOGALLERY_LISTING:
                    allItems = (List<T>) getPhotoGalleryListing().getPhotoGalleries();
                    break;
                case RECIPE_LISTING:
                    allItems = (List<T>) getRecipeListing().getRecipes();
                    break;
                default:
                    break;
            }
        }

        return (List<T>) allItems;
    }

    @Override
    public <T> List<T> getCurrentItems() {
        List<T> currentItems;
        IntRange myRange = getCurrentRange();

        currentItems = getAllItems();
        if (currentItems != null) {
            currentItems = currentItems.subList(myRange.getMinimumInteger(), myRange.getMaximumInteger());
        }

        return currentItems;
    }

    @Override
    public List<SniPage> getCurrentPages() {
        if (currentPages == null) {
            currentPages = new ArrayList<SniPage>();
            switch (pageType) {
                case RECIPE_LISTING:
                    List<?> myItems = getCurrentItems();
                    if (myItems != null) {
                        for (int i=0; i<myItems.size(); i++) {
                            Recipe r = (Recipe) myItems.get(i);
                            currentPages.add(r.getRecipePage());
                        }
                    }
                    break;
            }
        }

        return currentPages;
    }

    @Override
    public IntRange getCurrentRange() {
        int start = 0;
        int end = 0;

        if (currentRange == null) {
            if (getCurrentPageNumber() != null) {
                start = getItemsPerPage() * (getCurrentPageNumber()-1);
                end = start + getItemsPerPage();
                if (start > getListingSize()) {
                    start = 0;
                    end = 0;
                } else if (end > getListingSize()) {
                    end = getListingSize();
                }
            }
            currentRange = new IntRange(start, end);
        }

        return currentRange;
    }

    private MenuListing getMenuListing() {
        if (menuListing == null) {
            menuListing = new MenuListingFactory()
                .withSniPage(sniPage)
                .build();
        }

        return menuListing;
    }

    private PhotoGalleryListing getPhotoGalleryListing() {
        if (photoGalleryListing == null) {
            photoGalleryListing = new PhotoGalleryListingFactory()
                .withSniPage(sniPage)
                .build();
        }

        return photoGalleryListing;
    }

    private RecipeListing getRecipeListing() {
        if (recipeListing == null) {
            recipeListing = new RecipeListingFactory()
                .withSniPage(sniPage)
                .build();
        }

        return recipeListing;
    }
}

