package com.scrippsnetworks.wcm.regions.impl;

import com.scrippsnetworks.wcm.page.SniPage;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.fest.assertions.Assertions.*;

/**
 * User: kenshih
 * Date: 8/23/13
 */
public class FooterTest {

    @Mock ResourceResolver mockResourceResolver;
    @Mock SniPage mockSniPage;
    @Mock Resource mockContentResource;
    @Mock Resource mockFooterResource;
    @Mock ValueMap mockProperties;

    @Test
    public void construction(){
        //given
        when(mockSniPage.getProperties()).thenReturn(mockProperties);
        when(mockResourceResolver.getResource(anyString())).thenReturn(mockFooterResource);
        when(mockFooterResource.getPath()).thenReturn("/test/path");

        //when
        FooterImpl footer = new FooterImpl(mockSniPage);

        //then
        assertThat(footer.isValid() ).isTrue();
        assertThat(footer.getPath()).isEqualTo("/test/path");
        assertThat(footer.getResource()).isEqualTo(mockFooterResource);
    }

    @Test public void override(){
        //given
        String overridePath = "/override/path";
        when(mockSniPage.getProperties()).thenReturn(mockProperties);
        when(mockProperties.get(anyString(),anyString())).thenReturn(overridePath);
        when(mockResourceResolver.getResource(eq(overridePath+"/jcr:content/footer"))).thenReturn(mockFooterResource);
        when(mockFooterResource.getPath()).thenReturn(overridePath);

        //when
        FooterImpl footer = new FooterImpl(mockSniPage);

        //then
        assertThat(footer.isValid() ).isTrue();
        assertThat(footer.getPath()).isEqualTo(overridePath);
        assertThat(footer.getResource()).isEqualTo(mockFooterResource);
    }


    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        when(mockSniPage.getContentResource()).thenReturn(mockContentResource);
        when(mockContentResource.getResourceResolver()).thenReturn(mockResourceResolver);
    }
}
