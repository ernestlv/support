package com.scrippsnetworks.wcm.mobile.leadimagewithstack.impl;

import com.scrippsnetworks.wcm.mobile.leadimagewithstack.StackImageItem;
import org.apache.commons.lang.StringUtils;

public class StackImageItemImpl implements StackImageItem{
    private String title;
    private String link;
    private String imageDamPath;
    private String cssClassName;


    public StackImageItemImpl(String title, String link, String imageDamPath, String cssClassName) {
        this.title = title;
        this.link = link;
        this.imageDamPath = imageDamPath;
        this.cssClassName = cssClassName;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getLink() {
        return link;
    }

    @Override
    public String getImageDamPath() {
        return imageDamPath;
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
