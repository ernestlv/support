package com.scrippsnetworks.wcm.mobile.taglib;

import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.config.SiteConfigService;
import com.scrippsnetworks.wcm.fnr.sitesearch.Dimension;
import com.scrippsnetworks.wcm.fnr.sitesearch.Facet;
import com.scrippsnetworks.wcm.image.ImageAspect;
import com.scrippsnetworks.wcm.image.ImageUrlService;
import com.scrippsnetworks.wcm.image.RenditionInfo;
import com.scrippsnetworks.wcm.metadata.MetadataManager;
import com.scrippsnetworks.wcm.mobile.burgernavigation.BurgerNav;
import com.scrippsnetworks.wcm.mobile.burgernavigation.BurgerNavFactory;
import com.scrippsnetworks.wcm.mobile.circledisplaycarousel.CircleDisplayItem;
import com.scrippsnetworks.wcm.mobile.circledisplaycarousel.CircleItemsFactory;
import com.scrippsnetworks.wcm.mobile.generic1image.GenericOneImage;
import com.scrippsnetworks.wcm.mobile.generic1image.GenericOneImageFactory;
import com.scrippsnetworks.wcm.mobile.generic1video.GenericOneVideo;
import com.scrippsnetworks.wcm.mobile.generic1video.GenericOneVideoFactory;
import com.scrippsnetworks.wcm.mobile.headerstack.HeaderStack;
import com.scrippsnetworks.wcm.mobile.headerstack.HeaderStackFactory;
import com.scrippsnetworks.wcm.mobile.hubnavigation.HubNavElem;
import com.scrippsnetworks.wcm.mobile.hubnavigation.HubNavigationFactory;
import com.scrippsnetworks.wcm.mobile.imageicons.TransformIconClassToMobile;
import com.scrippsnetworks.wcm.mobile.lead3image.LeadThreeImage;
import com.scrippsnetworks.wcm.mobile.lead3image.LeadThreeImageFactory;
import com.scrippsnetworks.wcm.mobile.lead3imagerecipe.LeadThreeImageItemRecipe;
import com.scrippsnetworks.wcm.mobile.lead3imagerecipe.LeadThreeImageRecipeFactory;
import com.scrippsnetworks.wcm.mobile.leadimagewithstack.LeadImageItem;
import com.scrippsnetworks.wcm.mobile.leadimagewithstack.LeadImageWithStackFactory;
import com.scrippsnetworks.wcm.mobile.leadimagewithstack.StackImageItem;
import com.scrippsnetworks.wcm.mobile.schedule.ScheduleWeekDaysFactory;
import com.scrippsnetworks.wcm.mobile.schedule.WeekDays;
import com.scrippsnetworks.wcm.mobile.secondary3imageacross.ImageAcross;
import com.scrippsnetworks.wcm.mobile.secondary3imageacross.ImageAcrossFactory;
import com.scrippsnetworks.wcm.mobile.secondarybottom.SecondaryBottomFactory;
import com.scrippsnetworks.wcm.mobile.secondarybottom.SecondaryBottomItem;
import com.scrippsnetworks.wcm.mobile.secondarygrid.SecondaryGrid;
import com.scrippsnetworks.wcm.mobile.secondarygrid.SecondaryGridItem;
import com.scrippsnetworks.wcm.mobile.secondarygrid.SecondaryGridItemsFactory;
import com.scrippsnetworks.wcm.mobile.secondaryinline.SecondaryInlineVideo;
import com.scrippsnetworks.wcm.mobile.secondaryinline.SecondaryInlineVideoFactory;
import com.scrippsnetworks.wcm.mobile.subnavigation.SubNavElem;
import com.scrippsnetworks.wcm.mobile.subnavigation.SubNavigationFactory;
import com.scrippsnetworks.wcm.mobile.superleadcarousel.SuperleadItem;
import com.scrippsnetworks.wcm.mobile.superleadcarousel.SuperleadItemsFactory;
import com.scrippsnetworks.wcm.mobile.videoPromo.VideoPromo;
import com.scrippsnetworks.wcm.mobile.videoPromo.VideoPromoFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.snipackage.SniPackage;
import com.scrippsnetworks.wcm.util.Constant;
import com.scrippsnetworks.wcm.util.StringUtil;
import com.scrippsnetworks.wcm.util.modalwindow.MobileModalPath;
import com.scrippsnetworks.wcm.video.Video;
import com.scrippsnetworks.wcm.video.channel.Channel;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO9075;
import org.apache.sling.api.SlingException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.query.Query;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Functions {
    private static final Logger log = LoggerFactory.getLogger(Functions.class);
    private static final Set<String> stopWords = new HashSet<String>(Arrays.asList("a", "above", "about", "an", "and", "any", "are", "can", "do", "find", "for", "from", "have", "to", "how", "i", "is", "me", "not", "or", "show", "the", "what", "when", "where", "why", "with", "you", "your", "s"));
    private static final String AUTO_ASPECT = "auto";
    public static final String SUB_PATH_PROP = "subPath";
    private static final Set<String> pageTypesWithSubNav = new HashSet<String>();

    static {
        pageTypesWithSubNav.add("universal-landing");
        pageTypesWithSubNav.add("show");
        pageTypesWithSubNav.add("article-simple");
        pageTypesWithSubNav.add("video-channel");
        pageTypesWithSubNav.add("photo-gallery");
        pageTypesWithSubNav.add("talent");
    }

    private Functions() {
    }

    public static BurgerNav getBurgerNav(Resource resource, SniPage currentPage) {
        return new BurgerNavFactory().withResource(resource).withCurrentPage(currentPage).build();
    }

    public static List<SuperleadItem> getSuperleadItems(Resource resource) {
        return new SuperleadItemsFactory().withResource(resource).build();
    }

    public static String getImageUrlWithDefaultImage(String damPath, String rendition, String aspect, SlingHttpServletRequest slingRequest, boolean useDefaultImage) {
        RenditionInfo renditionInfoEnum = null;
        ImageAspect imageAspectEnum = null;
        if (rendition != null) {
            try {
                renditionInfoEnum = RenditionInfo.valueOf(rendition);
            } catch (IllegalArgumentException ex) {
                log.warn("Rendention is not found", ex);
                return "";
            }
        }

        BundleContext bundleContext = FrameworkUtil.getBundle(Functions.class).getBundleContext();
        ServiceReference imageUrlServiceRef = bundleContext.getServiceReference(ImageUrlService.class.getName());
        ImageUrlService imageUrlService = (ImageUrlService) bundleContext.getService(imageUrlServiceRef);
        ResourceResolver resourceResolver = slingRequest.getResourceResolver();
        Resource imageRes = null;
        try {
            imageRes = resourceResolver.getResource(damPath);
        } catch (SlingException exception) {
            log.warn("ImageRes is not found", exception);
        }
        if (StringUtils.isNotBlank(aspect)) {
            try {
                if (AUTO_ASPECT.equals(aspect)) {
                    imageAspectEnum = getNearestAspect(damPath, imageRes, renditionInfoEnum);
                    if (imageAspectEnum == null && imageRes != null && !StringUtils.isBlank(damPath)) {
                        return "";
                    }
                } else {
                    imageAspectEnum = ImageAspect.valueOf(aspect);
                }

            } catch (IllegalArgumentException ex) {
                log.warn("Aspect is not found", ex);
                return "";
            }
        }

        if (imageRes == null || StringUtils.isBlank(damPath)) {
            if (useDefaultImage) {
                try {
                    String path = slingRequest.getPathInfo().replace("/content/", "");
                    path = path.substring(0, path.indexOf("/"));
                    ServiceReference[] siteConfigServiceRef = bundleContext.getServiceReferences(SiteConfigService.class.getName(), "(siteName=" + path + ")");
                    SiteConfigService siteConfigService = (SiteConfigService) bundleContext.getService(siteConfigServiceRef[0]);
                    damPath = siteConfigService.getDefaultImage();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            } else {
                return "";
            }
        }

        return imageUrlService.getImageUrl(damPath, renditionInfoEnum, imageAspectEnum);
    }

    private static ImageAspect getNearestAspect(String damPath, Resource imageRes, RenditionInfo renditionInfoEnum) {
        if (StringUtils.isNotBlank(damPath)) {
            if (imageRes != null && !Resource.RESOURCE_TYPE_NON_EXISTING.equals(imageRes.getResourceType())) {
                Asset asset = imageRes.adaptTo(Asset.class);
                if (asset != null) {
                    if (renditionInfoEnum != null) {
                        String mdWidth = asset.getMetadataValue("tiff:ImageWidth");
                        String mdHeight = asset.getMetadataValue("tiff:ImageLength");
                        if (mdWidth != null && mdHeight != null && !mdWidth.isEmpty() && !mdHeight.isEmpty()) {
                            try {
                                int width = Integer.parseInt(mdWidth);
                                int height = Integer.parseInt(mdHeight);
                                return renditionInfoEnum.getNearestAspect(width, height);
                            } catch (NumberFormatException e) {
                                log.warn("error converting metadata height and width to integer");
                            }
                        }
                    }
                } else {
                    log.debug("could not adapt Resource {} to Asset", damPath);
                }
            } else {
                log.debug("could not acquire image resource {}", damPath);
            }
        }
        return null;
    }

    public static String getImageUrl(String damPath, String rendition, String aspect, SlingHttpServletRequest slingRequest) {
        return getImageUrlWithDefaultImage(damPath, rendition, aspect, slingRequest, true);
    }

    public static List<ImageAcross> getImageAcrossItems(Resource resource) {
        return new ImageAcrossFactory().withResource(resource).build();
    }

    public static List<CircleDisplayItem> getCircleDisplayItems(PageManager pageManager, Resource resource) {
        return new CircleItemsFactory().withResource(resource).withPageManager(pageManager).build();
    }

    public static String transformPathToUrl(String path, ResourceResolver resourceResolver) {
        String link = path;

        if (link == null || link == "") {
            return "#";
        }
        if (link.charAt(0) != '/') {
            return link;
        }

        Resource linkRes = resourceResolver.getResource(link);
        if (linkRes == null && !StringUtils.contains(link, ".html/")) {
            return "#";
        }

        if (link.contains(".html")) {
            return link;
        }

        return link + ".html";
    }

    public static Dimension getSelectedDimensionOfFacet(List<Dimension> selectedDimensions, Facet facet) {
        for (Dimension selectedDimension : selectedDimensions) {
            if (facet.getDisplayName().equals(selectedDimension.getFacetName())) {
                return selectedDimension;
            }
        }
        return null;
    }

    public static String getMediaUrl(String link, ResourceResolver resolver, Integer pageNumber) {
        if (StringUtils.isNotEmpty(link)) {
            BundleContext bundleContext = FrameworkUtil.getBundle(MobileModalPath.class).getBundleContext();
            ServiceReference MobileModalPathRef = bundleContext.getServiceReference(MobileModalPath.class.getName());
            final MobileModalPath mobileModalPathservice = (MobileModalPath) bundleContext.getService(MobileModalPathRef);
            link = new String(mobileModalPathservice.getModalWindowPath(resolver, link, pageNumber));
        }
        return link;
    }

    public static String getMediaUrl(String link, ResourceResolver resolver) {
        if (StringUtils.isNotEmpty(link)) {
            BundleContext bundleContext = FrameworkUtil.getBundle(MobileModalPath.class).getBundleContext();
            ServiceReference MobileModalPathRef = bundleContext.getServiceReference(MobileModalPath.class.getName());
            final MobileModalPath mobileModalPathservice = (MobileModalPath) bundleContext.getService(MobileModalPathRef);
            link = new String(mobileModalPathservice.getModalWindowPath(resolver, link));
        }
        return link;
    }

    public static String getHighLightingText(String text, String keywords, String highlightedClass) {
        String[] keywordsArray = keywords.split("[^\\w']+");

        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                if (s1.length() > s2.length())
                    return -1;

                if (s2.length() > s1.length())
                    return 1;

                return 0;
            }
        };
        Set<String> keywordsSet = new TreeSet<String>(comparator);
        if (!stopWords.contains(keywords.toLowerCase()) && StringUtils.isNotBlank(keywords) && keywords.length() > 1) {
            keywordsSet.add(keywords);
        }
        for (String keyword : keywordsArray) {
            if (!stopWords.contains(keyword.toLowerCase()) && StringUtils.isNotBlank(keyword) && keyword.length() > 1) {
                keywordsSet.add(keyword);
            }
        }
        try {
            for (String keyword : keywordsSet) {
                text = text.replaceAll("(?i)" + keyword, "<b class=\"" + highlightedClass + "\">$0</b>");
            }
        } catch (IndexOutOfBoundsException exception) {
            log.debug("No replacing target", exception);
        }
        return text;
    }

    public static String getDeepLinkVideo(Channel channel, String videoId) {
        SniPage sniPage = null;

        if (channel != null) {
            sniPage = channel.getSniPage();
        }

        if (sniPage != null) {
            String path = sniPage.getPath();
            if (StringUtils.isNotEmpty(path)) {
                if (StringUtils.isNotEmpty(videoId)) {
                    return path + "." + videoId + Constant.HTML;
                }
                return path + Constant.HTML;
            }
        }

        return "";
    }

    public static Video getTargetVideoForMobileChannelPage(Channel channel, RequestPathInfo requestPathInfo) {
        String[] selectors = requestPathInfo.getSelectors();
        for (String selector : selectors) {
            if (!"mobile".equals(selector) && StringUtils.isNumeric(selector)) {
                List<Video> channelVideos = channel.getVideos();
                for (Video video : channelVideos) {

                    if (video.getVideoId() != null && selector.equals(video.getVideoId())) {
                        return video;
                    }
                }
            }
        }
        return channel.getFirstVideo();
    }

    public static List<Video> getUpNextVideos(List<Video> videos, Video targetVideo) {
        List<Video> filteredVideos = new ArrayList<Video>();
        if (targetVideo == null) {
            return videos;
        }
        for (Video video : videos) {
            if (!targetVideo.getVideoId().equals(video.getVideoId())) {
                filteredVideos.add(video);
            }
        }
        return filteredVideos;
    }

    public static String getMetaDataValues(SniPage page) {
        String mdProps = null;
        if (page != null) {
            try {
                MetadataManager metadataManager = page.getMetadataManager();
                if (metadataManager != null) {
                    mdProps = metadataManager.getMetadataManagerJson();
                }
            } catch (Exception e) {
                log.error("Exception in MetaDataManager", e);
            }
        }
        return mdProps;
    }

    public static List<SubNavElem> getSubNavList(Resource resource, SniPage currentPage) {
        if (resource == null) {
            return new ArrayList<SubNavElem>();
        }
        return new SubNavigationFactory().withResource(resource).withCurrentPage(currentPage).build();
    }

    private static String getSubNavFromResources(Iterator<Resource> resources, ResourceResolver resolver) {
        String result = "";

        while (resources.hasNext()) {
            Resource r = resources.next();
            if (r == null) continue;
            String type = r.getResourceType();
            if (type == null) continue;
            if (type.endsWith("sub-navigation")) {
                return r.getPath();
            } else {
                ValueMap vMap = r.adaptTo(ValueMap.class);
                String refPath = vMap.get("path", "");
                Resource refRes = resolver.getResource(refPath);
                if (refRes == null) continue;
                String refType = refRes.getResourceType();
                if (refType != null && refType.endsWith("sub-navigation")) {
                    result = refPath;
                }
            }
        }
        return result;
    }

    public static Resource getSubMenuResource(SniPage page, ResourceResolver resolver) {
        if (!pageTypesWithSubNav.contains(page.getPageType())) {
            return null;
        }
        Resource subMenuReference = null;
        Resource pageRes = page.getContentResource();
        ValueMap vmPage = pageRes.adaptTo(ValueMap.class);
        String subNavPath = vmPage.get(SUB_PATH_PROP, "");
        if (StringUtils.isNotBlank(subNavPath)) {
            subMenuReference = resolver.getResource(subNavPath);
        }

        if (subMenuReference == null) {
            Resource superLead = resolver.getResource(page.getContentResource(), "superlead");
            String _result = null;

            if (superLead != null) {
                Iterator<Resource> resourceIterator = resolver.listChildren(superLead);
                _result = getSubNavFromResources(resourceIterator, resolver);
            }

            if (StringUtils.isEmpty(_result)) {
                Resource contentWell = resolver.getResource(page.getContentResource(), "content-well");
                if (contentWell != null) {
                    Iterator<Resource> resourceIterator = resolver.listChildren(contentWell);
                    _result = getSubNavFromResources(resourceIterator, resolver);
                }
            }
            if (StringUtils.isNotEmpty(_result)) subMenuReference = resolver.getResource(_result);
        }

        if (subMenuReference == null) {
            SniPackage sniPackage = page.getSniPackage();
            if (sniPackage != null) {
                List<Resource> modules = sniPackage.getModules();
                for (Resource module : modules) {
                    ValueMap vMap = module.adaptTo(ValueMap.class);
                    String pathToSubNav = "";
                    if (vMap != null) {
                        pathToSubNav = vMap.get("path", "");
                    }
                    if (StringUtils.isNotEmpty(pathToSubNav)) {
                        Resource inheritedModule = resolver.getResource(pathToSubNav);
                        if (inheritedModule != null && inheritedModule.getResourceType().endsWith("sub-navigation")) {
                            subMenuReference = inheritedModule;
                        }
                    }
                }
            }
        }

        String subPath = (subMenuReference != null) ? subMenuReference.getPath() : "";
        if (StringUtils.isEmpty(subPath) && !page.isPackageAnchor()) {
            SniPage anchorPage = page.getPackageAnchor();
            if (anchorPage != null) {
                subMenuReference = getSubMenuResource(anchorPage, resolver);
            }
        }

        return subMenuReference;
    }

    public static String getHubMenuPath(SniPage page, ResourceResolver resolver) {
        String hubPath = null;

        StringBuilder builder = new StringBuilder();
        builder.append("select * from [nt:unstructured] as hub where ISDESCENDANTNODE(hub, \"")
		.append(page.getPath())
		.append("\") ")
        .append("and (hub.[sling:resourceType] = \"")
        .append("sni-food/components/util/hub-navigation").append("\" ")
        .append("or hub.[sling:resourceType] = \"")
        .append("sni-core/components/util/hub-navigation").append("\") ");
        Iterator<Resource> iter = resolver.findResources(builder.toString(),
                Query.JCR_SQL2);
        if (iter.hasNext()) {
            Resource hubRes = iter.next();
            return hubRes.getPath();
        }
        return hubPath;
    }

    public static List<HubNavElem> getHubNavList(SniPage currentPage, Integer size) {
        return new HubNavigationFactory().withCurrentPage(currentPage).withSize(size).build();
    }

    public static SecondaryGrid getSecondaryGrid(final Resource resource) {
        return new SecondaryGridItemsFactory().withResource(resource).build();
    }

    public static List<SecondaryGridItem> getSecondaryGridMore(final Resource resource, final SlingHttpServletRequest slingRequest) {

        if (slingRequest == null) {
            return null;
        }
        int tabindex = 1;
        int startindex = 1;
        RequestPathInfo pathInfo = slingRequest.getRequestPathInfo();
        Pattern tabPattern = Pattern.compile("^tab-([0-9]+)$");
        Pattern startIndxPattern = Pattern.compile("^startind-([0-9]+)$");
        for (String selector : pathInfo.getSelectors()) {
            Matcher matcherStartIndex = startIndxPattern.matcher(selector);
            Matcher matcherTab = tabPattern.matcher(selector);
            if (matcherStartIndex.matches()) {
                startindex = Integer.valueOf(matcherStartIndex.group(1));
            }
            if (matcherTab.matches()) {
                tabindex = Integer.valueOf(matcherTab.group(1));
            }
        }

        return new SecondaryGridItemsFactory().withResource(resource).buildItems(tabindex, startindex);
    }

    public static GenericOneImage getGenericOneImage(final Resource resource) {
        return new GenericOneImageFactory().withResource(resource).build();
    }

    public static GenericOneVideo getGenericOneVideo(final Resource resource) {
        return GenericOneVideoFactory.buildWithResource(resource);
    }

    public static LeadThreeImage getLeadThreeImage(final Resource resource) {
        return new LeadThreeImageFactory().withResource(resource).build();
    }

    public static HeaderStack getHeaderStack(ResourceResolver resolver, SniPage page) {
        return new HeaderStackFactory().withCurrentPage(page).withResolver(resolver).build();
    }

    public static List<SubNavElem> getSubNavListFromPage(ResourceResolver resolver, SniPage currentPage) {
        Resource subNavRes = getSubMenuResource(currentPage, resolver);
        return getSubNavList(subNavRes, currentPage);
    }

    public static LeadImageItem getLeadImageItem(Resource resource) {
        return new LeadImageWithStackFactory().buildLeadImage(resource);
    }

    public static List<StackImageItem> getStackImageItems(Resource resource) {
        return new LeadImageWithStackFactory().withResource(resource).buildStackImages();
    }

    public static int getValidSlideCount(Resource resource) {
        return new LeadImageWithStackFactory().getCurrentLeadImagesSize(resource);
    }

    public static List<Resource> getResourcesFromContainer(String containerPath, String nodeTypes, Resource curResource) {
        List<Resource> resultResources = new ArrayList<Resource>();

        Pattern nodeTypesSplitter = Pattern.compile(",");
        String[] nodeTypesArray = nodeTypesSplitter.split(nodeTypes);
        Set<String> nodeTypesSet = new HashSet<String>();
        for (String type : nodeTypesArray) {
            nodeTypesSet.add(type);
        }

        Resource container = curResource.getChild(containerPath);

        if (container == null) {
            return resultResources;
        }

        Iterator<Resource> containerIterator = container.listChildren();

        while (containerIterator.hasNext()) {
            Resource tmpResource = containerIterator.next();
            String tmpType = "";
            if (tmpResource != null) {
                tmpType = tmpResource.getResourceType();
            }
            if (!nodeTypesSet.contains("foundation/components/reference") && "foundation/components/reference".equals(tmpType)) {
                ValueMap vm = tmpResource.adaptTo(ValueMap.class);
                String path = vm.get("path", "");
                tmpResource = curResource.getResourceResolver().getResource(path);
                if (tmpResource != null) {
                    tmpType = tmpResource.getResourceType();
                }
            }
            if (nodeTypesSet.contains(tmpType)) {
                resultResources.add(tmpResource);
            }
        }

        return resultResources;
    }

    public static WeekDays getScheduleWeekDays(SlingHttpServletRequest slingRequest) {
        return new ScheduleWeekDaysFactory().withCurrentPage(slingRequest).build();
    }

    public static List<SecondaryBottomItem> getSecondaryBottom(final Resource resource) {
        return new SecondaryBottomFactory().withResource(resource).build();
    }

    public static String getPageTitle(PageManager pageManager, String path) {
        if (pageManager == null || path == null || path.isEmpty()) {
            return null;
        }

        Pattern isContainHtml = Pattern.compile(".*[.]html$");
        if (isContainHtml.matcher(path).matches()) {
            path = path.replaceFirst("[.]html$", "");
        }
        Page thePage = pageManager.getPage(path);
        if (thePage != null) {
            /*SniPage sniPage = PageFactory.getSniPage(thePage);
            if (sniPage != null) {
                return sniPage.getTitle();
            }*/
            return thePage.getTitle();
        }
        return "";
    }

    public static VideoPromo getVideoPromo(final Resource resource) {
        return new VideoPromoFactory().withResource(resource).build();
    }

    public static String transformIconClassToMobile(final String iconClass) {
        return TransformIconClassToMobile.modify(iconClass);
    }

    public static String replaceHtmlTags(final String text, final String oldTag, final String newTag) {
        String updatedText = StringUtil.removeMarkupExceptAnchors(text);
        if (StringUtils.isNotBlank(updatedText)) {
            updatedText = updatedText.replaceAll("<" + oldTag + "[^>]*>", "<" + newTag + ">").replaceAll("</" + oldTag + "[^>]*>", "</" + newTag + ">");
        }
        return updatedText;
    }

    public static int getPositionAdblockInArticleContainer(String containerPath, Resource curResource) {
        Resource container = curResource.getChild(containerPath);

        if (container == null) {
            return -1;
        }

        Iterator<Resource> containerIterator = container.listChildren();
        int lastBlockPosition = -1;
        while (containerIterator.hasNext()) {
            Resource tmpResource = containerIterator.next();
            String tmpType = "";
            if (tmpResource != null) {
                tmpType = tmpResource.getResourceType();
                lastBlockPosition++;
                if ("sni-food/components/pagetypes/article-simple/components/rich-text-editor".equals(tmpType)) {
                    ValueMap vm = tmpResource.adaptTo(ValueMap.class);
                    if (vm != null && StringUtils.isNotBlank(vm.get("text", ""))) {
                        return lastBlockPosition;
                    }
                } else if ("sni-food/components/util/page-break".equals(tmpType)) {
                    return lastBlockPosition;
                }
            }
        }
        return lastBlockPosition;
    }

    public static List<LeadThreeImageItemRecipe> getLeadThreeImageRecipe(Map<String, Object> searchResponseMap, Resource resource) {
        return new LeadThreeImageRecipeFactory().withResourceAndSearchMap(searchResponseMap, resource).build();
    }

    public static SecondaryInlineVideo getSecondaryInlineVideo(final Resource resource) {
        return new SecondaryInlineVideoFactory().withResource(resource).build();
    }
    
    public static String getInheritedProperty(String propertyName, SniPage page) {
        if (StringUtils.isNotBlank(propertyName) && page != null) {
            String propValue = page.getProperties().get(propertyName, String.class);
            if (StringUtils.isNotBlank(propValue)) {
                return propValue;
            }
            SniPage packageAnchor = page.getPackageAnchor();
            if (packageAnchor != null) {
                return packageAnchor.getProperties().get(propertyName, String.class);
            }
        }
        return "";
    }
    
    
}
