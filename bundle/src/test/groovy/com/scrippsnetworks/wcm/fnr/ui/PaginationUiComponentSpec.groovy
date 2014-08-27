package com.scrippsnetworks.wcm.fnr.ui

import com.scrippsnetworks.wcm.paginator.ParagraphPaginator
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Ken Shih (156223)
 * @created 7/9/13 3:49 PM
 */
class PaginationUiComponentSpec extends Specification {
    ParagraphPaginator mockParagraphPaginator;
    def setup(){
        mockParagraphPaginator = Mock()
    }

    /**
     * simulates a UI component so provides some visual element as a proxy for the html/js element
     */
    def mockUiComponent = {paginationUiComponent ->
        def prevBtn = paginationUiComponent.prevBtnActive?"<<":""
        def nextBtn = paginationUiComponent.nextBtnActive?">>":""
        def leadEll = paginationUiComponent.hasLeadingEllipsis?"..":""
        def trailEll = paginationUiComponent.hasTrailingEllipsis?",,":""
        def pgs =""
        paginationUiComponent.pages.each { pg ->
            def activeIndicator = pg.active?"*":""
            pgs += "$activeIndicator${pg.pageNumber} "
        }
        "$prevBtn$leadEll$pgs$trailEll$nextBtn"
    }

    def "asset with single page rules"(){
        when:
        mockParagraphPaginator.totalPageCount >> 1
        mockParagraphPaginator.currentPageNumber >> 1
        PaginationUiComponent paginationUiComponent = new PaginationUiComponent(mockParagraphPaginator);

        then:
        paginationUiComponent.pages.size() == pgSize
        mockUiComponent(paginationUiComponent) == pgString

        where:
        activePg | pgSize | pgString
        1 | 1 | "*1 "
    }

    @Unroll
    def "asset with 2 page rules"(){
        when:
        mockParagraphPaginator.totalPageCount >> 2
        mockParagraphPaginator.currentPageNumber >> activePg
        PaginationUiComponent paginationUiComponent = new PaginationUiComponent(mockParagraphPaginator);

        then:
        paginationUiComponent.pages.size() == pgSize
        mockUiComponent(paginationUiComponent) == pgString

        where:
        activePg | pgSize | pgString
        1 | 2 | "*1 2 >>"
        2 | 2 | "<<1 *2 "
    }

    def "asset with 3 page rules"(){
        when:
        mockParagraphPaginator.totalPageCount >> 3
        mockParagraphPaginator.currentPageNumber >> activePg
        PaginationUiComponent paginationUiComponent = new PaginationUiComponent(mockParagraphPaginator);

        then:
        paginationUiComponent.pages.size() == pgSize
        mockUiComponent(paginationUiComponent) == pgString

        where:
        activePg | pgSize | pgString
        1 | 3 | "*1 2 3 >>"
        2 | 3 | "<<1 *2 3 >>"
        3 | 3 | "<<1 2 *3 "
    }

    def "asset with 4 page rules"(){
        when:
        mockParagraphPaginator.totalPageCount >> 4
        mockParagraphPaginator.currentPageNumber >> activePg
        PaginationUiComponent paginationUiComponent = new PaginationUiComponent(mockParagraphPaginator);

        then:
        paginationUiComponent.pages.size() == pgSize
        mockUiComponent(paginationUiComponent) == pgString

        where:
        activePg | pgSize | pgString
        1 | 4 | "*1 2 3 4 >>"
        2 | 4 | "<<1 *2 3 4 >>"
        3 | 4 | "<<1 2 *3 4 >>"
        4 | 4 | "<<1 2 3 *4 "
    }

    def "asset with 5 page rules"(){
        when:
        mockParagraphPaginator.totalPageCount >> 5
        mockParagraphPaginator.currentPageNumber >> activePg
        PaginationUiComponent paginationUiComponent = new PaginationUiComponent(mockParagraphPaginator);

        then:
        paginationUiComponent.pages.size() == pgSize
        mockUiComponent(paginationUiComponent) == pgString

        where:
        activePg | pgSize | pgString
        1 | 4 | "*1 2 3 5 ,,>>"
        2 | 5 | "<<1 *2 3 4 5 >>"
        3 | 5 | "<<1 2 *3 4 5 >>"
        4 | 5 | "<<1 2 3 *4 5 >>"
        5 | 5 | "<<1 2 3 4 *5 "
    }

    def "asset with 6 page rules"(){
        when:
        mockParagraphPaginator.totalPageCount >> 6
        mockParagraphPaginator.currentPageNumber >> activePg
        PaginationUiComponent paginationUiComponent = new PaginationUiComponent(mockParagraphPaginator);

        then:
        paginationUiComponent.pages.size() == pgSize
        mockUiComponent(paginationUiComponent) == pgString

        where:
        activePg | pgSize | pgString
        1 | 4 | "*1 2 3 6 ,,>>"
        2 | 5 | "<<1 *2 3 4 6 ,,>>"
        3 | 6 | "<<1 2 *3 4 5 6 >>"
        4 | 6 | "<<1 2 3 *4 5 6 >>"
        5 | 6 | "<<1 2 3 4 *5 6 >>"
        6 | 5 | "<<..1 2 4 5 *6 "
    }

    def "asset with 7 page rules"(){
        when:
        mockParagraphPaginator.totalPageCount >> 7
        mockParagraphPaginator.currentPageNumber >> activePg
        PaginationUiComponent paginationUiComponent = new PaginationUiComponent(mockParagraphPaginator);

        then:
        paginationUiComponent.pages.size() == pgSize
        mockUiComponent(paginationUiComponent) == pgString

        where:
        activePg | pgSize | pgString
        1 | 4 | "*1 2 3 7 ,,>>"
        2 | 5 | "<<1 *2 3 4 7 ,,>>"
        3 | 6 | "<<1 2 *3 4 5 7 ,,>>"
        4 | 7 | "<<1 2 3 *4 5 6 7 >>"
        5 | 7 | "<<1 2 3 4 *5 6 7 >>"
        6 | 6 | "<<..1 2 4 5 *6 7 >>"
        7 | 5 | "<<..1 2 5 6 *7 "
    }

    def "asset with 8 page rules"(){
        when:
        mockParagraphPaginator.totalPageCount >> 8
        mockParagraphPaginator.currentPageNumber >> activePg
        PaginationUiComponent paginationUiComponent = new PaginationUiComponent(mockParagraphPaginator);

        then:
        paginationUiComponent.pages.size() == pgSize
        mockUiComponent(paginationUiComponent) == pgString

        where:
        activePg | pgSize | pgString
        1 | 4 | "*1 2 3 8 ,,>>"
        2 | 5 | "<<1 *2 3 4 8 ,,>>"
        3 | 6 | "<<1 2 *3 4 5 8 ,,>>"
        4 | 7 | "<<1 2 3 *4 5 6 8 ,,>>"
        5 | 8 | "<<1 2 3 4 *5 6 7 8 >>"
        6 | 7 | "<<..1 2 4 5 *6 7 8 >>"
        7 | 6 | "<<..1 2 5 6 *7 8 >>"
        8 | 5 | "<<..1 2 6 7 *8 "
    }

    def "asset with 9 page rules"(){
        when:
        mockParagraphPaginator.totalPageCount >> 9
        mockParagraphPaginator.currentPageNumber >> activePg
        PaginationUiComponent paginationUiComponent = new PaginationUiComponent(mockParagraphPaginator);

        then:
        paginationUiComponent.pages.size() == pgSize
        mockUiComponent(paginationUiComponent) == pgString

        where:
        activePg | pgSize | pgString
        1 | 4 | "*1 2 3 9 ,,>>"
        2 | 5 | "<<1 *2 3 4 9 ,,>>"
        3 | 6 | "<<1 2 *3 4 5 9 ,,>>"
        4 | 7 | "<<1 2 3 *4 5 6 9 ,,>>"
        5 | 8 | "<<1 2 3 4 *5 6 7 9 ,,>>"
        6 | 8 | "<<..1 2 4 5 *6 7 8 9 >>"
        7 | 7 | "<<..1 2 5 6 *7 8 9 >>"
        8 | 6 | "<<..1 2 6 7 *8 9 >>"
        9 | 5 | "<<..1 2 7 8 *9 "
    }

    def "asset with 10 page rules"(){
        when:
        mockParagraphPaginator.totalPageCount >> 10
        mockParagraphPaginator.currentPageNumber >> activePg
        PaginationUiComponent paginationUiComponent = new PaginationUiComponent(mockParagraphPaginator);

        then:
        paginationUiComponent.pages.size() == pgSize
        mockUiComponent(paginationUiComponent) == pgString

        where:
        activePg | pgSize | pgString
        1 | 4 | "*1 2 3 10 ,,>>"
        2 | 5 | "<<1 *2 3 4 10 ,,>>"
        3 | 6 | "<<1 2 *3 4 5 10 ,,>>"
        4 | 7 | "<<1 2 3 *4 5 6 10 ,,>>"
        5 | 8 | "<<1 2 3 4 *5 6 7 10 ,,>>"
        6 | 8 | "<<..1 2 4 5 *6 7 8 10 ,,>>"
        7 | 8 | "<<..1 2 5 6 *7 8 9 10 >>"
        8 | 7 | "<<..1 2 6 7 *8 9 10 >>"
        9 | 6 | "<<..1 2 7 8 *9 10 >>"
        10| 5 | "<<..1 2 8 9 *10 "
    }
}
