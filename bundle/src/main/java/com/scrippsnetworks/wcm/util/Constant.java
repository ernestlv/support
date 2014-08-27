package com.scrippsnetworks.wcm.util;

/**
 * Provides constant variables used by various classes.
 * @author Pawan Gupta
 * Date: 6/29/12
 * 
 */

public class Constant {
	
	/* Static variables */
	public static final String JCR_TITLE="jcr:title";
	public static final String JCR_DESCRIPTION="jcr:description";
	public static final String SNI_RECIPES="sni:recipes";
	public static final String EP_TITLE="ep_title";
	public static final String EP_DESCRIPTION="ep_description";
	public static final String EP_RECEIPES="ep_recipes";
	public static final String SH_TITLE="show_title";
	public static final String CH_TITLE="chef_title";
	public static final String RECIPE_TITLE="recipe_title";
	public static final String SH_URL="show_url";
	public static final String CH_URL="chef_url";
	public static final String EP_URL="episode_url";
	public static final String CH_IMAGE="image_url";
	public static final String PG_TITLE="pg_title";
	public static final String SLASH="/";
	public static final String WEEK_DAY="week_day";
	public static final String URL="url";
	public static final String TIME="time";
	public static final String OPEN_SUB=" <sub>";
	public static final String CLOSE_SUB="</sub>";
	public static final String schedulePathPrefix = "/etc/sni-asset/schedules/cook/";
	public static final String RECIPE_MOBILE_CONTENT="/content/cook-mobile/recipes";
	public static final String RECIPE_CONTENT="/content/cook/recipes";
	public static final String SHOW_ASSET_PATH="/etc/sni-asset/shows";
	public static final String SHOW_CONTENT_PATH="/content/cook/shows";
	public static final String SHOW_MOBILE_CONTENT_PATH="/content/cook-mobile/shows";
	public static final String CHEF_MOBILE_CONTENT_PATH="/content/cook-mobile/chefs";
	public static final String CHEF_CONTENT_PATH="/content/cook/chefs";
	public static final String HTML=".html";
	public static final String PROPERTY="property";
	public static final String TYPE="type";
	public static final String PAGE_CONTENT="cq:PageContent";
	public static final String CQ_PAGE="cq:Page";
	public static final String ASSET_LINK="sni:assetLink";
	public static final String SLING_RESOURCE_TYPE = "sling:resourceType";
	public static final String PEOPLE_LINK="sni:people";
	public static final String IMAGE_LINK="sni:image";
	public static final String SHOW_PAGE_TYPE="show";
	public static final String CHEF_PAGE_TYPE="chef";
	public static final String EXCLUDE = "exclude";
	public static final String PATH="path";
	public static final String GROUP = "group";
	public static final String CHAR_P = "p";
	public static final String OR = "or";
	public static final String LIMIT = "limit";
	public static final String ROOT_PATH = "_path";
	public static final String VALUE="_value";
	public static final String DOT=".";
	public static final String ORDER_BY = "orderby";
	public static final String SORT = "sort";
	public static final String SORT_ORDER_ASC = "asc";
	public static final String SORT_ORDER_DESC = "desc";
	public static final String CHAR_AT_THE_RATE = "@";
	public static final String JCR_CONTENT="jcr:content";
	public static final String SNI_EPISODE="sni:episode";
	public static final String TRUE="true";
	public static final String FALSE="false";
	public static final String AM_PM="am_pm";
	public static final String COLON=" : ";
	public static final String HOUR=" hr ";
	public static final String MINUTE=" min ";
	public static final String LINK="link";
	public static final String PARENT="parent";
	public static final String LEVEL="level";
	public static final String ATTRIBUTE="attr";
	public static final String JCR_DATA="jcr:data";
	public static final String SNI_PROP_PREP_TIME="sni:preparationTime";
	public static final String SNI_PROP_COOK_TIME="sni:cookTime";
	public static final String SNI_PROP_INACTIVE_PREP_TIME="sni:activePreparationTime";
	public static final String SNI_PROP_DIFFICULTY="sni:difficulty";
	public static final String SNI_PROP_IMAGES="sni:images";
	public static final String SNI_PROP_BRAND="sni:brand";
	public static final String SNI_PROP_PRIMARY_TALENT="sni:primaryTalent";
	public static final String BRAND_NAME="Cooking Channel";
	public static final String PROP_DIFFICULTY="difficulty";
	public static final String TITLE="title";
	public static final String DESCRIPTION="description";
	public static final String SECTION="section";
	public static final String PICTURES="Pictures";
	public static final String PRIMARYTALENT="sni:primaryTalent";
	public static final String PAGELINKS="sni:pageLinks";
	public static final String SNI_ABSTRACT="sni:abstract";
	public static final String SNI_RUNTIME= "sni:totalRunTime";
	public static final String SNI_SHOW= "sni:show";
	public static final String SNI_SORT_TITLE = "sni:sortTitle";
	
	public static final String SNI_VIDEOS = "sni:videos";
	
	/* Assigned "-1" to retrieve all results. */
	public static final int P_LIMIT = -1;
	
	public static final String []time={"6:30am","7am","7:30am","8am","8:30am","9am","9:30am","10am","10:30am","11am","11:30am","12pm","12:30pm","1pm","1:30pm","2pm","2:30pm","3pm","3:30pm","4pm","4:30pm","5pm","5:30pm","6pm","6:30pm","7pm","7:30pm","8pm","8:30pm","9pm","9:30pm","10pm","10:30pm","11pm","11:30pm","12am","12:30am","1am","1:30am","2am","2:30am","3am","3:30am","4am","4:30am","5am","5:30am","6am"};
	public static final String []onTvTime={"6:30 AM","7:00 AM","7:30 AM","8:00 AM","8:30 AM","9:00 AM","9:30 AM","10:00 AM","10:30 AM","11:00 AM","11:30 AM","12:00 PM","12:30 PM","1:00 PM","1:30 PM","2:00 PM","2:30 PM","3:00 PM","3:30 PM","4:00 PM","4:30 PM","5:00 PM","5:30 PM","6:00 PM","6:30 PM","7:00 PM","7:30 PM","8:00 PM","8:30 PM","9:00 PM","9:30 PM","10:00 PM","10:30 PM","11:00 PM","11:30 PM","12:00 AM","12:30 AM","1:00 AM","1:30 AM","2:00 AM","2:30 AM","3:00 AM","3:30 AM","4:00 AM","4:30 AM","5:00 AM","5:30 AM","6:00 AM"};
	
	//Video Grid Constants
	public static final String VIDEO_GRID_RELATEDVIDEOS_URL_PATH="/services/food/relatedVideos";
	public static final int VIDEO_GRID_MAX_RESULTS=12;
	public static final int VIDEO_GRID_OFFSET=0;
	

}
