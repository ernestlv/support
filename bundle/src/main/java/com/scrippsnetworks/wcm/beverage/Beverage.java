/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scrippsnetworks.wcm.beverage;

import com.scrippsnetworks.wcm.page.SniPage;

/**
 *
 * @author Patrick
 */
public interface Beverage {
    
    /** If you need the content page the beverage came from. */
    public SniPage getBeveragePage();
    
    /** Get the Beverage title */
    public String getTitle();
    
    /** Get the Beverage description */
    public String getDescription();
    
    /** Path to the fullset image related to this Beverage. */
    public String getImagePath();
    
    /** Get the term link associated to this Beverage */
    public String getTermLink();
    
    /** Get the drink promotion associated to this Beverage */
    public String getDrinkPromotion();
    
    /** Get the promotion CTA Link associated to this Beverage */
    public String getCallToAction();
    
}
