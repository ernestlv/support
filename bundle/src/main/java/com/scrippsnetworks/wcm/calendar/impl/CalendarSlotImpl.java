package com.scrippsnetworks.wcm.calendar.impl;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.calendar.CalendarSlot;
import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.image.SniImageFactory;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Anatoli_Zapolski
 * Date: 9/9/13
 * Time: 12:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class CalendarSlotImpl implements CalendarSlot {
    private static final String THUMBNAIL_TITLE_PROP = "thumbnailTitle";
    private static final String TITLE_PROP = "title";
    private static final String TITLE_LINK_PROP = "titleLink";
    private static final String DESRIPTION_PROP = "description";
    private static final String SLIDE_LINK_PROP = "slide-link";
    private static final String FREE_ENABLED_PROP = "free-enabled";
    private static final String FREE_LABEL_PROP = "free-label";
    private static final String FREE_LINK_PROP = "free-link";
    private static final String IMAGE_NODE_NAME = "image";
    private static final String IMAGE_FILE_REFERENCE_PROP = "fileReference";

    private static final String ASSET_PROPERTY_NAME = "slide-link";
    private static final String FREELINK_ACTIVE_PROPERTY_NAME = "free-enabled";

    private static final String VIDEO_ICON_CLASS = "ss-video";
    private static final String PHOTO_ICON_CLASS = "ss-layers";

    private final static Logger log = LoggerFactory.getLogger(CalendarImpl.class);

    private static Map<String, String> lam;
    private static Set<String> photoGalleryPages;
    private static Set<String> videoPages;

    static{
        lam = new HashMap<String, String>();
        lam.put("article-simple", "You Might Also Like");
        lam.put("calendar", "You Might Also Like");
        lam.put("episode", "See More");
        lam.put("recipe", "Get the Recipe");
        lam.put("show", "See More About");
        lam.put("talent", "Check Out More");
        lam.put("video-channel", "Watch Videos About");
        lam.put("video", "Watch a Video About");
        lam.put("video-player", "Watch Videos About");
        lam.put("photo-gallery", "You Might Also Like");
    }
    static{
        photoGalleryPages = new HashSet<String>();
        photoGalleryPages.add("photo-gallery");
    }
    static{
        videoPages = new HashSet<String>();
        videoPages.add("video");
        videoPages.add("video-channel");
        videoPages.add("video-player");
    }

    private Node curNode;
    private ResourceResolver resourceResolver;
    private Resource resource;
    private ValueMap vm;

    private String nodeName;
    private String path;
    private String title;
    private String thumbnailTitle;
    private String description;
    private String titleLink;
    private String slideLink;
    private Boolean freeEnabled;
    private String freeLabel;
    private String freeLink;
    private SniImage image;
    private int dayIndex;
    private String iconType;

    private String assetType;
    private SniPage assetPage;
    private String linkUrl;
    private String linkText;
    private String linkLabel;

    public CalendarSlotImpl(Node curNode, ResourceResolver resourceResolver, int dayIndex) {
        this.curNode = curNode;
        this.dayIndex = dayIndex;
        this.resourceResolver = resourceResolver;
        try {
            resource = resourceResolver.getResource(curNode.getPath());
        } catch (RepositoryException re) {
            log.error("RepositoryException caught: ", re);
        }
        if (resource != null) {
            this.vm = resource.adaptTo(ValueMap.class);
        }

        inspectAsset();
        customizeCallToAction();
    }

    private void inspectAsset() {
        assetPage = null;
        assetType = "";
        linkUrl = "";
        linkText = "";

        if (resource != null && vm.containsKey(ASSET_PROPERTY_NAME)) {
            String assetLink = vm.get(ASSET_PROPERTY_NAME, String.class);
            String overrideStatus = vm.get(FREELINK_ACTIVE_PROPERTY_NAME, String.class);
            ResourceResolver rr = resource.getResourceResolver();
            Resource assetResource = rr.getResource(assetLink);
            if (assetResource != null && overrideStatus == null) {
                assetPage = PageFactory.getSniPage(assetResource.adaptTo(Page.class));
                if (assetPage != null) {
                    assetType = assetPage.getPageType();
                    linkUrl = assetLink;
                    linkText = assetPage.getTitle();
                }
            }
        }
    }

    private void customizeCallToAction() {
        linkLabel = "";

        if (!getFreeEnabled()) {
            if (lam.containsKey(assetType)) {
                linkLabel = lam.get(assetType);
            }
        }
    }

    @Override
    public Node getCurNode() {
        return curNode;
    }

    @Override
    public String getNodeName() {
        if (nodeName == null) {
            try {
                nodeName = curNode.getName();
            } catch (RepositoryException re) {
                log.error("RepositoryException caught: ", re);
            }
        }
        return nodeName;
    }

    @Override
    public String getPath() {
        if (path == null) {
            try {
                path = curNode.getName();
            } catch (RepositoryException re) {
                log.error("RepositoryException caught: ", re);
            }
        }
        return path;
    }

    @Override
    public String getTitle() {
        if (title == null) {
            try {
                if (curNode == null || !curNode.hasProperty(TITLE_PROP)) {
                    title = "";
                    return title;
                }

                title = curNode.getProperty(TITLE_PROP).getString();

            } catch (RepositoryException re) {
                log.error("RepositoryException caught: ", re);
            }
        }
        return title;
    }

    public String getThumbnailTitle() {
        if (thumbnailTitle == null) {
            try {
                if (curNode == null || !curNode.hasProperty(THUMBNAIL_TITLE_PROP)) {
                    thumbnailTitle = getTitle();
                    return thumbnailTitle;
                }

                thumbnailTitle = curNode.getProperty(THUMBNAIL_TITLE_PROP).getString();

            } catch (RepositoryException re) {
                log.error("RepositoryException caught: ", re);
            }
        }
        return thumbnailTitle;
    }

    @Override
    public String getDescription() {
        if (description == null) {
            try {
                if (curNode == null || !curNode.hasProperty(DESRIPTION_PROP)) {
                    description = "";
                    return description;
                }

                description = curNode.getProperty(DESRIPTION_PROP).getString();

            } catch (RepositoryException re) {
                log.error("RepositoryException caught: ", re);
            }
        }
        return description;
    }

    @Override
    public String getTitleLink() {
        if (titleLink == null) {
            try {
                if (curNode == null || !curNode.hasProperty(TITLE_LINK_PROP)) {
                    titleLink = "";
                    return titleLink;
                }

                titleLink = curNode.getProperty(TITLE_LINK_PROP).getString();

            } catch (RepositoryException re) {
                log.error("RepositoryException caught: ", re);
            }
        }
        return titleLink;
    }

    @Override
    public String getSlideLink() {
        if (slideLink == null) {
            try {
                if (curNode == null || !curNode.hasProperty(SLIDE_LINK_PROP)) {
                    slideLink = "";
                    return slideLink;
                }

                slideLink = curNode.getProperty(SLIDE_LINK_PROP).getString();

            } catch (RepositoryException re) {
                log.error("RepositoryException caught: ", re);
            }
        }
        return slideLink;
    }

    @Override
    public boolean getFreeEnabled() {
        if (freeEnabled == null) {
            try {
                if (curNode != null && curNode.hasProperty(FREE_ENABLED_PROP)) {
                    freeEnabled = curNode.getProperty(FREE_ENABLED_PROP).getBoolean();
                }

            } catch (RepositoryException re) {
                log.error("RepositoryException caught: ", re);
            }
        }
        return freeEnabled != null ? freeEnabled : false;
    }

    @Override
    public String getFreeLabel() {
        if (freeLabel == null) {
            try {
                if (curNode == null || !curNode.hasProperty(FREE_LABEL_PROP)) {
                    freeLabel = "";
                    return freeLabel;
                }

                freeLabel = curNode.getProperty(FREE_LABEL_PROP).getString();

            } catch (RepositoryException re) {
                log.error("RepositoryException caught: ", re);
            }
        }
        return freeLabel;
    }

    @Override
    public String getFreeLink() {
        if (freeLink == null) {
            try {
                if (curNode == null || !curNode.hasProperty(FREE_LINK_PROP)) {
                    freeLink = "";
                    return freeLink;
                }

                freeLink = curNode.getProperty(FREE_LINK_PROP).getString();

            } catch (RepositoryException re) {
                log.error("RepositoryException caught: ", re);
            }
        }
        return freeLink;
    }

    @Override
    public SniImage getImage() {
        if (image == null) {
            try {
                if (curNode == null || !curNode.hasNode(IMAGE_NODE_NAME)) {
                    image = new SniImageFactory().withPath(null).build();
                    return image;
                }

                Node imageNode = curNode.getNode(IMAGE_NODE_NAME);

                if (!imageNode.hasProperty(IMAGE_FILE_REFERENCE_PROP)) {
                    image = new SniImageFactory().withPath(null).build();
                    return image;
                }

                String imgPath = imageNode.getProperty(IMAGE_FILE_REFERENCE_PROP).getString();
                image = new SniImageFactory().withPath(imgPath).build();
            } catch (RepositoryException re) {
                log.error("RepositoryException caught: ", re);
            }
        }
        return image;
    }

    @Override
    public int getDayIndex() {
        return dayIndex;
    }

    @Override
    public SniPage getAssetPage() {
        return this.assetPage;
    }

    @Override
    public String getAssetType() {
        return this.assetType;
    }

    @Override
    public String getLinkLabel() {
        return this.linkLabel;
    }

    @Override
    public String getLinkUrl() {
        return this.linkUrl;
    }

    @Override
    public String getLinkText() {
        return this.linkText;
    }

    @Override
    public String getIconType() {
        if (iconType == null){
            if(videoPages.contains(assetType)){
                iconType = VIDEO_ICON_CLASS;
            }else if(photoGalleryPages.contains(assetType)){
                iconType = PHOTO_ICON_CLASS;
            }else{
                iconType = "";
            }
        }
        return iconType;
    }
}