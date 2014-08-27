package com.scrippsnetworks.wcm.page.impl;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.mockito.Mock;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Template;
import com.scrippsnetworks.wcm.AbstractSniMockFactory;
import com.scrippsnetworks.wcm.MockAdaptable;
import com.scrippsnetworks.wcm.fnr.util.PageSlingResourceTypes;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.resource.MockResource.MockResourceFactory;
import com.scrippsnetworks.wcm.util.PagePropertyNames;

/**
 * Utility class for setting up testing of a {@link com.day.cq.wcm.api.Page Page}.
 * Users should not instantiate this class directly, but instead use the factory 
 * {@link com.scrippsnetworks.wcm.page.impl.MockPage.MockPageFactory MockPageFactory}. 
 * <p>
 * This class will return a mock {@link com.day.cq.wcm.api.Page Page}, which defaults to returning the following:
 * <ul><li>A Mocked PageManager - this will default to returning the Page we are mocking, when utilizing the same path
 * as we pass into the {@code com.scrippsnetworks.wcm.page.impl.MockPage.MockPageFactory MockPageFactory}. It will 
 * also default to returning any pages which this Page needs in order to complete it's testing. These can be other pages
 * which have been mocked as well.</li>
 * <li>Properties will be exposed via the {@link org.apache.sling.api.resource.ValueMap ValueMap} API. These will be
 * used for the mock {@link org.apache.sling.api.resource.Resource Resource} which is also mocked upon creation of the 
 * object. The caller should utilize the {@link com.scrippsnetworks.wcm.resource.MockResrouceFactory MockResourceFactory} to
 * setup required to setup calls to getContentResource(String) that the caller needs for testing.
 * By default, getContentResource(), getContentResource(null), and getContentResource("") will return the mocked Resource.</li>
 * <li>By default this class can adapt to a ValueMap and a Resource.</li>
 * </p>
 */
public class MockPage extends MockAdaptable {
    @Mock private PageManager pageManager; 
    @Mock private Page page;
    
    private MockResourceFactory mockResourceFactory = new MockResourceFactory();
    private Resource resource;
    
    private final String path;
    private final List<Page> pages;
    private final List<Resource> childResources;
    private final Map<String, Object> props;
    
    public final static String JCR_PROPERTY_CQ_TEMPLATE_TYPE = "cq:template";
    public final static String JCR_PROPERTY_SLING_VANITY_URL = "sling:vanity";
    
    private MockPage(final String path, final List<Page> pages, final List<Resource> childResources, final Map<String, Object> properties) {
        super(); 
        this.path = path;
        this.props = new HashMap<String, Object>(properties);
        this.pages = pages;
        this.childResources = childResources;
        this.resource = mockResourceFactory.withPath(path).withProperties(properties).build();
        setupPageManager();
        setupPage();
        setupPageProperties();
        mockReturns.put(Resource.class, resource);
        setupMockReturnValues();
    }
    
    @Override
    protected Adaptable getBackingObject() {
        return page;
    }
    
    private Page getPage() {
        return page;
    }
    
    /**
     * Adds the properties desired to the Page object which is returned. If a user needs the Resource to adapt to 
     * a ValueMap, the same properties will be returned.
     * 
     * @param props
     */
    private void setupPageProperties() {
        setupProperties(props);
        mockReturns.put(ValueMap.class, properties);
        when(page.getProperties()).thenReturn(properties);
        
        /**
         * The following represents a list of methods which derive values from properties, but do not internally 
         * convert the object to a ValueMap to pull the value.
         */
        
        Template templateType = (Template) this.properties.get(JCR_PROPERTY_CQ_TEMPLATE_TYPE);
        String vanityUrl = this.properties.get(JCR_PROPERTY_SLING_VANITY_URL, String.class);
        String description = this.properties.get(PagePropertyNames.JCR_DESCRIPTION.propertyName(), String.class);
        String title = this.properties.get(PagePropertyNames.JCR_TITLE.propertyName(), String.class);

        when(page.getTemplate()).thenReturn(templateType);
        when(page.getVanityUrl()).thenReturn(vanityUrl);
        when(page.getDescription()).thenReturn(description);
        when(page.getPageTitle()).thenReturn(title);
        when(page.getTitle()).thenReturn(title);
    }
    
    /**
     * Creates a default page, and content resource which is a representation of the page itself. Currently we only
     * support pages which contain content, thus the getContentResource will always return a valid Resource. 
     */
    private void setupPage() {
        when(page.hasContent()).thenReturn(true);
        when(page.getPath()).thenReturn(path);
        when(page.getContentResource()).thenReturn(resource);
        when(page.getContentResource(null)).thenReturn(resource);
        when(page.getContentResource("")).thenReturn(resource);
        
        String[] splitPath = path.split("/");
        String name = (splitPath.length < 1) ? "" : splitPath[splitPath.length - 1];
        int depth = splitPath.length - 1;
        when(page.getName()).thenReturn(name);
        when(page.getDepth()).thenReturn(depth);
        for (Resource childResource : childResources) {
            when(page.getContentResource(childResource.getPath())).thenReturn(childResource);
            when(childResource.getParent()).thenReturn(resource);
        }
        mockReturns.put(Resource.class, resource);
    }
    
    /**
     * Create a mocked PageManager which will have access to any Page the test will rely upon. 
     */
    private void setupPageManager() {
        when(pageManager.getPage(path)).thenReturn(page);
        when(page.getPageManager()).thenReturn(pageManager);
        for (Page page : pages) {
            when(pageManager.getPage(page.getPath())).thenReturn(page);
            when(page.getPageManager()).thenReturn(pageManager);
            // Setup absolute parent pages
            String[] pagePath = page.getPath().replaceFirst("^/", "").split("/");
            String[] mockPagePath = path.replaceFirst("^/", "").split("/");
            boolean isInPath = true;
            int i = 0;
            while (isInPath && i < pagePath.length) {
                isInPath = pagePath[i].equals(mockPagePath[i]);
                if (i == pagePath.length - 1) { 
                    break;
                }
                i++;
            }
            if (isInPath) {
                when(this.page.getAbsoluteParent(i)).thenReturn(page);
            }
        }
    }

    /**
     *  Small builder class to assist in creating MockPages for test usage.
     *  As new features are needed, this class can be updated to handle more features. 
     */
    public static class MockPageFactory extends AbstractSniMockFactory<MockPageFactory> {
        
        private List<Page> pages = new ArrayList<Page>();
        private List<Resource> resources = new ArrayList<Resource>();

        @Override
        public SniPage build() {
            if (path == null || path.indexOf("/") != 0) {
                throw new IllegalArgumentException("When mocking a page, a path must be provided");
            }
            return PageFactory.getSniPage(new MockPage(path, pages, resources, properties).getPage());
        }
        
        /**
         * Sets the List of Pages which the MockPage will rely upon for testing.
         * 
         * @param pages
         * @return
         */
        public MockPageFactory reliesOnPages(final List<Page> pages) {
            this.pages = pages;
            return this;
        }

        /**
         * Convenience method for setting a single Page which the MockPage will rely upon for testing.
         * This is useful for situations when you need to mock a SniPage. The ContentPage will require 
         * the following property to be set: PagePropertyConstants.PROP_SNI_ASSETLINK.
         * @param page
         * @return
         */
        public MockPageFactory reliesOnPage(final Page page) {
            pages.add(page);
            return this;
        }
        
        /**
         * Convenience method for setting up a mock value to be returned due to calls to getContentResource("some/path") 
         * during testing. The mocked page will default out calls to getContentResource(null), getContentResource(""), and 
         * getContentResource()
         * 
         * @param child
         * @return
         */
        public MockPageFactory reliesOnChildResource(final Resource child) {
            resources.add(child);
            return this;
        }
        
        /**
         * Convenience method for setting up multiple resources which need to be mocked due to calls to getContentResource("some/path")
         * during testing. The mocked page will default out calls to getContentResource(null), getContentResource(""), and 
         * getContentResource().
         * 
         * @param childResources
         * @return
         */
        public MockPageFactory reliesOnChildResources(final List<Resource> childResources) {
            this.resources = childResources;
            return this;
        }
        
        /**
         * Convenience method for setting a property with a value from the PagePropertyNames enum. This should alleviate the need for 
         * having duplicate string values throughout the testing / mocking code.
         * 
         * @param propertyName
         * @param value
         * @return
         */
        public MockPageFactory withPageProperty(final PagePropertyNames propertyName, final String value) {
            withProperty(propertyName.propertyName(), value);
            return this;
        }
        
        /**
         * Convenience method for setting the resource type on the returned page. This should alleviate the need for having duplicate
         * string values throughout the testing / mocking code.
         * 
         * @param resourceType
         * @param value
         * @return
         */
        public MockPageFactory ofResourceType(PageSlingResourceTypes resourceType) {
            withPageProperty(PagePropertyNames.SLING_RESOURCE_TYPE, resourceType.resourceType());
            return this;
        }
        
    }
}
