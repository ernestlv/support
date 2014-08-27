package com.scrippsnetworks.wcm.calendar;

import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.page.SniPage;

import javax.jcr.Node;

/**
 * Created with IntelliJ IDEA.
 * User: Anatoli_Zapolski
 * Date: 9/9/13
 * Time: 12:59 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CalendarSlot {
    public Node getCurNode();

    public String getPath();

    public String getNodeName();

    public String getTitle();

    public String getDescription();

    public String getTitleLink();

    public String getSlideLink();

    public String getFreeLabel();

    public String getFreeLink();

    public SniImage getImage();

    public int getDayIndex();

    public SniPage getAssetPage();

    public String getAssetType();

    public String getLinkLabel();

    public String getLinkUrl();

    public String getLinkText();

    public boolean getFreeEnabled();

    public String getThumbnailTitle();

    public String getIconType();
}
