package com.scrippsnetworks.wcm.rewriter.impl;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.rewriter.ProcessingComponentConfiguration;
import org.apache.sling.rewriter.Transformer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;

public class SpanAddingTransformerFactoryTest {

    private String characters;

    private String charactersWithWrappingSpan;

    @Mock
    private ProcessingComponentConfiguration config;

    @Mock
    private ContentHandler nextHandler;

    private Transformer transformer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        transformer = new SpanAddingTransformerFactory().createTransformer();
    }

    @Test
    public void test_that_last_word_is_not_wrapped_inside_link_without_class() throws Exception {
        String spanClassName = RandomStringUtils.randomAlphabetic(3);

        Map<String, Object> configMap = new HashMap<String, Object>();
        configMap.put("markerClassNames", "videoLink");
        configMap.put("spanClassName", spanClassName);

        when(config.getConfiguration()).thenReturn(new ValueMapDecorator(configMap));
        transformer.init(null, config);
        transformer.setContentHandler(nextHandler);

        Attributes atts = createAttributes();
        initRandomWords(3, spanClassName);

        transformer.startElement("", "a", "a", atts);
        transformer.characters(characters.toCharArray(), 0, characters.length());
        transformer.endElement("", "a", "a");

        verify(nextHandler).startElement("", "a", "a", atts);
        verify(nextHandler)
                .characters(characters.toCharArray(), 0, characters.length());
        verify(nextHandler).endElement("", "a", "a");
    }

    @Test
    public void test_that_last_word_is_wrapped_inside_link() throws Exception {
        String spanClassName = RandomStringUtils.randomAlphabetic(3);

        Map<String, Object> configMap = new HashMap<String, Object>();
        configMap.put("markerClassNames", "videoLink");
        configMap.put("spanClassName", spanClassName);

        when(config.getConfiguration()).thenReturn(new ValueMapDecorator(configMap));
        transformer.init(null, config);
        transformer.setContentHandler(nextHandler);

        Attributes atts = createAttributes("class", "videoLink");
        initRandomWords(3, spanClassName);

        transformer.startElement("", "a", "a", atts);
        transformer.characters(characters.toCharArray(), 0, characters.length());
        transformer.endElement("", "a", "a");

        verify(nextHandler).startElement("", "a", "a", atts);
        verify(nextHandler)
                .characters(charactersWithWrappingSpan.toCharArray(), 0, charactersWithWrappingSpan.length());
        verify(nextHandler).endElement("", "a", "a");
    }

    @Test
    public void test_that_last_word_is_wrapped_inside_link_with_either_marker_class() throws Exception {
        String spanClassName = RandomStringUtils.randomAlphabetic(3);

        Map<String, Object> configMap = new HashMap<String, Object>();
        configMap.put("markerClassNames", new String[] { "videoLink", "photoLink" });
        configMap.put("spanClassName", spanClassName);

        when(config.getConfiguration()).thenReturn(new ValueMapDecorator(configMap));
        transformer.init(null, config);
        transformer.setContentHandler(nextHandler);

        Attributes firstAtts = createAttributes("class", "videoLink");
        initRandomWords(3, spanClassName);
        String firstCharacters = characters;
        String firstCharactersWithWrappingSpan = charactersWithWrappingSpan;

        transformer.startElement("", "a", "a", firstAtts);
        transformer.characters(firstCharacters.toCharArray(), 0, firstCharacters.length());
        transformer.endElement("", "a", "a");

        Attributes secondAtts = createAttributes("class", "photoLink");
        initRandomWords(4, spanClassName);
        String secondCharacters = characters;
        String secondCharactersWithWrappingSpan = charactersWithWrappingSpan;

        transformer.startElement("", "a", "a", secondAtts);
        transformer.characters(secondCharacters.toCharArray(), 0, secondCharacters.length());
        transformer.endElement("", "a", "a");

        verify(nextHandler).startElement("", "a", "a", firstAtts);
        verify(nextHandler).characters(firstCharactersWithWrappingSpan.toCharArray(), 0,
                firstCharactersWithWrappingSpan.length());
        verify(nextHandler, times(2)).endElement("", "a", "a");

        verify(nextHandler).startElement("", "a", "a", firstAtts);
        verify(nextHandler).characters(secondCharactersWithWrappingSpan.toCharArray(), 0,
                secondCharactersWithWrappingSpan.length());
    }
    @Test
    public void test_that_last_word_is_wrapped_inside_link_with_multiple_classes() throws Exception {
        String spanClassName = RandomStringUtils.randomAlphabetic(3);

        Map<String, Object> configMap = new HashMap<String, Object>();
        configMap.put("markerClassNames", "videoLink");
        configMap.put("spanClassName", spanClassName);

        when(config.getConfiguration()).thenReturn(new ValueMapDecorator(configMap));
        transformer.init(null, config);
        transformer.setContentHandler(nextHandler);

        Attributes atts = createAttributes("class", "videoLink otherClass");
        initRandomWords(3, spanClassName);

        transformer.startElement("", "a", "a", atts);
        transformer.characters(characters.toCharArray(), 0, characters.length());
        transformer.endElement("", "a", "a");

        verify(nextHandler).startElement("", "a", "a", atts);
        verify(nextHandler)
                .characters(charactersWithWrappingSpan.toCharArray(), 0, charactersWithWrappingSpan.length());
        verify(nextHandler).endElement("", "a", "a");
    }


    @Test
    public void test_that_last_word_is_wrapped_inside_link_with_multiple_classes_in_reverse_order() throws Exception {
        String spanClassName = RandomStringUtils.randomAlphabetic(3);

        Map<String, Object> configMap = new HashMap<String, Object>();
        configMap.put("markerClassNames", "videoLink");
        configMap.put("spanClassName", spanClassName);

        when(config.getConfiguration()).thenReturn(new ValueMapDecorator(configMap));
        transformer.init(null, config);
        transformer.setContentHandler(nextHandler);

        Attributes atts = createAttributes("class", "otherClass videoLink");
        initRandomWords(3, spanClassName);

        transformer.startElement("", "a", "a", atts);
        transformer.characters(characters.toCharArray(), 0, characters.length());
        transformer.endElement("", "a", "a");

        verify(nextHandler).startElement("", "a", "a", atts);
        verify(nextHandler)
                .characters(charactersWithWrappingSpan.toCharArray(), 0, charactersWithWrappingSpan.length());
        verify(nextHandler).endElement("", "a", "a");
    }

    @Test
    public void test_that_with_marker_classes_but_without_span_class_methods_are_passed_directly() throws Exception {
        Map<String, Object> configMap = new HashMap<String, Object>();
        configMap.put("markerClassNames", "videoLink");

        when(config.getConfiguration()).thenReturn(new ValueMapDecorator(configMap));
        transformer.init(null, config);
        transformer.setContentHandler(nextHandler);

        Attributes atts = createAttributes("class", "videoLink");
        String spanClassName = RandomStringUtils.randomAlphabetic(3);
        initRandomWords(3, spanClassName);

        transformer.startElement("", "a", "a", atts);
        transformer.characters(characters.toCharArray(), 0, characters.length());
        transformer.endElement("", "a", "a");

        verify(nextHandler).startElement("", "a", "a", atts);
        verify(nextHandler).characters(characters.toCharArray(), 0, characters.length());
        verify(nextHandler).endElement("", "a", "a");
    }

    @Test
    public void test_that_without_marker_classes_methods_are_passed_directly() throws Exception {
        when(config.getConfiguration()).thenReturn(ValueMapDecorator.EMPTY);
        transformer.init(null, config);
        transformer.setContentHandler(nextHandler);

        Attributes atts = createAttributes();
        String spanClassName = RandomStringUtils.randomAlphabetic(3);
        initRandomWords(3, spanClassName);

        transformer.startElement("", "a", "a", atts);
        transformer.characters(characters.toCharArray(), 0, characters.length());
        transformer.endElement("", "a", "a");

        verify(nextHandler).startElement("", "a", "a", atts);
        verify(nextHandler).characters(characters.toCharArray(), 0, characters.length());
        verify(nextHandler).endElement("", "a", "a");
    }

    private Attributes createAttributes(String... strings) {
        AttributesImpl atts = new AttributesImpl();
        for (int i = 0; i < strings.length; i = i + 2) {
            atts.addAttribute("", strings[i], strings[i], "CDATA", strings[i + 1]);
        }
        return atts;
    }

    private void initRandomWords(int wordCount, String spanClassName) {

        if (wordCount == 1) {
            characters = createWord();
            charactersWithWrappingSpan = String.format("<span class=\"%s\">%s</span>", spanClassName, characters);
        } else {
            StringBuilder builder = new StringBuilder();
            StringBuilder withSpanBuilder = new StringBuilder();
            for (int i = 0; i < wordCount - 1; i++) {
                String word = createWord();
                builder.append(word);
                builder.append(' ');
                withSpanBuilder.append(word);
                withSpanBuilder.append(' ');
            }
            String lastWord = createWord();
            builder.append(lastWord);
            withSpanBuilder.append(String.format("<span class=\"%s\">%s</span>", spanClassName, lastWord));

            characters = builder.toString();
            charactersWithWrappingSpan = withSpanBuilder.toString();
        }
    }

    private String createWord() {
        return RandomStringUtils.randomAlphabetic(5);
    }

}
