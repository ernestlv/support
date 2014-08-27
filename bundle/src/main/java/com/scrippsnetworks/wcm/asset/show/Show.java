package com.scrippsnetworks.wcm.asset.show;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import com.day.cq.wcm.api.Page;
import com.day.cq.commons.jcr.JcrConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.commons.lang.StringUtils;

import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.talent.Talent;
import com.scrippsnetworks.wcm.util.AssetPropertyNames;

/**
 * Class representing the Show asset resource
 * 
 * @author mei-yichang, ranand 10/21/2013
 * 
 */
public class Show extends AbstractResourceObject {

	private final Logger log = LoggerFactory.getLogger(getClass());
	 
	public static final String PROPERTY_SNI_FEATUREBANNER = "sni:featureBannerImage";
	
	public static final String ASSET_TYPE = "SHOW";

	// root path of the show asset in the /etc/sni-assets
	public static final String ASSET_ROOT_SHOW = SNI_ASSET_PREFIX + "/shows";

	// root path of the shows in the /content tree
	public static final String CONTENT_ROOT_SHOW = CONTENT_ROOT + "/shows";

	private List<Series> series;
	
	private SniPage talentSniPage;
	
	private String featureBannerImage;

	/**
	 * Constructor
	 * 
	 * @param resource
	 * @param depts
	 *            - depths in the referencing hierarchy where descendent assets
	 *            will be loaded. Show -> Series -> Episode -> Recipe ie. to
	 *            load the show, including the episode information, depth will
	 *            be 2.
	 */
	public Show(Resource resource, int depts) {
		super(resource);
		// load series object only when necessary
		if (depts > 0) {
			loadSeries(depts);
		}
		loadPrimaryTalentAndFeaturedImage();
	}

	@SuppressWarnings("unchecked")
	private void loadSeries(int depts) {
		series = new ArrayList<Series>();
		for (Iterator<Resource> iter = resource.listChildren(); iter.hasNext();) {
			Resource next = iter.next();
			if (checkResourceAssetType(next, Series.ASSET_TYPE)) {
				Series temp = new Series(next, depts - 1);
				series.add(temp);
			}
		}
		Collections.sort(series);
	}

	/**
	 * This method loads the primary talent and featured image used on the porgam guide page
	 */
	@SuppressWarnings("unchecked")
	private void loadPrimaryTalentAndFeaturedImage() {
		Resource showAssetResource = resolver.resolve(resource.getPath() + "/" + JcrConstants.JCR_CONTENT);
		if(showAssetResource != null){
			ValueMap showAssetProperties = showAssetResource.adaptTo(ValueMap.class);
            if (showAssetProperties != null && showAssetProperties.containsKey(AssetPropertyNames.SNI_PAGE_LINKS.propertyName())) {
                String[] showPageLinks = showAssetProperties.get(AssetPropertyNames.SNI_PAGE_LINKS.propertyName(), String[].class);
                if (showPageLinks != null) {
                    String showPagePath = showPageLinks[0];
                    if (StringUtils.isNotBlank(showPagePath)) {
                        Resource showContentResource = resolver.getResource(showPagePath);
                        SniPage showSniPage = PageFactory.getSniPage(showContentResource.adaptTo(Page.class));
                        //Get the featureBannerImage to be used on the daily program guide page
                        featureBannerImage = showSniPage.getProperties().get(PROPERTY_SNI_FEATUREBANNER,"");
                        String sniPrimaryTalent = showSniPage.getProperties().get(AssetPropertyNames.SNI_PRIMARY_TALENT.propertyName(), "");
	                        if (StringUtils.isNotBlank(sniPrimaryTalent)) {
	                        	Resource talentAssetResource = resolver.resolve(sniPrimaryTalent  + "/" + JcrConstants.JCR_CONTENT);
	                        	if(showAssetResource != null){
	                        		ValueMap talentAssetProperties = talentAssetResource.adaptTo(ValueMap.class);
	                                if (talentAssetProperties != null && talentAssetProperties.containsKey(AssetPropertyNames.SNI_PAGE_LINKS.propertyName())) {
	                                    String[] talentPageLinks = talentAssetProperties.get(AssetPropertyNames.SNI_PAGE_LINKS.propertyName(), String[].class);
	                                    String talentPagePath = talentPageLinks[0];
	                                    if (StringUtils.isNotBlank(talentPagePath)) {
	                                        Resource talentContentResource = resolver.getResource(talentPagePath);
	                                        talentSniPage = PageFactory.getSniPage(talentContentResource.adaptTo(Page.class));
	                                    }
	                                }
	                        	}
	                        }
                    }
                }
            }
		}
	}	
	
	public List<Series> getSeries() {
		return series;
	}
	
	public SniPage getPrimaryTalent() {
		return talentSniPage;
	}

	public String getFeatureBannerImage() {
		return featureBannerImage;
	}	
	
}