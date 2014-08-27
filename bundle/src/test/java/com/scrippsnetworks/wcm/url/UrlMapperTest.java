package com.scrippsnetworks.wcm.url;

import com.scrippsnetworks.wcm.SlingRemoteTest;

import java.net.URI;
import javax.jcr.Node;
import javax.jcr.Item;
import javax.jcr.Session;
import javax.jcr.Repository;
import javax.jcr.SimpleCredentials;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.util.TraversingItemVisitor;
import javax.jcr.util.TraversingItemVisitor.Default;
import javax.servlet.http.HttpServletRequest;

import java.util.regex.Matcher;
import java.util.Arrays;
import java.util.List;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.jcr.api.SlingRepository;
import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.day.cq.rewriter.linkchecker.Link;
import com.day.cq.rewriter.linkchecker.LinkValidity;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Page;

import com.scrippsnetworks.wcm.url.impl.PathHelper;
import com.scrippsnetworks.wcm.url.impl.UrlMapperImpl;
import com.scrippsnetworks.wcm.url.impl.SniRequestLinkChecker;
import com.scrippsnetworks.wcm.url.impl.LinkImpl;
import com.day.cq.rewriter.linkchecker.LinkCheckerSettings;

import org.apache.sling.commons.testing.sling.MockSlingHttpServletRequest;

public class UrlMapperTest extends SlingRemoteTest {

    private Logger log = LoggerFactory.getLogger(UrlMapperTest.class);
    public static final UrlMapper urlMapper = new UrlMapperImpl();

    public static final String pathsToMap[]={
		"/content/food/shows/f/fat-chef/fat-chef",
		"/content/food/shows/articles/a/about-the-show-the-great-food-truck-race-season-2",
		"/content/food/shows/photos/h/how-sweet-it-is",
		"/content/food/shows/5/5-ingredient-fix/recipes",
		"/content/food/videos/a/a-/a-b/a-ba/a-bakers-kitchen-94162",
		"/content/food/videos/channels/p/paula-deen",
		"/content/food/recipes/articles/a/a-brief-history-of-grilling",
		"/content/food/recipes/menus/b/bobby-flays-thanksgiving-menu",
		"/content/food/recipes/alton-brown/d/de/dee/deep/deep-fried-turkey-recipe/deep-fried-turkey2",
		"/content/food/recipes/no-chef/g/gr/gri/gril/grilled-fruit-salad-with-honey-yogurt-dressing-recipe",
		"/content/food/recipes/no-chef/g/gr/gri/gril/grilled-fruit-salad-with-honey-yogurt-dressing-recipe/grilled-fruit-salad",
		"/content/food/search-terms/4-word/25/a/ar/aru/arugula-goat-cheese-salad",
		"/content/food/healthy/articles/a/a-vegetarian-pantry",
		"/content/food/thanksgiving/packages/t/thanksgiving",
		"/content/food/grilling/articles/g/Grill-It-Top-5-Cocktails",
		"/content/food/restaurants/ca/los-angeles/t/the-golden-state-restaurant"				
		};    
    
	public static final String mappedPaths[]={
		"/content/food/shows/fat-chef/fat-chef",
		"/content/food/shows/articles/about-the-show-the-great-food-truck-race-season-2",
		"/content/food/shows/photos/how-sweet-it-is",
		"/content/food/shows/5-ingredient-fix/recipes",
		"/content/food/videos/a-bakers-kitchen-94162",
		"/content/food/videos/channels/paula-deen",
		"/content/food/recipes/articles/a-brief-history-of-grilling",
		"/content/food/recipes/menus/bobby-flays-thanksgiving-menu",
		"/content/food/recipes/alton-brown/deep-fried-turkey-recipe/deep-fried-turkey2",
		"/content/food/recipes/grilled-fruit-salad-with-honey-yogurt-dressing-recipe",
		"/content/food/recipes/grilled-fruit-salad-with-honey-yogurt-dressing-recipe/grilled-fruit-salad",
		"/content/food/search-terms/4-word/25/arugula-goat-cheese-salad",
		"/content/food/healthy/articles/a-vegetarian-pantry",
		"/content/food/thanksgiving/packages/thanksgiving",
		"/content/food/grilling/articles/Grill-It-Top-5-Cocktails",
		"/content/food/restaurants/ca/los-angeles/the-golden-state-restaurant"					
		}; 
		
	public static final String pathsToResolve[]={
		"/content/food/shows/fat-chef/fat-chef.html",
		"/content/food/shows/articles/about-the-show-the-great-food-truck-race-season-2.html",
		"/content/food/shows/photos/how-sweet-it-is.html",
		"/content/food/shows/5-ingredient-fix/recipes.html",
		"/content/food/videos/a-bakers-kitchen-94162.html",
		"/content/food/videos/channels/paula-deen.html",
		"/content/food/recipes/articles/a-brief-history-of-grilling.html",
		"/content/food/recipes/menus/bobby-flays-thanksgiving-menu.html",
		"/content/food/recipes/alton-brown/deep-fried-turkey-recipe/deep-fried-turkey2.html",
		"/content/food/recipes/grilled-fruit-salad-with-honey-yogurt-dressing-recipe.html",
		"/content/food/recipes/grilled-fruit-salad-with-honey-yogurt-dressing-recipe/grilled-fruit-salad.html",
		"/content/food/search-terms/4-word/25/arugula-goat-cheese-salad.html",
		"/content/food/healthy/articles/a-vegetarian-pantry.html",
		"/content/food/thanksgiving/packages/thanksgiving.html",
		"/content/food/grilling/articles/Grill-It-Top-5-Cocktails.html",
		"/content/food/restaurants/ca/los-angeles/the-golden-state-restaurant.html",
		"/content/food/shows/articles/shooting-baskets-behind-the-scenes-of-chopped/chopped-behind-the-scenes-photos.html",
        "/content/food/shows/sandwich-king/sandwich-king/_jcr_content/content-well/video-grid"
		};	
	   
	public static final String resolvedPaths[]={
		"/content/food/shows/f/fat-chef/fat-chef.html",
		"/content/food/shows/articles/a/about-the-show-the-great-food-truck-race-season-2.html",
		"/content/food/shows/photos/h/how-sweet-it-is.html",
		"/content/food/shows/5/5-ingredient-fix/recipes.html",
		"/content/food/videos/a/a-/a-b/a-ba/a-bakers-kitchen-94162.html",
		"/content/food/videos/channels/p/paula-deen.html",
		"/content/food/recipes/articles/a/a-brief-history-of-grilling.html",
		"/content/food/recipes/menus/b/bobby-flays-thanksgiving-menu.html",
		"/content/food/recipes/alton-brown/d/de/dee/deep/deep-fried-turkey-recipe/deep-fried-turkey2.html",
		"/content/food/recipes/no-chef/g/gr/gri/gril/grilled-fruit-salad-with-honey-yogurt-dressing-recipe.html",
		"/content/food/recipes/no-chef/g/gr/gri/gril/grilled-fruit-salad-with-honey-yogurt-dressing-recipe/grilled-fruit-salad.html",
		"/content/food/search-terms/4-word/25/a/ar/aru/arugula-goat-cheese-salad.html",
		"/content/food/healthy/articles/a/a-vegetarian-pantry.html",
		"/content/food/thanksgiving/packages/t/thanksgiving.html",
		"/content/food/grilling/articles/g/Grill-It-Top-5-Cocktails.html",
		"/content/food/restaurants/ca/los-angeles/t/the-golden-state-restaurant.html",
		"/content/food/shows/articles/s/shooting-baskets-behind-the-scenes-of-chopped/chopped-behind-the-scenes-photos.html",
        "/content/food/shows/s/sandwich-king/sandwich-king/jcr:content/content-well/video-grid"
		};      
    

    // Nodes existing directly under sections affected by bucketing.
    // These nodes should not be bucketed, but the only way to know is to see if they exist.
    // For the test to work, then, these nodes should exist.
    private static String[] directlyUnderSection = {
        "/content/cook/shows/shows-a-z",
        "/content/cook/shows/shows-a-z.html",
        "/content/cook/shows/shows-a-z.selector.html",
        "/content/cook/recipes/recipes-a-z",
        "/content/cook/recipes/recipes-a-z.html",
        "/content/cook/recipes/recipes-a-z.selector.html",
        "/content/cook/recipes/test-article.html",
        "/content/cook/topics/a-z",
        "/content/cook/topics/a-z.html",
        "/content/cook/topics/a-z.selector.html",
        "/content/cook/shows/jcr:content/content-well-top/parsys/",
    };


    private static String[] nonResolvingPaths = {
        "/content/cook/site/articles/t/testarticle.html"
    };

    
    public class BidirectionalMappingWalker extends TraversingItemVisitor.Default {

        PrintWriter dupOut = null;
        List<String> typeBuckets = Arrays.asList("articles", "packages", "photo-galleries", "channels", "players");

        public BidirectionalMappingWalker() {
            super(false, -1);
            String dupLog = System.getProperty("urlmapper.test.dupLog");
            if (dupLog != null) {
                try {
                    dupOut = new PrintWriter(new FileWriter(dupLog));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        protected void finalize() {
            if (dupOut != null) {
                dupOut.close();
            }
        }

        public void logDup(String line) {
            if (dupOut != null) {
                dupOut.println(line);
                dupOut.flush();
            }
        }

        public void entering(Node node, int level) throws RepositoryException {

            String path = node.getPath();
            String resourceType  = null;
            if (node.getPrimaryNodeType().getName().equals("cq:Page")) {
                if (node.hasNode("jcr:content")) {
                    Node contentNode = node.getNode("jcr:content");
                    if (contentNode.hasProperty("sling:resourceType")) {
                        resourceType = contentNode.getProperty("sling:resourceType").getValue().getString();
                    }

                }
            }

            String mappedPath;
            String resolvedPath = null;
            URI pathURI;
            if (resourceType != null && !resourceType.contains("blank-structural-page")) {
                try {
                    mappedPath = urlMapper.map(resourceResolver, request, path);
                    log.debug("mapped {} to {}", path, mappedPath);

                    // Assumes you're using the resource resolver map. If you are, verifies it's being used on mapping.
                    // assertFalse("mappedPath " + mappedPath + " doesn't start with /content", mappedPath.startsWith("/content"));

                    // Not really necessary, since the path should come back relative using a nonnull request.
                    // However, this is safer if somehow that weren't the case.
                    pathURI = new URI(mappedPath);
                    resolvedPath = urlMapper.resolvePath(resourceResolver, request, pathURI.getPath());
                    log.debug("resolved {} to {}", mappedPath, resolvedPath);
                } catch (Throwable e) {
                    log.error("caught " + e.getClass().getSimpleName() + ": " + e.getMessage());
                    e.printStackTrace();
                    fail(e.getMessage());
                    return;
                }

                if (resolvedPath == null) {
                    if (mappedPath.equals(path)) {
                        log.warn("ignoring null resolving {}, path mapped to itself", pathURI.getPath());
                        return;
                    }
                    
                   // Node might exist directly at mappedPath too.
                   Node testExist = null;
                   try {
                       testExist = session.getNode(mappedPath);
                   } catch (PathNotFoundException e) {
                       testExist = null;
                   }

                   if (testExist != null) {
                       log.warn("ignoring null resolving {}, dup node exists at mapped path {}", pathURI.getPath(), testExist.getPath());
                       return;
                   }

                   String[] pathSplit = path.split("/");
                   String nodeName = pathSplit[pathSplit.length - 1];
                   String bucket = pathSplit[pathSplit.length - 2];
                   if (bucket.length() == 1 && bucket.charAt(0) != nodeName.charAt(0)) {
                       log.warn("node {} is in wrong bucket", path);
                       return;
                   }

                   fail("resolved path for " + pathURI.getPath() + " was null");

                   // let logic below handle this
                }

                boolean doassert = true;
                // This is touchy. Some conflicts are legitimate, you can't do anything about them. Don't fail for those.
                if (!resolvedPath.equals(path)) {
                    log.warn("doassert check {} != {}", path, resolvedPath);
                    String[] resolvedPathSplit = resolvedPath.split("/");
                    String[] pathSplit = path.split("/");


                    // The type paths have buckets, so they're 1 longer.
                    int offset = 1;
                    int bucketLength = 1;
                    boolean recipeMatch = false;
                    if (path.contains("cook/recipes/")) {
                        offset = -3;
                        if (resolvedPath.matches("/content/cook/recipes/.*/[^/]/[^/]{2}/[^/]{3}/[^/]{4}/.*")) {
                            recipeMatch = true; 
                            log.info("{} is a recipe path", recipeMatch);
                        }
                    } else if (path.contains("cook/videos/")) {
                        offset = -2;
                        bucketLength = 4;
                    }

                    // Make sure the number of path components is right for this kind of match.
                    if (resolvedPathSplit.length + offset == pathSplit.length || resolvedPathSplit.length == pathSplit.length) {
                        // Start at 1, since the paths start with slash
                        for (int i = 1; i < resolvedPathSplit.length && i < pathSplit.length; i++) {
                            if (resolvedPathSplit[i].equals(pathSplit[i])) {
                                continue;
                            }
                            log.info("first difference {} != {}", resolvedPathSplit[i], pathSplit[i]);
                            // since we prefer the section's main page type, the first different path component should be a bucket
                            if ((resolvedPathSplit[i].length() == bucketLength || resolvedPathSplit[i].equals("numbers") || recipeMatch || typeBuckets.contains(resolvedPathSplit[i])) && typeBuckets.contains(pathSplit[i])) {
                                log.warn("allowing {} != {} as legitimate", resolvedPath, path);
                                doassert = false;
                            } else {
                                log.info("{} and {} not a main type vs type bucket issue", resolvedPathSplit[i], pathSplit[i]);
                            }
                            break;
                        }
                    }

                    if (path.contains("/videos")) {
                        String testPath = path.replaceAll("/([0-9])/", "/0$1/");
                        String testResolvedPath = resolvedPath.replaceAll("/([0-9])/", "/0$1/");                        
                        if (testPath.compareTo(testResolvedPath) > 0) {
                            log.warn("allowing {} > {} as legitimate, prefer earlier", resolvedPath, path);
                            doassert = false;
                        } else {
                            log.warn("NOT allowing {} <= {} as legitimate, prefer later", resolvedPath, path);
                        }

                        // if (resolvedPathSplit.length - 2 == pathSplit.length) {
                        //     // Start at 1, since the paths start with slash
                        //     for (int i = 1; i < resolvedPathSplit.length && i < pathSplit.length; i++) {
                        //         if (resolvedPathSplit[i].equals(pathSplit[i])) {
                        //             continue;
                        //         }
                        //         // since we prefer the section's main page type, the first different path component should be a year
                        //         if (resolvedPathSplit[i].length() == 4 && Arrays.asList("articles", "packages", "photo-galleries", "channels", "players").contains(pathSplit[i])) {
                        //             log.warn("allowing {} != {} as legitimate", resolvedPath, path);
                        //             doassert = false;
                        //             break;
                        //         }
                        //     }

                        // }
                    }
                }

                if (doassert) {
                    assertTrue("resolvedPath " + resolvedPath + " = " + path, resolvedPath.equals(path));
                } else {
                    logDup(mappedPath + " " + path + " " + resolvedPath);
                }
            }
        }
    }
    
    @Test
    public void testTest() {
        log.debug("running test test");
        UrlMapper urlMapper = new UrlMapperImpl();
        return;
    }

    @Test
    public void testMapBucketPath() {
        log.info("running testMapBucketPath test");
        UrlMapper urlMapper = new UrlMapperImpl();
                
        for (int i = 0; i < pathsToMap.length; i++) {        	
        	String path = pathsToMap[i];
            log.info("trying {} against urlMapper.map(resourceResolver, request, path)", path);        	        	
            String mappedPath=urlMapper.map(resourceResolver, request, path);        	        	
            log.info("mapped path is {}", mappedPath);            
            log.info("Expected result is {}",mappedPaths[i]);
        	assertTrue(path+" =>" +mappedPath+",mappedPaths[i]="+mappedPaths[i],mappedPath.equals(mappedPaths[i]));
		}
        
    }

    @Test
    public void resolvedPathTest() {
        log.info("running resolvedPathTest");

        UrlMapper urlMapper = new UrlMapperImpl();
                       
        for (int i = 0; i < pathsToResolve.length; i++) {
       
        	String pathToResolve=pathsToResolve[i];
        	        	
        	String expectedResult= resolvedPaths[i];

            String resolvedPath=urlMapper.resolvePath(resourceResolver, request, pathToResolve);
            log.info(pathToResolve);            
            if (resolvedPath!=null)
            {
            	assertTrue("Path to resolve="+pathToResolve+", expected result="+expectedResult+", resolvedPath="+resolvedPath,resolvedPath.equals(expectedResult));
           
                log.info("trying {} against urlMapper.resolvePath", pathToResolve);        	         		
                log.info("resolved path is {}", resolvedPath);            
                log.info("Expected result is {}",expectedResult);              
            } else {
                log.warn("{} resolved to null expected {}", pathsToResolve[i], expectedResult);
            }
		}          
    }
    
    /*
    @Test
    public void SniRequestLinkCheckerTest() {
    	SniRequestLinkChecker s= new SniRequestLinkChecker();  
    	
    	MockSlingHttpServletRequest sreq = new MockSlingHttpServletRequest("/content/food/recipe", "", "html", "", "");
    	
    	
    	LinkCheckerSettings settings = LinkCheckerSettings.fromRequest((SlingHttpServletRequest) sreq);
    	     	    	     	 
    	Link link=s.getLink("/content/food/recipe.selector.html", settings);
    	
    	assertTrue("Link to check=/content/food/recipe.selector.html , expected result=VALID", link.getValidity()== LinkValidity.VALID);

    	
    	if (link.getValidity()== LinkValidity.VALID)
    	{
    		log.info("Link /content/food/recipe.selector.html is valid");
    	}
    	
    	Link link2=s.getLink("/content/food/recipe.9999.8888.html", settings);
    	
    	assertTrue("/content/food/recipe.9999.8888.html , expected result=VALID", link.getValidity()== LinkValidity.VALID);
    	    	
    	if (link2.getValidity()== LinkValidity.VALID)
    	{
    		log.info("Link /content/food/recipe.9999.8888.html is valid");
    	}    	
    	
    	    	
    	try {
    		Link link3=s.getLink("/content/food/recipe.html", null);  
        	assertTrue("/content/food/recipe.html , expected result=null", link3.getValidity()== null);
    		
		} catch (Exception e) {
			// TODO Auto-generated catch block

			log.info("Link /content/food/recipe.html is null");		
		}    	
    }
    */
}
