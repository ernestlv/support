package com.scrippsnetworks.wcm.hub.count.impl

import spock.lang.Specification
import spock.lang.Unroll

import com.scrippsnetworks.wcm.page.impl.MockPage.MockPageFactory;

/**
 * Tests for HubCountImpl
 * @author Jason Clark
 * Date: 5/13/13
 */
class HubCountImplTest extends Specification {

    private static final MockPageFactory MOCK_PAGE_FACTORY = new MockPageFactory();

    @Unroll
    def "HubCount for a page that is not hub-able should return null"() {

    }
}
