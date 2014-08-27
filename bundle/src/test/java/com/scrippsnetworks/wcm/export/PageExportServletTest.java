package com.scrippsnetworks.wcm.export;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.sling.api.resource.*;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class PageExportServletTest {

    private static final String ASSET_UID = "4e155ba5-34c1-4947-b77f-70e351875d92";
    private static final String ASSET_UID_PATH = "/id/" + ASSET_UID + ".xml";
    private static final String LEGACY_TUPLE_PATH = "/brand/FOOD/pageType/SHOW/fastfwdId/1234.xml";
    private static final String PAGE_PATH = "/content/food/shows/a/a-show";
    private static final String PAGE_RESOURCE_TYPE = "sni-food/components/pagetypes/show";
    private static final String PAGE_CQ_LAST_REPLICATION_ACTION = "Publish";

    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock ResourceResolverFactory resourceResolverFactory;
    @Mock ResourceResolver resourceResolver;
    @Mock Resource pageResource;
    @Mock Resource pageContentResource;
    @Mock ValueMap pageProperties;
    @Mock Page page;
    @Mock Iterator<Resource> resourceIterator;
    @Mock PageManager pageManager;

    private PageExportServlet pageExportServlet = null;
    private StringWriter stringWriter = null;
    private PrintWriter printWriter = null;

    @Before
    public void before() throws LoginException, IOException {
        MockitoAnnotations.initMocks(this);
        pageExportServlet = new PageExportServlet();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
        when(request.getServletPath()).thenReturn(PageExportServlet.ALIAS);
        pageExportServlet.resourceResolverFactory = resourceResolverFactory;
        when(resourceResolverFactory.getAdministrativeResourceResolver(null)).thenReturn(resourceResolver);
        when(resourceIterator.next()).thenReturn(pageResource);
        when(pageResource.adaptTo(Page.class)).thenReturn(page);
        when(page.hasContent()).thenReturn(true);
        when(page.getProperties()).thenReturn(pageProperties);
        when(page.getDepth()).thenReturn(3);
        when(page.getPath()).thenReturn(PAGE_PATH);
        when(page.getPageManager()).thenReturn(pageManager);
        when(page.getContentResource()).thenReturn(pageContentResource);
        when(pageContentResource.getResourceResolver()).thenReturn(resourceResolver);
        when(pageContentResource.getResourceType()).thenReturn(PAGE_RESOURCE_TYPE);

        // This ensures there's at least one property in the result.
        when(pageProperties.get(NameConstants.PN_PAGE_LAST_REPLICATION_ACTION, String.class)).thenReturn(PAGE_CQ_LAST_REPLICATION_ACTION);
    }

    @Test
    public void testAssetUidPathSuccess() throws ServletException, IOException, XPathExpressionException {
        // Set up valid request
        when(request.getPathInfo()).thenReturn(ASSET_UID_PATH);
        when(resourceResolver.findResources(anyString(), anyString())).thenReturn(resourceIterator);
        when(resourceIterator.hasNext()).thenReturn(true);
        when(resourceIterator.next()).thenReturn(pageResource);
        when(pageResource.adaptTo(Page.class)).thenReturn(page);

        pageExportServlet.doGet(request, response);

        // validate an error code wasn't set and the response was written
        verify(response, never()).sendError(HttpServletResponse.SC_NOT_FOUND);

        // Don't know how the pri
        assertNotNull("response string nonnull", stringWriter.toString());
        assertTrue("response body nonempty", !stringWriter.toString().isEmpty());
        String path = "/RECORDS/RECORD/PROP"; // I don't really like this class knowing the schema
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xpath.evaluate(path, new InputSource(new StringReader(stringWriter.toString())), XPathConstants.NODESET);
        assertTrue("xml contains at least one property", nodeList.getLength() > 0);
    }

    @Test
    public void testLegacyPathRedirectSuccess() throws ServletException, IOException {
        // Set up valid legacy request
        when(request.getPathInfo()).thenReturn(LEGACY_TUPLE_PATH);
        when(resourceResolver.findResources(anyString(), anyString())).thenReturn(resourceIterator);
        when(resourceIterator.hasNext()).thenReturn(true);
        when(resourceIterator.next()).thenReturn(pageContentResource);
        when(pageContentResource.adaptTo(ValueMap.class)).thenReturn(pageProperties);
        when(pageProperties.get("sni:assetUId", String.class)).thenReturn(ASSET_UID);

        pageExportServlet.doGet(request, response);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(captor.capture());
        assertEquals("redirect is to asset path", PageExportServlet.ALIAS + ASSET_UID_PATH, captor.getValue());
    }

    @Test
    public void notFoundAssetUid() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn(ASSET_UID_PATH);
        when(resourceResolver.findResources(anyString(), anyString())).thenReturn(resourceIterator);
        when(resourceIterator.hasNext()).thenReturn(false);

        pageExportServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void notFoundLegacyTuple() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn(LEGACY_TUPLE_PATH);
        when(resourceResolver.findResources(anyString(), anyString())).thenReturn(resourceIterator);
        when(resourceIterator.hasNext()).thenReturn(false);

        pageExportServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

}
