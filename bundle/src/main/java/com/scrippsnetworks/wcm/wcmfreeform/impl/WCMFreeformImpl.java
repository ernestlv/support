package com.scrippsnetworks.wcm.wcmfreeform.impl;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.wcmfreeform.WCMFreeform;

public class WCMFreeformImpl implements WCMFreeform {

	private String adhocText;

	/** ValueMap of properties merged from free form page and asset. */
	private ValueMap wcmFreeformProperties;

	/** ResourceResolver for convenience. */
	private ResourceResolver resourceResolver;

	/** Resource for convenience, because you need a Resource from time to time. */
	private Resource resource;

	private SniPage sniPage;

	private static final String SNI_ADHOC_TEXT = "sni:adhocText";

	public WCMFreeformImpl(final SniPage sniPage) {
		this.sniPage = sniPage;
		this.wcmFreeformProperties = sniPage.getProperties();
		Resource resource = sniPage.getContentResource();
		if (resource != null) {
			resourceResolver = resource.getResourceResolver();
		}

	}

	@Override
	public String getAdhocText() {
		if (adhocText == null) {
			if (wcmFreeformProperties != null
					&& wcmFreeformProperties.containsKey(SNI_ADHOC_TEXT)) {
				adhocText = wcmFreeformProperties.get(SNI_ADHOC_TEXT,
						String.class);
			}
		}
		return adhocText;
	}
	
	public SniPage getSniPage()
	{
		return sniPage;
	}

}
