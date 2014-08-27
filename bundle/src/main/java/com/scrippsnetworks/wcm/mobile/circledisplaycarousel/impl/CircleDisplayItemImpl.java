package com.scrippsnetworks.wcm.mobile.circledisplaycarousel.impl;

import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.mobile.circledisplaycarousel.CircleDisplayItem;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.Arrays;
import java.util.regex.Pattern;

public class CircleDisplayItemImpl implements CircleDisplayItem {
    private static final String ASSET_PAGE_PROP = "selectedPage";
    private static final String FIRST_NAME_PROP = "title";
    private static final String LAST_NAME_PROP = "title2";
    private static final String LINK_NAME_PROP = "link";
    private static final String DAM_IMAGE_PATH_PROP = "image/fileReference";
    private static final String SNI_IMAGES_PROP = "sni:images";
    private static final String SNI_PAGE_LINK_PROP = "sni:pageLinks";

    public String damImagePath;
    public String link;
    public String firstName;
    public String lastName;

    public CircleDisplayItemImpl(PageManager pageManager, Resource resource){
        ValueMap vm = null;
        SniPage assetPage = null;

        if (resource != null) {
            vm = resource.adaptTo(ValueMap.class);
        }

        if (vm == null){
            return;
        }

        String assetPagePath = vm.get(ASSET_PAGE_PROP, "");

        if (!StringUtils.isEmpty(assetPagePath)){
            assetPage = PageFactory.getSniPage(pageManager, assetPagePath);
        }

        damImagePath = vm.get(DAM_IMAGE_PATH_PROP, "");
        link = vm.get(LINK_NAME_PROP, "");
        firstName = vm.get(FIRST_NAME_PROP, "");
        lastName = vm.get(LAST_NAME_PROP, "");

        if (assetPage == null){
            return;
        }

        ValueMap assetVm = assetPage.getProperties();
        if (assetVm == null){
            return;
        }

        if (StringUtils.isEmpty(damImagePath)){
            damImagePath = assetVm.get(SNI_IMAGES_PROP, "");
        }

        if (StringUtils.isEmpty(link)){
            link = assetVm.get(SNI_PAGE_LINK_PROP, "");
        }

        if (StringUtils.isEmpty(firstName) && StringUtils.isEmpty(lastName)){
            Pattern pattern = Pattern.compile(" ");
            String title = assetPage.getTitle();
            if (title == null){
                title = "";
            }
            firstName = pattern.split(title)[0];
            lastName = title.substring(firstName.length(), title.length());
        }
    }

    @Override
    public String getDamImagePath() {
        return damImagePath;
    }

    @Override
    public String getLink() {
        return link;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }
}
