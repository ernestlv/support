package com.scrippsnetworks.wcm.analytics;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.analytics.sitecatalyst.Framework;
import com.day.cq.analytics.sitecatalyst.FrameworkComponent;
import com.day.cq.wcm.webservicesupport.Configuration;
import com.day.cq.wcm.webservicesupport.ConfigurationConstants;
import com.day.cq.wcm.webservicesupport.ConfigurationManager;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import org.apache.jackrabbit.JcrConstants;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AnalyticsAnnotationFilterTest {

    private static final String REQUEST_RESOURCE_PATH = "/content/food/shows/a/a-show/jcr:content/content-well/a-module";
    private static final String PAGE_RESOURCE_TYPE = "sni-food/components/pagetypes/show";
    private static final String SITECATALYST_CONFIG_PATH = "/etc/cloudservices/sitecatalyst/GlobalSiteCatalystTracking/GlobalFramework/food-category/food-network";

    private static final String FC_1_RESOURCE_TYPE = "sni-core/components/modules/module1";
    private static final String FC_2_RESOURCE_TYPE = "sni-core/components/modules/module2";
    private static final String FC_3_RESOURCE_TYPE = "sni-food/components/modules/module1";

    @Mock SlingHttpServletRequest slingRequest;
    @Mock SlingHttpServletResponse slingResponse;
    @Mock FilterChain filterChain;
    @Mock RequestPathInfo requestPathInfo;
    @Mock Page page;
    @Mock Resource pageCR;
    @Mock ValueMap pageProps;
    @Mock ConfigurationManager configurationManager;
    @Mock Configuration configuration;
    @Mock Resource configurationResource;
    @Mock Resource configurationCR;
    @Mock ResourceResolver resourceResolver;
    @Mock Framework framework;
    @Mock FrameworkComponent frameworkComponent1;
    @Mock FrameworkComponent frameworkComponent2;
    @Mock FrameworkComponent frameworkComponent3;
    @Mock Resource requestResource;

    @Mock ComponentContext componentContext;
    @Mock Component contextComponent;
    @Mock Component superComponent;
    @Mock ValueMap contextComponentProps;
    @Mock Resource contextComponentResource;

    private final String[] services = Arrays.asList(SITECATALYST_CONFIG_PATH).toArray(new String[1]);

    private StringWriter stringWriter = null;
    private AnalyticsAnnotationFilter filter = null;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        when(componentContext.hasDecoration()).thenReturn(true);
        when(slingRequest.getAttribute(WCMMode.REQUEST_ATTRIBUTE_NAME)).thenReturn(WCMMode.DISABLED); // mame fromRequest() work
        when(slingRequest.getRequestPathInfo()).thenReturn(requestPathInfo);
        when(requestPathInfo.getResourcePath()).thenReturn(REQUEST_RESOURCE_PATH);
        when(slingRequest.getAttribute(ComponentContext.CONTEXT_ATTR_NAME)).thenReturn(componentContext);
        when(componentContext.isRoot()).thenReturn(false);
        when(componentContext.getPage()).thenReturn(page);
        when(page.hasContent()).thenReturn(true);
        when(page.getContentResource()).thenReturn(pageCR);
        when(pageCR.getResourceType()).thenReturn(PAGE_RESOURCE_TYPE);
        when(pageCR.adaptTo(ValueMap.class)).thenReturn(pageProps);
        when(pageProps.get(ConfigurationConstants.PN_CONFIGURATIONS, String[].class))
                .thenReturn(services);
        when(configurationManager.getConfiguration(AnalyticsAnnotationFilter.SITECATALYST_SERVICE_NAME, services))
                .thenReturn(configuration);
        when(configuration.getPath()).thenReturn(SITECATALYST_CONFIG_PATH);
        when(configuration.getResource()).thenReturn(configurationResource);
        when(configurationResource.getResourceResolver()).thenReturn(resourceResolver);
        when(configurationResource.getChild(JcrConstants.JCR_CONTENT)).thenReturn(configurationCR);
        when(configurationCR.adaptTo(Framework.class)).thenReturn(framework);

        Map<String, FrameworkComponent> allComponents = new HashMap<String, FrameworkComponent>();
        when(frameworkComponent1.getType()).thenReturn(FC_1_RESOURCE_TYPE);
        when(frameworkComponent2.getType()).thenReturn(FC_2_RESOURCE_TYPE);
        when(frameworkComponent3.getType()).thenReturn(FC_3_RESOURCE_TYPE);
        allComponents.put(frameworkComponent1.getType(), frameworkComponent1);
        allComponents.put(frameworkComponent2.getType(), frameworkComponent2);

        when(framework.getAllComponents()).thenReturn(allComponents);

        when(slingRequest.getResource()).thenReturn(requestResource);
        when(componentContext.getResource()).thenReturn(requestResource);
        when(requestResource.getPath()).thenReturn(REQUEST_RESOURCE_PATH);
        when(requestResource.getResourceType()).thenReturn(FC_1_RESOURCE_TYPE);

        when(componentContext.getComponent()).thenReturn(contextComponent);
        when(contextComponent.getProperties()).thenReturn(contextComponentProps);
        when(contextComponentProps.get(AnalyticsAnnotationFilter.TRACKEVENTS_PATH, String.class))
                .thenReturn(AnalyticsAnnotationFilter.Events.toArray(new String[AnalyticsAnnotationFilter.Events.size()])[0]);

        when(contextComponent.getResourceType()).thenReturn(FC_1_RESOURCE_TYPE);
        when(contextComponent.getPath()).thenReturn("/apps/" + FC_1_RESOURCE_TYPE);

        stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        try {
            when(slingResponse.getWriter()).thenReturn(printWriter);
        } catch (IOException e) {
            throw new RuntimeException("this won't happen", e);
        }

        filter = new AnalyticsAnnotationFilter();
        filter.configurationManager = configurationManager;
    }

    @Test
    public void testNonHierarchicalHappyPath() throws IOException, ServletException {
        // The before() method sets up all the prerequisites for a successful request.
        filter.doFilter(slingRequest, slingResponse, filterChain);
        Document doc = Jsoup.parseBodyFragment(stringWriter.toString());
        Element spanEl = doc.select(AnalyticsAnnotationFilter.ANNOTATION_TAG_NAME).first();

        // These two should be true of any successful annotation.
        assertNotNull("annotation tag output", spanEl);
        assertEquals("annotation tag has class", spanEl.className(), AnalyticsAnnotationFilter.ANNOTATION_CLASS);

        // In the scenario set up in before(), the component path and resource type are the same, i.e., the
        // component is tracked by its own resource type and not by resource supertype.
        assertEquals("annotation component path", FC_1_RESOURCE_TYPE,
                spanEl.attr(AnalyticsAnnotationFilter.DataAttributes.componentPath.getAttrName()));
        assertEquals("annotation resource path", REQUEST_RESOURCE_PATH,
                spanEl.attr(AnalyticsAnnotationFilter.DataAttributes.resourcePath.getAttrName()));
        assertEquals("annotation resource type", FC_1_RESOURCE_TYPE,
                spanEl.attr(AnalyticsAnnotationFilter.DataAttributes.resourceType.getAttrName()));

        verify(filterChain).doFilter(slingRequest, slingResponse);
    }

    @Test
    public void testHierarchicalHappyPath() throws IOException, ServletException {
        // This time, the resource's type is not in the framework.
        // Instead, the resource's super type is.
        when(requestResource.getResourceType()).thenReturn(FC_3_RESOURCE_TYPE);
        when(contextComponent.getSuperComponent()).thenReturn(superComponent);
        when(superComponent.getResourceType()).thenReturn(FC_1_RESOURCE_TYPE);

        filter.doFilter(slingRequest, slingResponse, filterChain);
        Document doc = Jsoup.parseBodyFragment(stringWriter.toString());
        Element spanEl = doc.select(AnalyticsAnnotationFilter.ANNOTATION_TAG_NAME).first();

        // These two should be true of any successful annotation.
        assertNotNull("annotation tag output", spanEl);
        assertEquals("annotation tag has class", spanEl.className(), AnalyticsAnnotationFilter.ANNOTATION_CLASS);

        // In the scenario set up in before(), the component path and resource type are the same, i.e., the
        // component is tracked by its own resource type and not by resource supertype.
        assertEquals("annotation component path", FC_1_RESOURCE_TYPE,
                spanEl.attr(AnalyticsAnnotationFilter.DataAttributes.componentPath.getAttrName()));
        assertEquals("annotation resource path", REQUEST_RESOURCE_PATH,
                spanEl.attr(AnalyticsAnnotationFilter.DataAttributes.resourcePath.getAttrName()));
        assertEquals("annotation resource type", FC_3_RESOURCE_TYPE,
                spanEl.attr(AnalyticsAnnotationFilter.DataAttributes.resourceType.getAttrName()));

        verify(filterChain).doFilter(slingRequest, slingResponse);
    }

    @Test
    public void testEditMode() throws IOException, ServletException {
        when(slingRequest.getAttribute(WCMMode.REQUEST_ATTRIBUTE_NAME)).thenReturn(WCMMode.EDIT);

        filter.doFilter(slingRequest, slingResponse, filterChain);
        Document doc = Jsoup.parseBodyFragment(stringWriter.toString());
        Element spanEl = doc.select(AnalyticsAnnotationFilter.ANNOTATION_TAG_NAME).first();

        assertNull("no annotation tag in edit mode", spanEl);
        verify(filterChain).doFilter(slingRequest, slingResponse);
    }

    @Test
    public void testNoDecoration() throws IOException, ServletException {
        when(componentContext.hasDecoration()).thenReturn(false);

        filter.doFilter(slingRequest, slingResponse, filterChain);
        Document doc = Jsoup.parseBodyFragment(stringWriter.toString());
        Element spanEl = doc.select(AnalyticsAnnotationFilter.ANNOTATION_TAG_NAME).first();

        assertNull("no annotation tag in edit mode", spanEl);
        verify(filterChain).doFilter(slingRequest, slingResponse);
    }

    @Test
    public void testNoDecorationTagName() throws IOException, ServletException {
        when(componentContext.getDecorationTagName()).thenReturn("");

        filter.doFilter(slingRequest, slingResponse, filterChain);
        Document doc = Jsoup.parseBodyFragment(stringWriter.toString());
        Element spanEl = doc.select(AnalyticsAnnotationFilter.ANNOTATION_TAG_NAME).first();

        assertNull("no annotation tag in edit mode", spanEl);
        verify(filterChain).doFilter(slingRequest, slingResponse);
    }

    @Test
    public void testBypass() throws IOException, ServletException {
        // The exact value doesn't matter, just that the attribute is set.
        when(slingRequest.getAttribute(ComponentContext.BYPASS_COMPONENT_HANDLING_ON_INCLUDE_ATTRIBUTE))
                .thenReturn(ComponentContext.BYPASS_COMPONENT_HANDLING_ON_INCLUDE_ATTRIBUTE);

        filter.doFilter(slingRequest, slingResponse, filterChain);
        Document doc = Jsoup.parseBodyFragment(stringWriter.toString());
        Element spanEl = doc.select(AnalyticsAnnotationFilter.ANNOTATION_TAG_NAME).first();

        assertNull("no annotation tag when bypass in request", spanEl);
        verify(filterChain).doFilter(slingRequest, slingResponse);
    }

    @Test
    public void testOutsideRoot() throws IOException, ServletException {
        when(requestPathInfo.getResourcePath()).thenReturn("/etc/whatever/foobar");

        filter.doFilter(slingRequest, slingResponse, filterChain);
        Document doc = Jsoup.parseBodyFragment(stringWriter.toString());
        Element spanEl = doc.select(AnalyticsAnnotationFilter.ANNOTATION_TAG_NAME).first();

        assertNull("no annotation tag when request is outside root", spanEl);
        verify(filterChain).doFilter(slingRequest, slingResponse);
    }

    @Test
    public void testNullComponentContext() throws IOException, ServletException {
        when(slingRequest.getAttribute(ComponentContext.CONTEXT_ATTR_NAME))
                .thenReturn(null);

        filter.doFilter(slingRequest, slingResponse, filterChain);
        Document doc = Jsoup.parseBodyFragment(stringWriter.toString());
        Element spanEl = doc.select(AnalyticsAnnotationFilter.ANNOTATION_TAG_NAME).first();

        assertNull("no annotation tag when context is null", spanEl);
        verify(filterChain).doFilter(slingRequest, slingResponse);
    }

    @Test
    public void testRootComponentContext() throws IOException, ServletException {
        when(componentContext.isRoot()).thenReturn(true);

        filter.doFilter(slingRequest, slingResponse, filterChain);
        Document doc = Jsoup.parseBodyFragment(stringWriter.toString());
        Element spanEl = doc.select(AnalyticsAnnotationFilter.ANNOTATION_TAG_NAME).first();

        assertNull("no annotation tag when context is root context", spanEl);
        verify(filterChain).doFilter(slingRequest, slingResponse);
    }

    @Test
    public void testNullPageFromContext() throws IOException, ServletException {
        when(componentContext.getPage()).thenReturn(null);

        filter.doFilter(slingRequest, slingResponse, filterChain);
        Document doc = Jsoup.parseBodyFragment(stringWriter.toString());
        Element spanEl = doc.select(AnalyticsAnnotationFilter.ANNOTATION_TAG_NAME).first();

        assertNull("no annotation tag when context is root context", spanEl);
        verify(filterChain).doFilter(slingRequest, slingResponse);
    }

    @Test
    public void testPageHasNoContent() throws IOException, ServletException {
        // I don't see how this could be possible, but what the heck.
        when(page.hasContent()).thenReturn(false);

        filter.doFilter(slingRequest, slingResponse, filterChain);
        Document doc = Jsoup.parseBodyFragment(stringWriter.toString());
        Element spanEl = doc.select(AnalyticsAnnotationFilter.ANNOTATION_TAG_NAME).first();

        assertNull("no annotation tag when page has no content", spanEl);
        verify(filterChain).doFilter(slingRequest, slingResponse);
    }

    @Test
    public void testResourceContextIsPageContext() throws IOException, ServletException {
        when(contextComponent.getResourceType()).thenReturn(PAGE_RESOURCE_TYPE);

        filter.doFilter(slingRequest, slingResponse, filterChain);
        Document doc = Jsoup.parseBodyFragment(stringWriter.toString());
        Element spanEl = doc.select(AnalyticsAnnotationFilter.ANNOTATION_TAG_NAME).first();

        assertNull("no annotation tag when request resource type is page resource type", spanEl);
        verify(filterChain).doFilter(slingRequest, slingResponse);
    }

    @Test
    public void testNoRequestResource() throws IOException, ServletException {
        when(slingRequest.getResource()).thenReturn(null);

        filter.doFilter(slingRequest, slingResponse, filterChain);
        Document doc = Jsoup.parseBodyFragment(stringWriter.toString());
        Element spanEl = doc.select(AnalyticsAnnotationFilter.ANNOTATION_TAG_NAME).first();

        assertNull("no annotation tag when request resource is null", spanEl);
        verify(filterChain).doFilter(slingRequest, slingResponse);
    }

    @Test
    public void testNoContextResource() throws IOException, ServletException {
        when(componentContext.getResource()).thenReturn(null);

        filter.doFilter(slingRequest, slingResponse, filterChain);
        Document doc = Jsoup.parseBodyFragment(stringWriter.toString());
        Element spanEl = doc.select(AnalyticsAnnotationFilter.ANNOTATION_TAG_NAME).first();

        assertNull("no annotation tag when component context resource is null", spanEl);
        verify(filterChain).doFilter(slingRequest, slingResponse);
    }

    @Test
    public void testComponentResourceNotRequestResource() throws IOException, ServletException {
        when(componentContext.getResource()).thenReturn(contextComponentResource);
        when(contextComponentResource.getPath()).thenReturn("/content/a/different/path");

        filter.doFilter(slingRequest, slingResponse, filterChain);
        Document doc = Jsoup.parseBodyFragment(stringWriter.toString());
        Element spanEl = doc.select(AnalyticsAnnotationFilter.ANNOTATION_TAG_NAME).first();

        assertNull("no annotation tag when component context resource is not request resource", spanEl);
        verify(filterChain).doFilter(slingRequest, slingResponse);
    }

    @Test
    public void testNoAnalyticsConfiguration() throws IOException, ServletException {
        when(pageProps.get(ConfigurationConstants.PN_CONFIGURATIONS, String[].class))
                .thenReturn(new String[0]);

        filter.doFilter(slingRequest, slingResponse, filterChain);
        Document doc = Jsoup.parseBodyFragment(stringWriter.toString());
        Element spanEl = doc.select(AnalyticsAnnotationFilter.ANNOTATION_TAG_NAME).first();

        assertNull("no annotation tag when component context resource is not request resource", spanEl);
        verify(filterChain).doFilter(slingRequest, slingResponse);
    }

    @Test
    public void testNoFramework() throws IOException, ServletException {
        when(configurationCR.adaptTo(Framework.class)).thenReturn(null);

        filter.doFilter(slingRequest, slingResponse, filterChain);
        Document doc = Jsoup.parseBodyFragment(stringWriter.toString());
        Element spanEl = doc.select(AnalyticsAnnotationFilter.ANNOTATION_TAG_NAME).first();

        assertNull("no annotation tag when framework not available", spanEl);
        verify(filterChain).doFilter(slingRequest, slingResponse);
    }

    @Test
    public void testNoFrameworkComponents() throws IOException, ServletException {
        when(framework.getAllComponents()).thenReturn(Collections.EMPTY_MAP);

        filter.doFilter(slingRequest, slingResponse, filterChain);
        Document doc = Jsoup.parseBodyFragment(stringWriter.toString());
        Element spanEl = doc.select(AnalyticsAnnotationFilter.ANNOTATION_TAG_NAME).first();

        assertNull("no annotation tag when no framework components", spanEl);
        verify(filterChain).doFilter(slingRequest, slingResponse);
    }

    @Test
    public void testComponentNotInFrameworkComponents() throws IOException, ServletException {
        when(contextComponent.getResourceType()).thenReturn("this/component/is/not/tracked");

        filter.doFilter(slingRequest, slingResponse, filterChain);
        Document doc = Jsoup.parseBodyFragment(stringWriter.toString());
        Element spanEl = doc.select(AnalyticsAnnotationFilter.ANNOTATION_TAG_NAME).first();

        assertNull("no annotation tag when component tracked", spanEl);
        verify(filterChain).doFilter(slingRequest, slingResponse);
    }

    @Test
    public void testComponentDoesNotTrackClickEvent() throws IOException, ServletException {
        when(contextComponentProps.get(AnalyticsAnnotationFilter.TRACKEVENTS_PATH, String.class))
                .thenReturn("notClick");

        filter.doFilter(slingRequest, slingResponse, filterChain);
        Document doc = Jsoup.parseBodyFragment(stringWriter.toString());
        Element spanEl = doc.select(AnalyticsAnnotationFilter.ANNOTATION_TAG_NAME).first();

        assertNull("no annotation tag when component does not track click events", spanEl);
        verify(filterChain).doFilter(slingRequest, slingResponse);
    }



}
