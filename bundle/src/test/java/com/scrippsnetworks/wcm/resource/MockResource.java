package com.scrippsnetworks.wcm.resource;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.mockito.Mock;

import com.scrippsnetworks.wcm.AbstractSniMockFactory;
import com.scrippsnetworks.wcm.MockAdaptable;

/**
 * Utility class for setting up testing of a {@link org.apache.sling.api.resource.Resource Resource}.
 * Users should not instantiate this class directly, but instead use the factory 
 * {@link com.scrippsnetworks.wcm.resource.MockResource.MockResourceFactory MockResourceFactory}. 
 * <p>
 * This class will return a mock {@link MockResourceFactory}, which defaults to returning the following:
 * <ul><li>A Mocked ResourceResolver - this will default to returning the Resource we are mocking, when utilizing the same path
 * as we pass into the {@code com.scrippsnetworks.wcm.resource.MockResource.MockResourceFactory MockResourceFactory}. It will 
 * also default to returning any resources which this mocked resource needs in order to complete it's testing. These can be other
 * resources which have been mocked as well.</li>
 * <li>Properties will be exposed via the {@link org.apache.sling.api.resource.ValueMap ValueMap} API. The caller should 
 * utilize the {@link com.scrippsnetworks.wcm.resource.MockResrouceFactory MockResourceFactory} to setup other required resources
 * it needs for testing.</li>
 * <li>By default this class can adapt to a ValueMap.</li>
 * </p>
 */
public class MockResource extends MockAdaptable {

    public final static String JCR_PROPERTY_RESOURCE_TYPE = "sling:resourceType";
    @Mock private Resource resource;
    @Mock private ResourceResolver resourceResolver;
    
    private final String path;
    private final List<Resource> childResources;
    private final Map<String, Object> props;
    
    private MockResource(final String path, final List<Resource> childResources, final Map<String, Object> properties) {
        super();
        this.path = path;
        this.childResources = childResources;
        this.props = new HashMap<String, Object>(properties);
        setupResourceResolver();
        setupResource();
        setupResourceProperties();
        setupMockReturnValues();
    }
    
    @Override
    protected Adaptable getBackingObject() {
        return resource;
    }
    
    public void setupResourceResolver() {
        when(resource.getResourceResolver()).thenReturn(resourceResolver);
        for (Resource childResource : childResources) {
            when(resourceResolver.getResource(childResource.getPath())).thenReturn(childResource);
            when(childResource.getResourceResolver()).thenReturn(resourceResolver);
        }
    }
    
    private void setupResourceProperties() {
        setupProperties(props);
        /**
         * The following represents a list of methods which derive values from properties, but do not internally 
         * convert the object to a ValueMap to pull the value.
         */
        String resourceType = this.properties.get(MockResource.JCR_PROPERTY_RESOURCE_TYPE, String.class);
        when(resource.getResourceType()).thenReturn(resourceType);
        mockReturns.put(ValueMap.class, this.properties);
    }
    
    public void setupResource() {
        String name = path.substring(path.lastIndexOf("/") + 1);
        when(resource.getName()).thenReturn(name);
        when(resource.getPath()).thenReturn(path);
    }
    
    private Resource getResource() {
        return resource;
    }
    
    public static final class MockResourceFactory extends AbstractSniMockFactory<MockResourceFactory> {

        private List<Resource> resources = new ArrayList<Resource>();
        
        @Override
        public Resource build() {
            return new MockResource(path, resources, properties).getResource();
        }

        /**
         * Convenience method for setting up a mock value to be returned due to calls to getContentResource("some/path") 
         * during testing. The mocked Resource will return itself upon calls to getContentResource(null), getContentResource(""), and 
         * getContentResource()
         * 
         * @param child
         * @return
         */
        public MockResourceFactory reliesOnChildResource(Resource child) {
            resources.add(child);
            return this;
        }
        
        /**
         * Convenience method for setting up multiple resources to be returned due to calls to getContentResource("some/path")
         * during testing. The mocked Resource will return itself upon calls to getContentResource(null), getContentResource(""), and 
         * getContentResource()
         * 
         * @param child
         * @return
         */
        public MockResourceFactory reliesOnChildResources(List<Resource> childResources) {
            this.resources = childResources;
            return this;
        }
        
    }

}
