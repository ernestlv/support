package com.scrippsnetworks.wcm.fnr.servlets;

import com.scrippsnetworks.wcm.url.UrlMapper;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.settings.SlingSettingsService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.jcr.query.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/** Test for the LegacyRecipeLinkServlet.
 *
 */
public class LegacyRecipeLinkServletTest {
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    ResourceResolverFactory resourceResolverFactory;
    @Mock
    SlingSettingsService slingSettings;
    @Mock
    ResourceResolver resourceResolver;
    @Mock
    Resource pageResource;
    @Mock
    Resource pageContentResource;
    @Mock
    PrintWriter printWriter;
    @Mock
    UrlMapper urlMapper;

    public static final String VALID_RECIPE_ID = "12345";
    public static final String PATH_INFO = "/0,," + VALID_RECIPE_ID + ",00.html";
    public static final String VALID_PAGE_PATH = "/content/food/recipes/alton-brown/a/aa/aaa/aaaa/aaaa-recipe";
    public static final String FRIENDLY_PAGE_URL = "http://www.foodnetwork.com/recipes/aaaa-recipe.html";
    public static final String MALFORMED_PATH_INFO = "/0,,,00.html";

    private LegacyRecipeLinkServlet linkServlet;

    @Before
    public void before() throws LoginException, IOException {
        MockitoAnnotations.initMocks(this);

        Set<String> runModeSet = new HashSet<String>();
        runModeSet.add(LegacyRecipeLinkServlet.PUBLISH_RUN_MODE);



        when(request.getPathInfo()).thenReturn(PATH_INFO);
        when(response.getWriter()).thenReturn(printWriter);
        when(resourceResolverFactory.getAdministrativeResourceResolver(null)).thenReturn(resourceResolver);
        when(slingSettings.getRunModes()).thenReturn(runModeSet);

        when(pageContentResource.getParent()).thenReturn(pageResource);
        when(pageResource.getPath()).thenReturn(VALID_PAGE_PATH);
        when(urlMapper.map(resourceResolver, null, VALID_PAGE_PATH)).thenReturn(FRIENDLY_PAGE_URL);

        linkServlet = new LegacyRecipeLinkServlet();
        linkServlet.resourceResolverFactory = resourceResolverFactory;
        linkServlet.urlMapper = urlMapper;
        linkServlet.slingSettings = slingSettings;
    }

    @Test
    public void testQuery() throws ServletException, IOException {
        linkServlet.doGet(request, response);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(resourceResolver).findResources(captor.capture(), eq(Query.JCR_SQL2));
        assertTrue("query constrained to root",
                captor.getValue().contains("ISDESCENDANTNODE(pageContent, \"" + LegacyRecipeLinkServlet.RECIPES_ROOT + "\")"));
        assertTrue("query constrained to requested id",
                captor.getValue().contains("pageContent.[sni:fastfwdId] = " + VALID_RECIPE_ID + ".0"));
        assertTrue("query constrained to page type",
                captor.getValue().contains("pageContent.[sni:assetType] = \"" + LegacyRecipeLinkServlet.PAGE_TYPE + "\""));
    }

    @Test
    public void testResponse() throws ServletException, IOException {
        // When the requested resource exists
        ArrayList<Resource> resourceList = new ArrayList<Resource>();
        resourceList.add(pageContentResource);
        when(resourceResolver.findResources(anyString(), anyString())).thenReturn(resourceList.iterator());

        linkServlet.doGet(request, response);

        // return a document containing a link to the recipe
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(printWriter).print(captor.capture());
        assertTrue("response nonempty", !captor.getValue().isEmpty());
        Document doc = Jsoup.parseBodyFragment(captor.getValue());
        Elements elements = doc.getElementsByTag("a");
        assertTrue("document has one anchor", elements.size() == 1);
        Element element = elements.first();
        Attributes attrs = element.attributes();
        String href = attrs.get("href");
        assertTrue("href is correct", FRIENDLY_PAGE_URL.equals(href));
    }

    @Test
    public void testInvalidRequest() throws ServletException, IOException {
        // When given a malformed path that cannot be turned into a query...
        when(request.getPathInfo()).thenReturn(MALFORMED_PATH_INFO);

        linkServlet.doGet(request, response);

        // return a 404 HTTP code
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testResourceNotFound() throws ServletException, IOException {
        // When a resource is not returned
        ArrayList<Resource> resourceList = new ArrayList<Resource>();
        when(resourceResolver.findResources(anyString(), anyString())).thenReturn(resourceList.iterator());

        linkServlet.doGet(request, response);

        // return a 200 response code
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(printWriter, never()).print(anyString());
    }
}
