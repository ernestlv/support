package com.scrippsnetworks.wcm.mobile.superleadcarousel;

import com.scrippsnetworks.wcm.mobile.burgernavigation.BurgerNavLink;
import com.scrippsnetworks.wcm.mobile.burgernavigation.impl.BurgerNavLinkImpl;
import com.scrippsnetworks.wcm.mobile.imageicons.TransformImageClassFromLink;
import com.scrippsnetworks.wcm.mobile.superleadcarousel.impl.SuperleadItemImpl;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.resource.JcrResourceConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SuperleadItemsFactory {
    private static Pattern classPattern = Pattern.compile("class=\".*?\"");

    private static final String THREEUP_TYPE = "sni-core/components/modules/carousel-superlead/threeup";
    private static final String ONEUP_TYPE = "sni-core/components/modules/carousel-superlead/oneup";

    private static final String ONE_UP_TITLE_PROP = "subheadertext";
    private static final String ONE_UP_DESCRIPTION_PROP = "description";
    private static final String ONE_UP_IMAGE_LINK_PROP = "imagelink1";
    private static final String ONE_UP_IMAGE_NODE = "image5";

    private static final String THREE_UP_TITLE_PROP = "subheader";
    private static final String THREE_UP_DESCRIPTION_PROP = "subheadertext";
    private static final String THREE_UP_IMAGE_LINK_PROP = "imagelink";
    private static final String THREE_UP_IMAGE_NODE = "image";

    private static final String DAM_IMAGE_PROP = "fileReference";


    private static final String IMAGE_COUNT_PROP = "list";
    private static final String SLIDE_NODE = "slide";

    private Resource resource;

    public List<SuperleadItem> build(){
        if(resource == null){
            return null;
        }

        ValueMap vm = resource.adaptTo(ValueMap.class);

        List<SuperleadItem> resList = new ArrayList<SuperleadItem>();

        Iterator<Resource> childrenIterator = resource.listChildren();
        for (int i = 1; i <= 3; i++){
            int count = vm.get(IMAGE_COUNT_PROP + i, 0);
            if (count > 0){
                Resource child = resource.getChild(SLIDE_NODE + i);

                if (child == null){
                    continue;
                }

                ValueMap values = child.adaptTo(ValueMap.class);
                if (values == null){
                    continue;
                }
                String type = values.get(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY,"");

                if (count == 2){
                    resList.add(buildSuperleadItemsFromOneup(child));
                } else if (count == 3){
                    resList.addAll(buildSuperleadItemsFromThreeup(child));
                }

            }
        }

        return resList;
    }

    private SuperleadItem buildSuperleadItemsFromOneup(Resource resource){
        ValueMap vm = resource.adaptTo(ValueMap.class);
        if (vm == null){
            return new SuperleadItemImpl("", "", "", "", "");
        }

        String title = vm.get(ONE_UP_TITLE_PROP, "");
        String description = vm.get(ONE_UP_DESCRIPTION_PROP, "");
        String imageLink = vm.get(ONE_UP_IMAGE_LINK_PROP, "");
        Resource imageRes = resource.getChild(ONE_UP_IMAGE_NODE);
        ValueMap imageVm = imageRes.adaptTo(ValueMap.class);
        String imageDamPath = "";
        if (imageVm != null){
            imageDamPath = imageVm.get(DAM_IMAGE_PROP, "");
        }

        String className = "";
        Matcher matcher = classPattern.matcher(description);
        if (matcher.find()){
            className = matcher.group();
            description = description.replace(className, "");
            className = className.substring(className.indexOf("\"") + 1, className.lastIndexOf("\""));
            className = TransformImageClassFromLink.modify(className);
        }

        return new SuperleadItemImpl(title, description, imageDamPath, imageLink, className);
    }

    private List<SuperleadItem> buildSuperleadItemsFromThreeup(Resource resource){
        List<SuperleadItem> resList = new ArrayList<SuperleadItem>();
        ValueMap vm = resource.adaptTo(ValueMap.class);
        if (vm == null){
            return resList;
        }
        for (int i = 1; i <= 3; i++){
            String title = vm.get(THREE_UP_TITLE_PROP + Integer.toString(i), "");
            String description = vm.get(THREE_UP_DESCRIPTION_PROP + Integer.toString(i), "");
            String imageLink = vm.get(THREE_UP_IMAGE_LINK_PROP + Integer.toString(i), "");
            Resource imageRes = resource.getChild(THREE_UP_IMAGE_NODE + Integer.toString(i));
            ValueMap imageVm = imageRes.adaptTo(ValueMap.class);

            String imageDamPath = "";
            if (imageVm != null){
                imageDamPath = imageVm.get(DAM_IMAGE_PROP, "");
            }

            String className = "";
            Matcher matcher = classPattern.matcher(description);
            if (matcher.find()){
                className = matcher.group();
                description = description.replace(className, "");
                className = className.substring(className.indexOf("\"") + 1, className.lastIndexOf("\""));
                className = TransformImageClassFromLink.modify(className);
            }

            resList.add(new SuperleadItemImpl(title, description, imageDamPath, imageLink, className));
        }

        return resList;
    }

    public SuperleadItemsFactory withResource(Resource resource){
        this.resource = resource;
        return this;
    }
}
