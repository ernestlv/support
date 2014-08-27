package com.scrippsnetworks.wcm.hub.count.impl;

import com.scrippsnetworks.wcm.hub.count.HubCount;
import com.scrippsnetworks.wcm.fnr.util.PageSlingResourceTypes;
import com.scrippsnetworks.wcm.menu.listing.MenuListingFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.photogallery.PhotoGallery;
import com.scrippsnetworks.wcm.photogallery.PhotoGalleryFactory;
import com.scrippsnetworks.wcm.photogallery.listing.PhotoGalleryListingFactory;
import com.scrippsnetworks.wcm.recipe.asset.AssetRecipeFactory;
import com.scrippsnetworks.wcm.recipe.listing.RecipeListingFactory;
import com.scrippsnetworks.wcm.util.PageTypes;
import com.scrippsnetworks.wcm.video.channel.Channel;
import com.scrippsnetworks.wcm.video.channel.ChannelFactory;
import com.scrippsnetworks.wcm.video.player.PlayerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Jason Clark
 *         Date: 5/10/13
 */
public class HubCountImpl implements HubCount {

    private Logger log = LoggerFactory.getLogger(HubCount.class);

    private Integer count;
    private SniPage sniPage;

    public HubCountImpl(final SniPage sniPage) {
        this.sniPage = sniPage;
    }

    /** where all the nasties live */
    private Integer pageSpecificCount() {
        if (sniPage == null) {
            return null;
        }
        PageTypes type = PageTypes.findPageType(sniPage.getPageType());
        if (type != null) {
            switch (type) {
                case VIDEO_CHANNEL:
                    return new ChannelFactory()
                            .withSniPage(sniPage)
                            .build()
                            .getVideos()
                            .size();
                case VIDEO_PLAYER:
                    List<Channel> videoChannels = new PlayerFactory()
                            .withSniPage(sniPage)
                            .build()
                            .getChannels();
                    Integer videoCount = 0;
                    for (Channel channel : videoChannels) {
                        videoCount += channel.getVideos().size();
                    }
                    return videoCount;
                case PHOTOGALLERY:
                    return new PhotoGalleryFactory()
                            .withSniPage(sniPage)
                            .build()
                            .getAllSlides()
                            .size();
                case PHOTOGALLERY_LISTING:
                    List<PhotoGallery> photoGalleries = new PhotoGalleryListingFactory()
                            .withSniPage(sniPage)
                            .build()
                            .getPhotoGalleries();
                    Integer photoCount = 0;
                    for (PhotoGallery gallery : photoGalleries) {
                        photoCount += gallery.getAllSlides().size();
                    }
                    return photoCount;
                case MENU_LISTING:
                    return new MenuListingFactory()
                            .withSniPage(sniPage)
                            .build()
                            .getMenus()
                            .size();
                case RECIPE_LISTING:
                    return new RecipeListingFactory()
                            .withSniPage(sniPage)
                            .build()
                            .getRecipes()
                            .size();
                case ASSET_RECIPES:
                    return new AssetRecipeFactory()
                            .withSniPage(sniPage)
                            .build()
                            .getRecipes()
                            .size();
                default:
                    break;
            }
        }
        return null;
    }

    /**
     * Count that will appear on Hub tabs, etc.
     * Can return null if no count logic specified for this page type
     * or sundry other reasons.
     * @return Integer or null
     */
    @Override
    public Integer getCount() {
        if (count == null ) {
            count = pageSpecificCount();
        }
        return count;
    }
}
