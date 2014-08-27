package com.scrippsnetworks.wcm.mobile.lead3image;


import com.scrippsnetworks.wcm.mobile.imageicons.TransformIconClassToMobile;
import com.scrippsnetworks.wcm.mobile.lead3image.impl.LeadThreeImageImpl;
import com.scrippsnetworks.wcm.mobile.lead3image.impl.LeadThreeImageItemImpl;
import com.scrippsnetworks.wcm.taglib.Functions;
import com.scrippsnetworks.wcm.util.CarouselMobileHelper;
import com.scrippsnetworks.wcm.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

public class LeadThreeImageFactory {
    private static String IMAGE_LINK_PROP = "image#_linkURL";
    private static String IMAGE_ASSET_TYPE_PROP = "image#_assetType";
    private static String DESCRIPTION_PROP = "entry#DescriptionOne";
    private static String HEADLINE_PROP = "entry#Headline";

    private static final String IMAGE_NODE = "image";
    private static final String IMAGE_REF = "fileReference";

    private Resource resource;


    public LeadThreeImageFactory withResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public LeadThreeImage build() {
        if (resource == null) {
            return null;
        }

        ValueMap vm = resource.adaptTo(ValueMap.class);

        LeadThreeImage leadThreeImage = new LeadThreeImageImpl();

        //define base properties

        if (vm != null) {

            for (int k = 1; k < 4; k++) {
                LeadThreeImageItem item = new LeadThreeImageItemImpl();

                String title = vm.get(numberToString(HEADLINE_PROP, k, true), "");
                String url = vm.get(numberToString(IMAGE_LINK_PROP, k, false), "");
                String assetType = vm.get(numberToString(IMAGE_ASSET_TYPE_PROP, k, false), "");
                String description = vm.get(numberToString(DESCRIPTION_PROP, k, true), "");

                //url = CarouselMobileHelper.getLink(url,resource.getResourceResolver(), title, description);
                title = StringUtil.cleanToPlainText(title);
                description =  StringUtil.cleanToPlainText(description);

                item.setTitle(title).setUrl(url).setCssClassName(TransformIconClassToMobile.modify(assetType)).setDescription(description);

                Resource imageRes = resource.getChild(IMAGE_NODE + k);

                if (imageRes != null) {
                    ValueMap imageMap = imageRes.adaptTo(ValueMap.class);
                    if (imageMap != null) {
                        String ref = imageMap.get(IMAGE_REF, "");
                        item.setImageDamPath(ref);
                    }
                }

                if (StringUtils.isEmpty(title) && StringUtils.isEmpty(description) && !CarouselMobileHelper.isValidDamImagePath(resource.getResourceResolver(),item.getImageDamPath())) {
                    item = null;//disabled
                }

                if (k == 1) {
                    leadThreeImage.setFirstItem(item);
                } else if (item != null) {
                    leadThreeImage.getItems().add(item);
                }
            }

        }

        return leadThreeImage;
    }

    private String numberToString(String constant, int index, boolean isWord) {
        String newString = "";
        if (index == 1) {
            if (isWord) {
                newString = constant.replace("#", "One");
            } else {
                newString = constant.replace("#", "1");
            }
        } else if (index == 2) {
            if (isWord) {
                newString = constant.replace("#", "Two");
            } else {
                newString = constant.replace("#", "2");
            }

        } else if (index == 3) {
            if (isWord) {
                newString = constant.replace("#", "Three");
            } else {
                newString = constant.replace("#", "3");
            }
        }
        return newString;
    }

}
