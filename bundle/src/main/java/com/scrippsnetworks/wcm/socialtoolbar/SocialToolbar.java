/**
 * 
 */
package com.scrippsnetworks.wcm.socialtoolbar;

/**
 * @author Mallik Vamaraju
 * 
 */
public interface SocialToolbar {
	
	/** get the twitter values associated for the page. */
	public String getTwitterText();
	
	/** get the pinterest values associated for the page. */
	public String getPintrestText();
	
	/** get the twitter Handler associated for the page */
	public String getTwitterHandle();
	
	
	/** get the site display name associated for the page */
	public String getSiteDisplayName();

	
	/** get the Open Graph Title associated for the page */
	public String getOpenGraphTitle();
	
	/** get the Open Graph Desc associated for the page */
	public String getOpenGraphDesc();
	
	/** get the Open Graph Image associated for the page */
	public String getOpenGraphImg();
	
	/** get the Open Graph type associated for the page */
	public String getOpenGraphType();
	
}
