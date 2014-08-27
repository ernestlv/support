package com.scrippsnetworks.wcm.mobile.generic1image;

import com.scrippsnetworks.wcm.mobile.generic1image.impl.GenericOneImageItemImpl;

public interface GenericOneImageItem {
    String getTitle();

    GenericOneImageItemImpl setTitle(String title);

    String getImageDamPath();

    GenericOneImageItemImpl setImageDamPath(String imageDamPath);

    String getUrl();

    GenericOneImageItemImpl setUrl(String url);

    String getCssClassName();

    GenericOneImageItemImpl setCssClassName(String cssClassName);

    String getEyebrow();

    GenericOneImageItemImpl setEyebrow(String eyebrow);

    String getCaption();

    GenericOneImageItemImpl setCaption(String caption);
}
