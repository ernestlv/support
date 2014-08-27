package com.scrippsnetworks.wcm.mobile.secondaryinline.impl;

import java.util.List;

import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.mobile.imageicons.TransformIconClassToMobile;
import com.scrippsnetworks.wcm.mobile.secondary3imageacross.ImageAcross;
import com.scrippsnetworks.wcm.mobile.secondaryinline.SecondaryInlineVideo;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.video.Video;
import com.scrippsnetworks.wcm.video.channel.Channel;
import com.scrippsnetworks.wcm.video.channel.ChannelFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

public class SecondaryInlineVideoImpl implements SecondaryInlineVideo{
    private static final String MODULE_TITLE_PROP = "moduleTitle";
    private static final String CTA_LINK_PROP="ctaLink";
    private static final String VIDEO_CHANNEL_PATH_PROP="videoChannelPath"; 
    
    private String moduleTitle;
    private String ctaLink;
    private String videoChannelPath;
    private PageManager pageManager;
    private Channel channel;
    
    

    public SecondaryInlineVideoImpl(Resource resource){
        ValueMap vm = resource.adaptTo(ValueMap.class);
        moduleTitle = vm.get(MODULE_TITLE_PROP, "");
        ctaLink = vm.get(CTA_LINK_PROP, "");
        videoChannelPath = vm.get(VIDEO_CHANNEL_PATH_PROP, "");
        ResourceResolver rr = resource.getResourceResolver();
        pageManager = rr.adaptTo(PageManager.class);
    }

	@Override
	public Channel getChannel() {
		if(StringUtils.isNotBlank(videoChannelPath) && channel == null){
			SniPage videoChannelPage = PageFactory.getSniPage(pageManager, videoChannelPath);
			channel =  new ChannelFactory().withSniPage(videoChannelPage).build();
		}
		return channel;
	}
	
	@Override
	public String getCtaLink() {
		// TODO Auto-generated method stub
		return ctaLink;
	}
	
	@Override
	public String getModuleTitle() {
		// TODO Auto-generated method stub
		return moduleTitle;
	}
	
	@Override
	public String getVideoChannelPath() {
		// TODO Auto-generated method stub
		return videoChannelPath;
	}
}
