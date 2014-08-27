package com.scrippsnetworks.wcm.sitemap.impl;

import com.google.common.collect.Lists;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.jcr.Node;
import javax.jcr.Session;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;

import org.apache.jackrabbit.commons.JcrUtils;

import org.jsoup.Jsoup;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;

import com.day.cq.replication.Replicator;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationStatus;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import com.scrippsnetworks.wcm.config.SiteConfigService;
import com.scrippsnetworks.wcm.fnr.util.OsgiHelper;
import com.scrippsnetworks.wcm.image.ImageAspect;
import com.scrippsnetworks.wcm.image.ImageUrlService;
import com.scrippsnetworks.wcm.image.RenditionInfo;
import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.image.SniImageFactory;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.photogallery.PhotoGallery;
import com.scrippsnetworks.wcm.photogallery.PhotoGalleryFactory;
import com.scrippsnetworks.wcm.photogallery.PhotoGallerySlide;
import com.scrippsnetworks.wcm.sitemap.Sitemap;
import com.scrippsnetworks.wcm.sitemap.SitemapPageType;
import com.scrippsnetworks.wcm.util.PageTypes;
import com.scrippsnetworks.wcm.video.Video;
import com.scrippsnetworks.wcm.video.VideoFactory;
import static com.scrippsnetworks.wcm.util.PageTypes.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SitemapImpl implements Sitemap {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sitemap.class);
    private static final EnumSet INDEXABLE_TYPES = EnumSet.of(
        ARTICLE_SIMPLE, BEVERAGE, BIO, CALENDAR, COMPANY, EPISODE, FREE_FORM_TEXT, HOMEPAGE, INDEX,
        MENU, PHOTOGALLERY, RECIPE, SECTION, SHOW, TALENT, TOPIC, UNIVERSAL_LANDING,
        VIDEO, VIDEO_CHANNEL, VIDEO_PLAYER);

    private static final String FULL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    private static final String PUBLISH_DATE_DISPLAY_FORMAT = "yyyy-MM-dd";

    private static final String TAG_URLSET = "urlset";
    private static final String TAG_URL = "url";
    private static final String TAG_LASTMOD = "lastmod";
    private static final String TAG_LOC = "loc";
    private static final String TAG_CHANGEFREQ = "changefreq";
    private static final String TAG_PRIORITY = "priority";
    private static final String TAG_IMAGE = "image:image";
    private static final String TAG_IMAGE_LOC = "image:loc";
    private static final String TAG_IMAGE_CAPTION = "image:caption";
    private static final String TAG_IMAGE_TITLE = "image:title";
    private static final String TAG_VIDEO = "video:video";
    private static final String TAG_VIDEO_THUMBNAIL_LOC = "video:thumbnail_loc";
    private static final String TAG_VIDEO_TITLE = "video:title";
    private static final String TAG_VIDEO_DESCRIPTION = "video:description";
    private static final String TAG_VIDEO_CONTENT_LOC = "video:content_loc";
    private static final String TAG_VIDEO_DURATION = "video:duration";
    private static final String TAG_VIDEO_PUBLICATION_DATE = "video:publication_date";
    private static final String TAG_VIDEO_TAG = "video:tag";
    private static final String TAG_VIDEO_CATEGORY = "video:category";

    private static final String CHANGEFREQ_DAILY = "daily";
    private static final String CHANGEFREQ_WEEKLY = "weekly";
    private static final String CHANGEFREQ_MONTHLY = "monthly";
    private static final String CHANGEFREQ_YEARLY = "yearly";
    private static final String CHANGEFREQ_DEFAULT = CHANGEFREQ_MONTHLY;

    private static final int VIDEO_DESCRIPTION_MAXLENGTH = 2048;
    private static final int VIDEO_TITLE_MAXLENGTH = 100;

    private static final String SNI_IMAGE_RENDITION = "sni4col";
    private static final int SITEMAP_LIMITS_URLS_PER_FILE = 50000;
    private static final int SITEMAP_LIMITS_IMAGES_PER_PAGE = 1000;
    private static final String EXTENSION_XML = ".xml";
    private static final String EXTENSION_GZIP = ".gz";
    private static final String HTTP_PROTOCOL = "http://";

    private static final String PRIORITY_DEFAULT = "0.5";

    private List<String> excludedPaths;
    private List<String> indexablePaths;
    private List<SniPage> pages = new ArrayList<SniPage>();
    private List<String> mapFiles = new ArrayList<String>();
    private List<String> mapUrls = new ArrayList<String>();
    private String destinationPath;
    private String mapName;
    private Replicator replicator;
    private ResourceResolver resourceResolver;
    private Session session;
    private int entryLimit = SITEMAP_LIMITS_URLS_PER_FILE;
    private String mapLastModifiedDate = null;

    private Document doc;
    private ImageUrlService imageUrlService;
    private SiteConfigService siteConfig;
    private String brand;

    public SitemapImpl() {}

    public SitemapImpl(List<String> indexablePaths, List<String> excludedPaths, String destinationPath,
            String mapName, Replicator replicator, ResourceResolver resourceResolver, String brand) {
        this.excludedPaths = excludedPaths;
        this.indexablePaths = indexablePaths;
        this.destinationPath = destinationPath;
        this.mapName = mapName;
        this.replicator = replicator;
        this.resourceResolver = resourceResolver;
        this.session = resourceResolver.adaptTo(Session.class);
        this.brand = brand;

        setSiteConfigService();
    }

    public boolean generate() {
        boolean generated = false;
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);

        for (String path : indexablePaths) {
            if (! StringUtils.isEmpty(path)) {
                SniPage page = PageFactory.getSniPage(pageManager, path);
                if (isValidPage(page)) {
                    pages.add(page);
                }
                collectChildPages(page);
            }
        }

        // Break job into chunks of entryLimit (50,000 typical) pages each
        int chunks = new Double(Math.ceil(pages.size() / new Float(entryLimit))).intValue();
        for (int chunkNumber=0; chunkNumber<chunks; chunkNumber++) {
            // The document is a global that we recreate for each chunk.
            if (startDocument()) {
                Element mainElement = createMainElement();
                doc.appendChild(mainElement);

                int start = chunkNumber * entryLimit;
                int end = start + entryLimit > pages.size() ? pages.size() : start + entryLimit;

                List<SniPage> chunkPages = pages.subList(start, end);
                for (SniPage page : chunkPages) {
                    Element elementPage = createUrlElement(page);
                    if (elementPage != null) {
                        mainElement.appendChild(elementPage);
                    }
                }

                String chunkPath = finishDocument(chunkNumber);
                if (! StringUtils.isEmpty(chunkPath)) {
                    mapFiles.add(chunkPath);
                    String mapPartInfo = String.valueOf(chunkNumber+1) + "/" + String.valueOf(chunks); 
                    LOGGER.info("Added map {} part {}", mapName, mapPartInfo);
                }
            }
        }

        if (mapFiles.size() > 0) {
            generated = true;

            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat(FULL_DATE_FORMAT);
            mapLastModifiedDate = sdf.format(now);
        }

        return generated;
    }

    public List<String> getPaths() {
        return mapFiles;
    }

    public List<String> getUrls() {
        if (mapUrls.size() == 0) {
            for (String path : mapFiles) {
                String url = HTTP_PROTOCOL + siteConfig.getDomain() + "/" + FilenameUtils.getName(path);
                if (! StringUtils.isEmpty(url)) {
                    mapUrls.add(url);
                }
            }
        }

        return mapUrls;
    }

    public String getDate() {
        return mapLastModifiedDate;
    }

    private void collectChildPages(SniPage startPage) {
        if (startPage != null) {
            Iterator<Page> childPages = startPage.listChildren();
            while (childPages.hasNext()) {        
                SniPage childPage = PageFactory.getSniPage(childPages.next());
                if (childPage != null && isValidPage(childPage) && isActivated(childPage)) {
                    pages.add(childPage);
                }
                collectChildPages(childPage);
            }
        }
    }

    private boolean isActivated(SniPage page) {
        boolean activated = false;

        try {
            ReplicationStatus status = replicator.getReplicationStatus(session, page.getPath());
            if (status.isActivated() && !status.isPending()) {
                activated = true;
            }
        } catch (Exception e) {
            LOGGER.error("Could not determine activation status of {}. Exception follows.", page.getPath());
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }

        return activated;
    }

    private boolean isValidPage(SniPage page) {
        boolean valid = false;

        if (page != null && page.getDepth() >= 3 && isExcludedPath(page.getPath()) == false) {
            PageTypes type = PageTypes.findPageType(page.getPageType());
            if (type != null && INDEXABLE_TYPES.contains(type)) {
                valid = true;
            }
        }

        return valid;
    }

    private boolean isExcludedPath(String path) {
        boolean excluded = false;

        for (String exclusion : excludedPaths) {
            String saneExclusion = exclusion.endsWith("/") ? exclusion : exclusion + "/";
            if (path.startsWith(saneExclusion)) {
                excluded = true;
                break;
            }
        }

        return excluded;
    }

    /**
     * DOCUMENT MANAGEMENT
     */

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

    private String finishDocument(int chunkNumber) {
        String mapPath = null;

        try {
            DOMSource source = new DOMSource(doc);
            Node pathNode = resourceResolver.getResource(destinationPath).adaptTo(Node.class);

            if (pathNode != null) {
                StringWriter xmlAsWriter = new StringWriter();  
                StreamResult result = new StreamResult(xmlAsWriter);  
                TransformerFactory.newInstance().newTransformer().transform(source, result);  
                ByteArrayOutputStream docOutput = new ByteArrayOutputStream();

                try {
                    Writer writer = new OutputStreamWriter(new GZIPOutputStream(docOutput), "UTF-8");
                    try {
                        writer.write(xmlAsWriter.toString());
                    } finally {
                        writer.close();
                    }
                } finally {
                    docOutput.close();
                }

                ByteArrayInputStream compressedXML = new ByteArrayInputStream(docOutput.toByteArray());
                String fileSuffix = chunkNumber > 0 ? Integer.toString(chunkNumber) : "";
                Node gzipFile = JcrUtils.putFile(
                    pathNode,
                    mapName + fileSuffix + EXTENSION_XML + EXTENSION_GZIP,
                    "gzip",
                    compressedXML);
                session.save();
                
                compressedXML.close();
                mapPath = gzipFile.getPath();
            }
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }

        return mapPath;
    }

    private Element createMainElement() {
        Element element = doc.createElement(TAG_URLSET);
 
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://www.sitemaps.org/schemas/sitemap/0.9");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:image", "http://www.google.com/schemas/sitemap-image/1.1");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:video", "http://www.google.com/schemas/sitemap-video/1.1");

        return element;
    }

    private Element createUrlElement(SniPage page) {
        String changeFreq;
        String priority;

        try {
            PageTypes type = PageTypes.findPageType(page.getPageType());
            if (type != null) {
                SitemapPageType mapPageType = SitemapPageType.valueOf(type.name());
                priority = mapPageType.getPriority();
                changeFreq = mapPageType.getChangeFreq();
            } else {
                LOGGER.error("Unexpected pagetype {} at {}", page.getPageType(), page.getPath());
                priority = PRIORITY_DEFAULT;
                changeFreq = CHANGEFREQ_DEFAULT;
            }
        } catch (IllegalArgumentException iae) {
            priority = PRIORITY_DEFAULT;
            changeFreq = CHANGEFREQ_DEFAULT;
        }
        if (page.getDepth() == 3) {
            changeFreq = CHANGEFREQ_DAILY;
        }

        List<Element> childElements = new ArrayList<Element>();

        Element element = doc.createElement(TAG_URL);

        // tUrl elements are sequential, per schema
        childElements.add(createElement(TAG_LOC, page.getCanonicalUrl()));
        childElements.add(createLastModifiedElement(page));
        childElements.add(createElement(TAG_CHANGEFREQ, changeFreq));
        childElements.add(createElement(TAG_PRIORITY, priority));

        childElements.add(createImageElement(page.getCanonicalImage()));
        childElements.add(createVideoElement(page));
        childElements.addAll(createGalleryImageElements(page));

        for (Element ce : childElements) {
            if (ce != null) {
                element.appendChild(ce);
            }
        }

        return element;
    }

    private String getLastReplicatedDate(SniPage page) {
        String lastReplication = page.getProperties().get(PagePropertyConstants.PROP_CQ_LAST_REPLICATED, "");
        String lastModified = null;

        if (! StringUtils.isEmpty(lastReplication)) {
            SimpleDateFormat longDateFormat = new SimpleDateFormat(FULL_DATE_FORMAT);
            SimpleDateFormat displayDateFormat = new SimpleDateFormat(PUBLISH_DATE_DISPLAY_FORMAT);

            try {
                Date lastReplicatedDate = (Date)longDateFormat.parse(lastReplication);
                lastModified = displayDateFormat.format(lastReplicatedDate);
            } catch (ParseException pe) {
                LOGGER.error("Could not parse replication date for {}. {}", page.getPath(), pe);
                lastModified = "";
            }
        }

        return lastModified;
    }

    private Element createLastModifiedElement(SniPage page) {
        Element element = null;

        String lastModified = getLastReplicatedDate(page);
        if (! StringUtils.isEmpty(lastModified)) {
            element = createElement(TAG_LASTMOD, lastModified);
        }

        return element;
    }

    private Element createElement(String elementTag, String elementValue) {
        Element element = doc.createElement(elementTag);
        element.appendChild(doc.createTextNode(elementValue == null ? "" : elementValue));

        return element;
    }

    private Element createImageElement(SniImage image) {
        Element element = null;

        if (image != null) {
            element = createImageElement(image, image.getCaption(), image.getTitle());
        }

        return element;
    }

    private Element createImageElement(SniImage image, String caption, String title) {
        Element element = null;

        if (image != null) {
            element = doc.createElement(TAG_IMAGE);
            element.appendChild(createElement(TAG_IMAGE_LOC, getImageCanonicalUrl(image)));
            if (! StringUtils.isEmpty(caption)) {
                element.appendChild(createElement(TAG_IMAGE_CAPTION, prepareText(caption)));
            }
            if (! StringUtils.isEmpty(title)) {
                element.appendChild(createElement(TAG_IMAGE_TITLE, prepareText(title)));
            }
        }

        return element;
    }

    private List<Element> createGalleryImageElements(SniPage page) {
        List<Element> elements = new ArrayList<Element>();
        List<PhotoGallerySlide> slides = null;

        PageTypes type = PageTypes.findPageType(page.getPageType());
        if (type == PHOTOGALLERY) {
            PhotoGallery gallery = new PhotoGalleryFactory()
                .withSniPage(page)
                .build();

            if (gallery != null && gallery.getSlideCount() > 0) {
                slides = gallery.getAllSlides();
                for (PhotoGallerySlide slide : slides) {
                    Element slideElement = createImageElement(
                        slide.getSniImage(), slide.getCaption(), slide.getTitle());
                    if (slideElement != null) {
                        elements.add(slideElement);
                        if (elements.size() == SITEMAP_LIMITS_IMAGES_PER_PAGE) {
                            break;
                        }
                    }
                }
            }
        }

        return elements;
    }

    private Element createVideoElement(SniPage page) {
        Element element = null;

        PageTypes type = PageTypes.findPageType(page.getPageType());
        if (type == VIDEO) {
            Video video = new VideoFactory()
                .withSniPage(page)
                .build();

            if (video != null) {
                element = doc.createElement(TAG_VIDEO);
                element.appendChild(createElement(TAG_VIDEO_THUMBNAIL_LOC, video.getThumbnailUrl()));
                element.appendChild(createElement(TAG_VIDEO_TITLE,
                    prepareText(video.getTitle(), VIDEO_TITLE_MAXLENGTH)));
                element.appendChild(createElement(TAG_VIDEO_DESCRIPTION,
                    prepareText(video.getVideoDescription(), VIDEO_DESCRIPTION_MAXLENGTH)));
                element.appendChild(createElement(TAG_VIDEO_CONTENT_LOC, video.getVideoUrl()));

                if (! StringUtils.isEmpty(video.getVideoRunTime())) {
                    element.appendChild(createElement(TAG_VIDEO_DURATION,
                        Integer.toString(durationStringToInt(video.getVideoRunTime()))
                    ));
                }
                                
                element.appendChild(createElement(TAG_VIDEO_PUBLICATION_DATE, getLastReplicatedDate(page)));
            }
        }

        return element;
    }

    private int durationStringToInt(String duration) {
        int runtime = 0;

        if (! StringUtils.isEmpty(duration)) {
            String[] runtimeSplit = duration.split(":");
            if (runtimeSplit.length > 0) {
                List<String> runtimeParts = Lists.reverse(Arrays.asList(runtimeSplit));
                for (int p=0; p<runtimeParts.size(); p++) {
                    int part = Integer.parseInt(runtimeParts.get(p));
                    switch (p) {
                        case 0:
                            runtime += part;
                            break;
                        case 1:
                            runtime += (part * 60);
                            break;
                        case 2:
                            runtime += (part * 3600);
                            break;
                        default:
                            break;
                    }
                } 
            }
        }

        return runtime;
    }

    /**
     * Prepare text for inclusion as XML text element; remove HTML markup and fix entities.
     */
    private String prepareText(String text) {
        String cleanText = null;

        if (! StringUtils.isEmpty(text)) {
            Whitelist whitelist = Whitelist.none();
            Cleaner cleaner = new Cleaner(whitelist);

            org.jsoup.nodes.Document jdoc = cleaner.clean(Jsoup.parse(text));
            jdoc.outputSettings().escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);

            cleanText = jdoc.body().html();
        }

        return cleanText;
    }

    private String prepareText(String text, int maxLength) {
        String cleanText = prepareText(text);

        if (! StringUtils.isEmpty(text)) {
            cleanText = StringUtils.abbreviate(cleanText, maxLength);
        }

        return cleanText;
    }

    private void setSiteConfigService() {
        OsgiHelper osgiHelper;

        if (siteConfig == null) {
            osgiHelper = new OsgiHelper();
            if (osgiHelper != null) {
                siteConfig = osgiHelper.getOsgiServiceBySite(SiteConfigService.class.getName(), brand);
            }
        }
    }

    private ImageUrlService getImageUrlService() {
        if (imageUrlService == null) {
            BundleContext bundle = FrameworkUtil.getBundle(ImageUrlService.class).getBundleContext();
            if (bundle != null) {
                ServiceReference serviceReference = bundle.getServiceReference(ImageUrlService.class.getName());
                if (serviceReference != null) {
                    imageUrlService = (ImageUrlService)bundle.getService(serviceReference);
                }
            }
        }

        return imageUrlService;
    }

    /**
     * Sitemaps run on author instances, but replicate completed documents
     * to a publish node; this results in improperly externalized URLs
     * for images when using image.getCanonicalUrl().
     */
    private String getImageCanonicalUrl(SniImage image) {
        String imageCanonicalUrl = "";

        if (image != null) {
            if (image.getPath() != null && image.getPath().startsWith("/")) {
                ImageUrlService urlService = getImageUrlService();
                if (urlService != null) {
                    imageCanonicalUrl = urlService.getImageUrl(
                        image.getPath(),
                        image.getRendition() != null ? RenditionInfo.valueOf(image.getRendition()) : null,
                        image.getAspect() != null ? ImageAspect.valueOf(image.getAspect()) : null,
                        "damsndimg");
                }
            } else {
                imageCanonicalUrl = image.getPath();
            }
        }

        return imageCanonicalUrl;
    }
}

