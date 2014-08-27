package com.scrippsnetworks.wcm.video.channel;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.video.channel.impl.ChannelImpl;

/**
 * @author Jason Clark
 *         Date: 5/10/13
 */
public class ChannelFactory {

    /** SniPage to build the Channel from. */
    private SniPage sniPage;

    /** Build a new Channel. */
    public Channel build() {
        return new ChannelImpl(sniPage);
    }

    /** Add an SniPage to this builder. */
    public ChannelFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }

}
