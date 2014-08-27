package com.scrippsnetworks.wcm.video;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.video.impl.VideoImpl;

/**
 * @author Jason Clark
 *         Date: 5/10/13
 */
public class VideoFactory {

    /** SniPage passed into factory. */
    private SniPage sniPage;

    /** Return a new instance of a Video. */
    public Video build() {
        if (sniPage != null) {
            return new VideoImpl(sniPage);
        }
        return null;
    }

    /** Add an SniPage to this builder. */
    public VideoFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }
}
