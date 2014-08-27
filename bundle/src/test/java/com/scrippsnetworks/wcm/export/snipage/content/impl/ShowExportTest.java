package com.scrippsnetworks.wcm.export.snipage.content.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.show.Show;

/**
 * This class is used for testing Show Exports.
 * @author Venkata Naga Sudheer Donaboina
 *
 */
public class ShowExportTest {
	public static final String PAGE_PATH = "/content/food/shows/a-show";
	public static final String PAGE_TYPE = "show";

	public static final String PAGE_SHOW_FEATURE_BANNER_PATH = "http://images.scrippsnetworks.com/up/images/0193/0193_92x69.jpg";
	
	@Mock
	SniPage showPage;
	@Mock
	Show show;

	@Mock
	Resource showPageCR;
	@Mock
	ValueMap showPageProperties;

	@Mock
	PageManager pageManager;
	@Mock
	ResourceResolver resourceResolver;

	@Mock SniImage featuredBannedImage;
	
	@Before
	public void before() {

		MockitoAnnotations.initMocks(this);

		when(showPage.hasContent()).thenReturn(true);
		when(showPage.getProperties()).thenReturn(showPageProperties);
		when(showPage.getContentResource()).thenReturn(showPageCR);
		when(showPage.getPath()).thenReturn(PAGE_PATH);
		when(showPage.getPageType()).thenReturn(PAGE_TYPE);

		when(showPage.getPageManager()).thenReturn(pageManager);

		when(show.getFeatureBanner()).thenReturn(featuredBannedImage);
		
		when(featuredBannedImage.getPath()).thenReturn(PAGE_SHOW_FEATURE_BANNER_PATH);
		
	}

	@Test
	public void testShowPropertyValues() {
		
		ShowExport showExport = new ShowExport(showPage, show);
		ValueMap exportProps = showExport.getValueMap();

		assertEquals(ShowExport.ExportProperty.SHOW_FEATURE_IMAGE_BANNER_PATH.name(), PAGE_SHOW_FEATURE_BANNER_PATH, 
				exportProps.get(ShowExport.ExportProperty.SHOW_FEATURE_IMAGE_BANNER_PATH.name(),
						ShowExport.ExportProperty.SHOW_FEATURE_IMAGE_BANNER_PATH.valueClass()));

        // Set core image path to feature banner too.
		/*
        assertEquals(SniPageExport.ExportProperty.CORE_IMAGE_PATH.name(), PAGE_SHOW_FEATURE_BANNER_PATH,
      				exportProps.get(SniPageExport.ExportProperty.CORE_IMAGE_PATH.name(),
                            SniPageExport.ExportProperty.CORE_IMAGE_PATH.valueClass()));
        */
	}

    @Test
    public void testNoFeatureBannerCoreImagePathEmpty() {
        when(show.getFeatureBanner()).thenReturn(null);
        when(showPage.getCanonicalImageUrl()).thenReturn("/content/dam/doesnotmatter");
        ShowExport showExport = new ShowExport(showPage, show);
        ValueMap exportProps = showExport.getValueMap();
        
        
        assertNull(ShowExport.ExportProperty.SHOW_FEATURE_IMAGE_BANNER_PATH.name(),
                exportProps.get(ShowExport.ExportProperty.SHOW_FEATURE_IMAGE_BANNER_PATH.name(),
                        ShowExport.ExportProperty.SHOW_FEATURE_IMAGE_BANNER_PATH.valueClass()));

        // Set core image path to feature banner too.
        /*
        assertNull(SniPageExport.ExportProperty.CORE_IMAGE_PATH.name(),
                exportProps.get(SniPageExport.ExportProperty.CORE_IMAGE_PATH.name(),
                        SniPageExport.ExportProperty.CORE_IMAGE_PATH.valueClass()));
        */
    }

}
