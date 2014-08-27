package com.scrippsnetworks.wcm.export.snipage.content.impl;

import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.photogallery.PhotoGallery;
import com.scrippsnetworks.wcm.page.SniPage;

import com.scrippsnetworks.wcm.photogallery.PhotoGallerySlide;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class PhotoGalleryExportTest {

    public static final String PAGE_PATH = "/content/food/shows/a/a-show/photos";
    public static final String SLIDE_1_TITLE = "Slide 1 Title";
    public static final String SLIDE_2_TITLE = "Slide 2 Title";
    public static final String SLIDE_3_TITLE = "Slide 3 Title";
    public static final String SLIDE_4_TITLE = "Slide 4 Title";
    public static final String SLIDE_1_IMAGE_PATH = "/content/dam/images/food/fullset/2013/1/1/0/image1.jpg";
    public static final String SLIDE_2_IMAGE_PATH = "/content/dam/images/food/fullset/2013/1/1/0/image2.jpg";
    public static final String SLIDE_3_IMAGE_PATH = "/content/dam/images/food/fullset/2013/1/1/0/image3.jpg";
    public static final String SLIDE_4_IMAGE_PATH = "/content/dam/images/food/fullset/2013/1/1/0/image4.jpg";

    @Mock PhotoGallery photoGallery;
    @Mock SniPage sniPage;
    @Mock ValueMap sniPageProperties;
    @Mock Resource sniPageCR;
    @Mock PhotoGallerySlide slide1;
    @Mock SniImage image1;
    @Mock PhotoGallerySlide slide2;
    @Mock SniImage image2;
    @Mock PhotoGallerySlide slide3;
    @Mock SniImage image3;
    @Mock PhotoGallerySlide slide4;
    @Mock SniImage image4;

    List<PhotoGallerySlide> slides;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        when(sniPage.hasContent()).thenReturn(true);
        when(sniPage.getContentResource()).thenReturn(sniPageCR);
        when(sniPage.getProperties()).thenReturn(sniPageProperties);
        when(sniPage.getPath()).thenReturn(PAGE_PATH);
        slides = Arrays.asList(slide1, slide2, slide3, slide4);
        when(photoGallery.getAllSlides()).thenReturn(slides);

        when(slide1.getSniImage()).thenReturn(image1);
        when(slide2.getSniImage()).thenReturn(image2);
        when(slide3.getSniImage()).thenReturn(image3);
        when(slide4.getSniImage()).thenReturn(image4);

        when(slide1.getTitle()).thenReturn(SLIDE_1_TITLE);
        when(slide2.getTitle()).thenReturn(SLIDE_2_TITLE);
        when(slide3.getTitle()).thenReturn(SLIDE_3_TITLE);
        when(slide4.getTitle()).thenReturn(SLIDE_4_TITLE);

        when(slide1.getSniImage()).thenReturn(image1);
        when(slide2.getSniImage()).thenReturn(image2);
        when(slide3.getSniImage()).thenReturn(image3);
        when(slide4.getSniImage()).thenReturn(image4);

        when(image1.getPath()).thenReturn(SLIDE_1_IMAGE_PATH);
        when(image2.getPath()).thenReturn(SLIDE_2_IMAGE_PATH);
        when(image3.getPath()).thenReturn(SLIDE_3_IMAGE_PATH);
        when(image4.getPath()).thenReturn(SLIDE_4_IMAGE_PATH);
    }

    @Test
    public void testPhotoGalleryPhotos() {

        PhotoGalleryExport pgExport = new PhotoGalleryExport(sniPage, photoGallery);
        assertNotNull("export is not null", pgExport);
        ValueMap exportProps = pgExport.getValueMap();
        String[] pgPhotos = exportProps.get(PhotoGalleryExport.ExportProperty.PHOTOGALLERY_PHOTO.name(),String[].class);
        assertNotNull("PHOTOGALLERY_PHOTOS property present", pgPhotos);
        assertEquals("all slides available", slides.size(), pgPhotos.length);
        String pval1 = pgPhotos[0];
        String[] pval1Split = pval1.split(Pattern.quote(SniPageExport.COMPOSITE_VALUE_DELIMITER));
        assertEquals("array element field number correct", 4, pval1Split.length);

        assertEquals("title is first", SLIDE_1_TITLE, pval1Split[0]);
        assertEquals("imagepath is second", SLIDE_1_IMAGE_PATH, pval1Split[1]);
        assertEquals("page url is third", PAGE_PATH + ".html", pval1Split[2]);
        assertEquals("ranking is fourth", "1", pval1Split[3]);

        String pval2 = pgPhotos[1];
        String[] pval2Split = pval2.split(Pattern.quote(SniPageExport.COMPOSITE_VALUE_DELIMITER));
        assertTrue("page 2 url contains page selector", pval2Split[2].endsWith(".page-2.html"));
    }

    @Test
    public void testPhotoGallerySlideWithNoImage() {
        when(slide1.getSniImage()).thenReturn(null);

        PhotoGalleryExport pgExport = new PhotoGalleryExport(sniPage, photoGallery);
        ValueMap exportProps = pgExport.getValueMap();
        String[] pgPhotos = exportProps.get(PhotoGalleryExport.ExportProperty.PHOTOGALLERY_PHOTO.name(),String[].class);
        assertEquals("slide with no image excluded", slides.size() - 1, pgPhotos.length);
    }

    @Test
    public void testPhotoGallerySlideWithNoImagePath() {
        when(image1.getPath()).thenReturn(null);

        PhotoGalleryExport pgExport = new PhotoGalleryExport(sniPage, photoGallery);
        ValueMap exportProps = pgExport.getValueMap();
        String[] pgPhotos = exportProps.get(PhotoGalleryExport.ExportProperty.PHOTOGALLERY_PHOTO.name(),String[].class);
        assertEquals("image with no path excluded", slides.size() - 1, pgPhotos.length);
    }

    @Test
    public void testTitleCompositeSeparatorEscaped() {
        when(slide1.getTitle()).thenReturn("FOO" + SniPageExport.COMPOSITE_VALUE_DELIMITER + "BAR");

        PhotoGalleryExport pgExport = new PhotoGalleryExport(sniPage, photoGallery);
        ValueMap exportProps = pgExport.getValueMap();
        String[] pgPhotos = exportProps.get(PhotoGalleryExport.ExportProperty.PHOTOGALLERY_PHOTO.name(),String[].class);
        String pval1 = pgPhotos[0];
        String[] pval1Split = pval1.split(Pattern.quote(SniPageExport.COMPOSITE_VALUE_DELIMITER));
        assertEquals("delimiter replaced in title", "FOO" + SniPageExport.COMPOSITE_VALUE_REPLACEMENT + "BAR", pval1Split[0]);
    }

}
