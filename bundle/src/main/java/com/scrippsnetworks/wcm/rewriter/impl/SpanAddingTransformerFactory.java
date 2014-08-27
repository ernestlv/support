package com.scrippsnetworks.wcm.rewriter.impl;

import java.io.IOException;
import java.util.Stack;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.rewriter.ProcessingComponentConfiguration;
import org.apache.sling.rewriter.ProcessingContext;
import org.apache.sling.rewriter.Transformer;
import org.apache.sling.rewriter.TransformerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Rewriter pipeline component which adds a span with a specific class name
 * around the last word inside of a elements which have at least one of the
 * specified class names.
 */
@Component
@Service
@Property(name = "pipeline.type", value = "last-word-span-adder", propertyPrivate = true)
public class SpanAddingTransformerFactory implements TransformerFactory {
    
    private static final String PN_SPAN_CLASS_NAME = "spanClassName";

    private static final String PN_MARKER_CLASS_NAMES = "markerClassNames";

    public Transformer createTransformer() {
        return new SpanAddingTransformer();
    }

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private class SpanAddingTransformer implements Transformer {

        private ContentHandler nextHandler;

        private Stack<Boolean> insideMarkedLink = new Stack<Boolean>();

        private StringBuilder stringBuilder = new StringBuilder();

        private String[] markerClassNames;

        private String addedSpanClassName;

        public void characters(char[] ch, int start, int length) throws SAXException {
            if (addedSpanClassName != null && (!insideMarkedLink.isEmpty()) && insideMarkedLink.peek()) {
                stringBuilder.append(ch, start, length);
            } else {
                nextHandler.characters(ch, start, length);
            }
        }

        public void dispose() {
        }

        public void endDocument() throws SAXException {
            nextHandler.endDocument();
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (insideMarkedLink.pop() && addedSpanClassName != null) {
                String linkString = stringBuilder.toString();
                logger.debug("Need to add span to {}", linkString);
                String[] linkStringParts = linkString.split("\\s");
                linkStringParts[linkStringParts.length - 1] = String.format("<span class=\"%s\">%s</span>",
                        addedSpanClassName, linkStringParts[linkStringParts.length - 1]);
                String newLinkString = StringUtils.join(linkStringParts, " ");
                nextHandler.characters(newLinkString.toCharArray(), 0, newLinkString.length());

                stringBuilder.setLength(0);
            }
            nextHandler.endElement(uri, localName, qName);
        }

        public void endPrefixMapping(String prefix) throws SAXException {
            nextHandler.endPrefixMapping(prefix);
        }

        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            nextHandler.ignorableWhitespace(ch, start, length);
        }

        public void init(ProcessingContext context, ProcessingComponentConfiguration config) throws IOException {
            markerClassNames = config.getConfiguration().get(PN_MARKER_CLASS_NAMES, String[].class);
            addedSpanClassName = config.getConfiguration().get(PN_SPAN_CLASS_NAME, String.class);
        }

        public void processingInstruction(String target, String data) throws SAXException {
            nextHandler.processingInstruction(target, data);
        }

        public void setContentHandler(ContentHandler handler) {
            this.nextHandler = handler;
        }

        public void setDocumentLocator(Locator locator) {
            nextHandler.setDocumentLocator(locator);
        }

        public void skippedEntity(String name) throws SAXException {
            nextHandler.skippedEntity(name);
        }

        public void startDocument() throws SAXException {
            nextHandler.startDocument();
        }

        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if ("a".equalsIgnoreCase(localName) && hasMarkerClass(atts)) {
                insideMarkedLink.push(Boolean.TRUE);
                nextHandler.startElement(uri, localName, qName, atts);
                return;
            }

            insideMarkedLink.push(Boolean.FALSE);

            nextHandler.startElement(uri, localName, qName, atts);
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            nextHandler.startPrefixMapping(prefix, uri);
        }

        private boolean hasMarkerClass(Attributes atts) {
            if (markerClassNames != null) {
                String classAttributeValue = null;
                int attsCount = atts.getLength();
                for (int i = 0; i < attsCount; i++) {
                    if ("class".equals(atts.getLocalName(i))) {
                        classAttributeValue = atts.getValue(i);
                        break;
                    }
                }
                if (classAttributeValue != null) {
                    String[] classes = classAttributeValue.split("\\s");
                    for (String markerClass : markerClassNames) {
                        if (ArrayUtils.contains(classes, markerClass)) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }

    }

}
