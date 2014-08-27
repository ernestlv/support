package com.scrippsnetworks.wcm.metadata.impl;

import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.page.SniPage;

import org.apache.sling.api.resource.Resource;
import org.apache.commons.lang.StringUtils;

import com.day.cq.wcm.api.Page;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.List;
import java.lang.String;


public class MetadataUtil {

    private static final String PUBLISH_DATE_FRMT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String MOBILE_SUFFIX = "-mobile";
    public static final String MOBILE_SELECTOR = "MOBILE";
    public static final String DELIVERY_CHANNEL_MOBILE = "Mobile";
    public static final String DELIVERY_CHANNEL_WEB = "Web";
    public static final String DEFAULT_SITE = "unknown";
    public static final String LAUNCHES_PATH = "/content/launches";
    public static final String CONTENT_ROOT = "/content";

    public static String getTitle(Page page) {
        if (page == null) {
            throw new IllegalArgumentException("page must not be null");
        }

        String retVal = page.getTitle();
        if (retVal == null || retVal.isEmpty()) {
            retVal = page.getName();
        }
        return retVal;
    }
    
    public static String getPublishTime(Page page) {
        String retVal = null;
        Calendar publishTime = page.getProperties().get("cq:lastReplicated", Calendar.class);

        if (publishTime != null) {
            SimpleDateFormat publishTimeDF = new SimpleDateFormat(PUBLISH_DATE_FRMT);
            retVal = publishTimeDF.format(publishTime.getTime());
        }
        return retVal;
    }

    public static String getSiteName(SniPage page) {
        if (page == null) {
            throw new IllegalArgumentException("page must not be null");
        }
        String retVal = null;
        if (page.getDepth() < 1) {
            return retVal;
        }
 
        String siteName = getSiteName(page.getPath());
        if (siteName != null && !siteName.isEmpty()) {
            retVal = siteName.replace(MOBILE_SUFFIX, "");
        } 

        return retVal;
    }
    
    public static String getHubPath(SniPage page) {
        String hubPath = "";
        Hub hub = page.getHub();
        if (hub != null) {
            hubPath = hub.getHubMaster().getPath();
        }
        return hubPath;
    }

    public static String getSourcePagePath(String path) {
        if (path.startsWith(MetadataUtil.LAUNCHES_PATH)) {
            path = path.substring(MetadataUtil.LAUNCHES_PATH.length());
            int indexOfContent = path.indexOf(MetadataUtil.CONTENT_ROOT);
            if (indexOfContent > 0) {
                path = path.substring(indexOfContent);
            }
        }
        return path;
    }

    public static String getSiteName(String path) {
        if (path == null) {
            return null;
        }
        String retVal = getSiteLevelNodeName(path);
        if (retVal != null) {
            retVal = retVal.replace(MOBILE_SUFFIX, "");
        }
        return retVal;
    }

    private static String getSiteLevelNodeName(String path) {
        if (path == null) {
            return null;
        }
        String retVal = null;
        
        // If this is a launch page, then the brand will be further down.
        //ex: /content/launches/rvg_pre_304_fridaypage/content/food/shows/r/rachael-vs-guy-celebrity-cook-off
        if (path.startsWith(LAUNCHES_PATH)) {
            path = path.substring(LAUNCHES_PATH.length());
            int indexOfContent = path.indexOf(CONTENT_ROOT);
            if (indexOfContent > 0) {
                path = path.substring(indexOfContent);
                String[] splitPath = path.split("/");
                if (splitPath != null && splitPath.length > 2) {
                    retVal = splitPath[2];
                }
            }
            return retVal;
        }

        String[] pathArr = path.split("/");
        if (pathArr.length > 2) {
            retVal = pathArr[2];
        }
        return retVal;
    }

	private static String getDeliveryChannelName(SniPage page) {
        if (page == null) {
            return null;
        }
        String retVal = null;
        if(page.getSelectors() != null) {
        	List<String> pageSelectors = page.getSelectors();
				for (String selector : pageSelectors) {
                	if ((selector.toUpperCase()).equals(MOBILE_SELECTOR)) {
                    	retVal = DELIVERY_CHANNEL_MOBILE;
                	}
        		}
        }
        return retVal;
    }
    
    public static String getDeliveryChannel(SniPage page) {
        if (page == null) {
            throw new IllegalArgumentException("page must not be null");
        }
        String retVal = DELIVERY_CHANNEL_WEB;
		String name = getDeliveryChannelName(page);
        if (name != null && name.equals(DELIVERY_CHANNEL_MOBILE)) {
        	retVal = DELIVERY_CHANNEL_MOBILE;
         }
        return retVal;
    }

    public static String getResourceType(Page page) {
        if (page == null) {
            throw new IllegalArgumentException("page must not be null");
        }
        Resource res = page.getContentResource();
        if (null != res) {
            String assetType = res.getResourceType();
            return getResourceType(assetType);
        }
        return null;
    } 

    public static String getResourceType(String assetType) {
        String resourceType = null;

        if (StringUtils.isNotBlank(assetType) && assetType.indexOf('/') >= 0) {
            resourceType = StringUtils.substringAfterLast(assetType, "/");
        }

        return resourceType;
    }

}

