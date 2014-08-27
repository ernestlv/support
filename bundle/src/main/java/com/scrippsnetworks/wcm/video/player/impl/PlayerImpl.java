package com.scrippsnetworks.wcm.video.player.impl;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.video.channel.Channel;
import com.scrippsnetworks.wcm.video.channel.ChannelFactory;
import com.scrippsnetworks.wcm.video.player.Player;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Video Player. Contains a List of Channels.
 * 
 * @author Jason Clark Date: 5/10/13
 */
public class PlayerImpl implements Player {
	
	private static final String SNI_CHANNELS = "sni:channels";
	
	/** First Channel in the List. */
	private Channel firstChannel;
	
	/** All Channels in this Player. */
	private List<Channel> channels;
	
	/** SniPage used to construct this Player. */
	private SniPage sniPage;
	
	/** Construct a new Player given an SniPage. */
	public PlayerImpl(final SniPage page) {
		this.sniPage = page;
	}
	
	/** {@inheritDoc} */
	@Override
	public List<Channel> getChannels() {
		if (channels == null) {
            channels = new ArrayList<Channel>();
			Resource resource = sniPage.adaptTo(Resource.class);
			ValueMap pageProperties = sniPage.getProperties();
			if (pageProperties != null
					&& pageProperties.containsKey(SNI_CHANNELS)) {
				String[] sniChannels = pageProperties.get(SNI_CHANNELS,
						String[].class);
				if (sniChannels != null) {
					for (String channelPath : sniChannels) {
						if (!StringUtils.isEmpty(channelPath)) {
							ResourceResolver rr = resource
									.getResourceResolver();
							Resource res = rr.getResource(channelPath);
							if(res != null) {
								SniPage channelPage = PageFactory.getSniPage(res
										.adaptTo(Page.class));
								Channel channel = new ChannelFactory().withSniPage(
										channelPage).build();
								if (channel != null) {
									channels.add(channel);
								}
							}
						}
					}
				}
			}
		}
		
		return channels;
	}
	
	/** {@inheritDoc} */
	@Override
	public Channel getFirstChannel() {
		if (firstChannel == null) {
			List<Channel> theChannels = getChannels();
			if (theChannels.size() > 0) {
				firstChannel = theChannels.get(0);
			}
		}
		
		return firstChannel;
	}
	
	/** {@inheritDoc} */
	@Override
	public SniPage getSniPage() {
		return sniPage;
	}
	
}
