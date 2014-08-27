package com.scrippsnetworks.wcm.video.player;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.video.player.impl.PlayerImpl;

/**
 * @author Jason Clark
 *         Date: 5/10/13
 */
public class PlayerFactory {

    private SniPage sniPage;

    public Player build() {
        return new PlayerImpl(sniPage);
    }

    public PlayerFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }
}
