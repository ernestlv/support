package com.scrippsnetworks.wcm.export.snipage.content.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.video.Video;

/**
 * This class is used for testing Video Exports.
 * @author Venkata Naga Sudheer Donaboina
 */
public class VideoExportTest {
    public static final String PAGE_PATH = "/content/food/video/a-video";
    public static final String PAGE_TYPE = "video";

    public static final String VIDEO_RUNTIME = "12:22:00";
    
    public static final String VIDEO_THUMBNAIL_IMAGE_16x9 = "http://images.scrippsnetworks.com/up/images/0193/0193921_92x69.jpg";
    
    public static final String VIDEO_THUMBNAIL_IMAGE_4x3 = "http://images.scrippsnetworks.com/up/images/0193/0193921_92x69.jpg";
    
    public static final String VIDEO_SHOW_ID = "aaaa-bbbb-dddd-cccc";
    
    public static final String VIDEO_SHOW_PATH = "/content/food/shows/a/aa/a-show";
    
    public static final String VIDEO_HOME = "/content/food/video/a/aa/a-video";
    
    @Mock
    SniPage videoPage;
    @Mock
    SniPage videoShowPage;
    @Mock 
    SniPage videoShowAssetPage;
    @Mock
    Video video;

    @Mock
    Resource videoPageCR;
    @Mock
    ValueMap videoPageProperties;
    @Mock
    ValueMap videoShowPageProperties;

    @Mock
    PageManager pageManager;
    @Mock
    ResourceResolver resourceResolver;

    @Before
    public void before() {

        MockitoAnnotations.initMocks(this);

        when(videoPage.hasContent()).thenReturn(true);
        when(videoPage.getProperties()).thenReturn(videoPageProperties);
        when(videoPage.getContentResource()).thenReturn(videoPageCR);
        when(videoPage.getPath()).thenReturn(PAGE_PATH);
        when(videoPage.getPageType()).thenReturn(PAGE_TYPE);

        when(videoPage.getPageManager()).thenReturn(pageManager);

        when(video.getVideoRunTime()).thenReturn(VIDEO_RUNTIME);
        
        when(video.getPromoUrl()).thenReturn(VIDEO_HOME);
        
        when(video.getSniPage()).thenReturn(videoPage);
        
        when(video.getSniPage().getPath()).thenReturn(PAGE_PATH);
        
        when(video.getThumbnailUrl()).thenReturn(VIDEO_THUMBNAIL_IMAGE_4x3);
        
        when(video.getThumbnailImage16X9()).thenReturn(VIDEO_THUMBNAIL_IMAGE_16x9);
        
    }
    
    @Test
    public void testVideoProps() {
    	 VideoExport videoExport = new VideoExport(videoPage, video);
         ValueMap exportProps = videoExport.getValueMap();

         assertEquals(VideoExport.ExportProperty.VIDEO_LENGTH.name(), VIDEO_RUNTIME, exportProps.get(
                 VideoExport.ExportProperty.VIDEO_LENGTH.name(),
                 VideoExport.ExportProperty.VIDEO_LENGTH.valueClass()));
         
         assertEquals(VideoExport.ExportProperty.VIDEO_THUMBNAIL_IMAGE_4x3.name(), VIDEO_THUMBNAIL_IMAGE_4x3, exportProps.get(
                 VideoExport.ExportProperty.VIDEO_THUMBNAIL_IMAGE_4x3.name(),
                 VideoExport.ExportProperty.VIDEO_THUMBNAIL_IMAGE_4x3.valueClass()));
         
         assertEquals(VideoExport.ExportProperty.VIDEO_THUMBNAIL_IMAGE_16x9.name(), VIDEO_THUMBNAIL_IMAGE_16x9, exportProps.get(
                 VideoExport.ExportProperty.VIDEO_THUMBNAIL_IMAGE_16x9.name(),
                 VideoExport.ExportProperty.VIDEO_THUMBNAIL_IMAGE_16x9.valueClass()));
         
         assertEquals(VideoExport.ExportProperty.VIDEO_HOME.name(), VIDEO_HOME, exportProps.get(
                 VideoExport.ExportProperty.VIDEO_HOME.name(),
                 VideoExport.ExportProperty.VIDEO_HOME.valueClass()));
         
    }
}
