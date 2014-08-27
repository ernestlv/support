package com.scrippsnetworks.wcm.taglib

import spock.lang.*

class TagUtilsGTest extends Specification {
    @Unroll
    def "completeHREF( #href ) should return #result"() {
        expect:
        TagUtils.completeHREF(href) == result

        where:
        href                         | result
        "/path/to/foo"               | "/path/to/foo.html"
        "/path/to/foo.html"          | "/path/to/foo.html"
        "http://www.foodnetwork.com" | "http://www.foodnetwork.com"
        "http://reddit.com/"         | "http://reddit.com/"
        "foodnetwork.com/foo/bar"    | "foodnetwork.com/foo/bar"
        "path/to/foo"                | "path/to/foo"
        null                         | null
    }
}