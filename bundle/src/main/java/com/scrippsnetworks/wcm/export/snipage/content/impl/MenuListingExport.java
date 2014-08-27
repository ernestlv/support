package com.scrippsnetworks.wcm.export.snipage.content.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.menu.Menu;
import com.scrippsnetworks.wcm.menu.listing.MenuListing;
import com.scrippsnetworks.wcm.menu.listing.MenuListingFactory;
import com.scrippsnetworks.wcm.page.SniPage;

public class MenuListingExport extends SniPageExport {

	private static final Logger LOG = LoggerFactory.getLogger(MenuListingExport.class);

	public enum ExportProperty {

		MENULISTING_MENUS(String[].class);

		final Class clazz;

		ExportProperty(Class clazz) {
			this.clazz = clazz;
		}

		public Class valueClass() {
			return clazz;
		}
	}

	private final MenuListing menuListing;

	public MenuListingExport(SniPage sniPage) {
		super(sniPage);
		this.menuListing = new MenuListingFactory().withSniPage(sniPage)
				.build();
		initialize();
	}

	protected MenuListingExport(SniPage sniPage, MenuListing menuListing) {
		super(sniPage);
		this.menuListing = menuListing;
		initialize();
	}

	public void initialize() {

		LOG.debug("Started MenuListing Export overrides");

		if (sniPage == null || !sniPage.hasContent() || menuListing == null) {
			return;
		}

		List<Menu> menus = menuListing.getMenus();
		if (menus != null) {
			List<String> menuIds = new ArrayList<String>();
			SniPage menuPage = null;
			for (Menu menu : menus) {
				menuPage = menu.getSniPage();
				if (menuPage != null) {
					menuIds.add(menuPage.getUid());
				}
			}
			if (menuIds.size() > 0) {
				setProperty(ExportProperty.MENULISTING_MENUS.name(), menuIds.toArray(new String[menuIds.size()]));
			}
		}
		
		LOG.debug("Completed MenuListing Export overrides");

	}
}
