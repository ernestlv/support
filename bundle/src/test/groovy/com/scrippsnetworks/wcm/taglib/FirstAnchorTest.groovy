package com.scrippsnetworks.wcm.taglib

import spock.lang.*
import javax.servlet.jsp.JspWriter
import javax.servlet.jsp.tagext.BodyContent

class FirstAnchorTest extends Specification {

    //is declaring these globally the best practice?
    def mockJspWriter   = Mock(JspWriter)
    def mockBodyContent = Mock(BodyContent)

    @Unroll
    def "FirstAnchor should write #result when given #text"() {
        setup:
        mockBodyContent.getString() >> "foobarbaz"
        mockBodyContent.getEnclosingWriter() >> mockJspWriter

        FirstAnchor firstanchor = new FirstAnchor()
        firstanchor.setBodyContent(mockBodyContent)

        when: "run the tag"
        firstanchor.setText(text)
        int retval = firstanchor.doAfterBody()

        then: "tag should write expected result"
        retval == 0
        1 * mockJspWriter.write(result)

        where:
        text <<   [ '<p><a href="http://www.foo.com">foo</a></p>',
                    '<h1>the quick brown fox</h1> <p>jumped over the lazy <a href="blah">dog</a></p>',
                    '<a href="foo.com">tag one</a> <a href="baz.com">tag two</a>',
                    'this text has no anchors at all' ]

        result << [ '<a href="http://www.foo.com">foobarbaz</a>',
                    '<a href="blah">foobarbaz</a>',
                    '<a href="foo.com">foobarbaz</a>',
                    'foobarbaz' ]
    }

    @Unroll
    def "FirstAnchor should write nothing when BodyContent=#getstring and text=#text"() {
        setup:
        mockBodyContent.getEnclosingWriter() >> mockJspWriter
        FirstAnchor firstanchor = new FirstAnchor()
        firstanchor.setBodyContent(mockBodyContent)

        when:
        mockBodyContent.getString() >> getstring
        firstanchor.setText(text)
        int retval = firstanchor.doAfterBody()

        then:
        retval == 0
        0 * mockJspWriter.write(_) //this method should never get called

        where:
        getstring << ['', null, '', null]
        text      << ['', null, null, '']
    }
}
