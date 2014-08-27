package com.scrippsnetworks.wcm.universallanding.impl;

import com.scrippsnetworks.wcm.universallanding.LeadImageInfo;
import com.scrippsnetworks.wcm.universallanding.UniversalLanding;
import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.image.SniImageFactory;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.paginator.PaginatorFactory;
import com.scrippsnetworks.wcm.paginator.ParagraphPaginator;
import com.scrippsnetworks.wcm.util.PagePropertyNames;

import java.util.Iterator;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import com.day.cq.wcm.api.Page;

/**
 * @author Jonathan Bell
 *         Date: 7/25/2013
 */
public class UniversalLandingImpl implements UniversalLanding {
    private Resource resource;
    private SniImage firstImage;
    private SniPage sniPage;

    private final String FORWARD_SLASH = "/";
    private final String EMPTY_STRING = "";

    public UniversalLandingImpl() {}

    public UniversalLandingImpl(final SniPage page) {
        this.sniPage = page;
        this.resource = page.adaptTo(Resource.class);
    }

    public UniversalLandingImpl(final Resource resource) {
        this.resource = resource;
        this.sniPage = PageFactory.getSniPage(resource.adaptTo(Page.class));
    }

    private String getComponentBaseName(String nodeResourceType) {
        if (StringUtils.isNotBlank(nodeResourceType)) {
            String[] chunks = nodeResourceType.split(FORWARD_SLASH);
            return chunks.length > 0 ? chunks[chunks.length - 1] : nodeResourceType;
        } else {
            return EMPTY_STRING;
        }
    }

    @Override
    public SniImage getCanonicalImage() {
        if (firstImage == null) {
            Iterator<Resource> childItr = sniPage.getContentResource().listChildren();
            findFirstImage: 
                while (childItr.hasNext()) {
                    Resource childRes = childItr.next();
                    ValueMap childVm = ResourceUtil.getValueMap(childRes);
                    Iterator<Resource> grandItr = childRes.listChildren();
                    while (grandItr.hasNext()) {
                        Resource grandRes = grandItr.next();
                        ValueMap grandVm = ResourceUtil.getValueMap(grandRes);
                        if (grandVm.containsKey(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName())) {
                            String rt = grandVm.get(
                                PagePropertyNames.SLING_RESOURCE_TYPE.propertyName(), String.class);
                            for (LeadImageInfo lii : LeadImageInfo.values()) {
                                if (getComponentBaseName(rt).equals(lii.getResourceType())) {
                                    firstImage = new SniImageFactory()
                                        .withPath(grandVm.get(lii.getImageReference(), String.class))
                                        .build();
                                    break findFirstImage;
                                }
                            }
                        }
                    }
            }
        }
        return this.firstImage;
    }

    public SniPage getSniPage() {
        return sniPage;
    }
}
