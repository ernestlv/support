package com.scrippsnetworks.wcm.socialtoolbar;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.socialtoolbar.SocialToolbar;
import com.scrippsnetworks.wcm.socialtoolbar.impl.SocialToolbarImpl;

/**
 * 
 * @author Mallik Vamaraju Date: 09/04/13
 */
public class SocialToolbarFactory {

	private SniPage sniPage;

	public SocialToolbar build() {
		if (sniPage != null) {
			return new SocialToolbarImpl(sniPage);
		}
		return null;

	}

	public SocialToolbarFactory withSniPage(SniPage sniPage) {
		this.sniPage = sniPage;
		return this;
	}

}
