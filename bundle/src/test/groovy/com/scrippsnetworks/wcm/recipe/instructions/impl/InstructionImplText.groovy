package com.scrippsnetworks.wcm.recipe.instructions.impl

import spock.lang.Specification
import spock.lang.Unroll

/**
 *
 * @author Jason Clark
 * Date: 6/19/13
 */
class InstructionImplText extends Specification {

    @Unroll
    def "Instruction built with title and text has title and text"() {
        when:
        InstructionImpl instruction = new InstructionImpl(title, text)

        then:
        instruction.getTitle() == title
        instruction.getText() == text

        where:
        title << ["Black Bean Blah"]
        text << ["Blah Blah Blah"]
    }

    @Unroll
    def "Instruction built with a blob of RMA-like text should have sane results"() {
        when:
        InstructionImpl instruction = new InstructionImpl(rmaText)

        then:
        instruction.getTitle() == title
        instruction.getText() == text

        where:
        rmaText << ["this is a title:\nand this is some text.", "ANOTHER TITLE\nand some more text."]
        title << ["this is a title:", "ANOTHER TITLE"]
        text << ["and this is some text.", "and some more text."]
    }
}
