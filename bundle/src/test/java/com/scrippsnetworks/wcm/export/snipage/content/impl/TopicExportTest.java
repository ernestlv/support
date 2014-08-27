package com.scrippsnetworks.wcm.export.snipage.content.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.topic.Topic;

/**
 * @author Venkata Naga Sudheer Donaboina
 * 
 *  Tests the TopicExport class, whose job it is to extract
 *         export values from an SniPage specific to episodes.
 */
public class TopicExportTest {
	public static final String PRIMARY_TOPIC_TAG ="food-tags:cuisine/mexican";
	public static final String SECONDARY_TOPIC_TAG ="food-tags:main-ingredient/poultry/chicken";
	public static final String[] TOPIC_TAGS = {"food-tags:cuisine/mexican", "food-tags:main-ingredient/poultry/chicken"};
	
	public static final String PAGE_PATH = "/content/food/topics/a-topic";
	public static final String PAGE_TYPE = "topic";
	
	@Mock Topic topic;
	
	@Mock Resource topicPageCR;
	@Mock ValueMap topicPageProperties;

    @Mock PageManager pageManager;
    @Mock ResourceResolver resourceResolver;
    
    @Mock SniPage topicePage;
    
    @Before
    public void before() {
    	MockitoAnnotations.initMocks(this);
    	
    	when(topicePage.hasContent()).thenReturn(true);
    	when(topicePage.getProperties()).thenReturn(topicPageProperties);
    	when(topicePage.getContentResource()).thenReturn(topicPageCR);
    	when(topicePage.getPath()).thenReturn(PAGE_PATH);
    	when(topicePage.getPageType()).thenReturn(PAGE_TYPE);
    	when(topicePage.getPageManager()).thenReturn(pageManager);
    	
    	when(topic.getPrimaryTopicTag()).thenReturn(PRIMARY_TOPIC_TAG);
    	
    	when(topic.getSecondaryTopicTag()).thenReturn(SECONDARY_TOPIC_TAG);
    	
    	when(topic.getTopicTags()).thenReturn(TOPIC_TAGS);
    }
    
    @Test
    public void testTopicPropertyValues() {
    	
    	TopicExport episodeExport = new TopicExport(topicePage, topic);
    	ValueMap exportProps = episodeExport.getValueMap();
    	
		assertEquals(TopicExport.ExportProperty.TOPIC_PRIMARY_TAG.name(),
				PRIMARY_TOPIC_TAG, exportProps.get(
						TopicExport.ExportProperty.TOPIC_PRIMARY_TAG.name(),
						TopicExport.ExportProperty.TOPIC_PRIMARY_TAG.valueClass()));
		
		assertEquals(TopicExport.ExportProperty.TOPIC_SECONDARY_TAG.name(),
				SECONDARY_TOPIC_TAG, exportProps.get(
						TopicExport.ExportProperty.TOPIC_SECONDARY_TAG.name(),
						TopicExport.ExportProperty.TOPIC_SECONDARY_TAG.valueClass()));
		
		assertEquals(TopicExport.ExportProperty.TOPIC_TAGS.name(),
				TOPIC_TAGS, exportProps.get(
						TopicExport.ExportProperty.TOPIC_TAGS.name(),
						TopicExport.ExportProperty.TOPIC_TAGS.valueClass()));
    	
    }
    
}
