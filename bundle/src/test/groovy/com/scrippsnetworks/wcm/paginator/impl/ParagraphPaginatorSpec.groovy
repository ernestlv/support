package com.scrippsnetworks.wcm.paginator.impl

import com.day.cq.wcm.foundation.Paragraph
import com.day.cq.wcm.foundation.ParagraphSystem
import com.scrippsnetworks.wcm.page.SniPage
import com.scrippsnetworks.wcm.paginator.ParagraphPaginator
import com.scrippsnetworks.wcm.resource.MockResource
import com.scrippsnetworks.wcm.util.SniSlingUtilService
import org.apache.sling.api.resource.Resource
import org.apache.sling.api.resource.ValueMap
import spock.lang.Specification

/**
 *
 * User: kenshih
 * Date: 7/5/13
 */
class ParagraphPaginatorSpec extends Specification {

    //private static final MockPage.MockPageFactory MOCK_PAGE_FACTORY = new MockPage.MockPageFactory()
    private static final MockResource.MockResourceFactory MOCK_RC_FACTORY = new MockResource.MockResourceFactory()

    SniPage mockSniPage
    ValueMap mockProperties
    Resource mockParsysRc
    ParagraphSystem mockParagraphSystem
    List<Paragraph> mockParagraphList
    SniSlingUtilService mockSniSlingUtilService

    def setup(){
        mockSniPage = Mock(SniPage)
        mockSniSlingUtilService = Mock(SniSlingUtilService)
        mockProperties = Mock(ValueMap)
        mockParsysRc = Mock(Resource)
        mockParagraphSystem = Mock(ParagraphSystem)
        mockParagraphList = new ArrayList<Paragraph>()

        mockSniPage.getProperties() >> mockProperties
        mockProperties.containsKey(_) >> true
        mockProperties.get("sni:paginatedParsys") >> "relative-node-of-parsys"
        mockSniPage.getContentResource("relative-node-of-parsys") >> mockParsysRc
        mockParsysRc.isResourceType(_) >> true
        mockParagraphSystem.paragraphs() >> mockParagraphList
    }

    def "get page 1 with some content"(){
        when:
        mockSniPage.getDeepLinkPageNumber() >> 1
        Paragraph mockParagraphRc = Mock(Paragraph)
        mockParagraphList.add(mockParagraphRc)
        mockParagraphRc.getResourceType() >> "components/util/not-a-page-break"

        ParagraphPaginator paginator = new ParagraphPaginatorImpl(mockSniPage,mockSniSlingUtilService)
        paginator.setParagraphSystem(mockParagraphSystem)

        then:
        paginator.currentPageParagraphs.size() == 1
        paginator.currentPageNumber == 1
        paginator.totalPageCount == 1
        paginator.nextPageNumber == null
        paginator.prevPageNumber == null
    }

    def "get page 2 with some content"(){
        when:
        mockSniPage.getDeepLinkPageNumber() >> 2

        def addParagraph = {name ->
            Paragraph mockPara = Mock(Paragraph)
            mockPara.getResourceType() >> "sni-core/components/util/$name"
            mockParagraphList.add(mockPara)
        }
        addParagraph "not-a-page-break1"
        addParagraph "page-break"
        addParagraph "not-a-page-break2"
        addParagraph "not-a-page-break3"
        addParagraph "not-a-page-break4"
        addParagraph "page-break"
        addParagraph "not-a-page-break5"

        ParagraphPaginator paginator = new ParagraphPaginatorImpl(mockSniPage,mockSniSlingUtilService)
        paginator.setParagraphSystem(mockParagraphSystem)

        then:
        paginator.currentPageParagraphs.size() == 3
        paginator.currentPageNumber == 2
        paginator.totalPageCount == 3
        paginator.nextPageNumber == 3
        paginator.prevPageNumber == 1
    }

    def "get page 3 with some content from page with eponymous-page-breaks"(){
        when:
        mockSniPage.getDeepLinkPageNumber() >> 3
        mockSniSlingUtilService.isPropertyOnResourceOrAncestor(_,_,_) >> true

        def addParagraph = {name ->
            Paragraph mockPara = Mock(Paragraph)
            mockPara.getResourceSuperType() >> "sni-core/components/util/$name"
            mockParagraphList.add(mockPara)
        }
        addParagraph "eponymous-page-break1"
        addParagraph "eponymous-page-break2"
        addParagraph "eponymous-page-break3"
        addParagraph "eponymous-page-break4"
        addParagraph "eponymous-page-break5"

        ParagraphPaginator paginator = new ParagraphPaginatorImpl(mockSniPage,mockSniSlingUtilService)
        paginator.setParagraphSystem(mockParagraphSystem)

        then:
        paginator.currentPageParagraphs.size() == 1
        paginator.currentPageNumber == 3
        paginator.totalPageCount == 5
        paginator.nextPageNumber == 4
        paginator.prevPageNumber == 2
    }

    def "get page 3 when there are only 2 pages with eponymous-page-breaks"(){
        when:
        mockSniPage.getDeepLinkPageNumber() >> 3
        mockSniSlingUtilService.isPropertyOnResourceOrAncestor(_,_,_) >> true

        def addParagraph = {name ->
            Paragraph mockPara = Mock(Paragraph)
            mockPara.getResourceSuperType() >> "sni-core/components/util/$name"
            mockParagraphList.add(mockPara)
        }
        addParagraph "eponymous-page-break1"
        addParagraph "eponymous-page-break2"

        ParagraphPaginator paginator = new ParagraphPaginatorImpl(mockSniPage,mockSniSlingUtilService)
        paginator.setParagraphSystem(mockParagraphSystem)

        then:
        paginator.currentPageParagraphs == null
        paginator.currentPageNumber == null
        paginator.totalPageCount == 2
        paginator.nextPageNumber == null
        paginator.prevPageNumber == null
    }
}
