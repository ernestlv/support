package com.scrippsnetworks.wcm.export.snipage.content.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.article.Article;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * This class is used for testing Article Exports.
 * 
 * @author Venkata Naga Sudheer Donaboina
 */
public class ArticleExportTest {

	public static final String PAGE_PATH = "/content/food/article/a-article";
	public static final String PAGE_TYPE = "article";

	public static final String ARTICLE_BYLINE = "Article Byline text";

	public static final String ARTICLE_BODY = "Testing Article Body";

	@Mock
	SniPage articlePage;
	@Mock
	Article article;

	@Mock
	Resource articlePageCR;
	@Mock
	ValueMap articlePageProperties;

	@Mock
	PageManager pageManager;
	@Mock
	ResourceResolver resourceResolver;

	@Before
	public void before() {

		MockitoAnnotations.initMocks(this);

		when(articlePage.hasContent()).thenReturn(true);
		when(articlePage.getProperties()).thenReturn(articlePageProperties);
		when(articlePage.getContentResource()).thenReturn(articlePageCR);
		when(articlePage.getPath()).thenReturn(PAGE_PATH);
		when(articlePage.getPageType()).thenReturn(PAGE_TYPE);
		when(articlePage.getPageManager()).thenReturn(pageManager);

		when(article.getByLine()).thenReturn(ARTICLE_BYLINE);

		when(article.getBody()).thenReturn(ARTICLE_BODY);

	}

	@Test
	public void testArticlePropertyValues() {
		ArticleExport companyExport = new ArticleExport(articlePage, article);
		ValueMap exportProps = companyExport.getValueMap();

		assertEquals(ArticleExport.ExportProperty.ARTICLE_BYLINE.name(),
				ARTICLE_BYLINE, exportProps.get(
						ArticleExport.ExportProperty.ARTICLE_BYLINE.name(),
						ArticleExport.ExportProperty.ARTICLE_BYLINE
								.valueClass()));

		assertEquals(ArticleExport.ExportProperty.ARTICLE_BODY.name(), ARTICLE_BODY, exportProps.get(
						ArticleExport.ExportProperty.ARTICLE_BODY.name(),
						ArticleExport.ExportProperty.ARTICLE_BODY.valueClass()));

	}

}
