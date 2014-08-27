package com.scrippsnetworks.wcm.page;

import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.breadcrumb.Breadcrumb;
import com.scrippsnetworks.wcm.config.SiteConfigService;
import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.metadata.MetadataManager;
import com.scrippsnetworks.wcm.metadata.SponsorshipManager;
import com.scrippsnetworks.wcm.opengraph.OpenGraph;
import com.scrippsnetworks.wcm.section.Section;
import com.scrippsnetworks.wcm.snipackage.SniPackage;
import com.scrippsnetworks.wcm.snitag.SniTag;

public interface SniPage extends Page {

    /** Retrieve the SniPage's Hub Object. */
    public Hub getHub();

    /** Count that appears in hub navigation buttons. */
    public Integer getHubCount();

    /** Retrieve PackageAnchor for SniPage */
    public SniPage getPackageAnchor();

    /** Retrieve canonical image URL for SniPage */
    public String getCanonicalImagePath();
    public String getCanonicalImageUrl();

    /** Retrieve canonical SniImage for SniPage */
    public SniImage getCanonicalImage();

    /** Retrieve SniPackage for SniPage */
    public SniPackage getSniPackage();

    /** Retrieve MetaDataManager Object for this SniPage */
    public MetadataManager getMetadataManager();

    /** Retrieves SponsorshipManager Object for this SniPage */
    public SponsorshipManager getSponsorshipManager();

    /** Retrieves OpenGraph Object */
    public OpenGraph getOpenGraph();

    /** Retrieves Breadcrumb Object */
    public Breadcrumb getBreadcrumb();

    /** Retrieve String SEO Title for SniPage */
    public String getSeoTitle();

    /** Retrieves String SEO Description for SniPage */
    public String getSeoDescription();

    /** Retrieves friendly URL for SniPage */
    public String getFriendlyUrl();

    /** Retrieves the page type descriptor for SniPage */
    public String getPageType();

    /** Retrieves the value of sni:assetUId property */
    public String getUid();

    /** Retrieve the brand associated to this SniPage */
    public String getBrand();

    /** Retrieve the SlingHttpServletRequest for this SniPage */
    public SlingHttpServletRequest getSlingRequest();

    /** Returns the deep linking page number from the Sling request, if any. */
    public Integer getDeepLinkPageNumber();
    
    /** Get the section for this SniPage */
    public Section getSection();
    
    /** Retrieve String Title for SniPage */
    public String getTitle();
    
    /** Retrieve String Description for SniPage */
    public String getDescription();

    /** Return a List of SniTags for the tags on this page. */
    public List<SniTag> getSniTags();

    /** Return a list of SniTags that link to active topic pages. */
    public List<SniTag> getCategoryTags();

    /** Return SniTag from sni:primaryTag property. */
    public SniTag getPrimarySniTag();

    /** Return SniTag from sni:secondaryTag property. */
    public SniTag getSecondarySniTag();

    /** Report whether current page is a package anchor */
    public Boolean isPackageAnchor();
    
    /** Retrieve the path to the large banner image */
    public String getLargeBannerImage();
    
    /** Get the social tag from page properties */
    public String getSocialTag();
    
    /** Get the tune in time from page properties */
    public String getTuneInTime();
    
    /** Retrieve String Short Title for SniPage */
    public String getShortTitle();
    
    /** Source for SniPage. */
    public String getSource();
    
    /** Get TagPageProperties. */
    public SniPage getSourcePage();
    
    /** Retrieves outward-facing URL for SniPage */
    public String getCanonicalUrl();

    /** Retrieves the pages URL */
	public String getUrl();

    /** Retrieve all page selectors */
    public List<String> getSelectors();

    /** Retrieve site config service */
    public SiteConfigService getSiteConfigService();
    
    /** Retrieve search property for SniPage */
    public boolean isDoNotSearch();

    /** This method determines the source path of the source page of launch pages      */
    public SniPage getLaunchSourceSniPage();

    public String getBannerLink();
}
