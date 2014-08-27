package com.scrippsnetworks.wcm.sitemap.impl;

import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Node;
import javax.jcr.Session;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.scrippsnetworks.wcm.sitemap.Sitemap;
import com.scrippsnetworks.wcm.sitemap.SitemapIndex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SitemapIndexImpl implements SitemapIndex {

    private static final Logger LOGGER = LoggerFactory.getLogger(SitemapIndex.class);

    private static final String EXTENSION_XML = ".xml";
    private static final String TAG_SITEMAPINDEX = "sitemapindex";
    private static final String TAG_SITEMAP = "sitemap";
    private static final String TAG_LOC = "loc";
    private static final String TAG_LASTMOD = "lastmod";

    private List<Sitemap> sitemaps;
    private String destinationPath;
    private String indexName;
    private String mapPath;
    private ResourceResolver resourceResolver;
    private Session session;

    private Document doc;

    public SitemapIndexImpl() {}

    public SitemapIndexImpl(List<Sitemap> sitemaps, String destinationPath, String indexName, ResourceResolver resourceResolver) {
        this.sitemaps = sitemaps;
        this.destinationPath = destinationPath;
        this.indexName = indexName;
        this.resourceResolver = resourceResolver;
        this.session = resourceResolver.adaptTo(Session.class);
    }

    public boolean generate() {
        boolean generated = false;

        if (startDocument()) {
            Element mainElement = createMainElement();
            doc.appendChild(mainElement);

            for (Sitemap sitemap : sitemaps) {
                for (String mapUrl : sitemap.getUrls()) {
                    Element elementSitemap = createSitemapElement();
                    elementSitemap.appendChild(createElement(TAG_LOC, mapUrl));
                    elementSitemap.appendChild(createElement(TAG_LASTMOD, sitemap.getDate()));
                    mainElement.appendChild(elementSitemap);
                }
            }

            mapPath = finishDocument();
            if (!StringUtils.isEmpty(mapPath)) {
                generated = true;
            }
        }

        return generated;
    }

    public String getPath() {
        return mapPath;
    }

    /**
     * DOCUMENT MANAGEMENT
     */

    private Element createMainElement() {
        Element element = doc.createElement(TAG_SITEMAPINDEX);
 
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://www.sitemaps.org/schemas/sitemap/0.9");

        return element;
    }

    private Element createSitemapElement() {
        Element element = doc.createElement(TAG_SITEMAP);

        return element;
    }

    private Element createElement(String elementTag, String elementValue) {
        Element element = doc.createElement(elementTag);
        element.appendChild(doc.createTextNode(elementValue == null ? "" : elementValue));

        return element;
    }

    private boolean startDocument() {
        boolean created = false;

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.newDocument();
            created = true;
        } catch (ParserConfigurationException pce) {
            LOGGER.error(ExceptionUtils.getStackTrace(pce));
        }

        return created;
    }

    private String finishDocument() {
        String indexPath = null;

        try {
            DOMSource source = new DOMSource(doc);
            Node pathNode = resourceResolver.getResource(destinationPath).adaptTo(Node.class);

            if (pathNode != null) {
                StringWriter xmlAsWriter = new StringWriter();  
                StreamResult result = new StreamResult(xmlAsWriter);  
                TransformerFactory.newInstance().newTransformer().transform(source, result);  
                InputStream inputStream = new ByteArrayInputStream(xmlAsWriter.toString().getBytes("UTF-8")); 

                Node indexNode = JcrUtils.putFile(pathNode, indexName + EXTENSION_XML, "text/xml", inputStream);
                indexPath = indexNode.getPath();
                session.save();
            }
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }

        return indexPath;
    }
}

