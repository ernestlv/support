/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scrippsnetworks.wcm.paginator;

import com.scrippsnetworks.wcm.util.PageTypes;
import java.util.EnumSet;

/**
 * A call is already made to the SearchComponentBean in JSP, so 
 * this class exists in order to prevent making the search call twice.
 * @author Patrick Armstrong
 */
public interface SearchResultPaginator extends Paginator {
    public static final EnumSet<PageTypes> SEARCH_RESULT_PAGES = EnumSet.of(PageTypes.TOPIC,PageTypes.ASSET_RECIPES,PageTypes.SOURCE_RECIPES,PageTypes.SEARCH_RESULTS);
    
    public void setTotalItems(int totalItems);
}
