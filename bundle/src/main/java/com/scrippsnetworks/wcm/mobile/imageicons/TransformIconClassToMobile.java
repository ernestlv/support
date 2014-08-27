package com.scrippsnetworks.wcm.mobile.imageicons;

import java.util.HashMap;
import java.util.Map;

public class TransformIconClassToMobile {
    private static Map<String, String> cssClassMap = new HashMap<String, String>();
    static{
        cssClassMap.put("ss-layers", "link-to-gallery");
        cssClassMap.put("ss-play", "link-to-video");
        cssClassMap.put("ss-video", "link-to-video");
        
    }
    public static String modify(String cssClass){
        return cssClassMap.get(cssClass);
    }
}
