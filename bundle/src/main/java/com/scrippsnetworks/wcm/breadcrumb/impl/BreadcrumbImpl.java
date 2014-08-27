package com.scrippsnetworks.wcm.breadcrumb.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.day.cq.wcm.api.Page;

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.breadcrumb.Breadcrumb;
import com.scrippsnetworks.wcm.breadcrumb.BreadcrumbHubLabels;
import com.scrippsnetworks.wcm.breadcrumb.crumb.Crumb;
import com.scrippsnetworks.wcm.breadcrumb.crumb.CrumbFactory;
import com.scrippsnetworks.wcm.breadcrumb.crumb.CrumbTypes;
import com.scrippsnetworks.wcm.config.SiteConfigService;
import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.util.PageTypes;

/**
 * @author Jonathan Bell
 *         Date: 10/1/2013
 * 
 */
public class BreadcrumbImpl implements Breadcrumb {

    private static final Logger LOG = LoggerFactory.getLogger(BreadcrumbImpl.class);
    private static final List<String> DEFAULT_ATOZ_SECTIONS = Arrays.asList("recipes");
    private static final List<String> DEFAULT_HIDDEN_SECTIONS = Arrays.asList("features", "site", "sponsored");
    private static final List<String> DEFAULT_INDEX_SECTIONS = Arrays.asList("topics");
    private static final String INDEX_SELECTOR = "^123|[A-W]|XYZ$";
    private static final String CONTENT_ROOT = "/content";
    private static final String HOME_TITLE = "Home";
    private static final String HOME_LINK = "/";
    private static final String ATOZ_STUMP = "a-z";

    private List<Crumb> crumbs;
    private Map crumbMap;
    private SniPage sniPage;
    private SniPage sectionPage;
    private SniPage parentPage;
    private SniPage hubMasterPage;
    private List<String> aToZSections;
    private List<String> hiddenSections;
    private List<String> indexSections;
    private boolean isSectionTemplated = false;
    private boolean hideInvalidPages = true;
    private SiteConfigService siteConfig = null;

    /**
     * Implementation of SNI Breadcrumb
     * @param page SniPage
     */
    public BreadcrumbImpl(final SniPage sniPage) {
        this.sniPage = sniPage;
        this.siteConfig = sniPage.getSiteConfigService();

        if (siteConfig != null) {
            this.aToZSections = Arrays.asList(siteConfig.getBreadcrumbAToZSections());
            this.hiddenSections = Arrays.asList(siteConfig.getBreadcrumbHiddenSections());
            this.indexSections = Arrays.asList(siteConfig.getBreadcrumbIndexSections());
        } else {
            this.aToZSections = DEFAULT_ATOZ_SECTIONS;
            this.hiddenSections = DEFAULT_HIDDEN_SECTIONS;
            this.indexSections = DEFAULT_INDEX_SECTIONS;
        }

        initializeCrumbs();
    }

    private void initializeCrumbs() {
        if (sniPage.getDepth() > 2) {
            this.hubMasterPage = getHubMasterPage();
            this.parentPage = getPage(sniPage.getParent());
            this.sectionPage = getPage(sniPage.getAbsoluteParent(2));
            this.crumbs = new ArrayList<Crumb>();
            this.crumbMap = Collections.synchronizedMap(new LinkedHashMap());
            addCrumbForHome();
            addCrumbForIndexSection();
            addCrumbForSection();
            addCrumbForTopic();
            addCrumbForShow();
            addCrumbForHub();
            addCrumbForPage();
            addCrumbForSelector();
        }
    }

    public List<Crumb> getCrumbs() {
        if (crumbs.size() == 0) {
            crumbs.addAll(crumbMap.values());
        }

        return crumbs;
    }

    private void addCrumbForHome() {
        addCrumb(CrumbTypes.HOME, HOME_TITLE, HOME_LINK);
    }

    private void addCrumbForIndexSection() {
        if (sectionPage != null && indexSections.contains(sectionPage.getName().toLowerCase())) {
            String indexPath = sectionPage.getPath() + "/" + ATOZ_STUMP;
            SniPage indexPage = getPage(indexPath);
            if (indexPage != null && !indexPage.getUrl().equals(sniPage.getUrl())) {
                addCrumb(CrumbTypes.SECTION, indexPage.getTitle(), indexPage.getUrl());
            }
        }
    }

    private void addCrumbForSection() {
        boolean hideInNavigation = false;

        if (sectionPage != null) {
            if (aToZSections.contains(sectionPage.getName().toLowerCase())
                    && sniPage.getName().equals(ATOZ_STUMP)) {
                hideInNavigation = true;
            }
            if (StringUtils.isEmpty(sectionPage.getProperties().get(PagePropertyConstants.PROP_CQ_TEMPLATE, String.class))
                    && hideInvalidPages) {
                LOG.warn("Hiding section page {} with no template", sectionPage.getTitle());
                hideInNavigation = true;
            }
            if (hiddenSections.contains(sectionPage.getName().toLowerCase())) {
                hideInNavigation = true;
            }
            if (sectionPage.getUrl().equals(sniPage.getUrl())) {
                hideInNavigation = true;
            }

            if (!hideInNavigation) {
                addCrumb(CrumbTypes.SECTION, sectionPage.getTitle(), sectionPage.getUrl());
            }
        }
    }

    private void addCrumbForTopic() {
        PageTypes type = PageTypes.findPageType(sniPage.getPageType());
        if (type == PageTypes.TOPIC) {
            if (parentPage != null) {
                PageTypes parentType = PageTypes.findPageType(parentPage.getPageType());
                if (parentType == PageTypes.TOPIC) {
                    addCrumb(CrumbTypes.PARENT, parentPage.getTitle(), parentPage.getUrl());
                }
            }

        }
    }

    private void addCrumbForShow() {
        int maxLevel = sniPage.getDepth()-1;

        for (int level=1; level < maxLevel; level++) {
            SniPage ancestorPage = getPage(sniPage.getParent(level));
            if (ancestorPage != null) { 
                PageTypes type = PageTypes.findPageType(ancestorPage.getPageType());
                if (type == PageTypes.SHOW) {
                    addCrumb(CrumbTypes.SHOW, ancestorPage.getTitle(), ancestorPage.getUrl());
                    break;
                }
            }
        }
    }

    private void addCrumbForHub() {
        if (hubMasterPage != null && isPageHubChild()) {
            addCrumb(CrumbTypes.HUB, hubMasterPage.getTitle(), hubMasterPage.getUrl());
            addCrumb(CrumbTypes.PAGE, this.getHubbedPageLabel(), sniPage.getUrl());
       }
    }

    private void addCrumbForPage() {
        addCrumb(CrumbTypes.PAGE, sniPage.getTitle(), sniPage.getUrl());
    }

    private void addCrumbForSelector() {
        PageTypes type = PageTypes.findPageType(sniPage.getPageType());
        if (type == PageTypes.INDEX) {
            List<String> pageSelectors = sniPage.getSelectors();
            for (String selector : pageSelectors) {
                Pattern pattern = Pattern.compile(INDEX_SELECTOR);
                Matcher matcher = pattern.matcher(selector.toUpperCase());
                if (matcher.matches()) {
                    addCrumb(CrumbTypes.SELECTOR, selector.toUpperCase(), null);
                    break;
                }
            }
        }
    }

    private SniPage getHubMasterPage() {
        SniPage page = null;

        Hub hubObj = sniPage.getHub();
        if (hubObj != null) {
            page = hubObj.getHubMaster();
        }

        return page;
    }

    private boolean isPageHubChild() {
        Hub hubObj = sniPage.getHub();

        return hubObj != null && hubObj.isPageHubChild(sniPage); 
    }

    /**
     * Returns the current page hub label 
     * @return String the current page hub label 
     */
    private String getHubbedPageLabel() {
        BreadcrumbHubLabels hubLabel = null;
        String label = null;

        PageTypes type = PageTypes.findPageType(sniPage.getPageType());
        if (type != null) {
            try {
                hubLabel = BreadcrumbHubLabels.valueOf(type.name());
                label = hubLabel.title();
            } catch (IllegalArgumentException iae) {
                label = sniPage.getTitle();
                LOG.debug("Missing breadcrumb hub asset label for {}", type.pageType());
            }
        }

        return label;
    }

    private SniPage getPage(String pagePath) {
        SniPage factoryPage = PageFactory.getSniPage(sniPage.getPageManager(), pagePath);
        if (factoryPage != null && (factoryPage.getTitle() == null || factoryPage.getUrl() == null)) {
            factoryPage = null;
        }

        return factoryPage;
    }

    private SniPage getPage(Page page) {
        SniPage factoryPage = PageFactory.getSniPage(page);
        if (factoryPage != null && (factoryPage.getTitle() == null || factoryPage.getUrl() == null)) {
            factoryPage = null;
        }

        return factoryPage;
    }

    private boolean isDuplicateCrumb(Crumb cmpCrumb) {
        boolean isDupe = false;

        if (crumbMap.containsKey(CrumbTypes.SHOW) && cmpCrumb.getType() == CrumbTypes.HUB) {
            Crumb showCrumb = (Crumb)crumbMap.get(CrumbTypes.SHOW);
            if (showCrumb.getUrl().equals(cmpCrumb.getUrl())) {
                isDupe = true;
            }
        } else if (crumbMap.containsKey(cmpCrumb.getType())) {
            isDupe = true;
        }

        return isDupe;
    }

    private void addCrumb(CrumbTypes type, String title, String url) {
        Crumb newCrumb = new CrumbFactory()
            .withType(type)
            .withTitle(title)
            .withUrl(url)
            .build();

        if (newCrumb != null) {
            if (! isDuplicateCrumb(newCrumb)) {
                crumbMap.put(newCrumb.getType(), newCrumb);
            }
        }
    }

}
