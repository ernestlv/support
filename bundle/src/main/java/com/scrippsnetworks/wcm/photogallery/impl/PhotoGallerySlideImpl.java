package com.scrippsnetworks.wcm.photogallery.impl;

import com.day.cq.wcm.api.Page;

import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.image.SniImageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.photogallery.PhotoGallerySlide;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import org.jsoup.Jsoup;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jason Clark
 *         Date: 4/28/13
 */
public class PhotoGallerySlideImpl implements PhotoGallerySlide {

    private static final String IMAGE_NODE_NAME = "image/fileReference";
    private static final String CAPTION_PROPERTY_NAME = "description";
    private static final String TITLE_PROPERTY_NAME = "title";
    private static final String CREDIT_PROPERTY_NAME = "credit";
    private static final String ASSET_PROPERTY_NAME = "slide-link";
    private static final String FREELINK_ACTIVE_PROPERTY_NAME = "free-enabled";
    private static final String FREELINK_LABEL_PROPERTY_NAME = "free-label";
    private static final String FREELINK_TEXT_PROPERTY_NAME = "free-link";

    private static final String VIDEO_ICON_CLASS = "ss-video";
    private static final String PHOTO_ICON_CLASS = "ss-layers";

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
    
    /* fields */
    private SniImage sniImage;
    private String caption;
    private String title;
    private String credit;
    private String assetType;
    private SniPage assetPage;
    private String linkLabel;
    private String linkUrl;
    private String linkText;
    private boolean freeEnabled;
    private String freeLinkLabel;
    private String freeLinkText;
    private String iconType;
    private String freeUrl;

    /* internal junk */
    private String jcrPath;
    private String imageDamPath;
    private Resource resource;
    private ValueMap vm;

    /** Default Constructor */
    public PhotoGallerySlideImpl () {}

    /**
     * Impl
     * @param path
     */
    public PhotoGallerySlideImpl(final Resource resource) {
        this.vm = resource.adaptTo(ValueMap.class);
        
        this.resource = resource;
        this.jcrPath = resource.getPath();
        this.caption = Jsoup.parse(this.vm.get(CAPTION_PROPERTY_NAME, "")).body().html();
        this.title = this.vm.get(TITLE_PROPERTY_NAME, "");
        this.credit = this.vm.get(CREDIT_PROPERTY_NAME, "");
        this.freeEnabled = Boolean.parseBoolean(this.vm.get(FREELINK_ACTIVE_PROPERTY_NAME, "false"));
        this.freeLinkLabel = this.vm.get(FREELINK_LABEL_PROPERTY_NAME, "");
        this.freeLinkText = this.vm.get(FREELINK_TEXT_PROPERTY_NAME, "");
        this.imageDamPath = this.vm.get(IMAGE_NODE_NAME, "");
        this.freeUrl = "";
        Pattern p = Pattern.compile("<a .*? href=\"(.*?)\".*?>");
        Matcher m = p.matcher(freeLinkText);
        if(m.find()) {
            this.freeUrl = m.group(1);
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
                assetType = assetPage.getPageType();
                linkUrl = assetLink;
                linkText = assetPage.getTitle();
            }
        }
    }

    private void customizeCallToAction() {
        linkLabel = "";

        if (!freeEnabled) {
            if (lam.containsKey(assetType)) {
                linkLabel = lam.get(assetType).toString();
            }
        }
    }

    @Override
    public SniImage getSniImage() {
        if (sniImage == null && StringUtils.isNotBlank(jcrPath)) {
            sniImage = new SniImageFactory().withPath(imageDamPath).build();
        }
        return sniImage;
    }

    @Override
    public String getCaption() {
        return this.caption;
    }

    @Override
    public String getTitle() {
        return this.title;
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
    public String getJcrPath() {
        return this.jcrPath;
    }

    @Override
    public String getCredit() {
        return this.credit;
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
    public boolean getFreeEnabled() {
        return this.freeEnabled;
    }

    @Override
    public String getFreeLinkLabel() {
        return this.freeLinkLabel;
    }

    @Override
    public String getFreeLinkText() {
        return this.freeLinkText;
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

    public String getFreeUrl() {
        return freeUrl;
    }
}
