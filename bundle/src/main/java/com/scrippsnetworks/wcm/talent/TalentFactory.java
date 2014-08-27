package com.scrippsnetworks.wcm.talent;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.talent.impl.TalentImpl;
import org.apache.sling.api.resource.Resource;

/**
 * @author Jonathan Bell
 *         Date: 7/25/2013
 */
public class TalentFactory {

    private SniPage page;
    private Resource resource;

    public Talent build() {
        if (resource != null) {
            return new TalentImpl(resource);
        }
        if (page != null) {
            return new TalentImpl(page);
        }
        return null;
    }

    public TalentFactory withSniPage(SniPage page) {
        this.page = page;
        return this;
    }

    public TalentFactory withResource(Resource resource) {
        this.resource = resource;
        return this;
    }
}
