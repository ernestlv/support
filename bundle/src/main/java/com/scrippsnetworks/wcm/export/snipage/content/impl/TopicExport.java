package com.scrippsnetworks.wcm.export.snipage.content.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.topic.Topic;
import com.scrippsnetworks.wcm.topic.TopicFactory;

/**
 * This class generates the Topic page specific properties.
 * @author Venkata Naga Sudheer Donaboina
 * 
 */
public class TopicExport extends SniPageExport {
	private static final Logger LOG = LoggerFactory.getLogger(TopicExport.class);

	public enum ExportProperty {

		TOPIC_PRIMARY_TAG(String.class),
		TOPIC_SECONDARY_TAG(String.class),
		TOPIC_TAGS(String[].class);

		final Class clazz;

		ExportProperty(Class clazz) {
			this.clazz = clazz;
		}

		public Class valueClass() {
			return clazz;
		}
	}

	private final Topic topic;

	public TopicExport(SniPage sniPage) {
		super(sniPage);
		this.topic = new TopicFactory().withSniPage(sniPage).build();
		initialize();
	}

	protected TopicExport(SniPage sniPage, Topic topic) {
		super(sniPage);
		this.topic = topic;
		initialize();
	}

	public void initialize() {

		LOG.debug("Started Topic Export overrides");

		if (sniPage == null || !sniPage.hasContent() || topic == null) {
			return;
		}

		 setProperty(ExportProperty.TOPIC_PRIMARY_TAG.name(), topic.getPrimaryTopicTag());
		 
		 setProperty(ExportProperty.TOPIC_SECONDARY_TAG.name(), topic.getSecondaryTopicTag());
		 
		 setProperty(ExportProperty.TOPIC_TAGS.name(), topic.getTopicTags());
		 
		LOG.debug("Completed Topic Export overrides");

	}
}
