package com.scrippsnetworks.wcm.socialtoolbar;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.socialtoolbar.impl.TwitterMessageImpl;

/**
 * 
 * @author Mallik Vamaraju Date: 09/16/13
 */
public class TwitterMessageFactory {

	private SniPage sniPage;

	public TwitterMessage build() {
		if (sniPage != null) {
			return new TwitterMessageImpl(sniPage);
		}
		return null;

	}

	public TwitterMessageFactory withSniPage(SniPage sniPage) {
		this.sniPage = sniPage;
		return this;
	}

}
