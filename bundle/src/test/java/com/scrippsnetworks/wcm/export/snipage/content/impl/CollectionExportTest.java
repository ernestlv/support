package com.scrippsnetworks.wcm.export.snipage.content.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.collection.Collection;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * This class is used for testing Collection Exports.
 * 
 * @author Mallik Vamaraju
 * @updated Venkata Naga Sudheer Donaboina: 11/21/2013
 */
public class CollectionExportTest {
	
	private Logger LOG = LoggerFactory.getLogger(CollectionExportTest.class);
	
	public static final String PAGE_PATH = "/content/food/mobile-app/testCollection1";
	public static final String PAGE_PATH_COLLECTION = "/content/food/mobile-app/testCollection2";
	public static final String PAGE_TYPE = "wcm-collection";
	
	public static final String PAGE_TYPE_ARTICLE = "article";
	
	public static final String PAGE_TYPE_IMAGE = "image";
	
	public static final String ASSET_PAGE1_UID = "aaaa-bbbb-cccc-dddd";
	public static final String ASSET_PAGE2_UID = "aaaa-cccc-bbbb-dddd";
	public static final String ASSET_IMAGE_PATH = "/content/dam/images/food/fullset/2013/9/30/0/ITK_alton-brown-fns8_s3x4.jpg";
	
	public static final String TEXT_VALUE1 = "item1|value1";
	
	public static final String TEXT_VALUE2 = "item|value2";
	
	@Mock
	SniPage collectionPage;
	@Mock
	Collection collection;
	
	@Mock
	ValueMap collectionPageProperties;
	
	@Mock
	Map<String, String> collectionAssetMap = new HashMap<String, String>();
	
	@Mock List<Map<String, String>> collectionAssetMapList = new ArrayList<Map<String, String>>();
	
	@Mock
	PageManager pageManager;
	@Mock
	ResourceResolver resourceResolver;
	
	List<String> textValues = Arrays.asList(TEXT_VALUE1, TEXT_VALUE2);
	
	List<String> collectionAssetValues = Arrays.asList(ASSET_PAGE1_UID, ASSET_PAGE1_UID, ASSET_IMAGE_PATH);
	
	
	@Before
	public void before() {
		
		MockitoAnnotations.initMocks(this);
		
		when(collectionPage.hasContent()).thenReturn(true);
		when(collectionPage.getProperties()).thenReturn(
				collectionPageProperties);
		when(collectionPage.getPath()).thenReturn(PAGE_PATH);
		when(collectionPage.getPageType()).thenReturn(PAGE_TYPE);
		
		when(collectionPage.getPageManager()).thenReturn(pageManager);
		
	}
	
	/** set up collection text values related to collection. */
	private void setupCollectionTextValues() {
		when(collection.getCollectedText()).thenReturn(textValues);
	}
	
	private void setupCollectionAssetMapList() {
		collectionAssetMapList = new ArrayList<Map<String, String>>();
		
		collectionAssetMap = new HashMap<String, String>();
		collectionAssetMap.put("PAGETYPE", PAGE_TYPE);
		collectionAssetMap.put("path", PAGE_PATH_COLLECTION);
		collectionAssetMap.put("content", ASSET_PAGE1_UID);
		collectionAssetMapList.add(collectionAssetMap);
		
		collectionAssetMap = new HashMap<String, String>();
		collectionAssetMap.put("PAGETYPE", PAGE_TYPE_ARTICLE);
		collectionAssetMap.put("path", PAGE_PATH_COLLECTION);
		collectionAssetMap.put("content", ASSET_PAGE2_UID);
		collectionAssetMapList.add(collectionAssetMap);
		
		collectionAssetMap = new HashMap<String, String>();
		collectionAssetMap.put("PAGETYPE", PAGE_TYPE_IMAGE);
		collectionAssetMap.put("path", ASSET_IMAGE_PATH);
		collectionAssetMapList.add(collectionAssetMap);
		
		when(collection.getCollectionAssetMapList()).thenReturn(collectionAssetMapList);
	}
	
	@Test
	public void testCollectionAssetMapList() {
		setupCollectionAssetMapList();
		CollectionExport collectionExport = new CollectionExport(
				collectionPage, collection);
		ValueMap exportProps = collectionExport.getValueMap();
		List<Map<String, String>> assetMapList = exportProps.get(
				CollectionExport.ExportProperty.COLLECTION_ASSETS.name(),
				List.class);
		LOG.info("assetMapList is :: " + assetMapList);
		LOG.info("collectionAssetMapList is :: " + collectionAssetMapList);
		assertEquals(CollectionExport.ExportProperty.COLLECTION_ASSETS.name(), collectionAssetMapList.size(), assetMapList.size());
	}
	
	@Test
	public void testCollectionText() {
		setupCollectionTextValues();
		CollectionExport collectionExport = new CollectionExport(
				collectionPage, collection);
		ValueMap exportProps = collectionExport.getValueMap();
		
		String[] values = exportProps.get(
				CollectionExport.ExportProperty.COLLECTION_TEXT.name(),
				String[].class);
		
		assertEquals(CollectionExport.ExportProperty.COLLECTION_TEXT.name(),
				textValues.size(), values.length);
		
		int i = 0;
		for (String item : collection.getCollectedText()) {
			assertEquals("Collected Asset Page", item, values[i++]);
		}
		
	}
	
}
