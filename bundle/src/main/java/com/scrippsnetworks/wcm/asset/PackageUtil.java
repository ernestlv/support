package com.scrippsnetworks.wcm.asset;

import java.lang.String;
import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageUtil {
	 // private Logger log = LoggerFactory.getLogger(PageMetadata.class);

    public static String getPackageName(Page page) {
        String retVal = null;
        String packagePath = page.getProperties().get("sni:package", String.class);
        if (packagePath != null) {
            Resource contentResource = page.getContentResource();
            if (contentResource != null) {
                Resource packageResource = contentResource.getResourceResolver().getResource(packagePath);
                if (packageResource != null) {
                    Page packagePage = packageResource.adaptTo(Page.class);
                    if (packagePage != null) {
                        // This is the title used by the export xml for package-landing currently.
                        retVal = packagePage.getTitle();
                    }
                }
            }
        }
        return retVal;
    }
}
