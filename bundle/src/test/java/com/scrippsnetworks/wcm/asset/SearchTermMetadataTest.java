package com.scrippsnetworks.wcm.asset;

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
import java.util.regex.Matcher;
import java.util.Arrays;
import java.util.List;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

import com.scrippsnetworks.wcm.asset.SearchTermMetadata;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.api.resource.Resource;
import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Page;


public class SearchTermMetadataTest extends SlingRemoteTest {

    private Logger log = LoggerFactory.getLogger(SearchTermMetadataTest.class);

    public class SearchTermTestWalker extends TraversingItemVisitor.Default {

        PrintWriter dupOut = null;
        List<String> typeBuckets = Arrays.asList("articles", "packages", "photo-galleries", "channels", "players");

        public SearchTermTestWalker() {
            super(false, -1);
        }

        public void entering(Node node, int level) throws RepositoryException {


            String path = node.getPath();
            String assetType  = null;
            Node contentNode = null;
            if (node.getPrimaryNodeType().getName().equals("cq:Page")) {
                if (node.hasNode("jcr:content")) {
                    contentNode = node.getNode("jcr:content");
                    if (contentNode.hasProperty("sni:assetType")) {
                        assetType = contentNode.getProperty("sni:assetType").getValue().getString();
                    }
                }
            }

            if (assetType == null || contentNode == null || ! "SEARCH_TERM".equals(assetType)) {
                return;
            }

            if (!contentNode.hasProperty("jcr:title")) {
                log.warn("{} has no title", node.getPath());
                return;
            }

            String searchTerm = contentNode.getProperty("jcr:title").getValue().getString();
            log.info("using search term {}", searchTerm);
            SearchTermMetadata stMd = SearchTermMetadata.getSearchTermMetadata("cook", searchTerm, resourceResolver);

            assertNotNull("test SearchTermMetdata is not null", stMd);

            if (! node.getPath().equals(stMd.getSearchTermPath()) && node.getPath().contains(stMd.getSearchTermPath())) {
                log.warn("started with {} got {}, possible duplicate", node.getPath(), stMd.getSearchTermPath());
                return;
            }

            assertEquals("test path to search term page is correct", node.getPath(), stMd.getSearchTermPath());
            log.info("path {} search term (jcr:title) {}", node.getPath(), searchTerm);
        }
    }

/*
    @Test
    public void testTest() {
        log.debug("running test test");
        SearchTermTestWalker walker = new SearchTermTestWalker();
        return;
    }

    @Test
    public void walkerTest() {
        Item item;
        SearchTermTestWalker walker = new SearchTermTestWalker();
        String walkerRootStr = System.getProperty("searchTermMetadata.test.walkerRoot", "/content/cook/search-terms");
        String[] walkerRoots = walkerRootStr.split(",");
        for (String walkerRoot : walkerRoots) {
            try {
                item = session.getNode(walkerRoot);
                item.accept(walker);
            } catch (PathNotFoundException e) {
                log.error("encountered PathNotFoundException {}", e.getMessage(), e);
                fail("path not found exception");
            } catch (RepositoryException e) {
                log.error("encountered RepositoryException {}", e.getMessage(), e);
                fail("repository exception");
            }
        }
        return;
    }
*/
}
