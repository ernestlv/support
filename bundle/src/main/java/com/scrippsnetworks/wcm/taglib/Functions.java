package com.scrippsnetworks.wcm.taglib;

import com.adobe.granite.xss.XSSAPI;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Template;
import com.google.common.base.Joiner;
import com.scrippsnetworks.wcm.article.Article;
import com.scrippsnetworks.wcm.article.ArticleFactory;
import com.scrippsnetworks.wcm.asset.DataUtil;
import com.scrippsnetworks.wcm.asset.article.simple.ArticleSimpleTextImageComponent;
import com.scrippsnetworks.wcm.asset.article.simple.ArticleSimpleTitleComponent;
import com.scrippsnetworks.wcm.asset.article.stepbystep.ArticleStepByStepTextImageComponent;
import com.scrippsnetworks.wcm.asset.episode.EpisodeDataUtil;
import com.scrippsnetworks.wcm.asset.photogallery.TitleDescriptionComponent;
import com.scrippsnetworks.wcm.beverage.Beverage;
import com.scrippsnetworks.wcm.beverage.BeverageFactory;
import com.scrippsnetworks.wcm.calendar.CalendarFactory;
import com.scrippsnetworks.wcm.calendar.CalendarSlot;
import com.scrippsnetworks.wcm.calendar.CalendarSlotFactory;
import com.scrippsnetworks.wcm.carousel.CarouselSlideFactory;
import com.scrippsnetworks.wcm.carousel.SuperleadCarouselSlide;
import com.scrippsnetworks.wcm.company.Company;
import com.scrippsnetworks.wcm.company.CompanyFactory;
import com.scrippsnetworks.wcm.episode.Episode;
import com.scrippsnetworks.wcm.episode.EpisodeFactory;
import com.scrippsnetworks.wcm.episodelisting.EpisodeListing;
import com.scrippsnetworks.wcm.episodelisting.EpisodeListingFactory;
import com.scrippsnetworks.wcm.episodelisting.EpisodeListingSelectors;
import com.scrippsnetworks.wcm.episodelisting.EpisodeListingSelectorsFactory;
import com.scrippsnetworks.wcm.fnr.util.AssetRootPaths;
import com.scrippsnetworks.wcm.fnr.util.AssetSlingResourceTypes;
import com.scrippsnetworks.wcm.fnr.util.PageSlingResourceTypes;
import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.map.MapEntry;
import com.scrippsnetworks.wcm.map.MapEntryFactory;
import com.scrippsnetworks.wcm.map.MapObj;
import com.scrippsnetworks.wcm.map.MapObjFactory;
import com.scrippsnetworks.wcm.menu.Menu;
import com.scrippsnetworks.wcm.menu.MenuFactory;
import com.scrippsnetworks.wcm.metadata.MetadataManager;
import com.scrippsnetworks.wcm.metadata.MetadataProperty;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.parsys.Paginator;
import com.scrippsnetworks.wcm.photogallery.PhotoGallery;
import com.scrippsnetworks.wcm.photogallery.PhotoGalleryFactory;
import com.scrippsnetworks.wcm.photogallery.PhotoGallerySlide;
import com.scrippsnetworks.wcm.photogallery.PhotoGallerySlideFactory;
import com.scrippsnetworks.wcm.recipe.Recipe;
import com.scrippsnetworks.wcm.recipe.RecipeFactory;
import com.scrippsnetworks.wcm.recipe.asset.AssetRecipeSelectors;
import com.scrippsnetworks.wcm.recipe.asset.AssetRecipeSelectorsFactory;
import com.scrippsnetworks.wcm.relationship.RelationshipModelFactory;
import com.scrippsnetworks.wcm.search.SearchObjectMapper;
import com.scrippsnetworks.wcm.search.SearchRequestHandler;
import com.scrippsnetworks.wcm.search.SearchResponse;
import com.scrippsnetworks.wcm.search.SearchService;
import com.scrippsnetworks.wcm.show.Show;
import com.scrippsnetworks.wcm.show.ShowFactory;
import com.scrippsnetworks.wcm.snitag.SniTag;
import com.scrippsnetworks.wcm.snitag.SniTagFactory;
import com.scrippsnetworks.wcm.socialtoolbar.SocialToolbar;
import com.scrippsnetworks.wcm.socialtoolbar.SocialToolbarFactory;
import com.scrippsnetworks.wcm.taboola.TaboolaProperties;
import com.scrippsnetworks.wcm.talent.Talent;
import com.scrippsnetworks.wcm.talent.TalentFactory;
import com.scrippsnetworks.wcm.topic.Topic;
import com.scrippsnetworks.wcm.topic.TopicFactory;
import com.scrippsnetworks.wcm.util.Constant;
import com.scrippsnetworks.wcm.util.ContentRootPaths;
import com.scrippsnetworks.wcm.util.PageTypes;
import com.scrippsnetworks.wcm.video.Video;
import com.scrippsnetworks.wcm.video.VideoFactory;
import com.scrippsnetworks.wcm.video.channel.Channel;
import com.scrippsnetworks.wcm.video.channel.ChannelFactory;
import com.scrippsnetworks.wcm.video.player.Player;
import com.scrippsnetworks.wcm.video.player.PlayerFactory;
import com.scrippsnetworks.wcm.video.player.PlayerSelectors;
import com.scrippsnetworks.wcm.video.player.PlayerSelectorsFactory;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Functions {

	private static final Logger log = LoggerFactory.getLogger(Functions.class);

	private Functions() {
	}

	public static Resource getResourceChild(Resource resource, String childName) {
		if (resource != null && childName != null) {
			Resource retVal = resource.getChild(childName);
			return retVal;
		} else {
			return null;
		}
	}

	/**
	 * this is useful for retrieving a module node from a known structure (such
	 * as a template). could be dangerous to retrieve anonymous nodes under
	 * other circumstances.
	 *
	 * @param resource
	 *            Sling Resource from which you wish to retrieve a single
	 *            anonymous child node
	 * @return Resource from child of passed-in resource
	 */
	public static Resource getResourceChild(final Resource resource) {
		Resource retVal = null;
		if (resource != null) {
			Iterator childItr = getResourceChildIterator(resource);
			if (childItr.hasNext()) {
				retVal = (Resource) childItr.next();
			}
		}
		return retVal;
	}

	public static Iterator<Resource> getResourceChildIterator(Resource resource) {
		if (resource != null) {
			return resource.listChildren();
		} else {
			return Collections.<Resource> emptyList().iterator();
		}
	}

	/** Method to get the children count of a resource. */
	public static int getResourceChildCount(Resource resource) {
		Iterator<Resource> iterator = getResourceChildIterator(resource);
		int i = 0;
		while(iterator.hasNext()) {
			i++;
			iterator.next();
		}
		return i;
	}

    public static Resource getResourceSibling(final Resource resource, String nodeName) {
		Resource retVal = null;
        if (resource != null && nodeName != null) {
            retVal = getResourceChild(resource.getParent(), nodeName);
        }
        return retVal;
    }

	public static ValueMap getResourceProperties(Resource resource) {
		if (resource != null) {
			return ResourceUtil.getValueMap(resource);
		} else {
			return ValueMap.EMPTY;
		}
	}

	public static String getResourceProperty(Resource resource, String propName) {
		if (resource != null) {
			String retVal = getResourceProperty(resource, propName, String.class);
			if (retVal != null) {
				return retVal;
			} else {
				return "";
			}
		} else {
			return "";
		}
	}


	public static String[] getResourcePropertyAsArray(Resource resource, String propName) {
			String []typed=new String[0];
			ValueMap vm = getResourceProperties(resource);
			if(vm!=null)
				return vm.get(propName,typed);
			else
				return null;
	}

        public static SuperleadCarouselSlide getSuperleadCarouselSlide(Resource slideRes) {
            return CarouselSlideFactory.getSuperleadSlide(slideRes);
        }

        public static List<SuperleadCarouselSlide> getSuperleadCarouselSlides(Iterator<Resource> slides) {
            List<SuperleadCarouselSlide> carouselSlides = new ArrayList<SuperleadCarouselSlide>();
            while (slides.hasNext()) {
                carouselSlides.add(CarouselSlideFactory.getSuperleadSlide(slides.next()));
            }
            return carouselSlides;
        }

	// There's going to be weirdness here, especially given whatever inferences
	// and reflection EL is doing.
	// @SuppressWarnings("unchecked")
	public static <T> T getResourceProperty(Resource resource, String propName,
			Class<T> type) {

		// Not sure if String is right to return here.
		if (resource == null || type == null) {
			return null;
		}

		ValueMap vm = getResourceProperties(resource);
		if (vm != null) {
			return vm.get(propName, type);
		} else {
			return null;
		}
	}

    /**
     *
     * @param resource
     * @return
     */
    public static Map<String, Object> getPropertyMap(final Resource resource) {
        if (resource == null) {
            return null;
        }
        ValueMap values = ResourceUtil.getValueMap(resource);
        Map<String, Object> output = new HashMap<String, Object>();
        for (Map.Entry<String,Object> entry : values.entrySet()) {
            output.put(entry.getKey(), entry.getValue());
        }
        return output;
    }

    /**
     * Removes all markup from input, returns stripped down string
     * @param input String dirty html ridden text
     * @return String plain text without markup
     */
	public static String removeMarkup(final String input) {
		if (input != null && input.length() > 0) {
			return Jsoup.parse(input).text();
		} else {
			return "";
		}
	}

    /**
     * Removes markup in text, keeping anchors
     * @param input String dirty html ridden text
     * @return String plain text with any anchor tags intact
     */
    public static String removeMarkupExceptAnchors(final String input) {
        if (StringUtils.isEmpty(input)) {
            return "";
        }
        Whitelist whitelist = Whitelist.none().addTags("a").addAttributes("a","href", "class", "debug");
        Cleaner cleaner = new Cleaner(whitelist);
        return cleaner.clean(Jsoup.parse(input)).body().html();
    }

    /**
     * Removes markup in anchor content, except for certain inline elements
     * @param input String dirty html ridden text
     * @return String plain text with any non-inline elements removed
     */
    public static String removeMarkupExceptSimpleText(final String input) {
        if (StringUtils.isEmpty(input)) {
            return "";
        }
        Whitelist whitelist = Whitelist.simpleText();

        Cleaner cleaner = new Cleaner(whitelist);
        return cleaner.clean(Jsoup.parse(input)).body().html();
    }

    /**
     * Removes markup in anchor content, except for certain inline elements and anchors
     * @param input String dirty html ridden text
     * @return String plain text with any non-inline or anchor elements removed
     */
    public static String removeMarkupExceptSimpleTextAndAnchors(final String input) {
        if (StringUtils.isEmpty(input)) {
            return "";
        }
        Whitelist whitelist = Whitelist.simpleText().addTags("a").addAttributes("a","href", "class", "debug", "target");

        Cleaner cleaner = new Cleaner(whitelist);
        return cleaner.clean(Jsoup.parse(input)).body().html();
    }

    /**
     * Removes markup except for simple text and certain other tags
     * suitable for printing; Any additional tags added to the whitelist
     * MUST NOT permit attributes!!!
     * @param input String dirty html ridden text
     * @return String with attribute-laden HTML removed
     */
    public static String removeMarkupExceptPrintable(final String input) {
        if (StringUtils.isEmpty(input)) {
            return "";
        }

        Document dirtydoc = Jsoup.parse(input);
        dirtydoc.select("p").prepend("<br/><br/>");

        Whitelist whitelist = Whitelist.simpleText();
        whitelist.addTags("br");

        Cleaner cleaner = new Cleaner(whitelist);
        return cleaner.clean(dirtydoc).body().html();
    }

    /**
     * Updates HTML fragment with specified attribute value;
     * handy for adding design-specific class attributes
     * to editorially hard-coded HTML.
     */
    public static String setHtmlAttribute(final String input, final String element, final String attribute, final String value) {
        String newText;

        if (StringUtils.isEmpty(input)) {
            newText = "";
        } else if (StringUtils.isEmpty(element) || StringUtils.isEmpty(attribute)) {
            newText = input;
        } else {
            Document codeFragment = Jsoup.parse(input);
            codeFragment.select(element).attr(attribute, value);
            newText = codeFragment.body().html();
        }

        return newText;
    }

	public static String unescapeHtml(String input) {
		if (input != null && input.length() > 0) {
			return StringEscapeUtils.unescapeHtml(input);
		} else {
			return "";
		}
	}

        public static String encodeUrl(String url) throws UnsupportedEncodingException {
            if (StringUtils.isEmpty(url)) {
                return url;
            }
            return URLEncoder.encode(url, "UTF-8");
        }

        public static String decodeUrl(String url) throws UnsupportedEncodingException {
            if (StringUtils.isEmpty(url)) {
                return url;
            }
            return URLDecoder.decode(url, "UTF-8");
        }

	public static double ceil(double num) {
		return Math.ceil(num);
	}

    /**
     * utility to strip namespace from values
     * @param input String value with namespace
     * @return String value sans namespace
     */
    public static String removeNamespace(final String input) {
        if (input == null) {
            return null;
        }
        Pattern pattern = Pattern.compile(".*:(.*)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return input;
        }
    }

	public static List<List<String>> splitStringArrayProperty(
			ValueMap properties, String name, int chunks) {
		String[] retArray;

		if (properties == null || name == null) {
			return Collections.emptyList();
		}

		retArray = properties.get(name, new String[0]);

		if (retArray != null) {
			return TagUtils.splitList(Arrays.asList(retArray), chunks);
		} else {
			return Collections.emptyList();
		}
	}

	public static ValueMap getPageTemplateProperties(Page page) {
		ValueMap retVal = ValueMap.EMPTY;

		if (page != null) {
			Template t = page.getTemplate();
			if (t != null) {
				Resource r = t.adaptTo(Resource.class);
				if (r != null) {
					retVal = r.adaptTo(ValueMap.class);
				}
			}
		}

		return retVal;
	}

	public static Resource getResource(ResourceResolver rr, String path) {
		if (rr != null && path != null && path.length() > 0) {
			return rr.getResource(path);
		} else {
			return null;
		}
	}

	public static boolean isNonExistingResource(Resource res) {
		if (res != null) {
			return ResourceUtil.isNonExistingResource(res);
		} else {
			return true;
		}
	}

    /**
     * I've run into a few instances where stripping everything from /jcr:content and beyond in a
     * resource path is a handy thing to do. Encapsulating that here so we can call it up in EL context
     * @param resourcePath String of complete path to resource, including unwanted jcr:content bits
     * @return String path without the unwanted jcr:content stuff
     */
    public static String getBasePath(String resourcePath) {
        if (resourcePath == null) {
            return null;
        }
        return resourcePath.replaceFirst("/jcr:content.*", "");
    }


    /** EL Accessor for creating Recipe objects. */
	public static Recipe getRecipe(final SniPage sniPage) {
        return new RecipeFactory()
                .withSniPage(sniPage)
                .build();
	}

	/** EL Accessor for creating Article objects. */
    public static Article getArticle(final SniPage sniPage) {
        return new ArticleFactory()
                .withSniPage(sniPage)
                .build();
    }

    /** EL Accessor for creating Menu objects. */
    public static Menu getMenu(final SniPage sniPage) {
        return new MenuFactory()
                .withSniPage(sniPage)
                .build();
    }

    /** EL Accessor for creating Talent objects. */
    public static Talent getTalent(final SniPage sniPage) {
        return new TalentFactory()
                .withSniPage(sniPage)
                .build();
    }

     /** EL Accessor for creating Episode objects. */
    public static Episode getEpisode(final SniPage sniPage) {
        return new EpisodeFactory()
                .withSniPage(sniPage)
                .build();
    }

    /** EL Accessor for creating EpisodeListing objects. */
    public static EpisodeListing getEpisodeListing(final SniPage sniPage) {
        return new EpisodeListingFactory()
                .withSniPage(sniPage)
                .build();
    }

     /** EL Accessor for creating Topic objects. */
    public static Topic getTopic(final SniPage sniPage) {
        return new TopicFactory()
                .withSniPage(sniPage)
                .build();
    }

    /** EL Accessor for creating Show objects. */
    public static Show getShow(final SniPage sniPage) {
        return new ShowFactory()
                .withSniPage(sniPage)
                .build();
    }

    /** EL Accessor for creating SocialToolbar objects. */
    public static SocialToolbar getSocialValues(final SniPage sniPage) {
        return new SocialToolbarFactory()
                .withSniPage(sniPage)
                .build();
    }


    /** EL Accessor for creating Channel objects. */
    public static Channel getChannel(final SniPage sniPage) {
        return new ChannelFactory()
                .withSniPage(sniPage)
                .build();
    }

    /** EL Accessor for creating Player objects. */
    public static Player getPlayer(final SniPage sniPage) {
        return new PlayerFactory()
                .withSniPage(sniPage)
                .build();
    }

    /** EL Accessor for creating Video objects. */
    public static Video getVideo(final SniPage sniPage) {
        return new VideoFactory()
                .withSniPage(sniPage)
                .build();
    }


    /** EL Accessor for parsing Player page deeplinks. */
    public static PlayerSelectors getPlayerPathInfo(final String pagePath) {
        return new PlayerSelectorsFactory()
                .withSniPagePath(pagePath);

    }


    /** EL Accessor for accessing Player page selectors. */
    public static PlayerSelectors getPlayerSelectors(final SniPage sniPage) {
        return new PlayerSelectorsFactory()
                .withSniPageSelectors(sniPage);

    }

    /** EL Accessor for accessing EpisodeListing page selectors. */
    public static EpisodeListingSelectors getEpisodeListingSelectors(final SniPage sniPage) {
        return new EpisodeListingSelectorsFactory()
        			.withSniPage(sniPage)
        			.build();

    }

    /** EL Accessor for accessing Asset-Recipe page selectors. */
    public static AssetRecipeSelectors getAssetRecipeSelectors(final SniPage sniPage) {
        return new AssetRecipeSelectorsFactory()
        			.withSniPage(sniPage)
        			.build();

    }


	/**
	 * Instantiate a Paginator from Sling Resource of a parsys
	 *
	 * @param resource
	 *            Sling Resource of the parsys you wish to paginate
	 * @return Paginator object
	 */
	public static Paginator getPaginator(Resource resource,
			SlingHttpServletRequest slingRequest) {
		if (resource == null) {
			return null;
		}
		return new Paginator(resource, slingRequest);
	}

	/**
	 * Function to get a page-worth of paragraphs from given Paginator
	 *
	 * @param paginator Paginator object to retrieve a "page" from
	 * @param pageNum "page" number to retrieve from the paginator
	 * @return List of Resource objects contained within the "page" requested
	 */
	public static List<Resource> getPageFromPaginator(Paginator paginator,
			int pageNum) {
		return paginator.page(pageNum);
	}

    /**
     * Construct an ArticleStepByStepTextImageComponent given the resource of an
     * article step-by-step text/image component
     * @param resource Sling resource of Article Step By Step text/image component
     * @return ArticleStepByStepTextImageComponent
     */
    public static ArticleStepByStepTextImageComponent articleStepByStepTextImageComponent(final Resource resource) {
        if (resource == null) {
            return null;
        }
        return new ArticleStepByStepTextImageComponent(resource);
    }

    /**
     * Construct an abstract ArticleSimpleTextImageComponent given the Resource of an
     * article simple text/image component
     * @param resource Sling Resource of an article-simple-text-image component
     * @return ArticleSimpleTextImageComponent
     */
    public static ArticleSimpleTextImageComponent articleSimpleTextImageComponent(final Resource resource) {
        if (resource == null) {
            return null;
        }
        return new ArticleSimpleTextImageComponent(resource);
    }

    /**
     * Construct an abstract ArticleSimpleTitleComponent given the Resource of an article-simple-title component
     * @param resource Sling Resource of an article-simple-title component
     * @return ArticleSimpleTitleComponent
     */
    public static ArticleSimpleTitleComponent articleSimpleTitleComponent(final Resource resource) {
        if (resource == null) {
            return null;
        }
        return new ArticleSimpleTitleComponent(resource);
    }

    /**
     * Construct a TitleDescriptionComponent given the Resource of a title-description component in Photo Gallery
     * @param resource Sling Resource of the title-description component
     * @return TitleDescriptionComponent
     */
    public static TitleDescriptionComponent photoGalleryTitleDescriptionComponent(final Resource resource) {
        if (resource == null) {
            return null;
        }
        return new TitleDescriptionComponent(resource);
    }

    /**
     * String util to swap out the asset specific part of an asset path with the content root path
     * @param assetPath String path to asset
     * @return String munged path should now point to content
     */
    @Deprecated
    public static String contentPathFromAssetPath(final String assetPath) {
        if (assetPath == null) {
            return null;
        }
        return assetPath.replaceFirst(AssetRootPaths.ASSET_ROOT.path(), ContentRootPaths.CONTENT_COOK.path());
    }

    /**
     * Function to get recent or upcoming episodes for specific show
     * @param currentPage
     * @param type
     * @param count
     * @return
     */
	public static List<Map<String,String>> getEpisodeData(Page currentPage,String type,String count,String pageType){
		EpisodeDataUtil dataUtil=new EpisodeDataUtil(currentPage,count);
		List <Map<String,String>>list=new ArrayList<Map<String,String>>();
		List <String> showNames=new ArrayList<String>();

		if(pageType.equalsIgnoreCase(Constant.CHEF_PAGE_TYPE)){
			showNames=DataUtil.findAssetsNameByPropertyValue(currentPage.getContentResource(), AssetRootPaths.SHOWS.path(),
					  AssetSlingResourceTypes.SHOW.resourceType() , currentPage.getProperties().get(Constant.ASSET_LINK,String.class));
			if(showNames==null || showNames.size()<1){
			//	log.debug("problem in getting list of showname "+showNames);
				return list;
			}

		}

		Calendar now=Calendar.getInstance();
		int hr=now.get(Calendar.HOUR_OF_DAY);
		int min= now.get(Calendar.MINUTE);
		String key;
		Integer node;
		Map<String, Integer> timeAndNodeMap = getTimeAndNodeMap();
		if(min>29 && min<60)
			key=hr+":30";
		else
			key=hr+":00";
		if(type.equalsIgnoreCase("up")){


			node = timeAndNodeMap.get(key)+1;

			return dataUtil.processUpComingEpisodes(node,showNames,pageType);
		}else if(type.equalsIgnoreCase("rec")){

			node = timeAndNodeMap.get(key);

			return dataUtil.processRecentEpisodes(node,showNames,pageType);
		}
		return list;
	}

    /**
     * Function to get current week episodes list
     * @param currentPage
     * @param isMobile
     * @param slingRequest
     * @param sling
     * @param pageType
     * @return
     */
	public static Map<String,List> getEpisodeDataThisWeek(Page currentPage, String isMobile,SlingHttpServletRequest slingRequest, SlingScriptHelper sling,String pageType){
		EpisodeDataUtil dataUtil=new EpisodeDataUtil(currentPage,"0");

		return dataUtil.processEpisodesThisWeek(isMobile,slingRequest,sling,pageType);
	}


	/**
     * Function to get episode "single recently aired, currently aired, and up coming episode in 2hrs time span (back and future)"
     * @param currentPage
     * @param isMobile
     * @return
     */
	public static Map<String,Object> getEpisodesJustOnTv(Page currentPage, boolean isMobile){

		EpisodeDataUtil dataUtil=new EpisodeDataUtil(currentPage, "1");
		return dataUtil.processJustOnTVEpisodes(isMobile);

	}

    //this same data can be found in DataUtil.TIME_CODE_MAP as a Map of Integer, String
	private static Map<String, Integer> getTimeAndNodeMap() {

		Map<String, Integer> timeHashMap = new HashMap<String, Integer>();

		timeHashMap.put("6:30", 1);
		timeHashMap.put("7:00", 2);
		timeHashMap.put("7:30", 3);
		timeHashMap.put("8:00", 4);
		timeHashMap.put("8:30", 5);
		timeHashMap.put("9:00", 6);
		timeHashMap.put("9:30", 7);
		timeHashMap.put("10:00", 8);
		timeHashMap.put("10:30", 9);
		timeHashMap.put("11:00", 10);
		timeHashMap.put("11:30", 11);
		timeHashMap.put("12:00", 12);
		timeHashMap.put("12:30", 13);
		timeHashMap.put("13:00", 14);
		timeHashMap.put("13:30", 15);
		timeHashMap.put("14:00", 16);
		timeHashMap.put("14:30", 17);
		timeHashMap.put("15:00", 18);
		timeHashMap.put("15:30", 19);
		timeHashMap.put("16:00", 20);
		timeHashMap.put("16:30", 21);
		timeHashMap.put("17:00", 22);
		timeHashMap.put("17:30", 23);
		timeHashMap.put("18:00", 24);
		timeHashMap.put("18:30", 25);
		timeHashMap.put("19:00", 26);
		timeHashMap.put("19:30", 27);
		timeHashMap.put("20:00", 28);
		timeHashMap.put("20:30", 29);
		timeHashMap.put("21:00", 30);
		timeHashMap.put("21:30", 31);
		timeHashMap.put("22:00", 32);
		timeHashMap.put("22:30", 33);
		timeHashMap.put("23:00", 34);
		timeHashMap.put("23:30", 35);
		timeHashMap.put("0:00", 36);
		timeHashMap.put("0:30", 37);
		timeHashMap.put("1:00", 38);
		timeHashMap.put("1:30", 39);
		timeHashMap.put("2:00", 40);
		timeHashMap.put("2:30", 41);
		timeHashMap.put("3:00", 42);
		timeHashMap.put("3:30", 43);
		timeHashMap.put("4:00", 44);
		timeHashMap.put("4:30", 45);
		timeHashMap.put("5:00", 46);
		timeHashMap.put("5:30", 47);
		timeHashMap.put("6:00", 48);

		return timeHashMap;
	}

	/**
	 * get the paths of referenced modules from region
	 *
	 * @param regionResource
	 *            Sling Resource of the parsys you wish to paginate
	 * @return pathList
	 */

public static ArrayList<String> referencedModulePaths(Resource regionResource )
	{
		ArrayList<String> pathList=new ArrayList<String>();
		if (regionResource != null)
		{
			Iterator<Resource> leadItr = regionResource.listChildren();
			while(leadItr.hasNext()){
				Resource currentResource = leadItr.next();
				if(currentResource != null) {
					String path = null;
					Node node = currentResource.adaptTo(Node.class);
					try {
						if(node.hasProperty("path"))
						{
							path = node.getProperty("path").getString();
							if(path !=null) {
								pathList.add(path);
							}
						}
					} catch (ValueFormatException e) {
						log.error(ExceptionUtils.getFullStackTrace(e));
					} catch (PathNotFoundException e) {
						log.error(ExceptionUtils.getFullStackTrace(e));
					} catch (RepositoryException e) {
						log.error(ExceptionUtils.getFullStackTrace(e));
					}
				}

			}
		}
		return pathList;
	}
	public static boolean  isValidMobileModule(SlingHttpServletRequest request ,String modulePath, String resourceType){
		try{
			ResourceResolver rr = request.getResourceResolver();
			Resource moduleResource = rr.getResource(modulePath);
			Node moduleNode = moduleResource.adaptTo(Node.class);
			if(moduleNode.hasProperty("sling:resourceType"))
			{
				String nodeResourceType = moduleNode.getProperty("sling:resourceType").getString();

				if(nodeResourceType != null && nodeResourceType .trim().length() > 0)
				{
					if(nodeResourceType.equals(resourceType))
					{
						return true;
					}
				}
			}
		}catch(Exception e) {  }
		return false;
	}

	public static boolean pageHasChild(Page currentPage, String path) {
		boolean hasPage = false;
		if (currentPage != null && path != null && path.length() > 0) {
			hasPage = currentPage.hasChild(path);
		}
		return hasPage;
	}

	/**
	 * Method will returns all the recipe details of the episodes in current day.
	 *
	 * @param pageManager
	 * @return Map contains scheduled times and respective recipe details
	 * @throws RepositoryException
	 */
	public static Map<String, Map<String, Object>> getCurrentDayEpisodeData(PageManager pageManager, Node queryRoot) throws RepositoryException
	{
		if(null == pageManager){
			return null;
		}

		Map<String, Map<String, Object>> currentDayEpisodeData = new HashMap<String, Map<String,Object>>();

		Calendar calendar = Calendar.getInstance();
		Calendar modifiedCalander = (Calendar) calendar.clone();

		int currentYear = 0;
		int currentDay = 0;
		int currentMonth = 0;

		boolean dateModified = false;

		Map<String, Integer> timeAndNodeMap = getTimeAndNodeMap();

		if (null != timeAndNodeMap) {
			Set<String> scheduleTimes = timeAndNodeMap.keySet();

			if (null != scheduleTimes) {
				Iterator<String> scheduleTimesItr = scheduleTimes.iterator();

				while(scheduleTimesItr.hasNext())
				{
					String sTime = scheduleTimesItr.next();
					Integer nodeNbr = timeAndNodeMap.get(sTime);

					// From 00:00 to 6:00, we need to get the details from previous day
					if(nodeNbr > 35 && nodeNbr < 49)
					{
						if(!dateModified)
						{
							modifiedCalander.add(Calendar.DAY_OF_YEAR, -1);
							dateModified = true;
						}

						currentYear = modifiedCalander.get(Calendar.YEAR);
						currentDay = modifiedCalander.get(Calendar.DAY_OF_MONTH);
						currentMonth = modifiedCalander.get(Calendar.MONTH) + 1;
					}
					else
					{
						currentYear = calendar.get(Calendar.YEAR);
						currentDay = calendar.get(Calendar.DAY_OF_MONTH);
						currentMonth = calendar.get(Calendar.MONTH) + 1;
					}

					String assetPath = "/etc/sni-asset/schedules/cook/" + currentYear + "/" + currentMonth + "/" + currentDay + "/" + nodeNbr;

					// the method 'getEpisodeDetailsForGivenAsset()' will get the properties and values from the episode of all the paths given
					//currentDayEpisodeData.put(sTime, getEpisodeDetailsForGivenAsset(pageManager, assetPath, queryRoot));
					Page timeSlotPage = pageManager.getPage(assetPath);
					if(timeSlotPage != null)
						currentDayEpisodeData.put(sTime, EpisodeDataUtil.processEpisode(timeSlotPage,true, false));

				}
			}
		}
		return currentDayEpisodeData;
	}

	/**
	 * Method will get the recipe properties thru asset path and returns the titles and url of all the recipes associated with the given asset path
	 *
	 * @param pageManager
	 * @param assetPath Path of the asset, method will fetch the episode path from the asset data node.
	 * @return Map will return the properties and their values for the requested episode data node.
	 * @throws RepositoryException
	 */
	private static Map<String, String> getEpisodeDetailsForGivenAsset(PageManager pageManager, String assetPath, Node queryRoot) throws RepositoryException {

		//log.info("Entering getEpisodeDetailsForGivenAsset");

		Map<String, String> recipeDetails = null;
		String peoplePathLst[] = null;

		// Fetching the asset node using asset path
		Page assetPage = pageManager.getPage(assetPath);

		if(null == assetPage)
		{
			log.info("Asset Page is NULL");
			return null;
		}

		// Fetching the asset properties
		ValueMap assetPageProperties = assetPage.getProperties();
		if(null == assetPageProperties)
		{
			log.info("Asset Properties are NULL");
			return null;
		}

		// 'sni:episode' contains the episode path
		String episodePath = assetPageProperties.get("sni:episode",	String.class);

		if(StringUtils.isNotBlank(episodePath))
		{
			// Fetching the episode node using episode path
			Page episodeAssetNode = pageManager.getPage(episodePath);
			if(null == episodeAssetNode)
			{
				return null;
			}

			// Fetching the episode properties
			ValueMap episodeProperties = episodeAssetNode.getProperties();
			if(null == episodeProperties)
			{
				return null;
			}

			//log.info("Get Episode Details Block Started");

			recipeDetails = setEpisodeDetailForOnTvNow(recipeDetails, episodeProperties, queryRoot, episodePath);

			// if peoplePath is not available at episode level then we need to get from show level
			/*if(null == peoplePathLst)
			{
				peoplePathLst = episodeProperties.get("sni:people", String[].class);
			}*/

			//log.info("Get Episode Details Block End");

			//log.info("Get Show Details Block Started");

			Page showAssetNode = getDesiredParentNode(episodeAssetNode, "SHOW");

			if(null != showAssetNode)
			{
				ValueMap showAssetProperties = showAssetNode.getProperties();

				recipeDetails = setShowDetailForOnTvNow(recipeDetails, showAssetNode, showAssetProperties, queryRoot);

				// if peoplePath is not available at episode level then we need to get from show level
				if(null == peoplePathLst && null != showAssetProperties)
				{
					peoplePathLst = showAssetProperties.get("sni:primaryTalent",String[].class);
				}
			}

			//log.info("Get Show Details Block End");

		//	log.info("Get People Details Block Started");

			if(null != peoplePathLst)
			{
				recipeDetails = setPeopleDetailForOnTvNow(pageManager, recipeDetails, peoplePathLst, queryRoot);
			}

			//log.info("Get People Details Block End");

			//log.info("Get Recipe Details Block Started");

			String[] sniRecipes = episodeProperties.get("sni:recipes", String[].class);
			
			/*
			 * If the recipes are empty then display the description of the episode
			 * 
			 * If the recipes are not empty, then display the titles of all recipes
			 */
			if(null != sniRecipes)
			{
				recipeDetails = setRecipeDetailForOnTvToday(pageManager, recipeDetails, sniRecipes, queryRoot);
			}
			else
			{
				if(null == recipeDetails){
					recipeDetails = new HashMap<String, String>();
				}

				recipeDetails.put("recipeDesc", episodeProperties.get("jcr:description", String.class));
			}

			//log.info("Get Recipe Details Block End");
		}

		return recipeDetails;
	}

	/**
	 * Method returns the data node from query response
     * see also DataUtil methods for querying for specific assets
	 *
	 * @param queryResForEpisode
	 * @return
	 */
	public static Node getContentNodeGivenQueryResponse(QueryResult queryResForEpisode)
	{
		//log.info("Entering getContentNodeGivenQueryResponse");

		if(null == queryResForEpisode)
		{
			log.info("Query Result is NULL");
			return null;
		}

		try
		{
			NodeIterator nodeItr = queryResForEpisode.getNodes();

			while(nodeItr.hasNext())
			{
				Node contentNode = nodeItr.nextNode();

				if(null != contentNode.getName() && "jcr:content".equals(contentNode.getName()))
				{
					return contentNode;
				}
			}
		}
		catch (RepositoryException e) {
			log.error(e.getMessage());
		}

		return null;
	}

	/**
	 * Method returns the parent of the given node. The parent is confirmed using the given assetType
	 *
	 * @param parentPage
	 * @param parentAssetType
	 * @return
	 */
	private static Page getDesiredParentNode(Page parentPage, String parentAssetType)
	{
	//	log.info("Entering getDesiredParentNode method");

		while(true)
		{
			if(null != parentPage)
			{
				if(null != parentPage.getProperties())
				{
					ValueMap properties = parentPage.getProperties();

					String assetType = properties.get("sni:assetType", String.class);

					if(StringUtils.isNotBlank(assetType) && parentAssetType.equals(assetType))
					{
						// Got the requested parent
						return parentPage;
					}
					else
					{
						// Going one more level up
						parentPage = parentPage.getParent();
					}
				}
				else
				{
					// Going one more level up
					parentPage = parentPage.getParent();
				}
			}
			else
			{
				log.info("Parent is NULL, Exiting getDesiredParentNode method");
				return null;
			}
		}
	}

	/**
	 * Method will query the given text and returns the response.
	 *
	 * @param queryRoot
	 * @param path :- This is a filter, it specifies the path in which the query is going to execute
	 * @param text :- This is the search text, which is going to search for
	 * @return QueryResult :- Search result
	 */
	public static QueryResult getQueryResponseGivenText(Node queryRoot, String path, String text)
	{
		//log.info("Entering getQueryResponseGivenText method");

		if(null == queryRoot || StringUtils.isBlank(path) || StringUtils.isBlank(text))
		{
			log.info("Query Root or Path or Text is NULL");
			return null;
		}

		try
		{
			QueryManager queryMgr = queryRoot.getSession().getWorkspace().getQueryManager();

			if(null == queryMgr)
			{
				log.info("Query Manager is NULL");
				return null;
			}
			 
			/*
			 * Building Query statement using the 'path' and 'text'
			 */
			String queryStatement = "SELECT * FROM [nt:base] AS s WHERE ISDESCENDANTNODE(["+ path +"]) and CONTAINS(s.*, '"+ text +"')";

			Query query = queryMgr.createQuery(queryStatement, Query.JCR_SQL2);

			return query.execute();

		}
		catch (RepositoryException e)
		{
			log.error(e.getMessage());
		}

		//log.info("Exiting getQueryResponseGivenText method");
		return null;
	}

	/**
	 * Method will set the episode details to recipeDetails object
	 *
	 * @param recipeDetails
	 * @param episodeProperties
	 * @param queryRoot
	 * @param episodePath
	 */
	private static Map<String, String> setEpisodeDetailForOnTvNow(Map<String, String> recipeDetails, ValueMap episodeProperties, Node queryRoot, String episodePath)
	{
		String episodeTitle = "";
		String episodeUrl = "";
		boolean needEpisodeAsset = true;

		Node episodeContentNode = getContentNodeGivenQueryResponse(getQueryResponseGivenText(queryRoot, "/content", episodePath));

		if(null != episodeContentNode)
		{
			try
			{
				if(episodeContentNode.hasProperty("jcr:title"))
				{
					episodeTitle = episodeContentNode.getProperty("jcr:title").getString();
				}
			}
			catch (ValueFormatException e) {
				log.error(e.getMessage());
			}
			catch (PathNotFoundException e) {
				log.error(e.getMessage());
			}
			catch (RepositoryException e) {
				log.error(e.getMessage());
			}

			if(StringUtils.isNotBlank(episodeTitle))
			{
				needEpisodeAsset = false;
			}
		}

		//log.info("needEpisodeAsset:::" + needEpisodeAsset);

		if(needEpisodeAsset)
		{
			episodeTitle = episodeProperties.get("jcr:title", String.class);
		}

		episodeUrl = episodeProperties.get("sni:fastfwdUrl", String.class);

		if(StringUtils.isNotBlank(episodeTitle) && StringUtils.isNotBlank(episodeUrl))
		{
			if(null == recipeDetails)
			{
				recipeDetails = new LinkedHashMap<String, String>();
			}

			recipeDetails.put("episodeTitle", episodeTitle);
			recipeDetails.put("episodeUrl", episodeUrl);
		}

		return recipeDetails;
	}

	/**
	 * Method sets the Show details to recipeDetails object
	 *
	 * @param recipeDetails
	 * @param showAssetNode
	 * @param showAssetProperties
	 * @param queryRoot
	 */
	private static Map<String, String> setShowDetailForOnTvNow(Map<String, String> recipeDetails, Page showAssetNode, ValueMap showAssetProperties, Node queryRoot)
	{
			String showTitle = "";
			String showUrl = "";
			boolean needShowAsset = true;

			Node showContentNode = getContentNodeGivenQueryResponse(getQueryResponseGivenText(queryRoot, "/content", showAssetNode.getPath()));

			if(null != showContentNode)
			{
				try
				{
					if(showContentNode.hasProperty("jcr:title"))
					{
						showTitle = showContentNode.getProperty("jcr:title").getString();
					}
				} catch (ValueFormatException e) {
					log.error(e.getMessage());
				} catch (PathNotFoundException e) {
					log.error(e.getMessage());
				} catch (RepositoryException e) {
					log.error(e.getMessage());
				}

				if(StringUtils.isNotBlank(showTitle))
				{
					needShowAsset = false;
				}
			}

			//log.info("needShowAsset" + needShowAsset);

			if(null != showAssetProperties)
			{
				if(needShowAsset)
				{
					showTitle = showAssetProperties.get("jcr:title", String.class);
				}

				showUrl = showAssetProperties.get("sni:fastfwdUrl", String.class);
			}

			if(StringUtils.isNotBlank(showTitle) && StringUtils.isNotBlank(showUrl))
			{
				if(null == recipeDetails){
					recipeDetails = new LinkedHashMap<String, String>();
				}

				recipeDetails.put("showTitle", showTitle);
				recipeDetails.put("showUrl", showUrl);
			}
		return recipeDetails;
	}

	/**
	 * Method sets the people Details to recipeDetails object
	 *
	 * @param pageManager
	 * @param recipeDetails
	 * @param peoplePathLst
	 * @param queryRoot
	 */
	private static Map<String, String> setPeopleDetailForOnTvNow(PageManager pageManager, Map<String, String> recipeDetails, String[] peoplePathLst, Node queryRoot)
	{
		int peopleCount = peoplePathLst.length;

		if(null == recipeDetails){
			recipeDetails = new LinkedHashMap<String, String>();
		}

		recipeDetails.put("peopleCount", peopleCount+"");

		for (int peopleIndx = 0; peopleIndx < peopleCount; peopleIndx++) {

			String hostName = "";
			String peoplePath = "";
			Value[] contentHostImgPaths = null;
			String[] assetHostImgPaths = null;
			boolean needPeopleAsset = true;
			boolean needHostImgAsset = true;

			ValueMap peopleProperties = null;

			peoplePath = peoplePathLst[peopleIndx];

			Node peopleContentNode = getContentNodeGivenQueryResponse(getQueryResponseGivenText(queryRoot, "/"+peoplePath, peoplePath));

			if (null != peopleContentNode)
			{
				try
				{
					if (peopleContentNode.hasProperty("jcr:title"))
					{
						hostName = peopleContentNode.getProperty("jcr:title").getString();
					}
					if (peopleContentNode.hasProperty("jcr:title"))
					{
						contentHostImgPaths = peopleContentNode.getProperty("sni:avatarImage").getValues();
						if (null != contentHostImgPaths && (contentHostImgPaths.length > 0))
						{
							needHostImgAsset = false;
						}
					}
				}
				catch (ValueFormatException e) {
					log.error(e.getMessage());
				} catch (PathNotFoundException e) {
					log.error(e.getMessage());
				} catch (RepositoryException e) {
					log.error(e.getMessage());
				}

				if (StringUtils.isNotBlank(hostName)) {
					needPeopleAsset = false;
				}
			}

			//log.info("needPeopleAsset:::" + needPeopleAsset);

			Page peopleAssetNode = pageManager.getPage(peoplePath);

			if(null != peopleAssetNode)
			{
				peopleProperties = peopleAssetNode.getProperties();
			}

			if(null != peopleProperties)
			{
				if(needPeopleAsset)
				{
					hostName = peopleProperties.get("jcr:title", String.class);
					assetHostImgPaths = peopleProperties.get("sni:avatarImage", String[].class);
				}
			}

			recipeDetails.put("hostName_" + (peopleIndx + 1), hostName);
			recipeDetails.put("hostNameUrl_" + (peopleIndx + 1), peopleProperties==null?null:peopleProperties.get("sni:pageLinks", String.class));

			if(!needHostImgAsset)
			{
				int peopleImgCount = contentHostImgPaths.length;

				for (int imgIndx = 0; imgIndx < peopleImgCount; imgIndx++)
				{
					try
					{
						recipeDetails.put("hostImg_" + (imgIndx + 1), contentHostImgPaths[imgIndx].getString());
					}
					catch (ValueFormatException e) {
						log.error(e.getMessage());
					} catch (IllegalStateException e) {
						log.error(e.getMessage());
					} catch (RepositoryException e) {
						log.error(e.getMessage());
					}
				}
			}
			else
			{
				if(null != assetHostImgPaths)
				{
					int peopleImgCount = assetHostImgPaths.length;
					recipeDetails.put("peopleImgCount", peopleImgCount+"");

					for (int imgIndx = 0; imgIndx < peopleImgCount; imgIndx++)
					{
						recipeDetails.put("hostImg_" + (imgIndx + 1), assetHostImgPaths[imgIndx]);
					}
				}
			}
		}
		return recipeDetails;
	}


	/**
	 * Method to retrieve list of featured items (recipes) on photo gallery page.
	 * @param currentResource
	 * @return
	 */

	public static List<Map<String,String>> getFeaturedGallery(final Resource currentResource){

		List <Map<String,String>>featuredList=new ArrayList <Map<String,String>>();
		Resource dummy=null,refContent=null,refAsset=null,talentResource=null;
		String refContentPath=null,refContentAssetPath=null,talentContentPath=null,talentAssetPath=null,totalTime=null;
		Map<String,String> dataMap=new HashMap<String,String>();
		ValueMap assetMap=null;
		String []overrideProp={Constant.JCR_TITLE,Constant.SNI_PROP_IMAGES,Constant.PEOPLE_LINK};
		String imagesPath=null;
		Map<String,Object> propMap;
		ResourceResolver resourceResolver=null;
		int count=0;

		try{

			Validate.notNull(currentResource);
			resourceResolver=currentResource.getResourceResolver();
			dummy=currentResource.getChild("gallery-contents");
			Validate.notNull(dummy);
			dummy=dummy.getChild("parsys");
			Validate.notNull(dummy);
			Iterator <Resource> itr=dummy.listChildren();
			while(itr.hasNext()){
				try{
					dummy=itr.next();
					if(dummy.getName().matches("^page_break[_a-z0-9]*"))
						continue;
					refContentPath=ResourceUtil.getValueMap(dummy).get("attachedContent",String.class);
					if(refContentPath==null || refContentPath.isEmpty())
						continue;
					refContent=resourceResolver.getResource(refContentPath).getChild(Constant.JCR_CONTENT);
					assetMap=ResourceUtil.getValueMap(refContent);
					refContentAssetPath=assetMap.get("sni:assetLink",String.class);
					refAsset=resourceResolver.getResource(refContentAssetPath).getChild(Constant.JCR_CONTENT);
					propMap=DataUtil.mergeResourceProperties(refAsset, refContent, overrideProp);
					if(refContent.getResourceType().equalsIgnoreCase(PageSlingResourceTypes.SHOW.resourceType()) && count==0){
						dataMap.put(Constant.SH_TITLE, propMap.get(Constant.JCR_TITLE).toString());
						dataMap.put(Constant.URL, refContentPath+Constant.HTML);
						String showDesc=propMap.get(Constant.JCR_DESCRIPTION).toString();
						if(showDesc.indexOf(" ", 200)>-1)
		                    showDesc=showDesc.substring(0,showDesc.indexOf(" ", 200));
						dataMap.put(Constant.DESCRIPTION, showDesc);
						featuredList.add(count,dataMap);
						dataMap=new HashMap<String,String>();
						count++;
						continue;
					}
					dataMap.put(Constant.RECIPE_TITLE, propMap.get(Constant.JCR_TITLE).toString());
					dataMap.put(Constant.URL, refContentPath+Constant.HTML);
					totalTime=getTotalTime(propMap);
					dataMap.put(Constant.TIME, totalTime);
					dataMap.put(Constant.PROP_DIFFICULTY, propMap.get(Constant.SNI_PROP_DIFFICULTY).toString());
					if(propMap.get(Constant.SNI_PROP_IMAGES)!=null){
						imagesPath=((Object[])propMap.get(Constant.SNI_PROP_IMAGES))[0].toString();
						dataMap.put(Constant.CH_IMAGE, imagesPath);
					}

					if(propMap.get(Constant.PEOPLE_LINK)!=null){
						talentAssetPath=((Object[])propMap.get(Constant.PEOPLE_LINK))[0].toString();
						talentContentPath=DataUtil.personPagePathFromAssetPath(talentAssetPath);
						dataMap.put(Constant.CH_URL, talentContentPath);
						talentResource=resourceResolver.getResource(talentAssetPath).getChild(Constant.JCR_CONTENT);
						dataMap.put(Constant.CH_TITLE, ResourceUtil.getValueMap(talentResource).get(Constant.JCR_TITLE,String.class));
					}

					featuredList.add(count,dataMap);
					dataMap=new HashMap<String,String>();
					count++;
				}catch(Exception ex){
					//log.error("NullPointerException occure in getFeaturedGallery "+ex.getMessage());
					continue;
				}
			}
		}catch(Exception ex){
			log.error("Exception occure in getFeaturedGallery due to "+ex.getMessage());
			return featuredList;
		}
		return featuredList;
	}

	private static String getTotalTime(Map<String,Object> propMap){
		Validate.notNull(propMap);
		String totalTime="";
		int prepTime=0,cookTime=0,inactivePrepTime=0,addTime=0;
		if(propMap.get(Constant.SNI_PROP_PREP_TIME)!=null)
			prepTime=Integer.parseInt(propMap.get(Constant.SNI_PROP_PREP_TIME).toString());
		if(propMap.get(Constant.SNI_PROP_INACTIVE_PREP_TIME)!=null)
			inactivePrepTime=Integer.parseInt(propMap.get(Constant.SNI_PROP_INACTIVE_PREP_TIME).toString());
		if(propMap.get(Constant.SNI_PROP_COOK_TIME)!=null)
			cookTime=Integer.parseInt(propMap.get(Constant.SNI_PROP_COOK_TIME).toString());
		addTime=prepTime+inactivePrepTime+cookTime;
		if(addTime>=60)
			totalTime=(addTime/60)+Constant.HOUR+(addTime%60)+Constant.MINUTE;
		else
			totalTime=addTime+Constant.MINUTE;

		return totalTime;
	}

	/**
	 * Set Recipe Details to the recipeDetails object
	 *
	 * @param pageManager
	 * @param recipeDetails
	 * @param sniRecipes
	 * @param queryRoot
	 */
	private static Map<String, String> setRecipeDetailForOnTvToday(PageManager pageManager, Map<String, String> recipeDetails, String[] sniRecipes, Node queryRoot)
	{
		int recipeCount = sniRecipes.length;

		if(null == recipeDetails){
			recipeDetails = new LinkedHashMap<String, String>();
		}

		recipeDetails.put("recipeCount", recipeCount+"");

		if(recipeCount > 0)
		{
			// Properties are getting for all the recipes in the list
			for(int recipeIndx = 0; recipeIndx < recipeCount; recipeIndx++)
			{
				String recipeTitle = "";
				String recipeUrl = "";
				ValueMap recipeProperties = null;
				boolean needRecipeAsset = true;

				Node recipeContentNode = getContentNodeGivenQueryResponse(getQueryResponseGivenText(queryRoot, "/content", sniRecipes[recipeIndx].trim()));

				Page recipeAssetNode = pageManager.getPage(sniRecipes[recipeIndx].trim());
				if(null != recipeAssetNode)
				{
					recipeProperties = recipeAssetNode.getProperties();
				}

				if(null != recipeContentNode)
				{
					try
					{
						if(recipeContentNode.hasProperty("jcr:title"))
						{
							recipeTitle = recipeContentNode.getProperty("jcr:title").getString();
						}
					}
					catch (ValueFormatException e) {
						log.error(e.getMessage());
					} catch (PathNotFoundException e) {
						log.error(e.getMessage());
					} catch (RepositoryException e) {
						log.error(e.getMessage());
					}

					if(StringUtils.isNotBlank(recipeTitle))
					{
						needRecipeAsset = false;
					}
				}

				//log.info("needRecipeAsset:::" + needRecipeAsset);

				if(null != recipeProperties)
				{
					if(needRecipeAsset)
					{
						recipeTitle = recipeProperties.get("jcr:title", String.class);
					}

					recipeUrl = recipeProperties.get("sni:fastfwdUrl", String.class);
				}

				if(StringUtils.isNotBlank(recipeTitle))
				{
					recipeDetails.put("recipeTitle_" + (recipeIndx + 1), recipeTitle);
					recipeDetails.put("recipeUrl_" + (recipeIndx + 1), recipeUrl);
				}
			}
		}
		return recipeDetails;
	}

	/**
	 * Method to retrieve show and episode information related to specific recipe
	 *
	 * @param currentPage
	 * @param queryText
	 * @param isMobile
	 */

	public static Map<String,String> getEpisodeAndShowForRecipe(Page currentPage, String queryText,String isMobile){
		Map<String,String> map=new HashMap<String,String>();
		String contentPath=null;
		String assetPath=Constant.SHOW_ASSET_PATH;
		PageManager pageManager=currentPage.getPageManager();
		if(isMobile.equals(Constant.FALSE))
			contentPath=Constant.SHOW_CONTENT_PATH;
		else
			contentPath=Constant.SHOW_MOBILE_CONTENT_PATH;

		try{
			Node queryRoot=currentPage.adaptTo(Node.class);
			Node episodeAssetNode = getContentNodeGivenQueryResponse(getQueryResponseGivenText(queryRoot,assetPath,queryText));
			Node episodeContentNode=getContentNodeGivenQueryResponse(getQueryResponseGivenText(queryRoot,contentPath,episodeAssetNode.getParent().getPath()));
			String episodeContentPath=episodeContentNode.getParent().getPath();
			Page episodePage=pageManager.getPage(episodeContentPath);
			ValueMap propMap=episodePage.getProperties();
			map.put(Constant.EP_TITLE, propMap.get(Constant.JCR_TITLE,String.class));
			map.put(Constant.EP_URL,episodeContentPath+Constant.HTML );
			Page showPage=episodePage.getParent().getParent();
			propMap=showPage.getProperties();
			map.put(Constant.SH_TITLE, propMap.get(Constant.JCR_TITLE,String.class));
			map.put(Constant.SH_URL,showPage.getPath()+Constant.HTML );
		}catch(Exception ex){
			log.error("Exception occure in getEpisodeAndShowForRecipe "+ex.getMessage());
		}
		return map;
	}
	/***
	 * Method to get number of videos within a page. For Video and Video Player pages it simply returns 1
	 * for Video Channel pages it will return the number of videos specifided in the sni:videos array under the channel component
	 *
	 * @param videoChannel of type CQ page
	 * @return
	 */
	public static int getNumberOfVideos(Page videoChannel) {
		if(videoChannel != null){

			String templatePath = videoChannel.getProperties().get("cq:template") != null ? videoChannel.getProperties().get("cq:template").toString() :null;
			if(templatePath != null){
				String vidTemplate = templatePath.substring(templatePath.lastIndexOf("/") +1);
			if(vidTemplate.equals("video") || vidTemplate.equals("video-player-page"))
				return 1;
			else if(vidTemplate.equals("video-channel"))
			{
				Resource channelResource = videoChannel.getContentResource("channel-component/sni:videos");
				if(channelResource != null)
				{
					try{
						Property videoListProp = channelResource.adaptTo(Property.class);
						if(videoListProp.isMultiple())
							return videoListProp.getValues().length;
						else
							return 1;
						}catch(Exception ex){
							log.error("Exception occured in getNumberOfVideos "+ex.getMessage());
						}
				}
			}
			}


		}
		return 0;
	}

	/***
	 *
	 * @param sling
	 * @return
	 */
	public static Map<String, Object> getEpisodeFinderMap(SlingScriptHelper sling)
	{
		SearchService ss = sling.getService(SearchService.class);
		if (ss != null) {
	        SearchRequestHandler srh;
	        try {
	            srh = ss.getSearchRequestHandler();
	            SearchResponse sr = srh.getResponse("episodeFinder", null);
				if (sr.isValid())
            	    return SearchObjectMapper.getAsMap(sr);
	        } catch (IllegalStateException ise) {
	            srh = null;
	        }
		}
		return null;
	}

    /** Get a property map of asset data with page data merged in.
     *
     * Given a page content resource, returns the properties of the asset linked by the sni:assetLink property.
     * If overrides are indicated, properties in the page override properties in the asset.
     */
    public static Map<String, Object> getMergedAssetProperties(Resource resource, String[] overrides) {

        Map<String, Object> retVal = ValueMap.EMPTY;

        if (resource == null) {
            return retVal;
        }
        ResourceResolver resourceResolver = resource.getResourceResolver();
        ValueMap properties = ResourceUtil.getValueMap(resource);
        String assetLink = properties.get("sni:assetLink", String.class);

        if (assetLink != null) {
            Resource assetResource = resourceResolver.getResource(assetLink + "/jcr:content");

            if (assetResource != null) {
                retVal = DataUtil.mergeResourceProperties(assetResource, resource, overrides);
            }
        }

        // no asset properties for some reason, return Page properties
        if (retVal == ValueMap.EMPTY) {
            retVal = new HashMap<String, Object>();
            for (String prop : overrides) {
                retVal.put(prop, properties.get(prop));
            }
        }

        return retVal;
    }

    public static Resource findContentForAssetPath(ResourceResolver resourceResolver, String assetPath, String contentRoot) {
        Resource contentResource = null;
        String contentPrefix = null;

        if (resourceResolver == null) {
            log.warn("null resource resolver");
            return null;
        }

        if (assetPath == null || ! (assetPath.length() > 0)) {
            log.warn("null or empty assetPath");
            return null;
        }

        if (! contentRoot.matches("^/content/[^/]+$")) {
            log.warn("content root not for site");
            return null;
        }

        Iterator<Resource> resIt = resourceResolver.findResources("/jcr:root" + contentRoot + "//element(*,cq:PageContent)[@sni:assetLink='" + assetPath + "']", Query.XPATH);

        if (resIt.hasNext()) {
            contentResource = resIt.next();
        } else {
            log.warn("no content resource found");
        }

        return contentResource;
    }

    public static String capitalizeFully(String str) {
        if (str != null) {
            return WordUtils.capitalizeFully(str);
        } else {
            return null;
        }
    }

    /** method to get talent's title associated with recipe
     *
     * @param recipePage
     * @return
     */
    public static String talentTitleFromRecipe(Page recipePage){
        String talentTitle = null;

    	if (recipePage != null) {
            SniPage recipeSniPage = PageFactory.getSniPage(recipePage);
            if (recipeSniPage != null) {
                Recipe recipe = getRecipe(recipeSniPage);
                if (recipe != null && recipe.getRelatedTalentPage() != null) {
                    talentTitle = recipe.getRelatedTalentPage().getTitle();
                }
            }
        }

        return talentTitle;
    }

    /**
     * Returns boolean if the Show Page has a regular (non-special) series. Series name will start with a 
     * number if it is regular standard series
     * Used to determine if the Episode Guide link should appear
     * @param showPage
     * @return
     */
    public static boolean isNonSpecialShow(Page showPage) {
    	if(showPage != null) {
    		Iterator<Page> seriesIter = showPage.listChildren();
    		while(seriesIter.hasNext()) {
    			Page seriesPage = seriesIter.next();
    			String firstChar = seriesPage.getName().charAt(0) + "";
    			try {
    				int seriesInt = Integer.parseInt(firstChar);

    				if(seriesPage.hasContent()) {
        				Iterator<Page> episodeIt = seriesPage.listChildren();
        				while(episodeIt.hasNext()) {
        					Page episodePage = episodeIt.next();
        					if(episodePage != null) {
        						return true;
        					}
        				}
        			}
    			} catch (NumberFormatException nfe) {
    				//do nothing, iterate to next series
    			}
    		}
    	}
    	return false;
    }

	/**
     * Returns the html needed to display a link type icon given the html from a RTE that contains the icon type.
     * @param valueOfRTE
     * @return
     */
    public static String getTextLinkIcon(String valueOfRTE) {
    	if(valueOfRTE != null) {
			if(valueOfRTE.contains("icontype=\"video\""))
			{
				return "<i class='ss-video'></i>";
			}
			if(valueOfRTE.contains("icontype=\"gallery\""))
			{
				return "<i class='ss-layers'></i>";
			}
    	}
    	return "";
    }

    public static PhotoGallery getPhotoGallery(Resource resource) {
        return new PhotoGalleryFactory().withParsysResource(resource).build();
    }

    public static PhotoGallerySlide getPhotoGallerySlide(Resource resource) {
        return new PhotoGallerySlideFactory().withResource(resource).build();
    }

    public static SniPage getContainingPage(Resource resource) {
        PageManager pageManager = resource.getResourceResolver().adaptTo(PageManager.class);
        return PageFactory.getSniPage(pageManager.getContainingPage(resource));
    }

    public static Talent getPrimaryTalent(SniPage page) {
        Talent talent = null;
        SniPage talentPage = new RelationshipModelFactory()
            .withSniPage(page)
            .build()
            .getPrimaryTalent();
        if (talentPage != null) {
            talent = new TalentFactory()
                .withSniPage(talentPage)
                .build();
        }
        return talent;
    }

    public static Talent getTalent(Resource resource) {
        return new TalentFactory().withResource(resource).build();
    }

    public static Beverage getBeverage(SniPage sniPage) {
        return new BeverageFactory().withSniPage(sniPage).build();
    }

    public static String getMetadataProperty(MetadataManager metadataManager, String prop) {
        MetadataProperty metadataProperty = MetadataProperty.valueOf(prop);
        return metadataManager.get(metadataProperty);
    }

    public static SniTag getSniTag(String tagText, SniPage page) {
        if (tagText != null && tagText != "") {
            return new SniTagFactory().withTagText(tagText).withSniPage(page).build();
        } else {
            return null;
        }
    }

     /** EL Accessor for creating Company  objects. */
    public static Company getCompany(final SniPage sniPage) {
        return new CompanyFactory()
        .withSniPage(sniPage)
        .build();
    }

    /**
     * <p>Added to accommodate the multiple situations which need to use this logic.</p>
     * <p>Gets a linked asset's image path. In this instance, the imageOverride parameter takes precedence if provided. 
     * Otherwise, if the asset is a photo gallery or video page, the associated image will be returned.</p>
     * <p><code>Null</code> will be returned if no suitable asset is found.</p>
     o @param assetRes The resource of a linked asset.
     * @param imageOverride The image override path
     * @return The path of the linked asset, or null if one was not found and no override was specified.
     */
    public static String getLinkedAssetImagePath(Resource assetRes, String imageOverride) {
        String linkedAssetImagePath = null;
        if (!StringUtils.isEmpty(imageOverride)) {
            linkedAssetImagePath = imageOverride;
        } else if (assetRes != null) {
            SniPage sniPage = PageFactory.getSniPage(assetRes.adaptTo(Page.class));
            PageTypes pageType = PageTypes.findPageType(sniPage.getPageType());
            if (pageType == PageTypes.VIDEO || pageType == PageTypes.VIDEO_CHANNEL || pageType == PageTypes.VIDEO_PLAYER) {
                Video video = null;
                if (pageType == PageTypes.VIDEO_CHANNEL) {
                    video = new ChannelFactory().withSniPage(sniPage).build().getFirstVideo();
                } else if (pageType == PageTypes.VIDEO_PLAYER) {
                    Channel channel = new PlayerFactory().withSniPage(sniPage).build().getFirstChannel();
                    if (channel != null) {
                        video = channel.getFirstVideo();
                    }
                } else {
                    video = new VideoFactory().withSniPage(sniPage).build();
                }
                if (video != null) {
                    linkedAssetImagePath = video.getAlImageUrl();
                    if (StringUtils.isEmpty(linkedAssetImagePath)) {
                        linkedAssetImagePath = video.getLeadImageUrl();
                    }
                    if (StringUtils.isEmpty(linkedAssetImagePath)) {
                        linkedAssetImagePath = video.getMediumImageUrl();
                    }
                    if (StringUtils.isEmpty(linkedAssetImagePath)) {
                        linkedAssetImagePath = video.getTzImageUrl();
                    }
                }
            } else if (pageType == PageTypes.PHOTOGALLERY) {
                PhotoGallery photoGallery = new PhotoGalleryFactory().withSniPage(sniPage).build();
                List<PhotoGallerySlide> photoGallerySlides = photoGallery.getAllSlides();
                if (photoGallerySlides.size() > 0) {
                    PhotoGallerySlide photoGallerySlide = photoGallerySlides.get(0);
                    SniImage sniImage = photoGallerySlide.getSniImage();
                    if (sniImage != null) {
                        linkedAssetImagePath = sniImage.getPath();
                    }
                }
            } else if (pageType == PageTypes.SHOW) {
                Show show = new ShowFactory().withSniPage(sniPage).build();
                SniImage sniImage = show.getFeatureBanner();
                if (sniImage != null) {
                    linkedAssetImagePath = sniImage.getPath();
                }
            }
        }
        return linkedAssetImagePath;
    }

    /**
     * Gets the corresponding icon type based on the provided asset resource.
     * @param assetRes
     * @return
     */
    public static String getIconCssClass(Resource assetRes) {
        String iconCssClass = null;
        if (assetRes != null) {
            SniPage sniPage = PageFactory.getSniPage(assetRes.adaptTo(Page.class));
            PageTypes pageType = null;
            if(sniPage != null)
                pageType = PageTypes.findPageType(sniPage.getPageType());
            if(pageType != null) {
                switch(pageType) {
                    case VIDEO:
                    case VIDEO_CHANNEL:
                    case VIDEO_PLAYER:
                        iconCssClass = "ss-play";
                        break;
                    case PHOTOGALLERY:
                        iconCssClass = "ss-layers";
                        break;
                }
            }
        }
        return iconCssClass;
    }

    /**
     * Gets the calendar object based on a node
     * @param resource
     * @return
     */
    public static com.scrippsnetworks.wcm.calendar.Calendar getCalendar(Resource resource) {
        return new CalendarFactory().withResource(resource).build();
    }

    /**
     * Gets the calendar object based on a node
     * @param currentNode
     * @return
     */
    public static CalendarSlot getCalendarSlot(Node currentNode, ResourceResolver resourceResolver) {
        return new CalendarSlotFactory().withNode(currentNode).withResourceResolver(resourceResolver).build();
    }

    /**
     * Getting all the Recipe/Company/Epsidoe assetIds ,appended by underscore(_)  for esi call to display review summary
     * @param recipePages
     * @return
     */
    public static String getCommunityAssetIds(List<SniPage> recipePages) {
        StringBuilder assestIds =new StringBuilder();
        if (recipePages != null) {
            for (SniPage recipePage : recipePages) {
                ValueMap properties = recipePage.getProperties();
                if (properties != null) {
                    assestIds.append(properties.containsKey("sni:assetUId") ? properties.get("sni:assetUId", String.class)
                                    :"").append("_"); // Every cq page will have assetUId
                }

            }

        }
        return assestIds.toString();
    }

    /**
     * Gets the map entry object based on a resource
     * @param resource
     * @return
     */
    public static MapEntry getMapEntry(Resource resource) {
        return new MapEntryFactory().withResource(resource).build();
    }

    /**
     * Gets the map object based on a resource
     * @param resource
     * @return
     */
    public static MapObj getMapObj(Resource resource) {
        return new MapObjFactory().withResource(resource).build();
    }

    /**
     * Gets value and text from property in select component, that divided by "="
     * @param valueText
     * @return
     */
    public static String[] getValueAndTextFromString(String valueText) {
        Pattern p = Pattern.compile("=");
        String[] tmpResult = p.split(valueText);
        if (tmpResult.length == 2) {
            return tmpResult;
        }
        if (tmpResult.length > 2){
            return Arrays.copyOf(tmpResult, 2);
        }
        if (tmpResult.length == 1){
            String[] result = new String[2];
            result[0] = tmpResult[0];
            result[1] = tmpResult[0];
            return result;
        }

        String[] result = {"", ""};
        return result;
    }

    /** Take the host name that you're on, turn it into the host name for the reviews app. */
    public static String formatCommunityReviewHostUrl(final String hostName) {
        return formatCommunityUrl(hostName, "echoapp");
    }

    /** Take the host name you're on, turn it into host name for reviews metadata app. */
    public static String formatCommunityReviewMetadataHostUrl(final String hostName) {
        return formatCommunityUrl(hostName, "echoint");
    }

    private static String formatCommunityUrl(final String hostName, final String communityHost) {
        if (StringUtils.isNotBlank(hostName) && StringUtils.isNotBlank(communityHost)) {
            String[] nameBits = hostName.split("\\.");
            int nameBitsLength = nameBits.length;
            if (nameBitsLength > 2) {
                return Joiner.on(".").join(communityHost, nameBits[nameBitsLength - 2], nameBits[nameBitsLength -1]);
            }
        }
        return hostName;
    }

	/**
	 * This method is used to split the string based on the delimeter and will
	 * remove the html markup.
	 *
	 * @param input
	 * @param delimeter
	 * @return
	 */
	public static List<String> splitAndRemoveMarkupExceptSimpleTextAndAnchors(
			String input, String delimeter) {
		List<String> textLinksList = null;
		if (input != null && delimeter != null) {
			String[] textLinks = input.split(delimeter);
			textLinksList = new ArrayList<String>();
			if (textLinks != null) {
				for (String text : textLinks) {
					textLinksList
							.add(removeMarkupExceptSimpleTextAndAnchors(text));
				}
			}
		}
		return textLinksList;
	}

    public static String getTalentNameFromBio(final SniPage bioPage) {
        return getTalentNameFromBio(bioPage, 4);
    }
    
    public static boolean pageContainsComponent(final SniPage page, String resourceType) {
        //Assert that the arguments are valid
        if (page == null || page.getContentResource() == null || StringUtils.isEmpty(resourceType)) {
            return false;
        }
        
        //Iteratively loop over all children of this page
        boolean isContained = false;
        String altResourceType = resourceType.startsWith("/apps/") ? resourceType.substring(6) : "/apps/" + resourceType;
        ArrayList<Resource> allChildren = new ArrayList<Resource>();
        int start = 0, end = 1;
        allChildren.add(page.getContentResource());
        //Check to see if any of the newly added resources had any children, or we found the resource type.
        while (end > start && !isContained) {
            //Loop over newly added children, and add their children, unless we find the resource type
            for (int i = start; i < end && !isContained; i++) {
                Iterator<Resource> children = allChildren.get(i).listChildren();
                //Loop over children of the newly added child, adding them, unless the resource type is found.
                while (children.hasNext() && !isContained) {
                    Resource child = children.next();
                    if (child.isResourceType(resourceType) || child.isResourceType(altResourceType)) {
                        isContained = true;
                    } else {
                        allChildren.add(child);
                    }
                }
            }
            start = end;
            end = allChildren.size();
        }
        
        allChildren = null;
        
        return isContained;

    }
    private static String getTalentNameFromBio(final SniPage bioPage, int countDown) {
        if (bioPage == null || bioPage.getPageType() == null || countDown < 0 || countDown > 50) return "";

        if (bioPage.getPageType().equals("talent")) {
            return bioPage.getTitle();
        }

        if (countDown > 0) {
            return getTalentNameFromBio(PageFactory.getSniPage(bioPage.getParent()), --countDown);
        }

        return "";

    }
    
    
    /**
	 * Formats the current date in the yyyyMMdd format and returns back the
	 * result by adding or subtracting the number of days based on the input
	 * parameter.
	 * 
	 * @param days
	 * @return
	 */
	public static String getCalculatedDate(Object days) {
		if (days != null) {
			try {
				int noOfDays = 0;
				if (days instanceof String) {
					noOfDays = Integer.parseInt((String) days);
				} else if (days instanceof Integer) {
					noOfDays = (Integer) days;
				}
				Date currentDate = new Date();
				Calendar cal = Calendar.getInstance();
				cal.setTime(currentDate);
				cal.add(Calendar.DAY_OF_MONTH, noOfDays);
				SimpleDateFormat sdfFormat = new SimpleDateFormat("yyyyMMdd");
				return sdfFormat.format(cal.getTime());
			} catch (Exception e) {
				log.error("Error while parsing the string/date", e);
			}

		}
		return "";
	}

    /**
     * return properties for taboola module by type
     * @param type
     * @return
     */
    public static TaboolaProperties getTaboolaPropertiesByType(String type){
        TaboolaProperties res = null;
        try{
            res = TaboolaProperties.valueOf(type);
        } finally {
            if (res == null){
                return TaboolaProperties.fromAroundTheWeb;
            }
            return res;
        }

    }

    public static String encodeXssForHtml(String text) {
        BundleContext bundleContext = FrameworkUtil.getBundle(Functions.class).getBundleContext();
        ServiceReference serviceReference = bundleContext.getServiceReference(XSSAPI.class.getName());
        XSSAPI xssApi = null;
        if (serviceReference != null) {
            xssApi = (XSSAPI)bundleContext.getService(serviceReference);
        } else{
            return "";
        }

        return xssApi.encodeForHTML(text);

    }

    /**
     * return JSON string of video playlist (channel or single video)
     * @param SniPage of the video (single or channel) 
     *        (be sure to pass default page for the asset, and not the page where module may live)
     * @return JSON string of playlist
     */
    public static String getVideoJSON(final SniPage sniPage){
			String vjson = "";
			if (sniPage == null) return vjson;
			PageTypes pageType = PageTypes.findPageType(sniPage.getPageType());
			if (pageType == PageTypes.VIDEO) {
				Video video = new VideoFactory().withSniPage(sniPage).build();
				if (video != null) vjson = video.getJSONstr();
			} else if (pageType == PageTypes.VIDEO_CHANNEL) {
				Channel channel = new ChannelFactory().withSniPage(sniPage).build();
				if (channel != null) vjson = channel.getJSONstr();
			}
			if (vjson == null) vjson = "";
			return vjson;
    }

}
