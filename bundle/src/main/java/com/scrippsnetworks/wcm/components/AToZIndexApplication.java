package com.scrippsnetworks.wcm.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;

import org.apache.sling.api.resource.Resource;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.AbstractComponent;
import com.scrippsnetworks.wcm.asset.show.AbstractResourceObject;
import com.scrippsnetworks.wcm.fnr.util.PageSlingResourceTypes;
import com.scrippsnetworks.wcm.util.ContentRootPaths;

/**
 * Class to load data for A to Z application
 * 
 * @author mei-yichang
 * 
 */
public class AToZIndexApplication extends AbstractComponent {
	
	private String currentIndex;
	private int currentPageNum;
	public static int MAX_ITEM_PER_PAGE = 150;
	
	private static final String PROPERTY_NUMBER_1 = "1_";
	private static final String PROPERTY_NUMBER_2 = "2_";
	private static final String PROPERTY = "property";
	private static final String TYPE = "type";
	private static final String CQ_PAGE = "cq:Page";
	private static final String SLING_RESOURCE_TYPE = "sling:resourceType";
	private static final String EXCLUDE = "exclude";
	private static final String PATH = "path";
	private static final String GROUP = "group";
	private static final String CHAR_P = "p";
	private static final String OR = "or";
	private static final String LIMIT = "limit";
	private static final String ROOT_PATH = "_path";
	private static final String VALUE = "_value";
	private static final String DOT = ".";
	private static final String ORDER_BY = "orderby";
	private static final String SORT = "sort";
	private static final String JCR_CONTENT = "jcr:content";
	private static final int P_LIMIT = -1;
	private static final String SLASH = "/";
	private static final String CHAR_AT_THE_RATE = "@";
	private static final String SORT_ORDER_ASC = "asc";
	
	public static final String CONTENT_ROOT_FOOD = ContentRootPaths.CONTENT_FOOD.path();
	
	public static final String CONTENT_FOOD_TEST = ContentRootPaths.CONTENT_FOOD_TEST.path();
	
	public static final String CONTENT_ROOT_TALENT = ContentRootPaths.CHEFS
			.path();
	
	public static final String CONTENT_ROOT_HOSTS = ContentRootPaths.HOSTS.path();
	
	public static final String CONTENT_ROOT_GUEST_CHEFS = ContentRootPaths.GUEST_CHEFS.path(); 
	
	public static final String CONTENT_ROOT_SHOW = ContentRootPaths.SHOWS
			.path();
	
	public static final String TALENT_RESOURCE_TYPE = PageSlingResourceTypes.TALENT
			.resourceType();
	public static final String TOPIC_RESOURCE_TYPE = PageSlingResourceTypes.TOPIC
			.resourceType();
	public static final String SHOW_RESOURCE_TYPE = PageSlingResourceTypes.SHOW
			.resourceType();
	public static final String RECIPE_RESOURCE_TYPE = PageSlingResourceTypes.RECIPE
			.resourceType();
	
	public static final String RESTAURANTS_RESOURCE_TYPE = PageSlingResourceTypes.COMPANY
			.resourceType();
	
	@Override
	public void doAction() throws Exception {
		
	}
	
	/* single page A To Z's */
	/**
	 * 
	 * @return List of Pages
	 */
	public List<Page> getShowsAToZ() {
		return collectAToZItem(CONTENT_ROOT_SHOW, SHOW_RESOURCE_TYPE);
	}
	
	/**
	 * 
	 * @return List of Pages
	 */
	public List<Page> getTalentsAToZ() {
		return collectAToZItem(CONTENT_ROOT_TALENT, TALENT_RESOURCE_TYPE);
	}
	
	/**
	 * Returns talents present under speicified folders.
	 * @return
	 */
	public List<Page> getChefsAndHostsAToZ() {
		String[] rootPaths = new String[] { CONTENT_ROOT_TALENT, CONTENT_ROOT_HOSTS };
		String[] excludePaths = new String[] { CONTENT_ROOT_GUEST_CHEFS };
		return searchNodes(rootPaths, excludePaths, TALENT_RESOURCE_TYPE);
	}
	
	/* Paginated A To Z's */
	/**
	 * 
	 * @return SearchResult of Recipes
	 */
	public SearchResult getRecipesAToZ() {
		MAX_ITEM_PER_PAGE = 150;
		initPaginationVar();
		return collectAToZItemWithPagination(RECIPE_RESOURCE_TYPE, null);
	}
	
	/**
	 * 
	 * @return SearchResult of Topics
	 */
	public SearchResult getTopicsAToZ() {
		MAX_ITEM_PER_PAGE = 150;
		initPaginationVar();
		String[] rootPaths = new String[] { CONTENT_ROOT_FOOD };
		String[] excludePaths = new String[] { CONTENT_FOOD_TEST };
		return searchNodesWithPagination(rootPaths, excludePaths, TOPIC_RESOURCE_TYPE);
	}
	
	/**
	 * 
	 * @return SearchResult of Restaurants
	 */
	public SearchResult getRestaurantsAToZ() {
		MAX_ITEM_PER_PAGE = 10;
		initPaginationVar();
		return collectAToZItemWithPagination(RESTAURANTS_RESOURCE_TYPE, null);
	}
	
	/**
	 * Build a list of names for Letter Index Navigation
	 * 
	 * @return
	 */
	public List<String> getNavigationList() {
		List<String> menu = new ArrayList<String>();
		
		menu.add("1 2 3");
		
		for (char i = 'A'; i <= 'W'; i++) {
			menu.add(Character.toString(i));
		}
		
		menu.add("X Y Z");
		
		return menu;
	}
	
	/**
	 * Initialize the variables that are to be used for creating pagination,
	 * data will be collected from the selectors, 1st selector being the letter
	 * index and 2nd selector being the page number starting from 1
	 * 
	 */
	private void initPaginationVar() {
		String[] selectors = getSlingRequest().getRequestPathInfo()
				.getSelectors();
		// default to 'A'
		currentIndex = "A";
		// default to first page
		currentPageNum = 1;
		if (selectors != null) {
			if (selectors.length > 0) {
				currentIndex = selectors[0];
			}
			if (selectors.length > 1) {
				try {
					currentPageNum = Integer.parseInt(selectors[1]);
				} catch (NumberFormatException e) {
				}
			}
		}
	}
	
	/**
	 * Run the SQL2 query to collect the matching pages and return it as
	 * SearchResult
	 * 
	 * @param type
	 * @param queryStr
	 * @return
	 */
	private SearchResult collectAToZItemWithPagination(String type,
			String queryStr) {
		
		String letter = currentIndex;
		
		if (currentIndex.equals("XYZ")) {
			letter = "X";
		}
		
		if (currentIndex.equals("123")) {
			letter = "0";
		}
		
		StringBuffer queryBuffer = new StringBuffer();
		
		queryBuffer
				.append("SELECT * FROM [cq:PageContent] WHERE [sling:resourceType]='")
				.append(type).append("'");
		
		if (queryStr != null && queryStr.length() > 0) {
			queryBuffer.append(" AND ").append(queryStr);
		}
		
		queryBuffer.append(" AND [")
				.append(AbstractResourceObject.PROPERTY_INDEX_LETTER)
				.append("]='").append(letter).append("' ")
				.append(" ORDER BY [")
				.append(AbstractResourceObject.PROPERTY_SORT_TITLE)
				.append("] ");
		
		log.debug("Executing query string {}", queryBuffer);
		
		try {
			Query query = getSession().getWorkspace().getQueryManager()
					.createQuery(queryBuffer.toString(), Query.JCR_SQL2);
			
			return new SearchResult(query.execute().getNodes(), (currentPageNum - 1) * MAX_ITEM_PER_PAGE);
			
		} catch (RepositoryException e2) {
			log.error(e2.getMessage(), e2);
		}
		return null;
	}
	
	
	
	
	/**
	 * Run the Xpath query to collect matching pages and return it as a List of
	 * Pages
	 * 
	 * @param root
	 * @param type
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private List<Page> collectAToZItem(String root, String type) {
		
		List<Page> pages = new ArrayList<Page>();
		StringBuffer queryBuffer = new StringBuffer();
		
		queryBuffer.append("/jcr:root").append(root)
		.append("//element(*, cq:PageContent)[@sling:resourceType=")
		.append('\'').append(type).append('\'').append(']')
		.append("order by @sni:sortTitle ascending");
		
		try {
			
			for (Iterator<Resource> iter = getSlingRequest()
					.getResourceResolver().findResources(
							queryBuffer.toString(), Query.XPATH); iter
					.hasNext();) {
				pages.add(getPageManager().getContainingPage(iter.next()));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return pages;
	}
	

	/**
	 * This method uses Querybuilder to search the JCR nodes and get the
	 * corresponding results based on the input parameters.
	 * 
	 * @param paths
	 * @param excludePaths
	 * @param type
	 * @return
	 */
	public List<Page> searchNodes(String[] paths, String[] excludePaths, String type) {
		List<Page> pageList = null;
		if(getSlingScriptHelper() != null && getSlingRequest() != null) {
			QueryBuilder builder = getSlingScriptHelper().getService(QueryBuilder.class);
			Session session = getSlingRequest().getResourceResolver().adaptTo(Session.class);
			
			Map<String, Object> map = new HashMap<String, Object>();
	
			map.put(GROUP + DOT + CHAR_P + DOT +  OR, "true");
			int i = 1;
			for (String path : paths) {
				map.put(GROUP + DOT + i + ROOT_PATH, path);
				i++;
			}
			map.put(TYPE, CQ_PAGE);
			map.put(PROPERTY, JCR_CONTENT + SLASH + SLING_RESOURCE_TYPE);
			map.put(PROPERTY + DOT, JCR_CONTENT + SLASH + SLING_RESOURCE_TYPE);
			map.put(PROPERTY + DOT + VALUE, type);
			map.put(ORDER_BY, CHAR_AT_THE_RATE + JCR_CONTENT + SLASH + AbstractResourceObject.PROPERTY_SORT_TITLE);
			map.put(ORDER_BY + SORT, SORT_ORDER_ASC);
			map.put(CHAR_P + DOT + LIMIT, P_LIMIT);
			
			//Excluding the nodes
			if(excludePaths != null) {
				log.debug("searchNodes - Addding exclusions");
				i = 1;
				for(String excludePath : excludePaths) {
					map.put(EXCLUDE + DOT + PATH, excludePath);
					i++;
				}
			}
			
			com.day.cq.search.Query query = builder.createQuery(PredicateGroup.create(map), session);
			query.setHitsPerPage(0);
			
			com.day.cq.search.result.SearchResult searchResult = query.getResult();
			
			log.debug("searchNodes - Total search results count is :: " + searchResult.getTotalMatches());
			
			if(searchResult.getTotalMatches() > 0) {
				pageList = new ArrayList<Page>();
				for (Hit hit : searchResult.getHits()) {
					try {
						pageList.add(getPageManager().getContainingPage(hit.getPath()));
					} catch (RepositoryException e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		}
		return pageList;
	}
	
	/**
	 * This method uses Querybuilder to search the JCR nodes and get the
	 * corresponding paginated results.
	 * 
	 * @param paths
	 * @param excludePaths
	 * @param type
	 * @return
	 */
	public SearchResult searchNodesWithPagination(String[] paths, String[] excludePaths, String type) {
		String firstLetter = currentIndex;
		
		/*
		 * The first letter for X,Y, Z, numeric or special characters is
		 * designed as below. The first letter for the page names which start
		 * with X, Y or Z will be 'X' and for the page names starts with numeric
		 * or special characters, it will be '0'
		 */
		if (currentIndex.equals("XYZ")) {
			firstLetter = "X";
		}
		
		if (currentIndex.equals("123")) {
			firstLetter = "0";
		}
		
		if(getSlingScriptHelper() != null && getSlingRequest() != null) {
			QueryBuilder builder = getSlingScriptHelper().getService(QueryBuilder.class);
			Session session = getSlingRequest().getResourceResolver().adaptTo(Session.class);
			
			Map<String, Object> map = new HashMap<String, Object>();
	
			map.put(GROUP + DOT + CHAR_P + DOT +  OR, "true");
			int i = 1;
			for (String path : paths) {
				map.put(GROUP + DOT + i + ROOT_PATH, path);
				i++;
			}
			map.put(TYPE, CQ_PAGE);
			map.put(PROPERTY_NUMBER_1 + PROPERTY, JCR_CONTENT + SLASH + SLING_RESOURCE_TYPE);
			map.put(PROPERTY_NUMBER_1 + PROPERTY + DOT, JCR_CONTENT + SLASH + SLING_RESOURCE_TYPE);
			map.put(PROPERTY_NUMBER_1 + PROPERTY + DOT + VALUE, type);
			
			map.put(PROPERTY_NUMBER_2 + PROPERTY, JCR_CONTENT + SLASH + AbstractResourceObject.PROPERTY_INDEX_LETTER);
			map.put(PROPERTY_NUMBER_2 + PROPERTY + DOT + VALUE, firstLetter);
			
			map.put(ORDER_BY, CHAR_AT_THE_RATE + JCR_CONTENT + SLASH + AbstractResourceObject.PROPERTY_SORT_TITLE);
			map.put(ORDER_BY + SORT, SORT_ORDER_ASC);
			
			//Excluding the nodes that should not be included in the results, such as test pages
			if(excludePaths != null) {
				log.debug("searchNodes - Addding exclusions");
				i = 1;
				for(String excludePath : excludePaths) {
					map.put(EXCLUDE + DOT + PATH, excludePath);
					i++;
				}
			}
			
			com.day.cq.search.Query query = builder.createQuery(PredicateGroup.create(map), session);
			query.setStart((currentPageNum - 1) * MAX_ITEM_PER_PAGE);
		    query.setHitsPerPage(MAX_ITEM_PER_PAGE);
			
			com.day.cq.search.result.SearchResult searchResult = query.getResult();
			
			log.debug("searchNodes - Total search results count is :: " + searchResult.getTotalMatches());
			return new SearchResult(searchResult);
		}
		return null;
	}
	
	public class SearchResult {
		private long totalMatch;
		private long size;
		private List<Page> items;
		
		public SearchResult(NodeIterator iter, int offset) {
			if (iter != null) {
				this.totalMatch = iter.getSize();
			}
			items = new ArrayList<Page>();
			if (this.totalMatch > offset) {
				iter.skip(offset);
				
				while (iter.hasNext() && items.size() < MAX_ITEM_PER_PAGE) {
					
					try {
						items.add(getPageManager().getContainingPage(
								iter.nextNode().getPath()));
					} catch (RepositoryException e) {
						log.error(e.getMessage(), e);
					}
				}
				size = items.size();
			}
		}
		
		public SearchResult(com.day.cq.search.result.SearchResult searchResult) {
			if(searchResult.getTotalMatches() > 0) {
				this.totalMatch = searchResult.getTotalMatches();
				items = new ArrayList<Page>();
				for (Hit hit : searchResult.getHits()) {
					try {
						items.add(getPageManager().getContainingPage(hit.getPath()));
					} catch (RepositoryException e) {
						log.error(e.getMessage(), e);
					}
				}
				size = items.size();
			}
		}
		
		public long getTotalMatch() {
			return totalMatch;
		}
		
		public long getSize() {
			return size;
		}
		
		public List<Page> getItems() {
			return items;
		}
	}
	
	public String getCurrentIndex() {
		return currentIndex;
	}
	
	public int getCurrentPageNum() {
		return currentPageNum;
	}
}
