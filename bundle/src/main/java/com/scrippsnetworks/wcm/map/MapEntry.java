package com.scrippsnetworks.wcm.map;

import com.scrippsnetworks.wcm.image.SniImage;

/**
 * Created with IntelliJ IDEA.
 * User: Anatoli_Zapolski
 * Date: 9/17/13
 * Time: 2:22 PM
 * To change this template use File | Settings | File Templates.
 */
public interface MapEntry extends Comparable<MapEntry>{
    public String getLocationName();
    public String getZipCode();
    public String getCity();
    public String getState();
    public String getTitle();
    public String getDescription();
    public String getPhotographer();
    public String getStateCode();
    public Double getLatitude();
    public Double getLongitude();
    public SniImage getImage();
    public Boolean getHasImage();
    public Boolean getHasLatitudeAndLongitude();
}
