package com.scrippsnetworks.wcm.breadcrumb;

import com.scrippsnetworks.wcm.breadcrumb.crumb.Crumb;

import java.util.List;

/**
 * Interface for Breadcrumb data management object. 
 *
 * @author Jonathan Bell
 *         Date: 10/3/2013
 */
public interface Breadcrumb {

    public List<Crumb> getCrumbs();
}
