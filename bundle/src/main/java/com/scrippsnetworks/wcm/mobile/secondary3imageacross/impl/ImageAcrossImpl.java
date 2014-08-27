package com.scrippsnetworks.wcm.mobile.secondary3imageacross.impl;

import com.scrippsnetworks.wcm.mobile.imageicons.TransformIconClassToMobile;
import com.scrippsnetworks.wcm.mobile.secondary3imageacross.ImageAcross;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

public class ImageAcrossImpl implements ImageAcross{
    private static final String HEADER_PROP = "header-text";
    private static final String SUBHEADER_PROP = "subheader";
    private static final String IMAGE_LINK_PROP = "image-link";
    private static final String IMAGE_DAM_PATH_PROP = "image/fileReference";
    private static final String IMAGE_ICON_PROP = "image-icon";

    private String header;
    private String subheader;
    private String imageLink;
    private String imageDamPath;
    private String imageIcon;


    public ImageAcrossImpl(Resource resource){
        ValueMap vm = resource.adaptTo(ValueMap.class);
        header = vm.get(HEADER_PROP, "");
        subheader = vm.get(SUBHEADER_PROP, "");
        imageLink = vm.get(IMAGE_LINK_PROP, "");
        imageDamPath = vm.get(IMAGE_DAM_PATH_PROP, "");
        imageIcon = vm.get(IMAGE_ICON_PROP, "");

        imageIcon = TransformIconClassToMobile.modify(imageIcon);
        ResourceResolver rr = resource.getResourceResolver();
    }

    @Override
    public String getHeader() {
        return header;
    }

    @Override
    public String getSubheader() {
        return subheader;
    }

    @Override
    public String getImageLink() {
        return imageLink;
    }

    @Override
    public String getImageDamPath() {
        return imageDamPath;
    }

    @Override
    public String getImageIcon() {
        return imageIcon;
    }
}
