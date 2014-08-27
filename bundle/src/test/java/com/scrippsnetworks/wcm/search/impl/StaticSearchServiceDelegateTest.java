package com.scrippsnetworks.wcm.search.impl;

import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Dictionary;
import java.util.Iterator;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.scrippsnetworks.wcm.search.SearchRequestException;
import com.scrippsnetworks.wcm.search.SearchResponse;
import com.scrippsnetworks.wcm.search.impl.SearchServiceDelegate.ConfigStateCreationException;

public class StaticSearchServiceDelegateTest {
    @Mock private ResourceResolverFactory rrf;
    @Mock private ResourceResolver rr;
    @Mock private Resource directory;
    @Mock private Resource child;
    @Mock private Dictionary dictionary;
    @Mock private Iterator<Resource> childrenIterator;
    
    private static final String EMPTY_RESPONSE = "{\"response\": {}}";
    
    private StaticSearchServiceDelegate sssd;
    
    @Before
    public void setup() throws LoginException {
        MockitoAnnotations.initMocks(this);
        when(rrf.getAdministrativeResourceResolver(null)).thenReturn(rr);
        sssd = new StaticSearchServiceDelegate(rrf);
    }
    
    @Test(expected = ConfigStateCreationException.class)
    public void testNullConfigValue() throws ConfigStateCreationException {
        when(dictionary.get(SearchServiceDelegate.RESOURCE_LOCATIONS)).thenReturn(null);
        sssd.activate(dictionary);
    }
    
    @Test(expected = ConfigStateCreationException.class)
    public void testEmptyConfigValue() throws ConfigStateCreationException {
        when(dictionary.get(SearchServiceDelegate.RESOURCE_LOCATIONS)).thenReturn("");
        sssd.activate(dictionary);
    }
    
    @Test
    public void testValidConfig() throws ConfigStateCreationException {
        when(dictionary.get(SearchServiceDelegate.RESOURCE_LOCATIONS)).thenReturn("/content/");
        when(directory.listChildren()).thenReturn(childrenIterator);
        when(childrenIterator.hasNext()).thenReturn(true,false);
        when(childrenIterator.next()).thenReturn(child);
        when(child.adaptTo(InputStream.class)).thenReturn(IOUtils.toInputStream(EMPTY_RESPONSE));
        when(child.getName()).thenReturn("recipeList");
        when(rr.getResource("/content/")).thenReturn(directory);
        sssd.activate(dictionary);
        SearchResponse sr = sssd.getSearchRequestHandler().getResponse("recipeList", null);
        Assert.assertEquals(EMPTY_RESPONSE, sr.getPayload());
    }
    
    @Test
    public void testUnconfiguredResource() throws ConfigStateCreationException {
        when(dictionary.get(SearchServiceDelegate.RESOURCE_LOCATIONS)).thenReturn("/content/");
        when(directory.listChildren()).thenReturn(childrenIterator);
        when(childrenIterator.hasNext()).thenReturn(true,false);
        when(childrenIterator.next()).thenReturn(child);
        when(child.adaptTo(InputStream.class)).thenReturn(IOUtils.toInputStream(EMPTY_RESPONSE));
        when(child.getName()).thenReturn("recipeList");
        when(rr.getResource("/content/")).thenReturn(directory);
        sssd.activate(dictionary);
        SearchResponse sr = sssd.getSearchRequestHandler().getResponse("invalidRecipeList", null);
        Assert.assertEquals(SearchRequestException.class, sr.getException().getClass());
    }
}
