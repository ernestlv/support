package com.scrippsnetworks.wcm.export.snipage.content.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.photogallery.PhotoGallery;
import com.scrippsnetworks.wcm.photogallery.listing.PhotoGalleryListing;

public class PhotoGalleryListingTest {

	public static final String PAGE_PATH = "/content/food/shows/photogalleries/a-photoGalleryListing";
	public static final String PAGE_TYPE = "photo-gallery-listing";
	
	public static final String PHOTO_GALLERY_PAGE1_UID = "aaaa-bbbb-cccc-dddd";
	public static final String PHOTO_GALLERY_PAGE2_UID = "aaaa-cccc-bbbb-dddd";
	public static final String PHOTO_GALLERY_PAGE3_UID = "aaaa-bbbb-dddd-cccc";
	
	@Mock PhotoGalleryListing photoGalleryListing;
	
	@Mock Resource photoGalleryListingPageCR;
	@Mock ValueMap photoGalleryPageProperties;

    @Mock PageManager pageManager;
    @Mock ResourceResolver resourceResolver;
    
    @Mock SniPage photoGalleryListingPage;
    
    @Mock PhotoGallery photoGallery1;
    @Mock PhotoGallery photoGallery2;
    @Mock PhotoGallery photoGallery3;
    
    @Mock SniPage photoGalleryPage1;
    @Mock SniPage photoGalleryPage2;
    @Mock SniPage photoGalleryPage3;

    @Mock List<PhotoGallery> photoGalleries;
    
    @Before
    public void before() {
    	MockitoAnnotations.initMocks(this);
    	
    	when(photoGalleryListingPage.hasContent()).thenReturn(true);
    	when(photoGalleryListingPage.getProperties()).thenReturn(photoGalleryPageProperties);
    	when(photoGalleryListingPage.getContentResource()).thenReturn(photoGalleryListingPageCR);
    	when(photoGalleryListingPage.getPath()).thenReturn(PAGE_PATH);
    	when(photoGalleryListingPage.getPageType()).thenReturn(PAGE_TYPE);
    	
    	when(photoGalleryListingPage.getPageManager()).thenReturn(pageManager);
    	
    }
    
    /** set up photoGallery, photoGallery pages and photoGallery Uid. */
    private void setupPhotoGalleries() {
    	photoGalleries = Arrays.asList(photoGallery1, photoGallery2, photoGallery3);
    	when(photoGalleryListing.getPhotoGalleries()).thenReturn(photoGalleries);
    	
    	when(photoGallery1.getPage()).thenReturn(photoGalleryPage1);
    	when(photoGallery2.getPage()).thenReturn(photoGalleryPage2);
    	when(photoGallery3.getPage()).thenReturn(photoGalleryPage3);
    	
    	when(photoGalleryPage1.getUid()).thenReturn(PHOTO_GALLERY_PAGE1_UID);
    	when(photoGalleryPage2.getUid()).thenReturn(PHOTO_GALLERY_PAGE2_UID);
    	when(photoGalleryPage3.getUid()).thenReturn(PHOTO_GALLERY_PAGE3_UID);
    	
    }
    
    @Test
    public void testPhotoGalleries() {
    	setupPhotoGalleries();
    	PhotoGalleryListingExport photoGalleryListingExport = new PhotoGalleryListingExport(photoGalleryListingPage, photoGalleryListing);
    	ValueMap exportProps = photoGalleryListingExport.getValueMap();
    	
    	String[] photoGalleries = exportProps.get(PhotoGalleryListingExport.ExportProperty.PHOTOGALLERYLISTING_PHOTOGALLERIES.name(), String[].class);
    	
    	int i = 0;
    	for(PhotoGallery photoGallery : photoGalleryListing.getPhotoGalleries()) {
    		assertEquals("Photo Gallery Page", photoGallery.getPage().getUid(), photoGalleries[i++]);
    	}
    }
}
