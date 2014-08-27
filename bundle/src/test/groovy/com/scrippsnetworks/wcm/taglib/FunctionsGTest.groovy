package com.scrippsnetworks.wcm.taglib

import spock.lang.*
import org.apache.sling.api.resource.*
import org.apache.sling.commons.testing.sling.*

/**
 * User: jason
 * Date: 4/10/12
 * Time: 1:06 PM
 */
class FunctionsGTest extends Specification {

    //sling scaffolding
    MockResourceResolver mockResourceResolver = new MockResourceResolver()
    MockResource mockResource = new MockResource(mockResourceResolver, "/foo", "module/foo")
    MockResource mockResourceChild = new MockResource(mockResourceResolver, "/foo/bar", "module/bar")
    MockResource mockResourceChild2 = new MockResource(mockResourceResolver, "/foo/baz", "module/baz")

    //prime resource resolver before each feature method executes
    def setup() {
      mockResourceResolver.setSearchPath("/")
      mockResourceResolver.addResource(mockResource)
      mockResourceResolver.addResource(mockResourceChild)
      mockResourceResolver.addResource(mockResourceChild2)
    }

    @Unroll
    def "removeMarkup( #text ) should return ( #result )"() {
        expect:
        Functions.removeMarkup(text) == result

        where:
        text << [ '<b>foo</b> <a href="foo.com">bar</a> <h1>baz</h1>',
                  '<h1><a href="blahdyblah.com">test text</a></h1>',
                  '<script>alert("blah");</script>' ]

        result << [ 'foo bar baz',
                    'test text',
                    '' ]
    }

    @Unroll
    def "getResourceChild(foo, bar) should return Resource bar"() {
        when:
        Resource child = Functions.getResourceChild(mockResource, "bar")

        then:
        child.getPath() == "/foo/bar"
        child.resourceType == "module/bar"
    }

    //the more I think about it, the more I think this function should return null in this case
    @Unroll
    def "getResourceChild(foo, blah) should return a NonExistingResource"() {
        when:
        Resource child = Functions.getResourceChild(mockResource, "blah")

        then:
		child == null
    }

    @Unroll
    def "getResourceChildIterator(foo) should return an Iterator<Resource>"() {
        when:
        Iterator<Resource> resourceIterator = Functions.getResourceChildIterator(mockResource)

        then:
        resourceIterator != null
    }

    @Unroll
    def "getResourceProperties(mockResource) should return expected ValueMap"() {
        when:
        ValueMap valueMap = Functions.getResourceProperties(mockResource)

        then:
        valueMap != null
        valueMap.get("resourceType", "") == "module/foo"
    }

    @Unroll
    def "getResourceProperty(mockResource, 'resourceType') should return 'module/foo'"() {
        when:
        String property = Functions.getResourceProperty(mockResource, "resourceType")

        then:
        property != null
        property.length() > 0
        property == "module/foo"
    }

    @Unroll
    def "removeMarkupExceptAnchors"() {
        when:
        String result = Functions.removeMarkupExceptAnchors(text)

        then:
        result == expected

        where:
        text << ["blahdy blah",
                 "<a href=\"blahdyblah\">foo</a>",
                 "blah <a href=\"www.foodterms.com/encyclopedia/sugar/index.html\" class=\"crosslink\" debug=\"328 333\">sugar</a>"]
        expected << ["blahdy blah",
                     "<a href=\"blahdyblah\">foo</a>",
                     "blah \n<a href=\"www.foodterms.com/encyclopedia/sugar/index.html\" class=\"crosslink\" debug=\"328 333\">sugar</a>"]
    }

    @Unroll
    def "removeNamespace"() {
        when:
        String result = Functions.removeNamespace(input)

        then:
        result == expected

        where:
        input << ["foo-bar:baz", "cook-stuff:blah"]
        expected << ["baz", "blah"]
    }
}
