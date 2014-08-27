package com.scrippsnetworks.wcm.export.snipage.content.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.menu.Menu;
import com.scrippsnetworks.wcm.menu.MenuFactory;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * This class generates the Menu page specific properties.
 * 
 * @author Venkata Naga Sudheer Donaboina
 */
public class MenuExport extends SniPageExport {
	private static final Logger LOG = LoggerFactory.getLogger(MenuExport.class);

	public enum ExportProperty {

		MENU_MEALTYPE_RECIPES(String.class);

		final Class clazz;

		ExportProperty(Class clazz) {
			this.clazz = clazz;
		}

		public Class valueClass() {
			return clazz;
		}
	}

	private final Menu menu;

	public MenuExport(SniPage sniPage) {
		super(sniPage);
		this.menu = new MenuFactory().withSniPage(sniPage).build();
		initialize();
	}

	protected MenuExport(SniPage sniPage, Menu menu) {
		super(sniPage);
		this.menu = menu;
		initialize();
	}

	public void initialize() {

		LOG.debug("Started Menu Export overrides");

		if (sniPage == null || !sniPage.hasContent() || menu == null) {
			return;
		}

		setProperty(ExportProperty.MENU_MEALTYPE_RECIPES.name(), menu.getMealTypeRecipes());

		LOG.debug("Completed Menu Export overrides");

	}
}
