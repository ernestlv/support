package com.scrippsnetworks.wcm.export.snipage.content.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.photogallery.PhotoGallery;
import com.scrippsnetworks.wcm.photogallery.listing.PhotoGalleryListing;
import com.scrippsnetworks.wcm.photogallery.listing.PhotoGalleryListingFactory;

public class PhotoGalleryListingExport extends SniPageExport {
	
	private static final Logger LOG = LoggerFactory.getLogger(PhotoGalleryListingExport.class);

	public enum ExportProperty {

		PHOTOGALLERYLISTING_PHOTOGALLERIES(String[].class);

		final Class clazz;

		ExportProperty(Class clazz) {
			this.clazz = clazz;
		}

		public Class valueClass() {
			return clazz;
		}
	}

	private final PhotoGalleryListing photoGalleryListing;

	public PhotoGalleryListingExport(SniPage sniPage) {
		super(sniPage);
		this.photoGalleryListing = new PhotoGalleryListingFactory().withSniPage(sniPage)
				.build();
		initialize();
	}

	protected PhotoGalleryListingExport(SniPage sniPage, PhotoGalleryListing photoGalleryListing) {
		super(sniPage);
		this.photoGalleryListing = photoGalleryListing;
		initialize();
	}

	public void initialize() {

		LOG.debug("Started PhotoGallery Export overrides");

		if (sniPage == null || !sniPage.hasContent() || photoGalleryListing == null) {
			return;
		}

		List<PhotoGallery> photoGalleries = photoGalleryListing.getPhotoGalleries();
		if (photoGalleries != null) {
			List<String> photoGalleryIds = new ArrayList<String>();
			SniPage photoGalleryPage = null;
			for (PhotoGallery photoGallery : photoGalleries) {
				photoGalleryPage = photoGallery.getPage();
				if (photoGalleryPage != null) {
					photoGalleryIds.add(photoGalleryPage.getUid());
				}
			}
			if (photoGalleryIds.size() > 0) {
				setProperty(ExportProperty.PHOTOGALLERYLISTING_PHOTOGALLERIES.name(), photoGalleryIds.toArray(new String[photoGalleryIds.size()]));
			}
		}
		
		LOG.debug("Completed PhotoGalleryListing Export overrides");

	}

}
