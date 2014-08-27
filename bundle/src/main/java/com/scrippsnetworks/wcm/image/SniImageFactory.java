package com.scrippsnetworks.wcm.image;

import com.scrippsnetworks.wcm.image.impl.SniImageImpl;
import com.scrippsnetworks.wcm.page.SniPage;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;

/**
 * @author Jason Clark
 *         Date: 4/28/13
 */
public class SniImageFactory {

    private String aspect;
    private String jcrPath;
    private SniPage sniPage;
    private String rendition;
    private Resource resource;

    /**
     * Construct an SniImage with a combination
     * @return
     */
    public SniImage build() {
        return new SniImageImpl(jcrPath, resource, sniPage, rendition, aspect);
    }

    /**
     * Add a jcrPath to SniImage
     * @param path String JCR Path to image resource
     * @return ImageFactory
     */
    public SniImageFactory withPath(String path) {
        this.jcrPath = path;
        return this;
    }

    /**
     * @param sniPage SniPage
     * @return this SniImageFactory
     */
    public SniImageFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }

    /**
     *
     * @param resource Resource
     * @return this SniImageFactory
     */
    public SniImageFactory withResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public SniImageFactory withRendition(String rendition) {
        this.rendition = rendition;
        return this;
    }

    public SniImageFactory withAspect(String aspect) {
        this.aspect = aspect;
        return this;
    }
}
