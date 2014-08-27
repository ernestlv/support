package com.scrippsnetworks.wcm.paginator;

/**
 * @author Ken Shih (156223)
 * @created 7/11/13 5:32 PM
 */
public interface Paginator {
    /**
     * @return the page you're on according to convention this Paginator implements/assumes
     */
    public Integer getCurrentPageNumber();

    /**
     * @return the totally number of pages in this asset
     */
    public Integer getTotalPageCount();

    /**
     * @return null means there is no previous page number (you're probably on page 1)
     */
    public Integer getPrevPageNumber();

    /**
     * @return null means there is no next page number (you're probably on the last page)
     */
    public Integer getNextPageNumber();
}
