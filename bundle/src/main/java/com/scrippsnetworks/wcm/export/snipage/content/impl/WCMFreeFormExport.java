package com.scrippsnetworks.wcm.export.snipage.content.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.util.StringUtil;
import com.scrippsnetworks.wcm.wcmfreeform.WCMFreeform;
import com.scrippsnetworks.wcm.wcmfreeform.WCMFreeformFactory;

public class WCMFreeFormExport extends SniPageExport {
	
	private static final Logger LOG = LoggerFactory
			.getLogger(WCMFreeFormExport.class);
	 
	private WCMFreeform wcmFreeForm;
	
	public WCMFreeFormExport(SniPage sniPage) {
		super(sniPage);
		this.wcmFreeForm = new WCMFreeformFactory().withSniPage(sniPage).build();
		initialize();
	}
	
	protected WCMFreeFormExport(SniPage sniPage, WCMFreeform wcmFreeform) {
		super(sniPage);
		this.wcmFreeForm = wcmFreeform;
		initialize();
	}
	
	public enum ExportProperty {
		WCM_FREEFORM_ADHOC_TEXT(String.class);
		
		final Class clazz;
		
		ExportProperty(Class clazz) {
			this.clazz = clazz;
		}
		
		public Class valueClass() {
			return clazz;
		}
	}
	
	public void initialize() {
		LOG.debug("Started Wcm Free Form Export overrides");
		if (sniPage == null || !sniPage.hasContent() || wcmFreeForm == null) {
			return;
		}
		setProperty(ExportProperty.WCM_FREEFORM_ADHOC_TEXT.name(), StringUtil.cleanToPlainText(wcmFreeForm.getAdhocText()));
		LOG.debug("Completed WCM Free Form Export overrides");
	}

}
