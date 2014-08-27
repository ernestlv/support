package com.scrippsnetworks.wcm.fnr.ui;

import com.scrippsnetworks.wcm.paginator.ParagraphPaginator;

import java.util.ArrayList;
import java.util.List;

/**
 * Information about a known UiComponent that supports pagination,
 * has a prev/next button
 * has leading and trailing ellipses
 * with specific rules
 *
 * this class serves 2 purposes
 * 1) as a data container which configures the PaginationUiComponent (item marked with CSS class "pagination")
 * examples of things configured: whether leading ellipsis should be shown, whether "next" button should be activated
 *
 * 2) on construction, given totalPageCount and currentPage, it will resolve UI rules and set the data members of
 * the resultant object (that is, creating #1 above)
 * @author Ken Shih (156223)
 * @created 7/8/13 5:20 PM
 */
public class PaginationUiComponent {
    final private boolean isPrevBtnActive;
    final private boolean isNextBtnActive;
    final private boolean hasLeadingEllipsis;
    final private boolean hasTrailingEllipsis;
    final private List<Page> pages;
    /**
     * information about a page represented in the PaginationUIComponent
     * a.k.a. imagine a number with a box around it that links to a page
     */
    public static class Page {
        final int pageNumber;
        final boolean isActive;
        Page(final int pageNumber, final boolean isActive){
            this.pageNumber=pageNumber;
            this.isActive=isActive;
        }
        public int getPageNumber() {
            return pageNumber;
        }
        public boolean isActive() {
            return isActive;
        }
    }

    public PaginationUiComponent(ParagraphPaginator paragraphPaginator){
        int currentPg = paragraphPaginator.getCurrentPageNumber()==null?0:paragraphPaginator.getCurrentPageNumber();
        int totalPgCount = paragraphPaginator.getTotalPageCount()==null?0:paragraphPaginator.getTotalPageCount();
        isPrevBtnActive = isPrevBtnActive(currentPg, totalPgCount);
        isNextBtnActive = isNextBtnActive(currentPg, totalPgCount);
        hasLeadingEllipsis = hasLeadingEllipsis(currentPg,totalPgCount);
        hasTrailingEllipsis = hasTrailingEllipsis(currentPg,totalPgCount);
        pages = createPages(currentPg,totalPgCount);
    }

    // rules
    static List<Page> createPages(final int currentPage, final int totalPageCount){
        List<Page> pages=new ArrayList<Page>();
        for(int i=1; i<=totalPageCount ;i++){
            if( i < 3 || i==totalPageCount //the ends
                || (i >= currentPage - 2 && i <= currentPage +2) // the 2 numbers surrounding current page
                    ){
                Page page = new Page(i,i==currentPage);
                pages.add(page);
            }
        }
        return pages;
    }
    static boolean isPrevBtnActive(int currentPage, int totalPageCount){
        if(currentPage > 1){
            return true;
        }
        return false;
    }
    static boolean isNextBtnActive(int currentPage, int totalPageCount){
        if(currentPage < totalPageCount){
            return true;
        }
        return false;
    }
    static boolean hasLeadingEllipsis(int currentPage, int totalPageCount){
        if(currentPage >=6){
            return true;
        }
        return false;
    }
    static boolean hasTrailingEllipsis(int currentPage, int totalPageCount){
        if(totalPageCount > currentPage + 3){
            return true;
        }
        return false;
    }

    //getters
    public boolean isPrevBtnActive() {
        return isPrevBtnActive;
    }

    public boolean isNextBtnActive() {
        return isNextBtnActive;
    }

    public boolean isHasLeadingEllipsis() {
        return hasLeadingEllipsis;
    }

    public boolean isHasTrailingEllipsis() {
        return hasTrailingEllipsis;
    }

    public List<Page> getPages() {
        return pages;
    }

}
