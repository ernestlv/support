package com.scrippsnetworks.wcm.opengraph.impl

import com.scrippsnetworks.wcm.opengraph.OpenGraph
import spock.lang.Specification
import spock.lang.Unroll

import com.day.cq.wcm.api.Page
import com.scrippsnetworks.wcm.fnr.util.PageSlingResourceTypes
import com.scrippsnetworks.wcm.page.PageFactory
import com.scrippsnetworks.wcm.page.SniPage
import com.scrippsnetworks.wcm.page.impl.MockPage.MockPageFactory
import com.scrippsnetworks.wcm.image.SniImageFactory

/**
 * Tests for OpenGraphImpl
 * @author Jason Clark
 * Date: 4/24/13
 */
class OpenGraphImplTest extends Specification {

    private static final MockPageFactory MOCK_PAGE_FACTORY = new MockPageFactory()

    private static final TEST_TITLE = "test title"
    private static final TEST_DESC = "test description"

    private static final Map<String, Object> props = new HashMap<String, Object>()
    static {
        props.put(OpenGraph.JCR_PROPERTY_OG_TITLE, TEST_TITLE)
        props.put(OpenGraph.JCR_PROPERTY_OG_DESCRIPTION, TEST_DESC)
    }

    @Unroll
    def "OpenGraph page type for most pages should be article"() {
        when:
        SniPage fooPage = MOCK_PAGE_FACTORY
                .withPath("/content/food")
                .withProperty("sling:resourceType", PageSlingResourceTypes.PHOTO_GALLERY.resourceType())
                .build()
        OpenGraphImpl openGraph = fooPage.getOpenGraph()

        then:
        openGraph.getOGType() == OpenGraph.DEFAULT_OG_TYPE
    }

    @Unroll
    def "OpenGraph page type should be video.tv_show for shows"() {
        when:
        SniPage fooPage = MOCK_PAGE_FACTORY
                .withPath("/content/food")
                .withProperty("sling:resourceType", PageSlingResourceTypes.SHOW.resourceType())
                .build()
        OpenGraphImpl openGraph = fooPage.getOpenGraph()

        then:
        openGraph.getOGType() == OpenGraph.OG_VALUE_TYPE_SHOW
    }

    @Unroll
    def "OpenGraph page type should be video.episode for episodes"() {
        when:
        SniPage fooPage = MOCK_PAGE_FACTORY
                .withPath("/content/food")
                .withProperty("sling:resourceType", PageSlingResourceTypes.EPISODE.resourceType())
                .build()
        OpenGraphImpl openGraph = fooPage.getOpenGraph()

        then:
        openGraph.getOGType() == OpenGraph.OG_VALUE_TYPE_EPISODE
    }

    @Unroll
    def "Default OpenGraph uses Page's SEO Title and Description" () {
        when:
        SniPage fooPage = MOCK_PAGE_FACTORY
                .withPath("/content/food/shows/fooshow")
                .build()
        OpenGraphImpl openGraph = fooPage.getOpenGraph()

        then:
        openGraph.getOGTitle() == fooPage.getSeoTitle()
        openGraph.getOGDescription() == fooPage.getSeoDescription()
    }

    @Unroll
    def "OpenGraph User Overrides work for Title and Description"() {
        when:
        SniPage fooPage = MOCK_PAGE_FACTORY
                .withPath("/content/food/shows/fooshow")
                .withProperties(props)
                .build()
        OpenGraphImpl openGraph = fooPage.getOpenGraph()


        then:
        openGraph.getOGTitle() == TEST_TITLE
        openGraph.getOGDescription() == TEST_DESC
    }

    @Unroll
    def "Default OpenGraph uses Canonical URL for og url"() {
        when:
        SniPage fooPage = MOCK_PAGE_FACTORY
                .withPath("/content/food/shows/fooshow")
                .build()
        OpenGraphImpl openGraph = fooPage.getOpenGraph()


        then:
        openGraph.data.get(OpenGraph.OG_PROPERTY_URL) == fooPage.getCanonicalUrl()
    }
}
