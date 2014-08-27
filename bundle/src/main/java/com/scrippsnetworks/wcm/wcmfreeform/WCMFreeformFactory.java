package com.scrippsnetworks.wcm.wcmfreeform;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.wcmfreeform.impl.WCMFreeformImpl;

public class WCMFreeformFactory {

	private SniPage sniPage;

	/**
	 * Can return null
	 * 
	 * @return
	 */
	public WCMFreeform build() {
		if (sniPage != null) {
			return new WCMFreeformImpl(sniPage);
		}
		return null;
	}

	/**
	 * Add SniPage to WCMFreeform Builder
	 * 
	 * @param page
	 *            SniPage in hand
	 * @return this FreeFormTextFactory
	 */
	public WCMFreeformFactory withSniPage(SniPage sniPage) {
		this.sniPage = sniPage;
		return this;
	}
}
