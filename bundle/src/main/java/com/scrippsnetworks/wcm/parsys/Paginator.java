package com.scrippsnetworks.wcm.parsys;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.SyntheticResource;
import org.apache.sling.api.resource.Resource;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.StringUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

/**
 * Logic for pagination in SNI WCM
 * Relies on the page-break component to determine page boundaries in a ParagraphSystem
 * Also, operates on generic Sling Resource component, currently does not do any type checking
 * to see if the resource is truly a ParagraphSystem.
 * TODO: add type validation for the resource passed into the constructor to make sure it's a ParagraphSystem
 *
 * @author Jason Clark
 * Date: 6/17/12
 * @deprecated see {@link com.scrippsnetworks.wcm.paginator.ParagraphPaginator}
 */
@Deprecated
public class Paginator {

    public static final String SLING_RESOURCE_TYPE_PAGE_BREAK = "sni-wcm/components/util/page-break";
    public static final String PAGE_BREAK = "page-break";

    private List<Integer>  pageBreaks = new ArrayList<Integer>(); //for internal use only
    private List<Resource> paragraphs = new ArrayList<Resource>(); //full list of paragraphs in parsys
    private SlingHttpServletRequest slingRequest; //storing as a field for reuse in formatting tag(s)
    private int pageCount; //total number of pageCount in paginated ParagraphSystem
    private int pageNum; //current page number

    /**
     * Construct a Paginator from an apache Sling Resource
     * "pageCount" are groups of Paragraphs that are separated by a page-break component
     * @param resource Sling Resource for ParagraphSystem you wish to paginate
     * @param request SlingHttpServletRequest for current page
     */
    public Paginator(final Resource resource, final SlingHttpServletRequest request) {
        Validate.notNull(resource);
        Validate.notNull(request);

        final SyntheticResource fakeBreak = new SyntheticResource(resource.getResourceResolver(),
                PAGE_BREAK, SLING_RESOURCE_TYPE_PAGE_BREAK);

        slingRequest = request;

        Iterator<Resource> paragraphItr = resource.listChildren();

        while (paragraphItr.hasNext()) {
            Resource par = paragraphItr.next();
            paragraphs.add(par);
            String parType = par.getResourceType();
            if (StringUtils.isBlank(parType)) {
                continue;
            }
            //find page-break components
            if (PageBreakTypes.isPageBreakResourceType(parType) && paragraphItr.hasNext()) {
                paragraphs.add(fakeBreak);
                pageBreaks.add(paragraphs.size() - 1);
            } else if (parType.equals(SLING_RESOURCE_TYPE_PAGE_BREAK)) {
        		pageBreaks.add(paragraphs.size() -1);
        	}
        }

        if (pageBreaks.size() > 0) {
            pageCount = pageBreaks.size() + 1;
        } else if (pageBreaks.size() == 0 && paragraphs.size() > 0) {
            pageCount = 1;
        } else {
            pageCount = 0;
        }

        pageNum = pageNumberFromSlingRequest(slingRequest);
    }

    /**
     * Loops through selectors from request, if any, finds the pattern page-X and returns X
     * Used in the constructor for Paginator, also can be used in EL context in a JSP
     * @param slingRequest SlingHttpServletRequest for current page
     * @return int page number
     */
    public static int pageNumberFromSlingRequest(final SlingHttpServletRequest slingRequest) {
        int output = 1;
        //check for selectors
        RequestPathInfo pathInfo = slingRequest.getRequestPathInfo();
        Pattern pattern = Pattern.compile("^page-([0-9]+)$");
        for (String selector : pathInfo.getSelectors()) {
            Matcher matcher = pattern.matcher(selector);
            if (matcher.matches()) {
                output = Integer.valueOf(matcher.group(1));
                break;
            }
        }
        return output;
    }

    /**
     * Looks through paragraphs to return the given pageNum's worth of paragraphs
     * to represent that page's content.
     * @param pageNum Integer representing desired page worth of content
     * @return List of paragraphs to render
     */
    public List<Resource> page(final Integer pageNum) {
        if (pageNum == null) {
            return null;
        }
        List<Resource> output = new ArrayList<Resource>();
        int startIndex = 0; //this will be the index of paragraphs to start on
        int endIndex = 0;   //and this is the index to end on
        if (pageBreaks.size() > 0) {
        	//render only the "page" named in the selector
            if (pageNum < 1 || pageNum > pageCount) {
            	//if you got here, then page num was out of bounds
            	return null;
            } else {
            	//time to paginate
            	if (pageNum == 1) {
            		//first page
            		startIndex = 0;
            		endIndex = pageBreaks.get(0) - 1;
            	} else if (pageNum == pageCount) {
            		//last page
           			startIndex = pageBreaks.get(pageBreaks.size() - 1) + 1;
           			endIndex = paragraphs.size() - 1;
            	} else {
            		//somewhere in the middle
            		startIndex = pageBreaks.get(pageNum - 2) + 1;
            		endIndex = pageBreaks.get(pageNum - 1) - 1;
            	}
            }
        } else {
        	//get ALL the things
            startIndex = 0;
        	endIndex = paragraphs.size() - 1;
        }

        //return paragraphs between start & end
        for (int i = startIndex; i <= endIndex; i++) {
            output.add(paragraphs.get(i));
        }
        return output;
    }

    /**
     * Get a List of Resources representing all of the non page-break contents of paginator
     * @return List of Resources
     */
    public List<Resource> getAllPages() {
        if (paragraphs.size() == 0) {
            return null;
        }
        List<Resource> output = new ArrayList<Resource>();
        for (int i = 0; i <= paragraphs.size() - 1; i++) {
            if (pageBreaks.contains(i)) {
                continue;
            }
            output.add(paragraphs.get(i));
        }
        return output;
    }

    public List<Resource> getParagraphs() {
        return paragraphs;
    }
    public int getPageCount() {
        return pageCount;
    }
    public int getPageNum() {
        return pageNum;
    }
    public SlingHttpServletRequest getSlingRequest() {
        return slingRequest;
    }
}
