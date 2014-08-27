package com.scrippsnetworks.wcm.canonicalimage.impl;

import com.day.cq.wcm.api.Page;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;

import com.scrippsnetworks.wcm.canonicalimage.CanonicalImage;
import com.scrippsnetworks.wcm.canonicalimage.CanonicalImageFactory;
import com.scrippsnetworks.wcm.config.SiteConfigService;
import com.scrippsnetworks.wcm.episode.Episode;
import com.scrippsnetworks.wcm.episode.EpisodeFactory;
import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.image.SniImageFactory;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.util.PagePropertyNames;
import com.scrippsnetworks.wcm.util.PageTypes;

import com.scrippsnetworks.wcm.article.Article;
import com.scrippsnetworks.wcm.article.ArticleFactory;
import com.scrippsnetworks.wcm.show.Show;
import com.scrippsnetworks.wcm.show.ShowFactory;
import com.scrippsnetworks.wcm.video.Video;
import com.scrippsnetworks.wcm.video.VideoFactory;
import com.scrippsnetworks.wcm.video.channel.Channel;
import com.scrippsnetworks.wcm.video.channel.ChannelFactory;
import com.scrippsnetworks.wcm.video.player.Player;
import com.scrippsnetworks.wcm.video.player.PlayerFactory;
import com.scrippsnetworks.wcm.menu.Menu;
import com.scrippsnetworks.wcm.menu.MenuFactory;
import com.scrippsnetworks.wcm.menu.listing.MenuListing;
import com.scrippsnetworks.wcm.menu.listing.MenuListingFactory;
import com.scrippsnetworks.wcm.recipe.Recipe;
import com.scrippsnetworks.wcm.recipe.RecipeFactory;
import com.scrippsnetworks.wcm.recipe.listing.RecipeListing;
import com.scrippsnetworks.wcm.recipe.listing.RecipeListingFactory;
import com.scrippsnetworks.wcm.photogallery.PhotoGallery;
import com.scrippsnetworks.wcm.photogallery.PhotoGalleryFactory;
import com.scrippsnetworks.wcm.photogallery.listing.PhotoGalleryListing;
import com.scrippsnetworks.wcm.photogallery.listing.PhotoGalleryListingFactory;
import com.scrippsnetworks.wcm.photogallery.PhotoGallerySlide;
import com.scrippsnetworks.wcm.photogallery.PhotoGallerySlideFactory;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CanonicalImageImpl implements CanonicalImage {

    private static final Logger LOG = LoggerFactory.getLogger(CanonicalImage.class);

    private Resource resource;
    private ResourceResolver resolver;
    private SniImage image;
    private SniPage page;
    private boolean defaultImageFlag;

    public CanonicalImageImpl() {}

    public CanonicalImageImpl(Resource resource, boolean defaultImageFlag) {
        this.resource = resource;
        this.page = PageFactory.getSniPage(resource.adaptTo(Page.class));
        this.defaultImageFlag = defaultImageFlag;
    }

    public CanonicalImageImpl(SniPage page, boolean defaultImageFlag) {
        this.page = page;
        this.resource = page.adaptTo(Resource.class);
        this.defaultImageFlag = defaultImageFlag;
    }

    public SniImage getImage() {
        if (page.getContentResource() != null) {
            resolver = page.getContentResource().getResourceResolver();
        }

        if (image == null) {
            String candidate = null;
            candidate = getPagePropertyImage();
            if (! isValidImage(candidate)) {
                candidate = getPageTypeImage();
                if (! isValidImage(candidate)) {
                    //Use the default image (ex: the site logo) if .withDefaultImage(true) was used when building this.
                    if(defaultImageFlag == true) {
                        candidate = getSiteDefaultImage();
                    }
                    if (! isValidImage(candidate)) {
                        candidate = null;
                    }
                }
            }

            if (candidate != null) {
                image = new SniImageFactory()
                    .withPath(candidate)
                    .build();
            }
        }

        return image;
    }

    private String getPagePropertyImage() {
        String imagePath = "";
        if (page != null) {
            imagePath = page.getProperties().get(PagePropertyNames.SNI_IMAGE.propertyName(), String.class);
        }

        return imagePath;
    }

    private String getPageTypeImage() {
        String imagePath = "";
        SniImage pageImage = null;

        if (page != null) {
            PageTypes type = PageTypes.findPageType(page.getPageType());
            if (type != null) {
                switch (type) {
                    case ARTICLE_SIMPLE:
                        Article article = new ArticleFactory().withSniPage(page).build();
                        if (article != null) {
                            pageImage = article.getFirstImage();
                        }
                        break;
                    case BIO:
                        SniPage talentPage = PageFactory.getSniPage(page.getParent());
                        imagePath = getReferencePageImage(talentPage);
                        break;
                    case MENU:
                        Menu menu = new MenuFactory().withSniPage(page).build();
                        if (menu != null) {
                            List<SniPage> menuRecipes = menu.getRecipePages();
                            if (menuRecipes.size() > 0) {
                                imagePath = getReferencePageImage(menuRecipes.get(0));
                            } 
                        } 
                        break;
                    case MENU_LISTING:
                        MenuListing menuListing = new MenuListingFactory().withSniPage(page).build();
                        if (menuListing != null) { 
                            List<Menu> menus = menuListing.getMenus();
                            if (menus != null && menus.size() > 0) {
                                Menu listingMenu = menus.get(0);
                                if (listingMenu != null) {
                                    SniPage menuPage = listingMenu.getSniPage();
                                    if (menuPage != null) {
                                        imagePath = getReferencePageImage(menuPage);
                                    }
                                }
                            }
                        }
                        break;
                    case PHOTOGALLERY:
                        HttpServletRequest request = page.getSlingRequest();
                        PhotoGallery gallery = null;
                        if (request != null) {
                            gallery = (PhotoGallery)request.getAttribute("gallery");
                        }
                        if (gallery == null) {
                            gallery = new PhotoGalleryFactory().withSniPage(page).build();
                        }

                        if (gallery != null) {
                            PhotoGallerySlide slide = gallery.getSlide(0);
                            if (slide != null) {
                                pageImage = slide.getSniImage();
                            }
                        }
                        break;
                    case PHOTOGALLERY_LISTING:
                        PhotoGalleryListing listing = new PhotoGalleryListingFactory().withSniPage(page).build();
                        if (listing != null) {
                            List<PhotoGallery> galleries = listing.getPhotoGalleries();
                            if (galleries != null && galleries.size() > 0) {
                                PhotoGallery listingGallery = galleries.get(0);
                                imagePath = getReferencePageImage(listingGallery.getPage());
                            }
                        }
                        break;
                        
                    case RECIPE:
                        Recipe recipe = new RecipeFactory().withSniPage(page).build();
                        if (recipe != null) {
                            imagePath = recipe.getImagePath();
                        } 
                        break;
                        
                    case EPISODE:
                        Episode episode = new EpisodeFactory().withSniPage(page).build();
                        if (episode != null) {
                            imagePath = episode.getImagePath();
                        } 
                        break;
                        
                    case RECIPE_LISTING:
                        RecipeListing recipeListing = new RecipeListingFactory().withSniPage(page).build();
                        if (recipeListing != null) { 
                            List<Recipe> recipes = recipeListing.getRecipes();
                            if (recipes != null && recipes.size() > 0) {
                                Recipe listingRecipe = recipes.get(0);
                                SniPage recipePage = listingRecipe.getRecipePage();
                                if (recipePage != null) {
                                    imagePath = getReferencePageImage(recipePage);
                                }
                            }
                        }
                        break;
                    case SHOW:
                        Show show = new ShowFactory().withSniPage(page).build();
                        if (show != null) {
                            pageImage = show.getFeatureBanner();   
                        }
                        break;
                    case TALENT:
                        imagePath = page.getProperties().get(PagePropertyNames.SNI_IMAGES.propertyName(), String.class);
                        break;
                    case VIDEO:
                        Video video = new VideoFactory().withSniPage(page).build();
                        if (video != null) {
                            imagePath = video.getPosterUrl();
                        }
                        break;
                    case VIDEO_CHANNEL:
                        Channel channel = new ChannelFactory().withSniPage(page).build();
                        if (channel != null) {
                            Video firstVideo = channel.getFirstVideo();
                            if (firstVideo != null) {
                                imagePath = firstVideo.getPosterUrl();
                            }
                        }
                        break;
                    case VIDEO_PLAYER:
                        Player player = new PlayerFactory().withSniPage(page).build();
                        if (player != null) {
                            Channel firstChannel = player.getFirstChannel();
                            if (firstChannel != null) {
                                Video firstChannelVideo = firstChannel.getFirstVideo();
                                if (firstChannelVideo != null) {
                                    imagePath = firstChannelVideo.getPosterUrl();
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }

            if (pageImage != null) {
                imagePath = pageImage.getPath();
            }
        }

        return imagePath;
    }

    private String getSiteDefaultImage() {
        String imagePath = "";
        if (page != null) {
            SiteConfigService sc = page.getSiteConfigService();
            if (sc != null) {
                imagePath = sc.getBrandLogoDamPath();
            }
        }

        return imagePath;
    }

    private String getReferencePageImage(SniPage targetPage) {
        String imagePath = "";

        CanonicalImage canon = new CanonicalImageFactory().withSniPage(targetPage).build();
        if (canon != null) {
            SniImage canImg = canon.getImage();
            if (canImg != null) {
                imagePath = canImg.getPath();
            }
        }

        return imagePath;
    }

    private boolean isValidImage(String testImagePath) {
        boolean isValid = false;

        if (resolver != null && ! StringUtils.isEmpty(testImagePath)) {
            if (! testImagePath.startsWith("/")) {
                isValid = true;
            } else {
                try {
                    Resource res = resolver.resolve(testImagePath);
                    if (! ResourceUtil.isNonExistingResource(res)) {
                        isValid = true;
                    }
                } catch (Exception e) {
                    LOG.error("Could not resolve image resource {}", testImagePath);
                    LOG.error(ArrayUtils.toString(e.getStackTrace()));
                }
            }
        }

        return isValid; 
    }

}

