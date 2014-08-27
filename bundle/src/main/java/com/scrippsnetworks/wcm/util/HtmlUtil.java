package com.scrippsnetworks.wcm.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtil {
    public static String getHrefFromLink(String text){
        String res = "";
        Pattern p = Pattern.compile("<a .*?href=\"(.*?)\".*?>");
        Matcher m = p.matcher(text);
        if(m.find()) {
            res = m.group(1);
        }

        return res;
    }
}
