/**
 * 
 */
package com.scrippsnetworks.wcm.company.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.company.Company;
import com.scrippsnetworks.wcm.map.util.StateHelper;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * @author veerinaiduj
 * 
 */
public class CompanyImpl implements Company {

    private static final String SNI_DESCRIPTION = "sni:description";
    private static final String SNI_EPISODES = "sni:episodes"; // migrated
    // values
    private static final String SNI_PEOPLE = "sni:people"; // editorial field
    // from page properties
    private static final String SNI_IMAGE = "sni:image";
    private static final String SNI_IMAGE_CAPTION = "sni:imageCaption";
    private static final String SNI_IMAGE_ALT_TEXT = "sni:imageAltText";

    /* All the address fields */
    private static final String SNI_ADDRESS1 = "sni:address1";
    private static final String SNI_ADDRESS2 = "sni:address2";
    private static final String SNI_ADDRESS3 = "sni:address3";
    private static final String SNI_CITY = "sni:city";
    private static final String SNI_STATE = "sni:state";
    private static final String SNI_ZIP = "sni:zip";
    private static final String SNI_PHONE = "sni:phone";
    private static final String SNI_TOLLFREE_PHONE = "sni:tollFreePhone";
    private static final String SNI_FAX = "sni:fax";
    private static final String SNI_EMAIL = "sni:email";
    private static final String SNI_URL = "sni:url";
    private static final String SNI_SPECIAL_DISH = "sni:specialDish";
    private static final String SNI_COMPANY_URL = "sni:companyUrl";

    /* values required for google map */
    private static final String SNI_LONGITUDE = "sni:longitude";
    private static final String SNI_LATITUDE = "sni:latitude";

    /* Utility constants */

    private static final String SPACE = " ";
    private static final String COMMA = ",";

    /** ValueMap of merged properties from SniPage. */
    private ValueMap properties;
    /** ResourceResolver for convenience. */
    private ResourceResolver resourceResolver;

    /** SniPage of company  used to create this object. */
    private SniPage sniPage;

    /** Member for list of talent pages related to this company. */
    private List<SniPage> talentPages;

    /** Member for list of episode pages related to this company. */
    private List<SniPage> episodePages;

     /** Title of this Company. */
    private String title;

    /** Description of this company. */
    private String description;

    /** Formatted Address of this company. */
    private String formattedAddress;
    
    /** Address1 of the company. */
    private String address1;
    
    /** Address2 of the company. */
    private String address2;
    
    /** Address3 of the company. */
    private String address3;
    
    /** city of the company. */
    private String city;
    
    /** state code of the company. */
    private String state;
    
    /** state full name of the Company.*/
    private String stateFullName;
    
    /** zip of the company. */
    private String zip;

    /** Image path of company */
    private String imagePath;

    /** Phone number of company */
    private String phone;

    /** TollFree of company */
    private String tollFreePhone;

    /** Fax of company */
    private String fax;

    /** Email of company */
    private String email;

    /** Website Url of company */
    private String url;

    /** Google map longitude of company */
    private String longitude;

    /** Google map latitude of company */
    private String latitude;

    /** Special or additional info  of  Company's most well-known dish. */
    private String specialDish;
    
    /** companyUrl. */
    private String companyUrl;

    public CompanyImpl(SniPage sniPage) {
        this.sniPage = sniPage;
        this.properties = sniPage.getProperties();
        Resource resource = sniPage.getContentResource();
        if (resource != null) {
            resourceResolver = resource.getResourceResolver();
        }

    }

    public ValueMap getProperties() {
        return properties;
    }

    @Override
    public String getImagePath() {
        if (imagePath == null) {
            if (properties != null && properties.containsKey(SNI_IMAGE)) {
                imagePath = properties.get(SNI_IMAGE, String.class);
            }
        }
        return imagePath;
    }

    /** Get the formatted address of company */
    public String getFormattedAddress() {
        StringBuilder _formattedAddress=new StringBuilder();
        if (formattedAddress == null) {
            if (getAddress1() != null) {
                _formattedAddress = _formattedAddress
                        .append("<span itemprop=\"streetAddress\">")
                        .append(getAddress1() != null ? getAddress1() : "")
                        .append(getAddress2() != null ? COMMA+SPACE + getAddress2() : "")
                        .append(getAddress3() != null ? COMMA+SPACE + getAddress3() : "")
                        .append("</span>");
            }
            if (getCity() != null) {
                if (_formattedAddress.length() > 0) {
                    _formattedAddress.append(COMMA).append(SPACE);
                }
                _formattedAddress
                        .append("<span itemprop=\"addressLocality\">")
                        .append(getCity())
                        .append("</span>");
            }
            if (getState() != null) {
                if (_formattedAddress.length() > 0) {
                    _formattedAddress.append(COMMA).append(SPACE);
                }
                _formattedAddress
                        .append("<span itemprop=\"addressRegion\">")
                        .append(getState())
                        .append("</span>");
            }
            if (getZip() != null) {
                _formattedAddress
                        .append(SPACE)
                        .append("<span itemprop=\"postalCode\">")
                        .append(getZip())
                        .append("</span>");
            }
            formattedAddress=_formattedAddress.toString();
        }

        return formattedAddress;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        if (description == null) {
            if (properties != null && properties.containsKey(SNI_DESCRIPTION)) {
                description = properties.get(SNI_DESCRIPTION, String.class);
            }
        }
        return description;
    }

    /** {@inheritDoc} Get the company title */
    @Override
    public String getTitle() {
        if (title == null) {
            title = sniPage.getTitle();
        }
        return title;
    }

    @Override
    public String getEmail() {
        if (email == null) {
            if (properties != null && properties.containsKey(SNI_EMAIL)) {
                email = properties.get(SNI_EMAIL, String.class);
            }
        }
        return email;
    }

    @Override
    public String getTollFreePhone() {

        if (tollFreePhone == null) {
            if (properties != null && properties.containsKey(SNI_TOLLFREE_PHONE)) {
                tollFreePhone = properties.get(SNI_TOLLFREE_PHONE, String.class);
            }
        }
        return tollFreePhone;
    }

    @Override
    public String getUrl() {
        if (url == null) {
            if (properties != null && properties.containsKey(SNI_URL)) {
                url = properties.get(SNI_URL, String.class);
            }
        }
        return url;
    }

    @Override
    public String getPhone() {
        if (phone == null) {
            if (properties != null && properties.containsKey(SNI_PHONE)) {
                phone = properties.get(SNI_PHONE, String.class);
            }
        }
        return phone;
    }

    @Override
    public String getFax() {
        if (fax == null) {
            if (properties != null && properties.containsKey(SNI_FAX)) {
                fax = properties.get(SNI_FAX, String.class);
            }
        }
        return fax;
    }

    @Override
    public String getImageAltText() {
        if (properties != null) {
            return (properties.containsKey(SNI_IMAGE_ALT_TEXT) ? properties
                    .get(SNI_IMAGE_ALT_TEXT, String.class) : "");
        }
        return "";
    }

    @Override
    public String getImageCaption() {
        if (properties != null) {
            return (properties.containsKey(SNI_IMAGE_CAPTION) ? properties.get(
                    SNI_IMAGE_CAPTION, String.class) : "");
        }
        return "";
    }

    @Override
    public String getSpecialDish() {
        if (specialDish == null) {
            if (properties != null && properties.containsKey(SNI_SPECIAL_DISH)) {
                specialDish = properties.get(SNI_SPECIAL_DISH, String.class);
            }
        }
        return specialDish;
    }

    @Override
    public String getLongitude() {
        if (longitude == null) {
            if (properties != null && properties.containsKey(SNI_LONGITUDE)) {
                longitude = properties.get(SNI_LONGITUDE, String.class);
            }
        }
        return longitude;
    }

    @Override
    public String getLatitude() {
        if (latitude == null) {
            if (properties != null && properties.containsKey(SNI_LATITUDE)) {
                latitude = properties.get(SNI_LATITUDE, String.class);
            }
        }
        return latitude;
    }

    @Override
    public List<SniPage> getTalentPages() {
        if (talentPages == null) { // null pointer check has to be in otherway
            if (properties != null && properties.containsKey(SNI_PEOPLE)) {
                String[] talentPaths = properties.get(SNI_PEOPLE,
                        String[].class);
                if (talentPaths != null) {
                    talentPages = new ArrayList<SniPage>();
                    for (String path : talentPaths) {
                        Resource talentResource = resourceResolver
                                .getResource(path);
                        if (talentResource != null) {
                            SniPage talentPage = PageFactory
                                    .getSniPage(talentResource
                                            .adaptTo(Page.class));
                            if (talentPage != null) {
                                talentPages.add(talentPage);
                            }
                        }
                    }
                }
            }
        }
        return talentPages;
    }

    @Override
    public List<SniPage> getEpisodePages() {
        if (episodePages == null) {
            if (properties != null && properties.containsKey(SNI_EPISODES)) {
                String[] episodePaths = properties.get(SNI_EPISODES,
                        String[].class);
                if (episodePaths != null) {
                    episodePages = new ArrayList<SniPage>();
                    for (String path : episodePaths) {
                        Resource episodeResource = resourceResolver
                                .getResource(path);
                        if (episodeResource != null) {
                            SniPage episodePage = PageFactory
                                    .getSniPage(episodeResource
                                            .adaptTo(Page.class));
                            if (episodePage != null) {
                                episodePages.add(episodePage);
                            }
                        }
                    }
                }
            }
        }
        return episodePages;
    }

  
    /** {@inheritDoc} */
    @Override
    public String getAddress1() {
        if(address1 == null) {
            if(properties != null && properties.containsKey(SNI_ADDRESS1)) {
                address1 = properties.get(SNI_ADDRESS1, String.class);
            }
        }
        return address1;
    }

    /** {@inheritDoc} */
    @Override
    public String getAddress2() {
        if(address2 == null) {
            if(properties != null && properties.containsKey(SNI_ADDRESS2)) {
                address2 = properties.get(SNI_ADDRESS2, String.class);
            }
        }
        return address2;
    }

    /** {@inheritDoc} */
    @Override
    public String getAddress3() {
        if(address3 == null) {
            if(properties != null && properties.containsKey(SNI_ADDRESS3)) {
                address3 = properties.get(SNI_ADDRESS3, String.class);
            }
        }
        return address3;
    }

    /** {@inheritDoc} */
    @Override
    public String getCity() {
        if(city == null) {
            if(properties != null && properties.containsKey(SNI_CITY)) {
                city = properties.get(SNI_CITY, String.class);
            }
        }
        return city;
    }

    /** {@inheritDoc} */
    @Override
    public String getState() {
        if(state == null) {
            if(properties != null && properties.containsKey(SNI_STATE)) {
                state = properties.get(SNI_STATE, String.class);
            }
        }
        return state;
    }
    
    /** {@inheritDoc} */
    @Override
	public String getStateFullName() {
		if (stateFullName == null) {
			if (state == null) {
				getState();
			}
			stateFullName = StateHelper.getStateByCode(state);
		}
		return stateFullName;
	}

    /** {@inheritDoc} */    
    @Override
    public String getZip() {
        if(zip == null) {
            if(properties != null && properties.containsKey(SNI_ZIP)) {
                zip = properties.get(SNI_ZIP, String.class);
            }
        }
        return zip;
    }

    /** {@inheritDoc} */
    @Override
    public String getCompanyUrl() {
        if(companyUrl == null) {
            if(properties != null && properties.containsKey(SNI_COMPANY_URL)) {
                companyUrl = properties.get(SNI_COMPANY_URL, String.class);
            }
        }
        return companyUrl;
    }
}
