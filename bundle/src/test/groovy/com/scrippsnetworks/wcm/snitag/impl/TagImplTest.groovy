package com.scrippsnetworks.wcm.snitag.impl

import spock.lang.Specification
import spock.lang.Unroll

/**
 *
 * @author Jason Clark
 * Date: 6/29/13
 */
class TagImplTest extends Specification {

    @Unroll
    def "Tags should have the right data in the right fields."() {

        when:
        SniTagImpl tag = new SniTagImpl(text)

        then:
        tag.getFacet() == facet
        tag.getClassification() == classification
        tag.getValue() == value
        tag.getNamespace() == namespace

        where:
        text << ["blah:foo/bar/baz", "food-tags:main-ingredient/fish/kipper", "foo-bar:thing/value", "foo:bar", "foo-bar:food/cuisine/blahdy/thing/blah"]
        namespace << ["blah", "food-tags", "foo-bar", "foo", "foo-bar"]
        facet << ["foo", "main-ingredient", "thing", "bar", "food"]
        classification << ["bar", "fish", null, null, "cuisine/blahdy/thing"]
        value << ["baz", "kipper", "value", null, "blah"]

    }
}
