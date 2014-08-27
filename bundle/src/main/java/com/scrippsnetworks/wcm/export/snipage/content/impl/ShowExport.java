package com.scrippsnetworks.wcm.export.snipage.content.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.show.Show;
import com.scrippsnetworks.wcm.show.ShowFactory;

/**
 * This class generates the Show page specific properties.
 * 
 * @author Venkata Naga Sudheer Donaboina
 * 
 */
public class ShowExport extends SniPageExport {
	private static final Logger LOG = LoggerFactory.getLogger(ShowExport.class);

	private Show show;

	public ShowExport(SniPage sniPage) {
		super(sniPage);
		this.show = new ShowFactory().withSniPage(sniPage).build();
		initialize();
	}

	protected ShowExport(SniPage sniPage, Show show) {
		super(sniPage);
		this.show = show;
		initialize();
	}

	public enum ExportProperty {
		SHOW_FEATURE_IMAGE_BANNER_PATH(String.class);

		final Class clazz;

		ExportProperty(Class clazz) {
			this.clazz = clazz;
		}

		public Class valueClass() {
			return clazz;
		}
	}

	public void initialize() {
		LOG.debug("Started Show Export overrides");
		if (sniPage == null || !sniPage.hasContent() || show == null) {
			return;
		}
        
		SniImage sniImg = show.getFeatureBanner();
		if (sniImg != null) {
			setProperty(ExportProperty.SHOW_FEATURE_IMAGE_BANNER_PATH.name(),
					sniImg.getPath());
		}

		LOG.debug("Completed Show Export overrides");
	}
}
