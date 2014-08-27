package com.scrippsnetworks.wcm.company;

import java.util.List;

import org.apache.sling.api.resource.ValueMap;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * @author veerinaiduj
 * 
 */
public interface Company {
    /** Merged company content page + asset properties */
    public ValueMap getProperties();

    /** Get the Company title */
    public String getTitle();

    /** Company description from sni:description. */
    public String getDescription();

    /** Image of company. */
    public String getImagePath();

    /** Email of company. */
    public String getEmail();

    /** Image Alt Text of Image for company. */
    public String getImageAltText();

    /** Image Caption of Image for company. */
    public String getImageCaption();

    /** Toll free of company. */
    public String getTollFreePhone();

    /** Phone number of company */
    public String getPhone();

    /** URL of company. */
    public String getUrl();

    /** Special or additional info  of  Company's most well-known dish. */
    public String getSpecialDish();

    /** Fax of Company */
    public String getFax();

    /** Longitude of Company for Google Map */
    public String getLongitude();

    /** Latitude of Company for Google Map */
    public String getLatitude();

    /** Address1 of the company. */
    public String getAddress1();
    
    /** Address2 of the company. */
    public String getAddress2();
    
    /** Address3 of the company. */
    public String getAddress3();
    
    /** City of the company. */
    public String getCity();
    
    /** State Full Name of the company. */
    public String getStateFullName();
    
    /** State Code of the company. */
    public String getState();
    
    /** Zip of the company. */
    public String getZip();
    
    /** Get the formatted address of company */
    public String getFormattedAddress();

    /** Find the talent pages associated with this company. */
    public List<SniPage> getTalentPages();

    /** Find the episode pages associated with this company. */
    public List<SniPage> getEpisodePages();

    
    /** Company Url. */
    public String getCompanyUrl();

}
