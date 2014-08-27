package com.scrippsnetworks.wcm.util

import spock.lang.Specification
import spock.lang.Unroll

/**
 *
 * @author Jason Clark
 * Date: 5/22/13
 */
class CompoundPropertyTest extends Specification {

    private static final TEST_PROPERTY = "foo|bar"
    private static final TEST_PROPERTY_NO_KEY = "baz"


    @Unroll
    def "A property of foo|bar should return key == foo"() {
        when:
        CompoundProperty prop = new CompoundProperty(TEST_PROPERTY)

        then:
        prop.getKey() == "foo"
    }

    @Unroll
    def "A property of foo|bar should return a value of bar"() {
        when:
        CompoundProperty prop = new CompoundProperty(TEST_PROPERTY)

        then:
        prop.getValue() == "bar"
    }

    @Unroll
    def "A property of baz should have a null key and a value of baz"() {
        when:
        CompoundProperty prop = new CompoundProperty(TEST_PROPERTY_NO_KEY)

        then:
        prop.getKey() == null
        prop.getValue() == "baz"
    }
}
