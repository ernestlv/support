package com.scrippsnetworks.wcm.mobile.secondarygrid;

import com.scrippsnetworks.wcm.mobile.secondarygrid.impl.SecondaryGridItemImpl;

public interface SecondaryGridItem {
    String getCssClassName();
    String getUrl();
    String getImageDamPath();
    String getAltText();
    String getTitle();
    String getDescription();

    SecondaryGridItemImpl setCssClassName(String cssClassName);
    SecondaryGridItemImpl setUrl(String url);
    SecondaryGridItemImpl setImageDamPath(String imageDamPath);
    SecondaryGridItemImpl setAltText(String altText);
    SecondaryGridItemImpl setTitle(String title);
    SecondaryGridItemImpl setDescription(String description);

}
