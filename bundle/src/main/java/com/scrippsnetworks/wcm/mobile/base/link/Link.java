package com.scrippsnetworks.wcm.mobile.base.link;

/**
 * Created by Dzmitry_Drepin on 2/6/14.
 */
public interface Link {

    String getLinksTitle();

    Link setLinksTitle(String linksTitle);

    String getClazz();

   Link setClazz(String clazz);

    String getHref();

    Link setHref(String href);

    String getTitle();

    Link setTitle(String title);

    String getTarget();

    Link setTarget(String target);
}
