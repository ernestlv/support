package com.scrippsnetworks.wcm.mobile.secondarybottom;


import com.scrippsnetworks.wcm.mobile.base.link.Link;
import com.scrippsnetworks.wcm.mobile.base.link.impl.LinkImpl;
import com.scrippsnetworks.wcm.mobile.imageicons.TransformIconClassToMobile;
import com.scrippsnetworks.wcm.mobile.secondarybottom.impl.SecondaryBottomItemImpl;
import com.scrippsnetworks.wcm.util.CarouselMobileHelper;
import com.scrippsnetworks.wcm.util.HtmlUtil;
import com.scrippsnetworks.wcm.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.ArrayList;
import java.util.List;

public class SecondaryBottomFactory {

    private static final String PROMO_IMAGE_URL_PROP = "imagelink";//1-2
    private static final String RIGHT_LINK_PROP = "rightlink";//1-3
    private static final String DESCRIPTION_PROP = "description";
    private static final String RIGHT_HEADER_PROP = "rightheader";
    private static final String PROMO_HEADER_PROP = "header";
    private static final String LINK_PROP = "link";//1-2
    private static final String ICONTYPE_PROP = "imageicontype";//1-2
    private static final String IMAGE_REF = "fileReference";
    private static final String IMAGE_NODE = "image";//1-2
    private static final String OPTIONS_PROP = "options";
    private static final String FIRST = "1";

    private Resource resource;

    public SecondaryBottomFactory withResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public List<SecondaryBottomItem> build() {
        if (resource == null) {
            return null;
        }

        ValueMap vm = resource.adaptTo(ValueMap.class);

        List<SecondaryBottomItem> items = new ArrayList<SecondaryBottomItem>();

        if (vm != null) {
            String option = vm.get(OPTIONS_PROP, FIRST);
            if (FIRST.equals(option)) {
                items.add(getItem(vm,1));
                items.add(getItem(vm,2));
            } else {
                items.add(getItem(vm,1));
            }
        }

        return items;
    }

    private SecondaryBottomItem getItem(ValueMap vm,int count) {
        SecondaryBottomItem secondaryBottomItem = new SecondaryBottomItemImpl();

        String linkUrl = vm.get(PROMO_IMAGE_URL_PROP + count, "");
        String assetType = vm.get(ICONTYPE_PROP + count, "");
        String description="";
        String header;
        String linkProp;

        if(count>1){
            header = vm.get(RIGHT_HEADER_PROP, "");
            linkProp = RIGHT_LINK_PROP;
        }else{
            header = vm.get(PROMO_HEADER_PROP, "");
            description = vm.get(DESCRIPTION_PROP, "");
            linkProp = LINK_PROP;
        }

        setImage(resource,count,secondaryBottomItem);

        if (StringUtils.isEmpty(header) && StringUtils.isEmpty(description) && !CarouselMobileHelper.isValidDamImagePath(resource.getResourceResolver(), secondaryBottomItem.getImageDamPath())) {
           return null;
        }

        for (int k = 1; k < count+2; k++) {
            String link = vm.get(linkProp + k, "");
            setLink(link,secondaryBottomItem,count+1);
        }

        header = StringUtil.cleanToPlainText(header);
        description = StringUtil.cleanToPlainText(description);
        secondaryBottomItem.setDescription(description).setTitle(header).setCssClassName(TransformIconClassToMobile.modify(assetType)).setUrl(linkUrl);

        return secondaryBottomItem;
    }

    private void setImage(Resource resource, int count, SecondaryBottomItem secondaryBottomItem) {
        Resource imageRes = resource.getChild(IMAGE_NODE + count);
        if (imageRes != null) {
            ValueMap imageMap = imageRes.adaptTo(ValueMap.class);
            if (imageMap != null) {
                String ref = imageMap.get(IMAGE_REF, "");
                secondaryBottomItem.setImageDamPath(ref);
            }
        }
    }

    private void setLink(String link, SecondaryBottomItem secondaryBottomItem, int size) {
        if (StringUtils.isBlank(link)) {
            return;
        }

        if (null == secondaryBottomItem.getLinks()) {
            secondaryBottomItem.setLinks(new ArrayList<Link>(size));
        }

        Link customLink = new LinkImpl();
        String linkFromText = HtmlUtil.getHrefFromLink(link);
        String clazz = CarouselMobileHelper.getClassFromText(link);
        String text = StringUtil.cleanToPlainText(link);
        String title = CarouselMobileHelper.getTitleLinkFromText(link);
        String target = CarouselMobileHelper.getTargetLinkFromText(link);

        customLink.setClazz(clazz).setLinksTitle(text).setHref(linkFromText).setTarget(target).setTitle(title);
        secondaryBottomItem.getLinks().add(customLink);
    }
}
