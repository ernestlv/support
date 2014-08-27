package com.scrippsnetworks.wcm.map;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Anatoli_Zapolski
 * Date: 9/18/13
 * Time: 11:45 AM
 * To change this template use File | Settings | File Templates.
 */
public interface MapObj {
    public List<MapEntry> getMapEntries();
    public String getTitle();
    public String getDescription();
    public String getColorTheme();
}
