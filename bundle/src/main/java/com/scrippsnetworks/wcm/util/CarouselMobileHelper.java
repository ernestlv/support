package com.scrippsnetworks.wcm.util;


import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CarouselMobileHelper {
    private static Logger logger = LoggerFactory.getLogger(CarouselMobileHelper.class);

    public static String getLink(String linkUrl,ResourceResolver resolver, String ... text) {
        if (!isLinkExist(linkUrl,resolver)) {
            String url ="";
            if(text!=null){
                for (int i = 0; i < text.length; i++) {
                    String tempLink = HtmlUtil.getHrefFromLink(text[i]);
                    if(!isLinkExist(url,resolver) && isLinkExist(tempLink,resolver)){
                        url = tempLink;
                    }
                }
            }

            return url;
        }
        return linkUrl;
    }

    private static boolean isLinkExist(String hrefLink, ResourceResolver resolver){

        if(StringUtils.isNotBlank(hrefLink)){
            String[] split = hrefLink.split("\\.");
            String path = split[0];
            Resource pageResource = resolver.getResource(path);

            if(pageResource!=null){
                return true;
            }
        }

        return false;
    }

   
    public static boolean isValidDamImagePath(ResourceResolver resourceResolver, String imageDamPath) {
        if (StringUtils.isNotBlank(imageDamPath)) {

            if(!imageDamPath.startsWith("/")){
               return true; //external imgs
            }

            Resource damRes = null;
            try {
                damRes = resourceResolver.getResource(imageDamPath);
            } catch (SlingException exception) {
                if(logger.isWarnEnabled()){
                    logger.warn("No image in dam:"+damRes,exception);
                }
            }
            if (damRes != null) {
                return true;
            }
        }
        return false;
    }

    private static String getAttributeFromText(String text,Pattern pattern) {
        if (StringUtils.isNotEmpty(text)) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String value = matcher.group();
                value = value.substring(value.indexOf("\"") + 1, value.lastIndexOf("\""));
                return value;
            }
        }
        return "";
    }

    public static String getClassFromText(String text) {
        Pattern classPattern = Pattern.compile("class=\".*?\"");
        return getAttributeFromText(text,classPattern);
    }

    public static String getTargetLinkFromText(String text) {
        Pattern targetPattern = Pattern.compile("target=\".*?\"");
        return getAttributeFromText(text,targetPattern);
    }

    public static String getTitleLinkFromText(String text) {
        Pattern titleLinkPattern = Pattern.compile("title=\".*?\"");
        return getAttributeFromText(text,titleLinkPattern);
    }
}
