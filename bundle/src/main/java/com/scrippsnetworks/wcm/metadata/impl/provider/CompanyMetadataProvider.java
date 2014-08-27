package com.scrippsnetworks.wcm.metadata.impl.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.scrippsnetworks.wcm.company.Company;
import com.scrippsnetworks.wcm.company.CompanyFactory;
import com.scrippsnetworks.wcm.metadata.MetadataProperty;
import com.scrippsnetworks.wcm.metadata.MetadataProvider;
import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.util.PageTypes;
import static com.scrippsnetworks.wcm.metadata.MetadataProperty.*;

public class CompanyMetadataProvider implements MetadataProvider {

    private Company company;
    /* Metadata expressed as CQ prooperty */
    String address1 = null;
    String address2 = null;
    String address3 = null;
    String city = null;
    String state = null;
    String zip = null;
    String tollFreePhone = null;
    String phone = null;
    String fax = null;
    String email = null;
    String website = null;
    String longitude = null;
    String latitude = null;
    String specialDish = null;

    public CompanyMetadataProvider(SniPage page) {
        if (page == null) {
            throw new IllegalArgumentException("page must not be null");
        }

        address1 = page.getProperties().get(
                PagePropertyConstants.PROP_COMPANY_ADDRESS1, String.class);
        address2 = page.getProperties().get(
                PagePropertyConstants.PROP_COMPANY_ADDRESS2, String.class);
        address3 = page.getProperties().get(
                PagePropertyConstants.PROP_COMPANY_ADDRESS3, String.class);
        city = page.getProperties().get(
                PagePropertyConstants.PROP_COMPANY_CITY, String.class);
        state = page.getProperties().get(
                PagePropertyConstants.PROP_COMPANY_STATE, String.class);
        zip = page.getProperties().get(
                PagePropertyConstants.PROP_COMPANY_ZIP, String.class);
        if (page.getPageType().equals(PageTypes.COMPANY.pageType())) {
            company = new CompanyFactory().withSniPage(page).build();
        }

        if (company != null) {

            tollFreePhone = company.getTollFreePhone();
            phone = company.getPhone();
            fax = company.getFax();
            email = company.getEmail();
            website = company.getUrl();
            longitude = company.getLongitude();
            latitude = company.getLatitude();
            specialDish = company.getSpecialDish();
        }
    }

    public List<MetadataProperty> provides() {
        return Arrays.asList(ADDRESS1, ADDRESS2, ADDRESS3, CITY, STATE, ZIP,
                PHONE, TOLLFREEPHONE, EMAIL, WEBSITE, FAX, LONGITUDE, LATITUDE,
                SPECIALDISH);
    }

    public String getProperty(MetadataProperty prop) {
        String retVal = null;

        switch (prop) {
        case ADDRESS1:
            retVal = address1;
            break;
        case ADDRESS2:
            retVal = address2;
            break;
        case ADDRESS3:
            retVal = address3;
            break;
        case CITY:
            retVal = city;
            break;
        case STATE:
            retVal = state;
            break;
        case ZIP:
            retVal = zip;
            break;
        case PHONE:
            retVal = phone;
            break;
        case TOLLFREEPHONE:
            retVal = tollFreePhone;
            break;
        case EMAIL:
            retVal = email;
            break;
        case WEBSITE:
            retVal = website;
            break;
        case FAX:
            retVal = fax;
            break;
        case LONGITUDE:
            retVal = longitude;
            break;
        case LATITUDE:
            retVal = latitude;
            break;
        case SPECIALDISH:
            retVal = specialDish;
            break;
        default:
            break;
        }

        return retVal;
    }

}