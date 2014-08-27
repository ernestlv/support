package com.scrippsnetworks.wcm.snitag.impl;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.cache.TopicPageCacheService;
import com.scrippsnetworks.wcm.fnr.util.OsgiHelper;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.snitag.SniTag;
import com.scrippsnetworks.wcm.util.PagePropertyNames;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

/**
 * @author Jason Clark
 *         Date: 6/29/13
 */
public class SniTagImpl implements SniTag {

    private static final String TAG_BASE_PATH = "/etc/tags";
    private static final String TAG_DESCRIPTION = "jcr:description";
    private static final String TAG_TITLE = "jcr:title";

    private static final String SOURCE_CONTENT_PATH = "/content/sources";
    private static final String SOURCE_IGNORABLE = "manual";

    private final static Logger log = LoggerFactory.getLogger(SniTagImpl.class);

    private ValueMap tagProperties;

    private SniPage sniPage;
    private String rawTag;
    private String namespace;
    private String facet;
    private String classification;
    private String value;
    private SniPage tagPage;
    private SniPage topicPage;
    private String displayValue;
    private String description;

    /** Construct a new Tag out of a String. */
    public SniTagImpl(final String tag, final SniPage sniPage) {
        this.sniPage = sniPage;
        if (StringUtils.isNotBlank(tag)) {
            rawTag = tag;
            String[] parts = tag.split("/");
            if (parts != null) {
                if (parts.length == 1) {
                    setNameAndFacet(parts[0]);
                } else if (parts.length == 2) {
                    setNameAndFacet(parts[0]);
                    value = parts[1];
                } else if (parts.length > 2) {
                    setNameAndFacet(parts[0]);
                    StringBuilder partsBuilder = new StringBuilder();
                    for (int i = 1; i < parts.length - 1; i++) {
                        partsBuilder.append(parts[i]);
                        if (i + 1 < parts.length - 1) {
                            partsBuilder.append("/");
                        }
                    }
                    classification = partsBuilder.toString();
                    value = parts[parts.length - 1];
                } else {
                    log.info("Found a tag with {} parts.", parts.length);
                }
            }
        }
    }

    /** Overloaded constructor for just tag by itself. */
    public SniTagImpl(final String tag) {
        this(tag, null);
    }

    /** Convenience method for setting name and facet, since they're combined by a colon. */
    private void setNameAndFacet(String nameFacet) {
        if (StringUtils.isNotBlank(nameFacet)) {
            String[] parts = nameFacet.split(":");
            if (parts != null && parts.length == 2) {
                namespace = parts[0];
                facet = parts[1];
            } else {
                log.info("Couldn't find a namespace and facet in \"{}\"", nameFacet);
            }
        }
    }

    /** {@inheritDoc} */
    public ValueMap getTagProperties() {
        if (tagProperties == null) {
            if (sniPage != null) {
                Resource resource = sniPage.getContentResource();
                if (resource != null) {
                    ResourceResolver resolver = resource.getResourceResolver();
                    if (resolver != null) {
                        if (StringUtils.isNotBlank(rawTag)) {
                            String[] parts = rawTag.split(":");
                            if (parts != null && parts.length == 2) {
                                StringBuilder builder = new StringBuilder();
                                builder
                                        .append(TAG_BASE_PATH)
                                        .append("/")
                                        .append(parts[0])
                                        .append("/")
                                        .append(parts[1]);
                                String tagPath = builder.toString();
                                Resource tagResource = resolver.getResource(tagPath);
                                if (tagResource != null) {
                                    tagProperties = tagResource.adaptTo(ValueMap.class);
                                }
                            }
                        }
                    }
                }
            }
        }
        return tagProperties;
    }

    /** {@inheritDoc} */
    public SniPage getTopicPage() {
        OsgiHelper osgiHelper = new OsgiHelper();
        try {
            if (topicPage == null && StringUtils.isNotBlank(rawTag)) {
                String brand = null;
                Resource resource = null;
                ResourceResolver resolver = null;
                if (sniPage != null) {
                    resource = sniPage.getContentResource();
                    resolver = resource.getResourceResolver();
                    brand = sniPage.getBrand();
                }
                if (resource != null) {
                    TopicPageCacheService topicCacheService = osgiHelper.getOsgiService(TopicPageCacheService.class.getName());
                    String path = topicCacheService.getTopicPagePath(resource,rawTag,brand);
                    if (StringUtils.isNotBlank(path)) {
                        Resource pageResource = resolver.resolve(path);
                        Page page = pageResource.adaptTo(Page.class);
                        topicPage = PageFactory.getSniPage(page);
                    }
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(),ex);
        }
        return topicPage;
    }

    /** {@inheritDoc} */
    public String getDescription() {
        if (description == null) {
            ValueMap tagProps = getTagProperties();
            if (tagProps != null && tagProps.containsKey(TAG_DESCRIPTION)) {
                description = tagProps.get(TAG_DESCRIPTION, String.class);
            }
        }
        return description;
    }

    /** {@inheritDoc} */
    public String getDisplayValue() {
        if (displayValue == null) {
            ValueMap properties = getTagProperties();
            if (properties != null && properties.containsKey(TAG_TITLE)) {
                displayValue = properties.get(TAG_TITLE, String.class);
            }
        }
        return displayValue;
    }

    /** {@inheritDoc} */
    public String getNamespace() {
        return namespace;
    }

    /** {@inheritDoc} */
    public String getFacet() {
        return facet;
    }

    /** {@inheritDoc} */
    public String getClassification() {
        return classification;
    }

    /** {@inheritDoc} */
    public String getValue() {
        return value;
    }

    /** {@inheritDoc} */
    public String getRawTag() {
        return rawTag;
    }

    /** {@inheritDoc} */
    public SniPage getTagMetadataPage() {
        if (tagPage == null && StringUtils.isNotBlank(rawTag) && !rawTag.endsWith(SOURCE_IGNORABLE)) {
            Resource resource = sniPage.getContentResource();
            if (resource != null) {
                ResourceResolver resolver = resource.getResourceResolver();
                if (resolver != null) {
                    try {
                        QueryManager queryManager = resource.adaptTo(Node.class)
                            .getSession().getWorkspace().getQueryManager();
                        StringBuilder query = new StringBuilder();
                        query.append("/jcr:root")
                            .append(SOURCE_CONTENT_PATH)
                            .append("/*[jcr:content/@")
                            .append(PagePropertyNames.CQ_TAGS.propertyName())
                            .append("='")
                            .append(rawTag)
                            .append("']");
                        Query compiledQuery = queryManager
                            .createQuery(query.toString(), Query.XPATH);
                        NodeIterator nodes = compiledQuery.execute().getNodes();
                        if (nodes.hasNext()) {
                            Node node = nodes.nextNode();
                            String path = node.getPath();
                            if (StringUtils.isNotBlank(path)) {
                                Resource pageResource = resolver.resolve(path);
                                Page page = pageResource.adaptTo(Page.class);
                                tagPage = PageFactory.getSniPage(page);
                            }
                        }
                    } catch (RepositoryException re) {
                        log.error("RepositoryException caught: ", re);
                    } catch (Exception e) {
                        log.error("Exception caught: ", e);
                    }
                }
            }
        }

        return tagPage;
    }

    /** Override toString to return the raw text tag. */
    @Override
    public String toString() {
        return StringUtils.isNotBlank(rawTag) ? rawTag : "";
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 29).append(rawTag).toHashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object object) {
        if (object == null) { return false; }
        if (object == this) { return true;  }
        if (object.getClass() != getClass()) {
            return false;
        }
        SniTag compareTag = (SniTag) object;
        return new EqualsBuilder()
                .append(rawTag, compareTag.getRawTag())
                .isEquals();
    }
}
