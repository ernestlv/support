package com.scrippsnetworks.wcm.video.player;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.video.channel.Channel;

import java.util.List;

/**
 * A Player contains Channels.
 * This has methods for retrieving those Channels.
 * @author Jason Clark
 *         Date: 5/10/13
 */
public interface Player {

    /** Get a List of the Channels this Player contains. */
    public List<Channel> getChannels();

    /** Return the first Channel from this Player. */
    public Channel getFirstChannel();
    
    /** SniPage for the video player. */
    public SniPage getSniPage();


}
