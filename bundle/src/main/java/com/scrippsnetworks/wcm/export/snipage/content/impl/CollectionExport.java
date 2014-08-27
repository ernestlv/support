package com.scrippsnetworks.wcm.export.snipage.content.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.collection.Collection;
import com.scrippsnetworks.wcm.collection.CollectionFactory;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * This class generates the Collection page specific properties.
 * 
 * @author Mallik Vamaraju Date: 9/23/13
 * @updated Venkata Naga Sudheer Donaboina: 11/21/2013
 */

public class CollectionExport extends SniPageExport {
	
	private static final Logger LOG = LoggerFactory
			.getLogger(CollectionExport.class);
	
	public enum ExportProperty {
		
		COLLECTION_TEXT(String[].class), COLLECTION_ASSETS(List.class);
		
		final Class clazz;
		
		ExportProperty(Class clazz) {
			this.clazz = clazz;
		}
		
		public Class valueClass() {
			return clazz;
		}
	}
	
	private final Collection collection;
	
	public CollectionExport(SniPage sniPage) {
		super(sniPage);
		this.collection = new CollectionFactory().withSniPage(sniPage).build();
		initialize();
	}
	
	protected CollectionExport(SniPage sniPage, Collection collection) {
		super(sniPage);
		this.collection = collection;
		initialize();
	}
	
	public void initialize() {
		
		LOG.debug("Begin : Initialize in Collection Exports... ");
		
		if (sniPage == null || !sniPage.hasContent() || collection == null) {
			return;
		}
		
		List<String> collectionText = collection.getCollectedText();
		
		if (collectionText != null && collectionText.size() > 0) {
			setProperty(ExportProperty.COLLECTION_TEXT.name(),
					collectionText.toArray(new String[collectionText.size()]));
		}

		setProperty(ExportProperty.COLLECTION_ASSETS.name(), collection.getCollectionAssetMapList());
		
		LOG.debug("End : Initialize in Collection Exports... ");
		
	}
}
