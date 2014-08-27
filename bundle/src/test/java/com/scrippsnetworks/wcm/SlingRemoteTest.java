package com.scrippsnetworks.wcm;

import java.lang.NoSuchFieldException;
import java.net.URI;
import javax.jcr.Session;
import javax.jcr.Repository;
import javax.jcr.SimpleCredentials;
import javax.jcr.NamespaceRegistry;
import javax.servlet.http.HttpServletRequest;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.BeforeClass;
import org.junit.Assume;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeTrue;
import junitx.util.PrivateAccessor;
import static org.mockito.Mockito.*; 
/*
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
*/

import org.apache.sling.api.resource.*;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.commons.testing.jcr.*;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.apache.sling.jcr.resource.internal.JcrResourceResolverFactoryImpl;
import org.apache.sling.jcr.resource.internal.helper.Mapping;
import org.apache.sling.jcr.resource.internal.helper.MapEntries;
import org.apache.sling.commons.testing.jcr.RepositoryUtil;

import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;

/** Superclass providing remote access to a CRX host.
 *
 * Base class for JUnit-driven (integration) tests that need to access a remote CRX repository. This class
 * provides an initialization procedure annotated with @BeforeClass so it is run automatically for you. If the
 * initialization fails, the subclasses tests are skipped (<em>not</em> failed). If it succeeds, intialized SlingRepository,
 * Session, ResourceResolver, and HTTPServletRequest objects will be provided as protected members.
 *
 * Test classes subclassing this base will not be enabled unless the property com.scrippsnetworks.wcm.slingRemoteTest = true.
 * Enable tests using this class by setting the property on the maven command line
 *
 * <code>mvn test -Dcom.scrippsnetworks.wcm.slingRemoteTest=true</code>
 *
 * This would run tests in all subclasses. To be more selective, use
 *
 * <code>mvn test -Dcom.scrippsnetworks.wcm.slingRemoteTest=true -Dtest=MyTestClass</code>
 *
 * or 
 *
 * <code>mvn test -Dcom.scrippsnetworks.wcm.slingRemoteTest=true -Dtest="MyTestClass#myTestMethod"</code>
 *
 * The default URI repository URI is http://localhost:4502/crx/server using credentials admin:admin. You can
 * override these values with the following properties:
 *
 * <ul>
 *  <li>crx.host</li>
 *  <li>crx.port</li>
 *  <li>crx.user</li>
 *  <li>crx.password</li>
 * </ul>
 *
 * For example
 *
 * <code>mvn test -Dcrx.host=foohost -Dcrx.port=1234 -Dcrx.user=me -Dcrx.password=t3h3l1t3</code>
 *
 */
public class SlingRemoteTest {

    public static final String DEFAULT_REPOSITORY_URI = "http://localhost:4502/crx/server";
    public static final String DEFAULT_CRX_HOST = "localhost";
    public static final String DEFAULT_CRX_PORT = "4502";
    public static final String DEFAULT_CRX_USER = "admin";
    public static final String DEFAULT_CRX_PASSWORD = "admin";

    protected static Logger log = LoggerFactory.getLogger(SlingRemoteTest.class);

    /** Sling repository object */
    protected static SlingRepository slingRepo;
    /** JCR Session object */
    protected static Session session;
    /** Sling resource resolver */
    protected static ResourceResolver resourceResolver;
    /** Servlet request object */
    protected static HttpServletRequest request;

    @BeforeClass
    public static void init() {
        log.info("initializing SlingRemoteTest");
        
        /*
        String slingRemoteTestFlag = System.getProperty("com.scrippsnetworks.wcm.slingRemoteTest", "false");
        assumeTrue((new Boolean(slingRemoteTestFlag)).booleanValue());
        */

        log.info("running SlingRemoteTest");


        String crxhost = System.getProperty("crx.host", DEFAULT_CRX_HOST);
        String crxport = System.getProperty("crx.port", DEFAULT_CRX_PORT);
        String crxuser = System.getProperty("crx.user", DEFAULT_CRX_USER);
        String crxpass = System.getProperty("crx.password", DEFAULT_CRX_PASSWORD);
        String repositoryURI = "http://" + crxhost + ":" + crxport + "/crx/server";

        log.info("using repository uri " + repositoryURI);

        try {
            Repository repository = JcrUtils.
                    getRepository(repositoryURI);
            log.info("got repository");
            SlingRepository slingRepo =
                    new RepositoryUtil.RepositoryWrapper(repository);
            // session = slingRepo.loginAdministrative(null);
            session = slingRepo.login(new SimpleCredentials(crxuser, crxpass.toCharArray()));

            JcrResourceResolverFactoryImpl resourceResolverFactory = new JcrResourceResolverFactoryImpl();
            log.info("got resource resolver factory, trying private access to factory properties");

            // Cool, but dicey. Justin Edelson sort of approved. :-)
            // http://svn.apache.org/repos/asf/sling/trunk/performance/jcr-resource-2.1.0/src/test/java/org/apache/sling/performance/PerformanceTest.java
            PrivateAccessor.setField(resourceResolverFactory, "repository", slingRepo);
            PrivateAccessor.setField(resourceResolverFactory, "mappings",
                    new Mapping[] { new Mapping("/-/"), new Mapping("/-/") });
            PrivateAccessor.setField(resourceResolverFactory, "mangleNamespacePrefixes", true);
            PrivateAccessor.setField(resourceResolverFactory, "mapRoot", "/etc/map");
            PrivateAccessor.setField(resourceResolverFactory, "useMultiWorkspaces", false);
            log.debug("set all properties but map entries, creating tracker");

            final EventAdmin mockVoidEA = new EventAdmin() {
                public void postEvent(Event event) {}
                public void sendEvent(Event event) {}
            };
            final ServiceTracker voidTracker = mock(ServiceTracker.class);
            when(voidTracker.getService()).thenReturn(mockVoidEA);

            log.debug("tracker created, creating mapEntryResolver");

            ResourceResolver mapEntryResolver = resourceResolverFactory.getResourceResolver(session);

            log.debug("mapEntryResolver created, creating spyFactory");

            JcrResourceResolverFactoryImpl spyFactory = spy(resourceResolverFactory);
            doReturn(mapEntryResolver).when(spyFactory).getAdministrativeResourceResolver(null);

            log.debug("spyFactory created");

            MapEntries mapEntries = new MapEntries(spyFactory, mock(BundleContext.class), voidTracker);
            PrivateAccessor.setField(resourceResolverFactory, "mapEntries", mapEntries);

            log.debug("mapEntries set on factory");

            /*
            try {
                NamespaceRegistry nsr = session.getWorkspace().getNamespaceRegistry();
                nsr.registerNamespace(SlingConstants.NAMESPACE_PREFIX, JcrResourceConstants.SLING_NAMESPACE_URI);
            } catch (Exception e) {
                log.warn("error registering namespace", e);
                // don't care for now
            }
            */

            log.debug("trying to get resource resolver");
            resourceResolver = resourceResolverFactory.getResourceResolver(session);
            // resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
            log.info("got resource resolver");

            request = mock(HttpServletRequest.class);
            when(request.getScheme()).thenReturn("http");
            when(request.getServerName()).thenReturn(crxhost);
            when(request.getServerPort()).thenReturn(Integer.valueOf(crxport));
        } catch (NoSuchFieldException e) {
            log.warn("Access to private internals has caught up with you!", e);
            assumeNoException(e);
            // throw new RuntimeException("error initializing sling remote test", e);
        } catch (Exception e) {
            log.warn("exception during repository setup", e);
            assumeNoException(e);
            // throw new RuntimeException("error initializing sling remote test", e);
        }

        log.info("initialization complete");
    }
}
