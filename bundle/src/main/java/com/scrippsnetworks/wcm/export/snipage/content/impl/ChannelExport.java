package com.scrippsnetworks.wcm.export.snipage.content.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.video.Video;
import com.scrippsnetworks.wcm.video.channel.Channel;
import com.scrippsnetworks.wcm.video.channel.ChannelFactory;

/**
 * This class generates the Video Channel page specific properties.
 * @author Venkata Naga Sudheer Donaboina
 *
 */
public class ChannelExport extends SniPageExport {
	

    private static final Logger LOG = LoggerFactory.getLogger(ChannelExport.class);

    private Channel channel;

    public ChannelExport(SniPage sniPage) {
        super(sniPage);
        this.channel = new ChannelFactory().withSniPage(sniPage).build();
        initialize();
    }

    protected ChannelExport(SniPage sniPage, Channel channel) {
        super(sniPage);
        this.channel = channel;
        initialize();
    }

    public enum ExportProperty {
        CHANNEL_VIDEOS(String[].class);

        final Class clazz;

        ExportProperty(Class clazz) {
            this.clazz = clazz;
        }

        public Class valueClass() {
            return clazz;
        }
    }

    public void initialize() {
        LOG.debug("Started Channel Export overrides");
        if (sniPage == null || !sniPage.hasContent() || channel == null) {
            return;
        }
        
        List<Video> videos = channel.getVideos();
        if(videos != null) {
        	List<String> videoIdsList = new ArrayList<String>();
        	SniPage videoPage = null;
        	for(Video video : videos) {
        		videoPage = video.getSniPage();
        		if(videoPage != null) {
        			videoIdsList.add(videoPage.getUid());
        		}
        	}
        	if(videoIdsList.size() > 0) {
        		setProperty(ExportProperty.CHANNEL_VIDEOS.name(), videoIdsList.toArray(new String[videoIdsList.size()]));
        	}
        }
        
        
        LOG.debug("Completed Channel Export overrides");
    }

}
