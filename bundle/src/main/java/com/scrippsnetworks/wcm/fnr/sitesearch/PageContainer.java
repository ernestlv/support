package com.scrippsnetworks.wcm.fnr.sitesearch;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Container for Pages.
 * <p/>
 * Also maintains links to the previous and next page where applicable.
 */
public class PageContainer {

    private int totalAssets;
    private final Integer currentPageNumber;
    private final Integer numberOfPages;
    private Page previous = null;
    private Page next = null;

    UrlHelper urlHelper;

    private final List<Page> pages = new ArrayList<Page>();

    public PageContainer(int currentPageNumber, int totalAssets, int itemsPerPage, UrlHelper urlHelper) {
        this.totalAssets = totalAssets;
        this.currentPageNumber = currentPageNumber;
        if (totalAssets == 0) {
            totalAssets = itemsPerPage;
        }
        int lastPageItems = totalAssets % itemsPerPage;
        this.numberOfPages = totalAssets / itemsPerPage + (lastPageItems > 0 ? 1 : 0);

        Iterator<Integer> pageIterator = getPagesSet().iterator();
        Integer lastPage = 0;
        while (pageIterator.hasNext()) {
            Integer i = pageIterator.next();
            if (i - lastPage != 1) {
                pages.add(new Page(null, null));
            }
            lastPage = i;
            String url = null;
            if (i != currentPageNumber) {
                url = urlHelper.getSearchUrlForPage(i);
            }

            Page thisPage = new Page(i, url);

            if (i == currentPageNumber - 1) {
                previous = thisPage;
            }

            if (i == currentPageNumber + 1) {
                next = thisPage;
            }

            pages.add(new Page(i, url));
        }
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setNextPage(Page next) {
        this.next = next;
    }

    public Page getNextPage() {
        return next;
    }

    public void setPreviousPage(Page previous) {
        this.previous = previous;
    }

    public Page getPreviousPage() {
        return previous;
    }
    
    public int getNumberOfPages() {
        return numberOfPages;
    }
    
    public int getTotalAssets() {
        return totalAssets;
    }

    /**
     * Returns a set of integers for page numbers that should appear in the pagination area.
     */
    Set<Integer> getPagesSet() {
        Set<Integer> pageNumbers = new TreeSet<Integer>();
        pageNumbers.add(1);
        if (currentPageNumber > 1) {
            pageNumbers.add(2);
        }
        pageNumbers.add(numberOfPages);

        for (Integer i = currentPageNumber; i < numberOfPages && i <= currentPageNumber + 2; i++) {
            pageNumbers.add(i);
        }

        for (Integer i = currentPageNumber; i > 0 && i >= currentPageNumber - 2; i--) {
            pageNumbers.add(i);
        }
        return pageNumbers;
    }
}
