/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scrippsnetworks.wcm.paginator.impl;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.paginator.SearchResultPaginator;
import com.scrippsnetworks.wcm.topic.Topic;
import com.scrippsnetworks.wcm.util.PageTypes;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Patrick
 */
public class SearchResultPaginatorImpl implements SearchResultPaginator {
    
    private static final Logger log = LoggerFactory.getLogger(SearchResultPaginatorImpl.class);
    
    private SniPage sniPage;
    private PageTypes pageType;
    private int itemsPerPage = 0;
    private int totalPages = 0;
    private int totalItems = 0;

    //public StringBuffer logX = new StringBuffer("");

    public SearchResultPaginatorImpl(SniPage page) {
        this.sniPage = page;
        this.pageType = PageTypes.findPageType(page.getPageType());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalItems() {
        return totalItems;
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
        Integer deepLinkPageNumber = pageType == PageTypes.SEARCH_RESULTS ? getQueryParamPageNumber() : sniPage.getDeepLinkPageNumber();
        Integer total = getTotalPageCount();
        return deepLinkPageNumber != null && total != null ?
            (total >= deepLinkPageNumber && total > 0 ? deepLinkPageNumber : null)
                : null;
    }

    /** Convenience method for search result pages. */
    public Integer getQueryParamPageNumber() {
        Integer queryParamPageNumber = null;
        String paramPage = sniPage.getSlingRequest().getParameter("page");
        if (paramPage != null && Pattern.matches("^\\d+$", paramPage)) {
            queryParamPageNumber = Integer.valueOf(paramPage);
        }
        if (queryParamPageNumber == null || queryParamPageNumber < 1) {
            queryParamPageNumber = 1;
        }
        return queryParamPageNumber;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getTotalPageCount() {
        if (totalPages == 0) {
            Double pcc = new Double(Math.ceil(
                (totalItems + 0.0) / getItemsPerPage()
            ));
            totalPages = pcc.intValue();
        }
        return totalPages > 0 ? totalPages : 1;
    }

    public int getItemsPerPage() {
        if (itemsPerPage == 0) {
            itemsPerPage = 10;  //set a default
            if(pageType != null) {
                switch (pageType) {
                    case TOPIC:
                        itemsPerPage = Topic.ITEMS_PER_PAGE;
                        break;
                }
            }
        }
        return itemsPerPage;
    }

}
