package com.scrippsnetworks.wcm.asset;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.asset.hub.Hub;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitHeightDestination;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.fest.assertions.Assertions.assertThat;
import org.mockito.MockitoAnnotations;

/**
 * To test the static methods of SponsorshipUtil. Only adding the functionality I'm changing, now....
 * @author Ken Shih (156223)
 * @created 3/6/13 1:46 PM
 */
public class SponsorshipUtilTest {

    @Mock Resource mockResource, mockSponsorshipResource;
    @Mock Page mockPage, mockSponsorshipPage;
    @Mock Hub mockHub;
    @Mock ValueMap mockValueMap;
    @Mock ResourceResolver mockResourceResolver;


    @Test
    public void getSponsorshipValueFromSponsorshipResource(){
        //setup
        when(mockResource.getPath()).thenReturn("/my/sample/path");

        //test
        String result = SponsorshipUtil.getSponsorshipValueFromSponsorshipResource(mockResource);

        //verify
        assertThat(result).as("tested method returns transformed value of sponsorship").isEqualTo("PATH");
    }

    @Test
    public void getSponsorshipPath_NullHub(){
        //setup
        when(mockPage.getContentResource()).thenReturn(mockResource);
        when(mockResource.adaptTo(eq(ValueMap.class))).thenReturn(mockValueMap);
        when(mockResource.getResourceResolver()).thenReturn(mockResourceResolver);
        when(mockValueMap.get(eq("sni:sponsorship"), eq(String.class))).thenReturn("/my/sample/path");
        when(mockResourceResolver.getResource(eq("/my/sample/path"))).thenReturn(mockSponsorshipResource);
        when(mockSponsorshipResource.adaptTo(eq(Page.class))).thenReturn(mockSponsorshipPage);
        when(mockSponsorshipPage.isValid()).thenReturn(true);
        when(mockSponsorshipResource.getPath()).thenReturn("/my/sample/path");

        //test
        String spPath = SponsorshipUtil.getSponsorshipPath(mockPage, null);

        //verify
        assertThat(spPath).isEqualTo("/my/sample/path");
    }

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
    }
}
