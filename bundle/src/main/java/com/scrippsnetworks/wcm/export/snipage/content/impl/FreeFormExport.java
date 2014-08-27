package com.scrippsnetworks.wcm.export.snipage.content.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.freeform.FreeForm;
import com.scrippsnetworks.wcm.freeform.FreeFormFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.util.StringUtil;

/**
 * This class generates the Freeform page specific properties.
 * 
 * @author Venkata Naga Sudheer Donaboina
 */
public class FreeFormExport extends SniPageExport {
	
	private static final Logger LOG = LoggerFactory
			.getLogger(FreeFormExport.class);
	
	private FreeForm freeForm;
	
	public FreeFormExport(SniPage sniPage) {
		super(sniPage);
		this.freeForm = new FreeFormFactory().withSniPage(sniPage).build();
		initialize();
	}
	
	protected FreeFormExport(SniPage sniPage, FreeForm freeForm) {
		super(sniPage);
		this.freeForm = freeForm;
		initialize();
	}
	
	public enum ExportProperty {
		FREE_FORM_BODY(String.class);
		
		final Class clazz;
		
		ExportProperty(Class clazz) {
			this.clazz = clazz;
		}
		
		public Class valueClass() {
			return clazz;
		}
	}
	
	public void initialize() {
		LOG.debug("Started Free Form Export overrides");
		if (sniPage == null || !sniPage.hasContent() || freeForm == null) {
			return;
		}
		setProperty(ExportProperty.FREE_FORM_BODY.name(), StringUtil.cleanToPlainText(freeForm.getBody()));
		LOG.debug("Completed Free Form Export overrides");
	}
	
}
