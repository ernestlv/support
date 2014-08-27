package com.scrippsnetworks.wcm.paginator.impl;

import com.day.cq.wcm.foundation.Paragraph;
import com.day.cq.wcm.foundation.ParagraphSystem;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.paginator.ParagraphPaginator;
import com.scrippsnetworks.wcm.util.PageTypes;
import com.scrippsnetworks.wcm.util.SniSlingUtilService;
import org.apache.sling.api.resource.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Constructed with an SniPage, ParagraphPaginator is aware of 2 kinds of paragraph systems
 * 1. the kind where page breaks are represended by ParagraphPaginator.RC_TYPE_PAGE_BREAK
 * 2. the kind where page breaks themselves are a page ParagraphPaginator.RC_TYPE_PAGE_BREAK_EPONYMOUS_UNIT
 *
 * This implementation locates the pagination-driving parsys by the path found in ParagraphPaginator.PROPERTY_SNI_PAGINATED_PARSYS
 *
 * @author Jason Clark
 *         Date: 4/28/13
 * updated Ken Shih 7/5/13 refactored PaginatorImpl into ParagraphPaginator
 *
 */
public class ParagraphPaginatorImpl implements ParagraphPaginator {

    private SniPage sniPage;
    private PageTypes pageType;
    private List<List<Paragraph>> cachedParagraphsGroupedByPage = null;
    SniSlingUtilService sniSlingUtilService;

    public ParagraphPaginatorImpl(SniPage page, SniSlingUtilService sniSlingUtilService) {
        this.sniPage = page;
        this.pageType = PageTypes.findPageType(page.getPageType());
        this.sniSlingUtilService = sniSlingUtilService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getPrevPageNumber() {
        Integer currentPageNumber = getCurrentPageNumber();
        if(currentPageNumber != null
                && currentPageNumber>1
                && currentPageNumber <= getTotalPageCount())
            return currentPageNumber-1;
        else
            return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getNextPageNumber() {
        Integer currentPageNumber = getCurrentPageNumber();
        if(currentPageNumber!=null && currentPageNumber<getTotalPageCount())
            return currentPageNumber+1;
        else
            return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getCurrentPageNumber(){
        Integer deepLinkPageNumber = sniPage.getDeepLinkPageNumber();
        Integer total = getTotalPageCount();
        return deepLinkPageNumber!=null && total!=null?
                (total >= deepLinkPageNumber && total>0?deepLinkPageNumber:null)
                        : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getTotalPageCount(){
        cacheParagraphs();
        if(cachedParagraphsGroupedByPage!=null){
            int totalPageCount = cachedParagraphsGroupedByPage.size();
            //Compensate for the end slide.
            if (pageType == PageTypes.PHOTOGALLERY) {
                totalPageCount++;
            }
            return totalPageCount;
        }
        return null;
    }

    @Override
    public List<Paragraph> getCurrentPageParagraphs() {
        //this answers: what page am i on?
        int pageNumber = sniPage.getDeepLinkPageNumber();

        //make sure paragraphs are cached
        cacheParagraphs();

        //this answers: do i have a meaningful page number to show? and do I have it?
        if(pageNumber>0 && cachedParagraphsGroupedByPage !=null &&
                cachedParagraphsGroupedByPage.size()>=pageNumber){
            return cachedParagraphsGroupedByPage.get(pageNumber - 1);
        }
        return null;
    }

    /**
     * The main algorithm of splitting parsys into pages/content is done in this method
     * the result is cached for the life of this object (e.g. which is typically 1 http request)
     *
     * @return a nested list, the outer-level/dimension represents the page,
     *  the inner-level represents the content in the page
     */
    private List<List<Paragraph>> cacheParagraphs(){
        if(cachedParagraphsGroupedByPage != null) {
            return cachedParagraphsGroupedByPage;
        }

        List<Paragraph> paragraphs = getAllParagraphs();
        if(paragraphs==null) return null;

        List<List<Paragraph>> listList = new ArrayList<List<Paragraph>>();
        List<Paragraph> list = new ArrayList<Paragraph>();
        for(Paragraph paragraph : paragraphs){
            if(paragraph.getResourceType() != null && paragraph.getResourceType().endsWith(RC_TYPE_PAGE_BREAK)){
                if(list.size()>0){
                    listList.add(list);
                }
                list = new ArrayList<Paragraph>();
            } else if(sniSlingUtilService.isPropertyOnResourceOrAncestor(
                    paragraph.getPath(),
                    ParagraphPaginator.PROPERTY_SNI_PAGINATED_PARSYS_PAGE,
                    ParagraphPaginator.VALUE_STANDALONE_SLIDE)
                    ) {
                if(list.size()>0){
                    listList.add(list);
                }
                list = new ArrayList<Paragraph>();
                list.add(paragraph);
                listList.add(list);
                list = new ArrayList<Paragraph>();
            } else {
                list.add(paragraph);
            }
        }
        if(list.size()>0){
            listList.add(list);
        }
        //cache it
        cachedParagraphsGroupedByPage=listList;
        return listList;
    }

    private List<Paragraph> getAllParagraphs() {
        if(sniPage == null || sniPage.getProperties()==null) return null;
        if(!sniPage.getProperties().containsKey(PROPERTY_SNI_PAGINATED_PARSYS)) return null;
        String relParsysPath = (String) sniPage.getProperties().get(PROPERTY_SNI_PAGINATED_PARSYS);

        if(relParsysPath==null || relParsysPath.isEmpty()) return null;
        if(sniPage.getContentResource(relParsysPath)==null) return null;
        Resource rc = sniPage.getContentResource(relParsysPath);

        if(!rc.isResourceType(RC_TYPE_PARSYS)) return null;
        ParagraphSystem system = paragraphSystem==null?new ParagraphSystem(rc):paragraphSystem;

        //TODO not taking defensive copy. should i?
        return system.paragraphs();
    }

    //injectable for utest only
    private ParagraphSystem paragraphSystem;

    protected void setParagraphSystem(ParagraphSystem paragraphSystem){
        this.paragraphSystem = paragraphSystem;
    }

}
