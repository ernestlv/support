package com.scrippsnetworks.wcm.breadcrumb.impl;


import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Template;
import com.scrippsnetworks.wcm.breadcrumb.Breadcrumb;
import com.scrippsnetworks.wcm.breadcrumb.BreadcrumbFactory;
import com.scrippsnetworks.wcm.breadcrumb.BreadcrumbHubLabels;
import com.scrippsnetworks.wcm.breadcrumb.crumb.Crumb;
import com.scrippsnetworks.wcm.breadcrumb.crumb.CrumbTypes;
import com.scrippsnetworks.wcm.fnr.util.PageSlingResourceTypes;
import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import com.scrippsnetworks.wcm.page.SniPage;

public class BreadcrumbTest {
    
    @Mock private SniPage page;
    @Mock private Hub hub;
    @Mock private Template template;
    @Mock private Page sectionPage;
    @Mock private ResourceResolver resourceResolver;
    @Mock private PageManager pageManager;
    @Mock private ValueMap valueMap;
    
    private void setup(final String pagePath, final String pageTitle, final String sectionTitle, final String sectionPath) {
        initMocks(this);
        when(page.getDepth()).thenReturn(pagePath.split("/").length - 1);
        when(page.getPath()).thenReturn(pagePath);
        when(page.getTitle()).thenReturn(pageTitle);
        when(sectionPage.getName()).thenReturn(FilenameUtils.getName(pagePath));
        when(sectionPage.getTitle()).thenReturn(sectionTitle);
        when(sectionPage.getPath()).thenReturn(sectionPath);
        when(sectionPage.getTemplate()).thenReturn(template);
        when(page.getAbsoluteParent(2)).thenReturn(sectionPage);
        Resource resource = Mockito.mock(Resource.class);
        when(sectionPage.getContentResource()).thenReturn(resource);
        when(resource.getResourceResolver()).thenReturn(resourceResolver);
        when(sectionPage.getPageManager()).thenReturn(pageManager);
        when(page.getPageManager()).thenReturn(pageManager);
        when(sectionPage.getProperties()).thenReturn(valueMap);
        when(valueMap.get(PagePropertyConstants.PROP_SNI_TITLE, String.class)).thenReturn(sectionTitle);
    }
    
    private void setupHub(final PageSlingResourceTypes hubResourceType, final String parentTitle, final String parentPath) {
        Resource resource = Mockito.mock(Resource.class);
        SniPage parentPage = Mockito.mock(SniPage.class);
        ValueMap valueMap = Mockito.mock(ValueMap.class);
        when(parentPage.getContentResource()).thenReturn(resource);
        when(resource.getResourceResolver()).thenReturn(resourceResolver);
        when(parentPage.getPageManager()).thenReturn(pageManager);
        when(parentPage.getProperties()).thenReturn(valueMap);
        when(valueMap.get(PagePropertyConstants.PROP_SNI_TITLE, String.class)).thenReturn(parentTitle);
        when(page.getHub()).thenReturn(hub);
        when(page.getParent()).thenReturn(parentPage);
        when(parentPage.getUrl()).thenReturn(parentPath+".html");
        when(parentPage.getPath()).thenReturn(parentPath);
        when(parentPage.getTitle()).thenReturn(parentTitle);
        when(hub.getHubMaster()).thenReturn(parentPage);
        when(hub.isPageHubChild(page)).thenReturn(true);
        when(page.getContentResource()).thenReturn(resource);
        when(resource.getResourceResolver()).thenReturn(resourceResolver);
        when(resource.getResourceType()).thenReturn(hubResourceType.resourceType());
    }

    @Test
    public void testNonHubbedArticle() {
        setup("/content/food/articles/testArticle", "Test Article", "Articles", "/content/food/articles");
        Breadcrumb bc = new BreadcrumbFactory().withSniPage(page).build();
        List<Crumb> crumbs = bc.getCrumbs();
        for (Crumb crumb : crumbs) {
            CrumbTypes type = crumb.getType();
            switch (type) {
                case SECTION:
                    Assert.assertEquals("The SECTION title should be " + "Articles",
                        "Articles",
                        crumb.getTitle());
                    break;
            }
        }
    }

    @Test 
    public void testHubbedArticle() {
        setup("/content/food/chefs/alton-brown/testArticle", "Test Article", "Chefs", "/content/food/chefs");
        setupHub(PageSlingResourceTypes.ARTICLE_SIMPLE, "Alton Brown", "/content/food/chefs/alton-brown");
        Assert.assertNotNull("Page should be hubbed", page.getHub().getHubMaster());
        Breadcrumb bc = new BreadcrumbFactory().withSniPage(page).build();
        List<Crumb> crumbs = bc.getCrumbs();
        for (Crumb crumb : crumbs) {
            CrumbTypes type = crumb.getType();
            switch (type) {
                case HUB:
                    Assert.assertEquals("The HUB title is incorrect",
                        "Alton Brown",
                        crumb.getTitle());
                    Assert.assertEquals("The HUB url is incorrect",
                        "/content/food/chefs/alton-brown.html",
                        crumb.getUrl());
                    break;
                case SECTION:
                    Assert.assertEquals("The SECTION title should be " + "Chefs",
                        "Chefs",
                        crumb.getTitle());
                    break;
            }
        }
    }

}
