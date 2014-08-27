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
import com.scrippsnetworks.wcm.video.Video;
import com.scrippsnetworks.wcm.video.channel.Channel;

/**
 * This class is used for testing Channel Exports.
 * @author Venkata Naga Sudheer Donaboina
 */
public class ChannelExportTest {

	public static final String PAGE_PATH = "/content/food/video/channels/a-video-channel";
	public static final String PAGE_TYPE = "video-channel";

	public static final String VIDEO_PAGE1_UID = "aaaa-bbbb-cccc-dddd";
    public static final String VIDEO_PAGE2_UID = "aaaa-cccc-bbbb-dddd";
	
	
	List<Video> videosList;

	@Mock
	SniPage channelPage;
	@Mock
	Channel channel;
	
	@Mock Video video1;
	@Mock Video video2;
	
	@Mock SniPage videoPage1;
	@Mock SniPage videoPage2;

	@Mock
	Resource channelPageCR;
	@Mock
	ValueMap channelPageProperties;
	@Mock
	ValueMap videoShowPageProperties;

	@Mock
	PageManager pageManager;
	@Mock
	ResourceResolver resourceResolver;

	@Before
	public void before() {

		MockitoAnnotations.initMocks(this);

		when(channelPage.hasContent()).thenReturn(true);
		when(channelPage.getProperties()).thenReturn(channelPageProperties);
		when(channelPage.getContentResource()).thenReturn(channelPageCR);
		when(channelPage.getPath()).thenReturn(PAGE_PATH);
		when(channelPage.getPageType()).thenReturn(PAGE_TYPE);
		when(channelPage.getPageManager()).thenReturn(pageManager);
		when(channel.getSniPage()).thenReturn(channelPage);

	}

	/** set up episode pages, episode page uid rlated to companies. */
    private void setupVideos() {
        videosList = Arrays.asList(video1, video2);
        when(channel.getVideos()).thenReturn(videosList);
        
        when(video1.getSniPage()).thenReturn(videoPage1);
        when(video2.getSniPage()).thenReturn(videoPage2);
        
        when(videoPage1.getUid()).thenReturn(VIDEO_PAGE1_UID);
        when(videoPage2.getUid()).thenReturn(VIDEO_PAGE2_UID);
    }
    
    @Test
    public void testChanelVideos() {
    	setupVideos();
    	ChannelExport channelExport = new ChannelExport(channelPage, channel);
		ValueMap exportProps = channelExport.getValueMap();
		
		String[] videos = exportProps.get(ChannelExport.ExportProperty.CHANNEL_VIDEOS.name(), String[].class);

        assertEquals(ChannelExport.ExportProperty.CHANNEL_VIDEOS.name(), videosList.size(), videos.length);

        int i = 0;
        for (Video video : channel.getVideos()) {
            assertEquals("Video Id ", video.getSniPage().getUid(), videos[i++]);
        }

    }
}
