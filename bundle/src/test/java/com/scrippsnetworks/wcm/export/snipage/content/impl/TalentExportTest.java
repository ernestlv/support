package com.scrippsnetworks.wcm.export.snipage.content.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.talent.Talent;

/**
 * This class is used for testing Talent Exports.
 * @author Venkata Naga Sudheer Donaboina
 *
 */
public class TalentExportTest {
	public static final String PAGE_PATH = "/content/food/chefs/a-talent";
	public static final String PAGE_TYPE = "talent";

	public static final String PAGE_TALENT_AVATAR_IMAGE_PATH = "http://images.scrippsnetworks.com/up/images/0193/0193_92x69.jpg";
	public static final String PAGE_TALENT_BANNER_IMAGE_PATH = "http://images.scrippsnetworks.com/up/images/0193/0193921_92x69.jpg";
	public static final String[] PAGE_TALENT_IMAGE_PATHS = {
            "/content/dam/images/food/fullset/2010/3/1/0/Talentmaster_Bio-Anne-Burrell-2010-4_s4x3.jpg",
            "/content/dam/images/food/fullset/2010/3/1/0/Talentmaster_Bio-Anne-Burrell-2010-5_s4x3.jpg"
    };

	
	@Mock SniImage avatarImage;
	
	@Mock SniImage bannerImage;

	@Mock ArrayList<SniImage> talentImages;

	@Mock
	SniPage talentPage;
	@Mock
	Talent talent;

	@Mock
	Resource talentPageCR;
	@Mock
	ValueMap talentPageProperties;

	@Mock
	PageManager pageManager;
	@Mock
	ResourceResolver resourceResolver;

	@Before
	public void before() {

		MockitoAnnotations.initMocks(this);

		when(talentPage.hasContent()).thenReturn(true);
		when(talentPage.getProperties()).thenReturn(talentPageProperties);
		when(talentPage.getContentResource()).thenReturn(talentPageCR);
		when(talentPage.getPath()).thenReturn(PAGE_PATH);
		when(talentPage.getPageType()).thenReturn(PAGE_TYPE);

        when(talent.getImagePaths()).thenReturn(PAGE_TALENT_IMAGE_PATHS);

		when(talentPage.getPageManager()).thenReturn(pageManager);

		when(talent.getBannerImage()).thenReturn(bannerImage);
		when(talent.getCanonicalImage()).thenReturn(avatarImage);
		when(talent.getTalentImages()).thenReturn(talentImages);

        for (int i=0;i<talentImages.size();i++) {
            when(talentImages.get(i).getPath()).thenReturn(PAGE_TALENT_IMAGE_PATHS[i]);
        }

		when(bannerImage.getPath()).thenReturn(PAGE_TALENT_BANNER_IMAGE_PATH);
		
		when(avatarImage.getPath()).thenReturn(PAGE_TALENT_AVATAR_IMAGE_PATH);

	}


	@Test
	public void testTalentPropertyValues() {
		TalentExport talentExport = new TalentExport(talentPage, talent);
		ValueMap exportProps = talentExport.getValueMap();

		assertEquals(TalentExport.ExportProperty.TALENT_AVATAR_IMAGE_PATH.name(), PAGE_TALENT_AVATAR_IMAGE_PATH, 
				exportProps.get(TalentExport.ExportProperty.TALENT_AVATAR_IMAGE_PATH.name(),
						TalentExport.ExportProperty.TALENT_AVATAR_IMAGE_PATH.valueClass()));
		
		assertEquals(TalentExport.ExportProperty.TALENT_BANNER_IMAGE_PATH.name(), PAGE_TALENT_BANNER_IMAGE_PATH, 
				exportProps.get(TalentExport.ExportProperty.TALENT_BANNER_IMAGE_PATH.name(),
						TalentExport.ExportProperty.TALENT_BANNER_IMAGE_PATH.valueClass()));

        assertEquals(TalentExport.ExportProperty.TALENT_IMAGES_PATHS.name(), PAGE_TALENT_IMAGE_PATHS,
                exportProps.get(TalentExport.ExportProperty.TALENT_IMAGES_PATHS.name(),
                        TalentExport.ExportProperty.TALENT_IMAGES_PATHS.valueClass()));

	}

}
