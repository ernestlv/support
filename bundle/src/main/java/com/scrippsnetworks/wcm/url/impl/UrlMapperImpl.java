package com.scrippsnetworks.wcm.url.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentException;
import org.osgi.framework.Constants;

import org.apache.felix.scr.annotations.*;

import java.lang.String;
import java.lang.StringBuilder;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.regex.*;
import java.net.URI;
import java.net.URISyntaxException;
import javax.jcr.query.Query;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.QuerySyntaxException;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.settings.SlingSettingsService;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.api.SlingException;
import org.apache.jackrabbit.util.ISO9075;

import javax.jcr.Session;
import javax.jcr.RepositoryException;
import javax.jcr.observation.ObservationManager;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import com.scrippsnetworks.wcm.url.UrlMapper;

/**
 * Implements the UrlMapper, which provides bidirectional mapping of resource
 * paths to url paths, as an OSGI service.
 * 
 * <p>
 * The mapping and resolving behavior in this class is intended to support
 * request forwarding and output rewriting functions. While there are resolve()
 * methods to mirror the ResourceResolver, and they attempt to always return a
 * path, there is no implication the returned path exists. The resolvePath()
 * methods should be considered the primary resolve methods, returning null if
 * no bucketing-specific replacement needs to be done, with the resolve()
 * methods merely a convenience. The resolvePath() methods are warranted for
 * their intended function, supporting internal forwarding from a servlet
 * filter. No such warrant is provided for the resolve() methods, as they have
 * no specific use case.
 * </p>
 * 
 * <p>
 * As much as possible any resource resolution that needs to be done is done on
 * path prefixes just below the site level and cached so the ResourceResolver
 * doesn't have to be used. Before bucketing is applied, standard resource
 * resolution is used to determine if the URL path is useable as-is; if it is
 * not, it is assumed that the bucketing rules must be applied. No actual
 * resource resolution is done on the path; in the supported use case (the
 * forwarding servlet filter) it is up to Sling to figure out what to do if the
 * resource doesn't exist.
 * </p>
 * 
 * @author Scott Johnson, Tyrone Tse
 */
@Component(label = "SNI WCM Url Mapper", description = "Provides bidirectional mapping of resource paths and URL paths.", enabled = true, immediate = true, metatype = false)
@Service(value = UrlMapper.class)
public class UrlMapperImpl implements UrlMapper {

	/**
	 * Event listener command object to flush cached mapped paths on changes to
	 * /etc/map.
	 */
	public class MapChangeListener implements EventListener {
		private UrlMapperImpl urlMapper;

		public MapChangeListener(UrlMapperImpl urlMapper) {
			this.urlMapper = urlMapper;
		}

		public void onEvent(EventIterator it) {
			if (urlMapper != null) {
				urlMapper.clearRootPaths();
			}
		}
	}

	/**
	 * Event listener command object to flush cached mapped paths on changes
	 * under content.
	 */
	public class ContentChangeListener implements EventListener {
		private UrlMapperImpl urlMapper;

		public ContentChangeListener(UrlMapperImpl urlMapper) {
			this.urlMapper = urlMapper;
		}

		public void onEvent(EventIterator it) {
			while (it.hasNext()) {
				Event event = it.nextEvent();
				String path;
				try {
					path = event.getPath();
				} catch (RepositoryException e) {
					logger.warn(
							"onEvent caught RepositoryException getting path from event",
							e);
					continue;
				}

				if (path.contains("/jcr:content")) {
					return;
				}

				logger.debug("ContentChangeListener got event for {}", path);

				String urlPath = urlMapper.map(path);			
				logger.debug("ContentChangeListener mapped {} to {}", path,
						urlPath);
				if (urlPath != null) {
					if (resolvedPaths.contains(urlPath)) {
						logger.debug(
								"removing cached resolved path {} given event for {}",
								urlPath, path);
						resolvedPaths.remove(path);
					}
				}
			}
		}
	}

	protected static Logger logger = LoggerFactory
			.getLogger(UrlMapperImpl.class);

	/*
	 * Some people, when confronted with a problem, think
	 * "I know, I'll use regular expressions." Now they have two problems. --
	 * Jamie Zawinski
	 */

	/**
	 * Regex pattern matching bucketed recipe paths, e.g.
	 * /recipes/chef-name/a/ab/abc/abcd.
	 */
	/* Used in CCTV
	 * 
	 * public static final String fourLevelBuckets = "(?<=/recipes/[^/]{1,255})/[^/]/[^/]{2}/[^/]{3}/[^/]{4}";
	 */

	/**
	 * Regex pattern matching bucketed video paths, e.g. /videos, e.g. /videos
	 * /YYYY/MM/DD/0/a-video-page
	 */
	
	/* Used in CCTV
	 * 
	 * public static final String dateBuckets = "(?<=/videos)/[0-9]{4}/[0-9]{1,2}/[0-9]{1,2}(?:/[0-9]+)?";
	 */

	/**
	 * Regex pattern matching bucketed show paths, e.g.
	 * /shows/a/a-/a-s/a-sh/a-show-name.
	 */
	/* Used in CCTV
	 * 
	 * public static final String singleLetterBuckets = "(?<=/shows|/topics)/[^/]";
	 */

	/*  Used in CCTV
	 * 
	 *  public static final String typeBuckets =
	 * "(?<=/[^/]+)/(?:photo-galleries|articles|packages|channels|players)/(?:[a-zA-Z0-9]|numbers)";
	 * 
	 */
	
	/** Regex patterh matching buckets for specific page types */
	
	/* Used in CCTV
	 * 
	 * public static final String typeBuckets = "/(?:photo-galleries|articles|packages|channels|players)/(?:[a-zA-Z0-9]|numbers)";
	 */

	/**
	 * Format string for searching the several content-type-specific buckets,
	 * and one additional type-specific bucket whose name is supplied when the
	 * format string is expanded.
	 */
	/* Used in CCTV
	 * 
	 * public static final String typeSearchPaths = "/element(*, cq:Page)[fn:name() = '%s' or fn:name() = 'numbers' or fn:name() = 'photo-galleries' or fn:name() = 'articles' or fn:name() = 'packages' or fn:name() = 'channels' or fn:name() = 'players']";
	 */

	/**
	 * Handy way to indicate a null value (one we shouldn't return) in the
	 * resolvedPaths hash, which can't take nulls.
	 */
	private static final String NULLVALUE = "\u0000";

	/** The pattern used to match mappable paths. */
	
/*	Used in CCTV
 * 
 * 	public static final Pattern bucketedPathPattern = Pattern
			.compile("^(/cook|/cook-mobile|/content/[^/]+)?(?!/cook|/content/[^/]+)"
					+ // captured context -- group #1
					"("
					+ "/recipes/(?!photo-galleries|articles|packages|channels|players)[^/]+(?=/)"
					+ "|"
					+ "/recipes(?=articles|photo-galleries|packages|channels|players)"
					+ "|"
					+ "/(?!/recipes)[^/]+(?=/)"
					+ ")"
					+ // captured section (or section+chef for recipes) -- group
						// #2
					"(?![^/]\\.(?:html|xml|json)|$)"
					+ "(?:"
					+ // recipes with bucketing
					fourLevelBuckets
					+ "|"
					+ // shows and topics with bucketing
					singleLetterBuckets
					+ "|"
					+ // videos with bucketing
					dateBuckets
					+ // bucketing pattern for videos
					"|"
					+ // page type buckets
					typeBuckets
					+ // bucketing pattern for types
					")"
					+ "(?!/articles|/photo-galleries|/packages|/channels|/players)"
					+ // we don't support this, so just prevent us from handling
					"(/.{1,})"); // capture everything past bucket -- group #3
*/

	/** The pattern used to match resolveable paths. */
/*	Used in CCTV
 * 
	public static final Pattern requestPathPattern = Pattern
			.compile("^(/cook|/cook-mobile|/content/[^/]+)?(?!/cook|/content/[^/]+)"
					+ // captured context path -- group #1
					"("
					+ // Request paths under recipes have two path components
						// for section. (e.g., /recipes/chef-name).
					"/recipes/(?!photo-galleries|articles|packages|channels|players)[^/]+(?=/)"
					+ // Slurp up any path component after /recipes except type
						// buckets
					"|"
					+ // For request paths directly under /recipes, the section
						// is /recipes
					"/recipes(?=/[^/]+$)"
					+ // For request paths directly under /recipes, the section
						// is /recipes
					"|"
					+ // Any other path, first component after context is
						// section.
					"(?!/recipes)/[^/.]+"
					+ ")"
					+ // captured section -- group #2 (recipes chef segment will
						// have >=2 chars)
					"(?!/jcr:content|/_jcr_content)"
					+ // don't match section's content resource
					// "(?!/[^/.][/.][^/]+$)" + // don't handle requests for
					// one-letter node names
					"(?!"
					+ // don't match if the path is bucketed
					"(?:"
					+ // recipes with bucketing
					fourLevelBuckets
					+ "|"
					+ // shows with bucketing
					singleLetterBuckets
					+ "|"
					+ // or videos with bucketing
					dateBuckets
					+ "|"
					+ // page type buckets
					typeBuckets
					+ // bucketing pattern for videos
					")(?=/)"
					+ ")"
					+ //
					"(?!/articles|/photo-galleries|/packages|/channels|/players)"
					+ // we don't support this, so just prevent us from handling
					// "(/.*)?$"); // captured item in section -- group #3
					"(/.{1,})"); // captured item in section -- group #3
*/
	
	/** The pattern used to find the node name to use for a query */
/*	Used in CCTV
 * 
	public static final Pattern nodeNamePattern = Pattern.compile("/"
			+ "([^/.]+)" + // nodename
			"(.*)?$"); // suffix
*/
	/**
	 * The pattern used for replacing single-number buckets in video paths with
	 * sortable padded numbers.
	 */
/*	Used in CCTV
 * 
	public static final Pattern singleNumberBucketPattern = Pattern
			.compile("(?<=[0-9]+)/([0-9])/");
*/
	/** Known environments, for run mode matching. */
	private static final String[] ENVIRONMENTS = { "qa", "dev", "devint",
			"stage", "prod" };

	/** Resolver map location. */
	private static final String RESOLVER_MAP_LOCATION = "/etc/map";

	@Reference
	private ResourceResolverFactory resourceResolverFactory;

	@Reference
	SlingRepository slingRepository;

	@Reference
	SlingSettingsService slingSettingsService;

	/**
	 * Observation manager used to register an event listener to catch changes
	 * to /etc/map and flush the cache of mapped paths.
	 */
	protected ObservationManager observationManager;

	/** Session used in registering event listener. */
	private Session session;

	/**
	 * Cache of section-level paths already resolved using the
	 * ResourceResolver's map.
	 */
	private ConcurrentHashMap<String, String> rootPaths = new ConcurrentHashMap<String, String>();

	/** Cache of resolved urls. */
	private ConcurrentHashMap<String, String> resolvedPaths = new ConcurrentHashMap<String, String>();

	/**
	 * Event listener command object for flushing the cache of section-level
	 * paths on map change.
	 */
	private MapChangeListener mapChangeListener;

	private String[] contentChangePaths = { "/content/cook",
			"/content/cook-mobile" };

	/**
	 * Event listener command object for flushing the cache of resolved paths on
	 * node move/delete.
	 */
	private ContentChangeListener[] contentChangeListeners = new ContentChangeListener[contentChangePaths.length];

	/** Indicates errors during component configuration. */
	private static class UrlMapperConfigException extends Exception {
		public UrlMapperConfigException(String msg, Throwable cause) {
			super(msg, cause);
		}
	};

	/**
	 * Activates this OSGi component, setting its properties from the
	 * ComponentContext and initializing its state.
	 */
	@Activate
	protected void activate(ComponentContext ctx) {
		Dictionary props = ctx.getProperties();
		try {
			internalActivate(props);
		} catch (UrlMapperConfigException e) {
			String pid = (String) props.get(Constants.SERVICE_PID);
			ctx.disableComponent(pid);
			logger.error("disabling {} due to error during activation", pid);
			// rethrowing since there's no other way to signal failure of this
			// method;
			throw new ComponentException(
					"disabling search service due to activation failure", e);
		}
	}

	/**
	 * Activation method that doesn't rely on OSGi ComponentContext for
	 * parameters, instead allowing them to be passed in directly.
	 * 
	 * @param props
	 *            Dictionary of properties to set.
	 */
	protected void internalActivate(Dictionary props)
			throws UrlMapperConfigException {
		logger.debug("activate");
		this.mapChangeListener = new MapChangeListener(this);

		Set<String> runModes = slingSettingsService.getRunModes();

		StringBuilder sbMapLocation = new StringBuilder(RESOLVER_MAP_LOCATION);
		if (runModes.contains("publish")) {
			for (String env : ENVIRONMENTS) {
				if (runModes.contains(env)) {
					sbMapLocation.append(".publish.").append(env);
				}
			}
		}
		String mapLocation = sbMapLocation.toString();

		try {
			session = slingRepository.loginAdministrative(null);
			observationManager = session.getWorkspace().getObservationManager();
			observationManager.addEventListener(this.mapChangeListener,
					Event.PROPERTY_ADDED | Event.PROPERTY_REMOVED
							| Event.PROPERTY_CHANGED | Event.NODE_MOVED
							| Event.NODE_ADDED | Event.NODE_REMOVED,
					mapLocation, // path to watch
					true, // isDeep, want events for subgraph of node
					null, // uuid, don't set
					null, // nodeTypeNames
					true); // noLocal, don't get events from this session

			int i = 0;
			for (String path : contentChangePaths) {
				this.contentChangeListeners[i] = new ContentChangeListener(this);

				observationManager.addEventListener(
						this.contentChangeListeners[i++], Event.NODE_MOVED
								| Event.NODE_REMOVED, path, // path to watch
						true, // isDeep, want events for subgraph of node
						null, // uuid, don't set
						new String[] { "cq:Page" }, // nodeTypeNames
						true); // noLocal, don't get events from this session
			}
		} catch (RepositoryException e) {
			logger.error("error activating: {}");
			throw new UrlMapperConfigException("error during activation", e);
		}
	}

	/**
	 * Deactivates this OSGi component, cleaning up any state.
	 */
	@Deactivate
	protected void deactivate(ComponentContext ctx) {
		internalDeactivate();
	}

	/**
	 * Deactivation method that doesn't rely on OSGi ComponentContext
	 */
	protected void internalDeactivate() {
		logger.debug("deactivate");
		try {
			if (observationManager != null) {
				observationManager.removeEventListener(mapChangeListener);
				for (ContentChangeListener l : this.contentChangeListeners) {
					observationManager.removeEventListener(l);
				}
			}
		} catch (RepositoryException e) {
			logger.warn("error removing event listener");
		} finally {
			if (session != null) {
				session.logout();
				session = null;
			}
		}
	}

	/**
	 * Updates the state of this OSGi component when the ComponentContext has
	 * changed.
	 */
	@Modified
	protected void modified(ComponentContext ctx) {
		Dictionary props = ctx.getProperties();
		internalDeactivate();
		try {
			internalActivate(props);
		} catch (UrlMapperConfigException e) {
			String pid = (String) props.get(Constants.SERVICE_PID);
			ctx.disableComponent(pid);
			logger.error("disabling {} due to error during activation", pid);
			// rethrowing since there's no other way to signal failure of this
			// method;
			throw new ComponentException(
					"disabling service due to activation failure", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String map(String path) {
		ResourceResolver resourceResolver = getAdminResourceResolver();
		String retVal = path;
		if (resourceResolver != null) {
			retVal = map(resourceResolver, null, path);
			resourceResolver.close();
		}
		return retVal;
	}

	/**
	 * {@inheritDoc}
	 */
	public String map(HttpServletRequest request, String path) {
		String retVal = path;
		ResourceResolver resourceResolver = null;
		boolean closeResolver = false;

		if (request != null && request instanceof SlingHttpServletRequest) {
			resourceResolver = ((SlingHttpServletRequest) request)
					.getResourceResolver();
		} else {
			resourceResolver = getAdminResourceResolver();
			closeResolver = true;
		}

		if (resourceResolver != null) {
			retVal = map(resourceResolver, request, path);
			if (closeResolver) {
				resourceResolver.close();
			}
		}

		return retVal;
	}

	/**
	 * {@inheritDoc}
	 */
	
/*	Original mapmapCCTV(ResourceResolver resourceResolver,HttpServletRequest request, String path)
 *  for CCTV
 *  
	public String mapCCTV(ResourceResolver resourceResolver,
			HttpServletRequest request, String path) {
		String mappedPath = path;
		String retVal = null;

		if (resourceResolver == null) {
			throw new IllegalArgumentException(
					"resource resolver needed for mapping");
		}

		if (request == null && !path.startsWith("/content")) {
			throw new IllegalArgumentException(
					"with no request, path must be absolute from content root");
		}

		Matcher m = bucketedPathPattern.matcher(path);

		if (m.find()) {
			String context = m.group(1) != null ? m.group(1) : "";
			// Special case: strip "no-chef" out of recipe path.

			mappedPath = m.replaceFirst(context
					+ (m.group(2).equals("/recipes/no-chef") ? "/recipes" : m
							.group(2)) + m.group(3));
						
			logger.debug("replaced {} with {} before mapping", path, mappedPath);
		} else {
			mappedPath = path;
		}
		
		retVal = resourceResolver.map(request, mappedPath);
		
		
		logger.debug("map() returning {} for {}", retVal, path);
		return retVal;
	}
*/		
	
	/* FNR version the map() method
	 * (non-Javadoc)
	 * @see com.scrippsnetworks.wcm.url.UrlMapper#map(org.apache.sling.api.resource.ResourceResolver, javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	
	public String map(ResourceResolver resourceResolver,
			HttpServletRequest request, String path) {
		String mappedPath = path;
		String retVal = null;

		if (resourceResolver == null) {
			throw new IllegalArgumentException(
					"resource resolver needed for mapping");
		}

		if (request == null && !path.startsWith("/content")) {
			throw new IllegalArgumentException(
					"with no request, path must be absolute from content root");
		}

		mappedPath=PathHelper.unBucketPath(path);
		
		retVal = resourceResolver.map(request, mappedPath);
		
		
		logger.debug("map() returning {} for {}", retVal, path);
		return retVal;
	}

	/*
	 * public String resolve(String path) { ResourceResolver resourceResolver =
	 * getAdminResourceResolver(); String retVal = path; if (resourceResolver !=
	 * null) { retVal = resolve(resourceResolver, null, path);
	 * resourceResolver.close(); } return retVal; }
	 * 
	 * public String resolve(HttpServletRequest request, String path) {
	 * ResourceResolver resourceResolver = null; boolean closeResolver = false;
	 * String retVal = path;
	 * 
	 * if (request != null && request instanceof SlingHttpServletRequest) {
	 * resourceResolver =
	 * ((SlingHttpServletRequest)request).getResourceResolver(); } else {
	 * resourceResolver = getAdminResourceResolver(); closeResolver = true; }
	 * 
	 * if (resourceResolver != null) { retVal = resolve(resourceResolver,
	 * request, path); if (closeResolver == true) { resourceResolver.close(); }
	 * }
	 * 
	 * return retVal; }
	 * 
	 * 
	 * public String resolve(ResourceResolver resourceResolver,
	 * HttpServletRequest request, String path) { return
	 * internalResolve(resourceResolver, request, path, true); }
	 */

	/**
	 * {@inheritDoc}
	 */
	public String resolvePath(HttpServletRequest request, String path) {
		ResourceResolver resourceResolver = null;
		boolean closeResolver = false;
		String retVal = null; // In the forwarding case, return null unless we
								// have something to do.

		if (request != null && request instanceof SlingHttpServletRequest) {
			resourceResolver = ((SlingHttpServletRequest) request)
					.getResourceResolver();
		} else {
			resourceResolver = getAdminResourceResolver();
			closeResolver = true;
		}

		if (resourceResolver != null) {
			try {
				retVal = internalResolve(resourceResolver, request, path);
			} finally {
				if (closeResolver == true) {
					resourceResolver.close();
				}
			}
		}

		return retVal;
	}

	/**
	 * {@inheritDoc}
	 */
	public String resolvePath(ResourceResolver resourceResolver,
			HttpServletRequest request, String path) {
		String retVal = null; // In the forwarding case, return null unless we
								// have something to do.

		if (resourceResolver != null) {
						
			retVal = internalResolve(resourceResolver, request, path);						
		}

		return retVal;
	}

	/** The pattern used to match resolveable paths. */
	
	/*
	public static final Pattern requestFNRPathPattern = Pattern
			.compile("/recipes/*.*|/how-tos/*.*|/chefs/*.*|/hosts/*.*|/shows/*.*|/magazine/*.*|/restaurants/*.*|/grilling/*.*|/holidays/*.*"
					+ "|/healthy/*.*|/in-season-now/*.*|/super-bowl/*.*|/thanksgiving/*.*|/features/*.*|/site/*.*|/sponsored/*.*|/search/*.*|/topics/*.*|/videos/*.*");
    */
	public static final Pattern requestFNRPathPattern = Pattern
        .compile("/recipes/*.*|/how-tos/*.*|/how-to/*.*|/chefs/*.*|/hosts/*.*|/shows/*.*|/magazine/*.*|/restaurants/*.*|/grilling/*.*|/holidays/*.*"
            + "|/holidays-and-parties/*.*|/quick-and-easy/*.*"
            + "|/healthy/*.*|/in-season-now/*.*|/big-game/*.*|/thanksgiving/*.*|/features/*.*|/site/*.*|/sponsored/*.*|/search/*.*|/topics/*.*|/videos/*.*");

	public String internalResolve(ResourceResolver resourceResolver,
			HttpServletRequest request, String path) {

		logger.debug("resolve() {}", path);
		String retVal = null;
		long startTime = System.currentTimeMillis();
		long endTime = 0;
		
				
		/* ToDo for caching
		 * 
		 * This is how the caching of the resolved paths could be added
		 * 
		String cachedPath = resolvedPaths.get(path);			
		if (cachedPath!=null)
		{
			retVal=cachedPath;
			return retVal;
		}
		
		When ever a path is resolved it would be added to the cache like this
		resolvedPaths.put(path, retval);
		*/	

		String matchPath;
	    String rootPath = "";

		if (path == null) {
			matchPath = "/";
		} else if (path.startsWith("/")) {
			matchPath = path;
		} else {
			matchPath = "/" + path;
		}

		Matcher m = requestFNRPathPattern.matcher(matchPath);
		if (m.find()) {
			
			if (resourceResolver == null) {
				throw new IllegalArgumentException(
						"resource resolver is required for resolution");
			}

			if (request == null && !path.startsWith("/content")) {
				throw new IllegalArgumentException(
						"with no request, path must be absolute from content root");
			}

			if (!path.startsWith("/content/food/")) {
				rootPath = "/content/food";
			}

			if ((rootPath+path).startsWith("/content")) {

				retVal = null;
				PathHelper pObj = new PathHelper(rootPath + path);

				// Check to see if the path node exists in the CRX (take of the
				// extension .html or what ever)

				String oneLetterBucketPath = pObj.getOneLetterBucketPath();
				String twoLetterBucketPath = pObj.getTwoLetterBucketPath();
				String threeLetterBucketPath = pObj.getThreeLetterBucketPath();
				String fourLetterBucketPath = pObj.getFourLetterBucketPath();

				if (pObj.getContext().matches("recipes")) {

					// Check to see the Chef node exists
					if (resourceResolver.getResource(pObj.getRecipeChefNode()) == null) {
						
						//No Chef Node
						
						oneLetterBucketPath = pObj.getNochefOneLetterBucketPath();
						twoLetterBucketPath = pObj.getNochefTwoLetterBucketPath();
						threeLetterBucketPath = pObj.getNoChefThreeLetterBucketPath();
						fourLetterBucketPath = pObj.getNoChefFourLetterBucketPath();					
						
						oneLetterBucketPath = oneLetterBucketPath.replace("/recipes/", "/recipes/no-chef/");
						twoLetterBucketPath = twoLetterBucketPath.replace("/recipes/", "/recipes/no-chef/");
						threeLetterBucketPath = threeLetterBucketPath.replace("/recipes/", "/recipes/no-chef/");
						fourLetterBucketPath = fourLetterBucketPath.replace("/recipes/", "/recipes/no-chef/");
					}

				}

				if (resourceResolver.getResource(pObj.getCQPath()) != null) {
					logger.debug("Bingo Resolved direct path=" + rootPath + path);
					retVal = rootPath + path;

				} else if (resourceResolver.getResource(PathHelper
						.getCQPathHTML(oneLetterBucketPath)) != null) {
					// Check if level 1 exists
					logger.debug("Bingo Resolved level 1 path="
							+ PathHelper.getCQPathHTML(oneLetterBucketPath));
					retVal = oneLetterBucketPath;

				} 
				else if (resourceResolver.getResource(PathHelper
						.getCQPathHTML(twoLetterBucketPath)) != null) {

					// Check if level 2 exists
					logger.debug("Bingo Resolved level 2 path="
							+ PathHelper.getCQPathHTML(twoLetterBucketPath));
					retVal = twoLetterBucketPath;

				} else if (resourceResolver.getResource(PathHelper
						.getCQPathHTML(threeLetterBucketPath)) != null) {
					// Check if level 3 exists
					logger.debug("Bingo Resolved level 3 path="
							+ PathHelper.getCQPathHTML(threeLetterBucketPath));
					retVal = threeLetterBucketPath;

				}
				else if (resourceResolver.getResource(PathHelper
						.getCQPathHTML(fourLetterBucketPath)) != null) {
					// Check if level 4 exists
					logger.debug("Bingo Resolved level 4 path="
							+ PathHelper.getCQPathHTML(fourLetterBucketPath));
					retVal = fourLetterBucketPath;

				}

			}

		}

		else {
			logger.debug("{} did not match requestFNRPathPattern", matchPath);
		}

		endTime = System.currentTimeMillis();

        if (retVal != null && retVal.equals(rootPath + path)) {
            logger.debug("resolved path same as original. Deferring resolution.");
            retVal = null;
        } else {
            logger.debug("resolve() returning {} in {} ms", retVal,
                    String.valueOf(endTime - startTime));
        }

		return retVal;

	}

	/**
	 * Gets an admin resource resolver from the resource resolver factory.
	 */
	private ResourceResolver getAdminResourceResolver() {
		ResourceResolver resourceResolver = null;
		if (this.resourceResolverFactory != null) {
			try {
				resourceResolver = this.resourceResolverFactory
						.getAdministrativeResourceResolver(null);
			} catch (LoginException e) {
				logger.warn("error getting resource resolver: {}",
						e.getMessage());
			}
		} else {
			logger.warn("no resource resolver factory available");
		}
		return resourceResolver;
	}

	/**
	 * Use the resource resolver to map the section path to a full JCR path and
	 * cache the path.
	 * 
	 * This is intended to be used for section-level paths. The result is cached
	 * in a hash map so that subsequent requests don't have to use the resource
	 * resolver. This is acceptable for the small number of section-level paths
	 * (three deep in the JCR hierarchy, /content/&lt;site&gt;/&lt;section&gt;).
	 * 
	 * @param resourceResolver
	 *            The ResourceResolver to use. If it is null, a resource
	 *            resolver will be acquired from the request (if it's a
	 *            SlingHttpServletRequest) or an administrative resource
	 *            resolver will be used.
	 * @param request
	 *            The request to use in mapping paths. Details about the host in
	 *            the request are used to locate the proper resource resolver
	 *            map entry.
	 * @param path
	 *            The path to map to a real JCR path.
	 * @return String The absolute JCR path as determined by the
	 *         ResourceResolver map.
	 */
	private String getRootPath(ResourceResolver resourceResolver,
			HttpServletRequest request, String path) {
		logger.debug("getRootPath() {}", path);
		String key;

		if (request != null) {
			key = request.getScheme()
					+ "/"
					+ request.getServerName()
					+ "."
					+ Integer.toString(request.getServerPort() < 0 ? (request
							.getScheme().equals("https") ? 443 : 80) : request
							.getServerPort()) + path;

			logger.debug("getRootPath() using key {}", key);
		} else {
			// We'll at least make things work with a null request, since the
			// sling docs say that request can be null
			// when calling resolve() and
			// "the implementation should use reasonable defaults". You probably
			// won't get
			// what you want though. Setting the key like this will at least
			// mean we cache these results.
			logger.debug("null request, using path {} as key", path);
			key = path;
		}

		if (!rootPaths.containsKey(key)) {
			// deferred this check till when we knew we'd need it
			if (resourceResolver != null) {
				Resource res = null;
				if (path.startsWith("/content")) {
					res = resourceResolver.getResource(path);
				} else {
					res = resourceResolver.resolve(request, path);
				}
				if (res != null && !ResourceUtil.isNonExistingResource(res)) {
					logger.debug("adding key {} path {} to root path cache",
							key, res.getPath());
					rootPaths.putIfAbsent(key, res.getPath());
				} else {
					logger.debug(
							"null or nonexisting resource for path {}, returning null",
							path);
				}
			} else {
				throw new IllegalArgumentException(
						"getRootPath(): resource resolver must be nonnull");
			}
		} else {
			logger.debug("root path cache hit for key {}: {}", key,
					rootPaths.get(key));
		}

		return rootPaths.get(key);
	}

	/** Clears the cache of mapped section-level paths */
	protected void clearRootPaths() {
		logger.debug("clearing root paths");
		this.rootPaths.clear();
	}

	/** Clears the cache of resolved paths */
	protected void clearResolvedPaths() {
		logger.debug("clearing resolved paths");
		this.resolvedPaths.clear();
	}

	/** Test if path contains node name more than once */
	private boolean nodeOnlyInTail(String path, String nodeName) {
		return path.indexOf(nodeName) == path.lastIndexOf(nodeName);
	}

}
