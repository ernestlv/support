package com.scrippsnetworks.wcm.fnr.module;

import com.day.cq.replication.ReplicationStatus;
import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.page.SniPage;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.fest.assertions.Assertions.*;

/**
 * @author Ken Shih (156223)
 * @created 4/17/13 10:26 AM
 */
public class ListSelectShowsHelperTest {

    ListSelectShowsHelper listSelectShowsHelper;
    @Mock
    ResourceResolver mockResourceResolver;
    @Mock
    ResourceResolverFactory mockResourceResolverFactory;
    @Mock
    Page mockPage;
    @Mock
    SniPage mockSniPage;
    @Mock
    Resource mockPageResource, mockContentResource;
    @Mock ReplicationStatus mockReplicationStatus;

    @Test
    public void convertShowPathsToGroupCpmnData(){
        //setup
        String[] showPaths = {"/test-path1","/test-path2"};
        when(mockResourceResolver.getResource(eq("/test-path1"))).thenReturn(mockPageResource);
        when(mockResourceResolver.getResource(eq("/test-path2"))).thenReturn(mockPageResource);
        when(mockPageResource.adaptTo(eq(Page.class))).thenReturn(mockPage);
        when(mockPage.getContentResource()).thenReturn(mockContentResource);
        when(mockContentResource.adaptTo(eq(ReplicationStatus.class))).thenReturn(mockReplicationStatus);
        when(mockReplicationStatus.isActivated()).thenReturn(true);

        //test
        List<Map<String,Object>> out = listSelectShowsHelper.convertShowPathsToGroupCpmnData(showPaths);

        //verify
        assertThat(out).as("has data for both test paths").hasSize(2);
        assertThat(out.get(0).get("title")).isEqualTo("mytitle");
        assertThat(out.get(0).get("tuneInTime")).isEqualTo("mytuneintime");
        assertThat(out.get(0).get("imagePath")).isEqualTo("/mybannerpath");
        assertThat(out.get(0).get("showPath")).isEqualTo("/test-path1");
        assertThat(out.get(1).get("showPath")).isEqualTo("/test-path2");

    }

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        listSelectShowsHelper = new ListSelectShowsHelper();
        listSelectShowsHelper = spy(listSelectShowsHelper);
        listSelectShowsHelper.resourceResolverFactory=mockResourceResolverFactory;
        when(listSelectShowsHelper.newSniPage(any(Page.class))).thenReturn(mockSniPage);
        when(mockResourceResolverFactory.getAdministrativeResourceResolver(any(Map.class))).thenReturn(mockResourceResolver);
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("jcr:title","mytitle");
        map.put("sni:tuneInTime","mytuneintime");
        map.put("sni:featureBannerImage","/mybannerpath");
        ValueMap valueMap = new ValueMapDecorator(map);
        when(mockSniPage.getProperties()).thenReturn(valueMap);
    }
}
