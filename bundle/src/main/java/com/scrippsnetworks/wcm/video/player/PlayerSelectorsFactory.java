/**
 * 
 */
package com.scrippsnetworks.wcm.video.player;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.video.player.impl.PlayerSelectorsImpl;

/**
 * @author Mallik Vamaraju
 * 
 */
public class PlayerSelectorsFactory {
	
	private SniPage sniPage;
	
	private String pagePath;
	
	
	public PlayerSelectorsImpl withSniPageSelectors(SniPage sniPage) {
		this.sniPage = sniPage;
		return new PlayerSelectorsImpl(sniPage);
	}
	
	public PlayerSelectorsImpl withSniPagePath(String pagePath) {
		this.pagePath = pagePath;
		return new PlayerSelectorsImpl(pagePath);
	
	}
}
