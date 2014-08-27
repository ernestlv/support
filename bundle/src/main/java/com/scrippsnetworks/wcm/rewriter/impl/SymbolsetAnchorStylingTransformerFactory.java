package com.scrippsnetworks.wcm.rewriter.impl;

// Factory imports
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.rewriter.Transformer;
import org.apache.sling.rewriter.TransformerFactory;

// Transformer imports
import java.io.IOException;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import java.lang.IllegalArgumentException;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.sling.rewriter.ProcessingComponentConfiguration;
import org.apache.sling.rewriter.ProcessingContext;
import org.apache.sling.rewriter.Transformer;
import org.apache.sling.rewriter.TransformerFactory;
import org.apache.cocoon.xml.sax.AbstractSAXPipe;

import com.scrippsnetworks.wcm.rewriter.AnchorTagRewriterConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms anchor tags depending on the value of a marker attribute added to links.
 */
@Component(label="SNI WCM Symbolset Anchor Styling Transformer Factory", description="Factory for anchor tag styling transformers", metatype=false)
@Service(value=TransformerFactory.class)
@Property(name="pipeline.type", value="symbolset-anchor-styling", propertyPrivate=true)
public class SymbolsetAnchorStylingTransformerFactory implements TransformerFactory {

    private static Logger logger = LoggerFactory.getLogger(SymbolsetAnchorStylingTransformer.class);

    /** The map of attribute values to symbol set class names */
    private static final Map<String, String> markerMap = new HashMap<String, String>();
    static {
        markerMap.put(AnchorTagRewriterConstants.AnchorAttributeValue.VIDEO.getAttributeValue(), "ss-video");
        markerMap.put(AnchorTagRewriterConstants.AnchorAttributeValue.GALLERY.getAttributeValue(), "ss-layers");
    }

    private static final String markerAttribute = AnchorTagRewriterConstants.ATTRIBUTE_NAME;
    
    private static String[] markerNames = markerMap.keySet().toArray(new String[0]);

    public Transformer createTransformer() {
        return new SymbolsetAnchorStylingTransformer();
    }

    public class SymbolsetAnchorStylingTransformer extends AbstractSAXPipe implements Transformer {

    
        private ContentHandler nextHandler;
        private Stack<Anchor> insideMarkedLink = new Stack<Anchor>();
    
        public class Anchor {
            String marker;
            public Anchor(String marker) {
                this.marker = marker; 
            }
    
            public boolean hasMarker() {
                return marker != null;
            }
    
            public String getMarker() {
                return marker;
            }
        }
    
        public void dispose() {
            // nothing to do here
        }
    
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if ("a".equalsIgnoreCase(localName) && !insideMarkedLink.isEmpty()) {
                Anchor thisAnchor = insideMarkedLink.pop();
                if (thisAnchor != null && thisAnchor.hasMarker()) {
                    String linkMarker = " <i class=\"" + markerMap.get(thisAnchor.getMarker()) + "\"></i>";
                    nextHandler.characters(linkMarker.toCharArray(), 0, linkMarker.length());
                }
            }
        }
    
        public void init(ProcessingContext context, ProcessingComponentConfiguration config) throws IOException {
            logger.debug("initializing AnchorStylingTransformer for {}", context.getRequest().getRequestURL());
        }
    
        public void setContentHandler(ContentHandler handler) {
            this.nextHandler = handler;
            super.setContentHandler(handler);
        }
    
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if ("a".equalsIgnoreCase(localName)) {
                insideMarkedLink.push(new Anchor(getMarkerAttributeValue(atts)));
            }
            super.startElement(uri, localName, qName, atts);
        }
    
        private String getMarkerAttributeValue(Attributes atts) {
            String attributeValue = null;
            String typeMarker = null;
            if (markerNames != null) {
                int attsCount = atts.getLength();
                for (int i = 0; i < attsCount; i++) {
                    if (markerAttribute.equals(atts.getLocalName(i))) {
                        attributeValue = atts.getValue(i);
                        break;
                    }
                }
                if (attributeValue != null) {
                    String[] values = attributeValue.split("\\s");
                    for (String marker : markerNames) {
                        if (ArrayUtils.contains(values, marker)) {
                            typeMarker = marker;
                            return typeMarker;
                        }
                    }
                }
            }
    
            return null;
        }
    }
    
}
