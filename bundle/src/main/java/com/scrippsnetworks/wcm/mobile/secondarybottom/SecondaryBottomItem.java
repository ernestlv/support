package com.scrippsnetworks.wcm.mobile.secondarybottom;

import com.scrippsnetworks.wcm.mobile.base.link.Link;
import com.scrippsnetworks.wcm.mobile.secondarybottom.impl.SecondaryBottomItemImpl;

import java.util.List;

public interface SecondaryBottomItem {
    String getTitle();

    SecondaryBottomItemImpl setTitle(String title);

    String getImageDamPath();

    SecondaryBottomItemImpl setImageDamPath(String imageDamPath);

    String getUrl();

    SecondaryBottomItemImpl setUrl(String url);

    String getCssClassName();

    SecondaryBottomItemImpl setCssClassName(String cssClassName);

    String getDescription();

    SecondaryBottomItemImpl setDescription(String description);

    List<Link> getLinks();

    SecondaryBottomItemImpl setLinks(List<Link> links);
}
