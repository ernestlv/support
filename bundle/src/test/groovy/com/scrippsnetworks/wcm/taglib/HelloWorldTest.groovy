package com.scrippsnetworks.wcm.taglib

import spock.lang.*
import javax.servlet.jsp.*

class HelloWorldTest extends Specification {
    @Unroll
    def "HelloWorld tag should write Hello World! once"() {
        setup:
        def mockPageContext = Mock(PageContext)
        def mockJspWriter = Mock(JspWriter)
        mockPageContext.getOut() >> mockJspWriter

        HelloWorld hello = new HelloWorld()
        hello.setPageContext(mockPageContext)

        when:
        int startval = hello.doStartTag()
        int endval   = hello.doEndTag()

        then:
        startval == 0 //SKIP_BODY returned
        endval   == 0 //SKIP_BODY returned
        //1 * hello.pageContext.out.write(_) //can write anything once
        1 * hello.pageContext.out.write("Hello World!") //can only write "Hello World!" once
        //1 * hello.pageContext.out.write("blah") //should fail
    }
}
