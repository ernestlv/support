package com.scrippsnetworks.wcm.mobile.headerstack.impl;


import java.util.List;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.page.impl.SniPageImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.mobile.headerstack.HeaderStack;
import com.scrippsnetworks.wcm.mobile.subnavigation.SubNavElem;
import com.scrippsnetworks.wcm.mobile.taglib.Functions;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;


public class HeaderStackImpl implements HeaderStack {

    public String packageName;
    public String tuneIn;
    public String hostedBy;
    public String hostedByLink;
    public boolean isShow;
    private boolean packageIsShow;
    private boolean hasPackageModule;
    private boolean hasHubMenu;
    public boolean hasSubMenu;
    public String className = "";
    private String noArrowClassName;
    private boolean hasHeaderBanner;
    private String packagePath;
    private ResourceResolver resolver;

    private static final String PACKAGE_LANDING_SIMPLE = "package-landing";
    private static final String PACKAGE_LANDING_WITH_HUB_SIMPLE = "package-landing-with-3rd";
    private static final String SHOW_LANDING_PAGE = "show-landing";
    private static final String SHOW_SUBPAGE_PAGE = "show-subpage";
    private static final String SECTION_FRONT_PAGE = "section-fronts";

    public HeaderStackImpl() {
        // TODO Auto-generated constructor stub
    }

    /**
     * Header stack component constructor.
     *
     * @param page        current page
     * @param packagePage package of current page
     * @param resource    banner
     */
    public HeaderStackImpl(SniPage page, SniPage packagePage, Resource resource) {
        ValueMap resourceValueMap = null;
        ValueMap pageValueMap = null;
        resolver = page.getContentResource().getResourceResolver();
        hasPackageModule = page.getProperties().get("sni:packageModules") != null;
        List<SubNavElem> subMenu = Functions.getSubNavListFromPage(resolver, page);
        hasSubMenu = !subMenu.isEmpty();
        hasHubMenu = page.getHub() != null;

        //get banner properties
        if (resource != null) {
            resourceValueMap = resource.adaptTo(ValueMap.class);
            hasHeaderBanner = true;
            if (resourceValueMap != null) {
                packageName = resourceValueMap.get("title", "");
                tuneIn = resourceValueMap.get("tuneInTime", "");
                hostedBy = resourceValueMap.get("subTitle", "");
            }
        }

        Hub contextHub = page.getHub();
        if (contextHub != null && contextHub.getHubMaster() != null && !page.getPath().equals(contextHub.getHubMaster().getPath())) {
            SniPage hubMasterPage = contextHub.getHubMaster();
            ValueMap contPageValueMap = contextHub.getHubMaster().getProperties();
            if (contPageValueMap != null) {
                if (StringUtils.isBlank(packageName)) packageName = hubMasterPage.getTitle();
                if (StringUtils.isBlank(tuneIn)) tuneIn = hubMasterPage.getTuneInTime();
            }
        }

        PageManager pageManager = page.getPageManager();
        isShow = "show".equals(page.getPageType());
        packagePath = page.getBannerLink();
        if (hasSubMenu && (StringUtils.isBlank(packageName) || StringUtils.isBlank(packagePath))) {
            String mainPackagePath = subMenu.get(0).getPath();
            Page mainPackagePage = pageManager.getPage(mainPackagePath);
            if (mainPackagePage != null) {
                SniPage sniMainPackagePage = new SniPageImpl(mainPackagePage);
                if (sniMainPackagePage != null && page.getPath().indexOf(sniMainPackagePage.getPath()) == 0) {
                    if (StringUtils.isBlank(packageName)) packageName = sniMainPackagePage.getTitle();
                    if (StringUtils.isBlank(packagePath)) packagePath = sniMainPackagePage.getPath();
                }
            }
        }

        if (packagePage != null) {
            SniPage packageAnchor = packagePage.getPackageAnchor();
            if (StringUtils.isBlank(packageName)) {
                packageName = (packageAnchor != null) ? packageAnchor.getTitle() : packagePage.getTitle();
            }
            if (StringUtils.isBlank(packagePath)) {
                packagePath = (packageAnchor != null) ? packageAnchor.getPath() : packagePage.getPath();
            }
            packageIsShow = "show".equals(packagePage.getPageType());
            if (StringUtils.isBlank(tuneIn)) {
                tuneIn = packagePage.getTuneInTime();
            }
            pageValueMap = packagePage.getProperties();
            if ((packageIsShow || isShow) && StringUtils.isBlank(hostedBy) && pageValueMap != null) {
                String primaryTalent = pageValueMap.get("sni:primaryTalent", "");
                if (StringUtils.isNotBlank(primaryTalent)) {
                    Resource pageContent = page.getContentResource();
                    Resource primaryTalentResource = resolver.getResource(primaryTalent);

                    if (!pageContent.RESOURCE_TYPE_NON_EXISTING.equals(primaryTalentResource) && primaryTalentResource != null) {
                        SniPage talentAssetPage = PageFactory.getSniPage(pageManager.getContainingPage(primaryTalentResource));
                        String[] hostedLinks = talentAssetPage.getProperties().get("sni:pageLinks", String[].class);
                        if (hostedLinks != null) {
                            hostedByLink = hostedLinks[0];
                            SniPage talentPage = PageFactory.getSniPage(pageManager, hostedByLink);
                            if (talentPage != null) hostedBy = talentPage.getTitle();
                        }
                    }
                }
            }
        }

        if (StringUtils.isBlank(packageName)) {
            packageName = page.getTitle();
        }
        if (StringUtils.isBlank(packagePath)) {
            packagePath = page.getPath();
        }

        String subPathValue = page.getProperties().get(Functions.SUB_PATH_PROP, "");
        if (StringUtils.isNotBlank(subPathValue)) {
            className = SECTION_FRONT_PAGE;
        } else if (isShow) {
            className = SHOW_LANDING_PAGE;
        } else if (packageIsShow) {
            className = SHOW_SUBPAGE_PAGE;
        } else if (hasHubMenu) {
            className = PACKAGE_LANDING_WITH_HUB_SIMPLE;
        } else if (hasPackageModule) {
            className = PACKAGE_LANDING_SIMPLE;
        } else if (hasHeaderBanner) {
            className = PACKAGE_LANDING_SIMPLE;
        } else {
            className = SECTION_FRONT_PAGE;
        }

        if (!hasSubMenu && SECTION_FRONT_PAGE.equals(className)) {
            noArrowClassName = "no-arrow";
        }

        if (SECTION_FRONT_PAGE.equals(className)) {
            packagePath = "";
        }
    }

    public boolean getHasSubMenu() {
        return hasSubMenu;
    }

    public void setHasSubMenu(boolean hasSubMenu) {
        this.hasSubMenu = hasSubMenu;
    }

    public String getNoArrowClassName() {
        return noArrowClassName;
    }

    public void setNoArrowClassName(String noArrowClassName) {
        this.noArrowClassName = noArrowClassName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public void setPackagePath(String packagePath) {
        this.packagePath = packagePath;
    }

    public String getHostedByLink() {
        return hostedByLink;
    }

    public void setHostedByLink(String hostedByLink) {
        this.hostedByLink = hostedByLink;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getTuneIn() {
        return tuneIn;
    }

    public void setTuneIn(String tuneIn) {
        this.tuneIn = tuneIn;
    }

    public String getHostedBy() {
        return hostedBy;
    }

    public void setHostedBy(String hostedBy) {
        this.hostedBy = hostedBy;
    }

}
