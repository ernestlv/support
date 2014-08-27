package com.scrippsnetworks.wcm.paginator;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.paginator.impl.ListingPaginatorImpl;
import com.scrippsnetworks.wcm.paginator.impl.ParagraphPaginatorImpl;
import com.scrippsnetworks.wcm.paginator.impl.SearchResultPaginatorImpl;
import com.scrippsnetworks.wcm.util.PageTypes;
import com.scrippsnetworks.wcm.util.SniSlingUtilService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ValueMap;

/**
 * Used in global init.jsp to initialize a pagination object
 * where a Paginator implements a certain paginator strategy
 *
 * Currently, if a page has sni:paginatedParsys set, then a ParagraphPaginator will be returned
 * Otherwise, null
 *
 *
 * @author Jason Clark
 *         Date: 4/28/13
 * updated Ken Shih 7/5/2013 added ParagraphPaginator and refactored Paginator interface
 *
 */
@Component(immediate=true, metatype=true, label="PaginatorFactory",
        description="provides Paginators implemented with a strategy to paginate a page")
@Service(value=PaginatorFactory.class)
public class PaginatorFactory {

    private SniPage page;

    @Reference
    SniSlingUtilService sniSlingUtilService;

    //private paginationStrategy //TODO possible support: [ParagraphPaginator, SearchPaginator]

    public Paginator build() {
        if (page != null && page.getProperties() != null) {
            PageTypes type = PageTypes.findPageType(page.getPageType());
            ValueMap vm = page.getProperties();
            if (vm.containsKey(ParagraphPaginator.PROPERTY_SNI_PAGINATED_PARSYS)) {
                return new ParagraphPaginatorImpl(page, sniSlingUtilService);
            } else if (ListingPaginator.LISTING_PAGES.contains(type)) {
                return new ListingPaginatorImpl(page);
            } else if (SearchResultPaginator.SEARCH_RESULT_PAGES.contains(type)) {
                return new SearchResultPaginatorImpl(page);
            }
        }
        return null;
    }

    public PaginatorFactory withSniPage(SniPage page) {
        this.page = page;
        return this;
    }

}
