package com.scrippsnetworks.wcm.export.snipage.content.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.article.Article;
import com.scrippsnetworks.wcm.article.ArticleFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.util.StringUtil;

/**
 * This class generates the Article page specific properties.
 * 
 * @author Venkata Naga Sudheer Donaboina Date: 9/12/13
 */
public class ArticleExport extends SniPageExport {

	private static final Logger LOG = LoggerFactory
			.getLogger(ArticleExport.class);

	private Article article;

	public ArticleExport(SniPage sniPage) {
		super(sniPage);
		this.article = new ArticleFactory().withSniPage(sniPage).build();
		initialize();
	}

	protected ArticleExport(SniPage sniPage, Article article) {
		super(sniPage);
		this.article = article;
		initialize();
	}

	public enum ExportProperty {
		ARTICLE_BYLINE(String.class), ARTICLE_BODY(String.class);

		final Class clazz;

		ExportProperty(Class clazz) {
			this.clazz = clazz;
		}

		public Class valueClass() {
			return clazz;
		}
	}

	public void initialize() {
		LOG.debug("Started Article Export overrides");
		if (sniPage == null || !sniPage.hasContent() || article == null) {
			return;
		}
		setProperty(ExportProperty.ARTICLE_BYLINE.name(), article.getByLine());
		setProperty(ExportProperty.ARTICLE_BODY.name(), StringUtil.cleanToPlainText(article.getBody()));
		LOG.debug("Completed Article Export overrides");
	}

}
