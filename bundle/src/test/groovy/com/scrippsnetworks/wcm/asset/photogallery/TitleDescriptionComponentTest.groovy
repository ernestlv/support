package com.scrippsnetworks.wcm.asset.photogallery

import spock.lang.Specification
import org.apache.sling.commons.testing.sling.*
import spock.lang.Unroll

/**
 * Unit tests for TitleDescriptionComponent object
 * @author Jason Clark
 * Date: 7/25/12
 */
class TitleDescriptionComponentTest extends Specification {

    //sling scaffolding
    MockResourceResolver mockResourceResolver = new MockResourceResolver()
    MockResource mockResource = new MockResource(mockResourceResolver, "/foo",
            TitleDescriptionComponent.TITLE_DESCRIPTION_RESOURCE_TYPE)
    MockResource mockResource2 = new MockResource(mockResourceResolver, "/bar",
            TitleDescriptionComponent.TITLE_DESCRIPTION_RESOURCE_TYPE)

    //prime resource resolver before each feature method executes
    def setup() {
        mockResourceResolver.setSearchPath("/")
        mockResourceResolver.addResource(mockResource)
    }

    @Unroll
    def "basic construction of a TitleDescriptionComponent object" () {
        when:
        TitleDescriptionComponent testComponent = new TitleDescriptionComponent(mockResource);

        then:
        testComponent.getTitle() == ""
        testComponent.getDescription() == ""
    }

}

