package com.scrippsnetworks.wcm.mobile.lead3image;

import com.scrippsnetworks.wcm.mobile.lead3image.impl.LeadThreeImageItemImpl;

public interface LeadThreeImageItem {
   String getTitle();

   LeadThreeImageItemImpl setTitle(String title);

   String getImageDamPath();

   LeadThreeImageItemImpl setImageDamPath(String imageDamPath);

   String getUrl();

   LeadThreeImageItemImpl setUrl(String url);

   String getCssClassName() ;

   LeadThreeImageItemImpl setCssClassName(String cssClassName);

   String getDescription() ;

   LeadThreeImageItemImpl setDescription(String description);

}
