package com.scrippsnetworks.wcm.recipe.related;

import com.scrippsnetworks.wcm.page.SniPage;

/**
 * @author Jonathan Bell
 *         Date: 7/19/13
 */
public interface Related {

    /** Related pages associated to this Recipe. */
    public String getPromoImage();

    public String getOverlayType();
    
    public String getMobileOverlayType();

    public SniPage getSniPage();

    public Boolean getIsDamImage();
    
    public String getVideoRunTime();
    
}
