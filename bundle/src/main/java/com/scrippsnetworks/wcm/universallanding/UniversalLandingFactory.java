package com.scrippsnetworks.wcm.universallanding;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.universallanding.impl.UniversalLandingImpl;
import org.apache.sling.api.resource.Resource;

/**
 * @author Jonathan Bell
 *         Date: 7/25/2013
 */
public class UniversalLandingFactory {

    private SniPage page;
    private Resource resource;

    public UniversalLanding build() {
        if (resource != null) {
            return new UniversalLandingImpl(resource);
        }
        if (page != null) {
            return new UniversalLandingImpl(page);
        }
        return null;
    }

    public UniversalLandingFactory withSniPage(SniPage page) {
        this.page = page;
        return this;
    }

    public UniversalLandingFactory withParsysResource(Resource resource) {
        this.resource = resource;
        return this;
    }
}
