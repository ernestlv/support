package com.scrippsnetworks.wcm.fnr.sitesearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class MapUtil {

    private static final Logger logger = LoggerFactory.getLogger(MapUtil.class);

    public static <T> T getValueFromMap(Map<String, Object> map, String path, Class<T> type) {
        if (map == null || path == null || path.trim().isEmpty()) {
            return null;
        }

        T retVal;
        String[] segments = path.split("/");
        try {
            if (segments.length == 1) {
                retVal = (T)map.get(segments[0]);
            } else {
                String key = segments[0];
                String relPath = path.substring(key.length() + 1);
                Map<String, Object> newMap = (Map<String, Object>)map.get(key);
                retVal = getValueFromMap(newMap, relPath, type);
            }
        } catch (Exception e) {
            logger.warn("error retrieving value from map", e);
            retVal = null;
        }

        return retVal;
    }

    public static Map<String, Object> getObjectFromMap(Map<String, Object> map, String path) {
        if (map == null || path == null || path.trim().isEmpty()) {
            return null;
        }

        String[] segments = path.split("/");
        try {
            if (segments.length == 1) {
                return (Map<String, Object>)map.get(segments[0]);
            } else {
                String key = segments[0];
                String relPath = path.substring(key.length() + 1);
                Map<String, Object> newMap = (Map<String, Object>)map.get(key);
                return getObjectFromMap(newMap, relPath);
            }
        } catch (Exception e) {
            logger.warn("error retrieving object from map", e);
            return null;
        }
    }

    public static List<Object> getListFromMap(Map<String, Object> map, String path) {
        if (map == null || path == null || path.trim().isEmpty()) {
            return null;
        }

        String[] segments = path.split("/");
        try {
            if (segments.length == 1) {
                logger.debug("returning list {} from map", segments[0]);
                return (List<Object>)map.get(segments[0]);
            } else {
                String key = segments[0];
                String relPath = path.substring(key.length() + 1);
                logger.debug("calling getListFromMap with relPath {}", relPath);
                return getListFromMap(map, relPath);
            }
        } catch (Exception e) {
            logger.warn("error retrieving list from map", e);
            return null;
        }
    }
}
