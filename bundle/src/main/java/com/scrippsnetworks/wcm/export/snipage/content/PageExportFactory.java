package com.scrippsnetworks.wcm.export.snipage.content;

import com.scrippsnetworks.wcm.export.snipage.content.impl.ArticleExport;
import com.scrippsnetworks.wcm.export.snipage.content.impl.ChannelExport;
import com.scrippsnetworks.wcm.export.snipage.content.impl.CollectionExport;
import com.scrippsnetworks.wcm.export.snipage.content.impl.CompanyExport;
import com.scrippsnetworks.wcm.export.snipage.content.impl.EpisodeExport;
import com.scrippsnetworks.wcm.export.snipage.content.impl.FreeFormExport;
import com.scrippsnetworks.wcm.export.snipage.content.impl.MenuExport;
import com.scrippsnetworks.wcm.export.snipage.content.impl.MenuListingExport;
import com.scrippsnetworks.wcm.export.snipage.content.impl.PhotoGalleryExport;
import com.scrippsnetworks.wcm.export.snipage.content.impl.PhotoGalleryListingExport;
import com.scrippsnetworks.wcm.export.snipage.content.impl.RecipeExport;
import com.scrippsnetworks.wcm.export.snipage.content.impl.RecipeListingExport;
import com.scrippsnetworks.wcm.export.snipage.content.impl.ShowExport;
import com.scrippsnetworks.wcm.export.snipage.content.impl.SniPageExport;
import com.scrippsnetworks.wcm.export.snipage.content.impl.TalentExport;
import com.scrippsnetworks.wcm.export.snipage.content.impl.TopicExport;
import com.scrippsnetworks.wcm.export.snipage.content.impl.VideoExport;
import com.scrippsnetworks.wcm.export.snipage.content.impl.WCMFreeFormExport;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.util.PageTypes;

public class PageExportFactory {
    
    /** Produces the proper page export for site, page type, etc. */
    public static PageExport createPageExport(SniPage sniPage) {
        if (sniPage == null || !sniPage.hasContent()) {
            return null;
        }

        if (PageTypes.PHOTOGALLERY.pageType().equals(sniPage.getPageType())) {
            return new PhotoGalleryExport(sniPage);
        } else if (PageTypes.RECIPE.pageType().equals(sniPage.getPageType())) {
            return new RecipeExport(sniPage);
        } else if (PageTypes.EPISODE.pageType().equals(sniPage.getPageType())) {
            return new EpisodeExport(sniPage);
        } else if (PageTypes.COMPANY.pageType().equals(sniPage.getPageType())) {
            return new CompanyExport(sniPage);
        } else if(PageTypes.ARTICLE_SIMPLE.pageType().equals(sniPage.getPageType())) {
            return new ArticleExport(sniPage);
        } else if(PageTypes.VIDEO.pageType().equals(sniPage.getPageType())) {
            return new VideoExport(sniPage);
        } else if(PageTypes.TALENT.pageType().equals(sniPage.getPageType())) {
            return new TalentExport(sniPage);
        } else if(PageTypes.SHOW.pageType().equals(sniPage.getPageType())) {
            return new ShowExport(sniPage);
        } else if(PageTypes.VIDEO_CHANNEL.pageType().equals(sniPage.getPageType())) {
            return new ChannelExport(sniPage); 
        } else if(PageTypes.FREE_FORM_TEXT.pageType().equals(sniPage.getPageType())) {
        	return new FreeFormExport(sniPage);
        } else if(PageTypes.TOPIC.pageType().equals(sniPage.getPageType())) {
        	return new TopicExport(sniPage);
        } else if(PageTypes.MENU_LISTING.pageType().equals(sniPage.getPageType())) {
        	return new MenuListingExport(sniPage);
        } else if (PageTypes.COLLECTION.pageType().equals(sniPage.getPageType())) {
			return new CollectionExport(sniPage);
		} else if(PageTypes.RECIPE_LISTING.pageType().equals(sniPage.getPageType())) {
			return new RecipeListingExport(sniPage);
		} else if(PageTypes.PHOTOGALLERY_LISTING.pageType().equals(sniPage.getPageType())) {
			return new PhotoGalleryListingExport(sniPage);
		} else if(PageTypes.MENU.pageType().equals(sniPage.getPageType())) {
			return new MenuExport(sniPage);
		} else if(PageTypes.WCM_FREEFORM.pageType().equals(sniPage.getPageType())) {
			return new WCMFreeFormExport(sniPage);
		} else {
            return new SniPageExport(sniPage);
        }
    }
}
