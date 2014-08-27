package com.scrippsnetworks.wcm.talent.impl;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.image.SniImageFactory;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.talent.Talent;

/**
 * @author Jonathan Bell
 *         Date: 7/25/2013
 */
public class TalentImpl implements Talent {
    private Resource resource;
    private SniPage sniPage;
    private SniImage talentImage;
    private String title;
    private ValueMap properties;
    private SniImage bannerImage;
    private ArrayList<SniImage> talentImages = new ArrayList<SniImage>();

    private static final String SNI_IMAGES = "sni:images";
    private static final String ASSET_RECIPES_TYPE = "asset-recipes";
    
	/** Asset Recipe page under this Talent, if any. */
	private SniPage recipeListingPage;

    public TalentImpl(Resource resource) {
        this.resource = resource;
        this.sniPage = PageFactory.getSniPage(resource.adaptTo(Page.class));
        this.properties = this.sniPage.getProperties();
    }

    public TalentImpl(SniPage page) {
        this.sniPage = page;
        this.resource = page.adaptTo(Resource.class);
        this.properties = page.getProperties();
    }

    /** {@inheritDoc} */
    public String[] getImagePaths() {
        if (properties != null && properties.containsKey(SNI_IMAGES)) {
            return properties.get(SNI_IMAGES, String[].class);
        }
        return null;
    }

    public SniImage getCanonicalImage() {
        if (talentImage == null) {
            if (properties.containsKey(PagePropertyConstants.PROP_SNI_AVATAR)) {   
                talentImage = new SniImageFactory()
                    .withPath(properties.get(PagePropertyConstants.PROP_SNI_AVATAR, String.class))
                    .build();
            }
        }
        
        return talentImage;
    }

    public SniPage getSniPage() {
        return sniPage;
    }

    public ValueMap getProperties() {
        return properties;
    }

    public String getTitle() {
        if (title == null) {
            title = this.sniPage.getTitle();
        }
        return title;
    }

    @Override
    public ArrayList<SniImage> getTalentImages() {
        if (talentImages == null) {
            if (properties != null && properties.containsKey(SNI_IMAGES)) {
                String[] imagePaths = properties.get(SNI_IMAGES, String[].class);
                if (imagePaths != null && imagePaths.length > 0) {
                    for (String imgPath:imagePaths) {
                        talentImages.add(new SniImageFactory()
                                .withPath(imgPath)
                                .build());
                    }
                }
            }
        }
        return talentImages;
    }

	@Override
	public SniImage getBannerImage() {
		if (bannerImage == null) {
			if (properties.containsKey(PagePropertyConstants.PROP_SNI_BANNER)) {
				bannerImage = new SniImageFactory()
				.withPath(properties.get(PagePropertyConstants.PROP_SNI_BANNER, String.class))
				.build();
			}
		}
		return bannerImage;
	}
	
	
	/** {@inheritDoc} */
	@Override
	public SniPage getRecipeListingPage() {
		if (recipeListingPage == null) {
			if (sniPage != null) {
				Iterator<Page> children = sniPage.listChildren();
				if (children != null) {
					while (children.hasNext()) {
						Page child = children.next();
						if (child != null) {
							SniPage childSniPage = PageFactory
									.getSniPage(child);
							String pageType = childSniPage.getPageType();
							if (StringUtils.isNotBlank(pageType)
									&& pageType.equals(ASSET_RECIPES_TYPE)) {
								recipeListingPage = childSniPage;
							}
						}
					}
				}
			}
		}
		return recipeListingPage;
	}	
}

