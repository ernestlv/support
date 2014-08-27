package com.scrippsnetworks.wcm.content.model;

import java.io.Serializable;

public class ContentPageBean implements Serializable{
	
	/*
	 * path of content page where sponsorship and package code are associated
	 */
	private String pagePath=null;
	/*
	 * sponsorship code in system
	 */
	private String sponsorShipCode=null;
	/*
	 * path of package code in system
	 */
	private String packagePath=null;
	
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;
    
    /*
     * get content page path
     * @return pagePath
     */
	public String getPagePath(){
		return this.pagePath;
	}
	
	/*
	 * set content page path
	 * @param inPagePath
	 */
	public void setPagePath(String inPagePath){
		this.pagePath=inPagePath;
	}
	/*
     * get content sponsorship code 
     * @return pagePath
     */
	public String getSponsorShipCode(){
		return this.sponsorShipCode;
	}
	/*
	 * set sponsorship code
	 * @param inSponsorShipCode
	 */
	public void setSponsorShipCode(String inSponsorShipCode){
		this.sponsorShipCode=inSponsorShipCode;
	}
	/*
     * get package path
     * @return packagePath
     */
	public String getPackagePath(){
		return this.packagePath;
	}
	/*
	 * set package path
	 * @param inpackagePath
	 */
	public void setPackagePath(String inpackagePath){
		this.packagePath=inpackagePath;
	}
}
