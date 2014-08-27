package com.scrippsnetworks.wcm.mobile.headerstack;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.scrippsnetworks.wcm.hub.Hub;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import com.scrippsnetworks.wcm.mobile.headerstack.impl.HeaderStackImpl;
import com.scrippsnetworks.wcm.page.SniPage;

public class HeaderStackFactory {

    public static final String SIMPLE_BANNER_TYPE = "/components/modules/banner-simple";
    public static final String CUSTOM_BANNER_TYPE = "/components/modules/banner-custom";
    public static final String REFERENCE_COMPONENT_TYPE = "foundation/components/reference";
    private ResourceResolver resolver;
    private SniPage page;
    private SniPage packagePage;

    public HeaderStackFactory withResolver(ResourceResolver resolver) {
        this.resolver = resolver;
        return this;
    }

    public HeaderStackFactory withCurrentPage(SniPage sniPage) {
        this.page = sniPage;
        return this;
    }

    public HeaderStack build() {

        String pageType = page.getPageType();

        if ("recipe".equals(pageType) || "video".equals(pageType)) {
            return null;
        }

        packagePage = page.getPackageAnchor();
        Resource banner = getCurrentBanner();

        if (packagePage != null) {
            if ("show".equals(packagePage.getPageType())) {
                return new HeaderStackImpl(page, packagePage, banner);
            }
        }
        if ("show".equals(pageType)) {
            return new HeaderStackImpl(page, page, banner);
        } else if ("universal-landing".equals(pageType)) {
            return new HeaderStackImpl(page, packagePage, banner);
        } else if (banner != null) {
            return new HeaderStackImpl(page, packagePage, banner);
        }

        return null;
    }


    private Resource getCurrentBanner() {
        //get banner from current page
        Resource pageBanner = getBannerFromPage(page);
        if (pageBanner != null) return pageBanner;

        // get banner from package
        if (packagePage != null) {
            Resource packageBanner = getBannerFromPage(packagePage);
            if (packageBanner != null) return packageBanner;
        }

        //get banner from hub modules
        Hub contextHub = page.getHub();
        if (contextHub == null || page.getPath().equals(contextHub.getHubMaster().getPath())) {
            return null;
        }

        List<Resource> hubModules = contextHub.getSharedModules();
        if (hubModules != null && hubModules.size() > 0) {
            Collections.reverse(hubModules);
            for (Resource module : hubModules) {
                if (module != null) {
                    //get banner
                    String moduleType = module.getResourceType();
                    if (moduleType != null && ((moduleType.contains(CUSTOM_BANNER_TYPE) || moduleType.contains(SIMPLE_BANNER_TYPE)))) {
                        return module;
                    }
                    if (moduleType != null && moduleType.contains(REFERENCE_COMPONENT_TYPE)) {
                        Resource bannerRes = getReferenceBanner(module);
                        if (bannerRes != null) {
                            return bannerRes;
                        }
                    }

                }
            }
        }
        return null;
    }

    private Resource getBannerFromPage(SniPage page) {
        Resource firstBanner = null;
        Resource superLead = resolver.getResource(page.getContentResource(), "superlead");

        if (superLead != null) {
            Iterator<Resource> resourceIterator = resolver.listChildren(superLead);
            firstBanner = getFirstBannerResource(resourceIterator, resolver);
        }

        if (firstBanner != null) return firstBanner;

        Resource contentWell = resolver.getResource(page.getContentResource(), "content-well");

        if (contentWell != null) {
            Iterator<Resource> resourceIterator = resolver.listChildren(contentWell);
            firstBanner = getFirstBannerResource(resourceIterator, resolver);
        }

        if (firstBanner != null) return firstBanner;

        return null;
    }

    private Resource getFirstBannerResource(Iterator<Resource> resources, ResourceResolver resolver) {
        while (resources.hasNext()) {
            Resource refRes = resources.next();
            if (refRes == null) continue;
           /* ValueMap vMap = r.adaptTo(ValueMap.class);
            String refPath = vMap.get("path", "");
            Resource refRes = resolver.getResource(refPath);
            if (refRes == null) continue;*/
            String refType = refRes.getResourceType();
            if (refType != null && ((refType.contains(CUSTOM_BANNER_TYPE) || refType.contains(SIMPLE_BANNER_TYPE)))) {
                return refRes;
            }
            if (refType != null && refType.contains(REFERENCE_COMPONENT_TYPE)) {
                Resource bannerRes = getReferenceBanner(refRes);
                if (bannerRes != null) {
                    return bannerRes;
                }
            }
        }
        return null;
    }

    private Resource getReferenceBanner(Resource referenceComponent) {
        ValueMap iterVm = referenceComponent.adaptTo(ValueMap.class);
        String refBannerPath = iterVm.get("path", "");
        Resource bannerRes = resolver.getResource(refBannerPath);
        if (bannerRes == null) {
            return null;
        }
        String refBannerType = bannerRes.getResourceType();
        if (refBannerType != null && (refBannerType.contains(CUSTOM_BANNER_TYPE) || refBannerType.contains(SIMPLE_BANNER_TYPE))) {
            return bannerRes;
        }
        return null;
    }
}
