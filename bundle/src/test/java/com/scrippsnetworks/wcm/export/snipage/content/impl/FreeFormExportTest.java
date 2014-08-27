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
import com.scrippsnetworks.wcm.freeform.FreeForm;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * This class is used for testing FreeForm Exports.
 * 
 * @author Venkata Naga Sudheer Donaboina
 */
public class FreeFormExportTest {

	public static final String PAGE_PATH = "/content/food/free-form";
	public static final String PAGE_TYPE = "free-form-text";
	public static final String FREE_FORM_BODY = "Test body content";

	@Mock
	SniPage freeFormPage;
	@Mock
	FreeForm freeForm;

	@Mock
	Resource freeFormPageCR;
	@Mock
	ValueMap freeFormPageProperties;

	@Mock
	PageManager pageManager;
	@Mock
	ResourceResolver resourceResolver;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(freeFormPage.hasContent()).thenReturn(true);
		when(freeFormPage.getProperties()).thenReturn(freeFormPageProperties);
		when(freeFormPage.getContentResource()).thenReturn(freeFormPageCR);
		when(freeFormPage.getPath()).thenReturn(PAGE_PATH);
		when(freeFormPage.getPageType()).thenReturn(PAGE_TYPE);

		when(freeFormPage.getPageManager()).thenReturn(pageManager);

		when(freeForm.getBody()).thenReturn(FREE_FORM_BODY);
	}

	@Test
	public void testCompanyPropertyValues() {
		FreeFormExport freeFormExport = new FreeFormExport(freeFormPage,
				freeForm);
		ValueMap exportProps = freeFormExport.getValueMap();

		assertEquals(FreeFormExport.ExportProperty.FREE_FORM_BODY.name(),
				FREE_FORM_BODY, exportProps.get(
						FreeFormExport.ExportProperty.FREE_FORM_BODY.name(),
						FreeFormExport.ExportProperty.FREE_FORM_BODY
								.valueClass()));

	}

}
