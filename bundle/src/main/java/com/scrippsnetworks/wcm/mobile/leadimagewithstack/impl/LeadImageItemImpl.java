package com.scrippsnetworks.wcm.mobile.leadimagewithstack.impl;

import com.scrippsnetworks.wcm.mobile.leadimagewithstack.LeadImageItem;
import com.scrippsnetworks.wcm.util.StringUtil;
import org.apache.commons.lang.StringUtils;

public class LeadImageItemImpl implements LeadImageItem {
    private int num;
    private String title;
    private String titleLink;
    private String imageDamPath;
    private String imageUrl;
    private String cssClassName;


    public LeadImageItemImpl(int num, String title, String titleLink, String imageDamPath, String imageUrl, String cssClassName) {
        this.num = num;
        this.title = title;
        this.titleLink = titleLink;
        this.imageDamPath = imageDamPath;
        this.imageUrl = imageUrl;
        this.cssClassName = cssClassName;
    }

    @Override
    public int getNum() {
        return num;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getTitleLink() {
        return titleLink;
    }

    @Override
    public String getImageDamPath() {
        return imageDamPath;
    }

    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public String getCssClassName() {
        return cssClassName;
    }

    @Override
    public boolean isValid() {
        return (StringUtils.isNotBlank(title) || StringUtils.isNotBlank(imageDamPath));
    }
}
