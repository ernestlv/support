package com.scrippsnetworks.wcm.topic.impl

import com.scrippsnetworks.wcm.page.SniPage
import com.scrippsnetworks.wcm.topic.Topic
import org.apache.sling.api.resource.ValueMap
import org.apache.sling.api.resource.Resource
import spock.lang.Specification
import spock.lang.Unroll

/**
 *
 * @author Jason Clark
 * Date: 2/21/14
 */
class TopicImplTest extends Specification {

    SniPage mockSniPage
    ValueMap mockPageProperties
    Resource mockPageResource

    def setup() {
        mockSniPage = Mock(SniPage)
        mockPageProperties = Mock(ValueMap)
        mockPageResource = Mock(Resource)

        mockSniPage.getProperties() >> mockPageProperties
        mockPageProperties.containsKey(_) >> true
        mockPageProperties.get("sni:primaryTopicTag", String.class) >> "food-tags:drinks"
        mockPageProperties.get("sni:secondaryTopicTag", String.class) >> "food-tags:drinks/koolaid"
    }

    @Unroll
    def "Test getTopicTagsForSearch"() {
        when:
        Topic testTopic = new TopicImpl(mockSniPage)

        then:
        testTopic.getTopicTagsForSearch() == "drinks,koolaid"
    }
}
