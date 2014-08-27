package com.scrippsnetworks.wcm.contentfinder;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.naming.NamingException;
import javax.servlet.ServletException;

import junit.framework.Assert;

import org.apache.commons.collections.Predicate;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.commons.testing.jcr.MockNode;
import org.apache.sling.commons.testing.sling.MockResourceResolver;
import org.apache.sling.commons.testing.sling.MockSlingHttpServletRequest;
import org.apache.sling.commons.testing.sling.MockSlingHttpServletResponse;
import org.junit.Before;
import org.junit.Test;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.impl.result.EmptySearchResult;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.WCMMode;
import com.scrippsnetworks.http.MockSniSlingHttpSerletRequest;
import com.scrippsnetworks.http.MockStatusExposingSlingServletResponse;

public class ContentFinderPageQueryServletTest {

    private static final ContentFinderPageQueryServlet servlet = new ContentFinderPageQueryServlet();
    private MockStatusExposingSlingServletResponse response;
    private MockResourceResolver mockResourceResolver = mock(MockResourceResolver.class);
    private Session mockSession = mock(Session.class);
    private QueryBuilder mockQueryBuilder = mock(QueryBuilder.class);
    
    @Before
    public void setup() {
        response = new MockStatusExposingSlingServletResponse(mock(SlingHttpServletResponse.class));
    }
    
    @Test
    public void nonAuthorModeHasEmptyReturn() throws ServletException, IOException {
        MockSlingHttpServletRequest req = new MockSlingHttpServletRequest("", "", "", "", "");
        servlet.doGet(req, response, mock(Predicate.class));
    }
    
    @Test
    public void nonJSONRequestShouldEmptyReturn() throws ServletException, IOException {
        MockSlingHttpServletRequest req = new MockSniSlingHttpSerletRequest("", "", "html", "", "");
        req.setAttribute(WCMMode.class.getName(), WCMMode.EDIT);
        servlet.doGet(req, response, mock(Predicate.class));
        Assert.assertEquals(0, response.getOutput().length());
    }
    
    @Test
    public void blankRequestShouldEmptyReturn() throws ServletException, IOException, RepositoryException, NamingException {
        MockSlingHttpServletRequest req = new MockSniSlingHttpSerletRequest("", "", "json", "", "");
        req.setAttribute(WCMMode.class.getName(), WCMMode.EDIT);
        req.setResourceResolver(getMockResourceResolver(Collections.<Node>emptyList().iterator()));
        servlet.doGet(req, response, mock(Predicate.class));
        Assert.assertEquals("application/json", response.getContentType());
        MockSlingHttpServletResponse resp = new MockSlingHttpServletResponse();
        servlet.writeHits(new EmptySearchResult(0), resp);
        Assert.assertEquals(resp.getOutput().length(), response.getOutput().length());
    }
    
    
    @Test
    public void equestShouldEmptyReturn() throws ServletException, IOException, RepositoryException, NamingException {
        MockSlingHttpServletRequest req = new MockSniSlingHttpSerletRequest("", "", "json", "", ContentFinderPageQueryServlet.OPTION + "=true&" + ContentFinderPageQueryServlet.QUERY + "=testTitle");
        req.setAttribute(WCMMode.class.getName(), WCMMode.EDIT);
        List<Node> nodes = new ArrayList<Node>();
        Node mock = new MockNode("/content/food/modules");
        mock.setProperty("jcr:content/jcr:title", "SAMPLE_TITLE");
        mock.setProperty("jcr:content/cq:lastModified", Calendar.getInstance());
        nodes.add(mock);
        req.setResourceResolver(getMockResourceResolver(nodes.iterator()));
        servlet.doGet(req, response, mock(Predicate.class));
        Assert.assertEquals("application/json", response.getContentType());
    }
    
    private MockResourceResolver getMockResourceResolver(Iterator<Node> nodeIterator) throws RepositoryException, NamingException {
        given(mockResourceResolver.adaptTo(Session.class)).willReturn(mockSession);
        given(mockResourceResolver.adaptTo(QueryBuilder.class)).willReturn(mockQueryBuilder);
        Query mockQuery = mock(Query.class);
        SearchResult mockSearchResult = mock(SearchResult.class);
        given(mockQuery.getResult()).willReturn(mockSearchResult);
        given(mockSearchResult.getNodes()).willReturn(nodeIterator);
        given(mockQueryBuilder.createQuery(org.mockito.Matchers.any(PredicateGroup.class), org.mockito.Matchers.any(Session.class))).willReturn(mockQuery);
        return mockResourceResolver;
    }
    

}
