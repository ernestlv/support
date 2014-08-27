package com.scrippsnetworks.wcm.sitemap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.exception.ExceptionUtils;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyUnbounded;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.apache.sling.discovery.InstanceDescription;
import org.apache.sling.discovery.TopologyEvent;
import org.apache.sling.discovery.TopologyEventListener;
import org.apache.sling.discovery.TopologyView;
import org.apache.sling.settings.SlingSettingsService;

import org.osgi.service.component.ComponentContext;

import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationStatus;
import com.day.cq.replication.Replicator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.url.UrlMapper;

@Component(enabled=true, immediate=true, metatype=true,
    label="SNI WCM Sitemap Generator Service",
    description="Scheduled job that generates sitemap XML files for search engine optimization.")
@Service(value = {Runnable.class, TopologyEventListener.class})
@Property(name = "scheduler.expression")
public class SitemapGeneratorService implements Runnable, TopologyEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SitemapGeneratorService.class);
    private static final EnumSet<TopologyEvent.Type> TOPOLOGY_EVENTS =
        EnumSet.of(TopologyEvent.Type.TOPOLOGY_CHANGED, TopologyEvent.Type.TOPOLOGY_INIT);

    @Property(label = "Site Name", description = "Site brand code (e.g. food)")
    public static final String PROP_BRAND = "siteName";
    private String brand;

    @Property(label = "Excluded Paths", description = "Paths to exclude from index", unbounded=PropertyUnbounded.ARRAY)
    public static final String PROP_EXCLUDED_PATHS = "excludedPaths";
    private String[] excludedPaths;

    @Property(label = "Indexable Paths", description = "Paths to index", unbounded=PropertyUnbounded.ARRAY)
    public static final String PROP_INDEXABLE_PATHS = "indexablePaths";
    private String[] indexablePaths;

    @Property(label = "Sitemap Path", description = "Path to store site maps", value = "/etc/sitemaps/food")
    public static final String PROP_SITEMAP_PATH = "sitemapPath";
    private String sitemapPath;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private UrlMapper urlMapper;

    @Reference
    private Replicator replicator;

    @Reference
    private SlingSettingsService slingSettingsService;

    private boolean isActive = false;
    private boolean isLeader = false;
    private Set<String> runModes;

    private ResourceResolver resourceResolver;
    private Session session;
    private String sitemapWorkingPath;

    @Activate
    protected void activate(ComponentContext componentContext) {
        configure(componentContext.getProperties());

        sitemapWorkingPath = sitemapPath + ".tmp";

        runModes = slingSettingsService.getRunModes();
        if (runModes.contains("author")) {
            isActive = true;
        }
    }

    @Deactivate
    protected void deactivate(final ComponentContext ctx) throws InterruptedException {
        isActive = false;
    }

    @Override
    public void handleTopologyEvent(final TopologyEvent topoEvent) {
        LOGGER.info("Topology event received {}.", topoEvent.getType());
        if (TOPOLOGY_EVENTS.contains(topoEvent.getType())) {
            TopologyView topoView = topoEvent.getNewView();
            if (topoView != null) {
                InstanceDescription instance = topoView.getLocalInstance();
                isLeader = instance.isLeader();
                LOGGER.info("Instance {} running as leader? {}", instance.getSlingId(), isLeader);
            }
        }
    }

    protected void configure(Dictionary<?, ?> properties) {
        this.brand = OsgiUtil.toString(properties.get(PROP_BRAND), null);
        this.excludedPaths = OsgiUtil.toStringArray(properties.get(PROP_EXCLUDED_PATHS));
        if (this.excludedPaths == null) {
            this.excludedPaths = new String[0];
        }
        this.indexablePaths = OsgiUtil.toStringArray(properties.get(PROP_INDEXABLE_PATHS));
        if (this.indexablePaths == null) {
            this.indexablePaths = new String[0];
        }
        this.sitemapPath = OsgiUtil.toString(properties.get(PROP_SITEMAP_PATH), null);
    }

    @Override
    public void run() {
        if (isActive && isLeader) {
            LOGGER.info("Scheduled sitemap generation task starting on cluster leader.");
            try {
                resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
                if (resourceResolver != null) {
                    session = resourceResolver.adaptTo(Session.class);
                    if (session != null) {
                        generateSitemaps();
                        session.logout();
                    } else {
                        LOGGER.error("Could not adapt resource resolver to a session.");
                    }
                    resourceResolver.close();
                }
            } catch (LoginException le) {
                LOGGER.error("Could not get a resource resolver.", le);
            }
        } else {
            LOGGER.info("Service deactivated or instance is not the cluster leader.");
        }
    }

    /**
     * Real work happens here.
     */
    public void generateSitemaps() {
        boolean mapBuilt = false;
        boolean slateCleaned = false;

        List<String> excludeList = Arrays.asList(excludedPaths);
        List<String> pathList = new ArrayList<String>();
        List<Sitemap> sitemaps = new ArrayList<Sitemap>();

        Sitemap sitemap = null;
        SitemapIndex sitemapIndex = null;

        String indexName = "";
        String mapName = "";
        String pathNode = "";
        String[] pathParts;

        try {
            slateCleaned = createDestination(sitemapWorkingPath);

            if (slateCleaned) {
                for (String pathToIndex : indexablePaths) {
                    LOGGER.info("Generating map for {}", pathToIndex);

                    pathParts = pathToIndex.split("/");
                    if (pathParts.length > 0) {
                        pathNode = pathParts[pathParts.length-1];
                    }

                    pathList.clear();
                    pathList.add(pathToIndex);
                    mapName = String.format("%s_sitemap_%s", brand, pathNode);

                    sitemap = new SitemapFactory()
                        .withPaths(pathList)
                        .withExcludedPaths(excludeList)
                        .withName(mapName)
                        .withDestinationPath(sitemapWorkingPath)
                        .withResolver(resourceResolver)
                        .withReplicator(replicator)
                        .withBrand(brand)
                        .build();

                    if (sitemap != null) {
                        mapBuilt = sitemap.generate();
                        if (mapBuilt) {
                            sitemaps.add(sitemap);
                        }
                    }
                }

                if (sitemaps.size() > 0) {
                    LOGGER.info("Generating index");
                    indexName = String.format("%s_sitemap_index", brand);
                    sitemapIndex = new SitemapIndexFactory()
                        .withSitemaps(sitemaps)
                        .withName(indexName)
                        .withDestinationPath(sitemapWorkingPath)
                        .withResolver(resourceResolver)
                        .build();

                    if (sitemapIndex != null) {
                        mapBuilt = sitemapIndex.generate();
                        if (mapBuilt && cleanDestination(sitemapPath)) {
                            session.move(sitemapWorkingPath, sitemapPath);
                            for (Sitemap map : sitemaps) {
                                activateContent(map.getPaths());
                            }
                            activateContent(sitemapIndex.getPath());
                        } 
                    }
                } else {
                    LOGGER.warn("No sitemaps generated.");
                    cleanDestination(sitemapWorkingPath);
                }
            } else {
                LOGGER.error("Sitemap destination unavailable; sitemaps not generated.");
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred during sitemap generation.", e);
        }

        LOGGER.info("Scheduled sitemap generation task completed.");
    }

    private boolean cleanDestination(String path) {
        boolean pathIsReady = false;

        try {
            Resource sitemapPathResource = resourceResolver.getResource(path);
            if (sitemapPathResource != null) {
                replicator.replicate(session, ReplicationActionType.DELETE, path);
                sitemapPathResource.adaptTo(Node.class).remove();
                session.save();
            }
            pathIsReady = true;
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }

        return pathIsReady;
    }

    private boolean createDestination(String path) {
        boolean pathIsReady = false;

        if (cleanDestination(path)) {
            try {
                Node sitemapNode = JcrUtil.createPath(path, "sling:Folder", session);
                session.save();
                if (sitemapNode != null) {
                    pathIsReady = true;
                }
            } catch (Exception e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
        }

        return pathIsReady;
    }

    /**
     * Activate the given path and log errors;
     * Replication errors are not fatal, allowing manual intervention
     * without incurring the expense of regenerating the content.
     */
    private void activateContent(List<String> resourcePaths) {
        for (String resourcePath : resourcePaths) {
            activateContent(resourcePath);
        }
    }

    private void activateContent(String resourcePath) {
        String replicatePath = resourcePath.replace(sitemapWorkingPath, sitemapPath);

        LOGGER.info("Activating {}", replicatePath);
        try {
            replicator.replicate(session, ReplicationActionType.ACTIVATE, replicatePath);
        } catch (ReplicationException re) {
            LOGGER.error(ExceptionUtils.getStackTrace(re));
        }
    }
}
