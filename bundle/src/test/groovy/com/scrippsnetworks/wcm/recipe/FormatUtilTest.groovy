package com.scrippsnetworks.wcm.recipe

import spock.lang.Specification
import spock.lang.Unroll

/**
 *
 * @author Jason Clark
 * Date: 6/27/13
 */
class FormatUtilTest extends Specification {

    @Unroll
    def "markdown builder should return null if input text is null"() {
        when:
        String result = FormatUtil.addTruncationMarkdown(null, null)

        then:
        result == null;
    }

    @Unroll
    def "markdown builder should return sane results."()  {
        when:
        String result = FormatUtil.addTruncationMarkdown(text, length)

        then:
        result == expectedResult

        where:
        text << ["", "foobarbaz", "foobarbazly", "the rain in spain"]
        length << [0, 10, 10, 10]
        expectedResult << ["", "foobarbaz", "foobarbazly", "the rain in[ spain]"]
    }

}
