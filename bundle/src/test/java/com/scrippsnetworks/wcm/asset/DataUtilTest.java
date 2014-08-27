package com.scrippsnetworks.wcm.asset;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.apache.sling.api.wrappers.*;

/**
 * @author kenshih
 */
public class DataUtilTest {
	
	@Before
	public void setup(){
		 MockitoAnnotations.initMocks(this);
	}
	
	/**
	 * test the generic form of DataUtil#getAssetData -- to be renamed, btw
	 * @throws Exception
	 */
	@Test
	public void mergeResourceProperties() throws Exception{
		// mock/test data setup
		Resource mockDefaultResource = mock(Resource.class);
		Resource mockResourceToMerge = mock(Resource.class);
		ValueMap valMapDefault = createValueMapWithPropsAndValues(
				new String[]{"prop1","prop2","prop3"},
				new String[]{"value1","value2","value3-default-only"});
		ValueMap valMapChange = createValueMapWithPropsAndValues(
				new String[]{"prop1","prop2","prop4"},
				new String[]{"changedvalue1","changedvalue2","value4-changemap-only"});
		//warning: please note, this relies on the implementation of ResourceUtils only using adaptTo(ValueMap.class on the Resource)
		when(mockDefaultResource.adaptTo(ValueMap.class)).thenReturn(valMapDefault);
		when(mockResourceToMerge.adaptTo(ValueMap.class)).thenReturn(valMapChange);
		String[] allowedOverrides = new String[]{"prop1"};
		
		//to operation to test
		Map<String, Object> resultMap = DataUtil.mergeResourceProperties(
				mockDefaultResource, mockResourceToMerge, allowedOverrides);
		
		//todo add FEST to test dependencies, so we i don't have to add "//" comments
		assertEquals(resultMap.get("prop1"),"changedvalue1"); 	// override allowed and to-merge is overridden
		assertEquals(resultMap.get("prop2"),"value2"); 			// override not-allowed and to-merge has value, so should not override
		assertEquals(resultMap.get("prop3"),"value3-default-only"); 	// override not-allowed and to-merge missing property, should retain default value
		assertEquals(resultMap.get("prop4"),null); 				// override not-allowed, exists in to-merge, but not in default, should be null
		
	}
	
	/**
	 * adding this regression to test the refactoring out of the main merge loop out into a common method
	 * 
	 * before making a change, i wrote this test, ran to make sure it succeeded
	 * then, refactored and re-confirmed
	 * 
	 * i'd like is tp refactor this test, however, once we get to refactoring this method :)
	 * 
	 * jason, consult ken for questions
	 * @throws Exception
	 */
	@Test
	public void getAssetData() throws Exception{
		// mock/test data setup
		Resource mockDefaultResource = mock(Resource.class);
		Resource mockResourceToMerge = mock(Resource.class);
		ValueMap valMapDefault = createValueMapWithPropsAndValues(
				new String[]{"dc:title", "dc:description", "copyright","prop1","prop2"},
				new String[]{"title","description","copyrite","value1","value2"});
		ValueMap valMapChange = createValueMapWithPropsAndValues(
				new String[]{"prop1","dc:description","copyright"},
				new String[]{"shouldntchangevalue1","changed-description","changed-copyright"});
		//warning: please note, this relies on the implementation of ResourceUtils only using adaptTo(ValueMap.class on the Resource)
		when(mockDefaultResource.adaptTo(ValueMap.class)).thenReturn(valMapDefault);
		when(mockResourceToMerge.adaptTo(ValueMap.class)).thenReturn(valMapChange);
		
		//to operation to test
		Map<String, Object> resultMap = DataUtil.getAssetData(
				mockDefaultResource, mockResourceToMerge);
		
		//todo add FEST to test dependencies, so we i don't have to add "//" comments
		assertEquals(resultMap.get("prop1"),"value1"); // wasn't in allowedOverrides list, so shouldn't be overridden (hence test for orig value)
		assertEquals(resultMap.get("dc:description"),"changed-description"); // on override list so overriden
		assertEquals(resultMap.get("copyright"),"changed-copyright"); // on override list so overriden
		assertEquals(resultMap.get("prop2"),"value2"); // not in to-merge-resource, so retains default value
		
	}
	
	static ValueMap createValueMapWithPropsAndValues(String[] properties, String[] values){
		//annoying "Object" :(
		Map<String,Object> map = new HashMap<String,Object>();
		for(int i=0; i<properties.length;i++){
			map.put(properties[i], values[i]);
		}
		ValueMap vMap = new ValueMapDecorator(map);
		return vMap;
	}

}
