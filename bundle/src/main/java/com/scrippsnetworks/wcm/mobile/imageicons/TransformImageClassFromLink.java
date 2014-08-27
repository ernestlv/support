package com.scrippsnetworks.wcm.mobile.imageicons;

import java.util.HashMap;
import java.util.Map;

public class TransformImageClassFromLink {
    private static Map<String, String> cssClassMap = new HashMap<String, String>();
    static{
        cssClassMap.put("gallery-link", "link-to-gallery");
        cssClassMap.put("video-link", "link-to-video");
    }
    public static String modify(String cssClass){
        return cssClassMap.get(cssClass);
    }
}
