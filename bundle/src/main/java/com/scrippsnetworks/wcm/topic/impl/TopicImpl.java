package com.scrippsnetworks.wcm.topic.impl;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.topic.Topic;
import java.util.ArrayList;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author Venkata Naga Sudheer Donaboina
 * Date: 9/20/13
 */
public class TopicImpl implements Topic {

	private SniPage sniPage;
	/** Resource for convenience, because you need a Resource from time to time. */
    private Resource resource;
    
    private String primaryTopicTag;
    private String secondaryTopicTag;
    private String[] topicTags;
    
    private ValueMap topicPageProperties;
    
	public TopicImpl(final SniPage sniPage) {
		this.sniPage = sniPage;
		resource = sniPage.getContentResource();
		this.topicPageProperties = sniPage.getProperties();
	}
	
	@Override
	public SniPage getSniPage() {
		return sniPage;
	}

	/** {@inheritDoc} */
	@Override
	public String getPrimaryTopicTag() {
		if(primaryTopicTag == null) {
			if(topicPageProperties != null && topicPageProperties.containsKey(PagePropertyConstants.PROP_SNI_PRIMARY_TOPIC_TAG)) {
				primaryTopicTag = topicPageProperties.get(PagePropertyConstants.PROP_SNI_PRIMARY_TOPIC_TAG, String.class);
			}
		}
		return primaryTopicTag;
	}

	/** {@inheritDoc} */
	@Override
	public String getSecondaryTopicTag() {
		if(secondaryTopicTag == null) {
			if(topicPageProperties != null && topicPageProperties.containsKey(PagePropertyConstants.PROP_SNI_SECONDARY_TOPIC_TAG)) {
				secondaryTopicTag = topicPageProperties.get(PagePropertyConstants.PROP_SNI_SECONDARY_TOPIC_TAG, String.class);
			}
		}
		return secondaryTopicTag;
	}

	/** {@inheritDoc} */
	@Override
	public String[] getTopicTags() {
		if(topicTags == null) {
			if(topicPageProperties != null && topicPageProperties.containsKey(PagePropertyConstants.PROP_SNI_TOPIC_TAGS)) {
				topicTags = topicPageProperties.get(PagePropertyConstants.PROP_SNI_TOPIC_TAGS, String[].class);
			}
		}
		return topicTags;
	}
        
        /** {@inheritDoc} */
	@Override
        public String getTopicTagsForSearch() {
            ArrayList<String> allTopicTags = new ArrayList<String>();
            String [] allTopicTagsArr = (String[])ArrayUtils.addAll(getTopicTags(), new String[] { getPrimaryTopicTag(), getSecondaryTopicTag() });
            for (String topicTag : allTopicTagsArr) {
                if (!StringUtils.isEmpty(topicTag)) {                    
                    String tagName = topicTag;
                    if (topicTag.lastIndexOf("/") != -1) {
                        tagName = topicTag.substring(topicTag.lastIndexOf("/")+1);
                    } else if (topicTag.contains(":")) {
                        tagName = topicTag.substring(topicTag.lastIndexOf(":")+1);
                    }
                    if (!allTopicTags.contains(tagName)) {
                        allTopicTags.add(tagName);
                    }
                }
            }
            return StringUtils.join(allTopicTags, ",");
        }
        
        /** {@inheritDoc} */
	@Override
        public String getKeywordsForSearch() {
            return sniPage.getProperties().get("sni:keywords", String.class);
        }
        
        /** {@inheritDoc} */
	@Override
        public String getAssetRestrictions() {
            return sniPage.getProperties().get("sni:assetRestrictions", String.class);
        }

}
