package com.scrippsnetworks.wcm.mobile.generic1image;


import com.scrippsnetworks.wcm.mobile.base.link.Link;
import com.scrippsnetworks.wcm.mobile.base.link.impl.LinkImpl;
import com.scrippsnetworks.wcm.mobile.generic1image.impl.GenericOneImageImpl;
import com.scrippsnetworks.wcm.mobile.imageicons.TransformIconClassToMobile;
import com.scrippsnetworks.wcm.util.CarouselMobileHelper;
import com.scrippsnetworks.wcm.util.HtmlUtil;
import com.scrippsnetworks.wcm.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.Arrays;
import java.util.List;

public class GenericOneImageFactory {
    private static final String IMAGE_URL_PROP = "image1_linkURL";
    private static final String TEXT_DESCR_PROP = "entryTextDescription";
    private static final String TEXT_HEADLINE_PROP = "entryTextHeadline";
    private static final String TEXT_SUBTITLE_PROP = "moduleSubTitle";
    private static final String MORELINK_TEXT_PROP = "moreLinksText";
    private static final String IMAGE_ASSET_TYPE_PROP = "image1_assetType";
    private static final String LINKS_PROP = "fourLinks";

    private static final String IMAGE_NODE = "leftImage";
    private static final String IMAGE_REF_PROP = "fileReference";
    private static final String COLON = ":";


    private Resource resource;

    public GenericOneImageFactory withResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public GenericOneImage build() {
        if (resource == null) {
            return null;
        }

        ValueMap vm = resource.adaptTo(ValueMap.class);

        GenericOneImage genericOneImage = new GenericOneImageImpl();
        GenericOneImageItem item = genericOneImage.getItem();

        if (vm != null) {
            //define base properties
            String linkUrl = vm.get(IMAGE_URL_PROP, "");
            String description = vm.get(TEXT_DESCR_PROP, "");
            String headline = vm.get(TEXT_HEADLINE_PROP, "");
            String morelinkText = vm.get(MORELINK_TEXT_PROP, "");
            String assetType = vm.get(IMAGE_ASSET_TYPE_PROP, "");
            String subtitle = vm.get(TEXT_SUBTITLE_PROP, "");

            //linkUrl = CarouselMobileHelper.getLink(linkUrl,resource.getResourceResolver(), headline, description);
            headline =   StringUtil.cleanToPlainText(headline);
            description = StringUtil.cleanToPlainText(description);

            if(StringUtils.isNotBlank(subtitle)){
                subtitle = subtitle.toUpperCase();
            }

            item.setUrl(linkUrl).setCaption(description).setEyebrow(subtitle).setTitle(headline)
                    .setCssClassName(TransformIconClassToMobile.modify(assetType));

            if(StringUtils.isNotEmpty(morelinkText) && !StringUtils.contains(morelinkText,":")){
                morelinkText=morelinkText+COLON;
            }

            genericOneImage.setLinksTitle(morelinkText);

            String[] strings = vm.get(LINKS_PROP, String[].class);

            if (strings != null) {
                List<String> links = Arrays.asList(strings);
                for(String link:links){
                    Link customLink = new LinkImpl();
                    String linkFromText = HtmlUtil.getHrefFromLink(link);
                    String clazz = CarouselMobileHelper.getClassFromText(link);
                    String text = StringUtil.cleanToPlainText(link);
                    String title = CarouselMobileHelper.getTitleLinkFromText(link);
                    String target = CarouselMobileHelper.getTargetLinkFromText(link);

                    customLink.setClazz(clazz).setLinksTitle(text).setHref(linkFromText).setTarget(target).setTitle(title);
                    genericOneImage.getLinks().add(customLink);
                }
            }

            Resource imageRes = resource.getChild(IMAGE_NODE);

            if (imageRes != null) {
                ValueMap imageMap = imageRes.adaptTo(ValueMap.class);
                if(imageMap!=null){
                    String ref = imageMap.get(IMAGE_REF_PROP, "");
                    item.setImageDamPath(ref);
                }
            }

            if (StringUtils.isEmpty(headline) && StringUtils.isEmpty(description) && StringUtils.isEmpty(item.getImageDamPath())) {
                genericOneImage.setItem(null);//disabled
            }
        }

        return genericOneImage;
    }

}
