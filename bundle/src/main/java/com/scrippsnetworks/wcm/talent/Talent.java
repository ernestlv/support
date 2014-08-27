package com.scrippsnetworks.wcm.talent;

import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.page.SniPage;
import org.apache.sling.api.resource.ValueMap;

import java.util.ArrayList;

/**
 * @author Jonathan Bell
 *         Date: 7/25/2013
 */

public interface Talent {
    /** Path to the fullset image related to this Recipe. */
    public String[] getImagePaths();
    public ArrayList<SniImage> getTalentImages();
    public SniImage getCanonicalImage();
    public SniPage getSniPage();
    public String getTitle();
    public ValueMap getProperties();
    public SniImage getBannerImage();
	public SniPage getRecipeListingPage();    
}