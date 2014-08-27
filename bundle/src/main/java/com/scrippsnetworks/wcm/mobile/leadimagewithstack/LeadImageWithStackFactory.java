package com.scrippsnetworks.wcm.mobile.leadimagewithstack;

import com.scrippsnetworks.wcm.mobile.imageicons.TransformIconClassToMobile;
import com.scrippsnetworks.wcm.mobile.imageicons.TransformImageClassFromLink;
import com.scrippsnetworks.wcm.mobile.leadimagewithstack.impl.LeadImageItemImpl;
import com.scrippsnetworks.wcm.mobile.leadimagewithstack.impl.StackImageItemImpl;
import com.scrippsnetworks.wcm.util.HtmlUtil;
import com.scrippsnetworks.wcm.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LeadImageWithStackFactory {

    private Resource resource;
    private static final int STACK_IMAGE_COUNT = 3;
    private static final String SLIDE_NODE = "slide";
    private static final String SLIDE_COUNT_PROP = "slideCount";
    private static final String STACK_IMAGE_NODE = "image-";
    private static final String TITLE_PROP = "title";
    private static final String IMAGE_LINK_PROP = "image-link";
    private static final String IMAGE_ICON_PROP = "image-icon";
    private static final String IMAGE_NODE = "image";
    private static final String DAM_IMAGE_PROP = "fileReference";
    private static final String TEXT_LINK_PROP = "text-link";
    private static Pattern classPattern = Pattern.compile("class=\".*?\"");

    private static final Logger log = LoggerFactory.getLogger(LeadImageWithStackFactory.class);

    public LeadImageWithStackFactory withResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public List<StackImageItem> buildStackImages() {
        if (resource == null) {
            return null;
        }

        List<StackImageItem> results = new ArrayList<StackImageItem>(STACK_IMAGE_COUNT);
        for (int i = 1; i <= STACK_IMAGE_COUNT; i++) {
            Resource child = resource.getChild(STACK_IMAGE_NODE + i);
            if (child == null) {
                continue;
            }

            ValueMap values = child.adaptTo(ValueMap.class);
            if (values == null) {
                continue;
            }

            StackImageItem item = buildStackImage(child);
            if (item.isValid()) {
                results.add(item);
            }

        }
        return results;
    }

    private StackImageItem buildStackImage(Resource resource) {
        if (resource == null) {
            return null;
        }
        ValueMap vm = resource.adaptTo(ValueMap.class);
        if (vm == null) {
            return new StackImageItemImpl("", "", "", "");
        }
        String title = vm.get(TEXT_LINK_PROP, "");
        String link = vm.get(IMAGE_LINK_PROP, "");
        String imageDamPath = getImageDamPath(resource);
        if (!isValidDamImagePath(resource.getResourceResolver(), imageDamPath)) {
            imageDamPath = "";
        }
        String iconClass = vm.get(IMAGE_ICON_PROP, "");
        if (StringUtils.isNotBlank(iconClass)) {
            iconClass = TransformIconClassToMobile.modify(iconClass);
        }
        if (StringUtils.isBlank(link)) {
            link = HtmlUtil.getHrefFromLink(title);
        }
        title = StringUtil.cleanToPlainText(title);
        return new StackImageItemImpl(title, link, imageDamPath, iconClass);
    }

    public int getCurrentLeadImagesSize(Resource resource) {
        if (resource == null) {
            return 0;
        }

        ValueMap vm = resource.adaptTo(ValueMap.class);
        int slideCount = vm.get(SLIDE_COUNT_PROP, 1);
        int currentCount = 0;
        if (slideCount > 0) {
            for (int i = 1; i <= slideCount; i++) {
                Resource child = resource.getChild(SLIDE_NODE + i);
                if (child == null) {
                    continue;
                }
                ValueMap values = child.adaptTo(ValueMap.class);
                if (values == null) {
                    continue;
                }
                String title = StringUtil.cleanToPlainText(values.get(TITLE_PROP, ""));
                if (StringUtils.isBlank(title)) {
                    String imageDamPath = getImageDamPath(child);
                    if (isValidDamImagePath(resource.getResourceResolver(), imageDamPath)) {
                        currentCount++;
                    }
                } else {
                    currentCount++;
                }
            }
        }
        return currentCount;
    }

    public LeadImageItem buildLeadImage(Resource resource) {
        if (resource == null) {
            return null;
        }
        ValueMap vm = resource.adaptTo(ValueMap.class);
        if (vm == null) {
            return new LeadImageItemImpl(0, "", "", "", "", "");
        }

        String title = vm.get(TITLE_PROP, "");
        String imageLink = vm.get(IMAGE_LINK_PROP, "");
        String imageDamPath = getImageDamPath(resource);
        if (!isValidDamImagePath(resource.getResourceResolver(), imageDamPath)) {
            imageDamPath = "";
        }
        String iconClass = getMatchedAttribute(classPattern, title);
        if (StringUtils.isNotBlank(iconClass)) {
            iconClass = TransformImageClassFromLink.modify(iconClass);
        }
        String linkTitle = HtmlUtil.getHrefFromLink(title);
        title = StringUtil.cleanToPlainText(title);
        String path = resource.getPath();
        int num;
        try {
            num = Integer.parseInt(path.substring(path.length() - 1));
        } catch (NumberFormatException nfe) {
            num = 1;
        }
        return new LeadImageItemImpl(num, title, linkTitle, imageDamPath, imageLink, iconClass);
    }

    private String getMatchedAttribute(Pattern valuePattern, String prop) {
        String value = "";
        Matcher matcher = valuePattern.matcher(prop);
        if (matcher.find()) {
            value = matcher.group();
            value = value.substring(value.indexOf("\"") + 1, value.lastIndexOf("\""));
        }
        return value;
    }

    private String getImageDamPath(Resource resource) {
        if (resource != null) {
            Resource imageRes = resource.getChild(IMAGE_NODE);
            ValueMap imageVm = imageRes.adaptTo(ValueMap.class);
            if (imageVm != null) {
                return imageVm.get(DAM_IMAGE_PROP, "");
            }
        }
        return "";
    }

    private boolean isValidDamImagePath(ResourceResolver resourceResolver, String imageDamPath) {
        if (StringUtils.isNotBlank(imageDamPath)) {
            Resource damRes = null;
            try {
                damRes = resourceResolver.getResource(imageDamPath);
            } catch (SlingException exception) {
                log.warn("ImageRes is not found", exception);
            }
            if (damRes != null) {
                return true;
            }
        }
        return false;
    }

}
