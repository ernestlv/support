package com.scrippsnetworks.wcm.socialtoolbar.impl;

import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.episode.Episode;
import com.scrippsnetworks.wcm.episode.EpisodeFactory;
import com.scrippsnetworks.wcm.fnr.util.PageSlingResourceTypes;
import com.scrippsnetworks.wcm.opengraph.OpenGraph;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.socialtoolbar.SocialToolbar;
import com.scrippsnetworks.wcm.socialtoolbar.TwitterMessage;
import com.scrippsnetworks.wcm.socialtoolbar.TwitterMessageFactory;
import com.scrippsnetworks.wcm.util.StringUtil;

/**
 * 
 * @author Mallik Vamaraju Date: 09/04/13
 */

public class SocialToolbarImpl implements SocialToolbar {
	
	private static final Logger log = LoggerFactory
			.getLogger(SocialToolbar.class);
	
	/** ValueMap of properties merged from page and asset. */
	private ValueMap socialpageProperties;
	
	/** SniPage used to create this object. */
	private SniPage sniPage;
	
	private static final String SNI_TWITTER_MESSAGE = "sni:twitterMessage";
	
	private static final String SNI_PINTEREST_MESSAGE = "sni:pinterestMessage";
	
	private static final int MAXIMUM_TWEET_LENGTH = 117;
	
	private String twitterText;
	
	private String pintrestText;
	
	private String twitterHandle;
	
	private String displaySiteName;
	
	private String ogTitle;
	
	private String ogDesc;
	
	private String ogImg;
	
	private String ogType;
	
	private OpenGraph openGraph;
	
	/** Construct a new SocialToolbar given an SniPage. */
	public SocialToolbarImpl(SniPage sniPage) {
		this.sniPage = sniPage;
		this.socialpageProperties = sniPage.getProperties();
		openGraph = sniPage.getOpenGraph();
	}
	
	/**
	 * return override in the page properties and the default message if the
	 * override isn't exist
	 **/
	
	/** {@inheritDoc} */
	@Override
	public String getTwitterText() {
		
		if (twitterText == null) {
			if (socialpageProperties != null
					&& socialpageProperties.containsKey(SNI_TWITTER_MESSAGE)) {
				twitterText = socialpageProperties.get(SNI_TWITTER_MESSAGE,
						String.class);
				
			} else {
				
				twitterText = getPageText();
			}
		}

        twitterText = StringUtil.cleanText(twitterText, false, true);
		
		return twitterText;
	}
	
	public String getPageText() {
		
		if (twitterText == null) {
			
			String resourceType = sniPage.getContentResource()
					.getResourceType();
			StringBuilder defaultText = new StringBuilder();
			boolean isPageFound = false;
			String pageTitle = sniPage.getTitle();
			String viaHandler = getTwitterHandle();
			
			for (PageSlingResourceTypes type : PageSlingResourceTypes.values()) {
				if (resourceType.equals(type.resourceType())) {
					isPageFound = true;
					switch (type) {
					
						case VIDEO:
							defaultText = defaultText.append("Watch ")
									.append(pageTitle).append(viaHandler);
							
							break;
						
						case VIDEO_CHANNEL:
							defaultText = defaultText.append("Watch ")
									.append(pageTitle).append(" videos ")
									.append(viaHandler);
							
							break;
						
						case VIDEO_PLAYER:
							defaultText = defaultText.append("Watch ")
									.append(pageTitle).append(" videos ")
									.append(viaHandler);
							
							break;
						
						case TOPIC:
							defaultText = defaultText.append("Get ")
									.append(pageTitle).append(" recipes")
									.append(viaHandler);
							
							break;
						
						case UNIVERSAL_LANDING:
							SniPage packageAnchor = sniPage.getPackageAnchor();
							String packageName = packageAnchor != null ? packageAnchor
									.getTitle() : pageTitle;
							defaultText = defaultText.append("Get ")
									.append(packageName)
									.append(" recipes, videos and tips")
									.append(viaHandler);
							break;
						
						case SHOW:
							defaultText = defaultText.append(pageTitle).append(
									viaHandler);
							
							break;
						
						case TALENT:
							defaultText = defaultText.append("Get to know ")
									.append(pageTitle).append(viaHandler);
							
							break;
						
						case RECIPE:
							defaultText = defaultText
									.append("Get the recipe for ")
									.append(pageTitle).append(viaHandler);
							break;
						
						case PHOTO_GALLERY:
							// socialMessage = getSocialMessage(scService,
							// social);
							break;
						
						case MENU:
							defaultText = defaultText.append("Get the ")
									.append(pageTitle).append(viaHandler);
							break;
						
						case COMPANY:
							defaultText = defaultText
									.append("Find out more about ")
									.append(pageTitle).append(viaHandler)
									.append("#OnTheRoad ");
							break;
						
						case EPISODE:
							Episode episode = new EpisodeFactory().withSniPage(
									sniPage).build();
							if (episode != null) {
								SniPage showPage = episode.getRelatedShowPage();
								if (showPage != null) {
									String showName = showPage.getTitle();
									defaultText = defaultText
											.append("Get more on ")
											.append(showName).append(" - ")
											.append(pageTitle)
											.append(viaHandler);
								}
							}
							break;
						
						case ARTICLE_SIMPLE:
							defaultText = defaultText.append("Read ")
									.append(pageTitle).append(viaHandler);
							break;
					}
					break;
				}
				
			}
			if (!isPageFound) {
				defaultText = defaultText.append("Check out ")
						.append(pageTitle).append(viaHandler);
			}
			twitterText = getGenericTweetText(defaultText);
			
		}
		
		return twitterText;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getPintrestText() {
		if (pintrestText == null) {
			if (socialpageProperties != null
					&& socialpageProperties.containsKey(SNI_PINTEREST_MESSAGE)) {
				pintrestText = socialpageProperties.get(SNI_PINTEREST_MESSAGE,
						String.class);
				
			} else {
				String ogTitle = OpenGraph.JCR_PROPERTY_OG_TITLE;
				
				if (socialpageProperties != null
						&& socialpageProperties.containsKey(ogTitle)) {
					pintrestText = socialpageProperties.get(ogTitle,
							String.class);
				} else {
					pintrestText = sniPage.getSeoTitle() + " - "
							+ getSiteDisplayName();
				}
				
			}
		}
		
		return StringUtil.cleanText(pintrestText, false, true);
	}
	
	/** {@inheritDoc} */
	@Override
	public String getTwitterHandle() {
		if (twitterHandle == null) {
			TwitterMessage message = new TwitterMessageFactory().withSniPage(
					sniPage).build();
			twitterHandle = " via "+message.getTwitterHandle()+ " ";
		}
		return twitterHandle;
		
	}
	
	/** {@inheritDoc} */
	@Override
	public String getSiteDisplayName() {
		
		if (displaySiteName == null) {
			TwitterMessage message = new TwitterMessageFactory().withSniPage(
					sniPage).build();
			displaySiteName = message.getSiteDisplayName();
			
		}
		return displaySiteName;
		
	}
	
	/** {@inheritDoc} */
	@Override
	public String getOpenGraphTitle()
	
	{
		if (openGraph != null) {
			ogTitle = openGraph.getOGTitle();
			
		}
		return ogTitle;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getOpenGraphDesc()
	
	{
		if (openGraph != null) {
			ogDesc = openGraph.getOGDescription();
			
		}
		return ogDesc;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getOpenGraphImg()
	{
		if (openGraph != null) {
			ogImg = openGraph.getOGImg();
			
		}
		return ogImg;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getOpenGraphType()
	{
		if (openGraph != null) {
			ogType = openGraph.getOGType();
			
		}
		return ogType;
	}
	
	/**
	 * return generic text, if the Tweet message exceed more than 117
	 * chars(excluding the URL)
	 */
	public String getGenericTweetText(StringBuilder defaultText) 
	{
		if (defaultText.length() >= MAXIMUM_TWEET_LENGTH) {
			defaultText = new StringBuilder().append("Check this out").append(
					getTwitterHandle());
		}
		return defaultText.toString();
	}
}