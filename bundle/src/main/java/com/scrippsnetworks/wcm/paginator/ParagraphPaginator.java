package com.scrippsnetworks.wcm.paginator;

import com.day.cq.wcm.foundation.Paragraph;

import java.util.List;

/**
 * @author Jason Clark
 *         Date: 4/28/13
 * updated Ken Shih 7/5/13  refactored PaginatorImpl into ParagraphPaginator
 */
public interface ParagraphPaginator extends Paginator {
    public static final String RC_TYPE_PARSYS = "foundation/components/parsys";

    /** a marker that let's Factory know that a page participates in the ParagraphPagination strategy of pagination */
    public static final String PROPERTY_SNI_PAGINATED_PARSYS = "sni:paginatedParsys";

    public static final String PROPERTY_SNI_PAGINATED_PARSYS_PAGE = "sni:paginatedParsysPage";

    /** a Paragraph, that itself is a page break (so you must preserve the content itself) */
    public static final String VALUE_STANDALONE_SLIDE= "standalone-slide";

    /** a page break component that can be split() on */
    public static final String RC_TYPE_PAGE_BREAK = "components/util/page-break";

    /** the content available on the current page only (a subset of ALL paragraphs in the parsys) */
    public List<Paragraph> getCurrentPageParagraphs();
}
