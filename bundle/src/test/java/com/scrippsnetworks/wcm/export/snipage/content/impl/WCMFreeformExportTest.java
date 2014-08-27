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
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.wcmfreeform.WCMFreeform;

public class WCMFreeformExportTest {
	public static final String PAGE_PATH = "/content/food/mobile/a-wcm-freeform-page";
	public static final String PAGE_TYPE = "wcm-freeform";
	public static final String WCM_FREE_FORM_ADHOC_TEXT = "eut',.dust'n,husnt,.h'snoaehtj'oes nhtu t',ehu', hu',.";

	@Mock
	SniPage wcmFreeformPage;
	@Mock
	WCMFreeform wcmFreeForm;

	@Mock
	Resource wcmFreeFormPageCR;
	@Mock
	ValueMap wcmFreeFormPageProperties;

	@Mock
	PageManager pageManager;
	@Mock
	ResourceResolver resourceResolver;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(wcmFreeformPage.hasContent()).thenReturn(true);
		when(wcmFreeformPage.getProperties()).thenReturn(wcmFreeFormPageProperties);
		when(wcmFreeformPage.getContentResource()).thenReturn(wcmFreeFormPageCR);
		when(wcmFreeformPage.getPath()).thenReturn(PAGE_PATH);
		when(wcmFreeformPage.getPageType()).thenReturn(PAGE_TYPE);

		when(wcmFreeformPage.getPageManager()).thenReturn(pageManager);

		when(wcmFreeForm.getAdhocText()).thenReturn(WCM_FREE_FORM_ADHOC_TEXT);
	}

	@Test
	public void testCompanyPropertyValues() {
		WCMFreeFormExport wcmFreeFormExport = new WCMFreeFormExport(
				wcmFreeformPage, wcmFreeForm);
		ValueMap exportProps = wcmFreeFormExport.getValueMap();

		assertEquals(
				WCMFreeFormExport.ExportProperty.WCM_FREEFORM_ADHOC_TEXT.name(),
				WCM_FREE_FORM_ADHOC_TEXT,
				exportProps.get(WCMFreeFormExport.ExportProperty.WCM_FREEFORM_ADHOC_TEXT.name(),
								WCMFreeFormExport.ExportProperty.WCM_FREEFORM_ADHOC_TEXT.valueClass()));

	}
}
