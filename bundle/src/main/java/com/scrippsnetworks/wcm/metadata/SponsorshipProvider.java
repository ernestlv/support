package com.scrippsnetworks.wcm.metadata;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import org.apache.commons.lang3.StringUtils;

/** Simple bean class to contain sponsorship information.
 *
 * Relates a sponsorship page with the providing page and a key specifying the relation.
 */
public class SponsorshipProvider {
    private SponsorshipSource source;
    private SniPage provider;
    private Page sponsorship;

    public SponsorshipProvider(SponsorshipSource source, Page provider, Page sponsorship) {
        this.source = source;
        this.provider = provider != null ? PageFactory.getSniPage(provider) : null;
        this.sponsorship = sponsorship;
    }

    public SponsorshipSource getSource() {
        return source;
    }

    public SniPage getProvider() {
        return provider;
    }

    public Page getSponsorship() {
        return sponsorship;
    }

    public String getSponsorshipValue() {
        String retVal = null;

        if (sponsorship != null) {
            retVal = sponsorship.getTitle();

            if(StringUtils.isEmpty(retVal)){
                retVal = sponsorship.getPath().substring(sponsorship.getPath().lastIndexOf("/") + 1).toUpperCase();
            }else{
                retVal = retVal.toUpperCase();
            }
        }

        return retVal;
    }
}
